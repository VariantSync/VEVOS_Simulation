package org.variantsync.vevos.simulation.variability;

import org.variantsync.functjonal.list.NonEmptyList;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface SequenceExtractor {
    List<NonEmptyList<SPLCommit>> extract(final Collection<SPLCommit> commits);
}
