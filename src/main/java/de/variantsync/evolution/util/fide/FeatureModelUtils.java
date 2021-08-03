package de.variantsync.evolution.util.fide;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelElement;
import de.ovgu.featureide.fm.core.base.IFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.DefaultFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import de.ovgu.featureide.fm.core.base.impl.FactoryManager;
import de.ovgu.featureide.fm.core.base.impl.Feature;
import de.ovgu.featureide.fm.core.io.Problem;
import de.ovgu.featureide.fm.core.io.ProblemList;
import de.ovgu.featureide.fm.core.io.dimacs.DIMACSFormat;
import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Result;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FeatureModelUtils {
    public static IFeatureModel CreateEmptyFeatureModel() {
        return DefaultFeatureModelFactory.getInstance().create();
    }

    public static IFeatureModel FromOptionalFeatures(final Collection<String> featureNames) {
        final IFeatureModel fm = CreateEmptyFeatureModel();

        // create artificial root
        final Feature root = new Feature(fm, "Root");
        fm.addFeature(root);
        fm.getStructure().setRoot(root.getStructure());

        for (final String featureName : featureNames) {
            final Feature f = new Feature(fm, featureName);
            fm.addFeature(f);
            root.getStructure().addChild(f.getStructure());
        }
        return fm;
    }

    public static IFeatureModel FromOptionalFeatures(final String... featureNames) {
        return FromOptionalFeatures(Arrays.asList(featureNames));
    }
    
    public static IFeatureModel FromVariabilityModel(VariabilityModel vm) throws Resources.ResourceIOException {
        Logger.debug("Converting VariabilityModel to FeatureModel.");
        IFeatureModel fm = Resources.Instance().load(IFeatureModel.class, vm.getConstraintModel().toPath());
        Logger.debug("Read FeatureModel from DIMACS");
        Set<String> featureNames = new HashSet<>(vm.getVariableMap().keySet());
        // We only want to add features that are not part of the model yet
        fm.getFeatures().stream().map(IFeatureModelElement::getName).forEach(featureNames::remove);
        Logger.debug("Found " + featureNames.size() + " features that should be added.");
        IFeatureModelFactory factory = FMFactoryManager.getInstance().getFactory(fm);
        // Add all remaining feature from the variability model to the feature model
        featureNames.stream().map(name -> factory.createFeature(fm, name)).forEach(fm::addFeature);
        Logger.debug("Added all feature from VariabilityModel to FeatureModel.");
        return fm;
    }
}