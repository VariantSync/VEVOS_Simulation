package de.variantsync.evolution.repository;

import de.variantsync.evolution.util.GitUtil;
import de.variantsync.evolution.util.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

import java.io.IOException;
import java.nio.file.Path;

public abstract class Repository<C extends Commit<? extends IRepository<C>>> implements IRepository<C>{
    private Git git;
    private final Path path;
    private C currentCommit;

    public Repository(Path path){
        this.path = path;
    }

    @Override
    public C checkoutCommit(C c) throws GitAPIException, IOException {
        try {
            C previous = getCurrentCommit();
            Ref ref = git().checkout().setName(c.id()).call();
            currentCommit = idToCommit(ObjectId.toString(ref.getObjectId()));
            return previous;
        } catch (GitAPIException | IOException e) {
            Logger.exception("Failed to checkout commit " + c, e);
            throw e;
        }
    }

    @Override
    public void checkoutBranch(Branch branch) throws GitAPIException, IOException {
        try {
            git().checkout().setName(branch.name()).call();
        } catch (GitAPIException | IOException e) {
            Logger.exception("Failed to checkout branch " + branch.name(), e);
            throw e;
        }
    }

    @Override
    public C getCurrentCommit() throws IOException {
        if(currentCommit == null){
            currentCommit = idToCommit(getCurrentCommitId());
        }

        return currentCommit;
    }

    public abstract C idToCommit(String id) throws IOException;

    protected String getCurrentCommitId() throws IOException {
        String commitId = "";

        try {
            ObjectId head = git().getRepository().resolve(Constants.HEAD);
            // commitId = head.getName();
            commitId = ObjectId.toString(head);
        } catch (IOException e) {
            Logger.exception("Failed to get current commit", e);
            throw e;
        }

        return commitId;
    }

    @Override
    public Path getPath() {
        return path;
    }

    public Branch getCurrentBranch() throws IOException {
        String branch = git().getRepository().getBranch();
        return new Branch(branch);
    }

    protected Git git() throws IOException {
        if(git == null){
            try {
                git = GitUtil.loadGitRepo(path.toFile());
            } catch (IOException e) {
                Logger.exception("Failed to load repository" + path, e);
                throw e;
            }
        }

        return git;
    }
}
