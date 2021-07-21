package de.variantsync.evolution.util.functional;

import de.variantsync.evolution.util.functional.interfaces.FragileProcedure;
import de.variantsync.evolution.util.functional.interfaces.FragileSupplier;
import de.variantsync.evolution.util.functional.interfaces.Procedure;

import java.util.Collection;
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
    public static <A> Function<A, A> performSideEffect(Consumer<A> sideEffect) {
        return a -> {
            sideEffect.accept(a);
            return a;
        };
    }

    public static <A, B> A uncheckedCast(B b) {
        return (A) b;
    }

    /// Lists

    public static <T, U> List<U> fmap(List<? extends T> a, Function<T, U> f) {
        return a.stream().map(f).collect(Collectors.toList());
    }

    /// Pattern matching

    public static <A, B> B match(Optional<A> ma, Function<A, ? extends B> just, Supplier<? extends B> nothing) {
        final Optional<B> x = ma.map(just);
        return x.orElseGet(nothing);
    }

    /**
     * Curried version of the above.
     */
    public static <A, B> Function<Optional<A>, B> match(Function<A, B> just, Supplier<? extends B> nothing) {
        return ma -> match(ma, just, nothing);
    }

    /**
     * Creates a branching function for given condition, then and else case.
     * @param condition The condition upon which'S result 'then' or 'otherwise' will be run.
     * @param then The function to apply when the given condition is met for a given a.
     * @param otherwise The function to apply when the given condition is not met for a given a.
     * @return A function that for a given a, returns then(a) if the given condition is met, and otherwise returns otherwise(a).
     */
    public static <A, B> Function<A, B> when(Predicate<A> condition, Function<A, B> then, Function<A, B> otherwise) {
        return a -> condition.test(a) ? then.apply(a) : otherwise.apply(a);
    }

    /**
     * The same as @see when but without an else case (i.e., else case function identity).
     */
    public static <A> Function<A, A> when(Predicate<A> condition, Function<A, A> then) {
        return when(condition, then, Function.identity());
    }

    /**
     * A variant of @see when with a boolean value instead of a predicate.
     */
    public static <B> Function<Boolean, B> when(Supplier<B> then, Supplier<B> otherwise) {
        return condition -> condition ? then.get() : otherwise.get();
    }

    /// Java to FP

    public static <A> Function<A, Unit> Lift(Consumer<A> f) {
        return a -> {
            f.accept(a);
            return Unit.Instance();
        };
    }

    public static Supplier<Unit> Lift(Procedure f) {
        return () -> {
            f.run();
            return Unit.Instance();
        };
    }

    public static <E extends Exception> FragileSupplier<Unit, E> LiftFragile(FragileProcedure<E> f) {
        return () -> {
            f.run();
            return Unit.Instance();
        };
    }
}
