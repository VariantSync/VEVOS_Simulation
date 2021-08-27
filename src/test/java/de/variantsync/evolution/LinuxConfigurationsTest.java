package de.variantsync.evolution;

import de.variantsync.evolution.feature.sampling.LinuxKernel;
import de.variantsync.evolution.feature.sampling.Sample;
import org.junit.Test;

import java.io.IOException;

public class LinuxConfigurationsTest {
    @Test
    public void configsAreLoaded() throws IOException {
        final Sample sample = LinuxKernel.GetSample();
        assert sample.size() == 5;
    }
}
