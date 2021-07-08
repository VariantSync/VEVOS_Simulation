package de.variantsync.evolution.util.fide;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.impl.DefaultFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import de.ovgu.featureide.fm.core.base.impl.Feature;
import de.ovgu.featureide.fm.core.io.Problem;
import de.ovgu.featureide.fm.core.io.ProblemList;
import de.ovgu.featureide.fm.core.io.dimacs.DIMACSFormat;
import de.variantsync.evolution.io.TextIO;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

public class FeatureModelUtils {
    public static IFeatureModel CreateEmptyFeatureModel() {
        return DefaultFeatureModelFactory.getInstance().create();
    }

    public static IFeatureModel FromOptionalFeatures(Collection<String> featureNames) {
        final IFeatureModel fm = CreateEmptyFeatureModel();

        // create artificial root
        final Feature root = new Feature(fm, "Root");
        fm.addFeature(root);
        fm.getStructure().setRoot(root.getStructure());

        for (String featureName : featureNames) {
            final Feature f = new Feature(fm, featureName);
            fm.addFeature(f);
            root.getStructure().addChild(f.getStructure());
        }
        return fm;
    }

    public static IFeatureModel FromOptionalFeatures(String... featureNames) {
        return FromOptionalFeatures(Arrays.asList(featureNames));
    }

    public static IFeatureModel FromDIMACSFile(Path pathToDIMACSFile) throws IOException {
        DefaultFeatureModelFactory factory = DefaultFeatureModelFactory.getInstance();
        IFeatureModel featureModel = factory.create();
        // Register the default factory, so that it can be found by the DIMACSReader
        FMFactoryManager.getInstance().addExtension(factory);
        DIMACSFormat dimacsFormat = new DIMACSFormat();
        try {
            ProblemList problems = dimacsFormat.read(featureModel, Files.readString(pathToDIMACSFile));
            if (problems.size() > 0) {
                Logger.error("DIMACSFormat encountered " + problems.size() + " problems during the parsing of the DIMACS file.");
                for (Problem problem : problems) {
                    Logger.error(problem.toString());
                }
            } else {
                Logger.debug("Feature model parsed successfully from " + pathToDIMACSFile);
            }
            return featureModel;
        } catch (IOException e) {
            Logger.exception("Was not able to load the DIMACS file under " + pathToDIMACSFile, e);
            throw e;
        }
    }
}