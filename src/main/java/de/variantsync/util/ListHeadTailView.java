package de.variantsync.util;

import de.variantsync.util.list.ListDecorator;

import java.util.List;
import java.util.Optional;

/**
 * A view that separates a list into head (first element) and tail (remaining elements).
 * A ListHeadTailView thus shows a sublist of the given list starting at a certain index and ending at the end of
 * the viewed list.
 */
public class ListHeadTailView<T> extends ListDecorator<T> {
    private final int index;

    public ListHeadTailView(List<T> list) {
        this(list, 0);
    }

    public ListHeadTailView(List<T> list, int headIndex) {
        super(list);
        this.index = headIndex;
    }

    public boolean empty() {
        return index >= size();
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

    public ListHeadTailView<T> tail() {
        return new ListHeadTailView<>(wrappee, index + 1);
    }
}
