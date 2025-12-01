package net.automation.reports.api;

public class ReportRetryUtil {
    public static void retry(Runnable action, int attempts, long delayMillis) {
        for (int i = 1; i <= attempts; i++) {
            try {
                action.run();
                return;
            } catch (Exception e) {
                if (i == attempts) {
                    throw e;
                }
                try {
                    Thread.sleep(delayMillis * (1L << (i - 1)));
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
