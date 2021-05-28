package de.variantsync.evolution.util.functional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Helper class containing methods for functional programming missing in the standard library
 * (or that we could not find).
 * Contains also methods for pattern matching.
 */
public class Functional {
    /// Lists

    public static <T, U> List<U> fmap(List<? extends T> a, Function<T, U> f) {
        return a.stream().map(f).collect(Collectors.toList());
    }

    /// Optional

    public static <A, B> B match(Optional<A> ma, Function<A, B> just, Supplier<? extends B> nothing) {
        final Optional<B> x = ma.map(just);
        return x.orElseGet(nothing);
    }

    /**
     * Curried version of the above.
     */
    public static <A, B> Function<Optional<A>, B> match(Function<A, B> just, Supplier<? extends B> nothing) {
        return ma -> match(ma, just, nothing);
    }
}
