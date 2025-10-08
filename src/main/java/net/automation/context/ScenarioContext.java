package net.automation.context;

import net.automation.hooks.TestScenario;

public class ScenarioContext {
    private TestScenario testScenario;

    public ScenarioContext() {
    }

    public TestScenario getTestScenario() {
        return this.testScenario;
    }

    public void setTestScenario(TestScenario testScenario) {
        this.testScenario = testScenario;
    }
}
