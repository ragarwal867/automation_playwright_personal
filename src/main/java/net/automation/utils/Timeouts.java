package net.automation.utils;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

public class Timeouts {
    // SLEEP TIME
    @Getter @Setter
    private static Duration shortSleep = Duration.ofSeconds(1);
    @Getter @Setter
    private static Duration mediumSleep = Duration.ofSeconds(5);
    @Getter @Setter
    private static Duration longSleep = Duration.ofSeconds(10);

    // TIMEOUTS
    @Getter @Setter
    private static Duration shortTimeout = Duration.ofSeconds(70);
    @Getter @Setter
    private static Duration mediumTimeout = Duration.ofMinutes(3);
    @Getter @Setter
    private static Duration longTimeout = Duration.ofMinutes(10);
}
