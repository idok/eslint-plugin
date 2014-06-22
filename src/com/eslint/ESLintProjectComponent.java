package com.eslint;

import com.eslint.settings.Settings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ESLintProjectComponent implements ProjectComponent {
    protected Project project;
    protected Settings settings;
    protected boolean settingValidStatus;
    protected String settingValidVersion;
    protected String settingVersionLastShowNotification;

    private static final Logger LOG = Logger.getInstance("ESLint-Plugin");
//    private VirtualFile requirejsBaseUrlPath;

    public String eslintRcFile;
    public String rulesPath;
    public String eslintExecutable;
    public boolean pluginEnabled;

    public ESLintProjectComponent(Project project) {
        this.project = project;
        settings = Settings.getInstance(project);
    }

    @Override
    public void projectOpened() {
        if (isEnabled()) {
            validateSettings();
        }
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public void initComponent() {
        if (isEnabled()) {
            validateSettings();
        }
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "ESLintProjectComponent";
    }

    public boolean isEnabled() {
        return Settings.getInstance(project).pluginEnabled;
    }

    public boolean isSettingsValid() {
        if (!settings.getVersion().equals(settingValidVersion)) {
            validateSettings();
            settingValidVersion = settings.getVersion();
        }
        return settingValidStatus;
    }

    public boolean validateSettings() {
        if (StringUtil.isNotEmpty(settings.rulesPath)) {
            File file = new File(project.getBasePath(), settings.rulesPath);
            if (!file.exists()) {
                showErrorConfigNotification(ESLintBundle.message("eslint.rules.dir.does.not.exist", file.toString()));
                LOG.debug("Rules directory not found");
                settingValidStatus = false;
                return false;
            }
            if (!file.isDirectory()) {
                showErrorConfigNotification(ESLintBundle.message("eslint.rules.dir.is.not.a.dir", file.toString()));
                LOG.debug("Rules path is not a directory");
                settingValidStatus = false;
                return false;
            }
        }
//        if (StringUtil.isNotEmpty(settings.eslintExecutable)) {
//            File file = new File(project.getBasePath(), settings.eslintExecutable);
//            if (!file.exists()) {
//                showErrorConfigNotification(ESLintBundle.message("eslint.rules.dir.does.not.exist", file.toString()));
//                LOG.debug("Rules directory not found");
//                settingValidStatus = false;
//                return false;
//            }
//        }
        eslintExecutable = settings.eslintExecutable;
        eslintRcFile = settings.eslintRcFile;
        rulesPath = settings.rulesPath;

        settingValidStatus = true;
        return true;
    }

    protected void showErrorConfigNotification(String content) {
        if (!settings.getVersion().equals(settingVersionLastShowNotification)) {
            settingVersionLastShowNotification = settings.getVersion();
            showInfoNotification(content, NotificationType.ERROR);
        }
    }

    public void showInfoNotification(String content, NotificationType type) {
        Notification errorNotification = new Notification("ESLint plugin", "ESLint plugin", content, type);
        Notifications.Bus.notify(errorNotification, this.project);
    }
}
