package net.automation.utils;

import lombok.Getter;

import java.time.Duration;
import java.util.function.Supplier;

public class TryOptions {
    @Getter
    private String actionName;

    @Getter
    private Supplier<Boolean> action;

    @Getter
    private Supplier<Boolean> failAction;

    @Getter
    private Duration failActionInterval;

    @Getter
    private Duration sleep = Timeouts.getShortSleep();

    @Getter
    private Duration timeout = Timeouts.getShortTimeout();

    @Getter
    private Integer maxAttempts = Integer.MAX_VALUE;

    /**
     * Creates new instance of options to be used in "Try" class
     * @param actionName describes the name of the action which will be executed in "Try" class
     * @param stringFormatArgs Optional string format arguments for the action name
     */
    public TryOptions(String actionName, Object... stringFormatArgs) {
        withName(actionName.formatted(stringFormatArgs));
        failActionInterval = Duration.ZERO;
    }

    /**
     * Define the action that will be executed in "Try" class
     * @param action the action to be executed.
     *               The action should return a flag indicating whether if was executed successfully or not.
     * @return options
     */
    public TryOptions withAction(Supplier<Boolean> action) {
        this.action = action;
        return this;
    }

    /**
     * Define the optional fail action that will be executed in case of errors
     * @param failAction the fail action to be executed in case of errors
     *               The fail action should return a flag indicating whether if was executed successfully or not.
     * @return options
     */
    public TryOptions withFailAction(Supplier<Boolean> failAction) {
        this.failAction = failAction;
        return this;
    }

    /**
     * Defines the time interval between executing the fail action.
     * By default, the interval is set to 0 which means the fail action will be executed on every fail.
     * @param failActionInterval duration of interval
     * @return options
     */
    public TryOptions withFailActionInterval(Duration failActionInterval) {
        this.failActionInterval = failActionInterval;
        return this;
    }

    /**
     * Define the name of the action which will be executed in "Try" class
     * @param name name of the action
     * @return options
     */
    public TryOptions withName(String name) {
        this.actionName = name;
        return this;
    }

    /**
     * Define the duration that will be used for waiting before next attempt of execution the action
     * @param sleep sleep time
     * @return options
     */
    public TryOptions withSleep(Duration sleep) {
        this.sleep = sleep;
        return this;
    }

    /**
     * Define the time period in which the action can be performed multiple times before throwing an exception
     * @param timeout timeout
     * @return options
     */
    public TryOptions withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Define the maximum number of attempts for executing the action before throwing an exception
     * @param maxAttempts maximum number of attempts
     * @return options
     */
    public TryOptions withMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }
}