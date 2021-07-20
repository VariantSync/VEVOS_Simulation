package de.variantsync.evolution.variability.pc.visitor;

import de.variantsync.evolution.variability.pc.ArtefactTree;
import de.variantsync.evolution.variability.pc.SyntheticArtefactTreeNode;

public class SyntheticArtefactTreeNodeVisitorContext<C extends ArtefactTree<?>> extends ArtefactTreeVisitorContext<SyntheticArtefactTreeNode<C>> {
    public SyntheticArtefactTreeNodeVisitorContext(SyntheticArtefactTreeNode<C> artefact) {
        super(artefact);
    }

    @Override
    public void accept(ArtefactVisitor visitor) {
        visitor.visitGenericArtefactTreeNode(this);
    }
}
