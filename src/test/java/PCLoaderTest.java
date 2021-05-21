import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.Main;
import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.io.data.CSV;
import de.variantsync.evolution.io.data.CSVLoader;
import de.variantsync.evolution.io.kernelhaven.KernelHavenPCLoader;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.fide.ConfigurationUtils;
import de.variantsync.evolution.util.fide.FeatureModelUtils;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Result;
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

    private static IFeatureModel features;
    private static FeatureTrace expectedTrace;

    private Result<FeatureTrace, Exception> parsedTrace, parsedIllTrace;

    @BeforeClass
    public static void setupStatic() {
        Main.Initialize();

        features = FeatureModelUtils.FromOptionalFeatures("A", "B", "C", "D", "E");
//        System.out.println(features);
//        System.out.println(FeatureUtils.getRoot(features));

        { // Build the expected result by hand.
            final SourceCodeFile alex = new SourceCodeFile(Path.of("src", "Alex.cpp"), FixTrueFalse.True);
            {
                LineBasedAnnotation a = new LineBasedAnnotation(new Literal("A"), 30, 60);
                a.addTrace(new LineBasedAnnotation(new Literal("B"), 50, 55));
                LineBasedAnnotation tru = new LineBasedAnnotation(FixTrueFalse.True, 1, 100);
                tru.addTrace(a);
                tru.addTrace(new LineBasedAnnotation(new Or(new And(new Literal("C"), new Literal("D")), new Literal("E")), 70, 90));
                alex.addTrace(tru);
            }

            final SourceCodeFile bar = new SourceCodeFile(Path.of("src", "foo", "bar.cpp"), new Literal("A"));
            {
                bar.addTrace(new LineBasedAnnotation(FixTrueFalse.False, 1, 20));
            }

            expectedTrace = new FeatureTraceTree<>(Arrays.asList(alex, bar));
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
    public void testProjection1() {
        assert parsedTrace.isSuccess();
        final FeatureTrace traceToTest = parsedTrace.getSuccess();

        final FeatureModelFormula fmf = new FeatureModelFormula(features);

        final Variant justA = new Variant("justA",
                ConfigurationUtils.FromFeatureModelAndSelection(fmf, Collections.singletonList("A")));
        final Variant justB = new Variant("justB",
                ConfigurationUtils.FromFeatureModelAndSelection(fmf, Collections.singletonList("B")));
        final Variant all = new Variant("all",
                ConfigurationUtils.FromFeatureModelAndSelection(fmf, Arrays.asList("A", "B", "C", "D", "E")));

        final List<Variant> variantsToTest = Arrays.asList(justA, justB, all);

//        System.out.println("original:");
//        System.out.println(traceToTest.prettyPrint("  "));
        for (Variant v : variantsToTest) {
            final FeatureTrace projection = traceToTest.project(v);
//            System.out.println("Showing projection to variant " + v.toString().replaceAll("\\n", ", "));
//            System.out.println("projection:");
//            System.out.println(projection.prettyPrint("  "));

            // TODO: Actually we have to build the expected projection manually here.
            //       Otherwise this is an always successful test iff loadTestFileCorrectly was successful.
            assert projection.equals(expectedTrace.project(v));
        }
    }
}
