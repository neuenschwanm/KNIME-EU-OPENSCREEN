package de.EUOPENSCREEN.SPARQLQuery;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "SPARQLQuery" Node.
 * performs a SPARQL search on an endpoint and retrieves the result as a table
 *
 * @author Martin Neuenschwander
 */
public class SPARQLQueryNodeView extends NodeView<SPARQLQueryNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link SPARQLQueryNodeModel})
     */
    protected SPARQLQueryNodeView(final SPARQLQueryNodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        SPARQLQueryNodeModel nodeModel = 
            getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    
        // TODO things to do when closing the view
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

        // TODO things to do when opening the view
    }

}

