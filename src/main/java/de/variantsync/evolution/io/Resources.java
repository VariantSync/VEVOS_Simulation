package de.variantsync.evolution.io;

import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Result;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton manager class to orchestrate resource loading.
 * ResourceLoaders can be registered here to be used upon resource loading.
 */
public class Resources {
    public static class ResourceLoadingFailure extends Exception {
        private ResourceLoadingFailure(String msg) {
            super(msg);
        }
    }

    private final static Resources instance = new Resources();
    private final Map<Class<?>, List<ResourceLoader<?>>> loaders;

    private Resources() {
        loaders = new HashMap<>();
    }

    /**
     * @return Returns a list of all resource loaders that are registered for loading the given type of resource T.
     */
    @SuppressWarnings("unchecked")
    private <T> List<ResourceLoader<T>> getLoaders(Class<T> type) {
        return (List<ResourceLoader<T>>) (Object) loaders.computeIfAbsent(type, k -> new ArrayList<>());
    }

    /**
     * Adds the given loader to this manager such that it will be queried for
     * resource loading when a resource of the given type T is requested by the user via @load.
     */
    public <T> void registerLoader(Class<T> type, ResourceLoader<T> loader) {
        getLoaders(type).add(loader);
    }

    /**
     * Loads the resource at path p as the given type T.
     * @return The loaded resource.
     * @throws ResourceLoadingFailure if no resource loader is registered for loading objects of type T
     *                                or if all resource loaders failed in loading.
     */
    public <T> T load(Class<T> type, Path p) throws ResourceLoadingFailure {
        final List<ResourceLoader<T>> loadersForT = getLoaders(type);

        if (loadersForT.isEmpty()) {
            throw new ResourceLoadingFailure("No ResourceLoader registered for type " + type + " that could parse " + p);
        }

        for (ResourceLoader<T> loader : loadersForT) {
            if (loader.canLoad(p)) {
                Result<T, ?> result = loader.load(p);
                if (result.isSuccess()) {
                    return result.getSuccess();
                } else {
                    Logger.error("ResourceLoader " + loader + " failed: ", result.getFailure());
                }
            }
        }

        throw new ResourceLoadingFailure("All ResourceLoaders failed in loading resource " + p + " as type " + type + "!");
    }

    public static Resources Instance() {
        return instance;
    }
}
