package de.variantsync.evolution.feature.sampling;

import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.IConfigurationGenerator;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.RandomConfigurationGenerator;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.job.LongRunningWrapper;
import de.ovgu.featureide.fm.core.job.monitor.NullMonitor;
import de.variantsync.evolution.feature.Sample;
import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.util.names.NameGenerator;
import de.variantsync.evolution.util.names.NumericNameGenerator;
import de.variantsync.evolution.variability.config.FeatureIDEConfiguration;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FeatureIDESampler extends ResizableSampler {
    private final Function<CNF, IConfigurationGenerator> generatorFactory;
    private NameGenerator variantNameGenerator;

    public static FeatureIDESampler CreateRandomSampler(final int size) {
        return new FeatureIDESampler(
                size,
                cnf -> new RandomConfigurationGenerator(cnf, size)
        );
    }

    public FeatureIDESampler(final int size, final Function<CNF, IConfigurationGenerator> generatorFactory) {
        super(size);
        this.generatorFactory = generatorFactory;
        variantNameGenerator = new NumericNameGenerator("Variant");
    }

    public void setNameGenerator(final NameGenerator variantNameGenerator) {
        this.variantNameGenerator = variantNameGenerator;
    }

    @Override
    public Sample sample(final IFeatureModel model) {
        final FeatureModelFormula featureModelFormula = new FeatureModelFormula(model);
        final CNF cnf = featureModelFormula.getCNF();
        final IConfigurationGenerator generator = generatorFactory.apply(cnf);
        // We could add a monitor here that writes to the log for example.
        // The monitor gets notified about the progress of the generator and can for example be used to update a progress bar.
        // I guess we do not need it.
        final List<LiteralSet> result = LongRunningWrapper.runMethod(generator, new NullMonitor<>());
        final AtomicInteger variantNo = new AtomicInteger();
        return new Sample(result.stream().map(literalSet -> new Variant(
                variantNameGenerator.getNameAtIndex(variantNo.getAndIncrement()),
                new FeatureIDEConfiguration(literalSet, featureModelFormula, cnf.getVariables())
        )).collect(Collectors.toList()));
    }
}
