package com.eslint.config;

import com.intellij.lang.javascript.json.JSONLanguageDialect;
import com.intellij.openapi.fileTypes.LanguageFileType;

import javax.swing.Icon;

import icons.ESLintIcons;
import org.jetbrains.annotations.NotNull;

public class ESLintConfigFileType extends LanguageFileType {
    public static final ESLintConfigFileType INSTANCE = new ESLintConfigFileType();
    public static final String ESLINTRC = "eslintrc";

    private ESLintConfigFileType() {
        super(JSONLanguageDialect.JSON);
    }

    @NotNull
    public String getName() {
        return "ESLint";
    }

    @NotNull
    public String getDescription() {
        return "ESLint configuration file";
    }

    @NotNull
    public String getDefaultExtension() {
        return ESLINTRC;
    }

    @NotNull
    public Icon getIcon() {
        return ESLintIcons.ESLint;
    }
}