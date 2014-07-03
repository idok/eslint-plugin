package com.eslint.fixes;

import com.eslint.ESLintBundle;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSBinaryExpression;
import com.intellij.lang.javascript.psi.impl.JSBinaryExpressionImpl;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author idok
 */
public class EqeqeqActionFix extends BaseActionFix {
    public EqeqeqActionFix(PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public String getText() {
        return ESLintBundle.message("inspection.fix.eqeqeq");
    }

    @Override
    public void fix(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
//            PsiElement parent = element == null ? null : element.getParent();
        PsiElement binary = PsiTreeUtil.findFirstParent(element, new Condition<PsiElement>() {
            @Override
            public boolean value(PsiElement psiElement) {
                return psiElement instanceof JSBinaryExpression;
            }
        });
        ASTNode op = ((JSBinaryExpressionImpl) binary).getOperator();
        Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());

        String replace = "";
        if (op.getText().equals("==")) {
            replace = "===";
        } else if (op.getText().equals("!=")) {
            replace = "!==";
        }
        document.replaceString(op.getStartOffset(), op.getStartOffset() + op.getTextLength(), replace);
    }
}
