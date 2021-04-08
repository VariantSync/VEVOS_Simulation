package de.variantsync.sat;

import org.prop4j.Node;
import org.prop4j.explain.solvers.SatSolverFactory;

public class SAT {
    public static boolean isSatisfiable(Node node) {
        final org.prop4j.explain.solvers.SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
        solver.addFormulas(node);
        return solver.isSatisfiable();
    }
}
