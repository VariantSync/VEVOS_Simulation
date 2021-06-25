package de.variantsync.evolution.variability;

import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.repository.IRepository;

public record CommitPair<T extends Commit<? extends IRepository<T>>>(T parent, T child) {}
