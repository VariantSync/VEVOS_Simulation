package de.variantsync.evolution;

import de.variantsync.evolution.feature.Sample;
import org.junit.Test;

import java.io.IOException;

public class LinuxConfigurationsTest {
    
    @Test
    public void configsAreLoaded() throws IOException {
        Sample sample = Sample.LinuxDistros();
        assert sample.size() == 5;
    }
    
}
