package vevos;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;
import vevos.io.data.VariabilityDatasetLoader;
import vevos.repository.Commit;
import vevos.util.GitUtil;
import vevos.variability.SPLCommit;
import vevos.variability.VariabilityDataset;
import vevos.variability.VariabilityHistory;
import vevos.variability.sequenceextraction.LongestNonOverlappingSequences;

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
    // I added an invalid String "ääääääää" to the DIMACS file of e12024473264e88058027290a348d1ada31af20a
    private static final String COMMIT_WITH_INVALID_DIMACS_FILE = "e12024473264e88058027290a348d1ada31af20a";

    static {
        Main.Initialize();
        try {
            TEMP_TEST_REPO_DIR = Files.createDirectories(Paths.get("temporary-test-repos"));
            SIMPLE_HISTORY_REPO_DIR = new File(TEMP_TEST_REPO_DIR.toFile(), "simple-history");
            if (!SIMPLE_HISTORY_REPO_DIR.exists()) {
                GitUtil.fromRemote(SPL_REPO_URI, "simple-history", TEMP_TEST_REPO_DIR.toString());
            }
        } catch (final IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private VariabilityDataset dataset;

    @Before
    public void loadData() {
        final var result = new VariabilityDatasetLoader().load(SIMPLE_VARIABILITY_METADATA_DIR);
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

        final List<SPLCommit> successCommits = dataset.getSuccessCommits();
        for (final var expectedCommit : expectedSuccessCommits) {
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

        final List<SPLCommit> errorCommits = dataset.getErrorCommits();
        for (final var expectedCommit : expectedErrorCommits) {
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

        final List<SPLCommit> incompletePCCommits = dataset.getPartialSuccessCommits();
        for (final var expectedCommit : expectedSuccessCommits) {
            assert Commit.contains(incompletePCCommits, expectedCommit);
        }
        assert incompletePCCommits.size() == expectedSuccessCommits.length;
    }

    @Test
    public void variabilityHistoryBuildCorrectly() {
        // We created a test VariabilityRepo for which we manually selected success and error commits. The following
        // commit lists represent all sequences of success commits that were created. Any deviation from these sequences
        // indicates a bug in loading the VariabilityRepo
        final var firstList = new String[]{"eab1607a6f137376e57a3381c2fdae9c9d46de4d", "78fe3d306860e11e327a43cfce2c97748a34c1e1"};
        final var secondList = new String[]{"454f7da158fdf3fe4b3c3fc8110f6c15861f97fa", "600f60df96cdbbf3319085d8e777d7e66c96e013", "a54c3c30f2dff6dc36331f06360630b697b7562c", "38e15e31eabccf82d3183273240cd44f2dec9fa9", "741ee98bf3edee477c504726fdc482ae85adf0e5"};
        final var thirdList = new String[]{"c79e89cd49fc17be386ca026686dd01e0985a5ea", "48c073dfcfd0907f1e460628a7379b4bcbc8c737"};

        final VariabilityHistory history = dataset.getVariabilityHistory(new LongestNonOverlappingSequences());
        final var commitSequences = history.commitSequences();
        // Check the size
        assert commitSequences.size() == 3;

        for (final var sequence : commitSequences) {
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
                case 5 -> assertCommitIdsAreEqual(secondList, sequence);
                // The retrieved sequence contains an unexpected number of commits and the history is therefore incorrect
                default -> throw new AssertionError("Invalid number of commits in the sequence.");
            }
        }
    }

    @Test
    public void messagesOfSuccessCommitsAreLoaded() {
        for (final SPLCommit commit : dataset.getSuccessCommits()) {
            final String message = commit.message().run().orElseThrow();
            assert message.equals(COMMIT_MESSAGE);
        }
    }

    @Test
    public void messagesOfIncompletePCCommitsAreLoaded() {
        for (final SPLCommit commit : dataset.getPartialSuccessCommits()) {
            final String message = commit.message().run().orElseThrow();
            assert message.equals(COMMIT_MESSAGE);
        }
    }

    @Test
    public void noMessagesForErrorCommits() {
        for (final SPLCommit commit : dataset.getErrorCommits()) {
            final Optional<String> message = commit.message().run();
            assert message.isEmpty();
        }
    }

    @Test
    public void logsOfEachCommitAreLoaded() {
        for (final SPLCommit commit : dataset.getAllCommits()) {
            final String log = commit.kernelHavenLog().run().orElseThrow();
            assert "".equals(log);
        }
    }

    @Test
    public void presenceConditionsOfSuccessCommitsAreLoaded() {
        for (final SPLCommit commit : dataset.getSuccessCommits()) {
           assert commit.presenceConditions().run().isPresent();
                    }
    }

    @Test
    public void presenceConditionsOfIncompletePCCommitsAreLoaded() {
        for (final SPLCommit commit : dataset.getPartialSuccessCommits()) {
            assert commit.presenceConditions().run().isPresent();
        }
    }

    @Test
    public void noPresenceConditionsForErrorCommits() {
        for (final SPLCommit commit : dataset.getErrorCommits()) {
            assert commit.presenceConditions().run().isEmpty();
        }
    }

    @Test
    public void featureModelsOfSuccessCommitsAreLoaded() {
        for (final SPLCommit commit : dataset.getSuccessCommits()) {
            assert commit.featureModel().run().isPresent();
        }
    }

    @Test
    public void featureModelsOfIncompletePCCommitsAreLoaded() {
        for (final SPLCommit commit : dataset.getPartialSuccessCommits()) {
            if (commit.id().equals(COMMIT_WITH_INVALID_DIMACS_FILE)) {
                // I added an invalid String to one of the commits
                continue;
            }
            assert commit.featureModel().run().isPresent();
        }
    }

    @Test
    public void expectParseError() {
        for (final SPLCommit commit : dataset.getPartialSuccessCommits()) {
            if (commit.id().equals(COMMIT_WITH_INVALID_DIMACS_FILE)) {
                final Optional<IFeatureModel> model = commit.featureModel().run();
                assert model.isEmpty();
            }
        }
    }

    @Test
    public void noFeatureModelForErrorCommits() {
        for (final SPLCommit commit : dataset.getErrorCommits()) {
            final Optional<IFeatureModel> model = commit.featureModel().run();
            assert model.isEmpty();
        }
    }

    private void assertCommitIdsAreEqual(final String[] ids, final List<SPLCommit> commits) {
        assert ids.length == commits.size();
        for (int i = 0; i < ids.length; i++) {
            assert ids[i].equals(commits.get(i).id());
        }
    }
}