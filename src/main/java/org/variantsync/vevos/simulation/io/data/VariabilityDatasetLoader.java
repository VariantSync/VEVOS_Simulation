package org.variantsync.vevos.simulation.io.data;

import net.lingala.zip4j.ZipFile;
import org.variantsync.functjonal.Result;
import org.variantsync.vevos.simulation.io.ResourceLoader;
import org.variantsync.vevos.simulation.io.TextIO;
import org.tinylog.Logger;
import org.variantsync.vevos.simulation.variability.SPLCommit;
import org.variantsync.vevos.simulation.variability.VariabilityDataset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

;

public class VariabilityDatasetLoader implements ResourceLoader<VariabilityDataset> {
    private final static String SUCCESS_COMMITS_FILE = "SUCCESS_COMMITS.txt";
    private final static String ERROR_COMMITS_FILE = "ERROR_COMMITS.txt";
    private final static String EMPTY_COMMITS_FILE = "EMPTY_COMMITS.txt";
    private final static String PARTIAL_SUCCESS_COMMITS_FILE = "PARTIAL_SUCCESS_COMMITS.txt";
    private final static String FEATURE_MODEL_FILE = "variability-model.json";
    private final static String PRESENCE_CONDITIONS_BEFORE_FILE = "code-variability.before.spl.csv";
    private final static String PRESENCE_CONDITIONS_AFTER_FILE = "code-variability.after.spl.csv";
    private final static String PRESENCE_CONDITIONS_FALLBACK_FILE = "code-variability.spl.csv";
    private final static String MATCHING_BEFORE_FILE = "code-matching.before.spl.csv";
    private final static String MATCHING_AFTER_FILE = "code-matching.after.spl.csv";
    private final static String PARENTS_FILE = "PARENTS.txt";
    private final static String MESSAGE_FILE = "MESSAGE.txt";
    private final static String VARIABLES_FILE = "VARIABLES.txt";
    private static final String DATA_DIR_NAME = "data";
    private static final String LOG_DIR_NAME = "log";
    private static final String FILTER_COUNTS_FILE = "FILTERED.txt";

    @Override
    public boolean canLoad(final Path p) {
        try {
            return Files.list(p).map(Path::toFile).anyMatch(f -> {
                final String name = f.getName();
                return name.equals(SUCCESS_COMMITS_FILE) || name.equals(ERROR_COMMITS_FILE) || name.equals(EMPTY_COMMITS_FILE)
                                || name.equals(PARTIAL_SUCCESS_COMMITS_FILE);
            });
        } catch (final IOException e) {
            Logger.error("Was not able to check the file(s) under " + p, e);
            return false;
        }
    }

