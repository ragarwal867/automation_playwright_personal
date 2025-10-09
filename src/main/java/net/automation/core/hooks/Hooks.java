package net.automation.core.hooks;

import io.cucumber.java.*;
import io.cucumber.plugin.event.Result;
import net.automation.core.TestStepEvents;
import net.automation.core.context.ScenarioContext;
import net.automation.reports.api.ResultApiClient;
import net.automation.reports.html.*;
import net.automation.utils.Logger;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static net.automation.utils.Logger.logInfo;
import static net.automation.utils.Logger.logError;

public class Hooks {
    private static TestReport report;

    private ScenarioContext context;

    static {
        beforeAllScenarios();
    }

    public Hooks(ScenarioContext context) {
        this.context = context;
    }

    public static void beforeAllScenarios() {
        // Reporting
        report = new TestReport().setStart(LocalDateTime.now());
        TestReportHtmlGenerator.generateHtmlReport(report);
        ResultApiClient.getInstance().sendTestRunDetails(report);
    }

    @AfterAll
    public static void afterAllScenarios() {
        try {
            Logger.logInfo("After all scenarios");
            report.setEnd(LocalDateTime.now(ZoneOffset.UTC));
            TestReportHtmlGenerator.generateHtmlReport(report);
        } catch (Exception e) {
            logError("Cannot invoke afterAllScenarios. Details: " + e.getMessage());
        }
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        logInfo("Start scenario: %s".formatted(scenario.getName()));
        TestScenario testScenario = (new TestScenario()).setName(scenario.getName()).setStart(LocalDateTime.now(ZoneOffset.UTC)).setStatus(TestStatus.IN_PROGRESS);
        context.setTestScenario(testScenario);
        report.add(testScenario);
        TestReportHtmlGenerator.generateHtmlReport(report);
    }


    @After(order = 1)
    public void afterScenario(Scenario scenario) {
        TestStatus status = scenario.getStatus() == Status.PASSED ? TestStatus.PASSED : TestStatus.FAILED;
        context
                .getTestScenario()
                .setEnd(LocalDateTime.now())
                .setStatus(scenario.getStatus() == Status.PASSED ? TestStatus.PASSED : TestStatus.FAILED)
                .setScreenshotFilepath(null)
                .setFailedStep(status.equals(TestStatus.FAILED)
                        ? getScenarioError(scenario)
                        : null)
                .setFailedLogs(scenario.isFailed()
                        ? Logger.getLastLogs(100, "<br>")
                        : null);

        TestReportHtmlGenerator.generateHtmlReport(report);
        Logger.clearLogs();

        ResultApiClient.getInstance().sendScenarioResult(report, context.getTestScenario(), scenario);
    }

    @BeforeStep
    public void beforeStep() {
        TestStep testStep = new TestStep().setStart(LocalDateTime.now());
        context.getTestScenario().getSteps().add(testStep);
    }

    @AfterStep
    public void afterStep() {
        context.getTestScenario().getCurrentStep().setName(TestStepEvents.getTestStepName()).setEnd(LocalDateTime.now(ZoneOffset.UTC));
    }

    private String getScreenshotName(String scenarioName) {
        String currentDateTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()).replace(":", "_");
        String formattedScenarioName = scenarioName.substring(0, 10).replaceAll(" ", "_");
        return currentDateTime + "_" + formattedScenarioName;
    }

    /**
     * Get scenario error using reflection.
     * Please update the implementation when upgrading cucumber to a newer version.
     */
    private String getScenarioError(Scenario scenario) {
        String scenarioError = "";

        try {
            var delegateField = scenario.getClass().getDeclaredField("delegate");
            delegateField.setAccessible(true);
            Object delegate = delegateField.get(scenario);

            var stepResultsField = delegate.getClass().getDeclaredField("stepResults");
            stepResultsField.setAccessible(true);
            List<Result> stepResults = (List<Result>)stepResultsField.get(delegate);
            for (Result result : stepResults) {
                if (result.getError() != null) {
                    scenarioError += result.getError().getMessage() + "\n";
                }
            }
        } catch (Exception e) {
            scenarioError = "Cannot get the scenario error. Details: " + e.getMessage();
        }

        return scenarioError;
    }
}