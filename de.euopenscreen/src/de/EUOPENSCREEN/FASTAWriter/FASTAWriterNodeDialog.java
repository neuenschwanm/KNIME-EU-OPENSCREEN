package de.EUOPENSCREEN.FASTAWriter;

import javax.swing.JFileChooser;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * <code>NodeDialog</code> for the "FASTAWriter" Node.
 * takes input column with FASTA header, input column with FASTA sequence, and writes all entries with header and sequence to a single FASTA file
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Martin Neuenschwander
 */
public class FASTAWriterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring FASTAWriter node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */

	@SuppressWarnings("unchecked")
	protected FASTAWriterNodeDialog() {
        super();
        
        //Component for choosing the file
       
        /** create the settings models */
         SettingsModelString m_file = new SettingsModelString(FASTAWriterNodeModel.CFGKEY_FILE, null);
         SettingsModelString m_header = new SettingsModelString(FASTAWriterNodeModel.CFGKEY_COLUMN_NAME_HEADER,null);
         SettingsModelOptionalString m_comment = new SettingsModelOptionalString(FASTAWriterNodeModel.CFGKEY_COLUMN_NAME_COMMENT, null, true);
         SettingsModelString m_sequence = new SettingsModelString(FASTAWriterNodeModel.CFGKEY_COLUMN_NAME_SEQ, null);
      
       addDialogComponent(new DialogComponentFileChooser(m_file,"abcd",JFileChooser.SAVE_DIALOG,false));    
       addDialogComponent(new DialogComponentColumnNameSelection(m_header,"Column containing the header:",0,StringValue.class));
       addDialogComponent(new DialogComponentColumnNameSelection(m_comment,"Column containing the comment (optional):",0,false, true, StringValue.class));
       addDialogComponent(new DialogComponentColumnNameSelection(m_sequence,"Column containing the sequence:",0,StringValue.class));
       
                    
    }
}

