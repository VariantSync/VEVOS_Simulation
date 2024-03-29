package org.variantsync.vevos.simulation.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class GitUtil {

    /**
     * Loads a Git from a remote repository
     *
     * @param remoteUri      URI of the remote git repository
     * @param repositoryName Name of the repository. Sets the directory name in the default repositories directory where this repository is cloned to
     * @return A Git object of the repository
     */
    public static Git fromRemote(final String remoteUri, final String repositoryName, final String repoParentDir) throws GitAPIException {
        try {
            return Git.cloneRepository()
                    .setURI(remoteUri)
                    .setDirectory(Paths.get(repoParentDir, repositoryName).toFile())
                    .call();
        } catch (final GitAPIException e) {
            Logger.error("Failed to load git repo from " + remoteUri, e);
            throw e;
        }
    }

    public static Git loadGitRepo(final File repoDir) throws IOException {
        try {
            final Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(repoDir, ".git"))
                    .build();

            return new Git(repository);
        } catch (final IOException e) {
            Logger.error("Was not able to load git repo: ", e);
            throw e;
        }
    }

}
