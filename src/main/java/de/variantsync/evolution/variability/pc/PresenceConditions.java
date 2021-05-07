package de.variantsync.evolution.variability.pc;

import de.variantsync.evolution.feature.Variant;
import de.variantsync.evolution.repository.ISPLRepository;
import de.variantsync.evolution.repository.IVariantsRepository;
import de.variantsync.evolution.util.NotImplementedException;

import java.util.List;
import java.util.Objects;

public record PresenceConditions(List<SourceCodeFile> files) {
    // TODO: Implement Issue #1 somewhere here? Also in generateVariant?

    public void generateVariant(Variant variant, ISPLRepository splRepo, IVariantsRepository variantsRepo) {
        // TODO: Implement Issue #2 here.
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return "PresenceConditions: [" +
                files.stream().map(SourceCodeFile::toString).reduce((a, b) -> a + ",\n  " + b) +
                "]";
    }

    public String prettyPrint() {
        final StringBuilder builder = new StringBuilder();
        final String indent = "";
        for (SourceCodeFile file : files) {
            file.prettyPrint(indent, builder);
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PresenceConditions that = (PresenceConditions) o;
        return files.equals(that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(files);
    }
}
