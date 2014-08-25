package com.eslint;

import com.eslint.fixes.Fixes;
import com.eslint.settings.ESLintSettingsPage;
import com.eslint.utils.PsiUtil;
import com.google.common.base.Joiner;
import com.intellij.codeInspection.*;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Platform;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ex.SingleConfigurableEditor;
import com.intellij.openapi.options.newEditor.OptionsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.containers.ContainerUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ESLintInspection extends PropertySuppressableInspectionBase { // TODO this should just a be a LocalInspectionTool suppression needs other logic than what is in PropertySuppressableInspectionBase

    private static final Logger LOG = Logger.getInstance(ESLintBundle.LOG_ID);

    @NotNull
    public String getDisplayName() {
        return ESLintBundle.message("eslint.property.inspection.display.name");
    }

    @NotNull
    public String getShortName() {
        return "ESLintInspection";
    }

    private GeneralCommandLine getCommandLine(JSFileImpl file, ESLintProjectComponent component) {
        List<String> command = new ArrayList<String>();
        command.add(component.nodeInterpreter);
        command.add(component.eslintExecutable);
        if (StringUtils.isNotEmpty(component.eslintRcFile)) {
            command.add("-c");
            command.add(component.eslintRcFile);
        }
        if (StringUtils.isNotEmpty(component.rulesPath)) {
            command.add("--rulesdir");
            command.add(new StringBuilder("['").append(component.rulesPath).append("']").toString());
        }
        command.add(file.getVirtualFile().getPath());
        GeneralCommandLine commandLine = new GeneralCommandLine(command);
        commandLine.setWorkDirectory(file.getProject().getBasePath());
        return commandLine;
    }

    private static final Pattern pattern = Pattern.compile("^\\s+(\\d+):(\\d+)\\s+(\\S+)\\s+(.+)\\s+(\\S+)$");

    public ProblemDescriptor[] checkFile(@NotNull final PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        try {
            if (!(file instanceof JSFileImpl)) return null;
            ESLintProjectComponent component = file.getProject().getComponent(ESLintProjectComponent.class);
            if (!component.isSettingsValid() || !component.isEnabled()) {
                return null;
            }

            final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
            if (document == null) {
                component.showInfoNotification("Error running ESLint inspection: Could not get document for file " + file.getName(), NotificationType.WARNING);
                return null;
            }

            final ProblemsHolder problemsHolder = new ProblemsHolder(manager, file, isOnTheFly);
            final GeneralCommandLine commandLine = getCommandLine((JSFileImpl)file, component);
            final StringBuilder err = new StringBuilder();
            try {
                Process process = commandLine.createProcess();
                ProcessHandler processHandler = new OSProcessHandler(process, commandLine.getCommandLineString());
                processHandler.addProcessListener(new ProcessAdapter() {
                    @Override
                    public void onTextAvailable(ProcessEvent event, Key outputType) {
                        if (outputType == ProcessOutputTypes.STDERR) {
                             err.append(event.getText());
                        }
                        else if (outputType == ProcessOutputTypes.SYSTEM) {
                             // skip
                        }
                        else {
                            Matcher matcher = pattern.matcher(event.getText());
                            if (matcher.find()) {
                                int line = Integer.parseInt(matcher.group(1));
                                int column = Integer.parseInt(matcher.group(2));
                                String level = matcher.group(3);
                                String message = matcher.group(4);
                                String rule = matcher.group(5);
                                int offset = StringUtil.lineColToOffset(document.getText(), line - 1, column);
                                if (LOG.isDebugEnabled()) LOG.debug("+ " + message + " " + line + ":" + column + " " + offset);

                                LocalQuickFix fix = Fixes.getFixForRule(rule);

                                final AccessToken readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();
                                try {
                                    PsiElement lit = PsiUtil.getElementAtOffset(file, offset);
                                    if (LOG.isDebugEnabled()) LOG.debug("+ " + lit.getText());
                                    ProblemDescriptor problem = manager.createProblemDescriptor(
                                            lit,
                                            ESLintBundle.message("eslint.property.inspection.message", message.trim(), rule),
                                            fix,
                                            level.equals("error") ? ProblemHighlightType.ERROR :
                                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly);

                                    problemsHolder.registerProblem(problem);
                                }
                                finally {
                                    readAccessToken.finish();
                                }
                            }
                        }
                    }
                });
                processHandler.startNotify();
                processHandler.waitFor();

                if (process.exitValue() != 0 && !problemsHolder.hasResults()) {
                    // this is the condition that node was started but something went wrong either in the node code
                    // or the module resolution, report the errors to the user through a notification and log it
                    component.showInfoNotification(err.toString(), NotificationType.ERROR);
                    LOG.error(err.toString());
                }
            }
            catch (ExecutionException e) {
                String message = new StringBuilder("Error running ESLint inspection: ").append(e.getMessage()).append("\ncwd: ").append(commandLine.getWorkDirectory().getAbsolutePath()).append("\ncommand: ").append(commandLine.getPreparedCommandLine(Platform.current())).toString();
                LOG.error(message, e);
                ESLintProjectComponent.showNotification(message, NotificationType.ERROR);
            }
            return problemsHolder.getResultsArray();
        } catch (Exception e) { // TODO do we really need this block?
            e.printStackTrace();
            LOG.error("Error running ESLint inspection: ", e);
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            ESLintProjectComponent.showNotification("Error running ESLint inspection: " + e.getMessage(), NotificationType.ERROR);
        }
        return null;
    }

    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        HyperlinkLabel settingsLink = createHyperLink();
        panel.setBorder(IdeBorderFactory.createTitledBorder(getDisplayName() + " options"));
        panel.add(settingsLink);
        return panel;
    }

    @NotNull
    public String getId() {
        return "Settings.JavaScript.Linters.ESLint";
    }

    @NotNull
    private HyperlinkLabel createHyperLink() {
        List path = ContainerUtil.newArrayList(JSBundle.message("settings.javascript.root.configurable.name"), JSBundle.message("settings.javascript.linters.configurable.name"), getDisplayName());

        String title = Joiner.on(" / ").join(path);
        final HyperlinkLabel settingsLink = new HyperlinkLabel(title);
        settingsLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void hyperlinkActivated(HyperlinkEvent e) {
                DataContext dataContext = DataManager.getInstance().getDataContext(settingsLink);
                OptionsEditor optionsEditor = OptionsEditor.KEY.getData(dataContext);
                if (optionsEditor == null) {
                    Project project = CommonDataKeys.PROJECT.getData(dataContext);
                    if (project != null) {
                        // JSLinterConfigurable configurable = ESLintInspection.this.createConfigurable(project);
                        // configurable.showEditDialog();
                        showSettings(project);
                    }
                    return;
                }
                Configurable configurable = optionsEditor.findConfigurableById(ESLintInspection.this.getId());
                if (configurable != null) {
                    optionsEditor.clearSearchAndSelect(configurable);
                }
            }
        });
        return settingsLink;
    }

    public static void showSettings(Project project) {
        ESLintSettingsPage configurable = new ESLintSettingsPage(project);
        String dimensionKey = ShowSettingsUtilImpl.createDimensionKey(configurable);
        SingleConfigurableEditor singleConfigurableEditor = new SingleConfigurableEditor(project, configurable, dimensionKey, false);
        singleConfigurableEditor.show();
    }
}