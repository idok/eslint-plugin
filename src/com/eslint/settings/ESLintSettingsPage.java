package com.eslint.settings;

import com.eslint.ESLintProjectComponent;
import com.eslint.utils.ESLintFinder;
import com.eslint.utils.ESLintRunner;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.execution.ExecutionException;
import com.intellij.javascript.nodejs.NodeDetectionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.NotNullProducer;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.ui.SwingHelper;
import com.wix.settings.ValidationInfo;
import com.wix.settings.ValidationUtils;
import com.wix.ui.PackagesNotificationPanel;
import com.wix.utils.FileUtils;
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

public class ESLintSettingsPage implements Configurable {
    public static final String FIX_IT = "Fix it";
    public static final String HOW_TO_USE_ESLINT = "How to Use ESLint";
    public static final String HOW_TO_USE_LINK = "https://github.com/idok/eslint-plugin";
    protected Project project;

    private JCheckBox pluginEnabledCheckbox;
    private JTextField customRulesPathField;
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
    private TextFieldWithHistoryWithBrowseButton rulesPathField;
    private JLabel rulesDirectoryLabel1;
    private JTextField textFieldExt;
    private JLabel extensionsLabel;
    private final PackagesNotificationPanel packagesNotificationPanel;

    public ESLintSettingsPage(@NotNull final Project project) {
        this.project = project;
        configESLintBinField();
        configESLintRcField();
        configESLintRulesField();
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
        rulesPathField.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        customRulesPathField.getDocument().addDocumentListener(docAdp);
        textFieldExt.getDocument().addDocumentListener(docAdp);
    }

    private File getProjectPath() {
        return new File(project.getBaseDir().getPath());
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
        customRulesPathField.setEnabled(enabled);
        rulesPathField.setEnabled(enabled);
        searchForEslintrcInRadioButton.setEnabled(enabled);
        useProjectEslintrcRadioButton.setEnabled(enabled);
        eslintBinField2.setEnabled(enabled);
        nodeInterpreterField.setEnabled(enabled);
        ESLintConfigFilePathLabel.setEnabled(enabled);
        rulesDirectoryLabel.setEnabled(enabled);
        rulesDirectoryLabel1.setEnabled(enabled);
        pathToEslintBinLabel.setEnabled(enabled);
        nodeInterpreterLabel.setEnabled(enabled);
        treatAllEslintIssuesCheckBox.setEnabled(enabled);
        textFieldExt.setEnabled(enabled);
        extensionsLabel.setEnabled(enabled);
    }

    private void validateField(List<ValidationInfo> errors, TextFieldWithHistoryWithBrowseButton field, boolean allowEmpty, String message) {
        if (!validatePath(field.getChildComponent().getText(), allowEmpty)) {
            addError(errors, field.getChildComponent().getTextEditor(), message, FIX_IT);
        }
    }

    private void validate() {
        if (!pluginEnabledCheckbox.isSelected()) {
            return;
        }
        List<ValidationInfo> errors = new ArrayList<ValidationInfo>();
        validateField(errors, eslintBinField2, false, "Path to eslint is invalid {{LINK}}");
        validateField(errors, eslintrcFile, true, "Path to eslintrc is invalid {{LINK}}"); //Please correct path to
        validateField(errors, nodeInterpreterField, false, "Path to node interpreter is invalid {{LINK}}");
        if (!validateDirectory(customRulesPathField.getText(), true)) {
            addError(errors, customRulesPathField, "Path to custom rules is invalid {{LINK}}", FIX_IT);
        }
        if (!validateDirectory(rulesPathField.getChildComponent().getText(), true)) {
            addError(errors, rulesPathField.getChildComponent().getTextEditor(), "Path to rules is invalid {{LINK}}", FIX_IT);
        }
        if (!validateExt(textFieldExt.getText())) {
            addError(errors, textFieldExt, "Extensions format is invalid, should be e.g. .js,.jsx without white space {{LINK}}", FIX_IT);
        }
        if (errors.isEmpty()) {
            getVersion();
        }
        packagesNotificationPanel.processErrors(errors);
    }

    private static void addError(List<ValidationInfo> errors, @Nullable JTextComponent textComponent, @NotNull String errorHtmlDescriptionTemplate, @NotNull String linkText) {
        ValidationInfo error = new ValidationInfo(textComponent, errorHtmlDescriptionTemplate, linkText);
        errors.add(error);
    }

