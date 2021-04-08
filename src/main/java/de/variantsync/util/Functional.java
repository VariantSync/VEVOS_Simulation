package de.variantsync.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Functional {
    public static <T, U> List<U> fmap(List<T> a, Function<T, U> f) {
        return a.stream().map(f).collect(Collectors.toList());
    }
}
