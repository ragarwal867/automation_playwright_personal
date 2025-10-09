package net.automation.reports.summary;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestReportSummaryBuilder {

    public static String[] getTestScenarioCounts(String html, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        } else {
            System.err.println("Regex did not match any content in the HTML.");
            return new String[]{null, null};
        }
    }

    public static void main(String[] args) {
        if (args.length >= 4) {
            String directoryPath = args[0];
            String testReportName = args[1];
            String summaryFilePath = args[2];
            String duration = args[3];
            String regex = getRegex(directoryPath);

            File dir = new File(directoryPath);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".html"));

            JsonArray resultsArray = new JsonArray();

            if (files != null) {
                for (File file : files) {
                    try {
                        String html = new String(Files.readAllBytes(file.toPath()));
                        String[] counts = getTestScenarioCounts(html, regex);
                        String passed = counts[0];
                        String failed = counts[1];

                        JsonObject result = new JsonObject();
                        result.addProperty("name", testReportName);
                        result.addProperty("passed", Integer.parseInt(passed));
                        result.addProperty("failed", Integer.parseInt(failed));
                        result.addProperty("duration", duration);

                        resultsArray.add(result);
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + file.getName() + ". Details: " + e.getMessage());
                    }
                }
            }

            File summaryFile = new File(summaryFilePath);
            if (summaryFile.exists()) {
                try {
                    JsonObject summary = JsonParser.parseReader(new FileReader(summaryFile)).getAsJsonObject();
                    JsonArray existingResults = summary.getAsJsonArray("results");
                    for (int i = 0; i < resultsArray.size(); i++) {
                        existingResults.add(resultsArray.get(i));
                    }

                    try (PrintWriter writer = new PrintWriter(new FileWriter(summaryFilePath))) {
                        Gson gson = new Gson();
                        writer.write(gson.toJson(summary));
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                } catch (FileNotFoundException e) {
                    System.err.println("Summary file not found: " + e.getMessage());
                }
            } else {
                System.err.println("Summary file does not exist. Skipping update.");
            }
        } else {
            System.out.println("Skipping execution: Missing required arguments.");
        }
    }

    private static String getRegex(String directoryPath) {
        String testOutputReportRegex = "<div class=\"card\">\\n.+\\n.+Scenarios.+\\n.+\\n.+\\n.+\\n.+\\n.+\\n.+\\n.+\\n.+<b>([0-9]+)<.+\\n.+\\n.+<b>([0-9]+)<";
        String customReportRegex = "<th>Passed tests</th><th>Failed tests</th></tr><tr><td>[^<]*</td><td>[^<]*</td><td>[^<]*</td><td>(\\d+)</td><td>(\\d+)</td>";

        String regex;
        if (directoryPath.contains("/test-output")) {
            regex = testOutputReportRegex;
        } else if (directoryPath.contains("/reports")) {
            regex = customReportRegex;
        } else {
            throw new IllegalArgumentException("Unknown report type for directory path: " + directoryPath);
        }
        return regex;
    }
}