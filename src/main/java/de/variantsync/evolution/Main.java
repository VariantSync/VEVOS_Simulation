package de.variantsync.evolution;

import de.ovgu.featureide.fm.core.base.impl.*;
import de.ovgu.featureide.fm.core.configuration.*;
import de.ovgu.featureide.fm.core.io.sxfm.SXFMFormat;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelFormat;
import de.variantsync.evolution.io.data.VariabilityDatasetLoader;
import de.variantsync.evolution.repository.AbstractSPLRepository;
import de.variantsync.evolution.variability.VariabilityHistory;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Functional;
import de.variantsync.evolution.util.functional.Lazy;
import de.variantsync.evolution.util.functional.MonadTransformer;
import de.variantsync.evolution.util.functional.Unit;
import de.variantsync.evolution.util.list.NonEmptyList;
import de.variantsync.evolution.variability.CommitPair;
import de.variantsync.evolution.variability.SPLCommit;
import de.variantsync.evolution.variability.SequenceExtractors;
import de.variantsync.evolution.variability.VariabilityDataset;
import de.variantsync.evolution.variants.VariantsRepository;
import de.variantsync.evolution.variants.VariantsRevision;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class Main {
    private static final File PROPERTIES_FILE = new File("src/main/resources/user.properties");
    private static final String VARIABILITY_DATASET = "variability_dataset";
    private static final String SPL_REPO = "spl_repo";
    private static final String VARIANTS_REPO = "variants_repo";
    private static boolean initialized = false;

    private static void InitFeatureIDE() {
        /*
         * Who needs an SPL if we can clone-and-own from FeatureIDE's FMCoreLibrary, lol.
         */

        FMFactoryManager.getInstance().addExtension(DefaultFeatureModelFactory.getInstance());
        FMFactoryManager.getInstance().addExtension(MultiFeatureModelFactory.getInstance());
        FMFactoryManager.getInstance().setWorkspaceLoader(new CoreFactoryWorkspaceLoader());

        FMFormatManager.getInstance().addExtension(new XmlFeatureModelFormat());
        FMFormatManager.getInstance().addExtension(new SXFMFormat());

        ConfigurationFactoryManager.getInstance().addExtension(DefaultConfigurationFactory.getInstance());
        ConfigurationFactoryManager.getInstance().setWorkspaceLoader(new CoreFactoryWorkspaceLoader());

        ConfigFormatManager.getInstance().addExtension(new XMLConfFormat());
        ConfigFormatManager.getInstance().addExtension(new DefaultFormat());
        ConfigFormatManager.getInstance().addExtension(new FeatureIDEFormat());
        ConfigFormatManager.getInstance().addExtension(new EquationFormat());
        ConfigFormatManager.getInstance().addExtension(new ExpressionFormat());
    }

    public static void Initialize() {
        if (!initialized) {
            Logger.initConsoleLogger();
            InitFeatureIDE();
            initialized = true;
            Logger.debug("Finished initialization");
        }
    }

    public static void main(String[] args) throws IOException {
        Initialize();

        // Debug variability repo
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(PROPERTIES_FILE)) {
            properties.load(inputStream);
        } catch (IOException e) {
            Logger.error("Failed to open properties file: ", e);
            return;
        }
        /*
        Directory to which
        https://www.informatik.hu-berlin.de/de/forschung/gebiete/mse/forsch/Data/linux-variability-debug.7z/at_download/file
        or
        https://www.informatik.hu-berlin.de/de/forschung/gebiete/mse/forsch/Data/busybox-complete.7z/at_download/file
        (or any other dataset) was downloaded to
         */
        final Path variabilityDatasetDir = Paths.get(properties.getProperty(VARIABILITY_DATASET));

        // Check directory where the variability dataset is located
        if (variabilityDatasetDir.toFile().exists() && Files.list(variabilityDatasetDir).count() > 0) {
            Logger.debug("Variability dataset found under " + variabilityDatasetDir);
        } else {
            Logger.error("Was not able to find directory with variability dataset under " + variabilityDatasetDir);
            Logger.error("Please download and extract \"https://www.informatik.hu-berlin.de/de/forschung/gebiete/mse/forsch/Data/linux-variability-debug.7z/at_download/file\"");
        }

        // Directory to which https://github.com/torvalds/linux was cloned to
        final File splRepoDir = new File(properties.getProperty(SPL_REPO));

        Logger.info("variabilityDatasetDir: " + variabilityDatasetDir);
        Logger.info("splRepoDir: " + splRepoDir);
        VariabilityDataset variabilityDataset;

        VariabilityDatasetLoader datasetLoader = new VariabilityDatasetLoader();
        assert datasetLoader.canLoad(variabilityDatasetDir);
        variabilityDataset = datasetLoader.load(variabilityDatasetDir).getSuccess();
        Set<CommitPair<SPLCommit>> commitPairs = variabilityDataset.getCommitPairsForEvolutionStudy();
        Logger.info("The dataset contains " + variabilityDataset.getSuccessCommits().size() + " commits for which the variability extraction succeeded.");
        Logger.info("The dataset contains " + variabilityDataset.getErrorCommits().size() + " commits for which the variability extraction failed.");
        Logger.info("The dataset contains " + variabilityDataset.getPartialSuccessCommits().size() + " commits that for which the file presence conditions are missing.");
        Logger.info("The dataset contains " + commitPairs.size() + " usable pairs.");
        for (CommitPair<SPLCommit> pair : commitPairs) {
            Logger.debug("<<CHILD> " + pair.child().id() + "> -- <<PARENT> " + pair.parent().id() + ">");
            Logger.debug("<<CHILD> " + pair.child().id() + "> -- <<SPL_COMMIT> " + pair.child().id() + ">");
            Logger.debug("<<PARENT> " + pair.parent().id() + "> -- <<SPL_COMMIT> " + pair.parent().id() + ">");
            Logger.debug("");
        }
        VariabilityHistory history = variabilityDataset.getVariabilityHistory(SequenceExtractors.longestNonOverlappingSequences());
        NonEmptyList<NonEmptyList<SPLCommit>> sequencesInHistory = history.commitSequences();
        Logger.info("The dataset contains " + sequencesInHistory.size() + " sequences.");
        for (int i = 0; i < sequencesInHistory.size(); i++) {
            Logger.info("Sequence " + i + " has " + sequencesInHistory.get(i).size() + " commits.");
        }
        Logger.info("");

        // How to use variant generator
        {
            // Setup
            final AbstractSPLRepository splRepository = null; /* Get SPL Repo from somewhere. Integrate it into variabilityDataset?*/
            final VariantsRepository variantsRepo = new VariantsRepository(
                    Path.of(properties.getProperty(VARIANTS_REPO)),
                    splRepository,
                    history.toBlueprints());

            // Let's generate revisions for all variability commits here ...
            final Optional<VariantsRevision> firstRevisionToGenerate = variantsRepo.getStartRevision();

            // First possible way is to just run all revisions at once.
            {
                // This lazy holds the computation that will run everything.
                final Lazy<Unit> genAll = Functional.match(
                        firstRevisionToGenerate,
                        VariantsRevision::evolveAll, // If there is a first revision to generate, then generate all subsequent revision.
                        () -> Lazy.pure(Unit.Instance())); // If there was nothing to generate, return an empty computation.
                // Now run the generation process.
                // Only from this point on, we will see the program interact with the file system and git.
                genAll.run();
            }

            // Alternatively, we can also generate just a few steps if we like.
            {
                // First, let's build the necessary computations.
                final Lazy<Optional<VariantsRevision>> revision0 = Lazy.pure(firstRevisionToGenerate);
                final Lazy<Optional<VariantsRevision>> genRevision0 = MonadTransformer.bind(revision0, VariantsRevision::evolve);
                final Lazy<Optional<VariantsRevision>> genRevision1 = MonadTransformer.bind(genRevision0, VariantsRevision::evolve);
                final Lazy<Optional<VariantsRevision>> genRevision2 = MonadTransformer.bind(genRevision1, VariantsRevision::evolve);

                // Second, run them! Only from this point on, we will see the program interact with the file system and git.
                genRevision0.run(); // This would generate revision0.
                genRevision1.run(); // This would generate revision0 and then revision1.
                // This would generate revision0 and then revision1 and then revision2.
                // This returns a handle for revision3 which is not yet generated.
                Optional<VariantsRevision> revision3 = genRevision2.run();
                // Because Lazy caches intermediate results, revision0 and revision1 have only been generated exactly once.
            }
        }
    }
}