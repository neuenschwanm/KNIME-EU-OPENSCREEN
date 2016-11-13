package de.EUOPENSCREEN.DumbFileReader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "DumbFileReader" Node.
 * 
 *
 * @author 
 */
public class DumbFileReaderNodeFactory 
        extends NodeFactory<DumbFileReaderNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DumbFileReaderNodeModel createNodeModel() {
        return new DumbFileReaderNodeModel();
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
        return new DumbFileReaderNodeDialog();
    }

	@Override
	public NodeView<DumbFileReaderNodeModel> createNodeView(int viewIndex, DumbFileReaderNodeModel nodeModel) {
		// TODO Auto-generated method stub
		return null;
	}

}

