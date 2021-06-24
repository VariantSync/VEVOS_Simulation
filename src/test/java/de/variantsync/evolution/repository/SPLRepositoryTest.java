package de.variantsync.evolution.repository;

import de.variantsync.evolution.util.GitUtil;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.variability.SPLCommit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class SPLRepositoryTest {
    // TODO: use more suitable repo for test? CESM just used to test git functionality.
    private static final String exampleRepoURI = "https://github.com/ESCOMP/CESM";
    private static final File exampleRepoDir;
    private static final Path tempTestRepoDir;
    private static final Path exampleRepoPath;
    private SPLRepository repo;

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
            if(testGit != null){
                testGit.close();
            }
        }
    }

    @Before
    public void initialize() throws IOException, GitAPIException {
        repo = new SPLRepository(exampleRepoPath);
        repo.git().checkout().setName("master").call();
    }

    @Test
    public void testCheckoutBranch() throws IOException, GitAPIException {
        String expectedBranch = "cesm2_tutorial";
        Branch branch = new Branch(expectedBranch);
        repo.checkoutBranch(branch);
        String actualBranch = repo.git().getRepository().getBranch();
        assert expectedBranch.equals(actualBranch);
    }

    @Test
    public void testCheckoutSameBranch() throws IOException, GitAPIException {
        String expectedBranch = "cesm2_tutorial";
        Branch branch = new Branch(expectedBranch);
        repo.checkoutBranch(branch);
        repo.checkoutBranch(branch);
        String actualBranch = repo.git().getRepository().getBranch();
        assert expectedBranch.equals(actualBranch);
    }

    @Test
    public void testGetCurrentCommit() throws IOException {
        String expectedId = "463ce6e2d40796b04fa3520201f21c5842e1b2b5"; // current commit of master branch
        SPLCommit commit = repo.getCurrentCommit();
        assert expectedId.equals(commit.id());
    }


    @Test
    public void testCheckoutCommit() throws GitAPIException, IOException {
        String expectedPrevious = "463ce6e2d40796b04fa3520201f21c5842e1b2b5"; // current commit of master branch
        String expectedCheckedOut = "1a98411c7e006d58cadd1691449ca271568893d5";

        SPLCommit previous = repo.checkoutCommit(new SPLCommit(expectedCheckedOut));
        SPLCommit checkedOut = repo.getCurrentCommit();

        assert expectedPrevious.equals(previous.id());
        assert expectedCheckedOut.equals(checkedOut.id());
    }

    @After
    public void tearDown(){
        repo.close();
    }
}