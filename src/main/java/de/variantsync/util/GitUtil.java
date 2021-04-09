package de.variantsync.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

public class GitUtil {
    private static final SimpleConsoleLogger LOGGER = SimpleConsoleLogger.get();

    public static Git loadGitRepo(File repoDir) throws IOException {
        try {
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(repoDir, ".git"))
                    .build();

            return new Git(repository);
        } catch (IOException e) {
            LOGGER.exception("Was not able to load git repo: ", e);
            throw e;
        }
    }
    public static void cloneRepo(File targetDir, String uri) throws GitAPIException {
        LOGGER.info("Cloning " + uri + " into " + targetDir);
        Git.cloneRepository().setURI(uri).setCloneAllBranches(true).setDirectory(targetDir).call();
        LOGGER.info("Cloning complete.");
    }
}
