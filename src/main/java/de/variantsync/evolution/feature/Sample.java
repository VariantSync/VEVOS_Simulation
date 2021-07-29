package de.variantsync.evolution.feature;

import java.util.List;

public record Sample(List<Variant> variants) {
    public static Sample of(final List<Variant> variants) {
        return new Sample(variants);
    }

    public int size() {
        return variants.size();
    }
}
