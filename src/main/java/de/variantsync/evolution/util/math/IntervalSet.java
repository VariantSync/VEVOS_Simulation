package de.variantsync.evolution.util.math;

import de.variantsync.evolution.util.functional.Monoid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A set of integer intervals.
 */
public class IntervalSet implements Monoid<IntervalSet>, Iterable<Interval> {
    private final List<Interval> intervals;

    public IntervalSet() {
        intervals = new ArrayList<>();
    }

    public IntervalSet(Interval interval) {
        intervals = new ArrayList<>();
        intervals.add(interval);
    }

    /**
     * Add a new interval [a, b] to this set.
     */
    public void add(int a, int b) {
        intervals.add(new Interval(a, b));
    }

    /**
     * Check if the given point lies within any interval in this set.
     * @return True iff the given point is within any interval in this set.
     */
    public boolean contains(int i) {
        return intervals.stream().anyMatch(interval -> interval.contains(i));
    }

    @Override
    public IntervalSet mEmpty() {
        return new IntervalSet();
    }

    @Override
    public IntervalSet mAppend(IntervalSet other) {
        final IntervalSet i = mEmpty();
        i.intervals.addAll(this.intervals);
        i.intervals.addAll(other.intervals);
        return i;
    }

    /**
     * Add all intervals in the given set to this IntervalSet.
     * @param other IntervalSet whose intervals should be added to this interval.
     */
    public void mappendInline(IntervalSet other) {
        this.intervals.addAll(other.intervals);
    }

    @Override
    public Iterator<Interval> iterator() {
        return intervals.iterator();
    }
}
