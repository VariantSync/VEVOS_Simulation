import de.variantsync.evolution.io.data.VariabilityDataset;
import de.variantsync.evolution.io.data.VariabilityDatasetLoader;
import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.repository.VariabilityHistory;
import de.variantsync.evolution.util.GitUtil;
import de.variantsync.evolution.variability.SPLCommit;
import de.variantsync.evolution.variability.VariabilityCommit;
import de.variantsync.evolution.util.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// TODO: Fix commit ids once the data has been set up
public class VariabilityDatasetLoaderTest {
    private static final String simpleHistoryRepoURI = "https://gitlab.informatik.hu-berlin.de/mse/SampleRepos/SimpleHistory.git";
    private static final Path simpleVariabilityMetadataDir = new File("test/resources/simple-variability-metadata").toPath();
    private static final File simpleHistoryRepoDir;
    private static final Path tempTestRepoDir;

    static {
        Logger.initConsoleLogger();
        try {
            tempTestRepoDir = Files.createDirectories(Paths.get("temporary-test-repos"));
            simpleHistoryRepoDir = new File(tempTestRepoDir.toFile(), "simple-history");
            if (!simpleHistoryRepoDir.exists()) {
                GitUtil.fromRemote(simpleHistoryRepoURI, "simple-history", tempTestRepoDir.toString());
            }
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private VariabilityDataset dataset;

    @Before
    public void loadData() {
        var result = new VariabilityDatasetLoader().load(simpleVariabilityMetadataDir);
        assert result.isSuccess();
        this.dataset = result.getSuccess();
    }

    @Test
    public void successCommitsAreLoaded() {
        final String[] expectedSuccessCommits = new String[]{
                "674d9d7f78f92a3cea19392b853d3f39e6482959",
                "d398531661b986467c2f15e7ef3b1429f0d4ad54",

                "ca3644a751d12f6893a170deaf3acfd6be0fd7e2",

                "907d04e53eb1dc242cc05c3137c7a794c9639172",

                "ee7e6aaa41a5e69734bf1acea8e5f1e430f2e555",
                "301d31223fa90a57f6492d65ca6730371d606b6c",
                "c302e501b6581a9383edc37f35780cf6f6d4a7b9",

                "544e9b9dadf945fc4d109f81ce52a11192ce0ea8",

                "c69c1a5544c4d6b074f446c536bc8b5ff85cfa52",
                "426e2cdb99131fbbf5e8bba658f7641213ffadca",
                "8e9b1f5c820093e42030794dc414891f899a58f9"
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
                "1915b9aa580c6e3a332146b3a579f015db627377",
                "7cc1135c70cb6ce92bc3d41a7fbff984b2c0e3ea",
                "bd020e4f40e2726f138c901f6fa44a106a55a56d",
                "55f9b80f8396c0f2a1a8f9f9cc314425bf231b0f",
                "7f08e8f9e2e53f2d16db73b7752f03953ffe1df8",
                "ee42dcd245ca2530b7d119ceda13202b608ba022"
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
                "37c16cf271fa87c8f32514127837be4ce236f21e",
                "8f12802ceab73ba61235b7943196f11968b49472",
                "6e0a4e66c09be9850d5dc5537ac9980c369fb392"
        };

        List<SPLCommit> incompletePCCommits = dataset.getSuccessCommits();
        for (var expectedCommit : expectedSuccessCommits) {
            assert Commit.contains(incompletePCCommits, expectedCommit);
        }
        assert incompletePCCommits.size() == expectedSuccessCommits.length;
    }

    @Test
    public void logicalParentsAreLoaded() {
        assert dataset.getEvolutionParents("674d9d7f78f92a3cea19392b853d3f39e6482959").length == 0;
        assert dataset.getEvolutionParents("d398531661b986467c2f15e7ef3b1429f0d4ad54").length == 1;
        assert dataset.getEvolutionParents("d398531661b986467c2f15e7ef3b1429f0d4ad54")[0].id().equals("674d9d7f78f92a3cea19392b853d3f39e6482959");
        assert dataset.getEvolutionParents("6e0a4e66c09be9850d5dc5537ac9980c369fb392").length == 1;
        assert dataset.getEvolutionParents("6e0a4e66c09be9850d5dc5537ac9980c369fb392")[0].id().equals("907d04e53eb1dc242cc05c3137c7a794c9639172");
        assert dataset.getEvolutionParents("c69c1a5544c4d6b074f446c536bc8b5ff85cfa52")[0].id().equals("8e9b1f5c820093e42030794dc414891f899a58f9");
        assert dataset.getEvolutionParents("8f12802ceab73ba61235b7943196f11968b49472")[0].id().equals("426e2cdb99131fbbf5e8bba658f7641213ffadca");
        assert dataset.getEvolutionParents("426e2cdb99131fbbf5e8bba658f7641213ffadca")[0].id().equals("c69c1a5544c4d6b074f446c536bc8b5ff85cfa52");
        assert dataset.getEvolutionParents("8e9b1f5c820093e42030794dc414891f899a58f9")[0].id().equals("37c16cf271fa87c8f32514127837be4ce236f21e");
    }

    @Test
    public void variabilityHistoryBuildCorrectly() {
        // We created a test VariabilityRepo for which we manually selected success and error commits. The following
        // commit lists represent all sequences of success commits that were created. Any deviation from these sequences
        // indicates a bug in loading the VariabilityRepo
        var firstList = new String[]{"674d9d7f78f92a3cea19392b853d3f39e6482959", "d398531661b986467c2f15e7ef3b1429f0d4ad54"};
        var secondList = new String[]{"907d04e53eb1dc242cc05c3137c7a794c9639172", "6e0a4e66c09be9850d5dc5537ac9980c369fb392"};

        var thirdList = new String[]{"ee7e6aaa41a5e69734bf1acea8e5f1e430f2e555", "301d31223fa90a57f6492d65ca6730371d606b6c", "c302e501b6581a9383edc37f35780cf6f6d4a7b9"};
        var fourthList = new String[]{"544e9b9dadf945fc4d109f81ce52a11192ce0ea8", "37c16cf271fa87c8f32514127837be4ce236f21e", "8e9b1f5c820093e42030794dc414891f899a58f9", "c69c1a5544c4d6b074f446c536bc8b5ff85cfa52", "426e2cdb99131fbbf5e8bba658f7641213ffadca", "8f12802ceab73ba61235b7943196f11968b49472"};

        VariabilityHistory history = dataset.getCommitSequencesForEvolutionStudy();
        var commitSequences = history.commitSequences();
        // Check the size
        assert commitSequences.size() == 4;

        for (var sequence : commitSequences) {
            switch (sequence.size()) {
                case 2 -> {
                    // The retrieved sequence contains 2 commits, so it must contain the same commits as either firstList or secondList
                    if (firstList[0].equals(sequence.get(0).id())) {
                        assertCommitIdsAreEqual(firstList, sequence);
                    } else {
                        assertCommitIdsAreEqual(secondList, sequence);
                    }
                }
                // The retrieved sequence contains three commits so it must contain the same commits as thirdList
                case 3 -> assertCommitIdsAreEqual(thirdList, sequence);
                // The retrieved sequence contains six commits so it must contain the same commits as fourthList
                case 6 -> assertCommitIdsAreEqual(fourthList, sequence);
                // The retrieved sequence contains an unexpected number of commits and the history is therefore incorrect
                default -> throw new AssertionError("Invalid number of commits in the sequence.");
            }
        }
    }

    private void assertCommitIdsAreEqual(String[] ids, List<VariabilityCommit> commits) {
        assert ids.length == commits.size();
        for (int i = 0; i < ids.length; i++) {
            assert ids[i].equals(commits.get(i).id());
        }
    }
}


