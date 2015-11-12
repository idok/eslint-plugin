package com.eslint;

import com.eslint.config.ESLintConfigFileListener;
import com.eslint.fixes.BaseActionFix;
import com.eslint.fixes.Fixes;
import com.eslint.fixes.SuppressActionFix;
import com.eslint.fixes.SuppressLineActionFix;
import com.eslint.utils.ESLintRunner;
import com.eslint.utils.Result;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.impl.SeverityRegistrar;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.wix.ActualFile;
import com.wix.ThreadLocalActualFile;
import com.wix.annotator.ExternalLintAnnotationInput;
import com.wix.annotator.ExternalLintAnnotationResult;
import com.wix.utils.FileUtils;
import com.wix.utils.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author idok
 */
public class ESLintExternalAnnotator extends ExternalAnnotator<ExternalLintAnnotationInput, ExternalLintAnnotationResult<Result>> {

    public static final ESLintExternalAnnotator INSTANCE = new ESLintExternalAnnotator();
    private static final Logger LOG = Logger.getInstance(ESLintBundle.LOG_ID);
    private static final String MESSAGE_PREFIX = "ESLint: ";
    private static final Key<ThreadLocalActualFile> ESLINT_TEMP_FILE_KEY = Key.create("ESLINT_TEMP_FILE");
//    private static final int TABS = 4;
//    private int tabSize;

//    private static int getTabSize(@NotNull Editor editor) {
//        // Get tab size
//        int tabSize = 0;
//        Project project = editor.getProject();
//        PsiFile psifile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
//        CommonCodeStyleSettings commonCodeStyleSettings = new CommonCodeStyleSettings(psifile.getLanguage());
//        CommonCodeStyleSettings.IndentOptions indentOptions = commonCodeStyleSettings.getIndentOptions();
//
//        if (indentOptions != null) {
//            tabSize = commonCodeStyleSettings.getIndentOptions().TAB_SIZE;
//        }
//        if (tabSize == 0) {
//            tabSize = editor.getSettings().getTabSize(editor.getProject());
//        }
//        return tabSize;
//    }

    @Nullable
    @Override
    public ExternalLintAnnotationInput collectInformation(@NotNull PsiFile file) {
        return collectInformation(file, null);
    }

    @Nullable
    @Override
    public ExternalLintAnnotationInput collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        return collectInformation(file, editor);
    }

    @NotNull
    public static HighlightDisplayKey getHighlightDisplayKeyByClass() {
        String id = "ESLint";
        HighlightDisplayKey key = HighlightDisplayKey.find(id);
        if (key == null) {
            key = new HighlightDisplayKey(id, id);
        }
        return key;
    }

    @Override
    public void apply(@NotNull PsiFile file, ExternalLintAnnotationResult<Result> annotationResult, @NotNull AnnotationHolder holder) {
        if (annotationResult == null) {
            return;
        }
        InspectionProjectProfileManager inspectionProjectProfileManager = InspectionProjectProfileManager.getInstance(file.getProject());
        SeverityRegistrar severityRegistrar = inspectionProjectProfileManager.getSeverityRegistrar();
        HighlightDisplayKey inspectionKey = getHighlightDisplayKeyByClass();
        EditorColorsScheme colorsScheme = annotationResult.input.colorsScheme;

        Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        if (document == null) {
            return;
        }
        ESLintProjectComponent component = annotationResult.input.project.getComponent(ESLintProjectComponent.class);
        for (Result.Warn warn : annotationResult.result.warns) {
            HighlightSeverity severity = getHighlightSeverity(warn, component.treatAsWarnings);
            TextAttributes forcedTextAttributes = JSLinterUtil.getTextAttributes(colorsScheme, severityRegistrar, severity);
            Annotation annotation = createAnnotation(holder, file, document, warn, severity, forcedTextAttributes, false);
            if (annotation != null) {
                int offset = StringUtil.lineColToOffset(document.getText(), warn.line - 1, warn.column);
                PsiElement lit = PsiUtil.getElementAtOffset(file, offset);
                BaseActionFix actionFix = Fixes.getFixForRule(warn.rule, lit);
                if (actionFix != null) {
                    annotation.registerFix(actionFix, null, inspectionKey);
                }
                annotation.registerFix(new SuppressActionFix(warn.rule, lit), null, inspectionKey);
                annotation.registerFix(new SuppressLineActionFix(warn.rule, lit), null, inspectionKey);
            }
        }
    }

    private static HighlightSeverity getHighlightSeverity(Result.Warn warn, boolean treatAsWarnings) {
        if (treatAsWarnings) {
            return HighlightSeverity.WARNING;
        }
        return warn.level.equals("error") ? HighlightSeverity.ERROR : HighlightSeverity.WARNING;
    }

    @Nullable
    private static Annotation createAnnotation(@NotNull AnnotationHolder holder, @NotNull PsiFile file, @NotNull Document document, @NotNull Result.Warn warn,
                                               @NotNull HighlightSeverity severity, @Nullable TextAttributes forcedTextAttributes,
                                               boolean showErrorOnWholeLine) {
        int line = warn.line - 1;
        int column = warn.column - 1;

        if (line < 0 || line >= document.getLineCount()) {
            return null;
        }
        int lineEndOffset = document.getLineEndOffset(line);
        int lineStartOffset = document.getLineStartOffset(line);

        int errorLineStartOffset = StringUtil.lineColToOffset(document.getCharsSequence(), line, column);
//        int errorLineStartOffset = PsiUtil.calcErrorStartOffsetInDocument(document, lineStartOffset, lineEndOffset, column, tab);

        if (errorLineStartOffset == -1) {
            return null;
        }
//        PsiElement element = file.findElementAt(errorLineStartOffset);
        TextRange range;
        if (showErrorOnWholeLine) {
            range = new TextRange(lineStartOffset, lineEndOffset);
        } else {
//            int offset = StringUtil.lineColToOffset(document.getText(), warn.line - 1, warn.column);
            PsiElement lit = PsiUtil.getElementAtOffset(file, errorLineStartOffset);
            range = lit.getTextRange();
//            range = new TextRange(errorLineStartOffset, errorLineStartOffset + 1);
        }

        Annotation annotation = JSLinterUtil.createAnnotation(holder, severity, forcedTextAttributes, range, MESSAGE_PREFIX + warn.message.trim() + " (" + warn.rule + ')');
        if (annotation != null) {
            annotation.setAfterEndOfLine(errorLineStartOffset == lineEndOffset);
        }
        return annotation;
    }

    @Nullable
    private static ExternalLintAnnotationInput collectInformation(@NotNull PsiFile psiFile, @Nullable Editor editor) {
        if (psiFile.getContext() != null) {
            return null;
        }
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null || !virtualFile.isInLocalFileSystem()) {
            return null;
        }
        if (psiFile.getViewProvider() instanceof MultiplePsiFilesPerDocumentFileViewProvider) {
            return null;
        }
        Project project = psiFile.getProject();
        ESLintProjectComponent component = project.getComponent(ESLintProjectComponent.class);
        if (!component.isSettingsValid() || !component.isEnabled() || !isJavaScriptFile(psiFile, component.ext)) {
            return null;
        }
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) {
            return null;
        }
        String fileContent = document.getText();
        if (StringUtil.isEmptyOrSpaces(fileContent)) {
            return null;
        }
        EditorColorsScheme colorsScheme = editor == null ? null : editor.getColorsScheme();
