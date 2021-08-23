package de.variantsync.evolution.feature;

import de.variantsync.evolution.feature.config.SimpleConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public record Sample(List<Variant> variants) {
    private static final String[] DISTRO_CONFIGS = new String[]{
            "distro-configs/debian.config",
            "distro-configs/fedora.config",
            "distro-configs/mint.config",
            "distro-configs/mx-linux.config",
            "distro-configs/ubuntu.config"
    };

    public static Sample of(final List<Variant> variants) {
        return new Sample(variants);
    }

    public static Sample LinuxDistros() throws IOException {
        // TODO: Move loading of Configurations to somewhere else
        List<Variant> variants = new ArrayList<>(DISTRO_CONFIGS.length);
        for (String res : DISTRO_CONFIGS) {
            try (InputStream is = Objects.requireNonNull(Sample.class.getClassLoader().getResourceAsStream(res));
                 Scanner scanner = new Scanner(is)) {
                List<String> lines = new LinkedList<>();
                while (scanner.hasNext()) {
                    lines.add(scanner.nextLine());
                }
                List<String> activeFeatures = lines.stream().filter(l -> !l.startsWith("#")).map(l -> l.split("=")[0].trim()).filter(l -> !l.isEmpty()).collect(Collectors.toList());
                String name = res.split("/")[1].split("\\.")[0];
                variants.add(new Variant(name, new SimpleConfiguration(activeFeatures)));
            }
        }
        return Sample.of(variants);
    }

    public int size() {
        return variants.size();
    }
}
