package net.automation.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static net.automation.utils.ThreadHelper.sleep;
import static org.assertj.core.api.Fail.fail;

public class Try {
    /**
     * Repeat an action multiple times until we can the expected result.
     * The method will throw an exception if the action won't be executed successfully within a given timeout.
     * @param tryOptions defines the action that we want to execute and other parameters like timeout, max retries, etc.
     */
    public static void untilSuccess(TryOptions tryOptions) {
        Logger.logInfo(tryOptions.getActionName());

        String lastErrorMessage = "";
        Instant start = Instant.now();
        Instant lastFailActionTime = Instant.MIN;
        int attempt = 0;

        while (attempt < tryOptions.getMaxAttempts()) {
            attempt++;
            Duration duration = Duration.between(start, Instant.now());

            if (duration.toMillis() > tryOptions.getTimeout().toMillis()) {
                fail("Times out while trying to execute %s. Last error message: %s".formatted(tryOptions.getActionName(), lastErrorMessage));
            }

            try {
                if (tryOptions.getAction().get()) {
                    return;
                }

                lastErrorMessage = "Invalid result";
            } catch (Throwable t) {
                lastErrorMessage = t.getMessage();
            }

            Supplier<Boolean> failAction = tryOptions.getFailAction();
            Duration lastFailActionElapsedTime = Duration.between(lastFailActionTime, Instant.now());
            Boolean shouldExecuteFailAction = lastFailActionTime.equals(Instant.MIN)
                    ? true
                    : lastFailActionElapsedTime.toMillis() > tryOptions.getFailActionInterval().toMillis();

            if (failAction != null && shouldExecuteFailAction) {
                executeFailAction(failAction);
                lastFailActionTime = Instant.now();
            }

            Logger.logDebug("Retry action due to: %s".formatted(lastErrorMessage));
            sleep(tryOptions.getSleep());
        }

        fail("Exceeded max number of retries while trying to execute %s. Last error message: %s".formatted(tryOptions.getActionName(), lastErrorMessage));
    }

    private static void executeFailAction(Supplier<Boolean> failAction) {
        try {
            if (!failAction.get()) {
                fail("Cannot execute fail action (Invalid result)");
            }
        } catch (Throwable t) {
            fail("Cannot execute fail action. Details: %s".formatted(t.getMessage()));
        }
    }
}
