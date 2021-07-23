package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.io.TextIO;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Traversable;
import de.variantsync.evolution.variability.pc.visitor.SourceCodeFileVisitorFocus;
import org.prop4j.Node;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a variable source code file (e.g., because part of a plugin or only conditionally included).
 */
public class SourceCodeFile extends ArtefactTree<LineBasedAnnotation> {
    private final LineBasedAnnotation rootAnnotation;

    public SourceCodeFile(Node featureMapping, CaseSensitivePath relativePath) {
        this(featureMapping, relativePath, new LineBasedAnnotation(FixTrueFalse.True, 1, 1, false));
    }

    private SourceCodeFile(Node featureMapping, CaseSensitivePath relativePath, LineBasedAnnotation root) {
        super(featureMapping, Collections.singletonList(root), relativePath);
        rootAnnotation = root;
    }

    private static SourceCodeFile fromGroundTruth(SourceCodeFile other, Optional<GroundTruth> root) {
        return root
                .map(r -> new SourceCodeFile(other.getFeatureMapping(), other.getFile(), r.variantArtefact()))
                .orElseGet(() -> new SourceCodeFile(other.getFeatureMapping(), other.getFile()));
    }

    @Override
    public SourceCodeFileVisitorFocus createVisitorFocus() {
        return new SourceCodeFileVisitorFocus(this);
    }

    @Override
    public Result<SourceCodeFile, Exception> generateVariant(Variant variant, CaseSensitivePath sourceDir, CaseSensitivePath targetDir) {
        final CaseSensitivePath targetFile = targetDir.resolve(getFile());
        final Result<Optional<GroundTruth>, IOException> groundTruth =
                // 1. Create the target file.
                PathUtils.createEmptyAsResult(targetFile.path())
                // 2. Write to target file.
                .bind(unit -> Traversable.sequence(rootAnnotation.toVariant(variant).map(splGroundTruth -> {
                    final BlockMatching lineMatching = splGroundTruth.matching();
                    // only write lines of blocks that are part of our variant
                    final List<Integer> lines = rootAnnotation.getAllLinesFor(lineMatching::isPresentInVariant);
                    final CaseSensitivePath sourceFile = sourceDir.resolve(getFile());
                    return Result.Try(
                            () -> {
                                TextIO.copyTextLines(sourceFile.path(), targetFile.path(), lines);
                                return splGroundTruth;
                            });
                })));
        return groundTruth.bimap(
                // 3. In case of success, return ground truth.
                maybeGroundTruth -> fromGroundTruth(this, maybeGroundTruth),
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
