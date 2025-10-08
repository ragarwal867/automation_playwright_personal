package net.automation.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberHelper {
    private static String decimalSeparator = ",";
    private static String groupingSeparator = ".";
    private static String format = "#,##0.00";
    private static String frontendFormatKey = "#.##0,0#";

    public NumberHelper() {
    }

    public static Integer toInteger(String number) {
        return number == null ? null : Integer.parseInt(number);
    }

    public static Integer parseIntegerFromObject(Object obj) {
        return obj == null ? null : Integer.parseInt(obj.toString());
    }

    public static String toDecimalNumber(Integer doubleNumber) {
        return doubleNumber == null ? null : toDecimalNumber(doubleNumber.doubleValue());
    }

    public static String toDecimalNumber(String doubleNumber) {
        return doubleNumber == null ? null : toDecimalNumber(Double.parseDouble(doubleNumber));
    }

    public static String toDecimalNumber(Double number) {
        if (number == null) {
            return null;
        } else {
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator(decimalSeparator.charAt(0));
            decimalFormatSymbols.setGroupingSeparator(groupingSeparator.charAt(0));
            DecimalFormat decimalFormat = new DecimalFormat(format, decimalFormatSymbols);
            return decimalFormat.format(number);
        }
    }

    public static String toString(Integer number) {
        return String.format(Locale.UK, "%,d", number).replace(",", ".");
    }

    public static String getDecimalSeparator() {
        return decimalSeparator;
    }

    public static void setDecimalSeparator(String decimalSeparator) {
        NumberHelper.decimalSeparator = decimalSeparator;
    }

    public static String getGroupingSeparator() {
        return groupingSeparator;
    }

    public static void setGroupingSeparator(String groupingSeparator) {
        NumberHelper.groupingSeparator = groupingSeparator;
    }

    public static String getFormat() {
        return format;
    }

    public static void setFormat(String format) {
        NumberHelper.format = format;
    }

    public static String getFrontendFormatKey() {
        return frontendFormatKey;
    }

    public static void setFrontendFormatKey(String frontendFormatKey) {
        NumberHelper.frontendFormatKey = frontendFormatKey;
    }
}