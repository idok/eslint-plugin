package com.eslint.settings;

import com.eslint.utils.ESLintFinder;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(name = "ESLintProjectComponent",
        storages = {
                @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
                @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/eslintPlugin.xml", scheme = StorageScheme.DIRECTORY_BASED)})
public class Settings implements PersistentStateComponent<Settings> {
    public String eslintRcFile = ESLintFinder.ESLINTRC;
    public String rulesPath = "";
    public String builtinRulesPath = "";
    public String eslintExecutable = "";
    public String nodeInterpreter;
    public boolean treatAllEslintIssuesAsWarnings;
    public boolean pluginEnabled;

    protected Project project;

    public static Settings getInstance(Project project) {
        Settings settings = ServiceManager.getService(project, Settings.class);
        settings.project = project;
        return settings;
    }

    @Nullable
    @Override
    public Settings getState() {
        return this;
    }

    @Override
    public void loadState(Settings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getVersion() {
        return nodeInterpreter + eslintExecutable + eslintRcFile + rulesPath + builtinRulesPath;
    }
}
