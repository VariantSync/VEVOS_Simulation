package de.variantsync.evolution.io.kernelhaven;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.variability.pc.PresenceConditions;
import de.variantsync.evolution.variability.pc.SourceCodeFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class KernelHavenPCLoader implements ResourceLoader<PresenceConditions> {
    @Override
    public boolean canLoad(Path p) {
        return p.endsWith(".csv");
    }

    @Override
    public PresenceConditions load(Path p) {
        final List<SourceCodeFile> files = new ArrayList<>();
        // TODO: Implement Issue #9 here.
        return new PresenceConditions(files);
    }
}
