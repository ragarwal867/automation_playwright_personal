package net.automation.core;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestStepStarted;

import static net.automation.utils.Logger.logError;

public class TestStepEvents implements ConcurrentEventListener {
    private static final ThreadLocal<String> testStepName = new ThreadLocal<>();

    public EventHandler<TestStepStarted> testStepStartedHandler = (testStepStarted) -> {
        try {
            if (testStepStarted.getTestStep() != null && testStepStarted.getTestStep() instanceof PickleStepTestStep) {
                PickleStepTestStep pickleStep = (PickleStepTestStep) testStepStarted.getTestStep();
                testStepName.set(pickleStep.getPattern());
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
}