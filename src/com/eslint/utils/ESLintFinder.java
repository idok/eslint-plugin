package com.eslint.utils;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.wix.nodejs.NodeFinder;
import com.wix.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public final class ESLintFinder {
    public static final String ESLINTRC = ".eslintrc";
    public static final String ESLINT_BASE_NAME = NodeFinder.getBinName("eslint");

    // TODO figure out a way to automatically get this path or add it to config
    // should read from /usr/local/lib/node_modules/eslint/lib/rules
    //    public static String defaultPath = "/usr/local/lib/node_modules/eslint/lib/rules";
    // c:/users/user/appdata/roaming/npm/node_modules

    private ESLintFinder() {
    }

    public static List<File> tryFindRules(File projectRoot) {
        List<File> options = new ArrayList<File>();
        String relativeRules = NodeFinder.buildPath(NodeFinder.NODE_MODULES, "eslint", "lib", "rules");
        File local = new File(projectRoot, relativeRules);
        options.add(local);
        if (SystemInfo.isWindows) {
            File file = new File("C:\\Program Files (x86)\\nodejs", relativeRules);
            options.add(file);
            file = new File("C:\\Program Files\\nodejs", relativeRules);
            options.add(file);
        } else {
            File file = new File("/usr/local/lib", relativeRules);
            options.add(file);
        }
        List<File> valid = ContainerUtil.filter(options, new Condition<File>() {
            @Override
            public boolean value(File file) {
                return file.exists() && file.isDirectory();
            }
        });
        return valid;
    }

    public static List<String> tryFindRulesAsString(final File projectRoot) {
        List<File> files = tryFindRules(projectRoot);
        return ContainerUtil.map(files, new Function<File, String>() {
            public String fun(File file) {
//                return FileUtils.makeRelative(projectRoot, file);
                if (projectRoot != null && FileUtil.isAncestor(projectRoot, file, true)) {
                    return FileUtils.makeRelative(projectRoot, file);
                }
                return file.toString();
            }
        });
    }

    @NotNull
    public static List<File> listPossibleESLintExe() {
        return NodeFinder.searchNodeModulesBin(ESLINT_BASE_NAME);
    }

    @NotNull
    public static List<File> searchForESLintBin(File projectRoot) {
        return NodeFinder.searchAllScopesForBin(projectRoot, ESLINT_BASE_NAME);
    }

    /**
     * find possible eslint rc files
     *
     * @param projectRoot
     * @return
     */
    public static List<String> searchForESLintRCFiles(final File projectRoot) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.equals(ESLINTRC) || name.startsWith(ESLINTRC + '.');
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
}