package de.variantsync.evolution.repository;

import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.variability.SPLCommit;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SPLRepositoryTest {
    private final String path = "D:\\Maike\\git\\cesm";
    private SPLRepository repo;

    static {
        Logger.initConsoleLogger();
    }
    @Before
    public void initialize(){
        Path p = Paths.get(path);
        repo = new SPLRepository(p);
    }

    @Test
    public void branchCheckedOut() throws IOException {
        String expectedBranch = "cesm2_tutorial";
        Branch branch = new Branch(expectedBranch);
        repo.checkoutBranch(branch);
        String actualBranch = repo.git().getRepository().getBranch();
        assert expectedBranch.equals(actualBranch);
    }

    @Test
    public void checkoutSameBranchTwice() throws IOException {
        String expectedBranch = "cesm2_tutorial";
        Branch branch = new Branch(expectedBranch);
        repo.checkoutBranch(branch);
        repo.checkoutBranch(branch);
        String actualBranch = repo.git().getRepository().getBranch();
        assert expectedBranch.equals(actualBranch);
    }

    @Test
    public void currentCommit() {
        String expectedId = "357eb81ec77a126778f536d886040115b3d99727";
        repo.checkoutBranch(new Branch("master"));
        SPLCommit commit = repo.getCurrentCommit();
        assert expectedId.equals(commit.id());
    }


    @Test
    public void commitCheckedOut(){
        String expectedPrevious = "357eb81ec77a126778f536d886040115b3d99727";
        String expectedCheckedOut = "22967d8e4cb9f35392b1614d6d1ac4b8b0f897d3";

        repo.checkoutBranch(new Branch("master"));

        SPLCommit previous = repo.checkoutCommit(new SPLCommit(expectedCheckedOut));
        SPLCommit checkedOut = repo.getCurrentCommit();

        assert expectedPrevious.equals(previous.id());
        assert expectedCheckedOut.equals(checkedOut.id());
    }


}