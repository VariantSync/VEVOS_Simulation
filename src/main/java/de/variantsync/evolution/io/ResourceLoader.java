package de.variantsync.evolution.io;

import java.nio.file.Path;

public interface ResourceLoader<T> {
    boolean canLoad(Path p);
    T load(Path p);
}
