import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.io.data.CSV;
import de.variantsync.evolution.io.data.CSVLoader;
import de.variantsync.evolution.io.kernelhaven.KernelHavenPCLoader;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.pc.PreprocessorBlock;
import de.variantsync.evolution.variability.pc.PresenceConditions;
import de.variantsync.evolution.variability.pc.SourceCodeFile;
import org.junit.Test;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Or;

import java.nio.file.Path;
import java.util.Arrays;

public class PCLoaderTest {
    private static final Path testFile = Path.of("src", "main", "resources", "test", "KernelHavenPCs.csv");

    static {
        Logger.initConsoleLogger();
        final Resources r = Resources.Instance();
        r.registerLoader(CSV.class, new CSVLoader());
        r.registerLoader(PresenceConditions.class, new KernelHavenPCLoader());
    }

    @Test
    public void loadTestFileCorrectly() {
        final KernelHavenPCLoader pcLoader = new KernelHavenPCLoader();
        assert pcLoader.canLoad(testFile);
        final Result<PresenceConditions, Exception> result = pcLoader.load(testFile);
        assert result.isSuccess();

        PresenceConditions expectedResult;
        { // Build the expected result by hand.
            final SourceCodeFile alex = new SourceCodeFile(Path.of("src", "Alex.cpp"), FixTrueFalse.True);
            {
                PreprocessorBlock a = new PreprocessorBlock(new Literal("A"), 30, 60);
                a.addBlock(new PreprocessorBlock(new Literal("B"), 50, 55));
                PreprocessorBlock tru = new PreprocessorBlock(FixTrueFalse.True, 1, 100);
                tru.addBlock(a);
                tru.addBlock(new PreprocessorBlock(new Or(new And(new Literal("C"), new Literal("D")), new Literal("E")), 70, 90));
                alex.addBlock(tru);
            }

            final SourceCodeFile bar = new SourceCodeFile(Path.of("src", "foo", "bar.cpp"), new Literal("A"));
            {
                bar.addBlock(new PreprocessorBlock(FixTrueFalse.False, 1, 20));
            }

            expectedResult = new PresenceConditions(Arrays.asList(alex, bar));
        }

        if (!expectedResult.equals(result.getSuccess())) {
            Logger.error("Loaded PCs:\n" + result.getSuccess().prettyPrint() + "\nis different from expected result:\n" + expectedResult.prettyPrint());
            assert false;
        }
    }
}