    /**
     * Load a dataset containing the extracted variability information of a SPL.
     * <p>
     * The given path should point to the root of the dataset's directory. Assume that the given
     * path to the dataset is `/home/alice/data/extraction-results`. Then, the structure of
     * `extraction-results` should look as follows:
     * <p>
     * </p>
     * extraction-results/
     * <p>
     * |- log/
     * </p>
     * <p>
     * |- data/
     * </p>
     * <p>
     * |- SUCCESS_COMMITS.txt
     * </p>
     *
     * @param p path to the root directory of the dataset
     * @return The fully-loaded dataset if loading is successful, otherwise an Exception.
     */
    @Override
    public Result<VariabilityDataset, Exception> load(final Path p) {
        // Read the metadata
        List<String> successCommitIds = new ArrayList<>();
        List<String> errorCommitIds = new ArrayList<>();
        List<String> emptyCommitIds = new ArrayList<>();
        List<String> partialSuccessCommitIds = new ArrayList<>();

        Logger.info("Started loading of dataset under " + p);
        final Path successCommitFile = p.resolve(SUCCESS_COMMITS_FILE);
        if (Files.exists(successCommitFile)) {
            successCommitIds = TextIO.readLinesTrimmed(successCommitFile)
                            .expect("Success-commit file exists but could not be loaded.");
        }

        final Path errorCommitFile = p.resolve(ERROR_COMMITS_FILE);
        if (Files.exists(errorCommitFile)) {
            errorCommitIds = TextIO.readLinesTrimmed(errorCommitFile)
                            .expect("Error-commit file exists but could not be loaded.");
        }

        final Path emptyFile = p.resolve(EMPTY_COMMITS_FILE);
        if (Files.exists(emptyFile)) {
            emptyCommitIds = TextIO.readLinesTrimmed(emptyFile)
                    .expect("Success-commit file exists but could not be loaded.");
        }

        final Path partialSuccessCommitFile = p.resolve(PARTIAL_SUCCESS_COMMITS_FILE);
        if (Files.exists(partialSuccessCommitFile)) {
            partialSuccessCommitIds = TextIO.readLinesTrimmed(partialSuccessCommitFile)
                            .expect("Partial-success-commit file exists but could not be loaded.");
        }

        Logger.info("Read commit ids.");

        // Create SPLCommit objects for each commit
        final List<SPLCommit> successCommits = initializeSPLCommits(p, successCommitIds);
        final List<SPLCommit> errorCommits = initializeSPLCommits(p, errorCommitIds);
        final List<SPLCommit> emptyCommits = initializeSPLCommits(p, emptyCommitIds);
        final List<SPLCommit> partialSuccessCommits = initializeSPLCommits(p, partialSuccessCommitIds);
        Logger.info("Initialized SPL commits.");

        // Retrieve the SPLCommit objects for the parents of each commit
        final Map<String, SPLCommit> idToCommitMap = new HashMap<>();
        successCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        Logger.info("Mapped success commits.");
        errorCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        Logger.info("Mapped error commits.");
        emptyCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        Logger.info("Mapped empty commits.");
        partialSuccessCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        Logger.info("Mapped partial success commits.");

        Logger.info("Mapping commits to parents...");
        for (final Map.Entry<String, SPLCommit> entry : idToCommitMap.entrySet()) {
            final String[] parentIds = loadParentIds(p, entry.getKey());
            if (parentIds == null || parentIds.length == 0) {
                entry.getValue().setParents(null);
            } else {
                entry.getValue().setParents(Arrays.stream(parentIds).map(id -> {
                    var commit = idToCommitMap.get(id);
                    if (commit == null) {
                        commit = new SPLCommit(id);
                    }
                    return commit;
                }).toArray(SPLCommit[]::new));
            }
        }
        Logger.info("Done.");
        Logger.info("Found a total of " + idToCommitMap.size() + " commits.");
        // Return the fully-loaded dataset
        return Result.Success(new VariabilityDataset(successCommits, errorCommits, emptyCommits,
                        partialSuccessCommits));
    }

    private List<SPLCommit> initializeSPLCommits(final Path p, final List<String> commitIds) {
        final List<SPLCommit> splCommits = new ArrayList<>(commitIds.size());
        for (final String id : commitIds) {
            // Initialize a SPLCommit object for each commit id by resolving all paths to files with
            // data about the commit
            final SPLCommit splCommit = new SPLCommit(id, resolvePathToCommitOutputDir(p, id),
                            resolvePathToLogFile(p, id), resolvePathToFeatureModel(p, id),
                            resolvePathToPresenceConditionsBefore(p, id),
                            resolvePathToPresenceConditionsAfter(p, id),
                            resolvePathToPresenceConditionsFallback(p, id),
                            resolvePathToMatchingBefore(p, id),
                            resolvePathToMatchingAfter(p, id),
                            resolvePathToMessageFile(p, id), resolvePathToFilterCountsFile(p, id));
            splCommits.add(splCommit);
        }
        return splCommits;
    }

    private SPLCommit.CodeMatchingPath resolvePathToMatchingBefore(final Path rootDir, String commitId) {
        final Path p = resolvePathToCommitOutputDir(rootDir, commitId)
                .resolve(MATCHING_BEFORE_FILE);
        return new SPLCommit.CodeMatchingPath(p);
    }

    private SPLCommit.CodeMatchingPath resolvePathToMatchingAfter(final Path rootDir, String commitId) {
        final Path p = resolvePathToCommitOutputDir(rootDir, commitId)
                .resolve(MATCHING_AFTER_FILE);
        return new SPLCommit.CodeMatchingPath(p);
    }

