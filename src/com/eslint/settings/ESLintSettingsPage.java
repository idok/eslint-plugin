package com.eslint.settings;

import com.eslint.ESLintProjectComponent;
import com.eslint.utils.ESLintFinder;
import com.eslint.utils.ESLintRunner;
import com.eslint.utils.FileUtils;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.javascript.nodejs.NodeDetectionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.Function;
import com.intellij.util.NotNullProducer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.packaging.PackagesNotificationPanel;
import com.intellij.webcore.ui.SwingHelper;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

//path to JavaScript: JSBundle.message("settings.javascript.root.configurable.name", new Object[0])
public class ESLintSettingsPage implements Configurable {
    public static final String FIX_IT = "Fix it";
    public static final String HOW_TO_USE_ESLINT = "How to Use ESLint";
    public static final String HOW_TO_USE_LINK = "https://github.com/idok/eslint-plugin";
    protected Project project;

    private JCheckBox pluginEnabledCheckbox;
    private JTextField rulesPathField;
    private JPanel panel;
    private JPanel errorPanel;
    private TextFieldWithHistoryWithBrowseButton eslintBinField2;
    private TextFieldWithHistoryWithBrowseButton nodeInterpreterField;
    private TextFieldWithHistoryWithBrowseButton eslintrcFile;
    private JRadioButton searchForEslintrcInRadioButton;
    private JRadioButton useProjectEslintrcRadioButton;
    private HyperlinkLabel usageLink;
    private JLabel ESLintConfigFilePathLabel;
    private JLabel rulesDirectoryLabel;
    private JLabel pathToEslintBinLabel;
    private JLabel nodeInterpreterLabel;
    private JCheckBox treatAllEslintIssuesCheckBox;
    private JLabel versionLabel;
    private final PackagesNotificationPanel packagesNotificationPanel;

    public ESLintSettingsPage(@NotNull final Project project) {
        this.project = project;
        configESLintBinField();
        configESLintRcField();
        configNodeField();
//        searchForEslintrcInRadioButton.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                eslintrcFile.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
//                System.out.println("searchForEslintrcInRadioButton: " + (e.getStateChange() == ItemEvent.SELECTED ? "checked" : "unchecked"));
//            }
//        });
        useProjectEslintrcRadioButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                eslintrcFile.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
//                System.out.println("useProjectEslintrcRadioButton: " + (e.getStateChange() == ItemEvent.SELECTED ? "checked" : "unchecked"));
            }
        });
        pluginEnabledCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
                setEnabledState(enabled);
            }
        });

        this.packagesNotificationPanel = new PackagesNotificationPanel(project);
