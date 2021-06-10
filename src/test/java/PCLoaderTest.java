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
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.config.FeatureIDEConfiguration;
import de.variantsync.evolution.variability.config.SayYesToAllConfiguration;
import de.variantsync.evolution.variability.pc.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Or;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PCLoaderTest {
    private static class PCTestData {
        // init in constructor
        CaseSensitivePath pcs;
        CaseSensitivePath splDir, variantsDir;

        // init static
        Artefact expectedTrace;
        IFeatureModel features;

        // init dynamic
        Result<Artefact, Exception> traces;

        public PCTestData(CaseSensitivePath pcs) {
            this.pcs = pcs;
        }

        public PCTestData(CaseSensitivePath pcs, CaseSensitivePath splDir, CaseSensitivePath variantsDir) {
            this(pcs);
            this.splDir = splDir;
            this.variantsDir = variantsDir;
        }

        public void init(ResourceLoader<Artefact> pcLoader) {
            assert pcLoader.canLoad(pcs.path());
            traces = pcLoader.load(pcs.path());
        }

        public boolean generate(final List<Variant> variantsToTest) {
            traces.assertSuccess();
            PathUtils.deleteDirectory(variantsDir.path()).assertSuccess();
            final Artefact traceToTest = traces.getSuccess();

            for (Variant v : variantsToTest) {
                traceToTest
                        .generateVariant(v, splDir, variantsDir.resolve(v.getName()))
                        .assertSuccess();
            }

            return true;
        }
    }

    private static final CaseSensitivePath resDir = CaseSensitivePath.of("src", "main", "resources", "test");
    private static final CaseSensitivePath genDir = resDir.resolve("gen");

    private static final PCTestData pcTest1 = new PCTestData(
            resDir.resolve("KernelHavenPCs.csv"),
            resDir.resolve("tinySPLRepo"),
            genDir.resolve("tinySPLRepo")
    );
    private static final PCTestData illPcTest = new PCTestData(
            resDir.resolve("KernelHavenPCs_illformed.csv")
    );
    private static final PCTestData linux = new PCTestData(
            CaseSensitivePath.of("..", "variantevolution_datasets", "LinuxVariabilityData", "code-variability.csv"),
            CaseSensitivePath.of("\\\\wsl$","Ubuntu", "home", "bittner"),
            genDir.resolve("linux")
    );

    @BeforeClass
    public static void setupStatic() {
        Main.Initialize();

        /// Init pcTest1
        pcTest1.features = FeatureModelUtils.FromOptionalFeatures("A", "B", "C", "D", "E");
        { // Build the expected result by hand.
            final SourceCodeFile alex = new SourceCodeFile(CaseSensitivePath.of("src", "Alex.cpp"), FixTrueFalse.True);
            {
                LineBasedAnnotation a = new LineBasedAnnotation(new Literal("A"), 4, 11);
                a.addTrace(new LineBasedAnnotation(new Literal("B"), 6, 8));
                LineBasedAnnotation tru = new LineBasedAnnotation(FixTrueFalse.True, 1, 20);
                tru.addTrace(a);
                tru.addTrace(new LineBasedAnnotation(new Or(new And(new Literal("C"), new Literal("D")), new Literal("E")), 16, 18));
                alex.addTrace(tru);
            }

            final SourceCodeFile bar = new SourceCodeFile(CaseSensitivePath.of("src", "foo", "bar.cpp"), new Literal("A"));
            {
                bar.addTrace(new LineBasedAnnotation(FixTrueFalse.False, 1, 5));
            }

            pcTest1.expectedTrace = new ArtefactTree<>(Arrays.asList(alex, bar));
        }

        /// Init linux
        linux.features = FeatureModelUtils.FromOptionalFeatures(
                "CONFIG_IBM_PARTITION",
                "CONFIG_BLOCK"
        );
    }

    @Before
    public void setupTest() {
        final KernelHavenPCLoader pcLoader = new KernelHavenPCLoader();
        pcTest1.init(pcLoader);
        illPcTest.init(pcLoader);
        linux.init(pcLoader);
    }

    @Test
    public void loadTestFileCorrectly() {
        final PCTestData dataToCheck = pcTest1;
        dataToCheck.traces.assertSuccess();

        if (!dataToCheck.expectedTrace.equals(dataToCheck.traces.getSuccess())) {
            Logger.error("Loaded PCs:\n"
                    + dataToCheck.traces.getSuccess().prettyPrint()
                    + "\nis different from expected result:\n"
                    + dataToCheck.expectedTrace.prettyPrint());
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
    public void testLinuxGeneration() {
        final List<Variant> variantsToTest = Arrays.asList(
                new Variant("all", new SayYesToAllConfiguration())
        );

        assert linux.generate(variantsToTest);
    }

    @Test
    public void caseSensitivePathTest() {
        final CaseSensitivePath a = CaseSensitivePath.of("net", "netfilter", "xt_RATEEST.c");
        final CaseSensitivePath b = CaseSensitivePath.of("net", "netfilter", "xt_rateest.c");
        assert !a.equals(b);
    }
}
