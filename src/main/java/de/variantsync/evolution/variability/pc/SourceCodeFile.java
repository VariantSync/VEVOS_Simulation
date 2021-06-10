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
        final Result<Boolean, IOException> r = Result.Try(() -> PathUtils.createEmpty(targetFile.path()));
        if (r.isFailure()) {
            return Result.Failure(r.getFailure());
        } else if (!r.getSuccess()) {
            return Result.Failure(new IOException("Could not create file " + targetFile));
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
