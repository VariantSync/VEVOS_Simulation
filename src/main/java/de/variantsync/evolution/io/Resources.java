package de.variantsync.evolution.io;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.io.data.CSV;
import de.variantsync.evolution.io.data.CSVIO;
import de.variantsync.evolution.io.data.DimacsFeatureModelLoader;
import de.variantsync.evolution.io.kernelhaven.KernelHavenSPLPCIO;
import de.variantsync.evolution.io.kernelhaven.KernelHavenVariantPCIO;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;
import de.variantsync.evolution.variability.pc.Artefact;

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
    public static class ResourceIOException extends Exception {
        private ResourceIOException(final String msg) {
            super(msg);
        }
    }

    private final static Resources instance = new Resources();
    private final Map<Class<?>, List<ResourceLoader<?>>> loaders;
    private final Map<Class<?>, List<ResourceWriter<?>>> writers;

    private Resources() {
        loaders = new HashMap<>();
        writers = new HashMap<>();
        registerDefaultIOAt(this);
    }

    private static <T, U> List<U> lookup(final Class<T> type, final Map<Class<?>, List<U>> map) {
        return map.computeIfAbsent(type, k -> new ArrayList<>());
    }

    private static void registerDefaultIOAt(final Resources r) {
        r.registerLoader(IFeatureModel.class, new DimacsFeatureModelLoader());

        final CSVIO CSVIO = new CSVIO();
        r.registerLoader(CSV.class, CSVIO);
        r.registerWriter(CSV.class, CSVIO);

        final KernelHavenSPLPCIO splPCIO = new KernelHavenSPLPCIO();
        r.registerLoader(Artefact.class, splPCIO);
        r.registerWriter(Artefact.class, splPCIO);

        final KernelHavenVariantPCIO variantSPLIO = new KernelHavenVariantPCIO();
        r.registerLoader(Artefact.class, variantSPLIO);
        r.registerWriter(Artefact.class, variantSPLIO);
    }

    /**
     * @return Returns a list of all resource loaders that are registered for loading the given type of resource T.
     */
    @SuppressWarnings("unchecked")
    private <T> List<ResourceLoader<T>> getLoaders(final Class<T> type) {
        return (List<ResourceLoader<T>>) (Object) lookup(type, loaders);
    }

    /**
     * @return Returns a list of all resource writers that are registered for writing the given type of resource T.
     */
    @SuppressWarnings("unchecked")
    private <T> List<ResourceWriter<T>> getWriters(final Class<T> type) {
        return (List<ResourceWriter<T>>) (Object) lookup(type, writers);
    }

    /**
     * Adds the given loader to this manager such that it will be queried for
     * resource loading when a resource of the given type T is requested by the user via @load.
     */
    public <T> void registerLoader(final Class<T> type, final ResourceLoader<T> loader) {
        getLoaders(type).add(loader);
    }

    /**
     * Adds the given writer to this manager such that it will be queried for
     * resource writing when a resource of the given type T is given by the user via @write.
     */
    public <T> void registerWriter(final Class<T> type, final ResourceWriter<T> writer) {
        getWriters(type).add(writer);
    }

    /**
     * Loads the resource at path p as the given type T.
     * @return The loaded resource.
     * @throws ResourceIOException if no resource loader is registered for loading objects of type T
     *                                or if all resource loaders failed in loading.
     */
    public <T> T load(final Class<T> type, final Path p) throws ResourceIOException {
        final List<ResourceLoader<T>> loadersForT = getLoaders(type);

        if (loadersForT.isEmpty()) {
            throw new ResourceIOException("No ResourceLoader registered for type " + type + " that can parse " + p);
        }

        for (final ResourceLoader<T> loader : loadersForT) {
            if (loader.canLoad(p)) {
                final Result<T, ? extends Exception> result = loader.load(p);
                if (result.isSuccess()) {
                    return result.getSuccess();
                } else {
                    Logger.error("ResourceLoader " + loader + " failed: ", result.getFailure());
                }
            }
        }

        throw new ResourceIOException("All ResourceLoaders failed in loading resource " + p + " as type " + type + "!");
    }

    public <T> void write(final Class<T> type, final T object, final Path p) throws ResourceIOException {
        final List<ResourceWriter<T>> writersForT = getWriters(type);

        if (writersForT.isEmpty()) {
            throw new ResourceIOException("No ResourceWriter registered for type " + type + " that can write " + p);
        }

        for (final ResourceWriter<T> writer : writersForT) {
            if (writer.canWrite(p)) {
                final Result<Unit, ? extends Exception> result = writer.write(object, p);
                if (result.isSuccess()) {
                    return;
                } else {
                    Logger.error("ResourceWriter " + writer + " failed: ", result.getFailure());
                }
            }
        }

        throw new ResourceIOException("All ResourceWriters failed in writing resource " + p + " as type " + type + "!");
    }

    public static Resources Instance() {
        return instance;
    }
}
