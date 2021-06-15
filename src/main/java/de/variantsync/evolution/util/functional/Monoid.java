package de.variantsync.evolution.util.functional;

public interface Monoid<M> {
    M mEmpty();
    M mAppend(M other);
}
