package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.repository.ISPLRepository;
import de.variantsync.evolution.repository.IVariantsRepository;
import de.variantsync.evolution.util.NotImplementedException;

import java.util.List;

public record PresenceConditions(List<SourceCodeFile> files) {
    // TODO: Implement Issue #1 somewhere here? Also in generateVariant?

    public void generateVariant(Variant variant, ISPLRepository splRepo, IVariantsRepository variantsRepo) {
        // TODO: Implement Issue #2 here.
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return "PresenceConditions: [" +
                files.stream().map(SourceCodeFile::toString).reduce((a, b) -> a + ",\n  " + b) +
                "]";
    }
}
