package de.variantsync.evolution.repository;

import de.variantsync.evolution.util.GitUtil;
import de.variantsync.evolution.util.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import java.io.IOException;
import java.nio.file.Path;

public abstract class Repository<C extends Commit<? extends IRepository<C>>> implements IRepository<C>{
    private Git git;
    private final Path path;

    public Repository(Path path){
        this.path = path;
    }

    @Override
    public C checkoutCommit(C c) throws GitAPIException, IOException {
        try {
            C currentCommit = getCurrentCommit();
            git().checkout().setName(c.id()).call();
            return currentCommit;
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
    public abstract C getCurrentCommit() throws IOException;

    protected String getCurrentCommitId() throws IOException {
        String commitId = "";

        try {
            ObjectId head = git().getRepository().resolve(Constants.HEAD);
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

    public Git git() throws IOException {
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
