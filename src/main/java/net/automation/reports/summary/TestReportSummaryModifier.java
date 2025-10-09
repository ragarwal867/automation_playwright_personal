package net.automation.reports.summary;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;

public class TestReportSummaryModifier {

    public static void main(String[] args) {
        if (args.length >= 2) {
            String summaryFilePath = args[0];
            String buildDuration = args[1];

            File summaryFile = new File(summaryFilePath);
            if (!summaryFile.exists()) {
                System.out.println("Summary file not found: " + summaryFilePath);
                return;
            }

            try {
                JsonObject summary = JsonParser.parseReader(new FileReader(summaryFile)).getAsJsonObject();
                summary.addProperty("buildDuration", buildDuration);

                try (PrintWriter writer = new PrintWriter(new FileWriter(summaryFilePath))) {
                    Gson gson = new Gson();
                    writer.write(gson.toJson(summary));
                }

                System.out.println("Updated buildDuration to: " + buildDuration);

            } catch (IOException e) {
                System.err.println("Error updating summary file: " + e.getMessage());
            }
        } else {
            System.out.println("Skipping execution: Missing required arguments.");
        }
    }
}
