package de.variantsync.repository;

import java.nio.file.Path;

public interface IRepository<C extends Commit<? extends IRepository<C>>> {
    /**
     * Check out the given commit in the local copy of the repository.
     * (Method is designed for operating on a single branch only).
     * @param c The commit to check out. Will be the @see getCurrentCommitAfterwards.
     * @return The commit that was previously the current commit.
     */
    C checkoutCommit(C c);

    /**
     * Check out the given branch (`git checkout <branchname>`).
     * Afterwards, the contents of the given branch will be on disk.
     * @param branch The branch to check out.
     */
    void checkoutBranch(Branch branch);

    /**
     * @return The commit HEAD is currently pointing to (i.e., the commit whose state of the repository we see on disk currently).
     */
    C getCurrentCommit();

    /**
     * @return The path in the file system to a local copy of this repository.
     */
    Path getPath();
}
