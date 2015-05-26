package de.EUOPENSCREEN.FastTanimoto;


import org.knime.core.data.StringValue;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "FastTanimoto" Node.
 * compares fingerprints and lists identifiers having scores larger than a preset threshold
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Martin Neuenschwander
 */
public class FastTanimotoNodeDialog extends DefaultNodeSettingsPane {

 
	/**
     * New pane for configuring FastTanimoto node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    @SuppressWarnings("unchecked")
	protected FastTanimotoNodeDialog() {
        super();
        
      
        
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelDoubleBounded(
                    FastTanimotoNodeModel.CFGKEY_THRESHOLD,
                    FastTanimotoNodeModel.DEFAULT_THRESHOLD,
                    0.0, 1.0),
                    "Tanimoto Threshold:", /*step*/ 0.05, /*componentwidth*/ 5));
 
        SettingsModelString columnNameID  = new SettingsModelString(FastTanimotoNodeModel.CFGKEY_ID_COLUMN,null);
        
        DialogComponent columnChooser1 =
                new DialogComponentColumnNameSelection(columnNameID,
                        "Molecule Identifier: ", 0, StringValue.class);
        addDialogComponent(columnChooser1);
           
        
        SettingsModelString columnNameFP  = new SettingsModelString(FastTanimotoNodeModel.CFGKEY_FP_COLUMN,null);
        
        DialogComponent columnChooser2 =
                new DialogComponentColumnNameSelection(columnNameFP,
                        "Molecule Fingerprint: ", 0, BitVectorValue.class);
        addDialogComponent(columnChooser2);
        
        
    }
}

