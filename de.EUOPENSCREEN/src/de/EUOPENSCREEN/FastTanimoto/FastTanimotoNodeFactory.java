package de.EUOPENSCREEN.FastTanimoto;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FastTanimoto" Node.
 * compares fingerprints and lists identifiers having scores larger than a preset threshold
 *
 * @author Martin Neuenschwander
 */
public class FastTanimotoNodeFactory 
        extends NodeFactory<FastTanimotoNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FastTanimotoNodeModel createNodeModel() {
        return new FastTanimotoNodeModel();
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
    public NodeView<FastTanimotoNodeModel> createNodeView(final int viewIndex,
            final FastTanimotoNodeModel nodeModel) {
        return new FastTanimotoNodeView(nodeModel);
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
        return new FastTanimotoNodeDialog();
    }

}

