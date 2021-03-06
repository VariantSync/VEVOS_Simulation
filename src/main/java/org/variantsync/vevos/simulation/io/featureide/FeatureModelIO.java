package org.variantsync.vevos.simulation.io.featureide;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.impl.DefaultFeatureModelFactory;
import de.ovgu.featureide.fm.core.io.IFeatureModelFormat;
import de.ovgu.featureide.fm.core.io.ProblemList;
import de.ovgu.featureide.fm.core.io.manager.SimpleFileHandler;
import org.variantsync.functjonal.Result;
import org.variantsync.functjonal.Unit;
import org.variantsync.vevos.simulation.io.ResourceLoader;
import org.variantsync.vevos.simulation.io.ResourceWriter;
import org.variantsync.vevos.simulation.util.fide.ProblemListUtils;
import org.variantsync.vevos.simulation.util.io.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class FeatureModelIO implements ResourceLoader<IFeatureModel>, ResourceWriter<IFeatureModel> {
    private final IFeatureModelFormat format;

    public FeatureModelIO(final IFeatureModelFormat format) {
        this.format = format;
    }

    @Override
    public boolean canLoad(final Path p) {
        return format.supportsRead() && PathUtils.hasExtension(p, format.getSuffix());
    }

    @Override
    public boolean canWrite(final Path p) {
        return format.supportsWrite() && PathUtils.hasExtension(p, format.getSuffix());
    }

    @Override
    public Result<IFeatureModel, ? extends Exception> load(final Path p) {
        final IFeatureModel featureModel = DefaultFeatureModelFactory.getInstance().create();
        return Result
                .<ProblemList, Exception>Try(() -> format.read(featureModel, Files.readString(p)))
                .bind(problemList -> ProblemListUtils.toResult(
                        problemList,
                        () -> featureModel,
                        () -> "Could not load feature model " + p + ".")
                );
    }

    @Override
    public Result<Unit, ? extends Exception> write(final IFeatureModel model, final Path p) {
        return ProblemListUtils.toResult(
                SimpleFileHandler.save(p, model, format),
                Unit::Instance,
                () -> "Could not write feature model " + model.toString() + " to " + p
        );
    }
}
