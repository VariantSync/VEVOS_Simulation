package de.variantsync.evolution.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Wrapper for java.nio.file.Path that is case sensitive.
 * We experienced that Path.of("FOO.c") is equal to Path.of("foo.c").
 * The sole purpose of CaseSensitivePath is to make these unequal.
 */
public record CaseSensitivePath(Path path) implements Comparable<CaseSensitivePath> {
    /**
     * @see Path::of
     */
    public static CaseSensitivePath of(String first, String... levels) {
        return new CaseSensitivePath(Path.of(first, levels));
    }

    /**
     * @see Path::resolve
     */
    public CaseSensitivePath resolve(CaseSensitivePath other) {
        return new CaseSensitivePath(this.path.resolve(other.path));
    }

    /**
     * @see Path::resolve
     */
    public CaseSensitivePath resolve(String other) {
        return new CaseSensitivePath(this.path.resolve(other));
    }

    /**
     * @see Path::resolve but with extra arguments as a shortcut with Path::of
     */
    public CaseSensitivePath resolve(String first, String... levels) {
        return new CaseSensitivePath(this.path.resolve(Path.of(first, levels)));
    }

    /**
     * @return True iff the file or directory this path points to exists.
     */
    public boolean exists() {
        return Files.exists(path);
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

    @Override
    public String toString() {
        return path.toString();
    }
}
