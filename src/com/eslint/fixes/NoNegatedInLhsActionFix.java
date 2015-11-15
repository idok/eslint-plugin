package com.eslint.fixes;

import com.eslint.ESLintBundle;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSBinaryExpression;
import com.intellij.lang.javascript.psi.JSParenthesizedExpression;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author idok
 */
public class NoNegatedInLhsActionFix extends BaseActionFix {
    public NoNegatedInLhsActionFix(PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public String getText() {
        return ESLintBundle.message("inspection.fix.no-negated-in-lhs");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile psiFile, @Nullable("is null when called from inspection") Editor editor, @NotNull PsiElement element, @NotNull PsiElement end) throws IncorrectOperationException {
//        PsiElement element = descriptor.getPsiElement();
        JSBinaryExpression binary = PsiTreeUtil.getParentOfType(element, JSBinaryExpression.class);
        JSBinaryExpression binaryClone = (JSBinaryExpression) binary.copy();
        binaryClone.getLOperand().replace(binary.getLOperand().getLastChild());
        ASTNode negate = JSChangeUtil.createStatementFromText(project, "!(true)");
        JSParenthesizedExpression paren = PsiTreeUtil.getChildOfType(negate.getPsi().getFirstChild(), JSParenthesizedExpression.class);
        paren.getInnerExpression().replace(binaryClone);
        binary.replace(negate.getPsi());
    }
}
