package com.eslint.config.schema;

public class SchemaJsonObject extends BaseType {
    public BaseType[] properties;

    public SchemaJsonObject() {
        type = ESLintSchema.PropertyType.OBJECT;
    }

    public SchemaJsonObject(String title, ESLintSchema.PropertyType type, String description, BaseType[] properties) {
        super(title, type, description);
        this.properties = properties;
    }

    public BaseType find(String name) {
        for (BaseType b : properties) {
            if (b.title.equals(name)) {
                return b;
            }
        }
        return null;
    }

    public <T extends BaseType> T findOfType(String name) {
        for (BaseType b : properties) {
            if (b.title.equals(name)) {
//                if (b instanceof T) {
                return (T) b;
//                }
            }
        }
        return null;
    }
}
