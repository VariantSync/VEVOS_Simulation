package de.variantsync.evolution.variant;

import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.variantsync.evolution.spl.SPLRevision;
import de.variantsync.util.Functional;
import de.variantsync.util.NotImplementedException;
import org.eclipse.jgit.api.Git;

import java.util.*;

public record VariantsRevision(SPLRevision sourceSPL,
                               List<VariantRevision> variants)
        implements Iterable<VariantRevision>
{
    /**
     * Compute the next revision of variants from
     * the successor of the sourceSPL.
     *
     * @return The Variants at the next step of the evolution.
     */
    public Optional<VariantsRevision> evolve() {
        return sourceSPL.getSuccessor().map(evolvedSPL -> new VariantsRevision(evolvedSPL, Functional.fmap(variants, v -> v.evolveTo(evolvedSPL))));
    }

    public List<Configuration> getConfigurations() {
        return Functional.fmap(variants, VariantRevision::configuration);
    }

    /**
     * Writes the this revision as the next commit to the given repository.
     */
    void commitTo(Git repository) {
        /*
        - create branch for each variant if not already present
        -
         */

        throw new NotImplementedException();
    }

    @Override
    public Iterator<VariantRevision> iterator() {
        return variants.iterator();
    }
}