//        tabSize = getTabSize(editor);
//        tabSize = 4;
        return new ExternalLintAnnotationInput(project, psiFile, fileContent, colorsScheme);
    }

    private static boolean isInList(String file, String ext) {
        if (StringUtils.isEmpty(ext)) {
            return false;
        }
        String[] exts = ext.split(",");
        for (String ex : exts) {
            if (file.endsWith(ex)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isJavaScriptFile(PsiFile file, String ext) {
        return file instanceof JSFile && file.getFileType().equals(JavaScriptFileType.INSTANCE) || isInList(file.getName(), ext);
    }

    @Nullable
    @Override
    public ExternalLintAnnotationResult<Result> doAnnotate(ExternalLintAnnotationInput collectedInfo) {
        try {
            LOG.info("Running ESLint inspection");
            PsiFile file = collectedInfo.psiFile;
            ESLintProjectComponent component = file.getProject().getComponent(ESLintProjectComponent.class);
            if (!component.isSettingsValid() || !component.isEnabled() || !isJavaScriptFile(file, component.ext)) {
                return null;
            }
            ESLintConfigFileListener.start(collectedInfo.project);
            String relativeFile;
            ActualFile actualCodeFile = ActualFile.getOrCreateActualFile(ESLINT_TEMP_FILE_KEY, file.getVirtualFile(), collectedInfo.fileContent);
            if (actualCodeFile == null || actualCodeFile.getFile() == null) {
                return null;
            }
            relativeFile = FileUtils.makeRelative(new File(file.getProject().getBasePath()), actualCodeFile.getActualFile());
            Result result = ESLintRunner.lint(file.getProject().getBasePath(), relativeFile, component.nodeInterpreter, component.eslintExecutable, component.eslintRcFile, component.customRulesPath, component.settings.ext);
            actualCodeFile.deleteTemp();
            if (StringUtils.isNotEmpty(result.errorOutput)) {
                component.showInfoNotification(result.errorOutput, NotificationType.WARNING);
                return null;
            }
            Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
            if (document == null) {
                component.showInfoNotification("Error running ESLint inspection: Could not get document for file " + file.getName(), NotificationType.WARNING);
                LOG.error("Could not get document for file " + file.getName());
                return null;
            }
            return new ExternalLintAnnotationResult<Result>(collectedInfo, result);
        } catch (Exception e) {
            LOG.error("Error running ESLint inspection: ", e);
            ESLintProjectComponent.showNotification("Error running ESLint inspection: " + e.getMessage(), NotificationType.ERROR);
        }
        return null;
    }
}