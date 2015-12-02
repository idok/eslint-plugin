package com.eslint.config;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * @author idok
 */
public final class ESLintConfigFileUtil {
    private ESLintConfigFileUtil() {
    }

    private static final Set<String> FILES = createSet();

    public static boolean isESLintConfigFile(JSFile file) {
        return file != null && (isESLintConfigFile(file.getVirtualFile()) || file.getFileType().equals(ESLintConfigFileType.INSTANCE));
    }

    private static Set<String> createSet() {
        Set<String> s = new HashSet<String>();
        Collections.addAll(s, ESLintConfigFileType.ESLINTRC_FILES);
        return s;
    }

    public static boolean isRC(String fileName) {
        return FILES.contains(fileName);
    }

    public static boolean isESLintConfigFile(PsiElement position) {
        return isESLintConfigFile(position.getContainingFile().getOriginalFile().getVirtualFile());
    }

    public static boolean isESLintConfigFile(VirtualFile file) {
//        return file != null && file.getName().equals(ESLintConfigFileType.ESLINTRC);
        return file != null && isRC(file.getName());
    }

    @Nullable
    public static JSProperty getProperty(@NotNull PsiElement position) {
        JSProperty property = PsiTreeUtil.getParentOfType(position, JSProperty.class, false);
        if (property != null) {
            JSObjectLiteralExpression objectLiteralExpression = ObjectUtils.tryCast(property.getParent(), JSObjectLiteralExpression.class);
            if (objectLiteralExpression != null) {
                return property;
            }
        }
        return null;
    }

    @Nullable
    public static PsiElement getStringLiteral(@NotNull JSProperty property) {
        PsiElement firstElement = property.getFirstChild();
        if (firstElement != null && isStringLiteral(firstElement)) {
            return firstElement;
        }
        return null;
    }

    public static boolean isStringLiteral(@NotNull PsiElement element) {
        if (element instanceof ASTNode) {
            ASTNode node = (ASTNode) element;
            return node.getElementType().equals(JSTokenTypes.STRING_LITERAL);
        }
        return false;
    }
}
