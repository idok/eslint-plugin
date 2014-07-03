package com.eslint.fixes;

import com.eslint.ESLintBundle;
import com.intellij.lang.javascript.psi.JSIfStatement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author idok
 */
public class NoLonelyIfActionFix extends BaseActionFix {
    public NoLonelyIfActionFix(PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public String getText() {
        return ESLintBundle.message("inspection.fix.no-lonely-if");
    }

    @Override
    public void fix(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        JSIfStatement ifStatement = PsiTreeUtil.getParentOfType(element, JSIfStatement.class);
        JSIfStatement parentIf = PsiTreeUtil.getParentOfType(ifStatement, JSIfStatement.class);
        parentIf.getElse().replace(ifStatement);
    }
}
