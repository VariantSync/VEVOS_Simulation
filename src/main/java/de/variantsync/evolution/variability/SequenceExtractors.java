package de.variantsync.evolution.variability;

import de.variantsync.evolution.util.list.NonEmptyList;

import java.util.*;
import java.util.function.Function;

public class SequenceExtractors {

    /**
     *
     * @return
     */
    public static Function<Collection<SPLCommit>, List<NonEmptyList<SPLCommit>>> longestSequenceOnly() {
        return (Collection<SPLCommit> commits) -> {
            Map<SPLCommit, Set<SPLCommit>> parentChildMap = new HashMap<>();
            Set<SPLCommit> commitSet = new HashSet<>(commits);
            // Commits that have no parent in the given collection mark the start of a sequence
            Set<SPLCommit> sequenceStartCommits = new HashSet<>();
            // Map each parent to all of its children and determine sequence starts. Hereby we can determine splits
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
                }
            }

            return commitSequences;
        };
    }

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
