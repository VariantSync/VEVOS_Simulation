package de.variantsync.evolution.feature;

import de.variantsync.evolution.feature.config.SimpleConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record Sample(List<Variant> variants) {
    public static Sample of(final List<Variant> variants) {
        return new Sample(variants);
    }
    
    public static Sample LinuxDistros() throws IOException {
        // TODO: Move loading of Configurations to somewhere else
        Path configDirectory = Path.of("src/main/resources/distro-configs");
        List<Path> configFiles = Files.list(configDirectory).collect(Collectors.toList());
        List<Variant> variants = new ArrayList<>(configFiles.size());
        for (Path p : configFiles) {
            List<String> lines = Files.readAllLines(p);
            List<String> activeFeatures = lines.stream().filter(l -> !l.startsWith("#")).map(l -> l.split("=")[0].trim()).filter(l -> !l.isEmpty()).collect(Collectors.toList());
            String name = p.toFile().getName().split("\\.")[0];
            variants.add(new Variant(name, new SimpleConfiguration(activeFeatures)));
        }
        return Sample.of(variants);
    }

    public int size() {
        return variants.size();
    }
}
