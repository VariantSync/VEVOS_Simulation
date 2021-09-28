package de.variantsync.evolution.repository;

import de.variantsync.evolution.Main;
import de.variantsync.evolution.variability.SPLCommit;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

// Tests are disabled as they require custom setup. They also do not assert anything and are meant to be used when debugging.
@Ignore
public class BusyboxRepositoryTest {
    // Adjust the path to the root of Busybox for testing
    private final BusyboxRepository BUSYBOX = new BusyboxRepository(Path.of("C:\\Users\\Alex\\develop\\busybox"));

    static {
        Main.Initialize();
    }

    // Clean the repo after each test
    @After
    public void restoreFiles() throws GitAPIException, IOException {
        BUSYBOX.stashCreate(true);
        BUSYBOX.dropStash();
    }

    @Test
    public void normalize() throws IOException {
        BUSYBOX.preprocess();
    }

    @Test
    public void checkoutCommit() throws GitAPIException, IOException {
        final SPLCommit simpleCommit = new SPLCommit("f27a6a94a7fb172a6768bc450dbdec68f15bc78f");
        final SPLCommit previousCommit = BUSYBOX.checkoutCommit(simpleCommit);
        BUSYBOX.checkoutCommit(previousCommit, true);
    }

}
