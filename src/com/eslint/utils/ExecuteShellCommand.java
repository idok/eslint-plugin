package com.eslint.utils;

import com.eslint.ESLintBundle;
import com.eslint.ESLintProjectComponent;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExecuteShellCommand {

    private ExecuteShellCommand() {
    }

    private static final Logger LOG = Logger.getInstance(ESLintBundle.LOG_ID);

    // private static final Pattern pattern = Pattern.compile("\\d+:\\d+");
    private static final Pattern pattern = Pattern.compile("(\\d+):(\\d+)\\s+(\\w+)\\s+([\\w\\W]+)\\s+([\\w-]+)");

    public static Result executeCommand(String cwd, String command) {
        StringBuilder output = new StringBuilder();
        StringBuilder errOutput = new StringBuilder();

        System.out.println("cwd: " + cwd + " command: " + command);

        Result result = new Result();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command, new String[]{}, new File(cwd));
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errLine;
            while ((errLine = errReader.readLine()) != null) {
                errOutput.append(errLine).append('\n');
            }

            String file = null;
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (file == null) {
                    file = line;
                }
                boolean found = false;

                if (matcher.find() && matcher.groupCount() == 5) {
                    Warn warn = new Warn();
                    warn.line = Integer.parseInt(matcher.group(1));
                    warn.column = Integer.parseInt(matcher.group(2));
                    warn.level = matcher.group(3);
                    warn.message = matcher.group(4);
                    warn.rule = matcher.group(5);
                    result.warns.add(warn);
                }

                while (matcher.find()) {
                    LOG.debug("I found the text \"%s\" starting at index %d and ending at index %d.%n",
                            matcher.group(),
                            matcher.start(),
                            matcher.end());
                    found = true;
                }
                output.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
            ESLintProjectComponent.showNotification("Error running ESLint inspection: " + e.getMessage() + "\ncwd: " + cwd + "\ncommand: " + command, NotificationType.WARNING);
        }

        result.errorOutput = errOutput.toString();
        System.out.println("output: " + output);
        System.out.println("error: " + errOutput);
        return result;
    }

    private static Warn parseLine(String line) {
        Warn warn = null;
        Matcher matcher = pattern.matcher(line);
        if (matcher.find() && matcher.groupCount() == 5) {
            warn = new Warn();
            warn.line = Integer.parseInt(matcher.group(1));
            warn.column = Integer.parseInt(matcher.group(2));
            warn.level = matcher.group(3);
            warn.message = matcher.group(4);
            warn.rule = matcher.group(5);
        }
        while (matcher.find()) {
            LOG.debug("I found the text \"%s\" starting at index %d and ending at index %d.%n",
                    matcher.group(),
                    matcher.start(),
                    matcher.end());
        }
        return warn;
    }

    public static Result processResults(ProcessOutput output) {
        Result result = new Result();
        List<String> lines = output.getStdoutLines();
        String file = null;
        for (String line : lines) {
            if (file == null) {
                file = line;
            }
            Warn warn = parseLine(line);
            if (warn != null) {
                result.warns.add(warn);
            }
        }
        result.errorOutput = output.getStderr();
        return result;
    }

    public static Result run2(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String eslintBin, @Nullable String eslintrc, @Nullable String rulesdir) {
        ESLintRunner.ESLintSettings settings = ESLintRunner.buildSettings(cwd, path, nodeInterpreter, eslintBin, eslintrc, rulesdir);
        try {
            ProcessOutput output = ESLintRunner.lint(settings);
            return processResults(output);
        } catch (ExecutionException e) {
            LOG.warn("Could not lint file", e);
            ESLintProjectComponent.showNotification("Error running ESLint inspection: " + e.getMessage() + "\ncwd: " + cwd + "\ncommand: " + eslintBin, NotificationType.WARNING);
            e.printStackTrace();
        }
        return null;
    }

    public static Result run(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String eslintBin, @Nullable String eslintrc, @Nullable String rulesdir) {
        StringBuilder command = new StringBuilder();
        if (SystemInfo.isWindows) {
            command.append("cmd.exe /C ");
        }
        command.append(nodeInterpreter).append(' ').append(eslintBin);
        if (StringUtils.isNotEmpty(eslintrc)) {
            command.append(" -c ").append(eslintrc);
        }
        if (StringUtils.isNotEmpty(rulesdir)) {
            command.append(" --rulesdir ['").append(rulesdir).append("']");
        }
        command.append(' ').append(path);
        return executeCommand(cwd, command.toString());
    }

    public static class Result {
        public List<Warn> warns = new ArrayList<Warn>();
        public String errorOutput;
    }

    public static class Warn {
        public int line;
        public int column;
        public String message;
        public String level;
        public String rule;
    }
}