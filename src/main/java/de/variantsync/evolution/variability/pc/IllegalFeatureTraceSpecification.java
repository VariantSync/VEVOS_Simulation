package de.variantsync.evolution.variability.pc;

/**
 * Exception that is thrown upon parsing illegal specifications of feature traces.
 */
public class IllegalFeatureTraceSpecification extends RuntimeException {
    public IllegalFeatureTraceSpecification(String msg) {
        super(msg);
    }
}
