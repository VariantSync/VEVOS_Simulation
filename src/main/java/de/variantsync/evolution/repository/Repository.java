package de.variantsync.evolution.repository;

import de.variantsync.evolution.util.GitUtil;
import de.variantsync.evolution.util.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import java.io.IOException;
import java.nio.file.Path;

public abstract class Repository<C extends Commit> implements IRepository<C>{
    private final Path path;
    private Git git;

    public Repository(final Path path){
        this.path = path;
    }

    @Override
    public C checkoutCommit(final C c) throws GitAPIException, IOException {
        try {
            final C previous = getCurrentCommit();
            git().checkout().setName(c.id()).call();
            return previous;
        } catch (final GitAPIException | IOException e) {
            Logger.error("Failed to checkout commit " + c, e);
            close();
            throw e;
        }
    }

    @Override
    public void checkoutBranch(final Branch branch) throws GitAPIException, IOException {
        try {
            git().checkout().setName(branch.name()).call();
        } catch (final GitAPIException | IOException e) {
            Logger.error("Failed to checkout branch " + branch.name(), e);
            close();
            throw e;
        }
    }

    @Override
    public C getCurrentCommit() throws IOException {
        try {
            return idToCommit(getCurrentCommitId());
        } catch(final IOException e) {
            Logger.error("Failed to get current commit.", e);
            close();
            throw e;
        }
    }

    public abstract C idToCommit(String id) throws IOException;


    private String getCurrentCommitId() throws IOException {
        final String commitId;

        try {
            final ObjectId head = git().getRepository().resolve(Constants.HEAD);
            commitId = ObjectId.toString(head);
            return commitId;
        } catch (final IOException e) {
            Logger.error("Failed to get current commit ID", e);
            close();
            throw e;
        }
    }

    @Override
    public Path getPath() {
        return path;
    }

    protected Git git() throws IOException {
        if(git == null){
            git = GitUtil.loadGitRepo(path.toFile());
        }

        return git;
    }

    public void close() {
        if(git != null){
            git.getRepository().close();
            git.close();
            git = null;
        }
    }
}
