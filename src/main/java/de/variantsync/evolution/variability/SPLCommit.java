package de.variantsync.evolution.variability;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.impl.FeatureModel;
import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.repository.AbstractSPLRepository;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Lazy;
import de.variantsync.evolution.variability.pc.Artefact;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SPLCommit extends Commit {
    private SPLCommit[] parents;
    private final Lazy<Optional<String>> kernelHavenLog;
    private final Lazy<Optional<IFeatureModel>> featureModel;
    private final Lazy<Optional<Artefact>> presenceConditions;
    private final Lazy<Optional<String>> message;

    /**
     * Constructor for commits that should only contain information about the commit id.
     *
     * @param commitId The id of the commit
     */
    public SPLCommit(String commitId) {
        this(commitId, null, null, null, null);
    }

    public SPLCommit(String commitId, KernelHavenLogPath kernelHavenLog, FeatureModelPath featureModel, PresenceConditionPath presenceConditions, CommitMessagePath message) {
        super(commitId);
        // Lazy loading of log file
        this.kernelHavenLog = Lazy.of(() -> Optional.ofNullable(kernelHavenLog).map(kernelHavenLogPath -> {
            try {
                return Files.readString(kernelHavenLogPath.path);
            } catch (IOException e) {
                Logger.exception("Was not able to load KernelHaven log for commit " + commitId, e);
                return null;
            }
        }));
        // Lazy loading of feature model
        this.featureModel = Lazy.of(() -> Optional.ofNullable(featureModel).map(featureModelPath -> {
            try {
                return Resources.Instance().load(FeatureModel.class, featureModelPath.path);
            } catch (Resources.ResourceLoadingFailure resourceLoadingFailure) {
                Logger.exception("Was not able to load feature model for id " + commitId, resourceLoadingFailure);
                return null;
            }
        }));
        // Lazy loading of presence condition
        this.presenceConditions = Lazy.of(() -> Optional.ofNullable(presenceConditions).map(presenceConditionPath -> {
            try {
                return Resources.Instance().load(Artefact.class, presenceConditionPath.path);
            } catch (Resources.ResourceLoadingFailure resourceLoadingFailure) {
                Logger.exception("Was not able to load presence conditions for id " + commitId, resourceLoadingFailure);
                return null;
            }
        }));
        // Lazy loading of commit message
        this.message = Lazy.of(() -> Optional.ofNullable(message).map(commitMessagePath -> {
            try {
                return Files.readString(commitMessagePath.path);
            } catch (IOException e) {
                Logger.exception("Was not able to load commit message for id " + commitId, e);
                return null;
            }
        }));
    }

    /**
     * Return the parents of this commit. As the dataset only contains the data about non-error commits,
     * not all <code>SPLCommit</code> objects are associated with their parents. Therefore, an <code>Optional</code> is returned.
     *
     * @return An <code>Optional</code> containing the <code>SPLCommit</code> objects of the parent commits, if there are any.
     */
    public Optional<SPLCommit[]> parents() {
        return Optional.ofNullable(parents);
    }

    public void setParents(SPLCommit[] parents) {
        this.parents = parents;
    }

    /**
     * @return A Lazy that loads the commit message of this commit.
     */
    public Lazy<Optional<String>> message() {
        return message;
    }

    /**
     * @return A Lazy that loads the KernelHaven log associated with this commit.
     */
    public Lazy<Optional<String>> kernelHavenLog() {
        return kernelHavenLog;
    }

    /**
     * @return A Lazy that loads the feature model associated with this commit.
     */
    public Lazy<Optional<IFeatureModel>> featureModel() {
        return featureModel;
    }

    /**
     * @return A Lazy that loads the presence conditions associated with this commit.
     */
    public Lazy<Optional<Artefact>> presenceConditions() {
        return presenceConditions;
    }

    public record KernelHavenLogPath(Path path) {
    }

    public record FeatureModelPath(Path path) {
    }

    public record PresenceConditionPath(Path path) {
    }

    public record CommitMessagePath(Path path) {
    }
}
