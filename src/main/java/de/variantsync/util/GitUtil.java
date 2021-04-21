package de.variantsync.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

// TODO: Exchange with https://spgit.informatik.uni-ulm.de/research-projects/variantsync/theses/soeren-viegener-bachelor/-/blob/master/tool/src/main/java/load/GitLoader.java if more functionality is required
public class GitUtil {

    public static Git loadGitRepo(File repoDir) throws IOException {
        try {
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(repoDir, ".git"))
                    .build();

            return new Git(repository);
        } catch (IOException e) {
            Logger.exception("Was not able to load git repo: ", e);
            throw e;
        }
    }

}
