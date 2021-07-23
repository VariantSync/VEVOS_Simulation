package de.variantsync.evolution.util.functional;

/**
 * Interfaces for class that support monoidal composition.
 * Implementing classes are expected to also supply a
 * public static M mEmpty();
 * @param <M> Deriving class.
 */
public interface Monoid<M> {
    M mAppend(M other);
}
