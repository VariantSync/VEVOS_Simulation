package org.variantsync.vevos.simulation.util;

import java.util.Stack;

public class StackUtil {
    public static <T> void pushAToB(final Stack<T> a, final Stack<T> b) {
        b.addAll(a);
    }
}
