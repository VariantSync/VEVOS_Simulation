package de.variantsync.evolution.repository;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a commit for a certain repository.
 * @param <T> The repository type to instances of which the commit can be made / was made.
 */
public abstract class Commit<T extends IRepository<? extends Commit<T>>> {
    private final String commitId;

    /**
     * Creates a new commit with the given id.
     * @param commitId The hashcode of the commit in the git repository history.
     */
    public Commit(String commitId) {
        Objects.requireNonNull(commitId);
        this.commitId = commitId;
    }

    /**
     * @return The hashcode of this commit.
     */
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

    @Override
    public int hashCode() {
        // The commit is already a hash computed by git.
        return Integer.parseInt(commitId);
    }

    /**
     * Checks whether the given collection of commits contains a commit with the given hash.
     * @param commits List of commits to be checked for containment.
     * @param id Hashcode to be searched.
     * @return True, iff there exists at least one commit on the given collection with the given id.
     */
    public static <U extends IRepository<? extends Commit<U>>> boolean contains(Collection<? extends Commit<U>> commits, String id) {
        return commits.stream().anyMatch(c -> c.id().equals(id));
    }
}
