package com.eslint.fixes;

import com.eslint.ESLintBundle;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author idok
 */
public class DotNotationActionFix extends BaseActionFix {
    public DotNotationActionFix(PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public String getText() {
        return ESLintBundle.message("inspection.fix.dot-notation");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile psiFile, @Nullable("is null when called from inspection") Editor editor, @NotNull PsiElement element, @NotNull PsiElement end) throws IncorrectOperationException {
        JSIndexedPropertyAccessExpression indexed = PsiTreeUtil.getParentOfType(element, JSIndexedPropertyAccessExpression.class);
        JSReferenceExpression ref = PsiTreeUtil.findChildOfType(indexed, JSReferenceExpression.class);
        JSLiteralExpression literalExpression = (JSLiteralExpression) indexed.getIndexExpression();
        String path = StringUtil.stripQuotesAroundValue(literalExpression.getText());
        ASTNode dotExp = JSChangeUtil.createStatementFromText(project, ref.getText() + '.' + path);
        indexed.replace(dotExp.getPsi());
    }
}
