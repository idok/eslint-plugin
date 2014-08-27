package com.eslint.fixes;

import com.eslint.ESLintBundle;
import com.intellij.lang.javascript.psi.JSElementFactory;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.psi.JSNewExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author idok
 */
public class NoNewObjectActionFix extends NoNewBaseActionFix {
    public NoNewObjectActionFix(PsiElement element) {
        super(element);
    }

    @Override
    protected String getNewExp() {
        return "{}";
    }

    @NotNull
    @Override
    public String getText() {
        return ESLintBundle.message("inspection.fix.no.new.object");
    }
}
