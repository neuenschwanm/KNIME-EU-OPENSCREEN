package de.EUOPENSCREEN.FASTAWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
//import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * This is the model implementation of FASTAWriter.
 * takes input column with FASTA header, input column with FASTA sequence, and writes all entries with header and sequence to a single FASTA file
 *
 * @author Martin Neuenschwander
 */
public class FASTAWriterNodeModel extends NodeModel {
    
    // the logger instance
   // private static final NodeLogger logger = NodeLogger.getLogger(FASTAWriterNodeModel.class);
        
    /** the settings key which is used to retrieve and 
        store the settings (from the dialog or from a settings file)    
       (package visibility to be usable from the dialog). */
	static final String CFGKEY_FILE = "fastafile";
	static final String CFGKEY_COLUMN_NAME_HEADER = "header";
	static final String CFGKEY_COLUMN_NAME_COMMENT = "comment";
	static final String CFGKEY_COLUMN_NAME_SEQ = "sequence";

     
    /** create the settings models */
    private SettingsModelString m_file = new SettingsModelString(CFGKEY_FILE, null);
    private SettingsModelString m_header = new SettingsModelString(CFGKEY_COLUMN_NAME_HEADER,null);
    private SettingsModelOptionalString m_comment = new SettingsModelOptionalString(CFGKEY_COLUMN_NAME_COMMENT, null, true);
    private SettingsModelString m_sequence = new SettingsModelString(CFGKEY_COLUMN_NAME_SEQ, null);
    
    /**
     * Constructor for the node model.
     */
    protected FASTAWriterNodeModel() {
     //one incoming port and no outcoming port
        super(1, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
		  
    	//derive indices of the selected columns
    	  //get data from in port 0
        DataTableSpec inputSpec = inData[0].getSpec();
          
        //get the indices of our input columns
      
        final int iHeader = inputSpec.findColumnIndex(m_header.getStringValue());
        final int iComment = inputSpec.findColumnIndex(m_comment.getStringValue());
    	final int iSequence = inputSpec.findColumnIndex(m_sequence.getStringValue());
    	
    	
    	//get file path information from dialog
		String file = m_file.getStringValue();
		file = file.replace("\\", "/");
		
		
		//delete file if it already exists		
		File f = new File(file);
		if (f.exists() && !f.isDirectory()) {
			f.delete();
		}
		

    	int counter = 0;
    	
    	  for (DataRow r : inData[0]) { 
  	    		
    		//get data from data row
    		String header = r.getCell(iHeader).toString();
    		
    		if (!(header.charAt(0) == '>')) {header = ">" + header;};
    		
    		//comment is set to "" if not available
    		String comment = "";
    		if (iComment>0) { 
    			comment = r.getCell(iComment).toString(); 
    			if (!(comment.charAt(0) == ';')) {comment = ";" + comment;};
    		} 
    		
    		String sequence = r.getCell(iSequence).toString();
    		//FASTA sequence is always uppercase
    		sequence = sequence.toUpperCase();
    		sequence = formatSequence(sequence);
    		
    		//write data to file
    		writeSequenceToFile(file,header,comment,sequence);
    		
    		//check if execution was canceled by the user
    		exec.checkCanceled();
    		//set progress with message
    		
    		exec.setProgress( (double) counter / (double) inData[0].getRowCount(), String.valueOf(counter) + " sequences written to file");
    		counter++;
    	  }
      
        return null ;
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

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_header.saveSettingsTo(settings);
        m_comment.saveSettingsTo(settings);
        m_file.saveSettingsTo(settings);
        m_sequence.saveSettingsTo(settings);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
    	   m_header.loadSettingsFrom(settings);
           m_comment.loadSettingsFrom(settings);
           m_file.loadSettingsFrom(settings);
           m_sequence.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
    	  m_header.validateSettings(settings);
          m_comment.validateSettings(settings);
          m_file.validateSettings(settings);
          m_sequence.validateSettings(settings);

    }

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}
    
   
	//write data set to file
	
	private void writeSequenceToFile(String path, String header, String comment, String sequence) throws IOException {
		
		try (FileWriter datei = new FileWriter(path,true)){
			
			datei.write(header + "\n");
			
			if (!comment.equals("")){
				datei.write(comment + "\n");
			}
			datei.write(sequence + "\n");
			
		} catch (IOException e) {
			throw e;
		}
		
	}
	
	
	private String formatSequence(String sequence) {
		
	
		String result = "";
		
		//remove all newline characters
		result = result.replace("\n", "");
		
		for (int i=0; i<sequence.length();i++){
	
			//after 80 chars
			if (!(i==0) && (i%80) == 0) {
				result = result + "\r\n";
			}
			
			result = result + sequence.charAt(i);
		}
		
		return result;
	
	}
	
}

