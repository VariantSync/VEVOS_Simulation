package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;
import org.prop4j.Node;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents a variable source code file (e.g., because part of a plugin or only conditionally included).
 */
public class SourceCodeFile extends Annotated {
    public SourceCodeFile(CaseSensitivePath relativePath, Node featureMapping) {
        super(featureMapping, relativePath);
    }

    @Override
    public Result<Unit, Exception> generateVariant(Variant variant, CaseSensitivePath sourceDir, CaseSensitivePath targetDir) {
        // 1.) create the target file
        final CaseSensitivePath targetFile = targetDir.resolve(getFile());
        final Result<Unit, Exception> result = Result.FromFlag(
                () -> PathUtils.createEmpty(targetFile.path()),
                () -> new IOException("File already exists!")
        );

        if (result.isFailure()) {
            Logger.error("Could not create file " + targetFile + " because ", result.getFailure());
            return result;
        }

        // 2.) write children
        return super.generateVariant(variant, sourceDir, targetDir);
    }

    @Override
    protected void prettyPrintHeader(String indent, StringBuilder builder) {
        builder
                .append(indent)
                .append(getFile())
                .append("<")
                .append(getFeatureMapping())
                .append(">[");
    }

    @Override
    protected void prettyPrintFooter(String indent, StringBuilder builder) {
        builder.append(indent).append("]");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SourceCodeFile that = (SourceCodeFile) o;
        return getFile().equals(that.getFile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getFile());
    }
}
