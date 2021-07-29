package de.variantsync.evolution.sat;

import de.variantsync.evolution.util.fide.FormulaUtils;
import org.prop4j.And;
import org.prop4j.Equals;
import org.prop4j.Implies;
import org.prop4j.Node;
import org.prop4j.explain.solvers.SatSolver;
import org.prop4j.explain.solvers.SatSolverFactory;

public class SAT {
    public static boolean isSatisfiable(Node formula) {
        final SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
        solver.addFormulas(formula);
        return solver.isSatisfiable();
    }

    public static boolean isTautology(Node formula) {
        return !isSatisfiable(FormulaUtils.negate(formula));
    }

    public static boolean implies(Node left, Node right) {
        return isTautology(new Implies(left, right));
    }

    public static boolean equivalent(Node pc, And and) {
        return isTautology(new Equals(pc, and));
    }
}
