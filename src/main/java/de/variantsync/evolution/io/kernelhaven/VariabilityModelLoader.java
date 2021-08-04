package de.variantsync.evolution.io.kernelhaven;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.fide.FeatureModelUtils;
import de.variantsync.evolution.util.functional.Result;
import net.ssehub.kernel_haven.variability_model.JsonVariabilityModelCache;

import java.nio.file.Path;

public class VariabilityModelLoader implements ResourceLoader<IFeatureModel> {
    
    @Override
    public boolean canLoad(Path p) {
        return PathUtils.hasExtension(p,".json");
    }

    @Override
    public Result<IFeatureModel, ? extends Exception> load(Path p) {
        return Result.Try(() -> {
            JsonVariabilityModelCache cache = new JsonVariabilityModelCache(p.getParent().toFile());
            return FeatureModelUtils.FromVariabilityModel(cache.readFixed(p.toFile()));
        });
    }


}
