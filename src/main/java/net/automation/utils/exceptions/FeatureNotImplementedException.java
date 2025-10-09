package net.automation.utils.exceptions;

public class FeatureNotImplementedException extends UnsupportedOperationException {

    public FeatureNotImplementedException() {
        super("Feature not implemented.");
    }
}