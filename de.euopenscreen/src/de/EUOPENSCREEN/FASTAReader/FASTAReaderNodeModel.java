package de.EUOPENSCREEN.FASTAReader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.knime.base.node.util.BufferedFileReader;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class FASTAReaderNodeModel extends NodeModel {
    
   // private static final NodeLogger logger = NodeLogger.getLogger(FASTAReaderNodeModel.class);
    
    static final String FILE = "Input File";
    static final String DEFAULT_FILE = "";
    
    private final SettingsModelString input_file =
    		new SettingsModelString(FASTAReaderNodeModel.FILE, FASTAReaderNodeModel.DEFAULT_FILE);
    
    protected FASTAReaderNodeModel() { super(0, 1); }

    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        DataColumnSpec[] allColSpecs = new DataColumnSpec[2];
        
        allColSpecs[0] = new DataColumnSpecCreator("Header", StringCell.TYPE).createSpec();
        allColSpecs[1] = new DataColumnSpecCreator("Sequence", StringCell.TYPE).createSpec();
        
        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
        
        BufferedDataContainer container = exec.createDataContainer(outputSpec);
        
        String line = "";
        String header = "";
        String sequence = "";
        URL input;
        
        if (FASTAReaderNodeCustom.isLocalFile(input_file.getStringValue())) {
        	
        	File inputFile = new File(input_file.getStringValue());
        	input = inputFile.toURI().toURL();
        	
        }
        
        else {
        	
        	input = new URL(input_file.getStringValue());
        	
        }
        
        int i = 0;
        long fileSize = 0;
        long bytesRead = 0;
        double progress = 0;
        
        try {
            
        	BufferedFileReader in = BufferedFileReader.createNewReader(input);
        	
        	fileSize = in.getFileSize();
        	
        	// System.out.println("Input file size: " + fileSize + " Bytes");
        	
        	line = in.readLine();
        	
        	do {
        		
        		bytesRead = in.getNumberOfBytesRead();
        		
        		// Set progress and progress bar
        		
        		if (fileSize > 0) {
        			progress = (double) bytesRead / fileSize;
                    exec.setProgress(progress, "Reading input file...");
                }
        		
        		else {
        			exec.setMessage("Reading input file...");
                }
        		
        		if (!line.isEmpty()) {							// skip empty rows in input file
        			
        			if (line.charAt(0) == '>') {
        		
        				sequence = "";
        				header = line;
        		
        			}
        			
        			else if (line.charAt(0) == ';') {			// skip comment lines
        				
        			}
        			
        			else {
        		
        				sequence = sequence.concat(line);
        		
        			}
        			
        		}
        		
        		//System.out.println("Line: " + line + " | Header: " + header + " | Sequence: " + sequence);
        		        		
        		line = in.readLine();
        		        		
        		if (line != null && !line.equals("")) { 
        			
        			if (line.charAt(0) == '>') { 
        				
        				//System.out.println("Next entry!");
        				
        				RowKey key = new RowKey("Row" + i);
        		        
        		        DataCell[] cells = new DataCell[2];
        		        
        		        cells[0] = new StringCell(header); 
        		        cells[1] = new StringCell(sequence); 
        		        
        		        DataRow row = new DefaultRow(key, cells);
        		        
        		        container.addRowToTable(row);
        		        
        				i++;
        				
        			} 
        			
        		}
        		
        		if (line == null) {
        			
        			//System.out.println("End of file!");
        			
        			RowKey key = new RowKey("Row" + i);
    		        
    		        DataCell[] cells = new DataCell[2];
    		        
    		        cells[0] = new StringCell(header); 
    		        cells[1] = new StringCell(sequence); 
    		        
    		        DataRow row = new DefaultRow(key, cells);
    		        
    		        container.addRowToTable(row);
        		
        		}  
        		
        	} while (line != null);
        	
        }
    	
        catch (IOException e) {
        	
        	//System.out.println("FEHLER HIER");
        	e.printStackTrace();
    		
    	}
        
        container.close();
        
        BufferedDataTable out = container.getTable();
        
        return new BufferedDataTable[] { out };
    }

	@Override
    protected void reset() {
        // TODO Code executed on reset.
        // Models build during execute are cleared here.
        // Also data handled in load/saveInternals will be erased here.
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        
        // TODO: check if user settings are available, fit to the incoming
        // table structure, and the incoming types are feasible for the node
        // to execute. If the node can execute in its current state return
        // the spec of its output data table(s) (if you can, otherwise an array
        // with null elements), or throw an exception with a useful user message

        return new DataTableSpec[] { null };
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        input_file.saveSettingsTo(settings);

    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        input_file.loadSettingsFrom(settings);

    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        input_file.validateSettings(settings);

    }
    
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