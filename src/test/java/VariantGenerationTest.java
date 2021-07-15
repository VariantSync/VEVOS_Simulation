import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.Main;
import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.io.kernelhaven.KernelHavenPCLoader;
import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.fide.FeatureModelUtils;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Functional;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.config.FeatureIDEConfiguration;
import de.variantsync.evolution.variability.config.SayYesToAllConfiguration;
import de.variantsync.evolution.variability.pc.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Or;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for presence condition loading and variant generation.
 */
public class VariantGenerationTest {
    private static class TestCaseData {
        // init in constructor
        CaseSensitivePath pcs, splDir, variantsDir;
        IFeatureModel features;

        // init dynamic
        Result<Artefact, ?> traces;

        public TestCaseData(CaseSensitivePath pcs) {
            this.pcs = pcs;
            assert pcLoader.canLoad(pcs.path());
            traces = pcLoader.load(pcs.path());
        }

        public TestCaseData(CaseSensitivePath pcs, CaseSensitivePath splDir, CaseSensitivePath variantsDir) {
            this(pcs);
            this.splDir = splDir;
            this.variantsDir = variantsDir;
        }

        public TestCaseData(CaseSensitivePath pcs, CaseSensitivePath splDir, CaseSensitivePath variantsDir, IFeatureModel fm) {
            this(pcs, splDir, variantsDir);
            this.features = fm;
        }

        public boolean generate(final List<Variant> variantsToTest) {
            traces.assertSuccess();
            PathUtils.deleteDirectory(variantsDir.path()).assertSuccess();
            final Artefact traceToTest = traces.getSuccess();

            for (Variant v : variantsToTest) {
                traceToTest
                        .generateVariant(v, splDir, variantsDir.resolve(v.getName()))
                        .map(Functional.performSideEffect(groundTruth -> System.out.println(groundTruth.prettyPrint())))
                        .assertSuccess();
            }

            return true;
        }
    }

    private static final ResourceLoader<Artefact> pcLoader = new KernelHavenPCLoader();

    private static final CaseSensitivePath resDir = CaseSensitivePath.of("src", "main", "resources", "test");
    private static final CaseSensitivePath genDir = resDir.resolve("gen");
    private static final CaseSensitivePath datasetsDir = CaseSensitivePath.of("..", "variantevolution_datasets");

    private static TestCaseData pcTest1;
    private static TestCaseData illPcTest;
    private static TestCaseData linuxSample;
    private static TestCaseData linux;

    @BeforeClass
    public static void setupStatic() {
        Main.Initialize();

        pcTest1 = new TestCaseData(
                resDir.resolve("KernelHavenPCs.csv"),
                resDir.resolve("tinySPLRepo"),
                genDir.resolve("tinySPLRepo"),
                FeatureModelUtils.FromOptionalFeatures("A", "B", "C", "D", "E")
        );
        illPcTest = new TestCaseData(
                resDir.resolve("KernelHavenPCs_illformed.csv")
        );
        linuxSample = new TestCaseData(
                resDir.resolve("LinuxPCS_Simple.csv"),
                datasetsDir.resolve("linux"),
                genDir.resolve("linux-sample")
        );
        linux = new TestCaseData(
                datasetsDir.resolve("LinuxVariabilityData", "code-variability.csv"),
                datasetsDir.resolve("linux"),
                genDir.resolve("linux")
        );
    }

    @Test
    public void loadPCTest1FileCorrectly() {
        final TestCaseData dataToCheck = pcTest1;
        dataToCheck.traces.assertSuccess();

        Artefact expectedTrace;
        { // Build the expected result by hand.
            final SourceCodeFile alex = new SourceCodeFile(FixTrueFalse.True, CaseSensitivePath.of("src", "Alex.cpp"));
            {
                LineBasedAnnotation a = new LineBasedAnnotation(new Literal("A"), 4, 11);
                a.addTrace(new LineBasedAnnotation(new Literal("B"), 6, 8));
                LineBasedAnnotation tru = new LineBasedAnnotation(FixTrueFalse.True, 0, 22);
                tru.addTrace(a);
                tru.addTrace(new LineBasedAnnotation(new Or(new And(new Literal("C"), new Literal("D")), new Literal("E")), 16, 18));
                alex.addTrace(tru);
            }

            final SourceCodeFile bar = new SourceCodeFile(new Literal("A"), CaseSensitivePath.of("src", "foo", "bar.cpp"));
            {
                bar.addTrace(new LineBasedAnnotation(FixTrueFalse.False, 0, 5));
            }

            expectedTrace = new ArtefactTree<>(Arrays.asList(alex, bar));
        }

        if (!expectedTrace.equals(dataToCheck.traces.getSuccess())) {
            Logger.error("Loaded PCs:\n"
                    + dataToCheck.traces.getSuccess().prettyPrint()
                    + "\nis different from expected result:\n"
                    + expectedTrace.prettyPrint());
            assert false;
        }
    }

    @Test
    public void crashOnIllFormedFile() {
        assert illPcTest.traces.isFailure();
        assert illPcTest.traces.getFailure() instanceof IllegalFeatureTraceSpecification;
    }

    @Test
    public void testGeneration() {
        final FeatureModelFormula fmf = new FeatureModelFormula(pcTest1.features);
        assert pcTest1.generate(Arrays.asList(
                new Variant("justA", new FeatureIDEConfiguration(fmf, Collections.singletonList("A"))),
                new Variant("justB", new FeatureIDEConfiguration(fmf, Collections.singletonList("B"))),
                new Variant("all", new FeatureIDEConfiguration(fmf, Arrays.asList("A", "B", "C", "D", "E")))
        ));
    }

    @Test
    public void testLinuxSampleGeneration() {
        assert linuxSample.generate(Arrays.asList(
                new Variant("all", new SayYesToAllConfiguration())
        ));
    }

    @Test
    public void testLinuxGeneration() {
        assert linux.generate(Arrays.asList(
                new Variant("all", new SayYesToAllConfiguration())
        ));
    }

    @Test
    public void caseSensitivePathTest() {
        final CaseSensitivePath sharedRoot = CaseSensitivePath.of("net", "netfilter");
        final CaseSensitivePath a = sharedRoot.resolve("xt_RATEEST.c");
        final CaseSensitivePath b = sharedRoot.resolve("xt_rateest.c");
        assert !a.equals(b);
    }
}
