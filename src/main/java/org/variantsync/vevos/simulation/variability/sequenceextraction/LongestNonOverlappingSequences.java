package org.variantsync.vevos.simulation.variability.sequenceextraction;

import org.variantsync.functjonal.list.NonEmptyList;
import org.variantsync.vevos.simulation.variability.SPLCommit;
import org.variantsync.vevos.simulation.variability.SequenceExtractor;

import java.util.*;

/**
 * Retrieve the longest non-overlapping sequences of commits from a collection of commits.
 */
public class LongestNonOverlappingSequences implements SequenceExtractor {

    /**
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
     *
     * @return A function that has the properties described above.
     */
    @Override
    public List<NonEmptyList<SPLCommit>> extract(final Collection<SPLCommit> commits) {
        final Map<SPLCommit, Set<SPLCommit>> parentChildMap = new HashMap<>();
        final Set<SPLCommit> commitSet = new HashSet<>(commits);
        final Set<SPLCommit> sequenceStartCommits = new HashSet<>();
        // Filter commits and map each parent to all of its children. Additionally, determine sequence starts.
        // Hereby we can handle splits later on
        commits.forEach(c -> c.parents()
                .filter(parents -> {
                    if (parents.length == 1) {
                        if (commitSet.contains(parents[0])) {
                            return true;
                        } else {
                            // Sequence start because the parent's extraction was unsuccessful
                            sequenceStartCommits.add(c);
                        }
                    } else {
                        // Sequence start because it is a merge commit or the start of the history
                        sequenceStartCommits.add(c);
                    }
                    return false;
                })
                // Add the commit as mapping for the parent
                .ifPresent(parents -> parentChildMap.computeIfAbsent(parents[0], k -> new HashSet<>()).add(c)));

        final List<NonEmptyList<SPLCommit>> commitSequences = new LinkedList<>();
        // Retrieve the longest non-overlapping sequences for each start commit
        // Start commits are all commits that have no valid ancestor in the given commit.
        // Therefore, no two start commits can be part of the same sequence
        for (final SPLCommit startCommit : sequenceStartCommits) {
            // We now retrieve sequences starting from the sequenceStartCommit and sort the sequences
            // descending by size, iterate over them, and remove all commits from a sequence that are already part
            // of a longer sequence
            final List<LinkedList<SPLCommit>> orderedSequences = new ArrayList<>(retrieveSequencesForStart(parentChildMap, startCommit));
            orderedSequences.sort((o1, o2) -> Integer.compare(o2.size(), o1.size()));
            for (int i = 0; i < orderedSequences.size(); i++) {
                if (i > 0) {
                    final LinkedList<SPLCommit> largerSequence = orderedSequences.get(i - 1);
                    for (int j = i; j < orderedSequences.size(); j++) {
                        final LinkedList<SPLCommit> shorterSequence = orderedSequences.get(j);
                        // Remove all commits from the shorter sequence that are contained in the larger sequence
                        for (final SPLCommit commit : largerSequence) {
                            // We only have to consider the first element
                            if (shorterSequence.getFirst() == commit) {
                                shorterSequence.removeFirst();
                            }
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

        // Sort the sequences once more, after filtering them
        commitSequences.sort((o1, o2) -> Integer.compare(o2.size(), o1.size()));
        return commitSequences;
    }

    private static Set<LinkedList<SPLCommit>> retrieveSequencesForStart(final Map<SPLCommit, Set<SPLCommit>> parentChildMap, final SPLCommit start) {
        // Set for holding all retrieved sequences
        Set<LinkedList<SPLCommit>> sequenceSet = new HashSet<>();
        {
            // We start with the 'start' commit
            final LinkedList<SPLCommit> sequence = new LinkedList<>();
            sequence.add(start);
            sequenceSet.add(sequence);
        }

        // We continue to build sequences, going from parent to descendents, until the ends of all possible sequences have been reached
        boolean incomplete = true;
        while(incomplete) {
            Set<LinkedList<SPLCommit>> updatedSet = new HashSet<>();
            // Set to false at the start of the iteration, it is set to true if at least one sequence has been extended
            incomplete = false;
            // For all found sequences, check whether their last commit has more children that can be added to the sequence
            // if multiple children exist, new sequences are created
            for (LinkedList<SPLCommit> sequence : sequenceSet) {
                final SPLCommit parent = sequence.getLast();
                if (parentChildMap.containsKey(parent)) {
                    final Set<SPLCommit> children = parentChildMap.get(parent);
                    for (SPLCommit child : children) {
                        // There is at least one child, we are not done yet
                        incomplete = true;
                        // Create a new sequence from the old sequence for each child
                        final LinkedList<SPLCommit> anotherSequence = new LinkedList<>(sequence);
                        anotherSequence.add(child);
                        // Add the new sequence to the new set
                        updatedSet.add(anotherSequence);
                    }
                } else {
                    // There are no children, simply add the old sequence to the updated set
                    updatedSet.add(sequence);
                }
            }
            // Update the sequence set
            sequenceSet = updatedSet;
        }

        return sequenceSet;
    }
}

