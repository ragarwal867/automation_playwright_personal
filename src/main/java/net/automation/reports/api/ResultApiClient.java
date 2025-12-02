package net.automation.reports.api;

import io.cucumber.java.Scenario;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.automation.clients.api.ApiClient;
import net.automation.utils.TypeHelper;
import net.automation.reports.html.TestReport;
import net.automation.reports.html.TestScenario;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ResultApiClient {
    @Getter
    private static final ResultApiClient instance = new ResultApiClient();
    public static final String URL = "resultApi.url";
    public static final String RUN_TYPE = "runType";
    public static final String BUILD_NUMBER = "buildNumber";
    public static final String BUILD_ENVIRONMENT = "env";
    public static final String RUN_BRANCH = "branch";
    public static final String SCENARIO_RECORD_ENDPOINT = "/scenario/record";

    private ResultApiClient() {
    }


    public void sendScenarioResult(TestReport report, TestScenario testScenario, Scenario scenario) {
        if (!resultApiIsConfigured()) {
            log.warn("Result API not configured — skipping reporting.");
            return;
        }

        String message = createScenarioMessage(report, testScenario, scenario);
        CompletableFuture.runAsync(() -> {
            try {
                ReportRetryUtil.retry(() -> send(message, SCENARIO_RECORD_ENDPOINT), 3, 1000);
                log.info("Scenario result sent successfully.");
            } catch (Exception e) {
                log.error("Failed to send scenario result after retries: {}", e.getMessage());
                saveFailedScenarioPayloadLocally(message);
            }
        });
    }

    public void sendUnsentReports() {
        if (!resultApiIsConfigured()) {
            log.warn("Result API not configured — skipping unsent records.");
            return;
        }

        try {
            String buildNumber = sanitize(System.getProperty(BUILD_NUMBER, "0"));
            String env = sanitize(System.getProperty(BUILD_ENVIRONMENT, "unknown"));
            String branch = sanitize(System.getProperty(RUN_BRANCH, "unknown"));
            String runFolderName = String.format("%s_%s_%s", env, branch, buildNumber);

            Path runDir = Paths.get("unsent-records", runFolderName);
            if (!Files.exists(runDir)) {
                log.info("No unsent records directory found for current run: {}", runFolderName);
                return;
            }

            List<Path> unsentFiles;

            try (var stream = Files.list(runDir)) {
                unsentFiles = stream
                        .filter(p -> p.toString().endsWith(".json"))
                        .toList();
            }

            if (unsentFiles.isEmpty()) {
                log.info("No unsent records for current run.");
                return;
            }

            log.info("Found {} unsent record(s) for current run. Attempting to send...", unsentFiles.size());

            for (var file : unsentFiles) {
                try {
                    String message = Files.readString(file);
                    ReportRetryUtil.retry(() -> send(message, SCENARIO_RECORD_ENDPOINT), 1, 1000);
                    Files.delete(file);
                    log.info("Successfully sent and deleted: {}", file.getFileName());
                } catch (Exception e) {
                    log.error("Failed to send unsent record {}: {}", file.getFileName(), e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error processing unsent records: {}", e.getMessage());
        }
    }

    private static String createScenarioMessage(TestReport report, TestScenario testScenario, Scenario scenario) {
        ScenarioResult payloadScenarioResult = ScenarioResult.fromTestScenario(testScenario)
                .usingScenario(scenario)
                .build();

        TestRun payloadTest = new TestRun()
                .setDatetimeStart(report.getStart().toInstant(ZoneOffset.UTC))
                .setServer(System.getProperty(BUILD_ENVIRONMENT, "QA"))
                .setRunType(System.getProperty(RUN_TYPE, "Galileo"))
                .setBranch(System.getProperty(RUN_BRANCH, "main"))
                .setBuildNumber(Integer.valueOf(System.getProperty(BUILD_NUMBER, "1")));


        String relativePath = Paths.get(System.getProperty("user.dir"))
                .relativize(Paths.get(scenario.getUri()))
                .toString();

        ScenarioDetails scenarioDetails = new ScenarioDetails();
        scenarioDetails
                .setUri(relativePath)
                .setLineNumber(scenario.getLine())
                .setName(scenario.getName());

        payloadScenarioResult.setTestRun(payloadTest);
        payloadScenarioResult.setScenario(scenarioDetails);

        return TypeHelper.convertToJson(payloadScenarioResult);
    }

    private String getUrl(String endpoint) {
        return System.getProperty(URL) + endpoint;
    }

    private void send(String message, String endpoint) {
        ApiClient apiClient = new ApiClient(getUrl(endpoint));
        apiClient.invoke(
                Method.POST,
                getUrl(endpoint),
                201,
                r -> r
                        .setBody(message)
                        .setContentType(ContentType.JSON));
    }

    private void saveFailedScenarioPayloadLocally(String json) {
        try {
            String buildNumber = System.getProperty(BUILD_NUMBER, "0");
            String env = System.getProperty(BUILD_ENVIRONMENT, "unknown");
            String branch = System.getProperty(RUN_BRANCH, "unknown");
            long timestamp = System.currentTimeMillis();

            String runFolderName = String.format("%s_%s_%s",
                    sanitize(env),
                    sanitize(branch),
                    sanitize(buildNumber));
            Path runDir = Paths.get("unsent-records", runFolderName);
            Files.createDirectories(runDir);

            String fileName = String.format("scenario_%d.json", timestamp);
            Path filePath = runDir.resolve(fileName);
            Files.writeString(filePath, json);
            log.warn("Stored failed scenario report locally for retry: {}/{}", runFolderName, fileName);
        } catch (IOException ex) {
            log.error("Could not store failed scenario payload: {}", ex.getMessage());
        }
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    public boolean resultApiIsConfigured() {
        String url = System.getProperty(URL);
        return url != null && !url.isEmpty();
    }
}
