package de.EUOPENSCREEN.FASTAWriter;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FASTAWriter" Node.
 * takes input column with FASTA header, input column with FASTA sequence, and writes all entries with header and sequence to a single FASTA file
 *
 * @author Martin Neuenschwander
 */
public class FASTAWriterNodeFactory 
        extends NodeFactory<FASTAWriterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FASTAWriterNodeModel createNodeModel() {
        return new FASTAWriterNodeModel();
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
    public NodeView<FASTAWriterNodeModel> createNodeView(final int viewIndex,
            final FASTAWriterNodeModel nodeModel) {
        return null;
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
        return new FASTAWriterNodeDialog();
    }

}