    private static boolean validateExt(String ext) {
        return StringUtils.isEmpty(ext) || ext.matches("^(\\.\\w+,?)+$");
    }

    private ESLintRunner.ESLintSettings settings;

    private void getVersion() {
        if (settings != null &&
                areEqual(nodeInterpreterField, settings.node) &&
                areEqual(eslintBinField2, settings.eslintExecutablePath) &&
                settings.cwd.equals(project.getBasePath())
                ) {
            return;
        }
        settings = new ESLintRunner.ESLintSettings();
        settings.node = nodeInterpreterField.getChildComponent().getText();
        settings.eslintExecutablePath = eslintBinField2.getChildComponent().getText();
        settings.cwd = project.getBasePath();
        try {
            String version = ESLintRunner.runVersion(settings);
            versionLabel.setText(version.trim());
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
        return ValidationUtils.validateDirectory(project, path, allowEmpty);
    }

    private static TextFieldWithHistory configWithDefaults(TextFieldWithHistoryWithBrowseButton field) {
        TextFieldWithHistory textFieldWithHistory = field.getChildComponent();
        textFieldWithHistory.setHistorySize(-1);
        textFieldWithHistory.setMinimumAndPreferredWidth(0);
        return textFieldWithHistory;
    }

    private void configESLintBinField() {
        configWithDefaults(eslintBinField2);
        SwingHelper.addHistoryOnExpansion(eslintBinField2.getChildComponent(), new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                List<File> newFiles = ESLintFinder.searchForESLintBin(getProjectPath());
                return FileUtils.toAbsolutePath(newFiles);
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, eslintBinField2, "Select ESLint.js cli", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configESLintRulesField() {
        TextFieldWithHistory textFieldWithHistory = rulesPathField.getChildComponent();
        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                return ESLintFinder.tryFindRulesAsString(getProjectPath());
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, rulesPathField, "Select Built in rules", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configESLintRcField() {
        TextFieldWithHistory textFieldWithHistory = configWithDefaults(eslintrcFile);
        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                return ESLintFinder.searchForESLintRCFiles(getProjectPath());
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, eslintrcFile, "Select ESLint config", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configNodeField() {
        TextFieldWithHistory textFieldWithHistory = configWithDefaults(nodeInterpreterField);
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

    private static boolean areEqual(TextFieldWithHistoryWithBrowseButton field, String value) {
        return field.getChildComponent().getText().equals(value);
    }

    @Override
    public boolean isModified() {
        Settings s = getSettings();
        return pluginEnabledCheckbox.isSelected() != s.pluginEnabled ||
                !areEqual(eslintBinField2, s.eslintExecutable) ||
                !areEqual(nodeInterpreterField, s.nodeInterpreter) ||
                treatAllEslintIssuesCheckBox.isSelected() != s.treatAllEslintIssuesAsWarnings ||
                !customRulesPathField.getText().equals(s.rulesPath) ||
                !textFieldExt.getText().equals(s.ext) ||
                !areEqual(rulesPathField, s.builtinRulesPath) ||
                !getESLintRCFile().equals(s.eslintRcFile);
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
        settings.rulesPath = customRulesPathField.getText();
        settings.builtinRulesPath = rulesPathField.getChildComponent().getText();
        settings.treatAllEslintIssuesAsWarnings = treatAllEslintIssuesCheckBox.isSelected();
        settings.ext = textFieldExt.getText();
        project.getComponent(ESLintProjectComponent.class).validateSettings();
        DaemonCodeAnalyzer.getInstance(project).restart();
        validate();
    }

    protected void loadSettings() {
        Settings settings = getSettings();
        pluginEnabledCheckbox.setSelected(settings.pluginEnabled);
        eslintBinField2.getChildComponent().setText(settings.eslintExecutable);
        eslintrcFile.getChildComponent().setText(settings.eslintRcFile);
        nodeInterpreterField.getChildComponent().setText(settings.nodeInterpreter);
        customRulesPathField.setText(settings.rulesPath);
        textFieldExt.setText(settings.ext);
        rulesPathField.getChildComponent().setText(settings.builtinRulesPath);
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
}
