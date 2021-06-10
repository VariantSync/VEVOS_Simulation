package de.variantsync.evolution.variability.config;

import org.prop4j.Node;

/**
 * Impossible configuration intended to be used for debugging only.
 * This configuration is a satisfying assignment for all propositional formulas.
 */
public class SayYesToAllConfiguration implements IConfiguration {
    @Override
    public boolean satisfies(Node formula) {
        return true;
    }
}
