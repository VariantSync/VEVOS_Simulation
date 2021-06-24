package de.variantsync.evolution.repository;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Models a repository of a version control system such as Git.
 * @param <C> Type of commits of this repository.
 */
public interface IRepository<C extends Commit<? extends IRepository<C>>> extends AutoCloseable {
    /**
     * Check out the given commit in the local copy of the repository.
     * (Method is designed for operating on a single branch only).
     * @param c The commit to check out. Will be the @see getCurrentCommitAfterwards.
     * @return The commit that was previously the current commit.
     */
    C checkoutCommit(C c) throws GitAPIException, IOException;

    /**
     * Check out the given branch (`git checkout <branchname>`).
     * Afterwards, the contents of the given branch will be on disk.
     * @param branch The branch to check out.
     */
    void checkoutBranch(Branch branch) throws GitAPIException, IOException;

    /**
     * @return The commit HEAD is currently pointing to (i.e., the commit whose state of the repository we see on disk currently).
     */
    C getCurrentCommit() throws IOException;

    /**
     * @return The path in the file system to a local copy of this repository.
     */
    Path getPath();
}
