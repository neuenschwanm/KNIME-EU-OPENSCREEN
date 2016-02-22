package de.EUOPENSCREEN.FASTAReader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FASTAReader" Node.
 * 
 *
 * @author Marc Wippich
 */
public class FASTAReaderNodeFactory 
        extends NodeFactory<FASTAReaderNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FASTAReaderNodeModel createNodeModel() {
        return new FASTAReaderNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<FASTAReaderNodeModel> createNodeView(final int viewIndex,
            final FASTAReaderNodeModel nodeModel) {
        return new FASTAReaderNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new FASTAReaderNodeDialog();
    }

}