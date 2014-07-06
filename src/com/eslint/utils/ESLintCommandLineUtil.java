package com.eslint.utils;

import com.google.common.base.Charsets;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ESLintCommandLineUtil {
    private static final Logger LOG = Logger.getInstance(ESLintCommandLineUtil.class);

    public static class ESLintSettings {
        public String node;
        public String eslintExecutablePath;
        public String rules;
        public String config;
        public String cwd;
        public String targetFile;
    }

    private ESLintCommandLineUtil() {
    }

    @NotNull
    public static ProcessOutput runESLintCommand(@NotNull ESLintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLine(settings);
        if (commandLine == null) {
            throw new ExecutionException("Bad parameters");
        }
//        commandLine.addParameters(commands);
        ProcessOutput output = runCommandLine(commandLine, (int) TimeUnit.SECONDS.toMillis(120L));
        int exitCode = output.getExitCode();
        if (output.isTimeout()) {
            throw new ExecutionException("Command '" + commandLine.getCommandLineString() + "' is timed out.");
        }
//        if (exitCode != 0) {
//            throw new ExecutionException("Exit code of '" + commandLine.getCommandLineString() + "' is " + exitCode + ". Stdout:\n" + output.getStdout() + "\n\nstderr:\n" + output.getStderr());
//        }

//        String stdout = output.getStdout();
//        if (StringUtil.isEmptyOrSpaces(stdout)) {
//            throw new ExecutionException("Got empty stdout, exit code: 0, stderr:\n" + output.getStderr());
//        }
        return output;
    }

    @NotNull
    public static ProcessOutput version(@NotNull ESLintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLine(settings);
        commandLine.addParameter("-v");
//        commandLine.addParameters(commands);
        ProcessOutput output = runCommandLine(commandLine, (int) TimeUnit.SECONDS.toMillis(120L));
        int exitCode = output.getExitCode();
        if (output.isTimeout()) {
            throw new ExecutionException("Command '" + commandLine.getCommandLineString() + "' is timed out.");
        }
//        if (exitCode != 0) {
//            throw new ExecutionException("Exit code of '" + commandLine.getCommandLineString() + "' is " + exitCode + ". Stdout:\n" + output.getStdout() + "\n\nstderr:\n" + output.getStderr());
//        }

//        String stdout = output.getStdout();
//        if (StringUtil.isEmptyOrSpaces(stdout)) {
//            throw new ExecutionException("Got empty stdout, exit code: 0, stderr:\n" + output.getStderr());
//        }
        return output;
    }

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

    @Nullable
    private static GeneralCommandLine createCommandLineLint(@NotNull ESLintSettings settings) {
        GeneralCommandLine commandLine = createCommandLine(settings);
//        File bowerConfigFile = new File(settings.getBowerJsonPath());
//        if (!bowerConfigFile.isFile()) {
//            return null;
//        }
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
    private static ProcessOutput runCommandLine(@NotNull GeneralCommandLine commandLine, int timeoutInMilliseconds) throws ExecutionException {
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
        return output;
    }
}