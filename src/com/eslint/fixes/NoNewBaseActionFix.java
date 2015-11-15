package com.eslint.fixes;

import com.intellij.lang.javascript.psi.JSElementFactory;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.psi.JSNewExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author idok
 */
public abstract class NoNewBaseActionFix extends BaseActionFix {
    protected NoNewBaseActionFix(PsiElement element) {
        super(element);
    }

    protected abstract String getNewExp();

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile psiFile, @Nullable("is null when called from inspection") Editor editor, @NotNull PsiElement element, @NotNull PsiElement end) throws IncorrectOperationException {
        PsiElement parent = element.getParent();
        if (!(parent instanceof JSNewExpression)) return;
        final JSExpressionCodeFragment useStrict = JSElementFactory.createExpressionCodeFragment(project, getNewExp(), parent);
        PsiElement child = useStrict.getFirstChild();
        parent.replace(child);
    }
}
