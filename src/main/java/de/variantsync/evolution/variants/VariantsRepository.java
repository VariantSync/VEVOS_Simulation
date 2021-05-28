package de.variantsync.evolution.variants;

import de.variantsync.evolution.util.list.ListHeadTailView;
import de.variantsync.evolution.variants.blueprints.VariantsRevisionBlueprint;
import de.variantsync.evolution.repository.Branch;
import de.variantsync.evolution.repository.ISPLRepository;
import de.variantsync.evolution.repository.IVariantsRepository;
import de.variantsync.evolution.util.*;
import de.variantsync.evolution.util.list.NonEmptyList;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation for IVariantsRepository.
 * Given a history of VariantsRevisionBlueprints, this class will generate a git repository with variants from
 * the original ISPLRepository.
 */
public class VariantsRepository implements IVariantsRepository {
    private Map<String, Branch> branchesByName;
    private final Path localPath;
    public Optional<VariantsRevision> revision0;

    /**
     * Creates a new VariantsRepository that will generate to the given directory.
     * @param localPath The directory where the VariantsRepository is already located or should be located.
     * @param splRepo The ISPLRepository from whose artefacts, variants should be generated.
     * @param blueprintHistory A history of blueprints on how to generate the variants.
     *                         The variants revisions will be generated in the order the blueprints appear in the list.
     */
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

    /**
     * Checks if the repository already exists and localPath.
     * If it does, reads all relevant metadata from it, such as the currently checked out commit and the existing branches.
     */
    private void parseRepoMetadata() {
        // TODO: Implement Issue 11 here.
        // If we start repo generation from the beginning, nothing has to be done, thus we have no branches in the beginning:
        branchesByName = new HashMap<>();
    }

    /**
     * Discards all blueprints from the given history for which revisions where already generated.
     * This means, this removes blueprints from the head of the given history until the current head was not already generated.
     * @param history The history to filter already generated revisions from.
     * @return A list of blueprints that still have to be generated.
     */
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

    @Override
    public Optional<VariantsRevision> getStartRevision() {
        return revision0;
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
    public VariantCommit checkoutCommit(VariantCommit variantCommit) {
        // TODO: Implement Issue #12 here.
        throw new NotImplementedException();
    }

    @Override
    public void checkoutBranch(Branch branch) {
        // TODO: Implement Issue #12 here.
        throw new NotImplementedException();
    }

    @Override
    public VariantCommit getCurrentCommit() {
        // TODO: Implement Issue #12 here.
        throw new NotImplementedException();
    }

    @Override
    public Optional<VariantCommit> commit(String message) {
        // TODO: Implement Issue #12 here.
        throw new NotImplementedException();
    }
}
