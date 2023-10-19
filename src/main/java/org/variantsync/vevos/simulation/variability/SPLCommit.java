package org.variantsync.vevos.simulation.variability;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import net.lingala.zip4j.ZipFile;
import org.variantsync.functjonal.CachedValue;
import org.variantsync.functjonal.Functjonal;
import org.variantsync.functjonal.Lazy;
import org.variantsync.functjonal.functions.FragileFunction;
import org.variantsync.vevos.simulation.io.Resources;
import org.variantsync.vevos.simulation.io.data.CSV;
import org.variantsync.vevos.simulation.repository.Commit;
import org.tinylog.Logger;
import org.variantsync.vevos.simulation.util.io.TypedPath;
import org.variantsync.vevos.simulation.variability.pc.Artefact;
import org.variantsync.vevos.simulation.variability.pc.EFilterOutcome;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.CodeMatching;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SPLCommit extends Commit implements CachedValue {
    private final Lazy<Optional<String>> kernelHavenLog;
    private final Lazy<Optional<IFeatureModel>> featureModel;
    private final Lazy<Optional<Artefact>> presenceConditionsBefore;
    private final Lazy<Optional<Artefact>> presenceConditionsAfter;
    private final Lazy<Optional<Artefact>> presenceConditionsFallback;
    private final Lazy<Optional<CodeMatching>> codeMatching;
    private final Lazy<Optional<String>> message;
    private final Lazy<Optional<Map<EFilterOutcome, Integer>>> filterCounts;
    private final Path dataDir;
    private final Path kernelHavenLogPath;
    private final Path featureModelPath;
    private final Path presenceConditionsBeforePath;
    private final Path presenceConditionsAfterPath;
    private final Path presenceConditionsFallbackPath;
    private final Path codeMatchingBeforePath;
    private final Path codeMatchingAfterPath;
    private final Path commitMessagePath;
    private final Path filterCountsPath;
    private SPLCommit[] parents;

    /**
     * Constructor for commits that should only contain information about the commit id. TODO:
     * Document params.
     *
     * @param commitId The id of the commit
     */
    public SPLCommit(final String commitId) {
        this(commitId, null, null, null, null, null, null, null, null, null, null);
    }

    public SPLCommit(final String commitId, final Path dataDir,
                    final KernelHavenLogPath kernelHavenLog, final FeatureModelPath featureModel,
                    final PresenceConditionPath presenceConditionsBefore,
                    final PresenceConditionPath presenceConditionsAfter,
                    final PresenceConditionPath presenceConditionsFallback,
                    final CodeMatchingPath codeMatchingBefore,
                    final CodeMatchingPath codeMatchingAfter,
                    final CommitMessagePath commitMessage, final FilterCountsPath filterCounts) {
        super(commitId);
        this.dataDir = dataDir;

        this.kernelHavenLogPath = TypedPath.unwrapNullable(kernelHavenLog);
        this.featureModelPath = TypedPath.unwrapNullable(featureModel);
        this.presenceConditionsBeforePath = TypedPath.unwrapNullable(presenceConditionsBefore);
        this.presenceConditionsAfterPath = TypedPath.unwrapNullable(presenceConditionsAfter);
        this.presenceConditionsFallbackPath = TypedPath.unwrapNullable(presenceConditionsFallback);
        this.codeMatchingBeforePath = TypedPath.unwrapNullable(codeMatchingBefore);
        this.codeMatchingAfterPath = TypedPath.unwrapNullable(codeMatchingAfter);
        this.commitMessagePath = TypedPath.unwrapNullable(commitMessage);
        this.filterCountsPath = TypedPath.unwrapNullable(filterCounts);

        final FragileFunction<Path, Path, IOException> tryUnzip = SPLCommit::tryUnzip;

        // Lazy loading of log file
        this.kernelHavenLog = Functjonal.mapFragileLazily(kernelHavenLogPath,
                        tryUnzip.andThen(Files::readString),
                        () -> "Was not able to load KernelHaven log for commit " + commitId);
        // Lazy loading of feature model
        this.featureModel = Functjonal.mapFragileLazily(featureModelPath, tryUnzip
                        .andThen(path -> Resources.Instance().load(IFeatureModel.class, path)),
                        () -> "Was not able to load feature model for id " + commitId);
        // Lazy loading of presence conditions
        this.presenceConditionsBefore = Functjonal.mapFragileLazily(presenceConditionsBeforePath,
                        tryUnzip.andThen(path -> Resources.Instance().load(Artefact.class, path)),
                        () -> "Was not able to load 'before' presence conditions for id "
                                        + commitId);
        this.presenceConditionsAfter = Functjonal.mapFragileLazily(presenceConditionsAfterPath,
                        tryUnzip.andThen(path -> Resources.Instance().load(Artefact.class, path)),
                        () -> "Was not able to load 'after' presence conditions for id "
                                        + commitId);
        this.presenceConditionsFallback = Functjonal.mapFragileLazily(
                        presenceConditionsFallbackPath,
                        tryUnzip.andThen(path -> Resources.Instance().load(Artefact.class, path)),
                        () -> "Was not able to load fallback presence conditions for id "
                                        + commitId);
        this.codeMatching = tryToLoadMatching();
        // Lazy loading of commit message
        this.message = Functjonal.mapFragileLazily(commitMessagePath,
                        tryUnzip.andThen(Files::readString),
                        () -> "Was not able to load commit message for id " + commitId);
        // Lazy loading of filter counts
        this.filterCounts = Functjonal.mapFragileLazily(filterCountsPath, tryUnzip.andThen(path -> {
            final Map<EFilterOutcome, Integer> countsMap = new HashMap<>();
            Files.readAllLines(path).stream().map(l -> l.split(":"))
                            .forEach(parts -> countsMap.put(EFilterOutcome.valueOf(parts[0]),
                                            Integer.parseInt(parts[1].trim())));
            return countsMap;
        }), () -> "Was not able to load filter counts for id " + commitId);
    }

    private Lazy<Optional<CodeMatching>> tryToLoadMatching() {
        Lazy<Optional<CSV>> matchingBefore = Functjonal.mapFragileLazily(codeMatchingBeforePath,
                path -> Resources.Instance().load(CSV.class, path),
                () -> "Was not able to load 'after' presence conditions for id "
                        + this.id());
        Lazy<Optional<CSV>> matchingAfter = Functjonal.mapFragileLazily(codeMatchingAfterPath,
                path -> Resources.Instance().load(CSV.class, path),
                () -> "Was not able to load 'after' presence conditions for id "
                        + this.id());
        return CodeMatching.lazyFromCSVs(matchingBefore, matchingAfter);
    }

    private static Path tryUnzip(final Path path) throws IOException {
        if (!Files.exists(path)) {
            final Path zippedParent = Path.of(path.getParent() + ".zip");
            Logger.debug("Checking whether there is an archive " + zippedParent);
            if (Files.exists(zippedParent)) {
                Logger.debug("Archive " + zippedParent.getFileName() + " found.");
                try (var zip = new ZipFile(zippedParent.toFile())) {
                    zip.extractAll(String.valueOf(zippedParent.getParent()));
                } catch (final IOException e) {
                    Logger.error("Was not able to unzip " + zippedParent, e);
                    throw e;
                }
            } else {
                Logger.warn("Path " + path + " does not exist and no ZIP file found.");
                return null;
            }
        } else {
            Logger.debug("Path " + path + " exists. No unzip required.");
        }
        return path;
    }

    /**
     * Clears all cached values by calling {@link Lazy#forget()} on all fields.
     */
    @Override
    public void forget() {
        kernelHavenLog.forget();
        featureModel.forget();
        presenceConditionsBefore.forget();
        presenceConditionsAfter.forget();
        presenceConditionsFallback.forget();
        message.forget();
        filterCounts.forget();
    }

    /**
     * Return the parents of this commit. As the dataset only contains the data about non-error
     * commits, not all <code>SPLCommit</code> objects are associated with their parents. Therefore,
     * an <code>Optional</code> is returned.
     *
     * @return An <code>Optional</code> containing the <code>SPLCommit</code> objects of the parent
     *         commits, if there are any.
     */
    public Optional<SPLCommit[]> parents() {
        return Optional.ofNullable(parents);
    }

    public void setParents(final SPLCommit... parents) {
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
     * @return A Lazy that loads the before change presence conditions associated with this commit.
     */
    public Lazy<Optional<Artefact>> presenceConditionsBefore() {
        return presenceConditionsBefore;
    }

    /**
     * @return A Lazy that loads the after change presence conditions associated with this commit.
     */
    public Lazy<Optional<Artefact>> presenceConditionsAfter() {
        return presenceConditionsAfter;
    }

    /**
     * @return A Lazy that loads the fallback presence conditions associated with this commit.
     */
    public Lazy<Optional<Artefact>> presenceConditionsFallback() {
        return presenceConditionsFallback;
    }

    /**
     * @return A Lazy that loads the filter counts associated with this commit.
     */
    public Lazy<Optional<Map<EFilterOutcome, Integer>>> filterCounts() {
        return filterCounts;
    }

    public Path getCommitDataDirectory() {
        return dataDir;
    }

    public Path getKernelHavenLogPath() {
        return kernelHavenLogPath;
    }

    public Path getFeatureModelPath() {
        return featureModelPath;
    }

    public Path getPresenceConditionsBeforePath() {
        return presenceConditionsBeforePath;
    }

    public Path getPresenceConditionsAfterPath() {
        return presenceConditionsAfterPath;
    }

    public Path getPresenceConditionsFallbackPath() {
        return presenceConditionsFallbackPath;
    }

    public Path getCommitMessagePath() {
        return commitMessagePath;
    }

    public Path getFilterCountsPath() {
        return filterCountsPath;
    }

    public record KernelHavenLogPath(Path path) implements TypedPath {

    }

    public record FeatureModelPath(Path path) implements TypedPath {

    }

    public record PresenceConditionPath(Path path) implements TypedPath {

    }

    public record CodeMatchingPath(Path path) implements TypedPath {

    }

    public record CommitMessagePath(Path path) implements TypedPath {

    }

    public record FilterCountsPath(Path path) implements TypedPath {

    }
}

