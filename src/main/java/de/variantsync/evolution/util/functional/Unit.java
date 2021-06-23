package de.variantsync.evolution.util.functional;

/**
 * Unit represents a type that has exactly one value (Instance()).
 */
public class Unit implements Monoid<Unit> {
    private static final Unit instance = new Unit();

    private Unit() {}

    public static Unit Instance() {
        return instance;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Unit;
    }

    @Override
    public int hashCode() {
        return 1; // all instances are the same as there exists just one.
    }

    @Override
    public String toString() {
        return "()";
    }

    @Override
    public Unit mEmpty() {
        return Instance();
    }

    @Override
    public Unit mAppend(Unit other) {
        return mEmpty();
    }
}
