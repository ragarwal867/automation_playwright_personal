package net.automation.utils;

import java.time.Duration;

import static org.assertj.core.api.Fail.fail;

public class ThreadHelper {
    public static void sleep(Duration duration) {
        sleep(duration.toMillis());
    }

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            fail("Cannot sleep. Details %s".formatted(e.getMessage()));
        }
    }
}
