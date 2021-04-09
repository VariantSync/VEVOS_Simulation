package de.variantsync;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.sat.SAT;
import org.prop4j.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hi Alex");

        // Test imports
        IFeatureModel m;

        final Node formula = new And(new Literal("A"), new Literal("A", false));
        System.out.println("SAT(" + formula + ") = " + SAT.isSatisfiable(formula));
    }
}