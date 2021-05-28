package de.variantsync.evolution.util.functional;

import java.util.Optional;
import java.util.function.Function;

public class OptionalLazyT<A> implements Functor<OptionalLazyT, A> {
    private final Lazy<Optional<A>> monad;

    public OptionalLazyT(Lazy<Optional<A>> wrappee) {
        monad = wrappee;
    }

    @Override
    public <B> OptionalLazyT<B> map(Function<? super A, ? extends B> f) {
        return new OptionalLazyT<>(monad.map(o -> o.map(f)));
    }

    public <B> OptionalLazyT<B> bind(Function<A, OptionalLazyT<B>> f) {
        return new OptionalLazyT<>(MonadTransformer.bind(monad, a -> f.apply(a).monad));
    }

    public Lazy<Optional<A>> getMonad() {
        return monad;
    }
}
