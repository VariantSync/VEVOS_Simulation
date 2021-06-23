package de.variantsync.evolution.variability;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.repository.AbstractVariabilityRepository;
import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.util.functional.Lazy;
import de.variantsync.evolution.variability.pc.Artefact;
import org.eclipse.jgit.api.errors.GitAPIException;
import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.util.Logger;


import java.io.IOException;
import java.nio.file.Path;

public class VariabilityCommit extends Commit<AbstractVariabilityRepository> {
    private AbstractVariabilityRepository sourceRepo;
    private final SPLCommit origin;
    private VariabilityCommit[] evolutionParents;

    public VariabilityCommit(AbstractVariabilityRepository source, String commitId, SPLCommit splCommit) {
        super(commitId);
        this.sourceRepo = source;
        origin = splCommit;
    }

    public final Lazy<IFeatureModel> featureModel = Lazy.of(() -> {
        IFeatureModel fm = null;

        try {
            sourceRepo.checkoutCommit(this);
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException("Failed commit checkout.");
        }

        Path fmPath = sourceRepo.getFeatureModelFile();
        // TODO: Implement Issue #3 here: Parse FM from fmPath. Use Resource.Instance() for that.
        return fm;
    });

    public final Lazy<Artefact> presenceConditions = Lazy.of(() -> {
        try {
            sourceRepo.checkoutCommit(this);
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException("Failed commit checkout.");
        }

        try {
            return Resources.Instance().load(Artefact.class, sourceRepo.getVariabilityFile());
        } catch (Resources.ResourceLoadingFailure resourceLoadingFailure) {
            Logger.exception("", resourceLoadingFailure);
            return null;
        }
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

