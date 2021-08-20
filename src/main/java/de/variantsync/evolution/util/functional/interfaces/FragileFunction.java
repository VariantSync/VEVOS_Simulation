package de.variantsync.evolution.util.functional.interfaces;

@FunctionalInterface
public interface FragileFunction<A, B, E extends Exception> {
    B run(A a) throws E;
}
