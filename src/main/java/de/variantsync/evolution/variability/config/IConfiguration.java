package de.variantsync.evolution.variability.config;

import org.prop4j.Node;

import java.util.Map;

@FunctionalInterface
public interface IConfiguration {
    boolean satisfies(Node formula);
}
