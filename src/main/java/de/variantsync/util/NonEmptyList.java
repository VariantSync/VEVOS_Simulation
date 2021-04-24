package de.variantsync.util;

import java.util.List;

public class NonEmptyList<T> extends ListDecorator<T> {
    public NonEmptyList(List<T> list) {
        super(list);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Given list cannot be empty!");
        }
    }
}
