package de.variantsync.evolution;

import de.variantsync.evolution.blueprints.VariantsRevisionBlueprint;
import de.variantsync.repository.Branch;
import de.variantsync.repository.ISPLRepository;
import de.variantsync.repository.IVariantsRepository;
import de.variantsync.util.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VariantsRepository implements IVariantsRepository {
    private Map<String, Branch> branchesByName;
    private final Path localPath;
    public Optional<VariantsRevision> revision0;

    public VariantsRepository(
            Path localPath,
            ISPLRepository splRepo,
            NonEmptyList<VariantsRevisionBlueprint> blueprintHistory)
    {
        this.localPath = localPath;
        parseRepoMetadata();

        final ListHeadTailView<VariantsRevisionBlueprint> history = filterExistingRevisions(new ListHeadTailView<>(blueprintHistory));
        revision0 = history.safehead().map(blueprint -> new VariantsRevision(splRepo, this, blueprint, history.tail()));
    }

    private void parseRepoMetadata() {
        // TODO: Implement Issue 11 here.
        // If we start repo generation from the beginning, nothing has to be done, thus we have no branches in the beginning:
        branchesByName = new HashMap<>();
    }

    private ListHeadTailView<VariantsRevisionBlueprint> filterExistingRevisions(ListHeadTailView<VariantsRevisionBlueprint> history) {
        branchesByName = new HashMap<>();
        // TODO: Implement Issue 11 here.
        // E.g. if we see that the first blueprint was already processed then we could return history.tail().
        // 1.) Find the last valid revision.
        // 2.) If there were commits after that revision they were incomplete (e.g., we generated some but not all variants).
        //     Thus, delete all changes after the last valid revision. This might require manipulating the git history.
        // 3.) Cut the first n blueprints in `history` such that in the end `history.head()` is the first blueprint whose revision has to be generated.
        //     (The blueprint before `history.head()` would be the blueprint that produced the last valid revision from 1).
        return history;
    }

    public Lazy<Optional<VariantsRevision>> generateNext() {
        return Functional.match(
                revision0,
                VariantsRevision::evolve,
                () -> Lazy.of(Optional::empty)
        );
    }

    public Lazy<Unit> generateAll() {
        // We know that the result of generate is Optional.empty so we don't have to return that.
        return generateAll(Lazy.pure(revision0)).map(l -> Unit.Instance());
    }

    /**
     * This is a special fold to generate all revision starting from the given one.
     * The returned lazy will generate all VariantsRevisions once run.
     * @param firstRevision The revision that denotes the start of the history to generate.
     * @return A lazy that will generate all VariantsRevisions once run.
     */
    private static Lazy<Optional<VariantsRevision>> generateAll(Lazy<Optional<VariantsRevision>> firstRevision) {
        // Generate the given revision and then ...
        return firstRevision.bind(
                // ... we may have a next revision to generate mr.
                Functional.match(
                        // If we have, a revision r (i.e., mr is not empty), we recursively generate the remaining history.
                        /*    Just */ r  -> generateAll(r.evolve()),
                        /* Nothing */ () -> Lazy.of(Optional::empty)));
    }

    @Override
    public Path getPath() {
        return localPath;
    }

    @Override
    public Branch getBranchByName(String name) {
        return branchesByName.computeIfAbsent(name, Branch::new);
    }

    @Override
    public VariantsCommit checkoutCommit(VariantsCommit variantsCommit) {
        throw new NotImplementedException();
    }

    @Override
    public void checkoutBranch(Branch branch) {
        throw new NotImplementedException();
    }

    @Override
    public VariantsCommit getCurrentCommit() {
        throw new NotImplementedException();
    }

    @Override
    public VariantsCommit commit(String message) {
        throw new NotImplementedException();
    }
}
