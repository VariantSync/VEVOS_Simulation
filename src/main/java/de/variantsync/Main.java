package de.variantsync;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.sat.SAT;
import de.variantsync.subjects.VariabilityRepo;
import de.variantsync.util.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.prop4j.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    // Directory to which https://gitlab.informatik.hu-berlin.de/mse/LinuxVariabilityData was cloned to
    private final static File variabilityRepoDir = new File("/home/alex/data/evolution-debug");
    // Directory to which https://github.com/torvalds/linux was cloned to
    private final static File splRepoDir = new File("/home/alex/dev/linux-analysis/linux");

    public static void main(String[] args) {
        Logger.initConsoleLogger();
        System.out.println("Hi Paul");

        // Test imports
        IFeatureModel m;

        final Node formula = new And(new Literal("A"), new Literal("A", false));
        System.out.println("SAT(" + formula + ") = " + SAT.isSatisfiable(formula));

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
            e.printStackTrace();
        }
    }
}