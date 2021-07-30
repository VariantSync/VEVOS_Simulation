package de.variantsync.evolution.variability.pc;

public record VariantGenerationOptions(boolean exitOnError, boolean ignoreNonExistentSPLFiles) {
    public static final VariantGenerationOptions ExitOnError = new VariantGenerationOptions(true, false);
    public static final VariantGenerationOptions IgnoreErrors = new VariantGenerationOptions(false, true);
    public static final VariantGenerationOptions ExitOnErrorButAllowNonExistentFiles = new VariantGenerationOptions(true, true);
}
