package org.variantsync.vevos.simulation.io.kernelhaven;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import net.ssehub.kernel_haven.variability_model.JsonVariabilityModelCache;
import org.variantsync.functjonal.Result;
import org.variantsync.vevos.simulation.io.ResourceLoader;
import org.variantsync.vevos.simulation.util.fide.FeatureModelUtils;
import org.variantsync.vevos.simulation.util.io.PathUtils;

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
