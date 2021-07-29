package de.variantsync.evolution.util.functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception that can group exceptions as a list.
 */
public class CompositeException extends Exception {
    public static final Monoid<CompositeException> MONOID = Monoid.Create(
            CompositeException::new,
            CompositeException::new
    );
    private final List<Exception> inner;

    /**
     * Wrap the given exception.
     * @param inner Exception to wrap.
     */
    public CompositeException(Exception inner) {
        super(inner.getClass() + ": " + inner.getMessage(), inner.getCause());
        this.inner = new ArrayList<>();
        this.inner.add(inner);
    }

    private CompositeException() {
        super("mEmpty");
        this.inner = new ArrayList<>();
    }

    /**
     * Combine all given exception.
     * @param others Exceptions to combine.
     */
    public CompositeException(CompositeException... others) {
        super(Arrays.stream(others).map(Exception::getMessage).collect(Collectors.joining("\n\n")));
        this.inner = new ArrayList<>();
        this.inner.addAll(Arrays.asList(others));
    }

    public static CompositeException mEmpty() {
        return new CompositeException();
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
