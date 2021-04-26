package de.variantsync.util.functional;

import java.util.function.Function;

public interface Functor<F, A> {
    <B> F map(Function<? super A, ? extends B> f);
}
