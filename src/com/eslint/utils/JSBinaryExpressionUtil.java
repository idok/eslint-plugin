package com.eslint.utils;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSBinaryExpression;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;

public final class JSBinaryExpressionUtil {
    private static final TokenSet BINARY_OPERATIONS = TokenSet.orSet(JSTokenTypes.OPERATIONS, JSTokenTypes.RELATIONAL_OPERATIONS);

    private JSBinaryExpressionUtil() {}

    public static ASTNode getOperator(PsiElement element) {
        PsiElement binary = PsiTreeUtil.findFirstParent(element, new Condition<PsiElement>() {
            @Override
            public boolean value(PsiElement psiElement) {
                return psiElement instanceof JSBinaryExpression;
            }
        });
        return binary == null ? null : binary.getNode().getChildren(BINARY_OPERATIONS)[0];
    }
}
