package de.variantsync.evolution.io.data;

import java.util.List;

/**
 * Record to represent CSV files row-wise.
 */
public record CSV(List<String[]> rows) {}
