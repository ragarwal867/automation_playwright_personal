package net.automation.hooks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TestStep {
    private String name;
    private TestStatus status;
    private LocalDateTime start;
    private LocalDateTime end;

    public Duration getDuration() {
        return this.end != null ? Duration.between(this.start, this.end) : Duration.between(this.start, LocalDateTime.now(ZoneOffset.UTC));
    }

    public TestStep() {
    }

    public String getName() {
        return this.name;
    }

    public TestStatus getStatus() {
        return this.status;
    }

    public LocalDateTime getStart() {
        return this.start;
    }

    public LocalDateTime getEnd() {
        return this.end;
    }

    public TestStep setName(String name) {
        this.name = name;
        return this;
    }

    public TestStep setStatus(TestStatus status) {
        this.status = status;
        return this;
    }

    public TestStep setStart(LocalDateTime start) {
        this.start = start;
        return this;
    }

    public TestStep setEnd(LocalDateTime end) {
        this.end = end;
        return this;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TestStep)) {
            return false;
        } else {
            TestStep other = (TestStep)o;
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

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof TestStep;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        Object $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : $status.hashCode());
        Object $start = this.getStart();
        result = result * 59 + ($start == null ? 43 : $start.hashCode());
        Object $end = this.getEnd();
        result = result * 59 + ($end == null ? 43 : $end.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getName();
        return "TestStep(name=" + var10000 + ", status=" + this.getStatus() + ", start=" + this.getStart() + ", end=" + this.getEnd() + ")";
    }
}