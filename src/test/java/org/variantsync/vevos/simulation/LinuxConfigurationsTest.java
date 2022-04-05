package org.variantsync.vevos.simulation;

import org.junit.Test;
import org.variantsync.vevos.simulation.feature.sampling.LinuxKernel;
import org.variantsync.vevos.simulation.feature.sampling.Sample;

import java.io.IOException;

public class LinuxConfigurationsTest {
    @Test
    public void configsAreLoaded() throws IOException {
        final Sample sample = LinuxKernel.GetSample();
        assert sample.size() == 5;
    }
}
