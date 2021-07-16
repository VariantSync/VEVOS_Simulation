package de.variantsync.evolution.io;

import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;

import java.nio.file.Path;

public interface ResourceWriter<T> {
    /**
     * @return True iff this load can load the resource at the given path p.
     */
    boolean canWrite(Path p);

    /**
     * Loads the resource at the given path p.
     * Will only be invoked by Resources when canLoad(p) returned true so no duplicate check is necessary.
     * @return Either the loaded resource at path p or an exception describing the failure.
     */
    Result<Unit, Exception> write(T object, Path p);
}
