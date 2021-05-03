package de.variantsync.evolution.util.list;

import java.util.Collection;
import java.util.List;

public class ImmutableList<T> extends ListDecorator<T> {
    private final static String ERROR_MESSAGE = "List is immutable.";

    public ImmutableList(List<T> list) {
        super(list);
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
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
    public T set(int index, T element) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }
}
