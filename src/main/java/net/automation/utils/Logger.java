package net.automation.utils;

import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Logger {
    private static final ThreadLocal<List<String>> logs = new ThreadLocal<>();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String getLastLogs(int maxCount, String separator) {
        List<String> actualLogs = new ArrayList<>(logs.get());
        Collections.reverse(actualLogs);
        actualLogs.subList(0, Math.min(maxCount, actualLogs.size()));
        return String.join(separator, actualLogs);
    }

    public static void clearLogs() {
        logs.set(null);
    }

    public static void logInfo(String message, Object... messageParameters) {
        log().info(message, messageParameters);
        logCucumber(message, messageParameters);
    }

    public static void logError(String message, Object... messageParameters) {
        log().error(message, messageParameters);
        logCucumber(message, messageParameters);
    }

    public static void logDebug(String message, Object... messageParameters) {
        log().debug(message, messageParameters);
        logCucumber(message, messageParameters);
    }

    private static void logCucumber(String message, Object... objects) {
        try {
            if (objects.length > 0) {
                try {
                    message = message.replace("{}", "%s").formatted(objects);
                } catch (Exception ex) {
                    log().error("Cannot convert log message. Details: " + ex.getMessage());
                }
            }

            if (ExtentCucumberAdapter.getCurrentScenario() == null) {
                log().info("No scenario found.");
                return;
            }

            String cucumberLogMessage = "<pre>[%s] ".formatted(getHHMMSSDate()) + HtmlHelper.escapeSpecialCharacters(message) + "</pre>";
            ExtentCucumberAdapter.addTestStepLog(cucumberLogMessage);

            if (logs.get() == null) {
                logs.set(new ArrayList<>());
            }

            logs.get().add(cucumberLogMessage);
        } catch (Throwable t) {
            log().error("Cannot log cucumber message. Details: " + t.getMessage());
        }
    }

    private static org.slf4j.Logger log() {
        return LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[3].getClassName());
    }

    private static String getHHMMSSDate() {
        return LocalDateTime.now(ZoneOffset.UTC).format(dateTimeFormatter);
    }
}