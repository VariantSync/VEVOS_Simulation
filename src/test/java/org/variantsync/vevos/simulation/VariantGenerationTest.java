package org.variantsync.vevos.simulation;

import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Or;
import org.tinylog.Logger;
import org.tinylog.Supplier;
import org.variantsync.functjonal.Result;
import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.feature.config.FeatureIDEConfiguration;
import org.variantsync.vevos.simulation.feature.config.IConfiguration;
import org.variantsync.vevos.simulation.feature.sampling.FeatureIDESampler;
import org.variantsync.vevos.simulation.feature.sampling.Sampler;
import org.variantsync.vevos.simulation.io.ResourceLoader;
import org.variantsync.vevos.simulation.io.Resources;
import org.variantsync.vevos.simulation.io.TextIO;
import org.variantsync.vevos.simulation.io.kernelhaven.KernelHavenSPLPCIO;
import org.variantsync.vevos.simulation.sat.SAT;
import org.variantsync.vevos.simulation.util.fide.FeatureModelUtils;
import org.variantsync.vevos.simulation.util.fide.bugfix.FixTrueFalse;
import org.variantsync.vevos.simulation.util.io.CaseSensitivePath;
import org.variantsync.vevos.simulation.util.io.PathUtils;
import org.variantsync.vevos.simulation.variability.pc.*;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.LineType;
import org.variantsync.vevos.simulation.variability.pc.options.ArtefactFilter;
import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;

