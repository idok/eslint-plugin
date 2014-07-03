package com.eslint.config.schema;

import com.google.gson.*;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public final class ESLintSchema {
//    public RootProp properties;

//    public static final SchemaJsonObject instance = load();

    public static SchemaJsonObject ROOT = new SchemaJsonObject("root", PropertyType.OBJECT, "", new BaseType[]{
            new SchemaJsonObject("env", PropertyType.OBJECT, "env",
                    new BaseType[]{
                            new BaseType("amd", PropertyType.BOOLEAN, "amd"),
                            new BaseType("node", PropertyType.BOOLEAN, "node"),
                            new BaseType("browser", PropertyType.BOOLEAN, "browser")
                    }
            ),
            new SchemaJsonObject("globals", PropertyType.OBJECT, "globals", new BaseType[]{
                    new BaseType(BaseType.ANY_NAME, PropertyType.BOOLEAN, "")
            }),
            new SchemaJsonObject("rules", PropertyType.OBJECT, "rules", new BaseType[]{})
    }
    );

    private ESLintSchema() {
    }

    public static void buildSchema() {
        BaseType rules = ROOT.find("rules");
        if (rules != null) {
            List<BaseType> rulesMap = ContainerUtil.map(RuleCache.instance.rulesMap, new Function<String, BaseType>() {
                public BaseType fun(String rule) {
                    return new BaseType(rule, PropertyType.ANY, rule);
                }
            });
            if (rules instanceof SchemaJsonObject) {
                SchemaJsonObject obj = (SchemaJsonObject) rules;
                obj.properties = rulesMap.toArray(new BaseType[rulesMap.size()]);
            }
        }
    }

//    public static Gson getGson() {
//        //            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        GsonBuilder builder = new GsonBuilder();
//        builder.registerTypeAdapter(BaseType.class, new BaseTypeAdapter());
//        return builder.setPrettyPrinting().create();
//    }

    public static Gson getGson() {
        RuntimeTypeAdapterFactory<BaseType> adapter = RuntimeTypeAdapterFactory.of(BaseType.class, "type")
                .registerSubtype(SchemaJsonObject.class, PropertyType.OBJECT.name())
                .registerSubtype(BaseType.SchemaString.class, PropertyType.STRING.name())
                .registerSubtype(BaseType.SchemaAny.class, PropertyType.ANY.name())
                .registerSubtype(BaseType.SchemaBoolean.class, PropertyType.BOOLEAN.name());
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(adapter);
        return builder.setPrettyPrinting().create();
    }

    public static SchemaJsonObject load() {
//        String schema = "/Users/idok/Projects/eslint-plugin/src/com/eslint/config/schema.json";
//        FileReader reader = null;
        InputStreamReader reader = null;
        try {
//            reader = new FileReader(schema);
            InputStream stream = ESLintSchema.class.getResourceAsStream("/com/eslint/config/schema/schema.json");
//            Reader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            reader = new InputStreamReader(stream, "UTF-8");
            Gson gson = getGson();
            SchemaJsonObject ret = gson.fromJson(reader, SchemaJsonObject.class);
            ROOT = ret;
//            System.out.println(ret.description);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

//    public static class BaseTypeAdapter implements JsonSerializer<BaseType>, JsonDeserializer<BaseType> {
//        private static final String CLASSNAME = "CLASSNAME";
//        private static final String INSTANCE = "INSTANCE";
//        private static final String TYPE = "class-type";
//
//        @Override
//        public JsonElement serialize(BaseType src, Type typeOfSrc, JsonSerializationContext context) {
//            JsonObject retValue = new JsonObject();
//            String className = src.getClass().getCanonicalName();
//            retValue.addProperty(CLASSNAME, className);
//            JsonElement elem = context.serialize(src);
//            elem.getAsJsonObject().addProperty(TYPE, className);
//            retValue.add(INSTANCE, elem);
//            return retValue;
//        }
//
//        @Override
//        public BaseType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//            JsonObject jsonObject = json.getAsJsonObject();
//            JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
//            String className = prim.getAsString();
//
//            Class<?> klass;
//            try {
//                klass = Class.forName(className);
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//                throw new JsonParseException(e.getMessage());
//            }
//            return context.deserialize(jsonObject.get(INSTANCE), klass);
//        }
//    }

//    public static class BookTypeAdapter extends TypeAdapter<BaseType> {
//        @Override
//        public BaseType read(final JsonReader in) throws IOException {
//            final BaseType book = new BaseType();
//            in.beginObject();
//            while (in.hasNext()) {
//                switch (in.nextName()) {
//                    case "isbn":
//                        super.read(in);
//                        book.setIsbn(in.nextString());
//                        break;
//                    case "title":
//                        book.setTitle(in.nextString());
//                        break;
//                    case "authors":
//                        book.setAuthors(in.nextString().split(";"));
//                        break;
//                }
//            }
//            in.endObject();
//            return book;
//        }
//
//        @Override
//        public void write(final JsonWriter out, final BaseType book) throws IOException {
//            out.beginObject();
//            out.name("isbn").value(book.getIsbn());
//            out.name("title").value(book.getTitle());
//            out.name("authors").value(StringUtils.join(book.getAuthors(), ";"));
//            out.endObject();
//        }
//    }

//    public static class PropertyTypeAdapter implements JsonSerializer<PropertyType>, JsonDeserializer<PropertyType> {
//        private static final String CLASSNAME = "CLASSNAME";
//        private static final String INSTANCE  = "INSTANCE";
//        private static final String TYPE  = "type";
//
//        @Override
//        public JsonElement serialize(PropertyType src, Type typeOfSrc, JsonSerializationContext context) {
////            JsonObject retValue = new JsonObject();
//            String className = src.getClass().getCanonicalName();
////            retValue.addProperty(CLASSNAME, className);
//            JsonElement elem = context.serialize(src);
//            elem.getAsJsonObject().addProperty(TYPE, className);
////            retValue.add(INSTANCE, elem);
//            return elem;
//        }
//
//        @Override
//        public PropertyType deserialize(PropertyType json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException  {
//            JsonObject jsonObject =  json.getAsJsonObject();
//            JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
//            String className = prim.getAsString();
//
//            Class<?> klass;
//            try {
//                klass = Class.forName(className);
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//                throw new JsonParseException(e.getMessage());
//            }
//            return context.deserialize(jsonObject.get(INSTANCE), klass);
//        }
//    }

    public enum PropertyType {ANY, BOOLEAN, STRING, OBJECT, INT}
}


//class RootProp {
//    public EnvProp env;
//    public BaseType globals;
//    public BaseType rules;
//}
//
//class EnvProp {
//    public BaseType amd;
//    public BaseType node;
//    public BaseType browser;
//}
