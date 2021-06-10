package de.variantsync.evolution.util.math;

public record Interval(int from, int to) {
    public boolean contains(int i) {
        return from <= i && i <= to;
    }
}
