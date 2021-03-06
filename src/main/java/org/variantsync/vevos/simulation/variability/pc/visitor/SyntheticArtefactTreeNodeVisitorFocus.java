package org.variantsync.vevos.simulation.variability.pc.visitor;

import org.variantsync.vevos.simulation.variability.pc.ArtefactTree;
import org.variantsync.vevos.simulation.variability.pc.SyntheticArtefactTreeNode;

public class SyntheticArtefactTreeNodeVisitorFocus<C extends ArtefactTree<?>> extends ArtefactTreeVisitorFocus<SyntheticArtefactTreeNode<C>> {
    public SyntheticArtefactTreeNodeVisitorFocus(final SyntheticArtefactTreeNode<C> artefact) {
        super(artefact);
    }

    @Override
    public void accept(final ArtefactVisitor visitor) {
        visitor.visitGenericArtefactTreeNode(this);
    }
}
