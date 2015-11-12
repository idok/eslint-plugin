package com.eslint.utils;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.text.StringUtil;
import com.wix.nodejs.CLI;
import com.wix.nodejs.NodeRunner;
import org.jetbrains.annotations.NotNull;

final class CliBuilder {

    public static final String V = "-v";
    public static final String RULESDIR = "--rulesdir";
    public static final String EXT = "--ext";
    public static final String C = "-c";
    public static final String FIX = "--fix";

    private CliBuilder() {
    }

    @NotNull
    static GeneralCommandLine create(@NotNull ESLintRunner.ESLintSettings settings) {
        return NodeRunner.createCommandLine(settings.cwd, settings.node, settings.eslintExecutablePath);
    }

    @NotNull
    static GeneralCommandLine createLint(@NotNull ESLintRunner.ESLintSettings settings) {
        GeneralCommandLine commandLine = create(settings);
        // TODO validate arguments (file exist etc)
        commandLine.addParameter(settings.targetFile);
        CLI.addParamIfNotEmpty(commandLine, C, settings.config);
        if (StringUtil.isNotEmpty(settings.rules)) {
            CLI.addParam(commandLine, RULESDIR, "['" + settings.rules + "']");
        }
        if (StringUtil.isNotEmpty(settings.ext)) {
            CLI.addParam(commandLine, EXT, settings.ext);
        }
        return commandLine;
    }

    @NotNull
    static GeneralCommandLine createFix(@NotNull ESLintRunner.ESLintSettings settings) {
        GeneralCommandLine commandLine = createLint(settings);
        commandLine.addParameter(FIX);
        return commandLine;
    }

    @NotNull
    static GeneralCommandLine createVersion(@NotNull ESLintRunner.ESLintSettings settings) {
        GeneralCommandLine commandLine = create(settings);
        commandLine.addParameter(V);
        return commandLine;
    }
}