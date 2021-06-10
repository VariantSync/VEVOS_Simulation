package de.variantsync.evolution.util.math;

import de.variantsync.evolution.util.functional.Monoid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IntervalSet implements Monoid<IntervalSet>, Iterable<Interval> {
    private final List<Interval> intervals;

    public IntervalSet() {
        intervals = new ArrayList<>();
    }

    public IntervalSet(Interval interval) {
        intervals = new ArrayList<>();
        intervals.add(interval);
    }

    public void add(int a, int b) {
        intervals.add(new Interval(a, b));
    }

    public boolean contains(int i) {
        return intervals.stream().anyMatch(interval -> interval.contains(i));
    }

    @Override
    public IntervalSet mempty() {
        return new IntervalSet();
    }

    @Override
    public IntervalSet mappend(IntervalSet other) {
        final IntervalSet i = mempty();
        i.intervals.addAll(this.intervals);
        i.intervals.addAll(other.intervals);
        return i;
    }

    public void mappendInline(IntervalSet other) {
        this.intervals.addAll(other.intervals);
    }

    @Override
    public Iterator<Interval> iterator() {
        return intervals.iterator();
    }
}