    private Path resolvePathToCommitOutputDir(final Path rootDir, final String commitId) {
        return rootDir.resolve(DATA_DIR_NAME).resolve(commitId);
    }

    private SPLCommit.FeatureModelPath resolvePathToFeatureModel(final Path rootDir,
                    final String commitId) {
        Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(FEATURE_MODEL_FILE);
        if (!Files.exists(p)) {
            // If no feature model is found, we instead set the variables file, as feature model
            // TODO: Move this logic to VEVOS_extraction, if we convert a feature model a FeatureIDE
            // format?
            p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(VARIABLES_FILE);
        }
        return new SPLCommit.FeatureModelPath(p);
    }

    private SPLCommit.PresenceConditionPath resolvePathToPresenceConditionsBefore(
                    final Path rootDir, final String commitId) {
        final Path p = resolvePathToCommitOutputDir(rootDir, commitId)
                        .resolve(PRESENCE_CONDITIONS_BEFORE_FILE);
        return new SPLCommit.PresenceConditionPath(p);
    }

    private SPLCommit.PresenceConditionPath resolvePathToPresenceConditionsAfter(final Path rootDir,
                    final String commitId) {
        final Path p = resolvePathToCommitOutputDir(rootDir, commitId)
                        .resolve(PRESENCE_CONDITIONS_AFTER_FILE);
        return new SPLCommit.PresenceConditionPath(p);
    }

    private SPLCommit.PresenceConditionPath resolvePathToPresenceConditionsFallback(
                    final Path rootDir, final String commitId) {
        // For the fallback file, we first try the 'after' version of the PCS. If it does not exist,
        // we use the fallback
        Path p = resolvePathToCommitOutputDir(rootDir, commitId)
                        .resolve(PRESENCE_CONDITIONS_AFTER_FILE);
        if (!Files.exists(p)) {
            p = resolvePathToCommitOutputDir(rootDir, commitId)
                            .resolve(PRESENCE_CONDITIONS_FALLBACK_FILE);
        }
        return new SPLCommit.PresenceConditionPath(p);
    }

    private Path resolvePathToParentsFile(final Path rootDir, final String commitId) {
        return resolvePathToCommitOutputDir(rootDir, commitId).resolve(PARENTS_FILE);
    }

    private SPLCommit.CommitMessagePath resolvePathToMessageFile(final Path rootDir,
                    final String commitId) {
        final Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(MESSAGE_FILE);
        return new SPLCommit.CommitMessagePath(p);
    }

    private SPLCommit.KernelHavenLogPath resolvePathToLogFile(final Path rootDir,
                    final String commitId) {
        final Path p = rootDir.resolve(LOG_DIR_NAME).resolve(commitId + ".log");
        return new SPLCommit.KernelHavenLogPath(p);
    }

    private SPLCommit.FilterCountsPath resolvePathToFilterCountsFile(final Path rootDir,
                    final String commitId) {
        final Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(FILTER_COUNTS_FILE);
        return new SPLCommit.FilterCountsPath(p);
    }

    private String[] loadParentIds(final Path p, final String commitId) {
        final Path parentsFile = resolvePathToParentsFile(p, commitId);
        if (!Files.exists(parentsFile)) {
            Logger.debug("No PARENTS.txt found, checking archive....");
            final File zipFile = new File(parentsFile.getParent() + ".zip");
            Logger.debug("Checking ZIP file " + zipFile);
            if (zipFile.exists()) {
                try (var zip = new ZipFile(zipFile)) {
                    Logger.debug("Unzipping PARENTS.txt");
                    zip.extractFile(commitId + "/PARENTS.txt", String.valueOf(
                                    resolvePathToCommitOutputDir(p, commitId).getParent()));
                } catch (final IOException e) {
                    // Not all commits have a ZIP file and not all commits with a ZIP file have a
                    // PARENTS.txt. So this is
                    // an expected exception
                    Logger.debug("Was not able to unzip commit data." + e.getMessage());
                }
            }
        }
        if (Files.exists(parentsFile)) {
            try {
                return Files.readString(parentsFile).split("\\s");
            } catch (final IOException e) {
                Logger.error("Was not able to load PARENTS.txt " + parentsFile
                                + " even though it exists:", e);
                return null;
            }
        }
        return null;
    }
}

