package net.automation.reports.html;

import net.automation.utils.ExceptionHelper;
import net.automation.utils.FileHelper;
import org.assertj.core.util.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.automation.utils.Logger.logError;

public class TestReportHtmlGenerator {
    private static final String reportName = getReportName();
    private static final String reportScreenshotsDirName = "screenshots";
    private static final String reportFilePath = geReportDirectoryPath() + File.separator + reportName + ".html";
    private static final String reportScreenshotsPath = geReportDirectoryPath() + File.separator + reportScreenshotsDirName;

    private static final DateTimeFormatter fullDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter shortDateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    static {
        FileHelper.deleteDirectory(geReportDirectoryPath());
        FileHelper.createDirectory(reportScreenshotsPath);
    }

    private static String getReportName() {
        String customReportName = System.getProperty("systeminfo.AppName");
        return Strings.isNullOrEmpty(customReportName)
                ? "report_" + LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"))
                : customReportName;
    }

    private static String geReportDirectoryPath() {
        String filePath = System.getProperty("user.dir");
        filePath += File.separator;
        filePath += "reports";
        filePath += File.separator;
        filePath += reportName;
        return filePath;
    }

    public synchronized static String saveScreenshot(byte[] screenshot) {
        if (screenshot == null || screenshot.length == 0) {
            return null;
        }

        String fileName = "screenshot_" + UUID.randomUUID() + ".jpg";
        String filePath = reportScreenshotsPath + File.separator + fileName;
        File file = new File(filePath);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(screenshot);
        } catch (Throwable t) {
            logError("Cannot save screenshot");
            return null;
        }

