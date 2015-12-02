package com.eslint.utils;

import java.util.List;

public class FileResult {
    public String filePath;
    public List<VerifyMessage> messages;
    public int errorCount;
    public int warningCount;
}
