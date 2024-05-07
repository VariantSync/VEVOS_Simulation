package org.variantsync.vevos.simulation.variability.pc.visitor.common;

import java.io.FileNotFoundException;

import org.prop4j.Node;
import org.prop4j.True;
import org.variantsync.functjonal.Result;
import org.variantsync.vevos.simulation.util.io.CaseSensitivePath;
import org.variantsync.vevos.simulation.variability.pc.ArtefactTree;
import org.variantsync.vevos.simulation.variability.pc.LineBasedAnnotation;
import org.variantsync.vevos.simulation.variability.pc.SourceCodeFile;
import org.variantsync.vevos.simulation.variability.pc.visitor.ArtefactVisitor;
import org.variantsync.vevos.simulation.variability.pc.visitor.LineBasedAnnotationVisitorFocus;
import org.variantsync.vevos.simulation.variability.pc.visitor.SourceCodeFileVisitorFocus;
import org.variantsync.vevos.simulation.variability.pc.visitor.SyntheticArtefactTreeNodeVisitorFocus;

public class ContainsVariabilityQuery implements ArtefactVisitor {
    private final CaseSensitivePath relativePath;

    private SourceCodeFile foundFile = null;
    private boolean containsVariability = false;

    public ContainsVariabilityQuery(final CaseSensitivePath relativePath) {
        this.relativePath = relativePath;
    }

    public Result<Boolean, Exception> getResult() {
        if (foundFile != null) {
            return Result.Success(containsVariability);
        }

        return Result.Failure(new FileNotFoundException("Could not find file " + relativePath.toString() + "!"));
    }

    @Override
    public <C extends ArtefactTree<?>> void visitGenericArtefactTreeNode(
            final SyntheticArtefactTreeNodeVisitorFocus<C> focus) {
        // Logger.info("visitGenericArtefactTreeNode(" + focus.getValue() + ")");
        for (int i = 0; foundFile == null && i < focus.getValue().getNumberOfSubtrees(); ++i) {
            focus.visitSubtree(i, this);
            if (this.containsVariability) {
                // Return if variability has been found
                return;
            }
        }
    }

    @Override
    public void visitSourceCodeFile(final SourceCodeFileVisitorFocus focus) {
        // Logger.info("visitSourceCodeFile(" + focus.getValue() + ")");
        if (foundFile == null && focus.getValue().getFile().equals(relativePath)) {
            foundFile = focus.getValue();
            focus.visitRootAnnotation(this);
        }
    }

    @Override
    public void visitLineBasedAnnotation(final LineBasedAnnotationVisitorFocus focus) {
        // Logger.info("visitLineBasedAnnotation(" + focus.getValue() + ")");
        final LineBasedAnnotation val = focus.getValue();
        if (!val.getPresenceCondition().equals(new True())) {
            // If it is not simply true, it contains variability
            // We only set the value if the condition is true, so that it is not set to
            // false by other calls
            this.containsVariability = true;
            return;
        }
        focus.visitAllSubtrees(this);
    }
}
