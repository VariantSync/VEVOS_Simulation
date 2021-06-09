package de.variantsync.evolution.variability;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.repository.ISPLRepository;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.NotImplementedException;
import de.variantsync.evolution.util.functional.Lazy;
import de.variantsync.evolution.variability.pc.FeatureTrace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SPLCommit extends Commit<ISPLRepository> {
    private SPLCommit[] parents;
    private final String message;
    private final Lazy<String> kernelHavenLog;
    private final Lazy<IFeatureModel> featureModel;
    private final Lazy<FeatureTrace> presenceConditions;

    public SPLCommit(String commitId, VariabilityFilePaths variabilityFilePaths) {
        // TODO: Implement Issue #13 here.
        this(commitId, variabilityFilePaths, "");
    }

    public SPLCommit(String commitId, VariabilityFilePaths variabilityFilePaths, String message) {
        super(commitId);
        this.message = message;
        this.kernelHavenLog = Lazy.of(() -> {
            try {
                return Files.readString(variabilityFilePaths.pathToKernelHavenLog());
            } catch (IOException e) {
                Logger.exception("Was not able to load KernelHaven log for commit " + commitId, e);
                return null;
            }
        });
        this.featureModel = Lazy.of(() -> {
            Path fmPath = variabilityFilePaths.pathToFeatureModel().orElseThrow();
            // TODO: Implement Issue #3 here: Parse FM from fmPath. Use Resource.Instance() for that.
            throw new NotImplementedException();
        });
        this.presenceConditions = Lazy.of(() -> {
            try {
                return Resources.Instance().load(FeatureTrace.class, variabilityFilePaths.pathToPresenceConditions().orElseThrow());
            } catch (Resources.ResourceLoadingFailure resourceLoadingFailure) {
                Logger.exception("", resourceLoadingFailure);
                return null;
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

    public String message() {
        return message;
    }

    public Lazy<String> kernelHavenLog() {
        return kernelHavenLog;
    }

    public Lazy<IFeatureModel> featureModel() {
        return featureModel;
    }

    public Lazy<FeatureTrace> presenceConditions() {
        return presenceConditions;
    }
}
