package com.eslint.fixes;

import com.eslint.ESLintBundle;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSBinaryExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public final class Fixes {
    private Fixes() {
    }

    public static LocalQuickFix getFixForRule(String rule) {
        if (rule.equals("strict")) {
            return MissingUseStrictFix.INSTANCE;
        }
        if (rule.equals("no-new-object")) {
            return NoNewObjectFix.INSTANCE;
        }
        if (rule.equals("no-array-constructor")) {
            return NoArrayConstructorFix.INSTANCE;
        }
        if (rule.equals("eqeqeq")) {
            return EqeqeqFix.INSTANCE;
        }
        if (rule.equals("no-negated-in-lhs")) {
            return NoNegatedInLhsFix.INSTANCE;
        }
        return null;
    }

    public static class MissingUseStrictFix implements LocalQuickFix {
        private static final MissingUseStrictFix INSTANCE = new MissingUseStrictFix();

        @NotNull
        public String getName() {
            return ESLintBundle.message("inspection.fix.strict");
        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            PsiElement parent = element == null ? null : element.getParent();
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
                final JSExpressionCodeFragment useStrict = JSElementFactory.createExpressionCodeFragment(project, "'use strict';", block);
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

    public static class NoNewObjectFix implements LocalQuickFix {
        private static final NoNewObjectFix INSTANCE = new NoNewObjectFix();

        @NotNull
        public String getName() {
            return ESLintBundle.message("inspection.fix.no.new.object");
        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            PsiElement parent = element == null ? null : element.getParent();
            if (!(parent instanceof JSNewExpression)) return;
            final JSExpressionCodeFragment useStrict = JSElementFactory.createExpressionCodeFragment(project, "{}", parent);
            PsiElement child = useStrict.getFirstChild();
            parent.replace(child);
        }
    }

    public static class NoArrayConstructorFix implements LocalQuickFix {
        private static final NoArrayConstructorFix INSTANCE = new NoArrayConstructorFix();

        @NotNull
        public String getName() {
            return ESLintBundle.message("inspection.fix.no-array-constructor");
        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            PsiElement parent = element == null ? null : element.getParent();
            if (!(parent instanceof JSNewExpression)) return;
            final JSExpressionCodeFragment useStrict = JSElementFactory.createExpressionCodeFragment(project, "[]", parent);
            PsiElement child = useStrict.getFirstChild();
            parent.replace(child);
        }
    }

    public static class EqeqeqFix implements LocalQuickFix {
        private static final EqeqeqFix INSTANCE = new EqeqeqFix();

        @NotNull
        public String getName() {
            return ESLintBundle.message("inspection.fix.eqeqeq");
        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
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

    public static class NoNegatedInLhsFix implements LocalQuickFix {
        private static final NoNegatedInLhsFix INSTANCE = new NoNegatedInLhsFix();

        @NotNull
        public String getName() {
            return ESLintBundle.message("inspection.fix.eqeqeq");
        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
//            PsiElement parent = element == null ? null : element.getParent();
            PsiElement binaryElem = PsiTreeUtil.findFirstParent(element, new Condition<PsiElement>() {
                @Override
                public boolean value(PsiElement psiElement) {
                    return psiElement instanceof JSBinaryExpression;
                }
            });
//            JSBinaryExpression binary = (JSBinaryExpression) binaryElem;
//            binary.getLOperand().replace(binary.getLOperand().getLastChild());
////            final JSExpressionCodeFragment parenthesis = JSElementFactory.createExpressionCodeFragment(project, "()", binary.getParent());
//            ASTNode parenthesis = JSChangeUtil.createStatementFromText(project, "()");
////            PsiUtil.deparenthesizeExpression()
//            parenthesis.addChild(binary.copy().getNode());
////            parenthesis.getFirstChild().add(binary.copy());
//
//            binary.replace(parenthesis.getFirstChild());

            JSBinaryExpression binary = (JSBinaryExpression) binaryElem;
            binary.getLOperand().replace(binary.getLOperand().getLastChild());
//            final JSExpressionCodeFragment parenthesis = JSElementFactory.createExpressionCodeFragment(project, "()", binary.getParent());
            ASTNode parenthesis = JSChangeUtil.createStatementFromText(project, "()");
//            PsiUtil.deparenthesizeExpression()
            parenthesis.addChild(binary.copy().getNode());
//            parenthesis.getFirstChild().add(binary.copy());

            binaryElem.getParent().getNode().replaceChild(binary.getNode(), parenthesis);
//            binary.getNode().replaceChild(parenthesis.getFirstChild());
        }
    }

//    private static class RemoveTrailingSpacesFix implements LocalQuickFix {
//        private static final RemoveTrailingSpacesFix INSTANCE = new RemoveTrailingSpacesFix();
//
//        @NotNull
//        public String getName() {
//            return "ESLint warning";
//        }
//
//        @NotNull
//        public String getFamilyName() {
//            return getName();
//        }
//
//        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
//            PsiElement element = descriptor.getPsiElement();
//            PsiElement parent = element == null ? null : element.getParent();
////            if (!(parent instanceof PropertyImpl)) return;
////            TextRange textRange = getTrailingSpaces(element);
////            if (textRange != null) {
////                Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
////                TextRange docRange = textRange.shiftRight(element.getTextRange().getStartOffset());
////                document.deleteString(docRange.getStartOffset(), docRange.getEndOffset());
////            }
//        }
//    }
}