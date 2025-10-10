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

import java.time.ZoneOffset;

@Slf4j
public class ResultApiClient {
    @Getter
    private static final ResultApiClient instance = new ResultApiClient();
    public static final String URL = "http://localhost:8090/";
    public static final String RUN_TYPE = "runType";
    public static final String BUILD_NUMBER = "buildNumber";
    public static final String PARENT_BUILD_NUMBER = "parentBuildNumber";
    public static final String BUILD_ENVIRONMENT = "env";
    public static final String RUN_BRANCH = "branch";
    public static final String SCENARIO_RECORD_ENDPOINT = "api/v1/scenario/record";
    public static final String TEST_RUN_START_ENDPOINT = "api/v1/testrun/start";

    private final Config config;

    private ResultApiClient() {
        this.config = Config.getInstance();
    }


    public void sendScenarioResult(TestReport report, TestScenario testScenario, Scenario scenario) {

        String message = createScenarioMessage(report, testScenario, scenario);
        log.info(message);
        send(message, SCENARIO_RECORD_ENDPOINT);

    }

    public void sendTestRunDetails(TestReport report) {
        String message = createRunMessage(report);
        log.info(message);
        send(message, TEST_RUN_START_ENDPOINT);

    }

    private static String createScenarioMessage(TestReport report, TestScenario testScenario, Scenario scenario) {
        ScenarioResult payloadScenarioResult = ScenarioResult.fromTestScenario(testScenario)
                .usingScenario(scenario)
                .build();

        TestRun payloadTest = new TestRun()
                .setDatetimeStart(report.getStart().toInstant(ZoneOffset.UTC))
                .setServer(System.getProperty(BUILD_ENVIRONMENT, "Demo"))
                .setRunType(System.getProperty(RUN_TYPE, "Galileo"))
                .setBranch(System.getProperty(RUN_BRANCH, "develop"))
                .setBuildNumber(Integer.valueOf(System.getProperty(BUILD_NUMBER, "1")));

        payloadScenarioResult.setTestRun(payloadTest);

        return TypeHelper.convertToJson(payloadScenarioResult);
    }


    private static String createRunMessage(TestReport report) {
        TestRun parentRun = null;
        if (System.getProperty(PARENT_BUILD_NUMBER) != null) {
            parentRun = new TestRun()
                    .setDatetimeStart(report.getStart().toInstant(ZoneOffset.UTC))
                    .setServer(System.getProperty(BUILD_ENVIRONMENT, "ad hoc"))
                    .setRunType(System.getProperty(RUN_TYPE, "ad hoc"))
                    .setBranch(System.getProperty(RUN_BRANCH, "ad hoc"))
                    .setBuildNumber(Integer.valueOf(System.getProperty(PARENT_BUILD_NUMBER, "ad hoc")));
        }


        TestRun payload = new TestRun()
                .setDatetimeStart(report.getStart().toInstant(ZoneOffset.UTC))
                .setServer(System.getProperty(BUILD_ENVIRONMENT, "ad hoc"))
                .setRunType(System.getProperty(RUN_TYPE, "ad hoc"))
                .setBranch(System.getProperty(RUN_BRANCH, "ad hoc"))
                .setBuildNumber(Integer.valueOf(System.getProperty(BUILD_NUMBER, "ad hoc")))
                .setParentRun(parentRun);

        return TypeHelper.convertToJson(payload);
    }

    public boolean resultApiIsConfigured() {
        return config.hasProperty(URL);
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
