package de.variantsync.util;

import java.util.List;
import java.util.Optional;

public class ListHeadTailView<T> {
    private final List<T> list;
    private final int index;

    public ListHeadTailView(List<T> list) {
        this(list, 0);
    }

    private ListHeadTailView(List<T> list, int index) {
        this.list = list;
        this.index = index;
    }

    public Optional<T> head() {
        if (empty()) {
            return Optional.empty();
        }

        return Optional.of(list.get(index));
    }

    public boolean empty() {
        return index >= list.size();
    }

    public ListHeadTailView<T> tail() {
        return new ListHeadTailView<>(list, index + 1);
    }
}
