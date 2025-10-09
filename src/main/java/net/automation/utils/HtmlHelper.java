package net.automation.utils;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class HtmlHelper {
    public static String escapeSpecialCharacters(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder escapedText = new StringBuilder(input.length());

        for (char c : input.toCharArray()) {
            switch (c) {
                case '&':
                    escapedText.append("&amp;");
                    break;
                case '<':
                    escapedText.append("&lt;");
                    break;
                case '>':
                    escapedText.append("&gt;");
                    break;
                case '"':
                    escapedText.append("&quot;");
                    break;
                case '\'':
                    escapedText.append("&#39;");
                    break;
                case '\n':
                    escapedText.append("\n");
                    break;
                case '\t':
                    escapedText.append("\t");
                    break;
                default:
                    escapedText.append(c);
                    break;
            }
        }

        return escapedText.toString();
    }

    public static List<String> extractAllByPattern(final String html, final String regex) {
        List<String> results = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results;
    }
}
