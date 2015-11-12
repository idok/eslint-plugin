package com.eslint;

import com.eslint.config.schema.RuleCache;
import com.eslint.settings.Settings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.wix.utils.FileUtils;
import com.wix.utils.FileUtils.ValidationStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;

public class ESLintProjectComponent implements ProjectComponent {
    public static final String FIX_CONFIG_HREF = "\n<a href=\"#\">Fix Configuration</a>";
    protected Project project;
    protected Settings settings;
    protected boolean settingValidStatus;
    protected String settingValidVersion;
    protected String settingVersionLastShowNotification;

    private static final Logger LOG = Logger.getInstance(ESLintBundle.LOG_ID);

    public String eslintRcFile;
    public String customRulesPath;
    public String ext;
    public String rulesPath;
    public String eslintExecutable;
    public String nodeInterpreter;
    public boolean treatAsWarnings;
    public boolean pluginEnabled;

    public static final String PLUGIN_NAME = "ESLint plugin";

    public ESLintProjectComponent(Project project) {
        this.project = project;
        settings = Settings.getInstance(project);
    }

    @Override
    public void projectOpened() {
        if (isEnabled()) {
            isSettingsValid();
        }
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public void initComponent() {
        if (isEnabled()) {
            isSettingsValid();
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
        // do not validate if disabled
        if (!settings.pluginEnabled) {
            return true;
        }
        boolean status = validateField("Node Interpreter", settings.nodeInterpreter, true, false, true);
        if (!status) {
            return false;
        }
        status = validateField("Rules", settings.rulesPath, false, true, false);
        if (!status) {
            return false;
        }
        status = validateField("ESLint bin", settings.eslintExecutable, false, false, true);
        if (!status) {
            return false;
        }
        status = validateField("Builtin rules", settings.builtinRulesPath, false, true, false);
        if (!status) {
            return false;
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
        customRulesPath = settings.rulesPath;
        rulesPath = settings.builtinRulesPath;
        nodeInterpreter = settings.nodeInterpreter;
        treatAsWarnings = settings.treatAllEslintIssuesAsWarnings;
        pluginEnabled = settings.pluginEnabled;
        ext = settings.ext;

        RuleCache.initializeFromPath(project, this);

        settingValidStatus = true;
        return true;
    }

    private boolean validateField(String fieldName, String value, boolean shouldBeAbsolute, boolean allowEmpty, boolean isFile) {
        ValidationStatus r = FileUtils.validateProjectPath(shouldBeAbsolute ? null : project, value, allowEmpty, isFile);
        if (isFile) {
            if (r == ValidationStatus.NOT_A_FILE) {
                String msg = ESLintBundle.message("eslint.file.is.not.a.file", fieldName, value);
                validationFailed(msg);
                return false;
            }
        } else {
            if (r == ValidationStatus.NOT_A_DIRECTORY) {
                String msg = ESLintBundle.message("eslint.directory.is.not.a.dir", fieldName, value);
                validationFailed(msg);
                return false;
            }
        }
        if (r == ValidationStatus.DOES_NOT_EXIST) {
            String msg = ESLintBundle.message("eslint.file.does.not.exist", fieldName, value);
            validationFailed(msg);
            return false;
        }
        return true;
    }

    private void validationFailed(String msg) {
        NotificationListener notificationListener = new NotificationListener() {
            @Override
            public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
                ESLintInspection.showSettings(project);
            }
        };
        String errorMessage = msg + FIX_CONFIG_HREF;
        showInfoNotification(errorMessage, NotificationType.WARNING, notificationListener);
        LOG.debug(msg);
        settingValidStatus = false;
    }

    protected void showErrorConfigNotification(String content) {
        if (!settings.getVersion().equals(settingVersionLastShowNotification)) {
            settingVersionLastShowNotification = settings.getVersion();
            showInfoNotification(content, NotificationType.WARNING);
        }
    }

    public void showInfoNotification(String content, NotificationType type) {
        Notification errorNotification = new Notification(PLUGIN_NAME, PLUGIN_NAME, content, type);
        Notifications.Bus.notify(errorNotification, this.project);
    }

    public void showInfoNotification(String content, NotificationType type, NotificationListener notificationListener) {
        Notification errorNotification = new Notification(PLUGIN_NAME, PLUGIN_NAME, content, type, notificationListener);
        Notifications.Bus.notify(errorNotification, this.project);
    }

    public static void showNotification(String content, NotificationType type) {
        Notification errorNotification = new Notification(PLUGIN_NAME, PLUGIN_NAME, content, type);
        Notifications.Bus.notify(errorNotification);
    }
}
