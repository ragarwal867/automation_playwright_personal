package net.automation.reports.html;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Accessors(chain = true)
@Data
public class TestStep {
    private String name;
    private TestStatus status;
    private LocalDateTime start;
    private LocalDateTime end;

    public Duration getDuration() {
        return end != null
                ? Duration.between(start, end)
                : Duration.between(start, LocalDateTime.now(ZoneOffset.UTC));
    }
}
