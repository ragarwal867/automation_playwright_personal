package net.automation.utils;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Fail.fail;

public class FileHelper {
    public static final String ContentTypeText = "text/plain";
    public static final String ContentTypeXml = "application/xml";
    public static final String ContentTypePdf = "application/pdf";

    private static final Map<String, String> mimeTypes = new HashMap<>() {{
        put("html", "text/html");
        put("htm", "text/html");
        put("txt", ContentTypeText);
        put("css", "text/css");
        put("js", "application/javascript");
        put("json", "application/json");
        put("xml", ContentTypeXml);
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("png", "image/png");
        put("gif", "image/gif");
        put("pdf", ContentTypePdf);
        put("zip", "application/zip");
        put("rar", "application/x-rar-compressed");
        put("mp3", "audio/mpeg");
        put("mp4", "video/mp4");
        put("avi", "video/x-msvideo");
        put("doc", "application/msword");
        put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }};

    public static InputStream getInputStream(File file) {
        InputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return fileInputStream;
    }

    public static void deleteDirectory(String path) {
        try {
            File directory = new File(path);
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static String getMimeTypeBasedOnExtension(String filename) {
        if (filename == null) {
            return null;
        }

        String extension = getFileExtension(filename);
        if (extension == null) {
            return null;
        }

        if (!mimeTypes.containsKey(extension)) {
            Assert.fail("Not supported file extension: " + extension);
        }

        return mimeTypes.get(extension);
    }

    public static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return null;
        } else {
            return filename.substring(lastDotIndex + 1);
        }
    }

    public static String getFileContent(Path pain001Path) {
        try {
            return Files.readString(pain001Path);
        } catch (IOException e) {
            fail("Cannot read file content. Details: " + ExceptionHelper.getDetailedExceptionInfo(e));
            return null;
        }
    }
}
