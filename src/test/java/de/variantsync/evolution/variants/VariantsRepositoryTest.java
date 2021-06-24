package de.variantsync.evolution.variants;

import de.variantsync.evolution.repository.Branch;
import de.variantsync.evolution.util.GitUtil;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.list.NonEmptyList;
import de.variantsync.evolution.variants.blueprints.VariantsRevisionBlueprint;
import de.variantsync.evolution.variants.blueprints.VariantsRevisionFromVariabilityBlueprint;
import junit.framework.TestCase;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class VariantsRepositoryTest extends TestCase {
    private VariantsRepository repo;
    private static final String exampleRepoURI = "https://github.com/ESCOMP/CESM";
    private static final File exampleRepoDir;
    private static final Path tempTestRepoDir;
    private static final Path exampleRepoPath;

    static {
        Logger.initConsoleLogger();
        Git testGit = null;

        try {
            tempTestRepoDir = Files.createDirectories(Paths.get("temporary-test-repos"));
            exampleRepoDir = new File(tempTestRepoDir.toFile(), "example-repo");

            if (!exampleRepoDir.exists()) {
                testGit = GitUtil.fromRemote(exampleRepoURI, "example-repo", tempTestRepoDir.toString());
                exampleRepoPath = exampleRepoDir.toPath();
                testGit.checkout().
                        setCreateBranch(true).
                        setName("cesm2_tutorial").
                        setStartPoint("origin/" + "cesm2_tutorial").
                        call();
                testGit.checkout().setName("master").call();
            } else {
                exampleRepoPath = exampleRepoDir.toPath();
            }

        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        } finally {
            if (testGit != null) {
                testGit.close();
            }
        }
    }
    /*
       TODO: Provide valid setup for VariantsRepo
       DOES NOT WORK AT THE MOMENT BECAUSE VARIANTSREPO IS NOT INITIALIZED PROPERLY.
     */

    public void setUp() throws Exception {
        super.setUp();
        List<VariantsRevisionBlueprint> list = new ArrayList<VariantsRevisionBlueprint>();
        list.add(new VariantsRevisionFromVariabilityBlueprint(null, null));
        repo = new VariantsRepository(exampleRepoPath, null, new NonEmptyList<>(list));
    }

    static {
        Logger.initConsoleLogger();
    }

    public void tearDown() throws Exception {
        repo.close();
    }

    public void testIdToCommit() throws GitAPIException, IOException {
        String id = "22967d8e4cb9f35392b1614d6d1ac4b8b0f897d3";

        repo.checkoutBranch(new Branch("master"));
        VariantCommit commit = repo.idToCommit(id);

        assert commit.id().equals(id);
        assert commit.branch().name().equals("master");
    }

    public void testEmptyCommit() throws GitAPIException, IOException {
        Optional<VariantCommit> result = repo.commit("empty");
        assert result.isEmpty();
    }

    public void testCommit() throws IOException, GitAPIException {
        String p = exampleRepoPath + File.separator + "hi" + (new Random()).nextInt() + ".txt";
        File f = new File(p);
        f.getParentFile().mkdirs();
        f.createNewFile();

        Optional<VariantCommit> result = repo.commit("commit");
        assert result.isPresent();
    }
}