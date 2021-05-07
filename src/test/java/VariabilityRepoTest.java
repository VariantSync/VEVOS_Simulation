import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import de.variantsync.evolution.repository.Commit;
import de.variantsync.evolution.repository.VariabilityHistory;
import de.variantsync.evolution.util.GitUtil;
import de.variantsync.evolution.variability.CommitPair;
import de.variantsync.evolution.variability.VariabilityCommit;
import de.variantsync.evolution.variability.VariabilityRepo;
import de.variantsync.evolution.util.GenericArray;
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
import java.util.Set;

public class VariabilityRepoTest {
    private static final String simpleHistoryRepoURI = "https://gitlab.informatik.hu-berlin.de/mse/SampleRepos/SimpleHistory.git";
    private static final String simpleVariabilityRepoURI = "https://gitlab.informatik.hu-berlin.de/mse/SampleRepos/SimpleVariabilityRepo.git";
    private static final File simpleHistoryRepoDir;
    private static final File simpleVariabilityRepoDir;
    private static final Path tempTestRepoDir;

    static {
        Logger.initConsoleLogger();
        try {
            tempTestRepoDir = Files.createDirectories(Paths.get("temporary-test-repos"));
            simpleHistoryRepoDir = new File(tempTestRepoDir.toFile(), "simple-history");
            simpleVariabilityRepoDir = new File(tempTestRepoDir.toFile(), "simple-variability");

            if (!simpleHistoryRepoDir.exists()) {
                GitUtil.fromRemote(simpleHistoryRepoURI, "simple-history", tempTestRepoDir.toString());
            }
            if (!simpleVariabilityRepoDir.exists()) {
                GitUtil.fromRemote(simpleVariabilityRepoURI, "simple-variability", tempTestRepoDir.toString());
            }
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private VariabilityRepo repo;

    @Before
    public void initializeRepo() throws GitAPIException, IOException {
        repo = VariabilityRepo.load(simpleVariabilityRepoDir, simpleHistoryRepoDir);
    }

    @Test
    public void successCommitsAreLoaded() {
        final String[] expectedSuccessCommits = new String[]{
                "674d9d7f78f92a3cea19392b853d3f39e6482959",
                "d398531661b986467c2f15e7ef3b1429f0d4ad54",

                "ca3644a751d12f6893a170deaf3acfd6be0fd7e2",

                "907d04e53eb1dc242cc05c3137c7a794c9639172",
                "6e0a4e66c09be9850d5dc5537ac9980c369fb392",

                "ee7e6aaa41a5e69734bf1acea8e5f1e430f2e555",
                "301d31223fa90a57f6492d65ca6730371d606b6c",
                "c302e501b6581a9383edc37f35780cf6f6d4a7b9",

                "544e9b9dadf945fc4d109f81ce52a11192ce0ea8",
                "37c16cf271fa87c8f32514127837be4ce236f21e",
                "8f12802ceab73ba61235b7943196f11968b49472",
                "c69c1a5544c4d6b074f446c536bc8b5ff85cfa52",
                "426e2cdb99131fbbf5e8bba658f7641213ffadca",
                "8e9b1f5c820093e42030794dc414891f899a58f9"
        };

        Set<VariabilityCommit> successCommits = repo.getSuccessCommits();
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

        Set<VariabilityCommit> errorCommits = repo.getErrorCommits();
        for (var expectedCommit : expectedErrorCommits) {
            assert Commit.contains(errorCommits, expectedCommit);
        }
        assert errorCommits.size() == expectedErrorCommits.length;
    }

    @Test
    public void initialCommitDiscarded() {
        assert repo.getVariabilityCommit("ebbe5041a6d15964251aee37b1b2ea81946f790b") == null;
    }

    @Test
    public void logicalParentsAreLoaded() {
        assert repo.getVariabilityCommit("674d9d7f78f92a3cea19392b853d3f39e6482959").getEvolutionParents().length == 0;
        assert repo.getVariabilityCommit("d398531661b986467c2f15e7ef3b1429f0d4ad54").getEvolutionParents().length == 1;
        assert repo.getVariabilityCommit("d398531661b986467c2f15e7ef3b1429f0d4ad54").getEvolutionParents()[0].id().equals("674d9d7f78f92a3cea19392b853d3f39e6482959");
        assert repo.getVariabilityCommit("6e0a4e66c09be9850d5dc5537ac9980c369fb392").getEvolutionParents().length == 1;
        assert repo.getVariabilityCommit("6e0a4e66c09be9850d5dc5537ac9980c369fb392").getEvolutionParents()[0].id().equals("907d04e53eb1dc242cc05c3137c7a794c9639172");
        assert repo.getVariabilityCommit("c69c1a5544c4d6b074f446c536bc8b5ff85cfa52").getEvolutionParents()[0].id().equals("8e9b1f5c820093e42030794dc414891f899a58f9");
        assert repo.getVariabilityCommit("8f12802ceab73ba61235b7943196f11968b49472").getEvolutionParents()[0].id().equals("426e2cdb99131fbbf5e8bba658f7641213ffadca");
        assert repo.getVariabilityCommit("426e2cdb99131fbbf5e8bba658f7641213ffadca").getEvolutionParents()[0].id().equals("c69c1a5544c4d6b074f446c536bc8b5ff85cfa52");
        assert repo.getVariabilityCommit("8e9b1f5c820093e42030794dc414891f899a58f9").getEvolutionParents()[0].id().equals("37c16cf271fa87c8f32514127837be4ce236f21e");
    }

    @Test
    public void correctCommitsWithOneParentFiltered() {
        final Pair<String, String>[] expectedCommitPairs = GenericArray.create(
                new Pair<>("d398531661b986467c2f15e7ef3b1429f0d4ad54", "674d9d7f78f92a3cea19392b853d3f39e6482959"),
                new Pair<>("6e0a4e66c09be9850d5dc5537ac9980c369fb392", "907d04e53eb1dc242cc05c3137c7a794c9639172"),

                new Pair<>("301d31223fa90a57f6492d65ca6730371d606b6c", "ee7e6aaa41a5e69734bf1acea8e5f1e430f2e555"),
                new Pair<>("c302e501b6581a9383edc37f35780cf6f6d4a7b9", "301d31223fa90a57f6492d65ca6730371d606b6c"),

                new Pair<>("37c16cf271fa87c8f32514127837be4ce236f21e", "544e9b9dadf945fc4d109f81ce52a11192ce0ea8"),
                new Pair<>("8f12802ceab73ba61235b7943196f11968b49472", "426e2cdb99131fbbf5e8bba658f7641213ffadca"),
                new Pair<>("c69c1a5544c4d6b074f446c536bc8b5ff85cfa52", "8e9b1f5c820093e42030794dc414891f899a58f9"),
                new Pair<>("426e2cdb99131fbbf5e8bba658f7641213ffadca", "c69c1a5544c4d6b074f446c536bc8b5ff85cfa52"),
                new Pair<>("8e9b1f5c820093e42030794dc414891f899a58f9", "37c16cf271fa87c8f32514127837be4ce236f21e")
        );

        Set<CommitPair> pairs = repo.getCommitPairsForEvolutionStudy();
        for (var commitPair : expectedCommitPairs) {
            try {
                assert pairs.stream().anyMatch(p -> p.child().id().equals(commitPair.getKey()) && p.parent().id().equals(commitPair.getValue()));
            } catch (AssertionError e) {
                System.err.println("No match for <" + commitPair.getKey() + " : " + commitPair.getValue() + ">");
                throw e;
            }
        }
        assert repo.getCommitPairsForEvolutionStudy().size() == expectedCommitPairs.length;
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

        VariabilityHistory history = repo.getCommitSequencesForEvolutionStudy();
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

    @Test
    public void splCommitsMapped() {
        final String[] variabilityCommits = new String[]{
                "674d9d7f78f92a3cea19392b853d3f39e6482959",
                "d398531661b986467c2f15e7ef3b1429f0d4ad54",
                "7cc1135c70cb6ce92bc3d41a7fbff984b2c0e3ea",
                "1915b9aa580c6e3a332146b3a579f015db627377",
                "ca3644a751d12f6893a170deaf3acfd6be0fd7e2",
                "bd020e4f40e2726f138c901f6fa44a106a55a56d",
                "907d04e53eb1dc242cc05c3137c7a794c9639172",
                "6e0a4e66c09be9850d5dc5537ac9980c369fb392",
                "55f9b80f8396c0f2a1a8f9f9cc314425bf231b0f",
                "ee7e6aaa41a5e69734bf1acea8e5f1e430f2e555",
                "301d31223fa90a57f6492d65ca6730371d606b6c",
                "c302e501b6581a9383edc37f35780cf6f6d4a7b9",
                "7f08e8f9e2e53f2d16db73b7752f03953ffe1df8",
                "544e9b9dadf945fc4d109f81ce52a11192ce0ea8",
                "37c16cf271fa87c8f32514127837be4ce236f21e",
                "8f12802ceab73ba61235b7943196f11968b49472",
                "c69c1a5544c4d6b074f446c536bc8b5ff85cfa52",
                "426e2cdb99131fbbf5e8bba658f7641213ffadca",
                "8e9b1f5c820093e42030794dc414891f899a58f9",
        };

        final String[] splCommits = new String[]{
                "c11c2aff04769cbcb6c568f90257dc3cc7bb1737",
                "aed45b2f723e372b750e8007c72730bcddee7174",
                "e12024473264e88058027290a348d1ada31af20a",
                "60dc6013409b060b6f68c34902b3390e26e585dd",
                "e183211505ba66c89234b25687b76a7f4e4679cd",
                "411129f5a1923ce107b2970311fdcea72e0b628b",
                "0e1827400a0d06b9112777261c4bdb541ffbb134",
                "48c073dfcfd0907f1e460628a7379b4bcbc8c737",
                "f83a980b7c948f2a7a50c75f06e8298f971e61aa",
                "eab1607a6f137376e57a3381c2fdae9c9d46de4d",
                "78fe3d306860e11e327a43cfce2c97748a34c1e1",
                "80e14e6db5ef9ec4149887b544621d10a19edf92",
                "4ea494802cd552464c2f1a47d727a206eccf1d20",
                "454f7da158fdf3fe4b3c3fc8110f6c15861f97fa",
                "c79e89cd49fc17be386ca026686dd01e0985a5ea",
                "38e15e31eabccf82d3183273240cd44f2dec9fa9",
                "a54c3c30f2dff6dc36331f06360630b697b7562c",
                "f0619022ca9f6aeaba51fb1b71e77f6887cca4a4",
                "600f60df96cdbbf3319085d8e777d7e66c96e013",
        };

        for (int i = 0; i < variabilityCommits.length; i++) {
            assert repo.getVariabilityCommit(variabilityCommits[i]).splCommit().id().equals(splCommits[i]);
        }
    }

    private void assertCommitIdsAreEqual(String[] ids, List<VariabilityCommit> commits) {
        assert ids.length == commits.size();
        for (int i = 0; i < ids.length; i++) {
            assert ids[i].equals(commits.get(i).id());
        }
    }
}
