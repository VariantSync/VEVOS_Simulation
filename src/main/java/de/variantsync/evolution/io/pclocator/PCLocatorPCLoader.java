package de.variantsync.evolution.io.pclocator;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.util.NotImplementedException;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.pc.Artefact;

import java.nio.file.Path;

/**
 * Loads presence conditions computed with PCLocator.
 * https://github.com/ekuiter/PCLocator
 */
public class PCLocatorPCLoader implements ResourceLoader<Artefact> {
    @Override
    public boolean canLoad(Path p) {
        // TODO: Implement Issue #14 here.
        return false;
    }

    @Override
    public Result<Artefact, Exception> load(Path p) {
        // TODO: Implement Issue #14 here.
        return Result.Failure(new NotImplementedException());
    }
}
