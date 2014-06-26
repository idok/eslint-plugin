package com.eslint.utils;

import com.eslint.ESLintBundle;
import com.eslint.ESLintProjectComponent;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
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

public class ExecuteShellCommand {

    private static final Logger LOG = Logger.getInstance(ESLintBundle.LOG_ID);

//    String cwd = "/Users/idok/Projects/react-viewer";

    public static void main(String[] args) {
        ExecuteShellCommand obj = new ExecuteShellCommand();
        String domainName = "google.com";
        //in mac oxs
//        String command = "grunt eslint";
        String file = "packages/core/src/main/util/comp-factory.js"; ///Users/idok/Projects/react-viewer/
        String command = "node node_modules/eslint/bin/eslint.js -c .eslintrc --rulesdir ['node_modules/grunt-packages/conf/rules'] " + file + "";
//        String command = "pwd";

        //in windows
        //String command = "ping -n 3 " + domainName;
//        String output = obj.executeCommand(command);
//        System.out.println("output: " + output);
    }

    //    private static final Pattern pattern = Pattern.compile("\\d+:\\d+");
    private static final Pattern pattern = Pattern.compile("(\\d+):(\\d+)\\s+(\\w+)\\s+([\\w\\W]+)\\s+([\\w-]+)");

    public Result executeCommand(String cwd, String command) {
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
                    LOG.debug("I found the text" +
                                    " \"%s\" starting at " +
                                    "index %d and ending at index %d.%n",
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

    public static Result run(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String eslintBin, @Nullable String eslintrc, @Nullable String rulesdir) {
        ExecuteShellCommand obj = new ExecuteShellCommand();
//        String command = "grunt eslint";
//        String file = "packages/core/src/main/util/comp-factory.js"; ///Users/idok/Projects/react-viewer/
        // String command = "node node_modules/eslint/bin/eslint.js -c .eslintrc --rulesdir ['node_modules/grunt-packages/conf/rules'] " + path + "";
        String command = nodeInterpreter + ' ' + eslintBin; //NodeDetectionUtil.NODE_INTERPRETER_BASE_NAME
        if (StringUtils.isNotEmpty(eslintrc)) {
            command += " -c " + eslintrc;
        }
        if (StringUtils.isNotEmpty(rulesdir)) {
            command += " --rulesdir ['" + rulesdir + "']";
        }
        command += ' ' + path;
        return obj.executeCommand(cwd, command);
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