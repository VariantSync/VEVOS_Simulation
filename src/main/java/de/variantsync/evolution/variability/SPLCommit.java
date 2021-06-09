package de.variantsync.evolution.variability;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.io.data.VariabilityDataPaths;
import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.repository.ISPLRepository;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.NotImplementedException;
import de.variantsync.evolution.util.functional.Lazy;
import de.variantsync.evolution.variability.pc.FeatureTrace;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SPLCommit extends Commit<ISPLRepository> {
    private SPLCommit[] parents;
    private final String message;
    private final Lazy<String> kernelHavenLog;
    private final Lazy<IFeatureModel> featureModel;
    private final Lazy<FeatureTrace> presenceConditions;

    public SPLCommit(String commitId, VariabilityDataPaths variabilityDataPaths) {
        // TODO: Implement Issue #13 here.
        this(commitId, variabilityDataPaths, "");
    }

    public SPLCommit(String commitId, VariabilityDataPaths variabilityDataPaths, String message) {
        super(commitId);
        this.message = message;
        this.kernelHavenLog = Lazy.of(() -> {
            try {
                return Files.readString(variabilityDataPaths.pathToKernelHavenLog());
            } catch (IOException e) {
                Logger.exception("Was not able to load KernelHaven log for commit " + commitId, e);
                return null;
            }
        });
        this.featureModel = Lazy.of(() -> {
            Path fmPath = variabilityDataPaths.pathToFeatureModel().orElseThrow();
            // TODO: Implement Issue #3 here: Parse FM from fmPath. Use Resource.Instance() for that.
            throw new NotImplementedException();
        });
        this.presenceConditions = Lazy.of(() -> {
            try {
                return Resources.Instance().load(FeatureTrace.class, variabilityDataPaths.pathToPresenceConditions().orElseThrow());
            } catch (Resources.ResourceLoadingFailure resourceLoadingFailure) {
                Logger.exception("", resourceLoadingFailure);
                return null;
            }
        });
    }

    public SPLCommit[] parents() {
        return parents;
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
