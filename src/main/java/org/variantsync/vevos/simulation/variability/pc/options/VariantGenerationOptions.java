package org.variantsync.vevos.simulation.variability.pc.options;

import org.variantsync.vevos.simulation.variability.pc.SourceCodeFile;

public record VariantGenerationOptions(
        boolean exitOnError,
        boolean ignoreNonExistentSPLFiles,
        boolean withMacros,
        ArtefactFilter<SourceCodeFile> filter
)
{
    public static VariantGenerationOptions ExitOnError(final boolean withMacros, final ArtefactFilter<SourceCodeFile> filter) {
        return new VariantGenerationOptions(true, false, withMacros, filter);
    }

    public static VariantGenerationOptions ExitOnErrorButAllowNonExistentFiles(final boolean withMacros, final ArtefactFilter<SourceCodeFile> filter) {
        return new VariantGenerationOptions(true, true, withMacros, filter);
    }
}
