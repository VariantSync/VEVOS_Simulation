package org.variantsync.vevos.simulation.variability.pc;

import org.prop4j.Node;
import org.variantsync.functjonal.Functjonal;
import org.variantsync.functjonal.Result;
import org.variantsync.functjonal.category.Traversable;
import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.io.TextIO;
import org.tinylog.Logger;
import org.variantsync.vevos.simulation.util.fide.bugfix.FixTrueFalse;
import org.variantsync.vevos.simulation.util.io.CaseSensitivePath;
import org.variantsync.vevos.simulation.util.io.PathUtils;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.BlockMatching;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.GroundTruth;
import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;
import org.variantsync.vevos.simulation.variability.pc.variantlines.VariantAnnotation;
import org.variantsync.vevos.simulation.variability.pc.visitor.SourceCodeFileVisitorFocus;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.Collections;

/**
 * Represents a variable source code file (e.g., because part of a plugin or only conditionally included).
 */
public class SourceCodeFile extends ArtefactTree<LineBasedAnnotation> {
    private final LineBasedAnnotation rootAnnotation;

    public SourceCodeFile(final Node featureMapping, final CaseSensitivePath relativePath) {
        this(featureMapping, relativePath, new LineBasedAnnotation(FixTrueFalse.True, 1, 1, AnnotationStyle.External));
    }

    private SourceCodeFile(final Node featureMapping, final CaseSensitivePath relativePath, final LineBasedAnnotation root) {
        super(featureMapping, Collections.singletonList(root), relativePath);
        rootAnnotation = root;
    }

    @Override
    public SourceCodeFileVisitorFocus createVisitorFocus() {
        return new SourceCodeFileVisitorFocus(this);
    }

    @Override
    public Result<GroundTruth, Exception> generateVariant(
            final Variant variant,
            final CaseSensitivePath sourceDir,
            final CaseSensitivePath targetDir,
            final VariantGenerationOptions strategy) {
        final CaseSensitivePath sourceFile = sourceDir.resolve(getFile());
        final CaseSensitivePath targetFile = targetDir.resolve(getFile());

        // Check if the source file exists.
        if (!Files.exists(sourceFile.path())) {
            return Result.Failure(new FileNotFoundException("Source file " + sourceFile + " does not exist!"));
        }
        return
                // Create the target file.
                PathUtils.createEmptyAsResult(targetFile.path())
                // Write to target file.
                .bind(unit -> Traversable.sequence(
                        // Compute ground truth for our variant (i.e., make the variant feature-aware
                        rootAnnotation
                                .deriveForVariant(variant)
                                .map(splAnnotationGroundTruth -> {
                    final BlockMatching lineMatching = splAnnotationGroundTruth.matching();
                    // Retrieve all lines of code from the SPL file that should be included in the variant file.
                    final VariantAnnotation variantCode = rootAnnotation.getLinesToCopy(lineMatching::isPresentInVariant);
                    return TextIO
                            // read all lines in the input SPL file
                            .readLines(sourceFile.path())
                            .bind(splLines -> Result.Try(() ->
                                    // write all lines that should be included in the variant to the text file
                                    TextIO.append(
                                            targetFile.path(),
                                            String.join(
                                                    TextIO.LINEBREAK,
                                                    //
                                                    variantCode.project(strategy, splLines)
                                            )
                                    )
                            ))
                            .map(unit2 -> splAnnotationGroundTruth);
                })))
                .bimap(
                        // In case of success, return ground truth.
                        Functjonal.match(
                                splAnnotationGroundTruth -> GroundTruth.forSourceCodeFile(
                                        new SourceCodeFile(getFeatureMapping(), getFile(), splAnnotationGroundTruth.variantArtefact()),
                                        splAnnotationGroundTruth
                                ),
                                () -> GroundTruth.withoutAnnotations(new SourceCodeFile(getFeatureMapping(), getFile()))
                        ),
                        // In case of failure, log it (and implicitly transform IOException to Exception).
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
    public void addTrace(final LineBasedAnnotation lineBasedAnnotation) {
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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final SourceCodeFile that = (SourceCodeFile) o;
        return getFile().equals(that.getFile());
    }
}
