package de.variantsync.evolution.util.math;
/**
 * Represents an interval of integers [from, to].
 */
public record Interval(int from, int to) {
    /**
     * @return True iff i is within this interval.
     */
    public boolean contains(int i) {
        return from <= i && i <= to;
    }
}
