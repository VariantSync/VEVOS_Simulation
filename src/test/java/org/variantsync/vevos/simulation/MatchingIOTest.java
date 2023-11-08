package org.variantsync.vevos.simulation;

import org.junit.Assert;
import org.junit.Test;
import org.variantsync.vevos.simulation.io.data.CSV;
import org.variantsync.vevos.simulation.io.data.CSVIO;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.CodeMatching;

import java.nio.file.Path;

public class MatchingIOTest {
    final Path FIRST_FILE = Path.of("miscutils/bc.c");
    final Path SECOND_FILE = Path.of("anotherFile.c");
    final Path THIRD_FILE = Path.of("thirdFile.c");

    private CodeMatching loadCorrectMatching() {
        VEVOS.Initialize();
        CSVIO io = new CSVIO();
        CSV csvBefore = io.load(Path.of("src/test/resources/matching/correct/code-matching.before.spl.csv")).getSuccess();
        CSV csvAfter = io.load(Path.of("src/test/resources/matching/correct/code-matching.after.spl.csv")).getSuccess();
        return CodeMatching.fromCSVs(csvBefore, csvAfter);
    }

    private void loadIncorrectMatching() {
        VEVOS.Initialize();
        CSVIO io = new CSVIO();
        CSV csvBefore = io.load(Path.of("src/test/resources/matching/incorrect/code-matching.before.spl.csv")).getSuccess();
        CSV csvAfter = io.load(Path.of("src/test/resources/matching/incorrect/code-matching.after.spl.csv")).getSuccess();
        CodeMatching.fromCSVs(csvBefore, csvAfter);
    }

    @Test
    public void allFilesLoaded() {
        CodeMatching matching = loadCorrectMatching();
        Assert.assertEquals(0, (int) matching.beforeCommitMatch(FIRST_FILE, 0).get());
        Assert.assertEquals(0, (int) matching.beforeCommitMatch(SECOND_FILE, 0).get());
        Assert.assertEquals(0, (int) matching.beforeCommitMatch(THIRD_FILE, 0).get());
    }

    @Test
    public void matchesAreCorrect() {
        CodeMatching matching = loadCorrectMatching();
        Assert.assertEquals(1, (int) matching.beforeCommitMatch(FIRST_FILE, 1).get());
        Assert.assertEquals(3, (int) matching.beforeCommitMatch(FIRST_FILE, 3).get());

        Assert.assertEquals(-1, (int) matching.beforeCommitMatch(SECOND_FILE, 1).get());
        Assert.assertEquals(1, (int) matching.beforeCommitMatch(SECOND_FILE, 3).get());
        Assert.assertEquals(3, (int) matching.afterCommitMatch(SECOND_FILE, 1).get());

        Assert.assertEquals(1, (int) matching.beforeCommitMatch(THIRD_FILE, 1).get());
        Assert.assertEquals(-1, (int) matching.afterCommitMatch(THIRD_FILE, 2).get());
        Assert.assertEquals(5, (int) matching.beforeCommitMatch(THIRD_FILE, 3).get());
        Assert.assertEquals(3, (int) matching.afterCommitMatch(THIRD_FILE, 5).get());
        Assert.assertEquals(-1, (int) matching.afterCommitMatch(THIRD_FILE, 6).get());
    }

    @Test
    public void findDisagreement() {
        Assert.assertThrows(IllegalArgumentException.class, this::loadIncorrectMatching);
    }
}
