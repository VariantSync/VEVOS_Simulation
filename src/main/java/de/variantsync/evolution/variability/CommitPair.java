package de.variantsync.evolution.variability;

import de.variantsync.evolution.repository.Commit;

public record CommitPair<T extends Commit<?>>(T parent, T child) {}
