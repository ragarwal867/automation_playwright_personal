package net.automation.reports;

public enum ContentType {
    JSON("application/json"),
    XML("application/xml"),
    TEXT("text/plain");

    private final String type;

    ContentType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}

