package org.variantsync.vevos.simulation.variability.pc.options;

import org.variantsync.vevos.simulation.variability.pc.SourceCodeFile;

public record VariantGenerationOptions(
        boolean exitOnError,
        boolean ignoreNonExistentSPLFiles,
        ArtefactFilter<SourceCodeFile> filter
)
{
    public static VariantGenerationOptions ExitOnError(final ArtefactFilter<SourceCodeFile> filter) {
        return new VariantGenerationOptions(true, false, filter);
    }

    public static VariantGenerationOptions IgnoreErrors(final ArtefactFilter<SourceCodeFile> filter) {
        return new VariantGenerationOptions(false, true, filter);
    }

    public static VariantGenerationOptions ExitOnErrorButAllowNonExistentFiles(final ArtefactFilter<SourceCodeFile> filter) {
        return new VariantGenerationOptions(true, true, filter);
    }
}