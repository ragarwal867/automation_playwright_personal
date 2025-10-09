package net.automation.reports.html;

import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public  class TestStepSummary {
    private Duration totalTime;
    private Duration averageTime;
    private Integer count;
    private String name;

    public TestStepSummary(String name, List<TestStep> steps) {
        this.name = name;
        this.count = steps.size();
        this.totalTime = steps.stream().map(TestStep::getDuration).reduce(Duration.ZERO, Duration::plus);
        this.averageTime = Duration.ofMillis(totalTime.toMillis() / this.count);
    }
}