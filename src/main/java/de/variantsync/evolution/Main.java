package de.variantsync.evolution;

import de.variantsync.evolution.io.Resources;
import de.variantsync.evolution.io.data.CSV;
import de.variantsync.evolution.io.data.CSVLoader;
import de.variantsync.evolution.io.kernelhaven.KernelHavenPCLoader;
import de.variantsync.evolution.io.pclocator.PCLocatorPCLoader;
import de.variantsync.evolution.repository.ISPLRepository;
import de.variantsync.evolution.repository.VariabilityHistory;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Functional;
import de.variantsync.evolution.util.functional.Lazy;
import de.variantsync.evolution.util.functional.MonadTransformer;
import de.variantsync.evolution.util.functional.Unit;
import de.variantsync.evolution.variability.CommitIdPair;
import de.variantsync.evolution.variability.VariabilityRepo;
import de.variantsync.evolution.variability.pc.FeatureTrace;
import de.variantsync.evolution.variants.VariantsRepository;
import de.variantsync.evolution.variants.VariantsRevision;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class Main {
    private static final File PROPERTIES_FILE = new File("src/main/resources/user.properties");
    private static final String VARIABILITY_REPO = "variability_repo";
    private static final String SPL_REPO = "spl_repo";

    private static void initResources() {
        final Resources r = Resources.Instance();
        r.registerLoader(CSV.class, new CSVLoader());
        r.registerLoader(FeatureTrace.class, new KernelHavenPCLoader());
        r.registerLoader(FeatureTrace.class, new PCLocatorPCLoader());
    }

    public static void main(String[] args) {
        Logger.initConsoleLogger();
        initResources();

        // Debug variability repo
        Properties properties = new Properties();
        try(FileInputStream inputStream = new FileInputStream(PROPERTIES_FILE)) {
            properties.load(inputStream);
        } catch (IOException e) {
            Logger.exception("Failed to open properties file: ", e);
            return;
        }
        // Directory to which https://gitlab.informatik.hu-berlin.de/mse/LinuxVariabilityData was cloned to
        final File variabilityRepoDir = new File(properties.getProperty(VARIABILITY_REPO));
        // Directory to which https://github.com/torvalds/linux was cloned to
        final File splRepoDir = new File(properties.getProperty(SPL_REPO));

        Logger.info("variabilityRepoDir: " + variabilityRepoDir);
        Logger.info("splRepoDir: " + splRepoDir);
        VariabilityRepo variabilityRepo = null;
        try {
            variabilityRepo = VariabilityRepo.load(variabilityRepoDir, splRepoDir);
            Set<CommitIdPair> commitIdPairs = variabilityRepo.getCommitPairsForEvolutionStudy();
            Logger.info("The repo contains " + variabilityRepo.getSuccessCommits().size() + " commits for which the variability extraction succeeded.");
            Logger.info("The repo contains " + variabilityRepo.getErrorCommits().size() + " commits for which the variability extraction failed.");
            Logger.info("The repo contains " + variabilityRepo.getNonMergeCommits().size() + " commits that processed an SPLCommit which was not a merge.");
            Logger.info("The repo contains " + commitIdPairs.size() + " usable pairs.");
            for (CommitIdPair pair : commitIdPairs) {
                Logger.info("<<CHILD> " + pair.child().id() + "> -- <<PARENT> " + pair.parent().id() + ">");
                Logger.info("<<CHILD> " + pair.child().id() + "> -- <<SPL_COMMIT> " + pair.child().splCommit().id() + ">");
                Logger.info("<<PARENT> " + pair.parent().id() + "> -- <<SPL_COMMIT> " + pair.parent().splCommit().id() + ">");
                Logger.info("");
            }
        } catch (IOException | GitAPIException e) {
            Logger.exception("Failed to load variability or spl repo:", e);
        }

        // How to use variant generator
        {
            assert variabilityRepo != null;

            // Setup
            final ISPLRepository splRepository = null; /* Get SPL Repo from somewhere. Integrate it into variabilityRepo?*/
            final VariabilityHistory history = variabilityRepo.getCommitSequencesForEvolutionStudy();
            final VariantsRepository variantsRepo = new VariantsRepository(
                    Path.of("/path/to/repo"),
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
                final Lazy<Optional<VariantsRevision>>    revision0 = Lazy.pure(variantsRepo.getStartRevision());
                final Lazy<Optional<VariantsRevision>> genRevision0 = MonadTransformer.bind(revision0,    VariantsRevision::evolve);
                final Lazy<Optional<VariantsRevision>> genRevision1 = MonadTransformer.bind(genRevision0, VariantsRevision::evolve);
                final Lazy<Optional<VariantsRevision>> genRevision2 = MonadTransformer.bind(genRevision1, VariantsRevision::evolve);

                // Second, run them! Only from this point on, we will see the program interact with the file system and git.
                genRevision0.run(); // This would generate revision0.
                genRevision1.run(); // This would generate revision0 and then revision1.
                // This would generate revision0 and then revision1 and then revision2.
                // This returns a handle for revision3 which is not yet generated.
                Optional<VariantsRevision> revision3 = genRevision2.run();
                // Because Lazy caches intermediate results, revision0 and revision1 have only be generated exactly once.
            }
        }
    }
}