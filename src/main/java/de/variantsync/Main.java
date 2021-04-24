package de.variantsync;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.VariantsRepository;
import de.variantsync.evolution.VariantsRevision;
import de.variantsync.repository.ISPLRepository;
import de.variantsync.repository.VariabilityHistory;
import de.variantsync.sat.SAT;
import de.variantsync.subjects.CommitPair;
import de.variantsync.subjects.VariabilityRepo;
import de.variantsync.util.Functional;
import de.variantsync.util.Lazy;
import de.variantsync.util.Logger;
import de.variantsync.util.Unit;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.prop4j.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

public class Main {
    private static final File PROPERTIES_FILE = new File("src/main/resources/user.properties");
    private static final String VARIABILITY_REPO = "variability_repo";
    private static final String SPL_REPO = "spl_repo";

    public static void main(String[] args) {
        Logger.initConsoleLogger();
        Logger.status("Hi Paul");

        // Test imports
        IFeatureModel m;

        final Node formula = new And(new Literal("A"), new Literal("A", false));
        Logger.info("SAT(" + formula + ") = " + SAT.isSatisfiable(formula));

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
            Set<CommitPair> commitPairs = variabilityRepo.getCommitPairsForEvolutionStudy();
            Logger.info("The repo contains " + variabilityRepo.getSuccessCommits().size() + " commits for which the variability extraction succeeded.");
            Logger.info("The repo contains " + variabilityRepo.getErrorCommits().size() + " commits for which the variability extraction failed.");
            Logger.info("The repo contains " + variabilityRepo.getNonMergeCommits().size() + " commits that processed an SPLCommit which was not a merge.");
            Logger.info("The repo contains " + commitPairs.size() + " usable pairs.");
            for (CommitPair pair : commitPairs) {
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
            Lazy<Unit> genAll = variantsRepo.generateAll(); // The returned lazy holds the computation that will run everything.
            genAll.run(); // execute everything

            // But we can also generate just a few steps if we like ...
            Optional<VariantsRevision> revision0 = variantsRepo.getStartRevision();
            Optional<VariantsRevision> revision1 = variantsRepo.generateNext().run();
            Optional<VariantsRevision> revision2 = variantsRepo.generateNext().run();

            // Or we can chain the above to do it in one step
            final Function<Optional<VariantsRevision>, Lazy<Optional<VariantsRevision>>> evolver = l -> variantsRepo.generateNext();
            Lazy<Optional<VariantsRevision>> generateTheNext3Revisions = variantsRepo
                    .generateNext()
                    .bind(evolver)
                    .bind(evolver);
            generateTheNext3Revisions.run();
        }
    }
}