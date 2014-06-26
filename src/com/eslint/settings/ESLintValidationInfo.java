package com.eslint.settings;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.JTextComponent;

public class ESLintValidationInfo {
    public static final String LINK_TEMPLATE = "{{LINK}}";
    private static final Logger LOG = Logger.getInstance(ESLintValidationInfo.class);
    private final JTextComponent myTextComponent;
    private final String myErrorHtmlDescription;
    private final String myLinkText;

    public ESLintValidationInfo(@Nullable JTextComponent textComponent, @NotNull String errorHtmlDescriptionTemplate, @NotNull String linkText) {
        this.myTextComponent = textComponent;
        if (!errorHtmlDescriptionTemplate.contains("{{LINK}}")) {
            LOG.warn("Cannot find {{LINK}} in " + errorHtmlDescriptionTemplate);
        }
        String linkHtml = "<a href='" + linkText + "'>" + linkText + "</a>";
        this.myErrorHtmlDescription = errorHtmlDescriptionTemplate.replace("{{LINK}}", linkHtml);
        this.myLinkText = linkText;
    }

    @Nullable
    public JTextComponent getTextComponent() {
        return this.myTextComponent;
    }

    @NotNull
    public String getErrorHtmlDescription() {
        return this.myErrorHtmlDescription;
    }

    @Nullable
    public String getLinkText() {
        return this.myLinkText;
    }
}