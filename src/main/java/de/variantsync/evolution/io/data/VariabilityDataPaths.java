package de.variantsync.evolution.io.data;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

public class VariabilityDataPaths {
    private final Path kernelHavenLog;
    private final Path featureModel;
    private final Path presenceConditions;

    /**
     * Constructor for the data of commits for which no data could be extracted. Only a log file exists.
     * @param kernelHavenLog The path to the log file of KernelHaven
     */
    public VariabilityDataPaths(Path kernelHavenLog, @Nullable Path featureModel, @Nullable Path presenceConditions) {
        this.kernelHavenLog = kernelHavenLog;
        this.featureModel = featureModel;
        this.presenceConditions = presenceConditions;
    }

    public Path pathToKernelHavenLog() {
        return kernelHavenLog;
    }

    public Optional<Path> pathToFeatureModel() {
        if (featureModel == null) {
            return Optional.empty();
        } else {
            return Optional.of(featureModel);
        }
    }

    public Optional<Path> pathToPresenceConditions() {
        if (presenceConditions == null) {
            return Optional.empty();
        } else {
            return Optional.of(presenceConditions);
        }
    }
}
