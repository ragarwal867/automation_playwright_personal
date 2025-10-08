package net.automation.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateHelper {
    public static final String DATE_FORMAT_DD_MM_YYYY = "dd-MM-yyyy";
    private static String defaultDateFormat = "yyyy-MM-dd";

    public DateHelper() {
    }

    public static LocalDate fromString(String date) {
        return fromString(date, defaultDateFormat);
    }

    public static LocalDate fromString(String date, String dateFormat) {
        return date == null ? null : LocalDate.parse(date, DateTimeFormatter.ofPattern(dateFormat));
    }

    public static LocalDateTime fromDateTimeString(String dateTime, String dateFormat) {
        return dateTime == null ? null : LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(dateFormat));
    }

    public static String getDefaultDateFormat() {
        return defaultDateFormat;
    }

    public static void setDefaultDateFormat(String defaultDateFormat) {
        DateHelper.defaultDateFormat = defaultDateFormat;
    }
}
