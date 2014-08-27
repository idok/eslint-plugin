package com.eslint.config;

import com.intellij.openapi.fileTypes.ExactFileNameMatcher;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class ESLintConfigFileTypeFactory extends FileTypeFactory {
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(ESLintConfigFileType.INSTANCE, new ExactFileNameMatcher(ESLintConfigFileType.ESLINTRC));
//                new ExtensionFileNameMatcher(ESLintConfigFileType.ESLINTRC), new ExactFileNameMatcher("eslint.json"));
    }
}