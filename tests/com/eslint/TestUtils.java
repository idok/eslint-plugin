package com.eslint;

import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author idok
 */
public final class TestUtils {

    private TestUtils() {
    }

    private static final Logger LOG = Logger.getInstance(TestUtils.class);

    private static String TEST_DATA_PATH;

    public static String getTestDataPath() {
        if (TEST_DATA_PATH == null) {
            ClassLoader loader = TestUtils.class.getClassLoader();
            URL resource = loader.getResource("testData");
            try {
                TEST_DATA_PATH = new File("testData").getAbsolutePath();
                if (resource != null) {
                    TEST_DATA_PATH = new File(resource.toURI()).getPath().replace(File.separatorChar, '/');
                }
            } catch (URISyntaxException e) {
                LOG.error(e);
                return null;
            }
        }
        return TEST_DATA_PATH;
    }
}
