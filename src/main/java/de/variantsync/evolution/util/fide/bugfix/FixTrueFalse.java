package de.variantsync.evolution.util.fide.bugfix;

import de.ovgu.featureide.fm.core.editing.NodeCreator;
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

    public final static Literal True = new Literal(NodeCreator.varTrue);
    public final static Literal False = new Literal(NodeCreator.varFalse);

    public static boolean isTrue(Node n) {
        return n instanceof Literal l && isTrueLiteral(l);
    }

    public static boolean isFalse(Node n) {
        return n instanceof Literal l && isFalseLiteral(l);
    }

    public static boolean isTrueLiteral(Literal l) {
        return trueNames.stream().anyMatch(t -> t.equals(l.var.toString().toLowerCase()));
    }

    public static boolean isFalseLiteral(Literal l) {
        return falseNames.stream().anyMatch(f -> f.equals(l.var.toString().toLowerCase()));
    }

    public static Node On(Node formula) {
        if (formula instanceof Literal l) {
            if (isTrueLiteral(l)) {
                return l.positive ? True : False;
            }
            if (isFalseLiteral(l)) {
                return l.positive ? False : True;
            }
            return l;
        }
        if (formula instanceof org.prop4j.True) {
            return True;
        }
        if (formula instanceof org.prop4j.False) {
            return False;
        }

        // else we have an operator (Not, And, Or, ...)
        Node[] children = formula.getChildren();
        for (int i = 0; i < children.length; ++i) {
            children[i] = On(children[i]);
        }
        return formula;
    }
}
