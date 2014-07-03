package com.eslint.fixes;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author idok
 */
public class SuppressActionFix implements IntentionAction {
    private static final Logger LOG = Logger.getInstance(SuppressActionFix.class);
    private final PsiElement element;
    private final String rule;


    public SuppressActionFix(String rule, PsiElement element) {
        this.element = element;
        this.rule = rule;
    }

    @NotNull
    @Override
    public String getText() {
        return "Suppress ESLint rule";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "ESLint";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
//        final PsiFile file = element.getContainingFile();
        if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;

//  InspectionManager inspectionManager = InspectionManager.getInstance(project);
//  ProblemDescriptor descriptor = inspectionManager.createProblemDescriptor(element, element, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false);

        final JSElement property = PsiTreeUtil.getParentOfType(element, JSElement.class);
        LOG.assertTrue(property != null);
        final int start = property.getTextRange().getStartOffset();

        @NonNls final Document doc = PsiDocumentManager.getInstance(project).getDocument(file);
        LOG.assertTrue(doc != null);
        final int line = doc.getLineNumber(start);
        final int lineStart = doc.getLineStartOffset(line);

        doc.insertString(lineStart, "/*eslint " + rule + ":0*/\n");
        DaemonCodeAnalyzer.getInstance(project).restart(file);
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
