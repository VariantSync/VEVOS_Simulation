package de.variantsync.repository;

import de.variantsync.subjects.VariabilityCommit;
import de.variantsync.util.NonEmptyList;

public record VariabilityHistory(NonEmptyList<NonEmptyList<VariabilityCommit>> commitSequence) {

}
