package net.automation.hooks;

import io.cucumber.java.*;;
import net.automation.context.ScenarioContext;
import net.automation.reports.ResultApiClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Hooks {
    private TestReport report;
    private ScenarioContext context;

    static {
        beforeAllScenarios();
    }

    public Hooks(ScenarioContext context) {
        this.context = context;
        this.report = (new TestReport()).setStart(LocalDateTime.now(ZoneOffset.UTC));
    }

    @BeforeAll
    public static void beforeAllScenarios() {
        System.out.println("Before All Scenarios");
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        ResultApiClient.getInstance().sendTestRunDetails(this.report);
        TestScenario testScenario = (new TestScenario()).setName(scenario.getName()).setStart(LocalDateTime.now(ZoneOffset.UTC)).setStatus(TestStatus.IN_PROGRESS);
        context.setTestScenario(testScenario);
        this.report.add(testScenario);
    }

    @BeforeStep
    public void beforeStep() {
        TestStep testStep = (new TestStep()).setStart(LocalDateTime.now(ZoneOffset.UTC));
        context.getTestScenario().getSteps().add(testStep);
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        context.getTestScenario().getCurrentStep().setName(TestStepEvents.getTestStepName()).setEnd(LocalDateTime.now(ZoneOffset.UTC));
    }

    @After
    public void afterScenario(Scenario scenario) {
        TestStatus status = scenario.getStatus() == Status.PASSED ? TestStatus.PASSED : TestStatus.FAILED;
        context.getTestScenario().setEnd(LocalDateTime.now(ZoneOffset.UTC)).setStatus(status);
        ResultApiClient.getInstance().sendScenarioResult(this.report, context.getTestScenario(), scenario);

    }

    @AfterAll
    public void afterAllScenarios(Scenario scenario) {
        TestStatus status = scenario.getStatus() == Status.PASSED ? TestStatus.PASSED : TestStatus.FAILED;
        this.report.setEnd(LocalDateTime.now(ZoneOffset.UTC));
    }
}

