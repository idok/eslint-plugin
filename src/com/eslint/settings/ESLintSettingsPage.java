package com.eslint.settings;

import com.eslint.ESLintProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ESLintSettingsPage implements Configurable {
    protected Project project;

    private JCheckBox pluginEnabledCheckbox;
    private JTextField eslintBinField;
    private JTextField configFilePathField;
    private JTextField rulesPathField;
    private JPanel panel;

    public ESLintSettingsPage(@NotNull final Project project) {
        this.project = project;
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
        return !pluginEnabledCheckbox.isSelected() == getSettings().pluginEnabled
                || !eslintBinField.getText().equals(getSettings().eslintExecutable)
                || !configFilePathField.getText().equals(getSettings().eslintRcFile)
                || !rulesPathField.getText().equals(getSettings().rulesPath);
    }

    @Override
    public void apply() throws ConfigurationException {
        saveSettings();
        PsiManager.getInstance(project).dropResolveCaches();
    }

    protected void saveSettings() {
        getSettings().pluginEnabled = pluginEnabledCheckbox.isSelected();
        getSettings().eslintExecutable = eslintBinField.getText();
        getSettings().eslintRcFile = configFilePathField.getText();
        getSettings().rulesPath = rulesPathField.getText();
        project.getComponent(ESLintProjectComponent.class).validateSettings();
    }

    protected void loadSettings() {
        pluginEnabledCheckbox.setSelected(getSettings().pluginEnabled);
        eslintBinField.setText(getSettings().eslintExecutable);
        configFilePathField.setText(getSettings().eslintRcFile);
        rulesPathField.setText(getSettings().rulesPath);
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
}
