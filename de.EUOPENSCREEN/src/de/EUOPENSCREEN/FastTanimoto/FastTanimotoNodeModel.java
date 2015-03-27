package de.EUOPENSCREEN.FastTanimoto;

import java.io.File;
import java.io.IOException;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import java.util.BitSet;
import java.util.Locale;


/**
 * This is the model implementation of FastTanimoto.
 * compares fingerprints and lists identifiers having scores larger than a preset threshold
 *
 * @author Martin Neuenschwander
 */
public class FastTanimotoNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(FastTanimotoNodeModel.class);
        
    /** the settings key which is used to retrieve and 
        store the settings (from the dialog or from a settings file)    
       (package visibility to be usable from the dialog). */
	static final String CFGKEY_THRESHOLD = "threshold";

    /** initial default count value. */
    static final double DEFAULT_THRESHOLD = 0.6;

    
    /** Config identifier: column name. */
    public static final String CFGKEY_ID_COLUMN = "Column_name_identifiers";
    public static final String CFGKEY_FP_COLUMN = "Column_name_fingerprints";

	
    private SettingsModelString m_columnName_ID = new SettingsModelString(CFGKEY_ID_COLUMN, null);
    private SettingsModelString m_columnName_FP = new SettingsModelString(CFGKEY_FP_COLUMN, null);
    private SettingsModelDoubleBounded m_threshold = new SettingsModelDoubleBounded(FastTanimotoNodeModel.CFGKEY_THRESHOLD, FastTanimotoNodeModel.DEFAULT_THRESHOLD,0.0,1.0); 
    
    
    private RowKey[]  	rowkey;
    private int 		counter;
    private String[]  	ID;
    private String[] 	similars;
    private String[] 	coefficients;
    private int[]       number_of_similars;
    
    /* ------------------------------------------------------------------------------------- */
    
    /**
     * takes a String with the molecule fingerprint as "01010100...", and creates a java BitSet object from it
     * @param s
     * @return
     */
    private static BitSet createFromString(String s) {
        BitSet t = new BitSet(s.length());
        int lastBitIndex = s.length() - 1;
        int i = lastBitIndex;
        while ( i >= 0) {
            if ( s.charAt(i) == '1'){
                t.set(lastBitIndex - i);           
                i--;
            }
            else
                i--;               
        }
        return t;
    }
          
    /* ------------------------------------------------------------------------------------- */
    
    /**
     * Constructor for the node model.
     */
    protected FastTanimotoNodeModel() {
    
        // one incoming port and one outgoing port
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        
      //determine the number of data sets in the input
      counter = inData[0].getRowCount();
        
      //get data from in port 0
      DataTableSpec inputSpec = inData[0].getSpec();
        
      //get the indices of our input columns
      final int idColIndex = inputSpec.findColumnIndex(m_columnName_ID.getStringValue());
      final int fpColIndex = inputSpec.findColumnIndex(m_columnName_FP.getStringValue());
  
      //declare variables for tanimoto search
      ID = new String[counter];
      BitSet[]  fp = new BitSet[counter];
      similars = new String[counter];
      coefficients = new String[counter];
      int[] cardinality = new int[counter];
      rowkey = new RowKey[counter];
      number_of_similars = new int[counter];
      
      //create and initialize arrays
      int i = 0;
      for (DataRow r : inData[0]) { 
	    	rowkey[i] = r.getKey();
	      	fp[i] = createFromString(r.getCell(fpColIndex).toString());
	      	ID[i] = r.getCell(idColIndex).toString();
	      	similars[i] = "";
	      	coefficients[i] = "";
	      	cardinality[i] = fp[i].cardinality();
	    	number_of_similars[i] = 0;
        
	    	i++;
      
	    	//always after 500 molecules are processed print a warning
	      	if ((i % 500) == 0) {
	      		logger.warn("BitSet created for: " + i + " molecules");
	      	}
      }
      
      
      //we want the numbers with . separator
      Locale.setDefault(Locale.ENGLISH);
 
     //perform tanimoto search and populate arrays 
      BitSet mybitset;
      double tanimoto;
      
      //get the threshold from the configure dialog
      double threshold = m_threshold.getDoubleValue();
      
      	for (int p=0; p<counter;p++){
      		for (int q=0; q < counter; q++){
      			mybitset = (BitSet) fp[p].clone();
      			mybitset.and(fp[q]) ;
      			
      			if (!(cardinality[p] == 0)) {
      				tanimoto = (double) mybitset.cardinality() / (double) cardinality[p];
      			} else {
      				tanimoto = 0.0;	
      			}	
      			
      			if (tanimoto > threshold) {
      					//dont compare the molecule to itself
      						if (!(q==p)){
      							similars[p] = similars[p] + "," + ID[q];
      							coefficients[p] = coefficients[p] + "," + String.format("%.2f",tanimoto);
      							number_of_similars[p] = number_of_similars[p] + 1;
      						}
      				}
      		}
      			
      	
      	//polish output      		
      	if (similars[p].length() > 0){similars[p] = similars[p].substring(1);}
      	if (coefficients[p].length() > 0){ coefficients[p] = coefficients[p].substring(1);}
      	
       		//always after 500 molecules are processed print a warning and set the progress bar
	      	 if ((p % 500) == 0) {
		       	  logger.warn("tanimoto calculated for: " + p + " molecules");
		       	  exec.setProgress(((double) p/ (double) counter));
		     }
      	}
    
        
     ColumnRearranger result = createColumnRearranger(inputSpec);
        
     return new BufferedDataTable[] { exec.createColumnRearrangeTable(inData[0], result, exec)};
       
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
     	m_columnName_ID.saveSettingsTo(settings);
    	m_columnName_FP.saveSettingsTo(settings);
    	m_threshold.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO load (valid) settings from the config object.
        // It can be safely assumed that the settings are valided by the 
        // method below.
        
     //   m_count.loadSettingsFrom(settings);
    	m_columnName_ID.loadSettingsFrom(settings);
    	m_columnName_FP.loadSettingsFrom(settings);
    	m_threshold.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO check if the settings could be applied to our model
        // e.g. if the count is in a certain range (which is ensured by the
        // SettingsModel).
        // Do not actually set any values of any member variables.

  //      m_count.validateSettings(settings);

    	m_columnName_ID.validateSettings(settings);
    	m_columnName_FP.validateSettings(settings);
    	m_threshold.validateSettings(settings);
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



/* ----------------- functions for column rearranger -------------------------------------- */

    
//using the rowkey, derives the index of the corresponding array data. Helper function for createColumnRearranger
private int geti(RowKey rk) {
	
	for (int j=0; j<counter; j++) {
		
		if (rowkey[j].toString().equals(rk.toString())){
			return j;
		}
		
	}
	
	return -1;
}


//function for joining the input table to the result table
private ColumnRearranger createColumnRearranger(DataTableSpec in) {
    ColumnRearranger c = new ColumnRearranger(in);
    // column spec of the appended column
   
    DataColumnSpec[] allColSpecs = new DataColumnSpec[4];
    allColSpecs[0] = new DataColumnSpecCreator("tanimoto.id", StringCell.TYPE).createSpec();
    allColSpecs[1] = new DataColumnSpecCreator("tanimoto.similars", StringCell.TYPE).createSpec();
    allColSpecs[2] = new DataColumnSpecCreator("tanimoto.coefficients", StringCell.TYPE).createSpec();
    allColSpecs[3] = new DataColumnSpecCreator("tanimoto.num.similars", IntCell.TYPE).createSpec();
      
 CellFactory factory = new AbstractCellFactory(allColSpecs) {
    	
    	public DataCell[] getCells(DataRow row) {
           	
    		DataCell[] cells = new DataCell[4];
    		
    		int i = geti(row.getKey());
    		cells[0] = new StringCell(ID[i]);
    		cells[1] = new StringCell(similars[i]);
    		cells[2] = new StringCell(coefficients[i]);
    		cells[3] = new IntCell(number_of_similars[i]);
    
              return cells;
            }
    };
    c.append(factory);
    return c;
}


}

