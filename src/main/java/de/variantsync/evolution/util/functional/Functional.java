package de.variantsync.evolution.util.functional;

import de.variantsync.evolution.util.functional.interfaces.FragileProcedure;
import de.variantsync.evolution.util.functional.interfaces.FragileSupplier;
import de.variantsync.evolution.util.functional.interfaces.Procedure;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Helper class containing methods for functional programming missing in the standard library
 * (or that we could not find).
 * Contains also methods for pattern matching.
 */
public class Functional {
    @SuppressWarnings("unchecked")
    public static <A, B> A uncheckedCast(final B b) {
        return (A) b;
    }

    /// Lists

    public static <T, U> List<U> fmap(final List<? extends T> a, final Function<T, U> f) {
        return a.stream().map(f).collect(Collectors.toList());
    }

    /// Pattern matching

    public static <A, B> B match(final Optional<A> ma, final Function<A, ? extends B> just, final Supplier<? extends B> nothing) {
        final Optional<B> x = ma.map(just);
        return x.orElseGet(nothing);
    }

    /**
     * Curried version of the above.
     */
    public static <A, B> Function<Optional<A>, B> match(final Function<A, B> just, final Supplier<? extends B> nothing) {
        return ma -> match(ma, just, nothing);
    }

    /**
     * Creates a branching function for given condition, then and else case.
     * @param condition The condition choosing whether to run 'then' or 'otherwise'.
     * @param then The function to apply when the given condition is met for a given a.
     * @param otherwise The function to apply when the given condition is not met for a given a.
     * @return A function that for a given a, returns then(a) if the given condition is met, and otherwise returns otherwise(a).
     */
    public static <A, B> Function<A, B> when(final Predicate<A> condition, final Function<A, B> then, final Function<A, B> otherwise) {
        return a -> condition.test(a) ? then.apply(a) : otherwise.apply(a);
    }

    /**
     * The same as @see when but without an else case (i.e., else case function identity).
     */
    public static <A> Function<A, A> when(final Predicate<A> condition, final Function<A, A> then) {
        return when(condition, then, Function.identity());
    }

    /**
     * A variant of @see when with a boolean value instead of a predicate.
     */
    public static <B> Function<Boolean, B> when(final Supplier<B> then, final Supplier<B> otherwise) {
        return condition -> condition ? then.get() : otherwise.get();
    }

    /// Java to FP

    public static <A> Function<A, Unit> Lift(final Consumer<A> f) {
        return a -> {
            f.accept(a);
            return Unit.Instance();
        };
    }

    public static Supplier<Unit> Lift(final Procedure f) {
        return () -> {
            f.run();
            return Unit.Instance();
        };
    }

    public static <E extends Exception> FragileSupplier<Unit, E> LiftFragile(final FragileProcedure<E> f) {
        return () -> {
            f.run();
            return Unit.Instance();
        };
    }
}
