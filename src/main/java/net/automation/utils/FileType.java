package net.automation.utils;

import lombok.Getter;

@Getter
public enum FileType {
    TEXT("text", ".txt"),
    XML("xml", ".xml");

    private final String fileType;
    private final String extension;

    FileType(String fileType, String extension) {
        this.fileType = fileType;
        this.extension = extension;
    }

    public static boolean isValidFile(FileType fileType) {
        return TEXT == fileType;
    }
}
