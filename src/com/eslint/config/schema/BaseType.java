package com.eslint.config.schema;

public class BaseType {
    public String title;
    public ESLintSchema.PropertyType type;
    public String description;

    public BaseType() {
    }

    public static final String ANY_NAME = "*";

    public BaseType(String title, ESLintSchema.PropertyType type, String description) {
        this.title = title;
        this.type = type;
        this.description = description;
    }

    public boolean isValidValue(String value) {
        switch (type) {
            case BOOLEAN:
                return isBoolean(value);
            default:
                return true;
        }
    }

    public static boolean isBoolean(String valueStr) {
        return Boolean.TRUE.toString().equals(valueStr) || Boolean.FALSE.toString().equals(valueStr);
    }

    public static class SchemaBoolean extends BaseType {
        public SchemaBoolean() {
            type = ESLintSchema.PropertyType.BOOLEAN;
        }
    }

    public static class SchemaAny extends BaseType {
        public SchemaAny() {
            type = ESLintSchema.PropertyType.ANY;
        }
    }

    public static class SchemaString extends BaseType {
        public SchemaString() {
            type = ESLintSchema.PropertyType.STRING;
        }
    }
}