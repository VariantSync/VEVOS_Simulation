package de.variantsync.evolution.util.fide.bugfix;

import de.variantsync.evolution.util.NotImplementedException;
import org.prop4j.*;

import java.util.Arrays;
import java.util.List;

/**
 * Class to fix bugs related to True and False classes of FeatureIDE.
 * See: https://github.com/FeatureIDE/FeatureIDE/issues/1111
 */
public class FixTrueFalse {
    private final static List<String> trueNames = Arrays.asList("true", "1");
    private final static List<String> falseNames = Arrays.asList("false", "0");

    private static boolean isTrue(Literal l) {
        return trueNames.stream().anyMatch(t -> t.equals(l.var.toString().toLowerCase()));
    }

    private static boolean isFalse(Literal l) {
        return falseNames.stream().anyMatch(f -> f.equals(l.var.toString().toLowerCase()));
    }

    public static Node On(Node formula) {
        if (formula instanceof And and) {
            return new And(fmapOn(and.getChildren()));
        }
        if (formula instanceof Or or) {
            return new Or(fmapOn(or.getChildren()));
        }
        if (formula instanceof Implies i) {
            return new Implies(
                    On(i.getChildren()[0]),
                    On(i.getChildren()[1])
            );
        }
        if (formula instanceof Equals e) {
            return new Equals(
                    On(e.getChildren()[0]),
                    On(e.getChildren()[1])
            );
        }
        if (formula instanceof Not n) {
            return new Not(On(n.getChildren()[0]));
        }
        if (formula instanceof Literal l) {
            if (isTrue(l)) {
                return l.positive ? FTrue.asLiteral() : FFalse.asLiteral();
            }
            if (isFalse(l)) {
                return l.positive ? FFalse.asLiteral() : FTrue.asLiteral();
            }
            return l.clone();
        }
        if (formula instanceof True) {
            return FTrue.asLiteral();
        }
        if (formula instanceof False) {
            return FFalse.asLiteral();
        }

        throw new NotImplementedException("Method not implemented for node type \"" + formula.getClass().getCanonicalName() + "\" but was invoked for " + formula);
    }

    private static Node[] fmapOn(Node[] children) {
        Node[] target = new Node[children.length];
        for (int i = 0; i < children.length; ++i) {
            target[i] = On(children[i]);
        }
        return target;
    }
}
