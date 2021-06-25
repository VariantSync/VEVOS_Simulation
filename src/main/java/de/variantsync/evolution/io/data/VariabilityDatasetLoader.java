package de.variantsync.evolution.io.data;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.SPLCommit;
import de.variantsync.evolution.variability.SPLCommit.KernelHavenLogPath;
import de.variantsync.evolution.variability.SPLCommit.FeatureModelPath;
import de.variantsync.evolution.variability.SPLCommit.PresenceConditionPath;
import de.variantsync.evolution.variability.SPLCommit.CommitMessagePath;
import de.variantsync.evolution.variability.VariabilityDataset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class VariabilityDatasetLoader implements ResourceLoader<VariabilityDataset> {
    private final static String SUCCESS_COMMIT_FILE = "SUCCESS_COMMITS.txt";
    private final static String ERROR_COMMIT_FILE = "ERROR_COMMITS.txt";
    private final static String PARTIAL_SUCCESS_COMMIT_FILE = "PARTIAL_SUCCESS_COMMITS.txt";
    private final static String FEATURE_MODEL_FILE = "variability-model.json";
    private final static String PRESENCE_CONDITIONS_FILE = "code-variability.csv";
    private final static String PARENTS_FILE = "PARENTS.txt";
    private final static String MESSAGE_FILE = "MESSAGE.txt";
    private static final String DATA_DIR_NAME = "data";
    private static final String LOG_DIR_NAME = "log";


    /**
     * @param p The path which should be checked.
     * @return true if the path points to a directory that contains at least one of the required metadata files, otherwise false.
     */
    @Override
    public boolean canLoad(Path p) {
        try {
            return Files.list(p)
                    .map(Path::toFile)
                    .anyMatch(f -> {
                        String name = f.getName();
                        return name.equals(SUCCESS_COMMIT_FILE) || name.equals(ERROR_COMMIT_FILE) || name.equals(PARTIAL_SUCCESS_COMMIT_FILE);
                    });
        } catch (IOException e) {
            Logger.exception("Was not able to check the file(s) under " + p, e);
            return false;
        }
    }

    /**
     * Load a dataset containing the extracted variability information of a SPL.
     * <p>
     * The given path should point to the root of the dataset's directory. Assume that the given path to the dataset is
     * `/home/alice/data/extraction-results`. Then, the structure of `extraction-results` should look as follows:
     * <p></p>
     * extraction-results/
     * <p>|- log/</p>
     * <p>|- data/</p>
     * <p>|- SUCCESS_COMMITS.txt</p>
     *
     * @param p path to the root directory of the dataset
     * @return The fully-loaded dataset if loading is successful, otherwise an Exception.
     */
    @Override
    public Result<VariabilityDataset, Exception> load(Path p) {
        // Read the metadata
        List<String> successIds = readLines(p, SUCCESS_COMMIT_FILE);
        List<String> errorIds = readLines(p, ERROR_COMMIT_FILE);
        List<String> partialSuccessIds = readLines(p, PARTIAL_SUCCESS_COMMIT_FILE);

        // Create SPLCommit objects for each commit
        List<SPLCommit> successCommits = initializeSPLCommits(p, successIds);
        List<SPLCommit> errorCommits = initializeSPLCommits(p, errorIds);
        List<SPLCommit> partialSuccessCommits = initializeSPLCommits(p, partialSuccessIds);

        // Retrieve the SPLCommit objects for the parents of each commit
        Map<String, SPLCommit> idToCommitMap = new HashMap<>();
        successCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        errorCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        partialSuccessCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        for (Map.Entry<String, SPLCommit> entry : idToCommitMap.entrySet()) {
            String[] parentIds = loadParentIds(p, entry.getKey());
            if (parentIds == null || parentIds.length == 0) {
                entry.getValue().setParents(null);
            } else {
                entry.getValue().setParents(Arrays.stream(parentIds).map(idToCommitMap::get).toArray(SPLCommit[]::new));
            }
        }

        // Return the fully-loaded dataset
        return Result.Success(new VariabilityDataset(successCommits, errorCommits, partialSuccessCommits));
    }

    private List<SPLCommit> initializeSPLCommits(Path p, List<String> commitIds) {
        List<SPLCommit> splCommits = new ArrayList<>(commitIds.size());
        for (String id : commitIds) {
            // Initialize a SPLCommit object for each commit id by resolving all paths to files with data about the commit
            SPLCommit splCommit = new SPLCommit(id, resolvePathToLogFile(p, id), resolvePathToFeatureModel(p, id), resolvePathToPresenceConditions(p, id), resolvePathToMessageFile(p, id));
            splCommits.add(splCommit);
        }
        return splCommits;
    }

    private Path resolvePathToCommitOutputDir(Path rootDir, String commitId) {
        return rootDir.resolve(DATA_DIR_NAME).resolve(commitId);
    }

    private FeatureModelPath resolvePathToFeatureModel(Path rootDir, String commitId) {
        Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(FEATURE_MODEL_FILE);
        return p.toFile().exists() ? new FeatureModelPath(p) : null;
    }

    private PresenceConditionPath resolvePathToPresenceConditions(Path rootDir, String commitId) {
        Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(PRESENCE_CONDITIONS_FILE);
        return p.toFile().exists() ? new PresenceConditionPath(p) : null;
    }

    private Path resolvePathToParentsFile(Path rootDir, String commitId) {
        Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(PARENTS_FILE);
        return p.toFile().exists() ? p : null;
    }

    private CommitMessagePath resolvePathToMessageFile(Path rootDir, String commitId) {
        Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(MESSAGE_FILE);
        return p.toFile().exists() ? new CommitMessagePath(p) : null;
    }

    private KernelHavenLogPath resolvePathToLogFile(Path rootDir, String commitId) {
        Path p = rootDir.resolve(LOG_DIR_NAME).resolve(commitId + ".log");
        return p.toFile().exists() ? new KernelHavenLogPath(p) : null;
    }

    private List<String> readLines(Path p, String fileName) {
        try {
            return Files.readAllLines(p.resolve(fileName)).stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        } catch (IOException e) {
            Logger.debug("Was not able to read file " + p.resolve(fileName));
            return Collections.emptyList();
        }
    }

    private String[] loadParentIds(Path p, String commitId) {
        Path parentsFile = resolvePathToParentsFile(p, commitId);
        if (parentsFile != null && Files.exists(parentsFile)) {
            try {
                return Files.readString(parentsFile).split("\\s");
            } catch (IOException e) {
                Logger.exception("Was not able to load PARENTS.txt " + parentsFile + " even though it exists:", e);
                return null;
            }
        } else {
            return null;
        }
    }
}
