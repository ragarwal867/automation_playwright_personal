package net.automation.reports.html;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
@Data
public class TestReport {
    private LocalDateTime start;

    private LocalDateTime end;

    private final List<TestScenario> testScenarios;

    public TestReport() {
        testScenarios = new ArrayList<>();
    }

    public synchronized void add(TestScenario testScenario) {
        testScenarios.add(testScenario);
    }

    public Duration getDuration() {
        return end != null
                ? Duration.between(start, end)
                : Duration.between(start, LocalDateTime.now(ZoneOffset.UTC));
    }
}
