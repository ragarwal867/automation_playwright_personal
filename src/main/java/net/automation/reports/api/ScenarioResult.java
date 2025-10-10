package net.automation.reports.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.cucumber.java.Scenario;
import lombok.Builder;
import lombok.Data;
import net.automation.reports.html.TestScenario;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.nio.file.Paths;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class ScenarioResult {
    private Integer id;
    private String scenarioName;
    private String testId;
    private String uri;
    private int lineNumber;
    private Set<ScenarioTag> tags;
    private String status;
    private String failedStep;
    private String screenshotFilepath;
    private TestRun testRun;
    private List<TestStep> steps;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant endTime;


    public static ScenarioResultBuilder fromTestScenario(TestScenario testScenario) {
        ScenarioResultBuilder builder = new ScenarioResultBuilder();
        builder.scenarioName(testScenario.getName());
        builder.status(testScenario.getStatus() == null ? "" : testScenario.getStatus().toString());
        builder.failedStep(testScenario.getFailedStep());
        builder.startTime(testScenario.getStart().toInstant(ZoneOffset.UTC));
        builder.endTime(testScenario.getEnd().toInstant(ZoneOffset.UTC));
        builder.screenshotFilepath(testScenario.getScreenshotFilepath());


        List<TestStep> steps = testScenario.getSteps().stream()
                .map(step -> new TestStep(step.getName(), step.getStatus() == null ? "" : step.getStatus().toString()
                        , step.getStart().toInstant(ZoneOffset.UTC), step.getEnd().toInstant(ZoneOffset.UTC)))
                .toList();

        builder.steps(steps);

        return builder;
    }

    public static class ScenarioResultBuilder {
        public ScenarioResultBuilder usingScenario(Scenario scenario) {
            String relativePath = Paths.get(System.getProperty("user.dir"))
                    .relativize(Paths.get(scenario.getUri()))
                    .toString();

            this.testId(scenario.getId())
                    .uri(relativePath)
                    .lineNumber(scenario.getLine())
                    .tags(
                            scenario.getSourceTagNames().stream()
                                    .map(tagName -> {
                                        ScenarioTag t = new ScenarioTag();
                                        t.setTag(tagName);
                                        return t;
                                    })
                                    .collect(Collectors.toSet())
                    );
            return this;
        }
    }
}
