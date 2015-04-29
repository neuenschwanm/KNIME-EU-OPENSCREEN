package de.EUOPENSCREEN.FastTanimoto2;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FastTanimoto" Node.
 * compares fingerprints and lists identifiers having scores larger than a preset threshold
 *
 * @author Martin Neuenschwander
 */
public class FastTanimoto2NodeFactory 
        extends NodeFactory<FastTanimoto2NodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FastTanimoto2NodeModel createNodeModel() {
        return new FastTanimoto2NodeModel();
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
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new FastTanimoto2NodeDialog();
    }

	@Override
	public NodeView<FastTanimoto2NodeModel> createNodeView(int viewIndex,
			FastTanimoto2NodeModel nodeModel) {
		// TODO Auto-generated method stub
		return null;
	}

}

