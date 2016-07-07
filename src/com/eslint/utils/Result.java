package com.eslint.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.execution.process.ProcessOutput;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * ESLint result
 * Created by idok on 8/25/14.
 */
public class Result {
    public List<VerifyMessage> warns = new ArrayList<VerifyMessage>();
    public String errorOutput;

    private static List<FileResult> parseInternal(String json) {
        GsonBuilder builder = new GsonBuilder();
//        builder.registerTypeAdapterFactory(adapter);
        Gson g = builder.setPrettyPrinting().create();
        Type listType = new TypeToken<ArrayList<FileResult>>() {}.getType();
        return g.fromJson(json, listType);
    }

    public static Result processResults(ProcessOutput output) {
        Result result = new Result();
        result.errorOutput = output.getStderr();
        try {
            List<FileResult> fileResults = parseInternal(output.getStdout());
            if (fileResults != null && !fileResults.isEmpty()) {
                result.warns = fileResults.get(0).messages;
            }
        } catch (Exception e) {
            result.errorOutput = output.getStdout();
//            result.errorOutput = e.toString();
        }
        return result;
    }

    public static Result createError(String error) {
        Result result = new Result();
        result.errorOutput = error;
        return result;
    }
}
