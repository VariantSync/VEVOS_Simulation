package de.variantsync.evolution.io.data;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.ProblemList;
import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.util.fide.FeatureModelUtils;
import de.variantsync.evolution.util.functional.Result;

import java.io.File;
import java.nio.file.Path;

public class IFeatureModelLoader implements ResourceLoader<IFeatureModel> {
    private static final String EXTENSION = ".dimacs";

    @Override
    public boolean canLoad(Path p) {
        File f = p.toFile();
        return f.exists() && f.getName().endsWith(EXTENSION);
    }

    @Override
    public Result<IFeatureModel, ProblemList> load(Path p) {
        return FeatureModelUtils.FromDIMACSFile(p);
    }
}
