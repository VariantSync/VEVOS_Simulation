package org.variantsync.vevos.simulation.io.kernelhaven;

import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.impl.DefaultFeatureModelFactory;
import net.ssehub.kernel_haven.variability_model.JsonVariabilityModelCache;
import org.variantsync.functjonal.Result;
import org.variantsync.vevos.simulation.io.ResourceLoader;
import org.variantsync.vevos.simulation.util.fide.FeatureModelUtils;
import org.variantsync.vevos.simulation.util.io.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class VariabilityModelLoader implements ResourceLoader<IFeatureModel> {
    
    @Override
    public boolean canLoad(Path p) {
        return PathUtils.hasExtension(p,".json", ".txt");
    }

    @Override
    public Result<IFeatureModel, ? extends Exception> load(Path p) {
        return Result.Try(() -> {
            if (p.endsWith(".json")) {
                JsonVariabilityModelCache cache = new JsonVariabilityModelCache(p.getParent().toFile());
                return FeatureModelUtils.FromVariabilityModel(cache.readFixed(p.toFile()));
            } else {
                List<String> variables = Files.readAllLines(p).stream().map(String::trim).collect(Collectors.toList());
                return FeatureModelUtils.FromOptionalFeatures(variables);
            }
        });
    }


}
