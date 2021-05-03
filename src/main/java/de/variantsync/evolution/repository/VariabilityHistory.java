package de.variantsync.evolution.repository;

import de.variantsync.evolution.variants.blueprints.VariantsRevisionBlueprint;
import de.variantsync.evolution.variants.blueprints.VariantsRevisionFromErrorBlueprint;
import de.variantsync.evolution.variants.blueprints.VariantsRevisionFromVariabilityBlueprint;
import de.variantsync.evolution.variability.VariabilityCommit;
import de.variantsync.evolution.util.list.NonEmptyList;

import java.util.ArrayList;

/**
 * An ordered list of coherent sub-histories of an IVariabilityRepository.
 * The history of the analysed ISPLRepository may contain commits that cannot or should not be analysed
 * (e.g., error or merge commits).
 * Such commits do not allow for generating a single continuous history in the target IVariantsRepository.
 * Thus, a VariabilityHistory to model contains all continuous sub-histories between error commits.
 */
public record VariabilityHistory(NonEmptyList<NonEmptyList<VariabilityCommit>> commitSequence) {
    /**
     * Default implementation to generate instructions (blueprints) for generating an IVariantsRepository for this
     * VariabilityHistory.
     *
     * @return Blueprints to generate an IVariantsRepository from.
     *         The list will contain exactly one VariantsRevisionFromVariabilityBlueprint for each VariabilityCommit
     *         in the sub-histories.
     *         After each continuous sub-history an explicit error commit will be introduced via a
     *         VariantsRevisionFromErrorBlueprint.
     */
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