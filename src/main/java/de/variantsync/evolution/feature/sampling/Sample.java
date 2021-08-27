package de.variantsync.evolution.feature.sampling;

import de.variantsync.evolution.feature.Variant;
import java.util.*;

public record Sample(List<Variant> variants) {
    public static Sample of(final List<Variant> variants) {
        return new Sample(variants);
    }

    public int size() {
        return variants.size();
    }
}
