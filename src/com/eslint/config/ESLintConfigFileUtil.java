package com.eslint.config;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

/**
 * @author idok
 */
public final class ESLintConfigFileUtil {
    private ESLintConfigFileUtil() {
    }

    public static boolean isESLintConfigFile(JSFile file) {
        if (file == null) {
            return false;
        }
        if (isESLintConfigFile(file.getVirtualFile())) {
            return true;
        }
        if (file.getFileType().equals(ESLintConfigFileType.INSTANCE)) {
            return true;
        }
        return false;
    }

    public static boolean isESLintConfigFile(PsiElement position) {
        return isESLintConfigFile(position.getContainingFile().getOriginalFile().getVirtualFile());
    }

    public static boolean isESLintConfigFile(VirtualFile file) {
        return file != null && file.getExtension() != null &&
                file.getExtension().equals(ESLintConfigFileType.ESLINTRC);
    }

}
