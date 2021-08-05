import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelElement;
import de.variantsync.evolution.Main;
import de.variantsync.evolution.util.fide.FeatureModelUtils;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class FeatureModelUtilsTest {
    
    static {
        Main.Initialize();
    }
    
    @Test
    public void simpleModelIntersection() {
        IFeatureModel modelA = FeatureModelUtils.FromOptionalFeatures("A", "B", "C", "D", "E");
        IFeatureModel modelB = FeatureModelUtils.FromOptionalFeatures("B", "C", "E", "F", "G");
        
        IFeatureModel intersection = FeatureModelUtils.IntersectModels(modelA, modelB);
        
        Set<String> featureIntersection = intersection.getFeatures().stream().map(IFeatureModelElement::getName).collect(Collectors.toSet());
        assert featureIntersection.contains("B");
        assert featureIntersection.contains("C");
        assert featureIntersection.contains("E");
        
        assert !featureIntersection.contains("A");
        assert !featureIntersection.contains("D");
        assert !featureIntersection.contains("F");
        assert !featureIntersection.contains("G");
    }

    @Test
    public void simpleModelDifference() {
        IFeatureModel modelA = FeatureModelUtils.FromOptionalFeatures("A", "B", "C", "D", "E");
        IFeatureModel modelB = FeatureModelUtils.FromOptionalFeatures("B", "C", "E", "F", "G");

        Set<String> difference = FeatureModelUtils.getFeatureDifference(modelA, modelB);

        assert !difference.contains("B");
        assert !difference.contains("C");
        assert !difference.contains("E");

        assert difference.contains("A");
        assert difference.contains("D");
        assert difference.contains("F");
        assert difference.contains("G");
    }
}
