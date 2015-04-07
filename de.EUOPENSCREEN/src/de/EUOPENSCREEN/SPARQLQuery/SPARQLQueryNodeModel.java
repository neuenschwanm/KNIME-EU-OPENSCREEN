package de.EUOPENSCREEN.SPARQLQuery;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.atlas.logging.LogCtl;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
//import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import javax.swing.Timer;

/**
 * This is the model implementation of SPARQLQuery.
 * performs a SPARQL search on an endpoint and retrieves the result as a table
 *
 * @author Martin Neuenschwander
 */
public class SPARQLQueryNodeModel extends NodeModel {
    
    // the logger instance
  //  private static final NodeLogger logger = NodeLogger.getLogger(SPARQLQueryNodeModel.class);
        
	static final String CFGKEY_QUERY = "Query";
	static final String CFGKEY_ENDPOINT = "Endpoint";
	static final String CFGKEY_TIMEOUT = "Timeout";
	
    static final String QUERY = "PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>  \n SELECT ?molecule \n WHERE \n { \n ?molecule a cco:SmallMolecule . \n ?molecule cco:highestDevelopmentPhase ?phase . \n FILTER(?phase = 4 ) \n}";
    static final String ENDPOINT = "https://www.ebi.ac.uk/rdf/services/chembl/sparql";
    static final int TIMEOUT = 15;
    
    private final  SettingsModelString m_query = new SettingsModelString(SPARQLQueryNodeModel.CFGKEY_QUERY, SPARQLQueryNodeModel.QUERY);
    private final  SettingsModelString m_endpoint = new SettingsModelString(SPARQLQueryNodeModel.CFGKEY_ENDPOINT, SPARQLQueryNodeModel.ENDPOINT);
    private final  SettingsModelIntegerBounded m_timeout = new SettingsModelIntegerBounded(SPARQLQueryNodeModel.CFGKEY_TIMEOUT, SPARQLQueryNodeModel.TIMEOUT,0, Integer.MAX_VALUE);
    
    
    private ExecutionContext innerExecContext;
    
    //inner class for the action listener of the timer
    class TimerListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			//only if we have a valid reference to the execution context
			if (! (innerExecContext==null)){
				try {
					//we check if the node was canceled by the user
					innerExecContext.checkCanceled();
				} catch (CanceledExecutionException e) {
					
				}
			}
			
		}
    	
    }
    
    /**
     * Constructor for the node model.
     */
    protected SPARQLQueryNodeModel() {
    
        // no incoming port and one outgoing port is assumed
        super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
 
   
    
    	//the execution context is passed to an instance variable
    	innerExecContext = exec;
    	
    	//a timer is started which checks for cancellation of the node every 50 msec
    	Timer timer = new Timer(50, new TimerListener());
		timer.setRepeats(true);
		timer.start();		
    	
        //heres the guts
        String myquery = m_query.getStringValue();
        String myendpoint = m_endpoint.getStringValue();
        int mytimeout = m_timeout.getIntValue()*1000;  //give timeout in msec
        
        BufferedDataContainer container=null;
        
        
        	LogCtl.setCmdLogging(); //deactivate the logging of apache jena
        	
        	
        		Query query = QueryFactory.create(myquery);
     	   
        	     // Remote execution, this is why we have to check with a timer for cancellation of the node
	            QueryExecution qexec = QueryExecutionFactory.sparqlService(myendpoint, query);
	         
	            //if the timeout is at least 1000 msec
	            if (mytimeout >= 1000) {
	            // Set the DBpedia specific timeout.
	            	((QueryEngineHTTP)qexec).addParam("timeout", String.valueOf(mytimeout)) ;
	            }
	 
	            //set progress with the message that query has been sent
		      	exec.setProgress(0.1, "Query sent to " + myendpoint + ", waiting for response");    

	            
	            // Execute and wait for response, meanwhile the timer checks for cancellation of the node
	            ResultSet rs = qexec.execSelect();
	            
	            int no_of_columns =  rs.getResultVars().size(); //the number of columns of the output table
	                  
	            //create column data table with String cells
	            DataColumnSpec[] allColSpecs = new DataColumnSpec[no_of_columns];
	            
	            //set the names of the result columns
	            int i= 0;
	            for (String s: rs.getResultVars()) { 	
	            	allColSpecs[i] = new DataColumnSpecCreator(s, StringCell.TYPE).createSpec();
	            	i++;
	            }
	            
	            DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
	            container = exec.createDataContainer(outputSpec);
	            
	            //iterate over all rows
	            int r = 0;
	            while (rs.hasNext()) {
	 	    			
	            	QuerySolution myRow = rs.next();
	            	RowKey key = new RowKey("Row" + r);
	            	DataCell[] cells = new DataCell[no_of_columns];
		        	
	            	//create cells array and instantiate first with Cells having an empty string
	            	//this is important for queries with OPTIONAL argument where not every result row contains
	            	//all result cells
	            	for (int q=0; q<no_of_columns; q++){
	            		cells[q] = new StringCell("");
	            	}

	            	//iterate over all columns in the current result row
	            	Iterator <String> myColNames = myRow.varNames(); //if OPTIONAL arguments are in the query it may be that varNames has less entries than rs.getResultVars
	            	String current_col_name = "";
	            	while (myColNames.hasNext()) {
	            		current_col_name = myColNames.next();
	            		//the index of the cell in the result table is determined by searching for current_col_name in the ResultVars() set
	            		cells[rs.getResultVars().indexOf(current_col_name)] = new StringCell( myRow.get(current_col_name).toString());	            	
	            	}
	            	
	            	DataRow row = new DefaultRow(key, cells);
	    			container.addRowToTable(row);
	        		r++;
	            	
	            }
	            
	   
	        //dispose the qexec object
	        qexec.close();
        
        // once we are done, we close the container and return its table
        container.close();
        BufferedDataTable out = container.getTable();
        return new BufferedDataTable[]{out};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Code executed on reset.
        // Models build during execute are cleared here.
        // Also data handled in load/saveInternals will be erased here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        
        // TODO: check if user settings are available, fit to the incoming
        // table structure, and the incoming types are feasible for the node
        // to execute. If the node can execute in its current state return
        // the spec of its output data table(s) (if you can, otherwise an array
        // with null elements), or throw an exception with a useful user message

        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

     	this.m_query.saveSettingsTo(settings);
    	this.m_endpoint.saveSettingsTo(settings);
    
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
     		this.m_query.loadSettingsFrom(settings);
    		this.m_endpoint.loadSettingsFrom(settings);
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
     		this.m_query.validateSettings(settings);
     		this.m_endpoint.validateSettings(settings);
    	
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        
        // TODO load internal data. 
        // Everything handed to output ports is loaded automatically (data
        // returned by the execute method, models loaded in loadModelContent,
        // and user settings set through loadSettingsFrom - is all taken care 
        // of). Load here only the other internals that need to be restored
        // (e.g. data used by the views).

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       
        // TODO save internal models. 
        // Everything written to output ports is saved automatically (data
        // returned by the execute method, models saved in the saveModelContent,
        // and user settings saved through saveSettingsTo - is all taken care 
        // of). Save here only the other internals that need to be preserved
        // (e.g. data used by the views).

    }

}

