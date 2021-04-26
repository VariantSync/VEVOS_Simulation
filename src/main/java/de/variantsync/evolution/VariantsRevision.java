package de.variantsync.evolution;

import de.variantsync.evolution.blueprints.VariantsRevisionBlueprint;
import de.variantsync.repository.Branch;
import de.variantsync.repository.ISPLRepository;
import de.variantsync.repository.IVariantsRepository;
import de.variantsync.util.functional.Lazy;
import de.variantsync.util.ListHeadTailView;
import de.variantsync.util.functional.MonadTransformer;
import de.variantsync.util.functional.Unit;

import java.util.Map;
import java.util.Optional;

public class VariantsRevision {
    public static record Branches(Map<Branch, VariantCommit> commitOf) {}

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

    /**
     * Runs evolve and repeats that for all subsequent VariantsRevisions.
     * @return A lazy computation holding the entire generation process.
     */
    public Lazy<Unit> evolveAll() {
        // We know that the result of evolveAll is Lazy.of(Optional::empty) so we don't have to return that.
        return evolveAll(MonadTransformer.pure(this)).map(l -> Unit.Instance());
    }

    /**
     * This is a special fold to generate all revision starting from the given one.
     * The returned lazy will generate all VariantsRevisions once run.
     * @param firstRevision The revision that denotes the start of the history to generate.
     * @return A lazy that will generate all VariantsRevisions once run.
     */
    private static Lazy<Optional<VariantsRevision>> evolveAll(Lazy<Optional<VariantsRevision>> firstRevision) {
        return MonadTransformer.bind(firstRevision, r  -> evolveAll(r.evolve()));
    }

    public ISPLRepository getSPLRepo() {
        return splRepo;
    }

    public IVariantsRepository getVariantsRepo() {
        return variantsRepo;
    }
}
