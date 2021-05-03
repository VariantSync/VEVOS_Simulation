package de.variantsync.evolution.sat;

import org.prop4j.Node;
import org.prop4j.explain.solvers.SatSolver;
import org.prop4j.explain.solvers.SatSolverFactory;

public class SAT {
    public static boolean isSatisfiable(Node formula) {
        final SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
        solver.addFormulas(formula);
        return solver.isSatisfiable();
    }
}
