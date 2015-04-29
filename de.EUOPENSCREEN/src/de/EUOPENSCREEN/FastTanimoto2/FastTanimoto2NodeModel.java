package de.EUOPENSCREEN.FastTanimoto2;

import java.io.File;
import java.io.IOException;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
//import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Locale;


/**
 * This is the model implementation of FastTanimoto.
 * compares fingerprints and lists identifiers having scores larger than a preset threshold
 *
 * @author Martin Neuenschwander
 */
public class FastTanimoto2NodeModel extends NodeModel {
    
    // the logger instance  
	//private static final NodeLogger logger = NodeLogger.getLogger(FastTanimoto2NodeModel.class);
        
    /** configuration key and default for the threshold value */
	static final String CFGKEY_THRESHOLD = "threshold";

	/** the default value for the tanimoto threshold*/
	static final double DEFAULT_THRESHOLD = 0.6;

    /** configuration key for the name of the molecule ID column */
    public static final String CFGKEY_ID_COLUMN = "Column_name_identifiers";
    /** configuration key for the name of the fingerprint column */
    public static final String CFGKEY_FP_COLUMN = "Column_name_fingerprints";

    /** configuration key for the name of the molecule ID column */
    public static final String CFGKEY_ID_COLUMN2 = "identifiers2";
    /** configuration key for the name of the fingerprint column */
    public static final String CFGKEY_FP_COLUMN2 = "fingerprints2";
    
    private SettingsModelString m_columnName_ID = new SettingsModelString(CFGKEY_ID_COLUMN, null);
    private SettingsModelString m_columnName_FP = new SettingsModelString(CFGKEY_FP_COLUMN, null);
    private SettingsModelString m_columnName_ID2 = new SettingsModelString(CFGKEY_ID_COLUMN2, null);
    private SettingsModelString m_columnName_FP2 = new SettingsModelString(CFGKEY_FP_COLUMN2, null);
  
    private SettingsModelDoubleBounded m_threshold = new SettingsModelDoubleBounded(FastTanimoto2NodeModel.CFGKEY_THRESHOLD, FastTanimoto2NodeModel.DEFAULT_THRESHOLD,0.0,1.0); 
    
    // the array variables for the unbuffered data table
    private Hashtable<String, Integer>  	rowkey;   //the identifier of the incoming data row
    private int 		counter0;  //the total number of rows to process, test compounds
    private int 		counter1;  //the total number of rows to process, reference set
    
    private String[]  	ID0;   //the identifier of the molecule
    private String[]  	ID1;   //the identifier of the molecule
 
    private String[] 	similars; //the output with the id's of all similar molecules
    private String[] 	coefficients; // the output with the corresponding tanimoto coefficients
    private int[]       number_of_similars; //the output indicating the number of similar molecules found
    private int[]		cardinality_threshold; //the minimum number of 1's required in the second fingerprint to reach the threshold
    
    /* ------------------------------------------------------------------------------------- */
    
