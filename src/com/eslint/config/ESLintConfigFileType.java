package com.eslint.config;

import com.intellij.lang.javascript.json.JSONLanguageDialect;
import com.intellij.openapi.fileTypes.LanguageFileType;

import javax.swing.Icon;

import icons.ESLintIcons;
import org.jetbrains.annotations.NotNull;

public class ESLintConfigFileType extends LanguageFileType {
    public static final ESLintConfigFileType INSTANCE = new ESLintConfigFileType();
    public static final String ESLINTRC_EXT = "eslintrc";
    public static final String ESLINTRC = '.' + ESLINTRC_EXT;

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
        return ESLINTRC_EXT;
    }

    @NotNull
    public Icon getIcon() {
        return ESLintIcons.ESLint;
    }
}