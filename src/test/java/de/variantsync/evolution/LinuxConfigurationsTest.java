package de.variantsync.evolution;

import de.variantsync.evolution.feature.Sample;
import de.variantsync.evolution.feature.Variant;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class LinuxConfigurationsTest {
    
    @Test
    public void configsAreLoaded() throws IOException {
        Sample sample = Sample.LinuxDistros();
        assert sample.size() == 5;
    }
    
}
