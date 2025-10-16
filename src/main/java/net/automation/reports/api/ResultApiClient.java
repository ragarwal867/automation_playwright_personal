package net.automation.reports.api;

import io.cucumber.java.Scenario;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.automation.clients.api.ApiClient;
import net.automation.utils.TypeHelper;
import net.automation.core.config.Config;
import net.automation.reports.html.TestReport;
import net.automation.reports.html.TestScenario;

import java.nio.file.Paths;
import java.time.ZoneOffset;

@Slf4j
public class ResultApiClient {
    @Getter
    private static final ResultApiClient instance = new ResultApiClient();
    public static final String URL = "http://localhost:8090/";
    public static final String RUN_TYPE = "runType";
    public static final String BUILD_NUMBER = "buildNumber";
    public static final String BUILD_ENVIRONMENT = "env";
    public static final String RUN_BRANCH = "branch";
    public static final String SCENARIO_RECORD_ENDPOINT = "api/v1/scenario/record";

    private final Config config;

    private ResultApiClient() {
        this.config = Config.getInstance();
    }


    public void sendScenarioResult(TestReport report, TestScenario testScenario, Scenario scenario) {
        String message = createScenarioMessage(report, testScenario, scenario);
        send(message, SCENARIO_RECORD_ENDPOINT);

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
        return URL + endpoint;
    }

    private void send(String message, String endpoint) {
        ApiClient apiClient = new ApiClient(getUrl(endpoint));
        apiClient.invoke(
                Method.POST,
                getUrl(endpoint),
                204,
                r -> r
                        .setBody(message)
                        .setContentType(ContentType.JSON));
    }


    public String get(String endpoint) {
        ApiClient apiClient = new ApiClient(getUrl(endpoint));
        return apiClient.invoke(
                Method.GET,
                getUrl(endpoint),
                200,
                r -> r.setContentType(ContentType.JSON)
        ).toString();
    }
}
