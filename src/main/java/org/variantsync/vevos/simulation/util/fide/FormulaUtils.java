package org.variantsync.vevos.simulation.util.fide;

import org.prop4j.*;
import org.variantsync.vevos.simulation.util.fide.bugfix.FixTrueFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class FormulaUtils {
    public static Node negate(final Node node) {
        if (node instanceof Literal l) {
            return negate(l);
        }

        return new Not(node);
    }

    public static Literal negate(final Literal lit) {
        if (lit == null || lit.var == null) {
            throw new NullPointerException();
        }
        return new Literal(lit.var, !lit.positive);
    }

    public static Node AndSimplified(final Node a, final Node b) {
        if (FixTrueFalse.isTrue(a)) {
            return b;
        } else if (FixTrueFalse.isTrue(b)) {
            return a;
        } else if (FixTrueFalse.isFalse(a) || FixTrueFalse.isFalse(b)) {
            return FixTrueFalse.False;
        }

        final And result = new And(a, b);
        flatten(result);
        return result;
    }

    /**
     * This method inlines all recursive Ands into the top level And, passed as argument.
     */
    public static void flatten(final And and) {
        final List<And> redundantChildren = new ArrayList<>();

        do {
            final List<Node> andsChildren = new ArrayList<>(Arrays.asList(and.getChildren()));
            for (final And redundantChild : redundantChildren) {
                andsChildren.remove(redundantChild);
                andsChildren.addAll(Arrays.asList(redundantChild.getChildren()));
            }
            redundantChildren.clear();
            and.setChildren(andsChildren.toArray());

            for (final Node child : and.getChildren()) {
                if (child instanceof And) {
                    redundantChildren.add((And)child);
                }
            }
        } while (!redundantChildren.isEmpty());
    }

    /**
     * Replaces all nodes within the given formula's tree that match the given predicate.
     * Matching nodes (i.e., nodes for which the predicate who returns true) will be replaced by the value returned
     * by the replacement function, invoked on the matching node.
     * @param root The root of the formula in which occurences of formulas should be replaced. The object remains unaltered.
     * @param who A replacement is made whenever this predicate evaluates to true on a given node.
     * @param replacement Whenever a node should be replaced, this function is invoked with that node as argument.
     *                    The node will be replaced with the node returned.
     * @return A new formula in which all nodes matching the given predicate are replaced.
     */
    public static Node replaceAll(final Node root, final Predicate<Node> who, final Function<Node, Node> replacement) {
        if (root == null) {
            return null;
        }

        return replaceAllInplace(root.clone(), who, replacement);
    }

    /**
     * Inplace variant of the {@link #replaceAll(Node, Predicate, Function)} function.
     * This means the given formula (root parameter) will be altered.
     */
    public static Node replaceAllInplace(final Node root, final Predicate<Node> who, final Function<Node, Node> replacement) {
        if (root == null) {
            return null;
        }

        if (who.test(root)) {
            return replacement.apply(root);
        } else {
            final Node[] children = root.getChildren();
            if (children != null) {
                for (int i = 0; i < children.length; ++i) {
                    children[i] = replaceAllInplace(children[i], who, replacement);
                }
                root.setChildren(children);
            }
            return root;
        }
    }

    /**
     * Wraps a literal name in a defined statement.
     * x -> defined(x)
     * !x -> !defined(x)
     */
    private static Node ifdef(final Literal l) {
        final Function<Object, Literal> toDefinedLiteral = s -> new Literal("defined(" + s + ")");

        if (l.positive) {
            return toDefinedLiteral.apply(l);
        } else {
            return new Not(toDefinedLiteral.apply(new Literal(l.var, true)));
        }
    }

    /**
     * Serializes the given formula to a string usable in C preprocessor conditions.
     * True and false will be converted to 1 and 0, respectively.
     * Variables will be wrapped in a defined expression.
     * @see FixTrueFalse#TrueAs1
     * @see FixTrueFalse#FalseAs0
     */
    public static String toCPPString(Node formula) {
        formula = replaceAll(formula, FixTrueFalse::isTrue, n -> FixTrueFalse.TrueAs1);
        formula = replaceAllInplace(formula, FixTrueFalse::isFalse, n -> FixTrueFalse.FalseAs0);
        formula = replaceAllInplace(formula, FixTrueFalse::isVariable,
                // we know that n is a literal because FixTureFalse::isVariable returned true
                n -> ifdef((Literal) n)
        );
        return toFormulaString(formula, NodeWriter.javaSymbols);
    }

    public static String toFormulaString(final Node formula, final String[] symbols) {
        final NodeWriter writer = new NodeWriter(formula);
        writer.setNotation(NodeWriter.Notation.INFIX);
        writer.setEnquoteWhitespace(false);
        writer.setEnforceBrackets(true);
        writer.setSymbols(symbols);
        return writer.nodeToString();
    }
}
