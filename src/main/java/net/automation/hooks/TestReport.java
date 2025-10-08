package net.automation.hooks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class TestReport {
    private LocalDateTime start;
    private LocalDateTime end;
    private final List<TestScenario> testScenarios = new ArrayList();

    public TestReport() {
    }

    public synchronized void add(TestScenario testScenario) {
        this.testScenarios.add(testScenario);
    }

    public Duration getDuration() {
        return this.end != null ? Duration.between(this.start, this.end) : Duration.between(this.start, LocalDateTime.now(ZoneOffset.UTC));
    }

    public LocalDateTime getStart() {
        return this.start;
    }

    public LocalDateTime getEnd() {
        return this.end;
    }

    public List<TestScenario> getTestScenarios() {
        return this.testScenarios;
    }

    public TestReport setStart(LocalDateTime start) {
        this.start = start;
        return this;
    }

    public TestReport setEnd(LocalDateTime end) {
        this.end = end;
        return this;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TestReport)) {
            return false;
        } else {
            TestReport other = (TestReport)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$start = this.getStart();
                Object other$start = other.getStart();
                if (this$start == null) {
                    if (other$start != null) {
                        return false;
                    }
                } else if (!this$start.equals(other$start)) {
                    return false;
                }

                Object this$end = this.getEnd();
                Object other$end = other.getEnd();
                if (this$end == null) {
                    if (other$end != null) {
                        return false;
                    }
                } else if (!this$end.equals(other$end)) {
                    return false;
                }

                Object this$testScenarios = this.getTestScenarios();
                Object other$testScenarios = other.getTestScenarios();
                if (this$testScenarios == null) {
                    if (other$testScenarios != null) {
                        return false;
                    }
                } else if (!this$testScenarios.equals(other$testScenarios)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof TestReport;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $start = this.getStart();
        result = result * 59 + ($start == null ? 43 : $start.hashCode());
        Object $end = this.getEnd();
        result = result * 59 + ($end == null ? 43 : $end.hashCode());
        Object $testScenarios = this.getTestScenarios();
        result = result * 59 + ($testScenarios == null ? 43 : $testScenarios.hashCode());
        return result;
    }

    public String toString() {
        LocalDateTime var10000 = this.getStart();
        return "TestReport(start=" + var10000 + ", end=" + this.getEnd() + ", testScenarios=" + this.getTestScenarios() + ")";
    }
}