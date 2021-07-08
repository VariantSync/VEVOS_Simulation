package de.variantsync.evolution.util.fide;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.impl.DefaultFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import de.ovgu.featureide.fm.core.base.impl.Feature;
import de.ovgu.featureide.fm.core.io.Problem;
import de.ovgu.featureide.fm.core.io.ProblemList;
import de.ovgu.featureide.fm.core.io.dimacs.DIMACSFormat;
import de.variantsync.evolution.util.functional.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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

    public static Result<IFeatureModel, ProblemList> FromDIMACSFile(Path pathToDIMACSFile) {
        DefaultFeatureModelFactory factory = DefaultFeatureModelFactory.getInstance();
        IFeatureModel featureModel = factory.create();
        // Register the default factory, so that it can be found by the DIMACSReader
        FMFactoryManager.getInstance().addExtension(factory);
        DIMACSFormat dimacsFormat = new DIMACSFormat();
        try {
            ProblemList problemList = dimacsFormat.read(featureModel, Files.readString(pathToDIMACSFile));
            if (problemList.size() > 0) {
                // The feature model is empty if there was a problem during parsing, hence, we return a Failure
                return Result.Failure(problemList);
            } else {
                return Result.Success(featureModel);
            }
        } catch (IOException e) {
            return Result.Failure(new ProblemList(Collections.singleton(new Problem(e))));
        }
    }
}