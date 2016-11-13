package de.EUOPENSCREEN.DumbFileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of DumbFileReader.
 * 
 *
 * @author 
 */
public class DumbFileReaderNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(DumbFileReaderNodeModel.class);
        
    /** the settings key which is used to retrieve and 
        store the settings (from the dialog or from a settings file)    
       (package visibility to be usable from the dialog). */
	static final String CFGKEY_FOLDERNAME = "Folder"; //the path to the source folder to analyze
	static final String CFGKEY_HEADER = "LineNumberHeader"; //the line number of the header line
	static final String CFGKEY_START = "LineNumberStart"; //the line number with the first data line
	static final String CFGKEY_END = "LineNumberEnd"; //the line number with the last data line
	static final String CFGKEY_DELIMITER = "Delimiter"; //the line number with the last data line
	
	
    /** initial default count value. */
    static final String DEFAULT_FOLDERNAME = "D:/input";
    static final int DEFAULT_HEADER = 1;
    static final int DEFAULT_START = 2;
    static final int DEFAULT_END = 385;
    static final String DEFAULT_DELIMITER = "\\t"; //the default delimiter is a tab
    
 
    
    private final SettingsModelIntegerBounded m_header =
            new SettingsModelIntegerBounded(DumbFileReaderNodeModel.CFGKEY_HEADER,
                        DumbFileReaderNodeModel.DEFAULT_HEADER,
                       0, 10000000);
        
    private final SettingsModelIntegerBounded m_start =
    		new SettingsModelIntegerBounded(DumbFileReaderNodeModel.CFGKEY_START,
                        DumbFileReaderNodeModel.DEFAULT_START,
                        0, 10000000);
        
    private final SettingsModelIntegerBounded m_end =
            new SettingsModelIntegerBounded(DumbFileReaderNodeModel.CFGKEY_END,
                        DumbFileReaderNodeModel.DEFAULT_END,
                        0, 10000000);
    
    private final SettingsModelString m_folder = new SettingsModelString(CFGKEY_FOLDERNAME, DEFAULT_FOLDERNAME);
    private final SettingsModelString m_delimiter = new SettingsModelString(CFGKEY_DELIMITER, DEFAULT_DELIMITER);
    
    
    
    /**
     * Constructor for the node model.
     */
    protected DumbFileReaderNodeModel() {
        super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
 
        List<String> filelist = new ArrayList<String>();  //filelist holds all filenames that are contained in the selected directory
       
        //analyze directory and store all found filenames in the filelist array
        File[] files = new File(m_folder.getStringValue()).listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null. 

        for (File file : files) {
            if (file.isFile()) {
                filelist.add(file.getName());
            }
        }
        
   
        //get header line from first file
        File firstFile = files[0];
        
        BufferedReader in = Files.newBufferedReader(firstFile.toPath());
        List<String> lines = new ArrayList<String>();   
		  for (String line; (line = in.readLine()) != null;) {	  
			  lines.add(line);
		  }
       
        
        //get header line
        String headerline = lines.get(m_header.getIntValue() - 1);
      
        
        String elements[] = headerline.split(m_delimiter.getStringValue());

        int table_width = elements.length;  //the table width is determined from the header line of the first file
        
        // the data table spec of the single output table, 
        DataColumnSpec[] allColSpecs = new DataColumnSpec[table_width+3];
        
       //create the column header definition for the additional informative data cells
       allColSpecs[0] = new DataColumnSpecCreator("DumbFileReader.filename", StringCell.TYPE).createSpec();
       allColSpecs[1] = new DataColumnSpecCreator("DumbFileReader.LineNumber", IntCell.TYPE).createSpec();
       allColSpecs[2] = new DataColumnSpecCreator("DumbFileReader.error", StringCell.TYPE).createSpec();
           
        
       // allColSpecs[0] = new DataColumnSpecCreator("DumbFileReader.filename", StringCell.TYPE).createSpec();
    
        for (int i = 0; i<table_width; i++) {
        	allColSpecs[i+3] = new DataColumnSpecCreator(elements[i].trim(), StringCell.TYPE).createSpec();
        }
        
        
        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
        // the execution context will provide us with storage capacity, in this
        // case a data container to which we will add rows sequentially
        // Note, this container can also handle arbitrary big data tables, it
        // will buffer to disc if necessary.
        BufferedDataContainer container = exec.createDataContainer(outputSpec);
       
        int counter = 0;
        
        //loop through all files
        for (File currentFile : files){
                
        //open current file
       try { // try opening a file
            
            
       in = Files.newBufferedReader(currentFile.toPath());

           lines = new ArrayList<String>();   
	       for (String line; (line= in.readLine()) != null;) {	  
	     	  lines.add(line);
	       }
        	
        	
        //now iterate through the data lines, rows
        for (int j = m_start.getIntValue() - 1; j < m_end.getIntValue();j++){
     
        	  String errorForLine = "fine"; //error message for the line, "fine" at the beginning
              
        	  //we add 3 additional data informative data cells ahead of the table
        	  DataCell[] cells = new DataCell[table_width+3];
        	  String data_elements[] = null;
        	  RowKey key = new RowKey(currentFile.getName() + ", Line " + String.valueOf(j+1));
         	 
        	 //try getting data elements 
        	 try {
        	  data_elements = lines.get(j).split(m_delimiter.getStringValue());
        	
        	  //get the number of elements to add to the data table
        	  int number_of_elements = data_elements.length;
        	  
        	  //if the number of elements in this data line is smaller than the table, we have a short line
        	  if (number_of_elements < table_width) {
        		 errorForLine = "Short Line, " + String.valueOf(number_of_elements) + " instead of " + String.valueOf(table_width) + " data points";
        	  }
        	  
        	//if the number of elements in this data line is larger than the table, we have to restrict addition of elements
        	  if (number_of_elements > table_width) {
        		 errorForLine = "Too many data points, " + String.valueOf(number_of_elements) + " instead of " + String.valueOf(table_width) + " data points";       		 
        	  }
        	  
        	  //any other error, e.g. index out of range
        	  } catch (Exception ex){       		  
         		 errorForLine = ex.getMessage();       		    
        	 } //end try getting data elements
        	  
        	  //now iterate through all data elements and fill the cells with values,
        	  //assignement is within a try catch block to prevent abortion of the file reader
        	  for (int p = 0; p < table_width; p++) {
        		  try {
        			  cells[p+3] = new StringCell(data_elements[p]);
        		  } catch (Exception ex) {
        			  cells[p+3] = new StringCell("");
        		  }
        	  }
        	        	  
        	  //set the informative data cells
        	  cells[0] = new StringCell(currentFile.getName());
        	  cells[1] = new IntCell(j+1);
        	  cells[2] = new StringCell(errorForLine);
        	  
        	  
        	  //create the new row
        	  DataRow row = new DefaultRow(key, cells);
        	  //and add it to the data container
        	  container.addRowToTable(row);
           }
       
        
        // end iterate through all files in folder
  
       } // end try opening a file
       catch (Exception e) {
    	   logger.warn(currentFile.getName() + " error: " + e.getMessage());
       }
        
            exec.checkCanceled();
            
            counter++;
            exec.setProgress(counter / (double) filelist.size(), 
                "Analyzing file " + String.valueOf(counter) + " of " + String.valueOf(filelist.size()));
       } // end loop through all files
        
        
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

        // TODO save user settings to the config object.
        
       m_folder.saveSettingsTo(settings);
       m_header.saveSettingsTo(settings);
       m_start.saveSettingsTo(settings);
       m_end.saveSettingsTo(settings);
       m_delimiter.saveSettingsTo(settings);
       

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
        
    	m_folder.loadSettingsFrom(settings);
        m_header.loadSettingsFrom(settings);
        m_start.loadSettingsFrom(settings);
        m_end.loadSettingsFrom(settings);
        m_delimiter.loadSettingsFrom(settings);

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

    	m_folder.validateSettings(settings);
        m_header.validateSettings(settings);
        m_start.validateSettings(settings);
        m_end.validateSettings(settings);
        m_delimiter.validateSettings(settings);
        
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
    
  

}

