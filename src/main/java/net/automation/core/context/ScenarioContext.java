package net.automation.core.context;

import lombok.Getter;
import lombok.Setter;
import net.automation.reports.api.TestRun;
import net.automation.reports.html.TestScenario;

public class ScenarioContext {
    @Getter
    @Setter
    private TestScenario testScenario;

    @Setter
    @Getter
    private TestRun testRun;
}