package net.automation.utils;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static net.automation.utils.Logger.logDebug;

public class SingleThreadExecutor {
    @Getter @Setter
    private static Instant nextActionMinimumTime = Instant.MIN;

    public static synchronized <T> T execute(Supplier<T> supplier) {
        return execute(Duration.ofMillis(0), supplier);
    }

    public static synchronized <T> T execute(Duration nextActionDelay, Supplier<T> supplier) {
        if (Instant.now().isBefore(nextActionMinimumTime)) {
            logDebug("Sleep before executing next action");
            ThreadHelper.sleep(Duration.between(Instant.now(), nextActionMinimumTime));
        }

        T output = supplier.get();
        nextActionMinimumTime = Instant.now().plus(nextActionDelay);
        return output;
    }
}
