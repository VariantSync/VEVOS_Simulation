package de.variantsync.evolution.variability.pc.visitor.common;

import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.pc.ArtefactTree;
import de.variantsync.evolution.variability.pc.LineBasedAnnotation;
import de.variantsync.evolution.variability.pc.SourceCodeFile;
import de.variantsync.evolution.variability.pc.visitor.ArtefactVisitor;
import de.variantsync.evolution.variability.pc.visitor.LineBasedAnnotationVisitorFocus;
import de.variantsync.evolution.variability.pc.visitor.SourceCodeFileVisitorFocus;
import de.variantsync.evolution.variability.pc.visitor.SyntheticArtefactTreeNodeVisitorFocus;
import org.prop4j.Node;

import java.io.FileNotFoundException;

public class PCQuery implements ArtefactVisitor {
    private final CaseSensitivePath path;
    private final int lineNumber;

    private SourceCodeFile foundFile = null;
    private boolean lineFound = false;
    private Node result = null;

    public PCQuery(final CaseSensitivePath path, final int lineNumber) {
        this.path = path;
        this.lineNumber = lineNumber;
    }

    public Result<Node, Exception> getResult() {
        if (foundFile != null) {
            if (lineFound) {
                return Result.Success(result);
            }

            return Result.Failure(new IndexOutOfBoundsException(
                    "Given line number "
                            + lineNumber
                            + " is not within bounds of file "
                            + path
                            + " that ranges from "
                            + foundFile.getRootAnnotation().getLineFrom()
                            + " to "
                            + foundFile.getRootAnnotation().getLineTo()
                            + "."
            ));
        }

        return Result.Failure(new FileNotFoundException("Could not find file " + path.toString() + "!"));
    }

    @Override
    public <C extends ArtefactTree<?>> void visitGenericArtefactTreeNode(SyntheticArtefactTreeNodeVisitorFocus<C> focus) {
//        Logger.info("visitGenericArtefactTreeNode(" + focus.getValue() + ")");
        if (foundFile == null) {
            focus.visitAllSubtrees(this);
        }
    }

    @Override
    public void visitSourceCodeFile(SourceCodeFileVisitorFocus focus) {
//        Logger.info("visitSourceCodeFile(" + focus.getValue() + ")");
        if (foundFile == null && focus.getValue().getFile().equals(path)) {
            foundFile = focus.getValue();
            focus.skipRootAnnotationButVisitItsSubtrees(this);
        }
    }

    @Override
    public void visitLineBasedAnnotation(LineBasedAnnotationVisitorFocus focus) {
//        Logger.info("visitLineBasedAnnotation(" + focus.getValue() + ")");
        final LineBasedAnnotation val = focus.getValue();
        if (!lineFound && val.annotates(lineNumber)) {
            focus.visitAllSubtrees(this);
            if (!lineFound) {
                result = val.getPresenceCondition();
                lineFound = true;
            }
        }
    }
}
