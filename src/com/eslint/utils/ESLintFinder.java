package com.eslint.utils;

import com.google.common.base.Joiner;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EnvironmentUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.wix.utils.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ESLintFinder {
    public static final String ESLINTRC = ".eslintrc";
    public static final String ESLINT_BASE_NAME = SystemInfo.isWindows ? "eslint.cmd" : "eslint";
    private static final Pattern NVM_NODE_DIR_NAME_PATTERN = Pattern.compile("^v?(\\d+)\\.(\\d+)\\.(\\d+)$");
    public static final String NODE_MODULES = "node_modules";
    public static final String DEFAULT_ESLINT_BIN = SystemInfo.isWindows ? "node_modules\\.bin\\eslint.cmd" : "node_modules/eslint/bin/eslint.js";

    private ESLintFinder() {
    }

    // List infos = ContainerUtil.newArrayList();
    // NodeModuleSearchUtil.findModulesWithName(infos, "eslint", project.getBaseDir(), null, false);

    @Nullable
    public static File findInterpreterInPath() {
        return PathEnvironmentVariableUtil.findInPath(ESLINT_BASE_NAME);
    }

    @NotNull
    public static List<File> listAllPossibleNodeInterpreters() {
        Set<File> interpreters = ContainerUtil.newLinkedHashSet();
        List<File> fromPath = PathEnvironmentVariableUtil.findAllExeFilesInPath(ESLINT_BASE_NAME);
        List<File> nvmInterpreters = listNodeInterpretersFromNvm();
        List<File> brewInterpreters = listNodeInterpretersFromHomeBrew();
        interpreters.addAll(fromPath);
        interpreters.removeAll(nvmInterpreters);
        interpreters.removeAll(brewInterpreters);
        interpreters.addAll(nvmInterpreters);
        interpreters.addAll(brewInterpreters);
        return ContainerUtil.newArrayList(interpreters);
    }

    @NotNull
    public static List<File> searchForESLintBin(File projectRoot) {
//        List<File> nodeModules = searchProjectNodeModules(projectRoot);
        List<File> globalESLintBin = listAllPossibleNodeInterpreters();

        if (SystemInfo.isWindows) {
            File file = resolvePath(projectRoot, NODE_MODULES, ".bin", "eslint.cmd");
            if (file.exists()) {
                globalESLintBin.add(file);
            }
        } else {
            File file = resolvePath(projectRoot, NODE_MODULES, "eslint", "bin", "eslint.js");
            if (file.exists()) {
                globalESLintBin.add(file);
            }
        }
//        globalESLintBin.addAll(nodeModules);
        return globalESLintBin;
    }

    public static File resolvePath(File root, @Nullable String first, @Nullable String second, String... rest) {
        String path = buildPath(first, second, rest);
        return new File(root, path);
    }

    public static String buildPath(@Nullable String first, @Nullable String second, String... rest) {
        return Joiner.on(File.separatorChar).join(first, second, (Object[]) rest);
    }

    @NotNull
    private static List<File> listNodeInterpretersFromNvm() {
        String nvmDirPath = EnvironmentUtil.getValue("NVM_DIR");
        if (StringUtil.isEmpty(nvmDirPath)) {
            return Collections.emptyList();
        }
        File nvmDir = new File(nvmDirPath);
        if (nvmDir.isDirectory() && nvmDir.isAbsolute()) {
            return listNodeInterpretersFromVersionDir(nvmDir);
        }
        return Collections.emptyList();
    }

    private static List<File> listNodeInterpretersFromHomeBrew() {
        return listNodeInterpretersFromVersionDir(new File("/usr/local/Cellar/node"));
    }

    private static List<File> listNodeInterpretersFromVersionDir(@NotNull File parentDir) {
        if (!parentDir.isDirectory()) {
            return Collections.emptyList();
        }
        File[] dirs = parentDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return ESLintFinder.structureNodeVersionStr(name) != null;
            }
        });
        if (dirs == null || dirs.length == 0) {
            return Collections.emptyList();
        }
        Arrays.sort(dirs, new Comparator<File>() {
            public int compare(File dir1, File dir2) {
                int[] v1 = ESLintFinder.structureNodeVersionStr(dir1.getName());
                int[] v2 = ESLintFinder.structureNodeVersionStr(dir2.getName());
                if (v1 != null && v2 != null) {
                    for (int i = 0; i < v1.length; i++) {
                        if (i < v2.length) {
                            int cmp = v2[i] - v1[i];
                            if (cmp != 0) {
                                return cmp;
                            }
                        }
                    }
                }
                return dir1.getName().compareTo(dir2.getName());
            }
        });
        List<File> interpreters = ContainerUtil.newArrayListWithCapacity(dirs.length);
        for (File dir : dirs) {
            File interpreter = new File(dir, "bin" + File.separator + ESLINT_BASE_NAME);
            if (interpreter.isFile() && interpreter.canExecute()) {
                interpreters.add(interpreter);
            }
        }
        return interpreters;
    }

    @Nullable
    private static int[] structureNodeVersionStr(@NotNull String name) {
        Matcher matcher = NVM_NODE_DIR_NAME_PATTERN.matcher(name);
        if (matcher.matches() && matcher.groupCount() == 3) {
            try {
                return new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3))};
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * find possible eslint rc files
     * @param projectRoot
     * @return
     */
    public static List<String> searchForESLintRCFiles(final File projectRoot) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.equals(ESLINTRC);
            }
        };
        // return Arrays.asList(files);
        List<String> files = FileUtils.recursiveVisitor(projectRoot, filter);
        return ContainerUtil.map(files, new Function<String, String>() {
            public String fun(String curFile) {
                return FileUtils.makeRelative(projectRoot, new File(curFile));
            }
        });
    }


//                List<File> newFiles = NodeDetectionUtil.listAllPossibleNodeInterpreters();
//                return ContainerUtil.map(newFiles, new Function<File, String>() {
//                    public String fun(File file) {
//                        return file.getAbsolutePath();
//                    }
//                });
}