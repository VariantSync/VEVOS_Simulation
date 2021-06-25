package de.variantsync.evolution.util.math;

import org.prop4j.Node;

import java.util.Objects;

/**
 * Represents an interval of integers [from, to].
 */
public final class Chunk {
    private int from;
    private int to;
    private Node pc;

    /**
     */
    public Chunk(int from, int to, Node pc) {
        this.from = from;
        this.to = to;
        this.pc = pc;
    }

    /**
     * @return True iff i is within this interval.
     */
    public boolean contains(int i) {
        return from <= i && i <= to;
    }

    public int getLength() {
        return to - from + 1;
    }

    void setFrom(int from) {
        this.from = from;
    }

    void setTo(int to) {
        this.to = to;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public Node pc() {
        return pc;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Chunk) obj;
        return this.from == that.from &&
                this.to == that.to &&
                Objects.equals(this.pc, that.pc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, pc);
    }

    @Override
    public String toString() {
        return "Chunk[" +
                "from=" + from + ", " +
                "to=" + to + ", " +
                "pc=" + pc + ']';
    }

}
