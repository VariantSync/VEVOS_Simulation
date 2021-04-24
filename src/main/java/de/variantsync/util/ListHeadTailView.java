package de.variantsync.util;

import java.util.List;
import java.util.Optional;

public class ListHeadTailView<T> extends ListDecorator<T> {
    private final int index;

    public ListHeadTailView(List<T> list) {
        this(list, 0);
    }

    private ListHeadTailView(List<T> list, int index) {
        super(list);
        this.index = index;
    }

    public T head() {
        return get(index);
    }

    public Optional<T> safehead() {
        if (empty()) {
            return Optional.empty();
        }

        return Optional.of(head());
    }

    public boolean empty() {
        return index >= size();
    }

    public ListHeadTailView<T> tail() {
        return new ListHeadTailView<>(wrappee, index + 1);
    }
}
