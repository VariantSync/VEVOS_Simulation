package org.variantsync.vevos.simulation;

import org.junit.Assert;
import org.junit.Test;
import org.tinylog.Logger;
import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.feature.config.SimpleConfiguration;
import org.variantsync.vevos.simulation.io.kernelhaven.KernelHavenSPLPCIO;
import org.variantsync.vevos.simulation.util.io.CaseSensitivePath;
import org.variantsync.vevos.simulation.variability.pc.*;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.GroundTruth;
import org.variantsync.vevos.simulation.variability.pc.options.ArtefactFilter;
import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

public class ContainsVariabilityQueryTest {
    final static Path BASE_PATH = Path.of("src/test/resources/contains-variability/");
    final static Path SPL_PC = BASE_PATH.resolve("code-variability.after.spl.csv");
    final static CaseSensitivePath SPL_DIR = new CaseSensitivePath(BASE_PATH.resolve("spl"));
    final static CaseSensitivePath GENERATION_DIR = new CaseSensitivePath(BASE_PATH.resolve("variants"));

    @Test
    public void splContainsNoVariability() {
        Assert.assertFalse(splFileContainsVariability("main.c"));
    }

    @Test
    public void splContainsTopLevelVariability() {
        Assert.assertTrue(splFileContainsVariability("top_level.c"));
    }

    @Test
    public void splContainsNestedVariability() {
        Assert.assertTrue(splFileContainsVariability("code.c"));
    }

    @Test
    public void variantContainsNoVariability() {
        Assert.assertFalse(variantFileContainsVariability("main.c", "FEATURE_A"));
    }

    @Test
    public void variantContainsTopLevelVariability() {
        Assert.assertTrue(variantFileContainsVariability("top_level.c", "FEATURE_A", "FEATURE_F"));
    }

    @Test
    public void variantContainsNestedVariability() {
        Assert.assertTrue(variantFileContainsVariability("code.c", "FEATURE_A"));
        Assert.assertTrue(variantFileContainsVariability("code.c", "FEATURE_B"));
        Assert.assertTrue(variantFileContainsVariability("code.c", "FEATURE_C"));
        Assert.assertTrue(variantFileContainsVariability("code.c", "FEATURE_D"));
    }

    private boolean splFileContainsVariability(String pathAsString) {
        Artefact gt = loadSPLPCs();
        return gt.fileContainsVariability(new CaseSensitivePath(Path.of(pathAsString)))
                .expect("Was not able to determine variability");
    }

    private boolean variantFileContainsVariability(String pathAsString, String... features) {
        Variant variant = new Variant("variant", new SimpleConfiguration(Arrays.asList(features)));
        GroundTruth variantGT = generateVariant(loadSPLPCs(), variant);

        return variantGT.variant().fileContainsVariability(new CaseSensitivePath(Path.of(pathAsString)))
                .expect("Was not able to determine variant's variability");
    }

    private Artefact loadSPLPCs() {
        KernelHavenSPLPCIO io = new KernelHavenSPLPCIO();
        return io.load(SPL_PC).expect("Was not able to load SPL PC file!");
    }

    private GroundTruth generateVariant(Artefact splPCs, Variant variant) {
        CaseSensitivePath targetDir = GENERATION_DIR.resolve(variant.getName());
        // Clean old files
        FileUtils.removeFilesRecursively(targetDir.path().toFile());
        return splPCs.generateVariant(variant, SPL_DIR,
                targetDir,
                VariantGenerationOptions.ExitOnErrorButAllowNonExistentFiles(false, ArtefactFilter.KeepAll()))
                .expect("Was not able to generate variant");
    }

}
