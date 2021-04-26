package de.variantsync.util.functional;

import java.util.Optional;
import java.util.function.Function;

public class MonadTransformer {
    public static <A, B> Lazy<Optional<B>> bind(Lazy<Optional<A>> m, Function<A, Lazy<Optional<B>>> f) {
        return m.bind(Functional.match(
                /* Just a  */ f,
                /* Nothing */ () -> Lazy.of(Optional::empty)
            ));
    }
}
