package com.eslint.fixes;

import com.eslint.ESLintBundle;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author idok
 */
public class StrictActionFix extends BaseActionFix {
    public StrictActionFix(PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public String getText() {
        return ESLintBundle.message("inspection.fix.strict");
    }

    @Override
    public void fix(@NotNull final Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        final PsiElement parent = element == null ? null : element.getParent();
        if (!(parent instanceof JSFunctionExpression || parent instanceof JSFunction)) return;

//            if (parent.getChildren().length < 2) {
//                return;
//            }

        JSBlockStatement block = null;
        for (PsiElement elem : parent.getChildren()) {
            if (elem instanceof JSBlockStatement) {
                block = (JSBlockStatement) elem;
                break;
            }
        }

        if (block != null) {
            TextRange textRange = block.getTextRange();

//                PsiTreeUtil. JSPsiImplUtils
            final JSExpressionCodeFragment useStrict = JSElementFactory.createExpressionCodeFragment(project, "'use strict';\n", block);
            PsiElement child = useStrict.getFirstChild();
            if (block.getStatements().length == 0) {
                block.add(child);
            } else {
                block.addBefore(child, block.getStatements()[0]);
            }
//                final JSBlockStatement finalBlock = block;
//                ApplicationManager.getApplication().runWriteAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        finalBlock.add(useStrict);
//                    }
//                });

//                if (textRange != null) {
//                    Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
////                    TextRange docRange = textRange.shiftRight(element.getTextRange().getStartOffset());
//                    document.insertString(textRange.getStartOffset() + 1, "\n'use strict';");
//                }
        }
    }
}
