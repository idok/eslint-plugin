package com.eslint.utils;

import com.eslint.ESLintBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.List;

public final class FileUtils {
    private FileUtils() {
    }

    private static final Logger LOG = Logger.getInstance(ESLintBundle.LOG_ID);

    // WildcardFileNameMatcher w = new WildcardFileNameMatcher("**/.eslintrc");

    @NotNull
    public static List<String> displayDirectoryContents(@NotNull File projectRoot, @NotNull File dir, @NotNull FilenameFilter filter) {
        List<String> ret = listFiles(projectRoot, dir, filter);
        File[] files = dir.listFiles();
        for (final File file : files) {
            if (file.isDirectory()) {
                ret.addAll(displayDirectoryContents(projectRoot, file, filter));
//            } else {
//                listFiles(file, filter, allFiles);
            }
        }
        return ret;
    }

    @NotNull
    public static List<String> listFiles(@NotNull final File projectRoot, @NotNull final File dir, @NotNull FilenameFilter filter) {
        String[] curFiles = dir.list(filter);
        //        allFiles.addAll(ret); //Arrays.asList(curFiles));
        return ContainerUtil.map(curFiles, new Function<String, String>() {
            public String fun(String curFile) {
                return new File(dir, curFile).getAbsolutePath().substring(projectRoot.getAbsolutePath().length() + 1);
            }
        });
    }

    @NotNull
    private static List<File> findExeFilesInPath(@Nullable String pathEnvVarValue,
                                                 @NotNull String fileBaseName,
                                                 boolean stopAfterFirstMatch,
                                                 boolean logDetails) {
        if (logDetails) {
            LOG.info("Finding files in PATH (base name=" + fileBaseName + ", PATH=" + StringUtil.notNullize(pathEnvVarValue) + ").");
        }
        if (pathEnvVarValue == null) {
            return Collections.emptyList();
        }
        List<File> result = new SmartList<File>();
        List<String> paths = StringUtil.split(pathEnvVarValue, File.pathSeparator, true, true);
        for (String path : paths) {
            File dir = new File(path);
            if (logDetails) {
                File file = new File(dir, fileBaseName);
                LOG.info("path:" + path + ", path.isAbsolute:" + dir.isAbsolute() + ", path.isDirectory:" + dir.isDirectory()
                        + ", file.isFile:" + file.isFile() + ", file.canExecute:" + file.canExecute());
            }
            if (dir.isAbsolute() && dir.isDirectory()) {
                File file = new File(dir, fileBaseName);
                if (file.isFile() && file.canExecute()) {
                    result.add(file);
                    if (stopAfterFirstMatch) {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    public static ValidationStatus validateProjectPath(Project project, String path, boolean allowEmpty, boolean isFile) {
        if (StringUtils.isEmpty(path)) {
            return allowEmpty ? ValidationStatus.VALID : ValidationStatus.IS_EMPTY;
        }
        File filePath = new File(path);
        if (filePath.isAbsolute()) {
            if (!filePath.exists()) {
                return ValidationStatus.DOES_NOT_EXIST;
            }
            if (isFile) {
                if (!filePath.isFile()) {
                    return ValidationStatus.NOT_A_FILE;
                }
            } else {
                if (!filePath.isDirectory()) {
                    return ValidationStatus.NOT_A_DIRECTORY;
                }
            }
        } else {
            if (project == null) {
                return ValidationStatus.DOES_NOT_EXIST;
            }
            VirtualFile child = project.getBaseDir().findFileByRelativePath(path);
            if (child == null || !child.exists()) {
                return ValidationStatus.DOES_NOT_EXIST;
            }
            if (isFile) {
                if (child.isDirectory()) {
                    return ValidationStatus.NOT_A_FILE;
                }
            } else {
                if (!child.isDirectory()) {
                    return ValidationStatus.NOT_A_DIRECTORY;
                }
            }
        }
        return ValidationStatus.VALID;
    }

    //    public static class ValidationError {
    //        public boolean a;
    //    }
    //
    public enum ValidationStatus {
        VALID, IS_EMPTY, DOES_NOT_EXIST, NOT_A_DIRECTORY, NOT_A_FILE
    }
}
