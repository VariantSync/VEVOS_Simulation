package de.variantsync.evolution.util.fide.bugfix;

import org.prop4j.Literal;

public class FFalse {
    private static final FFalse instance = new FFalse();
    private static final Literal litInstance = new Literal(instance);
    private FFalse() {}

    public static FFalse Instance() {
        return instance;
    }
    public static Literal asLiteral() { return litInstance; }

    @Override
    public String toString() {
        return "false";
    }
}
