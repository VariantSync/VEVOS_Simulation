package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.io.TextIO;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Functional;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;
import org.prop4j.Node;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Represents a variable source code file (e.g., because part of a plugin or only conditionally included).
 */
public class SourceCodeFile extends ArtefactTree<LineBasedAnnotation> {
    private final LineBasedAnnotation rootAnnotation = new LineBasedAnnotation(FixTrueFalse.True, 0, 0);

    public SourceCodeFile(Node featureMapping, CaseSensitivePath relativePath) {
        super(featureMapping, relativePath);
        super.addTrace(rootAnnotation);
    }

    public SourceCodeFile(SourceCodeFile other) {
        this(other.getFeatureMapping(), other.getFile());
    }

    @Override
    public Result<SourceCodeFile, Exception> generateVariant(Variant variant, CaseSensitivePath sourceDir, CaseSensitivePath targetDir) {
        final CaseSensitivePath targetFile = targetDir.resolve(getFile());
        // 1. create the target file
        return Result.FromFlag(
                () -> PathUtils.createEmpty(targetFile.path()),
                () -> new IOException("File already exists!")
        )
        // 2. compute lines to write to disk
        .bind(unit -> {
            final CaseSensitivePath sourceFile = sourceDir.resolve(getFile());
            final List<LineBasedAnnotation> splGroundTruth = rootAnnotation.getLinesToGenerateFor(variant, FixTrueFalse.True);
            return Result.<IOException>Try(() -> TextIO.copyTextLines(sourceFile.path(), targetFile.path(), splGroundTruth)).map(u -> splGroundTruth);
        })
        // 3. translate line numbers from SPL to variant
        .map(splGroundTruth -> {
            // We can mutate the splGroundTruth here as we do not need it anymore. So we can reuse the object for speeeeed.
            LineBasedAnnotation.projectInline(splGroundTruth, variant);
            final SourceCodeFile variantGroundTruth = plainCopy();
            for (LineBasedAnnotation variantAnnotation : splGroundTruth) {
                variantGroundTruth.addTrace(variantAnnotation);
            }
            return variantGroundTruth;
        })
        // 4. log if failure (and implicitly transform IOException to Exception)
        .mapFail((IOException ioexception) -> {
            Logger.exception("Could not create variant file " + targetFile + " because ", ioexception);
            return ioexception;
        });
    }

    @Override
    public void addTrace(LineBasedAnnotation lineBasedAnnotation) {
        rootAnnotation.addTrace(lineBasedAnnotation);
        rootAnnotation.setLineTo(Math.max(rootAnnotation.getLineTo(), lineBasedAnnotation.getLineTo()));
    }

    @Override
    public SourceCodeFile plainCopy() {
        return new SourceCodeFile(this);
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
}
