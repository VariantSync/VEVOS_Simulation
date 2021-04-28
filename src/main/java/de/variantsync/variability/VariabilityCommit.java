package de.variantsync.variability;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.repository.Commit;
import de.variantsync.repository.IVariabilityRepository;
import de.variantsync.util.functional.Lazy;

import java.nio.file.Path;

public class VariabilityCommit extends Commit<IVariabilityRepository> {
    private IVariabilityRepository sourceRepo;
    private final SPLCommit origin;
    private VariabilityCommit[] evolutionParents;

    public VariabilityCommit(IVariabilityRepository source, String commitId, SPLCommit splCommit) {
        super(commitId);
        this.sourceRepo = source;
        origin = splCommit;
    }

    public final Lazy<IFeatureModel> featureModel = Lazy.of(() -> {
        IFeatureModel fm = null;
        sourceRepo.checkoutCommit(this);
        Path fmPath = sourceRepo.getFeatureModelFile();
        // TODO: Implement Issue #3 here: Parse FM from fmPath.
        return fm;
    });

    public final Lazy<FeatureTraces> featureTraces = Lazy.of(() -> {
        FeatureTraces traces = null;
        sourceRepo.checkoutCommit(this);
        Path varPath = sourceRepo.getVariabilityFile();
        // TODO: Implement Issue #9 here: Parse Variability data from varPath
        return traces;
    });

    void setEvolutionParents(VariabilityCommit[] evolutionParents) {
        this.evolutionParents = evolutionParents;
    }

    /**
     * Each commit in the variability repo was responsible for processing one commit from the SPL repo. This method
     * returns the commits from the variability repo that processed the parent commits in the SPL repo of the SPL commit
     * that was processed by this commit.
     * <p>
     * Note that these are NOT the parents of the commit in the variability repository.
     *
     * @return Commits that processed the parent commits in the SPL history
     */
    public VariabilityCommit[] getEvolutionParents() {
        return evolutionParents;
    }

    /**
     * Get the SPL commit that was processed by this commit from the variability repo
     */
    public SPLCommit splCommit() {
        return origin;
    }
}

