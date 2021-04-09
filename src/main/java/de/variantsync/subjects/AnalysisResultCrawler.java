package de.variantsync.subjects;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class AnalysisResultCrawler {

    public static void crawl(Path pathToRepo) throws IOException, GitAPIException {
        Iterable<RevCommit> commitIterable;
        Repository repository = new FileRepositoryBuilder()
                .setGitDir(new File(pathToRepo.toFile(), ".git"))
                .build();
        Git git = new Git(repository);
        commitIterable = git.log().all().call();

        for (var commit : commitIterable) {
            // Check out the commit
            git.checkout().setName(commit.getName()).call();

            // Check whether the commit resulted in an error

            // Map commit to Linux revision
        }
    }
}
