package com.eslint.fixes;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author idok
 */
public abstract class BaseActionFix implements IntentionAction, HighPriorityAction {
    protected final PsiElement element;
    public BaseActionFix(PsiElement element) {
        this.element = element;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    protected abstract void fix(@NotNull Project project, Editor editor, PsiFile file);

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        fix(project, editor, file);
        DaemonCodeAnalyzer.getInstance(project).restart(file);
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
