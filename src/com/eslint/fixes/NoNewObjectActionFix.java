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
public class NoNewObjectActionFix extends BaseActionFix {
    public NoNewObjectActionFix(PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public String getText() {
        return ESLintBundle.message("inspection.fix.no.new.object");
    }

    @Override
    public void fix(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        PsiElement parent = element == null ? null : element.getParent();
        if (!(parent instanceof JSNewExpression)) return;
        final JSExpressionCodeFragment useStrict = JSElementFactory.createExpressionCodeFragment(project, "{}", parent);
        PsiElement child = useStrict.getFirstChild();
        parent.replace(child);
    }
}
