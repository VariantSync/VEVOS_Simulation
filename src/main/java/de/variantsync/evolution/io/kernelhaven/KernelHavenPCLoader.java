package de.variantsync.evolution.io.kernelhaven;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.variability.PresenceConditions;

import java.nio.file.Path;

public class KernelHavenPCLoader implements ResourceLoader<PresenceConditions> {
    @Override
    public boolean canLoad(Path p) {
        return p.endsWith(".csv");
    }

    @Override
    public PresenceConditions load(Path p) {
        // TODO: Implement Issue #9 here.
        return null;
    }
}
