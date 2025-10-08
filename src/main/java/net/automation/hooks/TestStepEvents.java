package net.automation.hooks;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestStepStarted;

public class TestStepEvents implements ConcurrentEventListener {
    private static final ThreadLocal<String> testStepName = new ThreadLocal();
    public EventHandler<TestStepStarted> testStepStartedHandler = (testStepStarted) -> {
        try {
            if (testStepStarted.getTestStep() != null && testStepStarted.getTestStep() instanceof PickleStepTestStep) {
                PickleStepTestStep pickleStep = (PickleStepTestStep)testStepStarted.getTestStep();
                testStepName.set(pickleStep.getPattern());
            }
        } catch (Throwable var2) {
           System.out.println("Cannot save current step name : " + var2);
            testStepName.set("Unknown step");
        }

    };

    public TestStepEvents() {
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestStepStarted.class, this.testStepStartedHandler);
    }

    public static synchronized String getTestStepName() {
        String currentStepName = (String)testStepName.get();
        return currentStepName != null ? currentStepName : "Unknown step";
    }
}
