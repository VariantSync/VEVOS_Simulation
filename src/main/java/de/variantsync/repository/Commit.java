package de.variantsync.repository;

import java.util.Collection;

public abstract class Commit<T extends IRepository> {
    private final String commitId;

    public Commit(String commitId) {
        this.commitId = commitId;
    }

    public String id() {
        return commitId;
    }

    public static <U extends IRepository> boolean contains(Collection<? extends Commit<U>> commits, String id) {
        return commits.stream().anyMatch(c -> c.id().equals(id));
    }
}
