package de.variantsync.evolution.variability;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class VariabilityFilePaths {
    private final Path kernelHavenLog;
    private final Path featureModel;
    private final Path presenceConditions;
    private final Path message;

    /**
     * Constructor for the data of commits for which no data could be extracted. Only a log file exists.
     * @param kernelHavenLog The path to the log file of KernelHaven
     */
    public VariabilityFilePaths(Path kernelHavenLog, @Nullable Path featureModel, @Nullable Path presenceConditions, @Nullable Path message) {
        this.kernelHavenLog = Objects.requireNonNull(kernelHavenLog);
        this.featureModel = featureModel;
        this.presenceConditions = presenceConditions;
        this.message = message;
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

    public Optional<Path> pathToMessage() {
        if (message == null) {
            return Optional.empty();
        } else {
            return Optional.of(message);
        }
    }
}
