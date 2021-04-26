package de.variantsync.repository;

import de.variantsync.evolution.blueprints.VariantsRevisionBlueprint;
import de.variantsync.evolution.blueprints.VariantsRevisionFromErrorBlueprint;
import de.variantsync.evolution.blueprints.VariantsRevisionFromVariabilityBlueprint;
import de.variantsync.subjects.VariabilityCommit;
import de.variantsync.util.list.NonEmptyList;

import java.util.ArrayList;

public record VariabilityHistory(NonEmptyList<NonEmptyList<VariabilityCommit>> commitSequence) {
    public NonEmptyList<VariantsRevisionBlueprint> toBlueprints()
    {
        final int lengthOfList = commitSequence.stream()
                .map(subHistory -> subHistory.size() + 1 /* because of error blueprints*/)
                .reduce(0, Integer::sum);
        final ArrayList<VariantsRevisionBlueprint> blueprints = new ArrayList<>(lengthOfList);
        VariantsRevisionFromVariabilityBlueprint lastVariabilityBlueprint = null;

        for (NonEmptyList<VariabilityCommit> coherentSubHistory : commitSequence) {
            for (VariabilityCommit varCommit : coherentSubHistory) {
                lastVariabilityBlueprint = new VariantsRevisionFromVariabilityBlueprint(varCommit, lastVariabilityBlueprint);
                blueprints.add(lastVariabilityBlueprint);
            }

            // We can be sure here that lastVariabilityBlueprint != null because coherentSubHistory is NonEmpty.
            blueprints.add(new VariantsRevisionFromErrorBlueprint(lastVariabilityBlueprint));
        }

        return new NonEmptyList<>(blueprints);
    }
}
