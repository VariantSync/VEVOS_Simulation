package de.variantsync.repository;

import de.variantsync.evolution.blueprints.VariantsRevisionBlueprint;
import de.variantsync.evolution.blueprints.VariantsRevisionFromErrorBlueprint;
import de.variantsync.evolution.blueprints.VariantsRevisionFromVariabilityBlueprint;
import de.variantsync.subjects.VariabilityCommit;
import de.variantsync.util.NonEmptyList;

import java.util.ArrayList;

public record VariabilityHistory(NonEmptyList<NonEmptyList<VariabilityCommit>> commitSequence) {
    public NonEmptyList<VariantsRevisionBlueprint> toBlueprints()
    {
        final NonEmptyList<VariantsRevisionBlueprint> blueprints = new NonEmptyList<>(new ArrayList<>());
        VariantsRevisionFromVariabilityBlueprint lastVariabilityBlueprint = null;

        for (NonEmptyList<VariabilityCommit> coherentSubHistory : commitSequence) {
            for (VariabilityCommit varCommit : coherentSubHistory) {
                lastVariabilityBlueprint = new VariantsRevisionFromVariabilityBlueprint(varCommit, lastVariabilityBlueprint);
                blueprints.add(lastVariabilityBlueprint);
            }

            // We can be sure here that lastVariabilityBlueprint != null because coherentSubHistory is NonEmpty.
            blueprints.add(new VariantsRevisionFromErrorBlueprint(lastVariabilityBlueprint));
        }

        return blueprints;
    }
}
