package org.variantsync.vevos.simulation;

import org.junit.Test;
import org.variantsync.vevos.simulation.io.kernelhaven.KernelHavenSPLPCIO;

import java.nio.file.Path;

public class KernelHavenPCIOTest {

    @Test
    public void test() {
        VEVOS.Initialize();
        KernelHavenSPLPCIO io = new KernelHavenSPLPCIO();
        io.load(Path.of("src/test/resources/gts/gt-1.spl.csv"));
    }
}
