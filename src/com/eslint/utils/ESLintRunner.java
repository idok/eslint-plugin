package com.eslint.utils;

import com.eslint.ESLintProjectComponent;
import com.google.common.base.Charsets;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    }

    public static ESLintSettings buildSettings(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String eslintBin, @Nullable String eslintrc, @Nullable String rulesdir) {
        ESLintRunner.ESLintSettings settings = new ESLintRunner.ESLintSettings();
        settings.cwd = cwd;
        settings.eslintExecutablePath = eslintBin;
        settings.node = nodeInterpreter;
        settings.rules = rulesdir;
        settings.config = eslintrc;
        settings.targetFile = path;
        return settings;
    }

    @NotNull
    public static ProcessOutput lint(@NotNull ESLintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLineLint(settings);
        return execute(commandLine, TIME_OUT);
    }

    public static Result lint(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String eslintBin, @Nullable String eslintrc, @Nullable String rulesdir) {
        ESLintRunner.ESLintSettings settings = ESLintRunner.buildSettings(cwd, path, nodeInterpreter, eslintBin, eslintrc, rulesdir);
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
    public static ProcessOutput version(@NotNull ESLintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLine(settings);
        commandLine.addParameter("-v");
        return execute(commandLine, TIME_OUT);
    }

    @NotNull
    public static String runVersion(@NotNull ESLintSettings settings) throws ExecutionException {
        ProcessOutput out = version(settings);
        if (out.getExitCode() == 0) {
            return out.getStdout().trim();
        }
        return "";
    }

    @NotNull
    private static GeneralCommandLine createCommandLine(@NotNull ESLintSettings settings) {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setWorkDirectory(settings.cwd);
        if (SystemInfo.isWindows) {
            commandLine.setExePath(settings.eslintExecutablePath);
        } else {
            commandLine.setExePath(settings.node);
            commandLine.addParameter(settings.eslintExecutablePath);
        }
        return commandLine;
    }

    @NotNull
    private static GeneralCommandLine createCommandLineLint(@NotNull ESLintSettings settings) {
        GeneralCommandLine commandLine = createCommandLine(settings);
        // TODO validate arguments (file exist etc)
        commandLine.addParameter(settings.targetFile);
        if (StringUtil.isNotEmpty(settings.config)) {
            commandLine.addParameter("-c");
            commandLine.addParameter(settings.config);
        }
        if (StringUtil.isNotEmpty(settings.rules)) {
            commandLine.addParameter("--rulesdir");
            commandLine.addParameter("['" + settings.rules + "']");
        }
        return commandLine;
    }

    @NotNull
    private static ProcessOutput execute(@NotNull GeneralCommandLine commandLine, int timeoutInMilliseconds) throws ExecutionException {
        LOG.info("Running eslint command: " + commandLine.getCommandLineString());
        Process process = commandLine.createProcess();
        OSProcessHandler processHandler = new ColoredProcessHandler(process, commandLine.getCommandLineString(), Charsets.UTF_8);
        final ProcessOutput output = new ProcessOutput();
        processHandler.addProcessListener(new ProcessAdapter() {
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                if (outputType.equals(ProcessOutputTypes.STDERR)) {
                    output.appendStderr(event.getText());
                } else if (!outputType.equals(ProcessOutputTypes.SYSTEM)) {
                    output.appendStdout(event.getText());
                }
            }
        });
        processHandler.startNotify();
        if (processHandler.waitFor(timeoutInMilliseconds)) {
            output.setExitCode(process.exitValue());
        } else {
            processHandler.destroyProcess();
            output.setTimeout();
        }
        if (output.isTimeout()) {
            throw new ExecutionException("Command '" + commandLine.getCommandLineString() + "' is timed out.");
        }
        return output;
    }
}