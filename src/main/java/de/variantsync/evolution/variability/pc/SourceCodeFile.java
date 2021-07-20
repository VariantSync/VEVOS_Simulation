package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.io.TextIO;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.pc.visitor.SourceCodeFileVisitorFocus;
import org.prop4j.Node;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Represents a variable source code file (e.g., because part of a plugin or only conditionally included).
 */
public class SourceCodeFile extends ArtefactTree<LineBasedAnnotation> {
    private final LineBasedAnnotation rootAnnotation;

    public SourceCodeFile(Node featureMapping, CaseSensitivePath relativePath) {
        super(featureMapping, Collections.singletonList(new LineBasedAnnotation(FixTrueFalse.True, 1, 1)), relativePath);
        rootAnnotation = getSubtrees().get(0);
    }

    public SourceCodeFile(SourceCodeFile other) {
        this(other.getFeatureMapping(), other.getFile());
    }

    @Override
    public SourceCodeFileVisitorFocus createVisitorFocus() {
        return new SourceCodeFileVisitorFocus(this);
    }

    @Override
    public Result<SourceCodeFile, Exception> generateVariant(Variant variant, CaseSensitivePath sourceDir, CaseSensitivePath targetDir) {
        final CaseSensitivePath targetFile = targetDir.resolve(getFile());
        // 1. Create the target file.
        return PathUtils.createEmptyAsResult(targetFile.path())
        // 2. Write to target file.
        .bind(unit -> {
            final CaseSensitivePath sourceFile = sourceDir.resolve(getFile());
            final List<LineBasedAnnotation> splGroundTruth = rootAnnotation.getLinesToGenerateFor(variant, FixTrueFalse.True);
            return Result.<IOException>Try(() -> TextIO.copyTextLines(sourceFile.path(), targetFile.path(), splGroundTruth)).map(u -> splGroundTruth);
        })
        .bimap(
                // 3. In case of success, translate line numbers from SPL to variant and return ground truth.
                splGroundTruth -> {
                    // We can mutate the splGroundTruth here as we do not need it anymore. So we can reuse the object for speeeeed.
                    LineBasedAnnotation.convertSPLLineNumbersToVariantLineNumbers(splGroundTruth);
                    // Return a copy of this subtree as ground truth.
                    final SourceCodeFile variantGroundTruth = plainCopy();
                    variantGroundTruth.addTraces(splGroundTruth);
                    return variantGroundTruth;
                },
                // 4. In case of failure, log it (and implicitly transform IOException to Exception).
                ioexception -> {
                    Logger.error("Could not create variant file " + targetFile + " because ", ioexception);
                    return ioexception;
                }
        );
    }

    public LineBasedAnnotation getRootAnnotation() {
        return rootAnnotation;
    }

    public void simplify() {
        rootAnnotation.simplify();
    }

    @Override
    public void addTrace(LineBasedAnnotation lineBasedAnnotation) {
        rootAnnotation.addTrace(lineBasedAnnotation);
        rootAnnotation.setLineTo(Math.max(rootAnnotation.getLineTo(), lineBasedAnnotation.getLineTo()));
    }

    public SourceCodeFile plainCopy() {
        return new SourceCodeFile(this);
    }

    @Override
    public String toString() {
        return "SourceCodeFile{" +
                "featureMapping=" + getFeatureMapping() +
                ", path=" + getFile() +
                '}';
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
