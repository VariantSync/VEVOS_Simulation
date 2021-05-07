package de.variantsync.evolution.io;

import de.variantsync.evolution.util.functional.Result;

import java.nio.file.Path;

public interface ResourceLoader<T> {
    boolean canLoad(Path p);
    Result<T, Exception> load(Path p);
}
