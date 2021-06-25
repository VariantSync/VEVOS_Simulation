package de.variantsync.evolution.util.functional;

public interface Monoid<M> {
    /* static */ M mEmpty();
    M mAppend(M other);
}