import java.io.IOException;
import java.nio.file.Files;
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
        Result<Artefact, Exception> traces;

        public TestCaseData(final CaseSensitivePath pcs) {
            this.pcs = pcs;
            assert splPCLoader.canLoad(pcs.path());
            traces = splPCLoader.load(pcs.path());
        }

        public TestCaseData(final CaseSensitivePath pcs, final CaseSensitivePath splDir, final CaseSensitivePath variantsDir) {
            this(pcs);
            this.splDir = splDir;
            this.variantsDir = variantsDir;
        }

        public TestCaseData(final CaseSensitivePath pcs, final CaseSensitivePath splDir, final CaseSensitivePath variantsDir, final IFeatureModel fm) {
            this(pcs, splDir, variantsDir);
            this.features = fm;
        }

        public boolean generate(final List<Variant> variantsToTest, final boolean writeConfigs) {
            traces.assertSuccess();
            PathUtils.deleteDirectory(variantsDir.path()).assertSuccess();
            final Artefact traceToTest = traces.getSuccess();

//            System.out.println("=== [SPL] ===");
//            System.out.println(traceToTest.prettyPrint());

            for (final Variant v : variantsToTest) {
                traceToTest
                        .generateVariant(v, splDir,
                                variantsDir.resolve(v.getName()),
                                VariantGenerationOptions.ExitOnErrorButAllowNonExistentFiles(false, ArtefactFilter.KeepAll()))
                        // Write ground truth
                        .bind(groundTruth -> Result.Try(() -> Resources.Instance().write(
                                Artefact.class,
                                groundTruth.variant(),
                                variantsDir.resolve(v.getName()).resolve("ground_truth.variant.csv").path())))
                        // Write configuration
                        .bind(unit -> {
                            if (writeConfigs) {
                                return Result.Try(() -> Resources.Instance().write(
                                        IConfiguration.class,
                                        v.getConfiguration(),
                                        variantsDir.resolve(v.getName()).resolve("configuration.xml").path()));
                            }

                            return Result.Success(unit);
                        })
                        .assertSuccess();
            }

            return true;
        }
    }

    private static final KernelHavenSPLPCIO splPCLoader = new KernelHavenSPLPCIO();

    private static final CaseSensitivePath resDir = CaseSensitivePath.of("src", "test", "resources", "variantgeneration");
    private static CaseSensitivePath genDir;
    // private static final CaseSensitivePath datasetsDir = CaseSensitivePath.of("..", "variantevolution_datasets");

    private static TestCaseData pcTest1;
    private static TestCaseData illPcTest;
    // private static TestCaseData linuxSample;
    // private static TestCaseData linux;

    @BeforeClass
    public static void setupStatic() throws IOException {
        VEVOS.Initialize();
        Result.HARD_CRASH_ON_TRY = true;
        genDir = new CaseSensitivePath(Files.createTempDirectory("gen"));

        pcTest1 = new TestCaseData(
                resDir.resolve("KernelHavenPCs.spl.csv"),
                resDir.resolve("tinySPLRepo"),
                genDir.resolve("tinySPLRepo"),
                FeatureModelUtils.FromOptionalFeatures("A", "B", "C", "D", "E")
        );
        illPcTest = new TestCaseData(
                resDir.resolve("KernelHavenPCs_illformed.spl.csv")
        );

        // TODO: Fix broken tests
        // linuxSample = new TestCaseData(
        //         resDir.resolve("LinuxPCS_Simple.spl.csv"),
        //         datasetsDir.resolve("linux"),
        //         genDir.resolve("linux-sample")
        // );
        // linux = new TestCaseData(
        //         datasetsDir.resolve("LinuxVariabilityData", "code-variability.spl.csv"),
        //         datasetsDir.resolve("linux"),
        //         genDir.resolve("linux")
        // );
    }

    @Test
    public void loadPCTest1FileCorrectly() {
        final TestCaseData dataToCheck = pcTest1;
        dataToCheck.traces.assertSuccess();

        final Artefact expectedTrace;
        { // Build the expected result by hand.
            final SourceCodeFile foofoo = new SourceCodeFile(FixTrueFalse.True, FixTrueFalse.True, CaseSensitivePath.of("src", "FooFoo.cpp"));
            {
                final LineBasedAnnotation a1 = new LineBasedAnnotation(new Literal("A"),new Literal("A"), LineType.IF, 4, 4, AnnotationStyle.Internal);
                final LineBasedAnnotation a2 = new LineBasedAnnotation(new Literal("A"),new Literal("A"), LineType.ARTIFACT, 5, 5, AnnotationStyle.Internal);
                final LineBasedAnnotation a3 = new LineBasedAnnotation(new Literal("A"),new Literal("A"), LineType.ARTIFACT, 9, 10, AnnotationStyle.Internal);
                final LineBasedAnnotation a4 = new LineBasedAnnotation(new Literal("A"),new Literal("A"), LineType.ENDIF, 11, 11, AnnotationStyle.Internal);

                Supplier<Node> generateB = () -> {
                    Node left = new Literal("A");
                    Node right = new Literal("B");
                    return new And(left, right);
                };
                final LineBasedAnnotation b1 = new LineBasedAnnotation(new Literal("B"), generateB.get(), LineType.IF, 6, 6, AnnotationStyle.Internal);
                final LineBasedAnnotation b2 = new LineBasedAnnotation(new Literal("B"), generateB.get(), LineType.ARTIFACT, 7, 7, AnnotationStyle.Internal);
                final LineBasedAnnotation b3 = new LineBasedAnnotation(new Literal("B"), generateB.get(), LineType.ENDIF, 8, 8, AnnotationStyle.Internal);
                final LineBasedAnnotation tru = new LineBasedAnnotation(FixTrueFalse.True, FixTrueFalse.True, LineType.ROOT, 1, 21, AnnotationStyle.External);
                tru.addTrace(a1);
                tru.addTrace(a2);
                tru.addTrace(b1);
                tru.addTrace(b2);
                tru.addTrace(b3);
                tru.addTrace(a3);
                tru.addTrace(a4);

                Supplier<Node> generateC = () -> new Or(new And(new Literal("C"), new Literal("D")), new Literal("E"));
                tru.addTrace(new LineBasedAnnotation(generateC.get(), generateC.get(), LineType.IF, 16, 16, AnnotationStyle.Internal));
                tru.addTrace(new LineBasedAnnotation(generateC.get(), generateC.get(), LineType.ARTIFACT, 17, 17, AnnotationStyle.Internal));
                tru.addTrace(new LineBasedAnnotation(generateC.get(), generateC.get(), LineType.ENDIF, 18, 18, AnnotationStyle.Internal));
                foofoo.addTrace(tru);
            }

            final SourceCodeFile bar = new SourceCodeFile(new Literal("A"), new Literal("A"), CaseSensitivePath.of("src", "foo", "bar.cpp"));
            {
                // This is a challenging case for the importer.
                // We can not differentiate if a block starting at line 1 is an external annotation by Kernelhaven or an actual macro.
                bar.addTrace(new LineBasedAnnotation(FixTrueFalse.False, FixTrueFalse.False, LineType.ROOT, 1, 4, AnnotationStyle.Internal));
            }

            expectedTrace = new SyntheticArtefactTreeNode<>(Arrays.asList(foofoo, bar));
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

    private static void readFromAndDirectlyWriteTo(final CaseSensitivePath inputPath, final CaseSensitivePath outputPath) throws Resources.ResourceIOException {
        // load pcs
        Logger.info("Reading " + inputPath);
        final Artefact pcs = Resources.Instance().load(Artefact.class, inputPath.path());

        // write pcs unmodified
        Logger.info("Writing " + outputPath);
        Resources.Instance().write(Artefact.class, pcs, outputPath.path());
    }

    @Test
    public void idempotentReadWriteOfPCFiles() throws Resources.ResourceIOException, IOException {
        // TODO: Fix broken test cases
        // final List<TestCaseData> testCases = Arrays.asList(pcTest1, linuxSample);
        final List<TestCaseData> testCases = Arrays.asList(pcTest1);
        for (final TestCaseData testCase : testCases) {
            final CaseSensitivePath sourcePath = testCase.pcs;
            final CaseSensitivePath intermediatePath = genDir.resolve(sourcePath.path().getFileName());
            final CaseSensitivePath outputPath = genDir.resolve(sourcePath.path().getFileName() + ".idempotent.spl.csv");

            PathUtils.deleteDirectory(intermediatePath.path());
            PathUtils.deleteDirectory(outputPath.path());

            readFromAndDirectlyWriteTo(sourcePath, intermediatePath);
            readFromAndDirectlyWriteTo(intermediatePath, outputPath);

            // assert that text at intermediatePath is the same as at outputPath
            Assert.assertEquals(TextIO.readAsString(intermediatePath.path()), TextIO.readAsString(outputPath.path()));
        }
    }

    @Test
    public void testPCQuery() {
        if (pcTest1.traces.isFailure()) {
            throw new RuntimeException(pcTest1.traces.getFailure());
        }
        final Result<Node, Exception> result =
                pcTest1.traces.getSuccess().getPresenceConditionOf(CaseSensitivePath.of("src", "FooFoo.cpp"), 7);
        Logger.debug(result);
        assert result.isSuccess();
        assert SAT.equivalent(result.getSuccess(), new And(new Literal("A"), new Literal("B")));
    }

    @Test
    public void testGeneration() {
        final FeatureModelFormula fmf = new FeatureModelFormula(pcTest1.features);
        assert pcTest1.generate(Arrays.asList(
                        new Variant("justA", new FeatureIDEConfiguration(fmf, Collections.singletonList("A"))),
                        new Variant("justB", new FeatureIDEConfiguration(fmf, Collections.singletonList("B"))),
                        new Variant("all", new FeatureIDEConfiguration(fmf, Arrays.asList("A", "B", "C", "D", "E")))
                ),
                true);
    }

    @Test
    public void testGenerationWithCustomSample() {
        final Sampler sampler = FeatureIDESampler.CreateRandomSampler(5);
        assert pcTest1.generate(sampler.sample(pcTest1.features).variants(), true);
    }

    // TODO: Fix broken test
//    public void testLinuxSampleGeneration() {
//        assert linuxSample.generate(
//                List.of(new Variant("all", new SayYesToAllConfiguration())),
//                false);
//    }

    // TODO: Fix broken test
//    public void testLinuxGeneration() {
//        assert linux.generate(
//                List.of(new Variant("all", new SayYesToAllConfiguration())),
//                false);
//    }

    @Test
    public void caseSensitivePathTest() {
        final CaseSensitivePath sharedRoot = CaseSensitivePath.of("net", "netfilter");
        final CaseSensitivePath a = sharedRoot.resolve("xt_RATEEST.c");
        final CaseSensitivePath b = sharedRoot.resolve("xt_rateest.c");
        assert !a.equals(b);
    }
}
