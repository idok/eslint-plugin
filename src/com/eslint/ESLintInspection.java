package com.eslint;

import com.intellij.codeInspection.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSBinaryExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author cdr
 */
public class ESLintInspection extends PropertySuppressableInspectionBase {

    private static final Logger LOG = Logger.getInstance(ESLintInspection.class);

    @NotNull
    public String getDisplayName() {
        return ESLintBundle.message("eslint.property.inspection.display.name");
    }

    @NotNull
    public String getShortName() {
        return "ESLintInspection";
    }

    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        try {
            if (!(file instanceof JSFileImpl)) return null;
            //        System.out.println("+++++++++++ SampleApplicationPlugin: checkFile");
            //        final List<ProblemDescriptor> descriptors = new SmartList<ProblemDescriptor>();
            ESLintProjectComponent component = file.getProject().getComponent(ESLintProjectComponent.class);
            if (!component.isSettingsValid() || !component.isEnabled()) return null;

            ExecuteShellComand.Result result = ExecuteShellComand.run(file.getProject().getBasePath(), file.getVirtualFile().getPath(), component.eslintExecutable, component.eslintRcFile, component.rulesPath);
            if (StringUtils.isNotEmpty(result.errorOutput)) {
                component.showInfoNotification(result.errorOutput, NotificationType.WARNING);
                return null;
            }
            Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
            final ProblemsHolder problemsHolder = new ProblemsHolder(manager, file, isOnTheFly);
            for (ExecuteShellComand.Warn warn : result.warns) {
                int offset = StringUtil.lineColToOffset(document.getText(), warn.line - 1, warn.column);
                System.out.println("+ " + warn.message + " " + warn.line + ":" + warn.column + " " + offset);
//                PsiElement lit2 = PsiTreeUtil.findElementOfClassAtOffset(file, offset, PsiElement.class, false);
                PsiElement lit = PsiUtil.getElementAtOffset(file, offset);
                LOG.debug("+ " + lit.getText());

                LocalQuickFix fix = getFixForRule(warn.rule);

                final ProblemDescriptor problem = manager.createProblemDescriptor(
                        lit,
                        ESLintBundle.message("eslint.property.inspection.message", warn.message.trim(), warn.rule),
                        fix,
                        warn.level.equals("error") ? ProblemHighlightType.ERROR :
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly);

                problemsHolder.registerProblem(problem);
                //            problemsHolder.registerProblem(lit, lit.getTextRange(), warn.message.trim(), RemoveTrailingSpacesFix.INSTANCE);
                //            descriptors.add(manager.createProblemDescriptor(lit, lit.getTextRange(), warn.message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, RemoveTrailingSpacesFix.INSTANCE));
            }

            //        return descriptors.toArray(new ProblemDescriptor[descriptors.size()]);
            return problemsHolder.getResultsArray();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
        }
        return null;
    }

    private static LocalQuickFix getFixForRule(String rule) {
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
        return null;
    }

    private static class RemoveTrailingSpacesFix implements LocalQuickFix {
        private static final RemoveTrailingSpacesFix INSTANCE = new RemoveTrailingSpacesFix();

        @NotNull
        public String getName() {
            return "ESLint warning";
        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            PsiElement parent = element == null ? null : element.getParent();
//            if (!(parent instanceof PropertyImpl)) return;
//            TextRange textRange = getTrailingSpaces(element);
//            if (textRange != null) {
//                Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
//                TextRange docRange = textRange.shiftRight(element.getTextRange().getStartOffset());
//                document.deleteString(docRange.getStartOffset(), docRange.getEndOffset());
//            }
        }
    }

    private static class SuppressForFileFix implements LocalQuickFix {
        private static final SuppressForFileFix INSTANCE = new SuppressForFileFix();

        @NotNull
        public String getName() {
            return "ESLint warning";
        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            PsiElement parent = element == null ? null : element.getParent();
//            if (!(parent instanceof PropertyImpl)) return;
//            TextRange textRange = getTrailingSpaces(element);
//            if (textRange != null) {
//                Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
//                TextRange docRange = textRange.shiftRight(element.getTextRange().getStartOffset());
//                document.deleteString(docRange.getStartOffset(), docRange.getEndOffset());
//            }
        }
    }

    private static class MissingUseStrictFix implements LocalQuickFix {
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

    private static class NoNewObjectFix implements LocalQuickFix {
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

    private static class NoArrayConstructorFix implements LocalQuickFix {
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

    private static class EqeqeqFix implements LocalQuickFix {
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
            document.replaceString(op.getStartOffset(), op.getStartOffset() + op.getTextLength(), "===");
        }
    }
}