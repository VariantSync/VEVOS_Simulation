package de.variantsync.feature;

import java.util.List;

public record Sample(List<Variant> variants) {
    public static Sample of(List<Variant> variants) {
        return new Sample(variants);
    }

    public int size() {
        return variants.size();
    }
}