    /**
     * Constructor for the node model.
     */
    protected FastTanimoto2NodeModel() {
    
        // one incoming port and one outgoing port
        super(2, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        
      //determine the number of data sets in the input
      counter0 = inData[0].getRowCount();
        
      //get data from in port 0
      DataTableSpec inputSpec = inData[0].getSpec();
      //get data from in port 1
      DataTableSpec inputSpec2 = inData[1].getSpec();
         
      
      //get the indices of our input columns, table0, test set
      final int idColIndex = inputSpec.findColumnIndex(m_columnName_ID.getStringValue());
      final int fpColIndex = inputSpec.findColumnIndex(m_columnName_FP.getStringValue());
  
      //get the indices of our input columns, table1, reference set
      final int idColIndex2 = inputSpec2.findColumnIndex(m_columnName_ID2.getStringValue());
      final int fpColIndex2 = inputSpec2.findColumnIndex(m_columnName_FP2.getStringValue());
  
      ExecutionMonitor exec1 = exec.createSubProgress(0.5);
      ExecutionMonitor exec2 = exec.createSubProgress(0.5);
     
      //declare variables for tanimoto search, initialize the array with the total number of rows
      ID0 = new String[counter0];
      BitSet[]  fp0 = new BitSet[counter0];
      similars = new String[counter0];
      coefficients = new String[counter0];
      int[] cardinality0 = new int[counter0];
      rowkey = new   Hashtable<String, Integer>() ;
      number_of_similars = new int[counter0];
      cardinality_threshold = new int[counter0];
      
      //determine the number of data sets in the input
      counter1 = inData[1].getRowCount();
        
      //get data from in port 0
    //  DataTableSpec inputSpec1 = inData[1].getSpec();
      
      
      //get the threshold from the configure dialog
      double threshold = m_threshold.getDoubleValue();
      
      //declare variables for tanimoto search, initialize the array with the total number of rows
      ID1 = new String[counter1];
      BitSet[]  fp1 = new BitSet[counter1];
      int[] cardinality1 = new int[counter1];
      
      //create and initialize arrays, table0, test set
      int i = 0;
    
      for (DataRow r : inData[0]) { 
	    	
    	  	//the rowkey is taken from the input table
    	   // rowkey[i] = r.getKey();
	    	
    	    rowkey.put(r.getKey().getString(),i);
    	    
	    	//tests whether the cell for the fingerprint is missing and creates the BitSet from the String representation. A missing cell leads to a an empty BitSet
	    	if(!r.getCell(fpColIndex).isMissing()) {
	    		fp0[i] = createFromString(r.getCell(fpColIndex).toString());
	    	} else {
	    		fp0[i] = createFromString("");
	    	}
	      	
	    	//tests whether the cell for the identifier is missing and creates a String cell representation. A missing cell gets an empty String as identifier
	    	if (!r.getCell(idColIndex).isMissing()) {
	    		ID0[i] = r.getCell(idColIndex).toString();
	    	} else {
	    		ID0[i] = "";
	    	}
	      	
	      	similars[i] = "";
	      	coefficients[i] = "";
	      	cardinality0[i] = fp0[i].cardinality();
	    	number_of_similars[i] = 0;
	    	cardinality_threshold[i] = (int) ((double) cardinality0[i] * threshold) - 1; 
        
	    	i++;
      
	      	//check if execution was cancelled by the user 
	      	exec.checkCanceled();
	      	//set the progress bar and set message
	      	exec1.setProgress(((double) i/ (double) (counter0 + counter1) ), "BitSet created for " + String.valueOf(i) + " molecules");    
	      	 
      }
      
      
      
      //create and initialize arrays, table1, reference set
      int j  = 0;
    
      for (DataRow r : inData[1]) { 
	    	
    		   
	    	//tests whether the cell for the fingerprint is missing and creates the BitSet from the String representation. A missing cell leads to a an empty BitSet
	    	if(!r.getCell(fpColIndex2).isMissing()) {
	    		fp1[j] = createFromString(r.getCell(fpColIndex2).toString());
	    	} else {
	    		fp1[j] = createFromString("");
	    	}
	      	
	    	//tests whether the cell for the identifier is missing and creates a String cell representation. A missing cell gets an empty String as identifier
	    	if (!r.getCell(idColIndex2).isMissing()) {
	    		ID1[j] = r.getCell(idColIndex2).toString();
	    	} else {
	    		ID1[j] = "";
	    	}
	      	
	       	cardinality1[j] = fp1[j].cardinality();
	    	
	    	j++;
      
	      	//check if execution was cancelled by the user 
	      	exec.checkCanceled();
	      	//set the progress bar and set message
	      	exec1.setProgress(((double) (i+j)/ (double) (counter0 + counter1) ), "BitSet created for " + String.valueOf(i) + " molecules");    
	      	 
      }
      
      
      //we want the numbers with . separator
      Locale.setDefault(Locale.ENGLISH);
 
     //perform tanimoto search and populate arrays 
      BitSet mybitset;
      double tanimoto;
      
      
      //outer loop, goes through table 0
      	for (int p=0; p<counter0;p++){
      		//inner loop, goes through table 1
      		for (int q=0; q < counter1; q++){
      			
      			//only if the fingerprint to compare to contains enough 1's - this increases speed for high tanimoto thresholds
      			if (cardinality1[q] > cardinality_threshold[p]) {
      			
	      			mybitset = (BitSet) fp0[p].clone();
	      			mybitset.and(fp1[q]) ;
	      			
	      			//calculate tanimoto as all 1's that occur in both fingerprints divided by the number of 1's in the original fingerprint
	      			if (!(cardinality0[p] == 0)) {
	      				tanimoto = (double) mybitset.cardinality() / (double) cardinality0[p];
	      			} else {
	      				tanimoto = 0.0;	
	      			}	
	      			
	      			if (tanimoto > threshold) {
	      				similars[p] = similars[p] + "," + ID1[q];
	      				coefficients[p] = coefficients[p] + "," + String.format("%.2f",tanimoto);
	      				number_of_similars[p] = number_of_similars[p] + 1;	
	      			}
      			
      			} //end cardinality check
      		} //end for q
      			
      	
      	//polish output      		
      	if (similars[p].length() > 0){similars[p] = similars[p].substring(1);}
      	if (coefficients[p].length() > 0){ coefficients[p] = coefficients[p].substring(1);}
     
      	//check if execution was cancelled by the user
      	exec.checkCanceled();  
      	//set the progress bar and message
     	exec2.setProgress(((double) p/ (double) counter0), "Tanimoto calculated for " + String.valueOf(p) + " molecules");    
  	     	 
       
      	} //end for p
    
      
      //	logger.warn("% excluded: " +(int)  ((double) (counterexclude*100)/ (double) countertotal));
      	
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
            	
    	//check whether there is at least 1 fingerprint and 1 String column in the input table
    	
    	boolean hasBitVectorColumn = false;
    	boolean hasStringColumn = false;
    	boolean hasBitVectorColumn2 = false;
    	boolean hasStringColumn2 = false;
    
    	//loop through all columns in the input table 0
    	for (int i=0; i < inSpecs[0].getNumColumns(); i++) {
    		DataColumnSpec columnSpec = inSpecs[0].getColumnSpec(i);
    		
    		if (columnSpec.getType().isCompatible (BitVectorValue.class)) {	
    			//found one BitVector column
    			hasBitVectorColumn = true;
    		}
    		
    		if (columnSpec.getType().isCompatible (StringValue.class)) {	
    			//found one String column
    			hasStringColumn = true;
    		}
    		
    	}
    	
    	//loop through all columns in the input table 1
    	for (int i=0; i < inSpecs[1].getNumColumns(); i++) {
   		
    		DataColumnSpec columnSpec = inSpecs[1].getColumnSpec(i);
    		
    		if (columnSpec.getType().isCompatible (BitVectorValue.class)) {	
    			//found one BitVector column
    			hasBitVectorColumn2 = true;
    		}
    		
    		if (columnSpec.getType().isCompatible (StringValue.class)) {	
    			//found one String column
    			hasStringColumn2 = true;
    		}
    		
    	}
    	
    	
    	//throw error messages if a required column type is missing
    	if (!hasBitVectorColumn) {
    		throw new InvalidSettingsException("The input table 1 must contain at least 1 BitVector column");
    	}	
    	
    	if (!hasStringColumn) {
    		throw new InvalidSettingsException("The input table 1 must contain at least 1 String column that contains molecule identifiers");
    	}
    	
    	//throw error messages if a required column type is missing
    	if (!hasBitVectorColumn2) {
    		throw new InvalidSettingsException("The input table 2 must contain at least 1 BitVector column");
    	}	
    	
    	if (!hasStringColumn2) {
    		throw new InvalidSettingsException("The input table 2 must contain at least 1 String column that contains molecule identifiers");
    	}
    	
    	//produce the output table spec which specifies the output of this node
    	DataColumnSpec[] newColumnSpec = createAppendedOutputTableSpec();  //the 4 appended columns
    	DataTableSpec appendedSpec = new DataTableSpec(newColumnSpec);
    	DataTableSpec outputSpec = new DataTableSpec(inSpecs[0], appendedSpec); //append the spec of the output columns to the input table spec
    	
    	return new DataTableSpec[]{outputSpec};
    	
    
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
     	m_columnName_ID.saveSettingsTo(settings);
    	m_columnName_FP.saveSettingsTo(settings);
    	m_columnName_ID2.saveSettingsTo(settings);
    	m_columnName_FP2.saveSettingsTo(settings);
    
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
    	m_columnName_ID2.loadSettingsFrom(settings);
    	m_columnName_FP2.loadSettingsFrom(settings);
    	
    	m_threshold.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
    	m_columnName_ID.validateSettings(settings);
    	m_columnName_FP.validateSettings(settings);
    	m_columnName_ID2.validateSettings(settings);
    	m_columnName_FP2.validateSettings(settings);
    
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

    


//takes a String with the molecule fingerprint as "01010100...", and creates a java BitSet object from it
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
      

//function for joining the input table to the result table
private ColumnRearranger createColumnRearranger(DataTableSpec in) {

	ColumnRearranger c = new ColumnRearranger(in);
    
    DataColumnSpec[] allColSpecs = createAppendedOutputTableSpec();  //columns specs of the appended columns
      
    CellFactory factory = new AbstractCellFactory(allColSpecs) {
    	
    	@Override
		public DataCell[] getCells(DataRow row) {
           	
    		DataCell[] cells = new DataCell[4];
    		
    		int i = rowkey.get(row.getKey().getString());
    		cells[0] = new StringCell(ID0[i]);
    		cells[1] = new StringCell(similars[i]);
    		cells[2] = new StringCell(coefficients[i]);
    		cells[3] = new IntCell(number_of_similars[i]);
    
             return cells;
            }
    };
    
    c.append(factory);
    return c;
}


//creates the columns spec of the appended data columns, for createColumnRearranger and Configure
private DataColumnSpec[] createAppendedOutputTableSpec() {
	
	 DataColumnSpec[] allColSpecs = new DataColumnSpec[4];
	 allColSpecs[0] = new DataColumnSpecCreator("tanimoto.id", StringCell.TYPE).createSpec();
	 allColSpecs[1] = new DataColumnSpecCreator("tanimoto.similars", StringCell.TYPE).createSpec();
	 allColSpecs[2] = new DataColumnSpecCreator("tanimoto.coefficients", StringCell.TYPE).createSpec();
	 allColSpecs[3] = new DataColumnSpecCreator("tanimoto.num.similars", IntCell.TYPE).createSpec();
	
	 return allColSpecs;	
}

}

