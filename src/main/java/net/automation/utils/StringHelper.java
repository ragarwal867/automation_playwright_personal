package net.automation.utils;


import net.automation.utils.lazyloader.LazyLoader;
import org.assertj.core.api.Fail;
import org.assertj.core.util.Strings;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {
    public static String transformVariables(String text, Map<String, LazyLoader<String>> variables) {
        if (Strings.isNullOrEmpty(text)) {
            return text;
        }

        Pattern variablePattern = Pattern.compile("\\$\\{([^\\}]+)\\}");
        Matcher matcher = variablePattern.matcher(text);
        String updatedText = matcher.replaceAll(result -> {
            String variableName = result.group(1);

            if (variables.containsKey(variableName)) {
                return variables.get(variableName).get();
            }

            Fail.fail("Cannot find variable: %s".formatted(result.group()));
            return null;
        });

        return updatedText;
    }

    public static String nullToEmptyString(String value) {
        return value == null
                ? ""
                : value;
    }

    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder camelCaseString = new StringBuilder();
        boolean nextWordCapital = false;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (Character.isWhitespace(currentChar)) {
                nextWordCapital = true;
            } else {
                if (nextWordCapital) {
                    camelCaseString.append(Character.toUpperCase(currentChar));
                    nextWordCapital = false;
                } else {
                    camelCaseString.append(Character.toLowerCase(currentChar));
                }
            }
        }

        return camelCaseString.toString();
    }

    public static InputStream toInputStream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    public static String convertToString(Object input) {
        if (input == null) {
            return null;
        }
        return input.toString();
    }
}
