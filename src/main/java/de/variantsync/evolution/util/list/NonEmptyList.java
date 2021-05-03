package de.variantsync.evolution.util.list;

import java.util.Collection;
import java.util.List;

/**
 * A list that holds at least one element.
 * It does not allow removing elements once they are inserted.
 *
 * @param <T> Type of elements that are contained in this list.
 */
public class NonEmptyList<T> extends ListDecorator<T> {
    private final static String ERROR_MESSAGE = "Operation disallowed as it could make this list become empty!";

    public NonEmptyList(List<T> list) {
        super(list);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Given list cannot be empty!");
        }
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }
}
