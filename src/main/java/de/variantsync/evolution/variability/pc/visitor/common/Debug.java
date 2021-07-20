package de.variantsync.evolution.variability.pc.visitor.common;

import de.variantsync.evolution.util.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class Debug {
    public static CallbackArtefactVisitor createSimpleTreePrinter() {
        AtomicInteger indent = new AtomicInteger();
        return new CallbackArtefactVisitor(
                artefact -> {
                    System.out.println(StringUtils.genIndent(indent.get()) + artefact);
                    indent.incrementAndGet();
                },
                artefact -> indent.decrementAndGet()
        );
    }
}
