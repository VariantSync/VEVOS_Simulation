package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.sat.SAT;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LineBasedAnnotationSimplifier {
    public static Node simplifiedMapping(LineBasedAnnotation root) {
        // Actually we could employ presence condition simplification by Rhein et al. here.
        return root.getFeatureMapping().simplifyTree();
    }

    public static List<LineBasedAnnotation> flattenedSubtrees(LineBasedAnnotation root) {
        final Queue<LineBasedAnnotation> subtreesToCheck = new LinkedList<>(root.getSubtrees());
        final List<LineBasedAnnotation> nonRedundantSubtrees = new ArrayList<>(subtreesToCheck.size());

        LineBasedAnnotation subtreeToCheck;
        while (!subtreesToCheck.isEmpty()) {
            subtreeToCheck = subtreesToCheck.poll();
            if (SAT.implies(root.getFeatureMapping(), subtreeToCheck.getFeatureMapping())) {
                subtreesToCheck.addAll(subtreeToCheck.getSubtrees());
            } else {
                nonRedundantSubtrees.add(subtreeToCheck);
            }
        }

        return nonRedundantSubtrees;
    }

    public static List<LineBasedAnnotation> mergedEqualNeighbours(final List<LineBasedAnnotation> mappings) {
        if (mappings.isEmpty()) return mappings;

        // Merge similar neighbouring children
        List<LineBasedAnnotation> simplifiedSubtrees = new ArrayList<>(mappings.size());
        LineBasedAnnotation currentSubtree = mappings.get(0);
        simplifiedSubtrees.add(currentSubtree);

        for (int subtreeIndex = 1; subtreeIndex < mappings.size(); ++subtreeIndex) {
            final LineBasedAnnotation nextSubtree = mappings.get(subtreeIndex);
            // If two neighbouring nodes are equal ...
            if (currentSubtree.getLineTo() + 1 == nextSubtree.getLineFrom() && currentSubtree.getFeatureMapping().equals(nextSubtree.getFeatureMapping())) {
                // ... merge them
                currentSubtree.addTraces(nextSubtree.getSubtrees());
                currentSubtree.setLineTo(nextSubtree.getLineTo());
            } else {
                // ... otherwise keep them as is.
                currentSubtree = nextSubtree;
                simplifiedSubtrees.add(currentSubtree);
            }
        }

//        System.out.println(mappings);
//        System.out.println(simplifiedSubtrees);

        return simplifiedSubtrees;
    }

    public static void simplify(LineBasedAnnotation root) {
        root.setFeatureMapping(simplifiedMapping(root));
        List<LineBasedAnnotation> flattenedSubtrees = mergedEqualNeighbours(flattenedSubtrees(root));
        for (LineBasedAnnotation child : flattenedSubtrees) {
            simplify(child);
        }
        root.setSubtrees(flattenedSubtrees);
    }
}
