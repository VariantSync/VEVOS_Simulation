package de.variantsync.util;

import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Monad for explizit lazy evaluation.
 * A Lazy<T> represents a computation returning a T.
 * Using lazy allows to compose such computations without ever running the actual computation.
 * This way, writing a program is separated from evaluating the program.
 * (This lazy monad is actually the reader monad with an empty environment + a cache.)
 *
 * Use Lazy to make explicit the points in computation when we interact with the environment.
 * In particular, in that moment, when you access the lazy's content, all necessary computations will run.
 * In this project, this means that the interactions with the file system and git repositories will take place at exactly
 * that moment.
 *
 * @param <T> The return type of this lazy computation.
 */
public class Lazy<T> {
    private final Supplier<? extends T> get;
    private T val = null;

    public Lazy(Supplier<? extends T> get) {
        this.get = get;
    }

    public T run() {
        if (val == null) {
            val = get.get();
        }
        return val;
    }

    public <U> Lazy<U> map(Function<? super T, ? extends U> f) {
        return new Lazy<>(() -> f.apply(run()));
    }

    public <U> Lazy<U> bind(Function<? super T, Lazy<U>> f) {
        // This is the inlined version of `join(map(f))` for performance reasons.
        return new Lazy<>(() -> f.apply(run()).run()); // == join(map(f))
    }

    public static <U> Lazy<U> join(Lazy<Lazy<U>> l) {
        return new Lazy<>(() -> l.run().run());
    }

    public <U> Lazy<Pair<T, U>> and(Lazy<? extends U> other) {
        return new Lazy<>(() -> new Pair<>(run(), other.run()));
    }

    public static <U> Lazy<U> of(Supplier<? extends U> f) {
        return new Lazy<>(f);
    }
}
