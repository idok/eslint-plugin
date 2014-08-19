package com.eslint.fixes;

import com.eslint.ESLintBundle;
import com.eslint.utils.JSBinaryExpressionUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
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
        ASTNode op = JSBinaryExpressionUtil.getOperator(element);
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
