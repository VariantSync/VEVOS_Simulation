package de.variantsync.evolution.variability;

import de.variantsync.evolution.util.list.NonEmptyList;

import java.util.*;
import java.util.function.Function;

public class SequenceExtractors {

    /**
     * Retrieve the longest non-overlapping sequences of commits from a collection of commits.
     *
     * <p>
     * The given commits are first filtered to only retain commits with exactly one parent that also must be contained in
     * the given collection. Then the sequences are determined. Considering branch/fork operations, each commit could
     * possible be part of multiple sequences if there is a branch/fork among its descendants. This function extracts
     * the longest sequence that is possible for a set of related commits (ancestors/descendants), i.e., the longest branch.
     * Afterwards, the second longest sequence is determined, then the third, and so on.
     * </p>
     * <p>
     * For example, if the commits comprise three partially overlapping sequences ([A-B-C-D-E], [X-Y-Z], [A-B-F-G]),
     * the function will return the sequences ([A-B-C-D-E], [X-Y-Z], [F-G]).
     * </p>
     * @return A function that has the properties described above.
     */
    public static Function<Collection<SPLCommit>, List<NonEmptyList<SPLCommit>>> longestNonOverlappingSequences() {
        return (Collection<SPLCommit> commits) -> {
            Map<SPLCommit, Set<SPLCommit>> parentChildMap = new HashMap<>();
            Set<SPLCommit> commitSet = new HashSet<>(commits);
            // Commits that have no parent in the given collection mark the start of a sequence
            Set<SPLCommit> sequenceStartCommits = new HashSet<>();
            // Filter commits and map each parent to all of its children. Additionally, determine sequence starts.
            // Hereby we can handle splits later on
            commitSet.forEach(c -> c.parents()
                    .filter(parents -> {
                        if (parents.length == 1) {
                            if (commitSet.contains(parents[0])) {
                                return true;
                            } else {
                                sequenceStartCommits.add(c);
                            }
                        }
                        return false;
                    })
                    // Add the commit as mapping for the parent
                    .ifPresent(parents -> parentChildMap.computeIfAbsent(parents[0], k -> new HashSet<>()).add(c)));

            List<NonEmptyList<SPLCommit>> commitSequences = new LinkedList<>();
            // Retrieve the longest non-overlapping sequences for each start commit
            for (Map.Entry<SPLCommit, Set<SPLCommit>> entry : parentChildMap.entrySet()) {
                // We now retrieve sequences starting from the sequenceStartCommits
                if (sequenceStartCommits.contains(entry.getKey())) {
                    Set<LinkedList<SPLCommit>> sequences = retrieveSequencesForStart(parentChildMap, entry.getKey());
                    LinkedList<SPLCommit> longestSequence = null;
                    for (LinkedList<SPLCommit> sequence : sequences) {
                        if (longestSequence == null) {
                            longestSequence = sequence;
                        } else if (longestSequence.size() < sequence.size()) {
                            longestSequence = sequence;
                        }
                    }
                    if (longestSequence != null && longestSequence.size() > 1) {
                        commitSequences.add(new NonEmptyList<>(longestSequence));
                    }

                    // Now, sort the sequences descending by size, iterate over them, and remove all commits
                    // from a sequence that are already part of a longer sequence
                    List<LinkedList<SPLCommit>> orderedSequences = new ArrayList<>(sequences);
                    orderedSequences.sort((o1, o2) -> Integer.compare(o2.size(), o1.size()));
                    for (int i = 1; i < orderedSequences.size(); i++) {
                        LinkedList<SPLCommit> largerSequence = orderedSequences.get(i-1);
                        for (int j = i; j < orderedSequences.size(); j++) {
                            LinkedList<SPLCommit> shorterSequence = orderedSequences.get(j);
                            // Remove all commits from the shorter sequence that are contained in the larger sequence
                            for (SPLCommit commit : largerSequence) {
                                // We only have to consider the first element
                                if (shorterSequence.getFirst() == commit) {
                                    shorterSequence.removeFirst();
                                }
                            }
                        }
                        // The sequence at i has now been filtered completely, so it can be added if it contains at least
                        // two commits
                        if (orderedSequences.get(i).size() > 1) {
                            commitSequences.add(new NonEmptyList<>(orderedSequences.get(i)));
                        }
                    }
                }
            }

            return commitSequences;
        };
    }

    // Recursively build sequences
    private static Set<LinkedList<SPLCommit>> retrieveSequencesForStart(Map<SPLCommit, Set<SPLCommit>> parentChildMap, SPLCommit start) {
        if (!parentChildMap.containsKey(start)) {
            // Create a new sequence that contains the start commit
            Set<LinkedList<SPLCommit>> sequenceSet = new HashSet<>();
            LinkedList<SPLCommit> sequence = new LinkedList<>();
            sequence.add(start);
            sequenceSet.add(sequence);
            return sequenceSet;
        } else {
            // Collect the sequences of the children and prepend the commit to each of them as parent
            Set<LinkedList<SPLCommit>> sequences = new HashSet<>();
            for (SPLCommit child : parentChildMap.get(start)) {
                Set<LinkedList<SPLCommit>> childSequences = retrieveSequencesForStart(parentChildMap, child);
                for (LinkedList<SPLCommit> childSequence : childSequences) {
                    childSequence.addFirst(start);
                    sequences.add(childSequence);
                }
            }
            return sequences;
        }
    }
}
