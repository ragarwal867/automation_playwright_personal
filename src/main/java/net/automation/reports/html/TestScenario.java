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
public class TestScenario {
    private String name;
    private TestStatus status;
    private String failedLogs;
    private String failedReason;
    private String failedStackTrace;
    private LocalDateTime start;
    private LocalDateTime end;
    private String screenshotFilepath;
    private List<TestStep> steps;

    public TestScenario() {
        failedReason = "";
        steps = new ArrayList<>();
    }

    public Duration getDuration() {
        return end != null
                ? Duration.between(start, end)
                : Duration.between(start, LocalDateTime.now(ZoneOffset.UTC));
    }

    public TestStep getCurrentStep() {
        return steps.get(steps.size() - 1);
    }
}
