package de.variantsync.evolution.io.data;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.SPLCommit;
import de.variantsync.evolution.variability.VariabilityDataset;
import de.variantsync.evolution.variability.VariabilityFilePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VariabilityDatasetLoader implements ResourceLoader<VariabilityDataset> {
    private final static String SUCCESS_COMMIT_FILE = "SUCCESS_COMMITS.txt";
    private final static String ERROR_COMMIT_FILE = "ERROR_COMMITS.txt";
    private final static String INCOMPLETE_PC_COMMIT_FILE = "INCOMPLETE_PC_COMMITS.txt";


    /**
     * @param p The path which should be checked.
     * @return true if the path points to a directory that contains at least one of the metadata files, otherwise false.
     */
    @Override
    public boolean canLoad(Path p) {
        try {
            return Files.list(p)
                    .map(Path::toFile)
                    .anyMatch(f -> {
                        String name = f.getName();
                        return name.equals(SUCCESS_COMMIT_FILE) || name.equals(ERROR_COMMIT_FILE) || name.equals(INCOMPLETE_PC_COMMIT_FILE);
                    });
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Result<VariabilityDataset, Exception> load(Path p) {
        // Read the metadata
        List<String> successIds = readLines(p, SUCCESS_COMMIT_FILE);
        List<String> errorIds = readLines(p, ERROR_COMMIT_FILE);
        List<String> incompletePCIds = readLines(p, INCOMPLETE_PC_COMMIT_FILE);

        // Create SPLCommit objects for each commit
        List<SPLCommit> successCommits = initializeSPLCommits(p, successIds);
        List<SPLCommit> errorCommits = initializeSPLCommits(p, errorIds);
        List<SPLCommit> incompletePCCommits = initializeSPLCommits(p, incompletePCIds);

        // Retrieve the SPLCommit objects for the parents of each commit
        Map<String, SPLCommit> idToCommitMap = new HashMap<>();
        successCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        errorCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        incompletePCCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        for (String id : idToCommitMap.keySet()) {
            SPLCommit commit = idToCommitMap.get(id);
            String[] parentIds = loadParentIds(p, id);
            if (parentIds == null || parentIds.length == 0) {
                commit.setParents(null);
            } else {
                commit.setParents(Arrays.stream(parentIds).map(idToCommitMap::get).toArray(SPLCommit[]::new));
            }
        }

        // Return the fully-loaded dataset
        return Result.Success(new VariabilityDataset(successCommits, errorCommits, incompletePCCommits));
    }

    private List<SPLCommit> initializeSPLCommits(Path p, List<String> commitIds) {
        List<SPLCommit> splCommits = new ArrayList<>(commitIds.size());
        for (String id : commitIds) {
            VariabilityFilePaths dataPaths = new VariabilityFilePaths(resolvePathToLogFile(p, id), resolvePathToFeatureModel(p, id), resolvePathToPresenceConditions(p, id));
            SPLCommit splCommit = new SPLCommit(id, dataPaths);
            splCommits.add(splCommit);
        }
        return splCommits;
    }

    private Path resolvePathToCommitOutputDir(Path rootDir, String commitId) {
        return rootDir.resolve("output/" + commitId);
    }

    private Path resolvePathToFeatureModel(Path rootDir, String commitId) {
        Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve("variability-model.json");
        return p.toFile().exists() ? p : null;
    }

    private Path resolvePathToPresenceConditions(Path rootDir, String commitId) {
        Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve("code-variability.csv");
        return p.toFile().exists() ? p : null;
    }

    private Path resolvePathToParentsFile(Path rootDir, String commitId) {
        Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve("PARENTS.txt");
        return p.toFile().exists() ? p : null;
    }

    private Path resolvePathToLogFile(Path rootDir, String commitId) {
        Path p = rootDir.resolve("log/" + commitId + ".log");
        return p.toFile().exists() ? p : null;
    }

    private List<String> readLines(Path p, String fileName) {
        try {
            return Files.readAllLines(p.resolve(fileName));
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
