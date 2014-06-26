package com.eslint.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by idok on 6/26/14.
 * copied from com.intellij.psi.util.PsiUtilCore to fix compatibility issue with webstorm
 */
public final class PsiUtil {
    private PsiUtil() {
    }

    @NotNull
    public static PsiElement getElementAtOffset(@NotNull PsiFile file, int offset) {
        PsiElement elt = file.findElementAt(offset);
        if (elt == null && offset > 0) {
            elt = file.findElementAt(offset - 1);
        }
        if (elt == null) {
            return file;
        }
        return elt;
    }
}
