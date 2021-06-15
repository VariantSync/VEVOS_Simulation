import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.variantsync.evolution.variability.VariabilityDataset;
import de.variantsync.evolution.io.data.VariabilityDatasetLoader;
import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.repository.VariabilityHistory;
import de.variantsync.evolution.util.GitUtil;
import de.variantsync.evolution.variability.SPLCommit;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.variability.pc.FeatureTrace;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class VariabilityDatasetLoaderTest {
    private static final String SPL_REPO_URI = "https://gitlab.informatik.hu-berlin.de/mse/SampleRepos/SimpleHistory.git";
    private static final Path SIMPLE_VARIABILITY_METADATA_DIR = new File("src/test/resources/simple-variability-metadata").toPath();
    private static final String COMMIT_MESSAGE = "Commit message stub";
    private static final File SIMPLE_HISTORY_REPO_DIR;
    private static final Path TEMP_TEST_REPO_DIR;

    // TODO Alex: Test for loading of message
    // TODO Alex: Test for loading of log

    static {
        // TODO Alex: Handle double initialization problem
        Logger.initConsoleLogger();
        try {
            TEMP_TEST_REPO_DIR = Files.createDirectories(Paths.get("temporary-test-repos"));
            SIMPLE_HISTORY_REPO_DIR = new File(TEMP_TEST_REPO_DIR.toFile(), "simple-history");
            if (!SIMPLE_HISTORY_REPO_DIR.exists()) {
                GitUtil.fromRemote(SPL_REPO_URI, "simple-history", TEMP_TEST_REPO_DIR.toString());
            }
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private VariabilityDataset dataset;

    @Before
    public void loadData() {
        var result = new VariabilityDatasetLoader().load(SIMPLE_VARIABILITY_METADATA_DIR);
        assert result.isSuccess();
        this.dataset = result.getSuccess();
    }

    @Test
    public void successCommitsAreLoaded() {
        final String[] expectedSuccessCommits = new String[]{
                "60dc6013409b060b6f68c34902b3390e26e585dd",
                "e183211505ba66c89234b25687b76a7f4e4679cd",
                "411129f5a1923ce107b2970311fdcea72e0b628b",

                "48c073dfcfd0907f1e460628a7379b4bcbc8c737",

                "eab1607a6f137376e57a3381c2fdae9c9d46de4d",
                "78fe3d306860e11e327a43cfce2c97748a34c1e1",

                "454f7da158fdf3fe4b3c3fc8110f6c15861f97fa",
                "c79e89cd49fc17be386ca026686dd01e0985a5ea",
                "600f60df96cdbbf3319085d8e777d7e66c96e013",
                "a54c3c30f2dff6dc36331f06360630b697b7562c",

                "38e15e31eabccf82d3183273240cd44f2dec9fa9",
                "741ee98bf3edee477c504726fdc482ae85adf0e5"
        };

        List<SPLCommit> successCommits = dataset.getSuccessCommits();
        for (var expectedCommit : expectedSuccessCommits) {
            assert Commit.contains(successCommits, expectedCommit);
        }
        assert successCommits.size() == expectedSuccessCommits.length;
    }

    @Test
    public void errorCommitsAreLoaded() {
        final String[] expectedErrorCommits = new String[]{
                "5e0646cefbafc911449d6c0f420ff633fa4b41e9",
                "c11c2aff04769cbcb6c568f90257dc3cc7bb1737",
                "0e1827400a0d06b9112777261c4bdb541ffbb134",
                "80e14e6db5ef9ec4149887b544621d10a19edf92",
                "4ea494802cd552464c2f1a47d727a206eccf1d20"
        };

        List<SPLCommit> errorCommits = dataset.getErrorCommits();
        for (var expectedCommit : expectedErrorCommits) {
            assert Commit.contains(errorCommits, expectedCommit);
        }
        assert errorCommits.size() == expectedErrorCommits.length;
    }

    @Test
    public void incompletePCCommitsAreLoaded() {
        final String[] expectedSuccessCommits = new String[]{
                "aed45b2f723e372b750e8007c72730bcddee7174",
                "e12024473264e88058027290a348d1ada31af20a",
                "f83a980b7c948f2a7a50c75f06e8298f971e61aa",
                "f0619022ca9f6aeaba51fb1b71e77f6887cca4a4"
        };

        List<SPLCommit> incompletePCCommits = dataset.getIncompletePCCommits();
        for (var expectedCommit : expectedSuccessCommits) {
            assert Commit.contains(incompletePCCommits, expectedCommit);
        }
        assert incompletePCCommits.size() == expectedSuccessCommits.length;
    }

    @Test
    public void variabilityHistoryBuildCorrectly() {
        // We created a test VariabilityRepo for which we manually selected success and error commits. The following
        // commit lists represent all sequences of success commits that were created. Any deviation from these sequences
        // indicates a bug in loading the VariabilityRepo
        var firstList = new String[]{"eab1607a6f137376e57a3381c2fdae9c9d46de4d", "78fe3d306860e11e327a43cfce2c97748a34c1e1"};
        var secondList = new String[]{"454f7da158fdf3fe4b3c3fc8110f6c15861f97fa", "c79e89cd49fc17be386ca026686dd01e0985a5ea", "600f60df96cdbbf3319085d8e777d7e66c96e013", "a54c3c30f2dff6dc36331f06360630b697b7562c"};
        var thirdList = new String[]{"38e15e31eabccf82d3183273240cd44f2dec9fa9", "741ee98bf3edee477c504726fdc482ae85adf0e5"};

        VariabilityHistory history = dataset.getCommitSequencesForEvolutionStudy();
        var commitSequences = history.commitSequences();
        // Check the size
        assert commitSequences.size() == 3;

        for (var sequence : commitSequences) {
            switch (sequence.size()) {
                case 2 -> {
                    // The retrieved sequence contains 2 commits, so it must contain the same commits as either firstList or secondList
                    if (firstList[0].equals(sequence.get(0).id())) {
                        assertCommitIdsAreEqual(firstList, sequence);
                    } else {
                        assertCommitIdsAreEqual(thirdList, sequence);
                    }
                }
                // The retrieved sequence contains three commits so it must contain the same commits as thirdList
                case 4 -> assertCommitIdsAreEqual(secondList, sequence);
                // The retrieved sequence contains an unexpected number of commits and the history is therefore incorrect
                default -> throw new AssertionError("Invalid number of commits in the sequence.");
            }
        }
    }

    @Test
    public void messagesOfSuccessCommitsAreLoaded() {
        for (SPLCommit commit : dataset.getSuccessCommits()) {
            String message = commit.message().run().orElseThrow();
            assert message.equals(COMMIT_MESSAGE);
        }
    }

    @Test
    public void messagesOfIncompletePCCommitsAreLoaded() {
        for (SPLCommit commit : dataset.getIncompletePCCommits()) {
            String message = commit.message().run().orElseThrow();
            assert message.equals(COMMIT_MESSAGE);
        }
    }

    @Test
    public void noMessagesForErrorCommits() {
        for (SPLCommit commit : dataset.getErrorCommits()) {
            Optional<String> message = commit.message().run();
            assert message.isEmpty();
        }
    }

    @Test
    public void logsOfEachCommitAreLoaded() {
        for (SPLCommit commit : dataset.getAllCommits()) {
            String log = commit.kernelHavenLog().run().orElseThrow();
            assert "".equals(log);
        }
    }

    @Test
    public void presenceConditionsOfSuccessCommitsAreLoaded() {
        for (SPLCommit commit : dataset.getSuccessCommits()) {
            FeatureTrace trace = commit.presenceConditions().run().orElseThrow();
            assert trace != null;
        }
    }

    @Test
    public void presenceConditionsOfIncompletePCCommitsAreLoaded() {
        for (SPLCommit commit : dataset.getIncompletePCCommits()) {
            FeatureTrace trace = commit.presenceConditions().run().orElseThrow();
            assert trace != null;
        }
    }

    @Test
    public void noPresenceConditionsForErrorCommits() {
        for (SPLCommit commit : dataset.getSuccessCommits()) {
            Optional<FeatureTrace> trace = commit.presenceConditions().run();
            assert trace.isEmpty();
        }
    }

    @Test
    public void featureModelsOfSuccessCommitsAreLoaded() {
        for (SPLCommit commit : dataset.getSuccessCommits()) {
            IFeatureModel model = commit.featureModel().run().orElseThrow();
            assert model != null;
        }
    }

    @Test
    public void featureModelsOfIncompletePCCommitsAreLoaded() {
        for (SPLCommit commit : dataset.getIncompletePCCommits()) {
            IFeatureModel model = commit.featureModel().run().orElseThrow();
            assert model != null;
        }
    }

    @Test
    public void noFeatureModelForErrorCommits() {
        for (SPLCommit commit : dataset.getSuccessCommits()) {
            Optional<IFeatureModel> model = commit.featureModel().run();
            assert model.isEmpty();
        }
    }

    private void assertCommitIdsAreEqual(String[] ids, List<SPLCommit> commits) {
        assert ids.length == commits.size();
        for (int i = 0; i < ids.length; i++) {
            assert ids[i].equals(commits.get(i).id());
        }
    }
}


