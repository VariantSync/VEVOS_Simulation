import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.Main;
import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.io.kernelhaven.KernelHavenPCLoader;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.fide.ConfigurationUtils;
import de.variantsync.evolution.util.fide.FeatureModelUtils;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;
import de.variantsync.evolution.variability.pc.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Or;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PCLoaderTest {
    private static final Path resDir = Path.of("src", "main", "resources", "test");
    private static final Path testFile = resDir.resolve("KernelHavenPCs.csv");
    private static final Path illTestFile = resDir.resolve("KernelHavenPCs_illformed.csv");
    private static final Path tinySPLDir = resDir.resolve("tinySPLRepo");
    private static final Path genDir = resDir.resolve("gen");

    private static IFeatureModel features;
    private static Artefact expectedTrace;

    private Result<Artefact, Exception> parsedTrace, parsedIllTrace;

    @BeforeClass
    public static void setupStatic() {
        Main.Initialize();

        features = FeatureModelUtils.FromOptionalFeatures("A", "B", "C", "D", "E");
//        System.out.println(features);
//        System.out.println(FeatureUtils.getRoot(features));

        { // Build the expected result by hand.
            final SourceCodeFile alex = new SourceCodeFile(Path.of("src", "Alex.cpp"), FixTrueFalse.True);
            {
                LineBasedAnnotation a = new LineBasedAnnotation(new Literal("A"), 4, 11);
                a.addTrace(new LineBasedAnnotation(new Literal("B"), 6, 8));
                LineBasedAnnotation tru = new LineBasedAnnotation(FixTrueFalse.True, 1, 20);
                tru.addTrace(a);
                tru.addTrace(new LineBasedAnnotation(new Or(new And(new Literal("C"), new Literal("D")), new Literal("E")), 16, 18));
                alex.addTrace(tru);
            }

            final SourceCodeFile bar = new SourceCodeFile(Path.of("src", "foo", "bar.cpp"), new Literal("A"));
            {
                bar.addTrace(new LineBasedAnnotation(FixTrueFalse.False, 1, 5));
            }

            expectedTrace = new ArtefactTree<>(Arrays.asList(alex, bar));
        }
    }

    @Before
    public void setupTest() {
        final KernelHavenPCLoader pcLoader = new KernelHavenPCLoader();
        assert pcLoader.canLoad(testFile);
        assert pcLoader.canLoad(illTestFile);
        parsedTrace = pcLoader.load(testFile);
        parsedIllTrace = pcLoader.load(illTestFile);
    }

    @Test
    public void loadTestFileCorrectly() {
        assert parsedTrace.isSuccess();

        if (!expectedTrace.equals(parsedTrace.getSuccess())) {
            Logger.error("Loaded PCs:\n"
                    + parsedTrace.getSuccess().prettyPrint()
                    + "\nis different from expected result:\n"
                    + expectedTrace.prettyPrint());
            assert false;
        }
    }

    @Test
    public void crashOnIllFormedFile() {
        assert parsedIllTrace.isFailure();
        assert parsedIllTrace.getFailure() instanceof IllegalFeatureTraceSpecification;
    }

    @Test
    public void testGeneration() {
        assert parsedTrace.isSuccess();
        assert PathUtils.deleteDirectory(genDir).isSuccess();
        final Artefact traceToTest = parsedTrace.getSuccess();

        final FeatureModelFormula fmf = new FeatureModelFormula(features);

        final Variant justA = new Variant("justA",
                ConfigurationUtils.FromFeatureModelAndSelection(fmf, Collections.singletonList("A")));
        final Variant justB = new Variant("justB",
                ConfigurationUtils.FromFeatureModelAndSelection(fmf, Collections.singletonList("B")));
        final Variant all = new Variant("all",
                ConfigurationUtils.FromFeatureModelAndSelection(fmf, Arrays.asList("A", "B", "C", "D", "E")));

        final List<Variant> variantsToTest = Arrays.asList(justA, justB, all);
        for (Variant v : variantsToTest) {
            final Result<Unit, Exception> res = traceToTest.project(v, tinySPLDir, genDir.resolve(Path.of("variants", v.getName())));
//System.out.println(traceToTest.prettyPrint());
            if (res.isFailure()) {
                Logger.error(res.getFailure().toString());
            }

            assert res.isSuccess();
        }
    }
}
