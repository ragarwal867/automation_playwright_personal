package net.automation.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileFactory {

    @FunctionalInterface
    public interface FileContentWriter {
        void write(File file, String uniqueFileName) throws IOException;
    }

    public static final Map<FileType, FileContentWriter> FILE_WRITERS = new HashMap<>();

    static {
        FILE_WRITERS.put(FileType.TEXT, (file, uniqueFileName) -> {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("This is a sample text file: " + uniqueFileName);
            }
        });
        FILE_WRITERS.put(FileType.XML, (file, uniqueFileName) -> {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("<message>This is a sample XML file: " + uniqueFileName + "</message>");
            }
        });
    }
}
