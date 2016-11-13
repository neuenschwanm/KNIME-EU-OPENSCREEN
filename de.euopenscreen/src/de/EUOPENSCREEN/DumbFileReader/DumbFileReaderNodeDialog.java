package de.EUOPENSCREEN.DumbFileReader;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "DumbFileReader" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author 
 */
public class DumbFileReaderNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring DumbFileReader node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected DumbFileReaderNodeDialog() {
        super();
        
        //the dialog for choosing the directory with the data files
        addDialogComponent(new DialogComponentFileChooser(
        		new SettingsModelString(DumbFileReaderNodeModel.CFGKEY_FOLDERNAME, DumbFileReaderNodeModel.DEFAULT_FOLDERNAME),
        		"myID222",JFileChooser.OPEN_DIALOG,true)
        		
        		);
       
       //the dialog for selecting the line number containing the column names
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    DumbFileReaderNodeModel.CFGKEY_HEADER,
                    DumbFileReaderNodeModel.DEFAULT_HEADER,
                    1, 10000000),
                    "Line number of header line", /*step*/ 1, /*componentwidth*/ 5));
        
        //the first data line to analyze
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    DumbFileReaderNodeModel.CFGKEY_START,
                    DumbFileReaderNodeModel.DEFAULT_START,
                    1, 10000000),
                    "Line number of first data line", /*step*/ 1, /*componentwidth*/ 5)); 
        
        //the last data line to analyze
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    DumbFileReaderNodeModel.CFGKEY_END,
                    DumbFileReaderNodeModel.DEFAULT_END,
                    1, 10000000),
                    "Line number of last data line", /*step*/ 1, /*componentwidth*/ 5)); 
        
        //the dialog for choosing the directory with the data files
        addDialogComponent(new DialogComponentString(
        		new SettingsModelString(DumbFileReaderNodeModel.CFGKEY_DELIMITER, DumbFileReaderNodeModel.DEFAULT_DELIMITER), "Delimiter")
        		
        		);
                    
    }
}

