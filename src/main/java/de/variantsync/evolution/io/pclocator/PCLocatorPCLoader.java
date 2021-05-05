package de.variantsync.evolution.io.pclocator;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.util.NotImplementedException;
import de.variantsync.evolution.variability.PresenceConditions;

import java.nio.file.Path;

public class PCLocatorPCLoader implements ResourceLoader<PresenceConditions> {
    @Override
    public boolean canLoad(Path p) {
        // TODO: Implement Issue #9 here.
        throw new NotImplementedException();
    }

    @Override
    public PresenceConditions load(Path p) {
        // TODO: Implement Issue #9 here.
        throw new NotImplementedException();
    }
}
