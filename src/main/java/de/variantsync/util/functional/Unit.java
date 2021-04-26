package de.variantsync.util.functional;

/**
 * Unit represents a type that has exactly one value (Instance()).
 */
public class Unit {
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
    public String toString() {
        return "()";
    }
}
