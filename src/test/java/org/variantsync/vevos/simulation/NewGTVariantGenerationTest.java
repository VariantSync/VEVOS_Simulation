package org.variantsync.vevos.simulation;

import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import org.junit.Assert;
import org.junit.Test;
import org.tinylog.Logger;
import org.variantsync.functjonal.Result;
import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.feature.config.FeatureIDEConfiguration;
import org.variantsync.vevos.simulation.feature.config.IConfiguration;
import org.variantsync.vevos.simulation.feature.config.SimpleConfiguration;
import org.variantsync.vevos.simulation.io.Resources;
import org.variantsync.vevos.simulation.io.featureide.FeatureModelIO;
import org.variantsync.vevos.simulation.io.kernelhaven.KernelHavenSPLPCIO;
import org.variantsync.vevos.simulation.io.kernelhaven.VariabilityModelLoader;
import org.variantsync.vevos.simulation.util.fide.FeatureModelUtils;
import org.variantsync.vevos.simulation.util.io.CaseSensitivePath;
import org.variantsync.vevos.simulation.variability.pc.*;
import org.variantsync.vevos.simulation.variability.pc.options.ArtefactFilter;
import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for presence condition loading and variant generation.
 */
public class NewGTVariantGenerationTest {
    final static Path BASE_PATH = Path.of("src/test/resources/new-gt-format/");
    final static Path SPL_PC = BASE_PATH.resolve("code-variability.after.spl.csv");
    final static CaseSensitivePath SPL_DIR = new CaseSensitivePath(BASE_PATH.resolve("spl"));
    final static CaseSensitivePath GENERATION_DIR = new CaseSensitivePath(BASE_PATH.resolve("actual"));
    final static CaseSensitivePath EXPECTED_RESULT_DIR = new CaseSensitivePath(BASE_PATH.resolve("expected"));

    @Test
    public void variantA() throws IOException {
        genericVariantGenerationTest("variant-A", "FEATURE_A");
    }

    @Test
    public void variantAB() throws IOException {
        genericVariantGenerationTest("variant-AB", "FEATURE_A", "FEATURE_B");
    }

    @Test
    public void variantAD() throws IOException {
        genericVariantGenerationTest("variant-AD", "FEATURE_A", "FEATURE_D");
    }

    @Test
    public void variantB() throws IOException {
        genericVariantGenerationTest("variant-B", "FEATURE_B");
    }

    @Test
    public void variantBase() throws IOException {
        genericVariantGenerationTest("variant-base");
    }

    @Test
    public void variantBD() throws IOException {
        genericVariantGenerationTest("variant-BD", "FEATURE_B", "FEATURE_D");
    }

    @Test
    public void variantC() throws IOException {
        genericVariantGenerationTest("variant-C", "FEATURE_C");
    }

    @Test
    public void variantD() throws IOException {
        genericVariantGenerationTest("variant-D", "FEATURE_D");
    }

    private void genericVariantGenerationTest(String variantName, String... variantFeatures) throws IOException {
        Variant variant = initVariant(variantName, variantFeatures);
        Artefact splPCs = loadSPLPCs();
        generateVariant(splPCs, variant);
        assertCorrectGeneration(variant);
    }

    private Artefact loadSPLPCs() {
        KernelHavenSPLPCIO io = new KernelHavenSPLPCIO();
        return io.load(SPL_PC).expect("Was not able to load SPL PC file!");
    }

    private Variant initVariant(String name, String... features) {
        return new Variant(name, new SimpleConfiguration(Arrays.asList(features)));
    }

    private void generateVariant(Artefact splPCs, Variant variant) {
        CaseSensitivePath targetDir = GENERATION_DIR.resolve(variant.getName());
        // Clean old files
        removeFilesRecursively(targetDir.path().toFile());
        splPCs.generateVariant(variant, SPL_DIR,
                targetDir,
                VariantGenerationOptions.ExitOnErrorButAllowNonExistentFiles(false, ArtefactFilter.KeepAll()))
                // Write ground truth
                .bind(groundTruth -> Result.Try(() -> Resources.Instance().write(
                        Artefact.class,
                        groundTruth.variant(),
                        targetDir.resolve("ground_truth.variant.csv").path())))
                .bimap((success) -> {
                            Logger.debug("generated variant {}", variant.getName());
                            return success;
                        }
                        ,
                        (failure) -> {
                    Logger.error(failure);
                    throw new RuntimeException(failure);
                        });
    }

    private void assertCorrectGeneration(Variant variant) throws IOException {
        CaseSensitivePath pathToActual = GENERATION_DIR.resolve(variant.getName());
        CaseSensitivePath pathToExpected = EXPECTED_RESULT_DIR.resolve(variant.getName());
        assertCorrectCode(pathToExpected, pathToActual);
        assertCorrectPCs(pathToExpected, pathToActual);
    }

    private void assertCorrectCode(CaseSensitivePath pathToExpected, CaseSensitivePath pathToActual) throws IOException {
        List<String> expectedCode = Files.readAllLines(pathToExpected.resolve("code.c").path());
        List<String> actualCode = Files.readAllLines(pathToActual.resolve("code.c").path());
        Assert.assertTrue(compareLines(expectedCode, actualCode));
    }

    private void assertCorrectPCs(CaseSensitivePath pathToExpected, CaseSensitivePath pathToActual) throws IOException {
        List<String> expectedPCs = Files.readAllLines(pathToExpected.resolve("code-variability.variant.csv").path());
        List<String> actualPCs = Files.readAllLines(pathToActual.resolve("ground_truth.variant.csv").path());
        Assert.assertTrue(compareLines(expectedPCs, actualPCs));
    }

    private boolean compareLines(List<String> listA, List<String> listB) {
        boolean equal = true;

        if (listA.size() != listB.size()) {
            Logger.error("Different number of lines: {} vs. {}", listA.size(), listB.size());
        }

        for (int lineNumber = 1; lineNumber <= Math.min(listA.size(), listB.size()); lineNumber++) {
            String lineA = listA.get(lineNumber-1);
            String lineB = listB.get(lineNumber-1);
            if (!lineA.equals(lineB)) {
                equal = false;
                Logger.error("Line {} does not match: '{}' vs. '{}'", lineNumber, lineA, lineB);
            }
        }

        return equal;
    }

    private static void removeFilesRecursively(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    removeFilesRecursively(file);
                }
            }
        } else if (directory.isFile()) {
            boolean deleted = directory.delete();
            if (deleted) {
                Logger.debug("Deleted file: {}", directory.getAbsolutePath());
            } else {
                Logger.debug("Failed to delete file: {}", directory.getAbsolutePath());
            }
        }
    }
}
