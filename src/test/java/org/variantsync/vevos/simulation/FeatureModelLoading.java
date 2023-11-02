package org.variantsync.vevos.simulation;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelElement;
import org.junit.Assert;
import org.junit.Test;
import org.variantsync.vevos.simulation.variability.SPLCommit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class FeatureModelLoading {

    static {
        VEVOS.Initialize();
    }

    @Test
    public void fromVariables() throws IOException {
        Path variablesPath = Path.of(
                        "src/test/java/org/variantsync/vevos/simulation/variability/TEST_VARIABLES.txt");
        SPLCommit commit = new SPLCommit("aaaaa", null, null,
                        new SPLCommit.FeatureModelPath(variablesPath), null, null, null, null, null, null,
                        null);
        IFeatureModel featureModel = commit.featureModel().run().orElseThrow();
        Collection<String> features = featureModel.getFeatures().stream()
                        .map(IFeatureModelElement::getName).toList();
        Assert.assertEquals(6, features.size());
        for (String feature : Files.readAllLines(variablesPath)) {
            Assert.assertTrue(features.contains(feature));
        }
    }
}
