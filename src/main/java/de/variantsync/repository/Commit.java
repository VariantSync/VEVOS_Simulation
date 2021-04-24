package de.variantsync.repository;

import java.util.Collection;

public abstract class Commit<T extends IRepository<? extends Commit<T>>> {
    private final String commitId;

    public Commit(String commitId) {
        this.commitId = commitId;
    }

    public String id() {
        return commitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commit<?> commit = (Commit<?>) o;
        return commitId.equals(commit.commitId);
    }

    public static <U extends IRepository<? extends Commit<U>>> boolean contains(Collection<? extends Commit<U>> commits, String id) {
        return commits.stream().anyMatch(c -> c.id().equals(id));
    }
}
