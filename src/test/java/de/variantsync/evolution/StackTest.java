package de.variantsync.evolution;

import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.util.functional.CachedValue;
import de.variantsync.evolution.util.list.StackUtil;
import de.variantsync.evolution.variability.DominoSortedEvolutionSteps;
import de.variantsync.evolution.variability.EvolutionStep;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Stack;

public class StackTest {
    private static class TestCommit extends Commit implements CachedValue {
        public TestCommit(final String commitId) {
            super(commitId);
        }
        @Override
        public void forget() {
            System.out.println("forget(" + super.id() + ")");
        }
    }

    private static TestCommit commit(final String id) {
        return new TestCommit(id);
    }

    private static EvolutionStep<TestCommit> pair(final String parent, final String child) {
        return new EvolutionStep<>(commit(parent), commit(child));
    }

    @Test
    public void testMergeStacks() {
        final Stack<Integer> b = new Stack<>();
        b.push(1);
        b.push(2);
        final Stack<Integer> a = new Stack<>();
        a.push(3);
        a.push(4);
        final Stack<Integer> result = new Stack<>();
        result.push(1);
        result.push(2);
        result.push(3);
        result.push(4);

        StackUtil.pushAToB(a, b);
        Assert.assertEquals(b, result);
    }

    @Test
    public void evolSteps() {
        final List<EvolutionStep<TestCommit>> input = List.of(
                pair("1", "2"),
                pair("4", "5"),
                pair("2", "3"),
                pair("3", "4"),

                pair("12", "13"),
                pair("10", "11"),
                pair("11", "12")
        );

        final DominoSortedEvolutionSteps<TestCommit> steps = new DominoSortedEvolutionSteps<>(input.stream());
        System.out.println("Sorted steps: " + steps);

        for (final EvolutionStep<TestCommit> step : steps) {
            System.out.println("Processing " + step);
        }
        System.out.println("Manual cleanup");
        steps.clearCaches();
    }
}
