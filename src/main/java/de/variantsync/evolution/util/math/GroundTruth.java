package de.variantsync.evolution.util.math;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.util.functional.Monoid;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A set of integer intervals.
 */
public class GroundTruth implements Monoid<GroundTruth>, Iterable<Chunk> {
    private final List<Chunk> chunks;

    public GroundTruth() {
        chunks = new ArrayList<>();
    }

    /**
     * Add a new interval [a, b] to this set.
     */
    public void add(int a, int b, Node presenceCondition) {
        chunks.add(new Chunk(a, b, presenceCondition));
    }

    public GroundTruth project(Variant variant) {
        final GroundTruth projection = new GroundTruth();
        projection.chunks.addAll(this.chunks);
        projection.projectInline(variant);
        return projection;
    }

    public void projectInline(Variant variant) {
        int currentLine = 0;
        int chunkLength;
        for (Chunk chunk : chunks) {
            chunkLength = chunk.getLength();
            chunk.setFrom(currentLine);
            currentLine += chunkLength;
            chunk.setTo(currentLine - 1);
        }
    }

    /**
     * Check if the given point lies within any interval in this set.
     * @return True iff the given point is within any interval in this set.
     */
    public boolean contains(int i) {
        return chunks.stream().anyMatch(interval -> interval.contains(i));
    }

    @Override
    public GroundTruth mEmpty() {
        return new GroundTruth();
    }

    @Override
    public GroundTruth mAppend(GroundTruth other) {
        final GroundTruth i = mEmpty();
        i.chunks.addAll(this.chunks);
        i.chunks.addAll(other.chunks);
        return i;
    }

    /**
     * Add all intervals in the given set to this IntervalSet.
     * @param other IntervalSet whose intervals should be added to this interval.
     */
    public void mappendInline(GroundTruth other) {
        this.chunks.addAll(other.chunks);
    }

    @Override
    public Iterator<Chunk> iterator() {
        return chunks.iterator();
    }

    @Override
    public String toString() {
        return "GroundTruth{" +
                "chunks=" + chunks +
                '}';
    }
}
