package de.variantsync.evolution.util.fide;

import de.ovgu.featureide.fm.core.base.*;
import de.ovgu.featureide.fm.core.base.impl.DefaultFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import de.ovgu.featureide.fm.core.base.impl.Feature;
import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.util.Logger;
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
    
    public static IFeatureModel FromVariabilityModel(VariabilityModel vm) throws Resources.ResourceIOException, IOException {
        Logger.debug("Converting VariabilityModel to FeatureModel.");
        // The DIMACS file is missing the 'CONFIG_' prefix for every feature, while the VariabilityModel and the PCs are
        // with prefix. We amend the DIMACS file by adding the prefix to each feature.
        amendDimacs(vm.getConstraintModel().toPath());
        
        // Load the initial FeatureModel
        IFeatureModel fm = Resources.Instance().load(IFeatureModel.class, vm.getConstraintModel().toPath());
        Logger.debug("Read FeatureModel from DIMACS");
        
        // Add all missing features
        Set<String> featureNames = new HashSet<>(vm.getVariableMap().keySet());
        // We only want to add features that are not part of the model yet
        fm.getFeatures().stream().map(IFeatureModelElement::getName).forEach(featureNames::remove);
        IFeatureModelFactory factory = FMFactoryManager.getInstance().getFactory(fm);
        IFeature root = fm.getFeature("__Root__");
        // Add all remaining feature from the variability model to the feature model
        featureNames.stream().map(name -> factory.createFeature(fm, name)).forEach(feature -> {
                FeatureUtils.addFeature(fm, feature);
                FeatureUtils.addChild(root, feature);
        });
        Logger.debug("Added all feature from VariabilityModel to FeatureModel.");
        return fm;
    }

    private static void amendDimacs(Path pathToFile) throws IOException {
        final String MEND = "CONFIG_";
        List<String> lines = Files.readAllLines(pathToFile);
        List<String> amendedLines = new ArrayList<>(lines.size());

        for (String line : lines) {
            if (line.trim().startsWith("c")) {
                String[] parts = line.split("\\s+");
                if (!parts[2].startsWith(MEND)) {
                    parts[2] = MEND + parts[2];
                }
                StringBuilder sb = new StringBuilder();
                for (String part : parts) {
                    sb.append(part);
                    sb.append(" ");
                }
                amendedLines.add(sb.toString().trim());
            } else {
                amendedLines.add(line);
            }
        }
        Files.write(pathToFile, amendedLines);
    }
}