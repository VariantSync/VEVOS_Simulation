package de.variantsync.evolution.util.fide.bugfix;

import org.prop4j.Literal;
import org.prop4j.Node;

import java.util.Map;

public class FTrue {
    private static final FTrue instance = new FTrue();
    private static final Literal litInstance = new Literal(instance);

    private FTrue() {}

    public static FTrue Instance() {
        return instance;
    }
    public static Literal asLiteral() { return litInstance; }

    @Override
    public String toString() {
        return "true";
    }
}
