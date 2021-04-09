import de.variantsync.subjects.VariabilityRepo;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

public class VariabilityRepoTest {
    private static final File simpleHistoryRepoDir = Paths.get("src", "test", "resources", "test-repos", "simple-history").toFile();
    private static final File simpleVariabilityRepoDir = Paths.get("src", "test", "resources", "test-repos", "simple-variability").toFile();

    @Test
    public void successCommitsAreLoaded() throws GitAPIException, IOException {
        VariabilityRepo repo = VariabilityRepo.load(simpleVariabilityRepoDir, simpleHistoryRepoDir);

        String[] expectedSuccessCommits = new String[]{
                "674d9d7f78f92a3cea19392b853d3f39e6482959",
                "d398531661b986467c2f15e7ef3b1429f0d4ad54",
                "ca3644a751d12f6893a170deaf3acfd6be0fd7e2",
                "907d04e53eb1dc242cc05c3137c7a794c9639172",
                "6e0a4e66c09be9850d5dc5537ac9980c369fb392",
        };

        Set<String> successCommits = repo.getSuccessCommits();
        for (var expectedCommit : expectedSuccessCommits) {
            assert successCommits.contains(expectedCommit);
        }
        assert successCommits.size() == expectedSuccessCommits.length;
    }

    @Test
    public void errorCommitsAreLoaded() throws GitAPIException, IOException {
        VariabilityRepo repo = VariabilityRepo.load(simpleVariabilityRepoDir, simpleHistoryRepoDir);

        String[] expectedErrorCommits = new String[]{
                "1915b9aa580c6e3a332146b3a579f015db627377",
                "7cc1135c70cb6ce92bc3d41a7fbff984b2c0e3ea",
                "bd020e4f40e2726f138c901f6fa44a106a55a56d",
        };

        Set<String> errorCommits = repo.getErrorCommits();
        for (var expectedCommit : expectedErrorCommits) {
            assert errorCommits.contains(expectedCommit);
        }
        assert errorCommits.size() == expectedErrorCommits.length;
    }

    @Test
    public void logicalParentsAreLoaded() throws GitAPIException, IOException {
        VariabilityRepo repo = VariabilityRepo.load(simpleVariabilityRepoDir, simpleHistoryRepoDir);

        assert repo.getExtractionParents("ebbe5041a6d15964251aee37b1b2ea81946f790b") == null;
        assert repo.getExtractionParents("674d9d7f78f92a3cea19392b853d3f39e6482959").length == 0;
        assert repo.getExtractionParents("d398531661b986467c2f15e7ef3b1429f0d4ad54").length == 1;
        assert repo.getExtractionParents("d398531661b986467c2f15e7ef3b1429f0d4ad54")[0].equals("674d9d7f78f92a3cea19392b853d3f39e6482959");
        assert repo.getExtractionParents("6e0a4e66c09be9850d5dc5537ac9980c369fb392").length == 1;
        assert repo.getExtractionParents("6e0a4e66c09be9850d5dc5537ac9980c369fb392")[0].equals("907d04e53eb1dc242cc05c3137c7a794c9639172");
    }

    @Test
    public void correctCommitsWithOneParentFiltered() throws GitAPIException, IOException {
        VariabilityRepo repo = VariabilityRepo.load(simpleVariabilityRepoDir, simpleHistoryRepoDir);

        String[] expectedSuccessCommits = new String[]{
                "d398531661b986467c2f15e7ef3b1429f0d4ad54",
                "6e0a4e66c09be9850d5dc5537ac9980c369fb392",
        };

        for (var commit : expectedSuccessCommits) {
            assert repo.getCommitsForEvolutionStudy().contains(commit);
        }
        assert repo.getCommitsForEvolutionStudy().size() == expectedSuccessCommits.length;
    }

    @Test
    public void splCommitsMapped() throws GitAPIException, IOException {
        VariabilityRepo repo = VariabilityRepo.load(simpleVariabilityRepoDir, simpleHistoryRepoDir);

        String[] variabilityCommits = new String[]{
                "674d9d7f78f92a3cea19392b853d3f39e6482959",
                "d398531661b986467c2f15e7ef3b1429f0d4ad54",
                "7cc1135c70cb6ce92bc3d41a7fbff984b2c0e3ea",
                "1915b9aa580c6e3a332146b3a579f015db627377",
                "ca3644a751d12f6893a170deaf3acfd6be0fd7e2",
                "bd020e4f40e2726f138c901f6fa44a106a55a56d",
                "907d04e53eb1dc242cc05c3137c7a794c9639172",
                "6e0a4e66c09be9850d5dc5537ac9980c369fb392",
        };

        String[] splCommits = new String[]{
                "c11c2aff04769cbcb6c568f90257dc3cc7bb1737",
                "aed45b2f723e372b750e8007c72730bcddee7174",
                "e12024473264e88058027290a348d1ada31af20a",
                "60dc6013409b060b6f68c34902b3390e26e585dd",
                "e183211505ba66c89234b25687b76a7f4e4679cd",
                "411129f5a1923ce107b2970311fdcea72e0b628b",
                "0e1827400a0d06b9112777261c4bdb541ffbb134",
                "48c073dfcfd0907f1e460628a7379b4bcbc8c737",
        };

        for (int i = 0; i < variabilityCommits.length; i++) {
            assert repo.getSPLCommit(variabilityCommits[i]).equals(splCommits[i]);
        }
    }
}