        return reportScreenshotsDirName + File.separator + fileName;
    }

    public synchronized static void generateHtmlReport(TestReport report) {
        try (FileWriter writer = new FileWriter(reportFilePath)) {
            writer.write("<html><head><title>Test Report - " + reportName + "</title>");
            writer.write("<style>");
            writer.write("body { font-family: Arial, sans-serif; margin: 20px; color: black; }");
            writer.write("h1 { color: #333; }");
            writer.write(".summary-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
            writer.write(".summary-table th, .summary-table td { padding: 8px; border: 1px solid #ddd; text-align: left; }");
            writer.write(".summary-table th { background-color: #f2f2f2; }");
            writer.write(".summary-table td { text-align: center; }");

            writer.write("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
            writer.write("table, th, td { border: 1px solid #ddd; }");
            writer.write("th, td { padding: 8px; text-align: left; }");
            writer.write("th { background-color: #f2f2f2; }");
            writer.write(".status-failed { background-color: #f8d7da; }");
            writer.write(".status-in-progress { background-color: #d6d8db; }");
            writer.write(".status-passed { background-color: #d4edda; }");
            writer.write(".scenario-group { background-color: #f2f2f2; cursor: pointer; }");
            writer.write(".message { display: none; }");
            writer.write(".message td { padding: 8px; background-color: #f2f2f2; }");
            writer.write(".message .screenshot-thumbnail { width: 200px; }");
            writer.write(".fixed-width { width: 100px; }");
            writer.write(".auto-width { width: auto; }");
            writer.write(".top-align { vertical-align: top; }");

            writer.write("</style>");
            writer.write("<script>");
            writer.write("function toggleMessage(element) {");
            writer.write("  var messageRow = element.nextElementSibling;");
            writer.write("  if (messageRow.style.display === 'table-row') {");
            writer.write("    messageRow.style.display = 'none';");
            writer.write("  } else {");
            writer.write("    messageRow.style.display = 'table-row';");
            writer.write("  }");
            writer.write("}");
            writer.write("</script>");
            writer.write("</head><body>");

            // Header
            writer.write("<h1>Test Report - " + reportName + "</h1>");
            writer.write("<table class='summary-table'>");
            writer.write("<tr><th>Start time</th><th>End time</th><th>Duration</th><th>Passed tests</th><th>Failed tests</th></tr>");
            writer.write("<tr>");
            writer.write("<td>" + report.getStart().format(fullDateTimeFormatter) + "</td>");
            if (report.getEnd() != null) {
                writer.write("<td>" + report.getEnd().format(fullDateTimeFormatter) + "</td>");
            } else {
                writer.write("<td>N/A</td>");
            }
            writer.write("<td>" + formatDuration(report.getDuration()) + "</td>");
            long passedCount = report.getTestScenarios().stream().filter(s -> s.getStatus() == TestStatus.PASSED).count();
            long failedCount = report.getTestScenarios().stream().filter(s -> s.getStatus() == TestStatus.FAILED).count();
            writer.write("<td>" + passedCount + "</td>");
            writer.write("<td>" + failedCount + "</td>");
            writer.write("</tr>");
            writer.write("</table>");

            // Generate tables
            generateFailedTable(writer, report.getTestScenarios());
            generateInProgressTable(writer, report.getTestScenarios());
            generatePassedTable(writer, report.getTestScenarios());

            writer.write("</body></html>");
        } catch (Throwable t) {
            logError("Cannot generate html report. Details: " + ExceptionHelper.getDetailedExceptionInfo(t));
        }
    }

    private static void generateInProgressTable(FileWriter writer, List<TestScenario> scenarios) throws IOException {
        List<TestScenario> filteredScenarios = scenarios
                .stream()
                .filter(s -> s.getStatus() == TestStatus.IN_PROGRESS)
                .sorted(Comparator.comparing(TestScenario::getStart))
                .collect(Collectors.toList());


        writer.write("<h2>Tests In Progress</h2>");
        writer.write("<table>");
        writer.write("<tr><th class='fixed-width'>Start Time</th><th class='fixed-width'>Duration</th><th class='auto-width'>Test Name</th></tr>");

        for (TestScenario scenario : filteredScenarios) {
            writer.write("<tr class='status-in-progress'>");
            writer.write("<td class='start-time fixed-width'>" + scenario.getStart().format(shortDateTimeFormatter) + "</td>");
            writer.write("<td class='duration fixed-width'>" + formatDuration(scenario.getDuration()) + "</td>");
            writer.write("<td class='test-name auto-width'>" + scenario.getName() + "</td>");
            writer.write("</tr>");
        }

        writer.write("</table>");
    }

    private static void generatePassedTable(FileWriter writer, List<TestScenario> scenarios) throws IOException {
        List<TestScenario> filteredScenarios = scenarios
                .stream()
                .filter(s -> s.getStatus() == TestStatus.PASSED)
                .sorted(Comparator.comparing(TestScenario::getStart))
                .collect(Collectors.toList());


        writer.write("<h2>Passed Tests</h2>");
        writer.write("<table>");
        writer.write("<tr><th class='fixed-width'>Start Time</th><th class='fixed-width'>Duration</th><th class='auto-width'>Test Name</th></tr>");

        for (TestScenario scenario : filteredScenarios) {
            List<TestStepSummary> testStepSummaryList = scenario
                    .getSteps()
                    .stream()
                    .collect(Collectors.groupingBy(s -> s.getName()))
                    .entrySet()
                    .stream()
                    .map(m -> new TestStepSummary(m.getKey(), m.getValue()))
                    .sorted((step1, step2) -> step2.getTotalTime().compareTo(step1.getTotalTime()))
                    .limit(15)
                    .toList();

            writer.write("<tr class='status-passed' onclick='toggleMessage(this)'>");
            writer.write("<td class='start-time fixed-width'>" + scenario.getStart().format(shortDateTimeFormatter) + "</td>");
            writer.write("<td class='duration fixed-width'>" + formatDuration(scenario.getDuration()) + "</td>");
            writer.write("<td class='test-name auto-width'>" + scenario.getName() + "</td>");
            writer.write("</tr>");

            writer.write("<tr class='message'>");
            writer.write("<td colspan='3'>");
            writer.write("<table>");
            writer.write("<thead>");
            writer.write("<tr>");
            writer.write("<td class='fixed-width'>Total time</td>");
            writer.write("<td class='fixed-width'>AVG Time</td>");
            writer.write("<td class='fixed-width'>Count</td>");
            writer.write("<td class='auto-width'>Step Name</td>");
            writer.write("</tr>");
            writer.write("</thead>");
            writer.write("<tbody>");
            for (TestStepSummary testStepSummary : testStepSummaryList) {
                writer.write("<tr>");
                writer.write("<td class='top-align'>" + formatDuration(testStepSummary.getTotalTime()) + "</td>");
                writer.write("<td class='top-align'>" + formatDuration(testStepSummary.getAverageTime()) + "</td>");
                writer.write("<td class='top-align'>" + testStepSummary.getCount() + "</td>");
                writer.write("<td class='top-align'>" + testStepSummary.getName() + "</td>");
                writer.write("</tr>");
            }
            writer.write("</tbody>");
            writer.write("</table>");
            writer.write("</td>");
            writer.write("</tr>");
        }

        writer.write("</table>");
    }

    private static void generateFailedTable(FileWriter writer, List<TestScenario> scenarios) throws IOException {
        Map<String, List<TestScenario>> filteredScenarios = scenarios.stream()
                .filter(s -> s.getStatus() == TestStatus.FAILED)
                .sorted(Comparator.comparing(TestScenario::getStart))
                .collect(Collectors.groupingBy(TestScenario::getFailedStep));

        writer.write("<h2>Failed Tests</h2>");
        writer.write("<table>");
        writer.write("<tr><th class='fixed-width'>Group</th><th class='fixed-width'>Start Time</th><th class='fixed-width'>Duration</th><th class='auto-width'>Test Name</th></tr>");

        for (Map.Entry<String, List<TestScenario>> scenarioGroup : filteredScenarios.entrySet()) {
            writer.write("<tr class='status-failed'>");
            writer.write("<td colspan='1' class='top-align'>");
            writer.write("%s tests".formatted(scenarioGroup.getValue().size()));
            writer.write("</td>");
            writer.write("<td colspan='3' class='top-align'>");
            writer.write(scenarioGroup.getKey());
            writer.write("</td>");
            writer.write("</tr>");

            for (TestScenario scenario : scenarioGroup.getValue()) {
                writer.write("<tr class='scenario-group' onclick='toggleMessage(this)'>");
                writer.write("<td class='status fixed-width'></td>");
                writer.write("<td class='start-time fixed-width'>" + scenario.getStart().format(shortDateTimeFormatter) + "</td>");
                writer.write("<td class='duration fixed-width'>" + formatDuration(scenario.getDuration()) + "</td>");
                writer.write("<td class='test-name auto-width'>" + scenario.getName() + "</td>");
                writer.write("</tr>");

                writer.write("<tr class='message'>");
                writer.write("<td colspan='1' class='top-align'></td>");
                writer.write("<td colspan='2' class='top-align'><a href='" + scenario.getScreenshotFilepath() + "' target='_blank'>");
                writer.write("<img src='" + scenario.getScreenshotFilepath() + "' class='screenshot-thumbnail' /></a></td>");
                writer.write("<td colspan='1' class='top-align'>" + scenario.getFailedLogs() + "</td>");
                writer.write("</tr>");
            }
        }

        writer.write("</table>");
    }

    private static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        long hours = absSeconds / 3600;
        long minutes = (absSeconds % 3600) / 60;
        long secs = absSeconds % 60;

        StringBuilder formattedTime = new StringBuilder();
        if (hours > 0) {
            formattedTime.append(hours).append(" h ");
        }
        if (minutes > 0 || hours > 0) {
            formattedTime.append(minutes).append(" min ");
        }
        formattedTime.append(secs).append(" sec");

        return formattedTime.toString().trim();
    }
}