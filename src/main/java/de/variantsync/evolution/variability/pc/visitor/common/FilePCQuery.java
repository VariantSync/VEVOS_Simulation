package de.variantsync.evolution.variability.pc.visitor.common;

import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.pc.ArtefactTree;
import de.variantsync.evolution.variability.pc.visitor.ArtefactVisitor;
import de.variantsync.evolution.variability.pc.visitor.LineBasedAnnotationVisitorFocus;
import de.variantsync.evolution.variability.pc.visitor.SourceCodeFileVisitorFocus;
import de.variantsync.evolution.variability.pc.visitor.SyntheticArtefactTreeNodeVisitorFocus;
import org.prop4j.Node;

import java.io.FileNotFoundException;

public class FilePCQuery implements ArtefactVisitor {
    private final CaseSensitivePath path;

    private boolean fileFound = false;
    private Node result = null;

    public FilePCQuery(final CaseSensitivePath path) {
        this.path = path;
    }

    public Result<Node, Exception> getResult() {
        if (fileFound) {
            return Result.Success(result);
        }

        return Result.Failure(new FileNotFoundException("Could not find file " + path.toString() + "!"));
    }

    @Override
    public <C extends ArtefactTree<?>> void visitGenericArtefactTreeNode(final SyntheticArtefactTreeNodeVisitorFocus<C> focus) {
        for (int i = 0; !fileFound && i < focus.getValue().getNumberOfSubtrees(); ++i) {
            focus.visitSubtree(i, this);
        }
    }

    @Override
    public void visitSourceCodeFile(final SourceCodeFileVisitorFocus focus) {
        if (!fileFound && focus.getValue().getFile().equals(path)) {
            result = focus.getValue().getPresenceCondition();
            fileFound = true;
        }
    }

    @Override
    public void visitLineBasedAnnotation(final LineBasedAnnotationVisitorFocus focus) {
        // should never be invoked
        assert false;
    }
}
