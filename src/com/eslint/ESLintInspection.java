package com.eslint;

import com.eslint.fixes.Fixes;
import com.eslint.settings.ESLintSettingsPage;
import com.eslint.utils.ExecuteShellCommand;
import com.eslint.utils.PsiUtil;
import com.google.common.base.Joiner;
import com.intellij.codeInspection.*;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ex.SingleConfigurableEditor;
import com.intellij.openapi.options.newEditor.OptionsEditor;
import com.intellij.openapi.project.Project;
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
import java.util.List;

public class ESLintInspection extends PropertySuppressableInspectionBase {

    private static final Logger LOG = Logger.getInstance(ESLintBundle.LOG_ID);

    @NotNull
    public String getDisplayName() {
        return ESLintBundle.message("eslint.property.inspection.display.name");
    }

    @NotNull
    public String getShortName() {
        return "ESLintInspection";
    }

    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        try {
            if (!(file instanceof JSFileImpl)) return null;
            // final List<ProblemDescriptor> descriptors = new SmartList<ProblemDescriptor>();
            ESLintProjectComponent component = file.getProject().getComponent(ESLintProjectComponent.class);
            if (!component.isSettingsValid() || !component.isEnabled()) {
                return null;
            }

            ExecuteShellCommand.Result result = ExecuteShellCommand.run(file.getProject().getBasePath(), file.getVirtualFile().getPath(), component.nodeInterpreter, component.eslintExecutable, component.eslintRcFile, component.rulesPath);
            if (StringUtils.isNotEmpty(result.errorOutput)) {
                component.showInfoNotification(result.errorOutput, NotificationType.WARNING);
                return null;
            }
            Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
            if (document == null) {
                component.showInfoNotification("Error running ESLint inspection: Could not get document for file " + file.getName(), NotificationType.WARNING);
                System.out.println("Could not get document for file " + file.getName());
                return null;
            }
            final ProblemsHolder problemsHolder = new ProblemsHolder(manager, file, isOnTheFly);
            for (ExecuteShellCommand.Warn warn : result.warns) {
                int offset = StringUtil.lineColToOffset(document.getText(), warn.line - 1, warn.column);
                System.out.println("+ " + warn.message + " " + warn.line + ":" + warn.column + " " + offset);
                LOG.debug("+ " + warn.message + " " + warn.line + ":" + warn.column + " " + offset);
                // PsiElement lit2 = PsiTreeUtil.findElementOfClassAtOffset(file, offset, PsiElement.class, false);
                PsiElement lit = PsiUtil.getElementAtOffset(file, offset);
                LOG.debug("+ " + lit.getText());

                LocalQuickFix fix = Fixes.getFixForRule(warn.rule);

                final ProblemDescriptor problem = manager.createProblemDescriptor(
                        lit,
                        ESLintBundle.message("eslint.property.inspection.message", warn.message.trim(), warn.rule),
                        fix,
                        warn.level.equals("error") ? ProblemHighlightType.ERROR :
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly);

                problemsHolder.registerProblem(problem);
                // problemsHolder.registerProblem(lit, lit.getTextRange(), warn.message.trim(), RemoveTrailingSpacesFix.INSTANCE);
                // descriptors.add(manager.createProblemDescriptor(lit, lit.getTextRange(), warn.message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, RemoveTrailingSpacesFix.INSTANCE));
            }

            // return descriptors.toArray(new ProblemDescriptor[descriptors.size()]);
            return problemsHolder.getResultsArray();
        } catch (Exception e) {
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