package de.variantsync.evolution.util.fide;

import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import org.prop4j.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormulaUtils {
    public static Node AndSimplified(Node a, Node b) {
        if (FixTrueFalse.isTrue(a)) {
            return b;
        } else if (FixTrueFalse.isTrue(b)) {
            return a;
        } else if (FixTrueFalse.isFalse(a) || FixTrueFalse.isFalse(b)) {
            return FixTrueFalse.False;
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
