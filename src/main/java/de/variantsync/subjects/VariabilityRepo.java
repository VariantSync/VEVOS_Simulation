package de.variantsync.subjects;

import de.variantsync.util.SimpleConsoleLogger;
import java.lang.String;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VariabilityRepo {
    private static final SimpleConsoleLogger LOGGER = SimpleConsoleLogger.get();
    private final Map<String, String> commitToSPLCommit;
    private final Map<String, String[]> childParentMap;
    private final Set<String> errorCommits;
    private final Set<String> successCommits;

    VariabilityRepo(Map<String, String> commitToSPLCommit, Map<String, String[]> childParentMap, Set<String> errorCommits, Set<String> successCommits) {
        this.commitToSPLCommit = commitToSPLCommit;
        this.childParentMap = childParentMap;
        this.errorCommits = errorCommits;
        this.successCommits = successCommits;
        LOGGER.status("Variability repository initialized");
    }

    public String[] getParents(String commit) {
        return childParentMap.get(commit);
    }

    public Set<String> getSuccessCommitsWithOneParent() {
        return successCommits.stream().filter((c) -> childParentMap.get(c).length == 1).collect(Collectors.toSet());
    }

    public String getSPLCommit(String commit) {
        return commitToSPLCommit.get(commit);
    }

    public Set<String> getErrorCommits() {
        return new HashSet<>(errorCommits);
    }

    public Set<String> getSuccessCommits() {
        return new HashSet<>(successCommits);
    }
}
