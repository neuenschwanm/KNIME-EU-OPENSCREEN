package de.EUOPENSCREEN.SPARQLQuery;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "SPARQLQuery" Node.
 * performs a SPARQL search on an endpoint and retrieves the result as a table
 *
 * @author Martin Neuenschwander
 */
public class SPARQLQueryNodeFactory 
        extends NodeFactory<SPARQLQueryNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SPARQLQueryNodeModel createNodeModel() {
        return new SPARQLQueryNodeModel();
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
        return new SPARQLQueryNodeDialog();
    }

	@Override
	public NodeView<SPARQLQueryNodeModel> createNodeView(int viewIndex,
			SPARQLQueryNodeModel nodeModel) {
		// TODO Auto-generated method stub
		return null;
	}

}

