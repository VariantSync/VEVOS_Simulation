package de.variantsync.evolution.util;

import java.util.function.IntFunction;

public class GenericArray {
    @SafeVarargs
    public static <T, U extends T> T[] create(U... ts) {
        return ts;
    }

    public static <T> IntFunction<T[]> create() {
        return len -> (T[]) new Object[len];
    }
}
