package de.variantsync.evolution.io;

import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Result;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Resources {
    private final static Resources instance = new Resources();
    private final Map<Class<?>, List<ResourceLoader<?>>> loaders;

    private Resources() {
        loaders = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private <T> List<ResourceLoader<T>> getLoaders(Class<T> type) {
        return (List<ResourceLoader<T>>) (Object) loaders.computeIfAbsent(type, k -> new ArrayList<>());
    }

    public <T> void registerLoader(Class<T> type, ResourceLoader<T> loader) {
        getLoaders(type).add(loader);
    }

    public <T> T load(Class<T> type, Path p) {
        final List<ResourceLoader<T>> loadersForT = getLoaders(type);

        if (loadersForT.isEmpty()) {
            throw new RuntimeException("No ResourceLoader registered for type " + type + " that could parse " + p);
        }

        for (ResourceLoader<T> loader : loadersForT) {
            if (loader.canLoad(p)) {
                Result<T, Exception> result = loader.load(p);
                if (result.isSuccess()) {
                    return result.getSuccess();
                } else {
                    Logger.exception("ResourceLoader " + loader + " failed: ", result.getFailure());
                }
            }
        }

        throw new RuntimeException("All ResourceLoaders failed in loading resource " + p + " as type " + type + "!");
    }

    public static Resources Instance() {
        return instance;
    }
}
