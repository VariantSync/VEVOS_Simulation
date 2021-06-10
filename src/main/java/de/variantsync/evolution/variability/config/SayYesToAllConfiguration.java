package de.variantsync.evolution.variability.config;

import org.prop4j.Node;

public class SayYesToAllConfiguration implements IConfiguration {
    @Override
    public boolean satisfies(Node formula) {
        return true;
    }
}
