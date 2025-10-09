package net.automation.utils;

import com.google.common.io.Resources;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Fail.fail;

public class ResourcesHelper {
    public static String getAbsoluteFilePath(String name) {
        ClassLoader classLoader = ResourcesHelper.class.getClassLoader();
        URL resource = classLoader.getResource(name);

        if (resource != null) {
            File file = new File(resource.getFile());
            return file.getAbsolutePath();
        } else {
            fail("Cannot find resource: " + name);
            return null;
        }
    }

    public static String readResource(String path) {
        String content = null;

        try {
            URL resourceUrl = Resources.getResource(path);
            content = Resources.toString(resourceUrl, StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail("Cannot read resource %s".formatted(path));
        }

        return content;
    }

    public static InputStream readResourceAsInputStream(String path) {
        InputStream content = null;

        try {
            URL resourceUrl = Resources.getResource(path);
            content = new ByteArrayInputStream(Resources.toByteArray(resourceUrl));
        } catch (Exception e) {
            fail("Cannot read resource %s".formatted(path));
        }

        return content;
    }

    public static InputStream getStream(String path) {
        InputStream inputStream = null;

        try {
            URL resourceUrl = Resources.getResource(path);
            byte[] resourceBytes = Resources.toByteArray(resourceUrl);
            inputStream = new ByteArrayInputStream(resourceBytes);
        } catch (IOException e) {
            fail("Cannot read resource %s".formatted(path));
        }

        return inputStream;
    }
}
