package de.variantsync.util;

import de.variantsync.evolution.variant.VariantRevision;

import java.lang.reflect.Array;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class Functional {
    public static <T, U> List<U> fmap(List<? extends T> a, Function<T, U> f) {
        return a.stream().map(f).collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T, U extends T> T[] createArray(U... ts) {
        return ts;
    }

    public static <T> IntFunction<T[]> createArray() {
        return len -> (T[]) new Object[len];
    }
}
