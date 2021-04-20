package de.variantsync;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.sat.SAT;
import de.variantsync.subjects.VariabilityRepo;
import de.variantsync.util.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.prop4j.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

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
        try {
            var variabilityRepo = VariabilityRepo.load(variabilityRepoDir, splRepoDir);
            var commitPairs = variabilityRepo.getCommitPairsForEvolutionStudy();
            Logger.info("The repo contains " + variabilityRepo.getSuccessCommits().size() + " commits for which the variability extraction succeeded.");
            Logger.info("The repo contains " + variabilityRepo.getErrorCommits().size() + " commits for which the variability extraction failed.");
            Logger.info("The repo contains " + variabilityRepo.getNonMergeCommits().size() + " commits that processed an SPLCommit which was not a merge.");
            Logger.info("The repo contains " + commitPairs.size() + " usable pairs.");
            for (var pair : commitPairs) {
                Logger.info("<<CHILD> " + pair.child().id() + "> -- <<PARENT> " + pair.parent().id() + ">");
                Logger.info("<<CHILD> " + pair.child().id() + "> -- <<SPL_COMMIT> " + variabilityRepo.getSPLCommit(pair.child()).id() + ">");
                Logger.info("<<PARENT> " + pair.parent().id() + "> -- <<SPL_COMMIT> " + variabilityRepo.getSPLCommit(pair.parent()).id() + ">");
                Logger.info("");
            }
        } catch (IOException | GitAPIException e) {
            Logger.exception("Failed to load variability or spl repo:", e);
        }
    }
}