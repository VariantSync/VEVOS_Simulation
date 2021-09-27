package de.variantsync.evolution.variability;

import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.util.functional.CachedValue;
import de.variantsync.evolution.util.list.StackUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class DominoSortedEvolutionSteps<C extends Commit & CachedValue> implements Iterator<EvolutionStep<C>>, Iterable<EvolutionStep<C>> {
    private final Stack<Stack<C>> dominos;
    private final List<C> garbage;

    public DominoSortedEvolutionSteps(final Stream<EvolutionStep<C>> steps) {
        this.dominos = DominoSort(steps);
        this.garbage = new ArrayList<>();
    }

    public void clearCaches() {
        for (final C c : garbage) {
            c.forget();
        }
        garbage.clear();
    }

    @Override
    public boolean hasNext() {
        return !dominos.isEmpty();
    }

    @Override
    public EvolutionStep<C> next() {
        clearCaches();

        final Stack<C> currentRow = dominos.peek();

        assert !currentRow.isEmpty();
        final C child = currentRow.pop();
        garbage.add(child);

        assert !currentRow.isEmpty();
        final C parent = currentRow.peek();

        final EvolutionStep<C> result = new EvolutionStep<C>(parent, child);
        if (currentRow.size() == 1) {
            garbage.add(parent);
            dominos.pop();
        }
        return result;
    }

    @NotNull
    @Override
    public Iterator<EvolutionStep<C>> iterator() {
        return this;
    }

    private static <C extends Commit> Stack<Stack<C>> DominoSort(final Stream<EvolutionStep<C>> dominos) {
        final Stack<Stack<C>> chains = new Stack<>();

        dominos.forEach(domino -> {
            // push the domino to an existing stack if possible
            for (final Stack<C> row : chains) {
                if (row.peek().equals(domino.parent())) {
                    row.push(domino.child());
                    return;
                }
            }

            // if there was no suitable stack, create one
            final Stack<C> newRow = new Stack<>();
            newRow.add(domino.parent());
            newRow.add(domino.child());
            chains.push(newRow);
        });

        // Merge all stacks until they cannot be reduced anymore.
        final Stack<Stack<C>> finalRows = new Stack<>();
        do {
            Stack<C> current = chains.pop();
//            System.out.println("Inspecting " + current);
            final Set<Stack<C>> mergedStacks = new HashSet<>();
            do {
                mergedStacks.clear();
                for (final Stack<C> other : chains) {
                    if (current.firstElement().equals(other.lastElement())) {
//                        System.out.println("  pushing it to " + other);
                        other.pop();
                        StackUtil.pushAToB(current, other);
                        current = other;
//                        System.out.println("  got " + current);
                        mergedStacks.add(other);
                    } else if (other.firstElement().equals(current.lastElement())) {
//                        System.out.println("  pushed " + other + " onto it");
                        current.pop();
                        StackUtil.pushAToB(other, current);
//                        System.out.println("  got " + current);
                        mergedStacks.add(other);
                    }
                }
                chains.removeAll(mergedStacks);
            } while (!mergedStacks.isEmpty());

            finalRows.push(current);
        } while (!chains.isEmpty());

        return finalRows;
    }

    @Override
    public String toString() {
        return "DominoSortedEvolutionSteps{" + dominos + '}';
    }
}
