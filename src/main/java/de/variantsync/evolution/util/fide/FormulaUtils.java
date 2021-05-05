package de.variantsync.evolution.util.fide;

import org.prop4j.And;
import org.prop4j.False;
import org.prop4j.Node;
import org.prop4j.True;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormulaUtils {
    public static Node AndSimplified(Node a, Node b) {
        if (a instanceof True) {
            return b;
        } else if (b instanceof True) {
            return a;
        } else if (a instanceof False || b instanceof False) {
            return new False();
        }

        return new And(a, b);
    }

    /**
     * This method inlines all recursive Ands into the top level And, passed as argument.
     */
    public static void flatten(And and) {
        List<And> redundantChildren = new ArrayList<>();

        do {
            List<Node> andsChildren = new ArrayList<>(Arrays.asList(and.getChildren()));
            for (And redundantChild : redundantChildren) {
                andsChildren.remove(redundantChild);
                andsChildren.addAll(Arrays.asList(redundantChild.getChildren()));
            }
            redundantChildren.clear();
            and.setChildren(andsChildren.toArray());

            for (Node child : and.getChildren()) {
                if (child instanceof And) {
                    redundantChildren.add((And)child);
                }
            }
        } while (!redundantChildren.isEmpty());
    }
}
