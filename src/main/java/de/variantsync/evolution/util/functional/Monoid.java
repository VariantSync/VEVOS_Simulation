package de.variantsync.evolution.util.functional;

public interface Monoid<M> {
    M mempty();
    M mappend(M other);
}
