package com.eslint.fixes;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
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
public abstract class BaseActionFix extends LocalQuickFixAndIntentionActionOnPsiElement implements IntentionAction, HighPriorityAction {
    public BaseActionFix(PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

//    @Override
//    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
//        return true;
//    }

//    protected abstract void fix(@NotNull Project project, Editor editor, PsiFile file, PsiElement start);

//    @Override
//    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
//        fix(project, editor, file);
//        DaemonCodeAnalyzer.getInstance(project).restart(file);
//    }

//    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable("is null when called from inspection") Editor editor, @NotNull PsiElement start, @NotNull PsiElement end) {
//        invoke(project, editor, file, start);
//    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
