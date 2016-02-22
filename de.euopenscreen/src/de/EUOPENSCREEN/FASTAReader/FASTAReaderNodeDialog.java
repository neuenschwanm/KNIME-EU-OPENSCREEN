package de.EUOPENSCREEN.FASTAReader;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class FASTAReaderNodeDialog extends DefaultNodeSettingsPane {

    public FASTAReaderNodeDialog() {
        
    	final SettingsModelString stringModel = new SettingsModelString(FASTAReaderNodeModel.FILE, FASTAReaderNodeModel.DEFAULT_FILE);
    	
    	DialogComponentFileChooser myDialogComponentFileChooser = new DialogComponentFileChooser(
    			stringModel,
    			"FASTAReaderHistory",
    			JFileChooser.OPEN_DIALOG,
    			false,
    			createFlowVariableModel(stringModel));  
    	
    	myDialogComponentFileChooser.setBorderTitle("Input File:");
    	
    	addDialogComponent(myDialogComponentFileChooser);
    	
    }
}