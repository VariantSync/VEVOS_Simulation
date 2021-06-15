package de.variantsync.evolution.variability;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.impl.FeatureModel;
import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.repository.ISPLRepository;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Lazy;
import de.variantsync.evolution.variability.pc.Artefact;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SPLCommit extends Commit<ISPLRepository> {
    private SPLCommit[] parents;
    private final Lazy<Optional<String>> kernelHavenLog;
    private final Lazy<Optional<IFeatureModel>> featureModel;
    private final Lazy<Optional<Artefact>> presenceConditions;
    private final Lazy<Optional<String>> message;

    public SPLCommit(String commitId, KernelHavenLogPath kernelHavenLog, FeatureModelPath featureModel, PresenceConditionPath presenceConditions,  CommitMessagePath message) {
        super(commitId);
        this.kernelHavenLog = Lazy.of(() -> {
            try {
                if (kernelHavenLog != null) {
                    return Optional.of(Files.readString(kernelHavenLog.path));
                } else {
                    return Optional.empty();
                }
            } catch (IOException e) {
                Logger.exception("Was not able to load KernelHaven log for commit " + commitId, e);
                return Optional.empty();
            }
        });
        this.featureModel = Lazy.of(() -> {
            try {
                if (featureModel != null) {
                    return Optional.of(Resources.Instance().load(FeatureModel.class, featureModel.path));
                } else {
                    return Optional.empty();
                }
            } catch (Resources.ResourceLoadingFailure resourceLoadingFailure) {
                Logger.exception("Was not able to load feature model for id " + commitId, resourceLoadingFailure);
                return Optional.empty();
            }
        });
        this.presenceConditions = Lazy.of(() -> {
            try {
                if (presenceConditions != null) {
                    return Optional.of(Resources.Instance().load(Artefact.class, presenceConditions.path));
                } else {
                    return Optional.empty();
                }
            } catch (Resources.ResourceLoadingFailure resourceLoadingFailure) {
                Logger.exception("Was not able to load presence conditions for id " + commitId, resourceLoadingFailure);
                return Optional.empty();
            }
        });
        this.message = Lazy.of(() -> {
            try {
                if (message != null) {
                    return Optional.of(Files.readString(message.path));
                } else {
                    return Optional.empty();
                }
            } catch (IOException e) {
                Logger.exception("Was not able to load commit message for id " + commitId, e);
                return Optional.empty();
            }
        });
    }

    public Optional<SPLCommit[]> parents() {
        if (parents == null) {
            return Optional.empty();
        } else {
            return Optional.of(parents);
        }
    }

    public void setParents(SPLCommit[] parents) {
        this.parents = parents;
    }

    public Lazy<Optional<String>> message() {
        return message;
    }

    public Lazy<Optional<String>> kernelHavenLog() {
        return kernelHavenLog;
    }

    public Lazy<Optional<IFeatureModel>> featureModel() {
        return featureModel;
    }

    public Lazy<Optional<Artefact>> presenceConditions() {
        return presenceConditions;
    }

    public record KernelHavenLogPath(Path path) {}
    public record FeatureModelPath(Path path) {}
    public record PresenceConditionPath(Path path) {}
    public record CommitMessagePath(Path path) {}
}
