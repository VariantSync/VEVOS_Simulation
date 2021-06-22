import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.io.data.CSV;
import de.variantsync.evolution.io.data.CSVLoader;
import de.variantsync.evolution.io.kernelhaven.KernelHavenPCLoader;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.pc.*;
import org.junit.Test;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Or;

import java.nio.file.Path;
import java.util.Arrays;

public class PCLoaderTest {
    private static final Path resDir = Path.of("src", "main", "resources", "test");
    private static final Path testFile = resDir.resolve("KernelHavenPCs.csv");
    private static final Path illTestFile = resDir.resolve("KernelHavenPCs_illformed.csv");

    static {
        Logger.initConsoleLogger();
        final Resources r = Resources.Instance();
        r.registerLoader(CSV.class, new CSVLoader());
        r.registerLoader(FeatureTrace.class, new KernelHavenPCLoader());
    }

    @Test
    public void loadTestFileCorrectly() {
        final KernelHavenPCLoader pcLoader = new KernelHavenPCLoader();
        assert pcLoader.canLoad(testFile);
        final Result<FeatureTrace, Exception> result = pcLoader.load(testFile);
        assert result.isSuccess();

        FeatureTrace expectedResult;
        { // Build the expected result by hand.
            final SourceCodeFile alex = new SourceCodeFile(Path.of("src", "Alex.cpp"), FixTrueFalse.True);
            {
                LineBasedAnnotation a = new LineBasedAnnotation(new Literal("A"), 30, 60);
                a.addTrace(new LineBasedAnnotation(new Literal("B"), 50, 55));
                LineBasedAnnotation tru = new LineBasedAnnotation(FixTrueFalse.True, 1, 100);
                tru.addTrace(a);
                tru.addTrace(new LineBasedAnnotation(new Or(new And(new Literal("C"), new Literal("D")), new Literal("E")), 70, 90));
                alex.addTrace(tru);
            }

            final SourceCodeFile bar = new SourceCodeFile(Path.of("src", "foo", "bar.cpp"), new Literal("A"));
            {
                bar.addTrace(new LineBasedAnnotation(FixTrueFalse.False, 1, 20));
            }

            expectedResult = new FeatureTraceTree<>(Arrays.asList(alex, bar));
        }

        if (!expectedResult.equals(result.getSuccess())) {
            Logger.error("Loaded PCs:\n" + result.getSuccess().prettyPrint() + "\nis different from expected result:\n" + expectedResult.prettyPrint());
            assert false;
        }
    }

    @Test
    public void crashOnIllFormedFile() {
        final KernelHavenPCLoader pcLoader = new KernelHavenPCLoader();
        assert pcLoader.canLoad(testFile);
        final Result<FeatureTrace, Exception> result = pcLoader.load(illTestFile);
        assert result.isFailure();
        assert result.getFailure() instanceof IllegalFeatureTraceSpecification;
    }
}
