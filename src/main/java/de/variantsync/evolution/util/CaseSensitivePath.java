package de.variantsync.evolution.util;

import java.nio.file.Path;
import java.util.Objects;

public record CaseSensitivePath(Path path) implements Comparable<CaseSensitivePath> {
    public static CaseSensitivePath of(String first, String... levels) {
        return new CaseSensitivePath(Path.of(first, levels));
    }

    public CaseSensitivePath resolve(CaseSensitivePath other) {
        return new CaseSensitivePath(this.path.resolve(other.path));
    }

    public CaseSensitivePath resolve(String other) {
        return new CaseSensitivePath(this.path.resolve(other));
    }

    public CaseSensitivePath resolve(String first, String... levels) {
        return new CaseSensitivePath(this.path.resolve(Path.of(first, levels)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaseSensitivePath that = (CaseSensitivePath) o;
        return path.equals(that.path) && path.toString().equals(that.path.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public int compareTo(CaseSensitivePath o) {
        return path.compareTo(o.path);
    }
}
