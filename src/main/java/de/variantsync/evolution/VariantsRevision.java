package de.variantsync.evolution;

import de.variantsync.evolution.blueprints.VariantsRevisionBlueprint;
import de.variantsync.repository.Branch;
import de.variantsync.repository.ISPLRepository;
import de.variantsync.repository.IVariantsRepository;
import de.variantsync.util.Lazy;
import de.variantsync.util.ListHeadTailView;

import java.util.Map;
import java.util.Optional;

public class VariantsRevision {
    public static record Branches(Map<Branch, VariantsCommit> commitOf) {}

    private final ISPLRepository splRepo;
    private final IVariantsRepository variantsRepo;
    private final Lazy<Branches> generate;
    private final Lazy<Optional<VariantsRevision>> evolve;

    VariantsRevision(
            ISPLRepository splRepo,
            IVariantsRepository variantsRepo,
            VariantsRevisionBlueprint blueprint,
            ListHeadTailView<VariantsRevisionBlueprint> remainingHistory)
    {
        this.splRepo = splRepo;
        this.variantsRepo = variantsRepo;

        generate = blueprint.generateArtefactsFor(this);
        evolve = generate.map(commits ->
                        remainingHistory.safehead().map(nextBlueprint ->
                                new VariantsRevision(splRepo, variantsRepo, nextBlueprint, remainingHistory.tail())));
    }

    /**
     * Generates all variants of this revision and commits them to respective branches.
     * @return The revision of the branches that were commited in this revision.
     */
    public Lazy<Branches> generate() {
        return generate;
    }

    /**
     * First, generates all variants (i.e., calls generate) and returns the next revision.
     * @return The next evolution step. Empty, when this was the last evolution step.
     */
    public Lazy<Optional<VariantsRevision>> evolve() {
        return evolve;
    }

    public ISPLRepository getSPLRepo() {
        return splRepo;
    }

    public IVariantsRepository getVariantsRepo() {
        return variantsRepo;
    }
}
