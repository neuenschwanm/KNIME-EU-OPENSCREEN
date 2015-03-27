package de.EUOPENSCREEN.SPARQLQuery;


import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "SPARQLQuery" Node.
 * performs a SPARQL search on an endpoint and retrieves the result as a table
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Martin Neuenschwander
 */
public class SPARQLQueryNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring SPARQLQuery node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected SPARQLQueryNodeDialog() {
        super();
    
       addDialogComponent(new  DialogComponentMultiLineString(new SettingsModelString(SPARQLQueryNodeModel.CFGKEY_QUERY, SPARQLQueryNodeModel.QUERY), " SPARQL Query:"));
   
       addDialogComponent(new DialogComponentString(new SettingsModelString(SPARQLQueryNodeModel.CFGKEY_ENDPOINT, SPARQLQueryNodeModel.ENDPOINT), "SPARQL Endpoint:",true, 50));
          
       addDialogComponent(new DialogComponentNumber( new SettingsModelIntegerBounded(SPARQLQueryNodeModel.CFGKEY_TIMEOUT, SPARQLQueryNodeModel.TIMEOUT,0, Integer.MAX_VALUE),"Timeout (sec):",1,30));
           
    }
}

