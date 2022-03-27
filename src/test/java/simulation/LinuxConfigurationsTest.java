package simulation;

import org.junit.Test;
import simulation.feature.sampling.LinuxKernel;
import simulation.feature.sampling.Sample;

import java.io.IOException;

public class LinuxConfigurationsTest {
    @Test
    public void configsAreLoaded() throws IOException {
        final Sample sample = LinuxKernel.GetSample();
        assert sample.size() == 5;
    }
}
