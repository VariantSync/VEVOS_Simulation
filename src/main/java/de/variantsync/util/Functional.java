package de.variantsync.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Functional {
    /// Lists

    public static <T, U> List<U> fmap(List<? extends T> a, Function<T, U> f) {
        return a.stream().map(f).collect(Collectors.toList());
    }

    /// Optional

    public static <A, B> Optional<B> bind(Optional<A> ma, Function<A, Optional<B>> f) {
        return join(ma.map(f));
    }

    public static <A, B> B match(Optional<? extends A> ma, Function<A, ? extends B> just, Supplier<? extends B> nothing) {
        final Optional<B> x = ma.map(just);
        return x.orElseGet(nothing);
    }

    /**
     * Curried version of the above.
     */
    public static <A, B> Function<Optional<? extends A>, B> match(Function<A, ? extends B> just, Supplier<? extends B> nothing) {
        return ma -> match(ma, just, nothing);
    }

    public static <A> Optional<A> join(Optional<Optional<A>> mma) {
        return mma.orElseGet(Optional::empty);
    }
}
