package com.eslint.utils;

import com.eslint.ESLintProjectComponent;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.wix.nodejs.NodeRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.TimeUnit;

public final class ESLintRunner {
    private ESLintRunner() {
    }

    private static final Logger LOG = Logger.getInstance(ESLintRunner.class);

    private static final int TIME_OUT = (int) TimeUnit.SECONDS.toMillis(120L);

    public static class ESLintSettings {
        public String node;
        public String eslintExecutablePath;
        public String rules;
        public String config;
        public String cwd;
        public String targetFile;
        public String ext;
    }

    public static ESLintSettings buildSettings(@NotNull String cwd, @NotNull String path, @NotNull ESLintProjectComponent component) {
        return ESLintRunner.buildSettings(cwd, path, component.nodeInterpreter, component.eslintExecutable, component.eslintRcFile, component.customRulesPath, component.ext);
    }

    public static ESLintSettings buildSettings(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String eslintBin, @Nullable String eslintrc, @Nullable String rulesdir, @Nullable String ext) {
        ESLintRunner.ESLintSettings settings = new ESLintRunner.ESLintSettings();
        settings.cwd = cwd;
        settings.eslintExecutablePath = eslintBin;
        settings.node = nodeInterpreter;
        settings.rules = rulesdir;
        settings.config = eslintrc;
        settings.targetFile = path;
        settings.ext = ext;
        return settings;
    }

    @NotNull
    public static ProcessOutput lint(@NotNull ESLintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = CliBuilder.createLint(settings);
        return NodeRunner.execute(commandLine, TIME_OUT);
    }

    public static Result lint(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String eslintBin, @Nullable String eslintrc, @Nullable String rulesdir, @Nullable String ext) {
        ESLintRunner.ESLintSettings settings = ESLintRunner.buildSettings(cwd, path, nodeInterpreter, eslintBin, eslintrc, rulesdir, ext);
        try {
            ProcessOutput output = ESLintRunner.lint(settings);
            return Result.processResults(output);
        } catch (ExecutionException e) {
            LOG.warn("Could not lint file", e);
            ESLintProjectComponent.showNotification("Error running ESLint inspection: " + e.getMessage() + "\ncwd: " + cwd + "\ncommand: " + eslintBin, NotificationType.WARNING);
            e.printStackTrace();
        }
        return null;
    }

    @NotNull
    public static ProcessOutput fix(@NotNull ESLintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = CliBuilder.createFix(settings);
        return NodeRunner.execute(commandLine, TIME_OUT);
    }

    @NotNull
    private static ProcessOutput version(@NotNull ESLintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = CliBuilder.createVersion(settings);
        return NodeRunner.execute(commandLine, TIME_OUT);
    }

    @NotNull
    public static String runVersion(@NotNull ESLintSettings settings) throws ExecutionException {
        if (!new File(settings.eslintExecutablePath).exists()) {
            LOG.warn("Calling version with invalid eslint exe " + settings.eslintExecutablePath);
            return "";
        }
        ProcessOutput out = version(settings);
        if (out.getExitCode() == 0) {
            return out.getStdout().trim();
        }
        return "";
    }
}