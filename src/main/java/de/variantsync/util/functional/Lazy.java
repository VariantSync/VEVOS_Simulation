package de.variantsync.util.functional;

import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Monad for explicit lazy evaluation.
 * A Lazy<A> represents a computation returning a A.
 * Using lazy allows to compose such computations without ever running the actual computation.
 * This way, writing a program is separated from evaluating the program.
 * (This lazy monad is actually the reader monad with an empty environment + a cache.)
 *
 * Use Lazy to make explicit the points in computation when we interact with the environment.
 * In particular, in that moment, when you access the lazy's content, all necessary computations will run.
 * In this project, this means that the interactions with the file system and git repositories will take place at exactly
 * that moment.
 *
 * @param <A> The return type of this lazy computation.
 */
public class Lazy<A> {
    private final Supplier<? extends A> get;
    private A val = null;

    private Lazy(A val) {
        assert val != null;
        this.val = val;
        this.get = null;
    }

    private Lazy(Supplier<? extends A> get) {
        this.get = get;
    }

    public static <B> Lazy<B> of(Supplier<? extends B> f) {
        return new Lazy<>(f);
    }

    public static <B> Lazy<B> pure(B b) {
        return new Lazy<>(b);
    }

    /**
     * Run the lazy computation and obtain the result.
     * @return The result of this lazy computation.
     */
    public A run() {
        if (val == null) {
            val = get.get();
        }
        return val;
    }

    /**
     * Lazy is a functor.
     * @param f Function to apply to the result of this lazy when it is computed.
     * @return Composed lazy that applies f to the result of this lazy after computation.
     */
    public <B> Lazy<B> map(Function<? super A, ? extends B> f) {
        return new Lazy<>(() -> f.apply(run()));
    }

    /**
     * Lazy is an applicative functor.
     * @param lf Lazy that holds a function to apply to this lazy's result after computation (similar to map).
     * @return Composed lazy that applies the function computed by lf to the result of this lazy after computation.
     */
    public <B> Lazy<B> splat(Lazy<Function<? super A, ? extends B>> lf) {
        return new Lazy<>(() -> lf.run().apply(run()));
    }

    /**
     * Lazy is a monad.
     * Chains the given lazy computation with this one (i.e., applies the given lazy comptuation to the result of this lazy once its computed).
     * You might replace "bind" with "then" in your mind to see how it works.
     * @param f A lazy computation to chain to this one.
     * @return Returns a new lazy computation composed of this and the given lazy.
     */
    public <B> Lazy<B> bind(Function<A, Lazy<B>> f) {
        // This is the inlined version of `join(map(f))` for performance reasons.
        return new Lazy<>(() -> f.apply(run()).run()); // == join(map(f))
    }

    public static <B> Lazy<B> join(Lazy<Lazy<B>> l) {
        return new Lazy<>(() -> l.run().run());
    }

    public <B> Lazy<Pair<A, B>> and(Lazy<? extends B> other) {
        return new Lazy<>(() -> new Pair<>(run(), other.run()));
    }
}