//        GridConstraints gridConstraints = new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
//                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
//                null, new Dimension(250, 150), null);
        errorPanel.add(this.packagesNotificationPanel.getComponent(), BorderLayout.CENTER);

        DocumentAdapter docAdp = new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                updateLaterInEDT();
            }
        };
        eslintBinField2.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        eslintrcFile.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        nodeInterpreterField.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        rulesPathField.getDocument().addDocumentListener(docAdp);
    }

    private void updateLaterInEDT() {
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            public void run() {
                ESLintSettingsPage.this.update();
            }
        });
    }

    private void update() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        validate();
    }

    private void setEnabledState(boolean enabled) {
        eslintrcFile.setEnabled(enabled);
        rulesPathField.setEnabled(enabled);
        searchForEslintrcInRadioButton.setEnabled(enabled);
        useProjectEslintrcRadioButton.setEnabled(enabled);
        eslintBinField2.setEnabled(enabled);
        nodeInterpreterField.setEnabled(enabled);
        ESLintConfigFilePathLabel.setEnabled(enabled);
        rulesDirectoryLabel.setEnabled(enabled);
        pathToEslintBinLabel.setEnabled(enabled);
        nodeInterpreterLabel.setEnabled(enabled);
        treatAllEslintIssuesCheckBox.setEnabled(enabled);
    }

    private void validate() {
        List<ESLintValidationInfo> errors = new ArrayList<ESLintValidationInfo>();
        if (!validatePath(eslintBinField2.getChildComponent().getText(), false)) {
            ESLintValidationInfo error = new ESLintValidationInfo(eslintBinField2.getChildComponent().getTextEditor(), "Path to eslint is invalid {{LINK}}", FIX_IT);
            errors.add(error);
        }
        if (!validatePath(eslintrcFile.getChildComponent().getText(), true)) {
            ESLintValidationInfo error = new ESLintValidationInfo(eslintrcFile.getChildComponent().getTextEditor(), "Path to eslintrc is invalid {{LINK}}", FIX_IT); //Please correct path to
            errors.add(error);
        }
        if (!validatePath(nodeInterpreterField.getChildComponent().getText(), false)) {
            ESLintValidationInfo error = new ESLintValidationInfo(nodeInterpreterField.getChildComponent().getTextEditor(), "Path to node interpreter is invalid {{LINK}}", FIX_IT);
            errors.add(error);
        }
        if (!validateDirectory(rulesPathField.getText(), true)) {
            ESLintValidationInfo error = new ESLintValidationInfo(rulesPathField, "Path to rules is invalid {{LINK}}", FIX_IT);
            errors.add(error);
        }
        if (errors.isEmpty()) {
            packagesNotificationPanel.removeAllLinkHandlers();
            packagesNotificationPanel.hide();
            getVersion();
        } else {
            showErrors(errors);
        }
    }

    private ESLintRunner.ESLintSettings settings;

    private void getVersion() {
        if (settings != null && settings.node.equals(nodeInterpreterField.getChildComponent().getText())
                && settings.eslintExecutablePath.equals(eslintBinField2.getChildComponent().getText())
                && settings.cwd.equals(project.getBasePath())
                ) {
            return;
        }
        settings = new ESLintRunner.ESLintSettings();
        settings.node = nodeInterpreterField.getChildComponent().getText();
        settings.eslintExecutablePath = eslintBinField2.getChildComponent().getText();
        settings.cwd = project.getBasePath();
        try {
            ProcessOutput out = ESLintRunner.version(settings);
            if (out.getExitCode() == 0) {
                versionLabel.setText(out.getStdout().trim());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private boolean validatePath(String path, boolean allowEmpty) {
        if (StringUtils.isEmpty(path)) {
            return allowEmpty;
        }
        File filePath = new File(path);
        if (filePath.isAbsolute()) {
            if (!filePath.exists() || !filePath.isFile()) {
                return false;
            }
        } else {
            VirtualFile child = project.getBaseDir().findFileByRelativePath(path);
            if (child == null || !child.exists() || child.isDirectory()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateDirectory(String path, boolean allowEmpty) {
        if (StringUtils.isEmpty(path)) {
            return allowEmpty;
        }
        File filePath = new File(path);
        if (filePath.isAbsolute()) {
            if (!filePath.exists() || !filePath.isDirectory()) {
                return false;
            }
        } else {
            VirtualFile child = project.getBaseDir().findFileByRelativePath(path);
            if (child == null || !child.exists() || !child.isDirectory()) {
                return false;
            }
        }
        return true;
    }

    private void configESLintBinField() {
        TextFieldWithHistory textFieldWithHistory = eslintBinField2.getChildComponent();
        textFieldWithHistory.setHistorySize(-1);
        textFieldWithHistory.setMinimumAndPreferredWidth(0);

        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                File projectRoot = new File(project.getBaseDir().getPath());
                List<File> newFiles = ESLintFinder.searchForESLintBin(projectRoot);
                return FileUtils.toAbsolutePath(newFiles);
            }
        });

        SwingHelper.installFileCompletionAndBrowseDialog(project, eslintBinField2, "Select ESLint.js cli", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configESLintRcField() {
        TextFieldWithHistory textFieldWithHistory = eslintrcFile.getChildComponent();
        textFieldWithHistory.setHistorySize(-1);
        textFieldWithHistory.setMinimumAndPreferredWidth(0);

        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                File projectRoot = new File(project.getBaseDir().getPath());
                return ESLintFinder.searchForESLintRCFiles(projectRoot);
            }
        });

        SwingHelper.installFileCompletionAndBrowseDialog(project, eslintrcFile, "Select ESLint config", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configNodeField() {
        TextFieldWithHistory textFieldWithHistory = nodeInterpreterField.getChildComponent();
        textFieldWithHistory.setHistorySize(-1);
        textFieldWithHistory.setMinimumAndPreferredWidth(0);

        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                List<File> newFiles = NodeDetectionUtil.listAllPossibleNodeInterpreters();
                return FileUtils.toAbsolutePath(newFiles);
            }
        });

        SwingHelper.installFileCompletionAndBrowseDialog(project, nodeInterpreterField, "Select Node interpreter", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "ESLint Plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        loadSettings();
        return panel;
    }

    @Override
    public boolean isModified() {
        return pluginEnabledCheckbox.isSelected() != getSettings().pluginEnabled
                || !eslintBinField2.getChildComponent().getText().equals(getSettings().eslintExecutable)
                || !nodeInterpreterField.getChildComponent().getText().equals(getSettings().nodeInterpreter)
                || treatAllEslintIssuesCheckBox.isSelected() != getSettings().treatAllEslintIssuesAsWarnings
                || !rulesPathField.getText().equals(getSettings().rulesPath)
                || !getESLintRCFile().equals(getSettings().eslintRcFile);
    }

    private String getESLintRCFile() {
        return useProjectEslintrcRadioButton.isSelected() ? eslintrcFile.getChildComponent().getText() : "";
    }

    @Override
    public void apply() throws ConfigurationException {
        saveSettings();
        PsiManager.getInstance(project).dropResolveCaches();
    }

    protected void saveSettings() {
        Settings settings = getSettings();
        settings.pluginEnabled = pluginEnabledCheckbox.isSelected();
        settings.eslintExecutable = eslintBinField2.getChildComponent().getText();
        settings.nodeInterpreter = nodeInterpreterField.getChildComponent().getText();
        settings.eslintRcFile = getESLintRCFile();
        settings.rulesPath = rulesPathField.getText();
        settings.treatAllEslintIssuesAsWarnings = treatAllEslintIssuesCheckBox.isSelected();
        project.getComponent(ESLintProjectComponent.class).validateSettings();
        DaemonCodeAnalyzer.getInstance(project).restart();
    }

    protected void loadSettings() {
        Settings settings = getSettings();
        pluginEnabledCheckbox.setSelected(settings.pluginEnabled);
        eslintBinField2.getChildComponent().setText(settings.eslintExecutable);
        eslintrcFile.getChildComponent().setText(settings.eslintRcFile);
        nodeInterpreterField.getChildComponent().setText(settings.nodeInterpreter);
        rulesPathField.setText(settings.rulesPath);
        useProjectEslintrcRadioButton.setSelected(StringUtils.isNotEmpty(settings.eslintRcFile));
        searchForEslintrcInRadioButton.setSelected(StringUtils.isEmpty(settings.eslintRcFile));
        eslintrcFile.setEnabled(useProjectEslintrcRadioButton.isSelected());
        treatAllEslintIssuesCheckBox.setSelected(settings.treatAllEslintIssuesAsWarnings);
        setEnabledState(settings.pluginEnabled);
    }

    @Override
    public void reset() {
        loadSettings();
    }

    @Override
    public void disposeUIResources() {
    }

    protected Settings getSettings() {
        return Settings.getInstance(project);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        usageLink = SwingHelper.createWebHyperlink(HOW_TO_USE_ESLINT, HOW_TO_USE_LINK);
    }

    private void showErrors(@NotNull List<ESLintValidationInfo> errors) {
        List<String> errorHtmlDescriptions = ContainerUtil.map(errors, new Function<ESLintValidationInfo, String>() {
            public String fun(ESLintValidationInfo info) {
                return info.getErrorHtmlDescription();
            }
        });
        String styleTag = UIUtil.getCssFontDeclaration(UIUtil.getLabelFont());
        String html = "<html>" + styleTag + "<body><div style='padding-left:4px;'>" + StringUtil.join(errorHtmlDescriptions, "<div style='padding-top:2px;'/>") + "</div></body></html>";

        for (ESLintValidationInfo error : errors) {
            String linkText = error.getLinkText();
            final JTextComponent component = error.getTextComponent();
            if (linkText != null && component != null) {
                this.packagesNotificationPanel.addLinkHandler(linkText, new Runnable() {
                    public void run() {
                        component.requestFocus();
                    }
                });
            }
        }
        this.packagesNotificationPanel.showError(html, null, null);
    }
}
