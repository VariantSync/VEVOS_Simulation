package de.variantsync.evolution.variant;

import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.variantsync.evolution.spl.SPLRevision;

public record VariantRevision(Configuration configuration,
                              VariantRevision predecessor) {
    public VariantRevision evolveTo(SPLRevision evolution) {
        // TODO: Integrate artefacts
        // TODO: Verify that configuration is still valid
        return new VariantRevision(configuration(), this);
    }
}
