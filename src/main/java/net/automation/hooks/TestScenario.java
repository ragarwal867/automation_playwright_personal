package net.automation.hooks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class TestScenario {
    private String name;
    private TestStatus status;
    private String failedLogs;
    private String failedStep = "";
    private LocalDateTime start;
    private LocalDateTime end;
    private String screenshotFilepath;
    private List<TestStep> steps = new ArrayList();

    public TestScenario() {
    }

    public Duration getDuration() {
        return this.end != null ? Duration.between(this.start, this.end) : Duration.between(this.start, LocalDateTime.now(ZoneOffset.UTC));
    }

    public TestStep getCurrentStep() {
        return (TestStep)this.steps.get(this.steps.size() - 1);
    }

    public String getName() {
        return this.name;
    }

    public TestStatus getStatus() {
        return this.status;
    }

    public String getFailedLogs() {
        return this.failedLogs;
    }

    public String getFailedStep() {
        return this.failedStep;
    }

    public LocalDateTime getStart() {
        return this.start;
    }

    public LocalDateTime getEnd() {
        return this.end;
    }

    public String getScreenshotFilepath() {
        return this.screenshotFilepath;
    }

    public List<TestStep> getSteps() {
        return this.steps;
    }

    public TestScenario setName(String name) {
        this.name = name;
        return this;
    }

    public TestScenario setStatus(TestStatus status) {
        this.status = status;
        return this;
    }

    public TestScenario setFailedLogs(String failedLogs) {
        this.failedLogs = failedLogs;
        return this;
    }

    public TestScenario setFailedStep(String failedStep) {
        this.failedStep = failedStep;
        return this;
    }

    public TestScenario setStart(LocalDateTime start) {
        this.start = start;
        return this;
    }

    public TestScenario setEnd(LocalDateTime end) {
        this.end = end;
        return this;
    }

    public TestScenario setScreenshotFilepath(String screenshotFilepath) {
        this.screenshotFilepath = screenshotFilepath;
        return this;
    }

    public TestScenario setSteps(List<TestStep> steps) {
        this.steps = steps;
        return this;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TestScenario)) {
            return false;
        } else {
            TestScenario other = (TestScenario)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$name = this.getName();
                Object other$name = other.getName();
                if (this$name == null) {
                    if (other$name != null) {
                        return false;
                    }
                } else if (!this$name.equals(other$name)) {
                    return false;
                }

                Object this$status = this.getStatus();
                Object other$status = other.getStatus();
                if (this$status == null) {
                    if (other$status != null) {
                        return false;
                    }
                } else if (!this$status.equals(other$status)) {
                    return false;
                }

                Object this$failedLogs = this.getFailedLogs();
                Object other$failedLogs = other.getFailedLogs();
                if (this$failedLogs == null) {
                    if (other$failedLogs != null) {
                        return false;
                    }
                } else if (!this$failedLogs.equals(other$failedLogs)) {
                    return false;
                }

                Object this$failedStep = this.getFailedStep();
                Object other$failedStep = other.getFailedStep();
                if (this$failedStep == null) {
                    if (other$failedStep != null) {
                        return false;
                    }
                } else if (!this$failedStep.equals(other$failedStep)) {
                    return false;
                }

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

                Object this$screenshotFilepath = this.getScreenshotFilepath();
                Object other$screenshotFilepath = other.getScreenshotFilepath();
                if (this$screenshotFilepath == null) {
                    if (other$screenshotFilepath != null) {
                        return false;
                    }
                } else if (!this$screenshotFilepath.equals(other$screenshotFilepath)) {
                    return false;
                }

                Object this$steps = this.getSteps();
                Object other$steps = other.getSteps();
                if (this$steps == null) {
                    if (other$steps != null) {
                        return false;
                    }
                } else if (!this$steps.equals(other$steps)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof TestScenario;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        Object $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : $status.hashCode());
        Object $failedLogs = this.getFailedLogs();
        result = result * 59 + ($failedLogs == null ? 43 : $failedLogs.hashCode());
        Object $failedStep = this.getFailedStep();
        result = result * 59 + ($failedStep == null ? 43 : $failedStep.hashCode());
        Object $start = this.getStart();
        result = result * 59 + ($start == null ? 43 : $start.hashCode());
        Object $end = this.getEnd();
        result = result * 59 + ($end == null ? 43 : $end.hashCode());
        Object $screenshotFilepath = this.getScreenshotFilepath();
        result = result * 59 + ($screenshotFilepath == null ? 43 : $screenshotFilepath.hashCode());
        Object $steps = this.getSteps();
        result = result * 59 + ($steps == null ? 43 : $steps.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getName();
        return "TestScenario(name=" + var10000 + ", status=" + this.getStatus() + ", failedLogs=" + this.getFailedLogs() + ", failedStep=" + this.getFailedStep() + ", start=" + this.getStart() + ", end=" + this.getEnd() + ", screenshotFilepath=" + this.getScreenshotFilepath() + ", steps=" + this.getSteps() + ")";
    }
}

