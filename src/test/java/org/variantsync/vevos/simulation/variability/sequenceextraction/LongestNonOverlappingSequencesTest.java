package org.variantsync.vevos.simulation.variability.sequenceextraction;

import org.junit.Test;
import org.variantsync.functjonal.list.NonEmptyList;
import org.variantsync.vevos.simulation.variability.SPLCommit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LongestNonOverlappingSequencesTest {

    @Test
    public void exampleExtraction() {
        // For example, if the commits comprise three partially overlapping sequences ([A-B-C-D-E], [X-Y-Z], [A-B-F-G]),
        // the function will return the sequences ([A-B-C-D-E], [X-Y-Z], [F-G]).
        final SPLCommit a = new SPLCommit("A");
        final SPLCommit b = new SPLCommit("B");
        final SPLCommit c = new SPLCommit("C");
        final SPLCommit d = new SPLCommit("D");
        final SPLCommit e = new SPLCommit("E");
        final SPLCommit f = new SPLCommit("F");
        final SPLCommit g = new SPLCommit("G");
        final SPLCommit x = new SPLCommit("X");
        final SPLCommit y = new SPLCommit("Y");
        final SPLCommit z = new SPLCommit("Z");
        // A has no parent
        a.setParents();
        // A-B
        b.setParents(a);
        // B-C
        c.setParents(b);
        // C-D
        d.setParents(c);
        // D-E
        e.setParents(d);
        // B-F
        f.setParents(b);
        // F-G
        g.setParents(f);
        // X has no parent
        x.setParents();
        // X-Y
        y.setParents(x);
        // Y-Z
        z.setParents(y);

        List<SPLCommit> exampleCommits = Arrays.asList(a, b, c, d, e, f, g, x, y, z);
        NonEmptyList<SPLCommit> expectedOne = new NonEmptyList<>(Arrays.asList(a,b,c,d,e));
        NonEmptyList<SPLCommit> expectedTwo = new NonEmptyList<>(Arrays.asList(x, y, z));
        NonEmptyList<SPLCommit> expectedThree = new NonEmptyList<>(Arrays.asList(f, g));

        LongestNonOverlappingSequences algorithm = new LongestNonOverlappingSequences();
        List<NonEmptyList<SPLCommit>> result = algorithm.extract(exampleCommits);

        assert result.size() == 3;
        assert sequencesAreEqual(result.get(0), expectedOne);
        assert sequencesAreEqual(result.get(1), expectedTwo);
        assert sequencesAreEqual(result.get(2), expectedThree);
    }

    @Test
    public void stackOverflowPrevented() {
        int size = 10_000;
        Set<SPLCommit> commits = new HashSet<>();
        SPLCommit previousCommit = new SPLCommit("0");
        previousCommit.setParents();
        commits.add(previousCommit);
        for (int i = 1; i < size; i++) {
            SPLCommit commit = new SPLCommit(String.valueOf(i));
            commit.setParents(previousCommit);
            commits.add(commit);
            previousCommit = commit;
        }

        LongestNonOverlappingSequences algo = new LongestNonOverlappingSequences();
        var result = algo.extract(commits);
        assert result.size() == 1;
        assert result.get(0).size() == size;
    }

    private boolean sequencesAreEqual(final NonEmptyList<SPLCommit> a, final NonEmptyList<SPLCommit> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).id().equals(b.get(i).id())) {
                return false;
            }
        }
        return true;
    }
}
