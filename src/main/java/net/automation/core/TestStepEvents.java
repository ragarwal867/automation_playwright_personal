package net.automation.core;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestStepStarted;

import java.util.List;

import static net.automation.utils.Logger.logError;

public class TestStepEvents implements ConcurrentEventListener {
    private static final ThreadLocal<String> testStepName = new ThreadLocal<>();

    public EventHandler<TestStepStarted> testStepStartedHandler = (testStepStarted) -> {
        try {
            if (testStepStarted.getTestStep() != null && testStepStarted.getTestStep() instanceof PickleStepTestStep) {
                PickleStepTestStep pickleStep = (PickleStepTestStep) testStepStarted.getTestStep();
                String keyword = pickleStep.getStep().getKeyword();
                String stepText = pickleStep.getStep().getText();
                StringBuilder fullStep = new StringBuilder();
                fullStep. append(keyword).append(stepText);

                if (pickleStep.getStep().getArgument() != null) {
                    fullStep.append("\n");
                    fullStep.append(formatStepArgument(pickleStep));
                }

                testStepName.set(fullStep.toString());
            }
        } catch (Throwable t) {
            logError("Cannot save current step name");
            testStepName.set("Unknown step");
        }
    };

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestStepStarted.class, testStepStartedHandler);
    }

    public static synchronized String getTestStepName() {
        String currentStepName = testStepName.get();
        return currentStepName != null
                ? currentStepName
                : "Unknown step";
    }

    private static String formatStepArgument(PickleStepTestStep pickleStep) {
        StringBuilder argument = new StringBuilder();

        try {
            io.cucumber.plugin.event.Step step = pickleStep.getStep();

            if (step.getArgument() != null) {
                io.cucumber.plugin.event.StepArgument stepArgument = step.getArgument();

                // Check if it's a DataTable
                if (stepArgument instanceof io.cucumber.plugin.event.DataTableArgument) {
                    io.cucumber.plugin.event.DataTableArgument dataTableArg =
                            (io.cucumber.plugin.event.DataTableArgument) stepArgument;

                    List<List<String>> rows = dataTableArg.cells();

                    // Format as table
                    for (List<String> row : rows) {
                        argument.append("      | ");
                        argument.append(String.join(" | ", row));
                        argument. append(" |\n");
                    }
                }
                // Check if it's a DocString (multi-line text)
                else if (stepArgument instanceof io.cucumber.plugin.event.DocStringArgument) {
                    io.cucumber.plugin.event.DocStringArgument docStringArg =
                            (io.cucumber.plugin.event.DocStringArgument) stepArgument;

                    argument.append("      \"\"\"\n");
                    argument.append("      ").append(docStringArg.getContent()).append("\n");
                    argument.append("      \"\"\"\n");
                }
            }
        } catch (Exception e) {
            logError("Cannot format step argument:  " + e.getMessage());
        }

        return argument. toString();
    }
}