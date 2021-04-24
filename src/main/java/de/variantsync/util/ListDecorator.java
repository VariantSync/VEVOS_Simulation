package de.variantsync.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ListDecorator<T> implements List<T> {
    protected final List<T> wrappee;

    public ListDecorator(List<T> list) {
        if (list == null) {
            throw new IllegalArgumentException("Given list cannot be null!");
        }
        this.wrappee = list;
    }

    @Override
    public int size() {
        return wrappee.size();
    }

    @Override
    public boolean isEmpty() {
        return wrappee.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return wrappee.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return wrappee.iterator();
    }

    @Override
    public Object[] toArray() {
        return wrappee.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return wrappee.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return wrappee.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return wrappee.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return wrappee.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return wrappee.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return wrappee.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return wrappee.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return wrappee.retainAll(c);
    }

    @Override
    public void clear() {
        wrappee.clear();
    }

    @Override
    public T get(int index) {
        return wrappee.get(index);
    }

    @Override
    public T set(int index, T element) {
        return wrappee.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        wrappee.add(index, element);
    }

    @Override
    public T remove(int index) {
        return wrappee.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return wrappee.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return wrappee.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return wrappee.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return wrappee.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return wrappee.subList(fromIndex, toIndex);
    }
}
