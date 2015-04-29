package de.EUOPENSCREEN.FastTanimoto2;



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
public class FastTanimoto2NodeDialog extends DefaultNodeSettingsPane {

 
	/**
     * New pane for configuring FastTanimoto node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    @SuppressWarnings("unchecked")
	protected FastTanimoto2NodeDialog() {
        super();
        
       
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelDoubleBounded(
                    FastTanimoto2NodeModel.CFGKEY_THRESHOLD,
                    FastTanimoto2NodeModel.DEFAULT_THRESHOLD,
                    0.0, 1.0),
                    "Tanimoto Threshold:", /*step*/ 0.05, /*componentwidth*/ 5));
 
        SettingsModelString columnNameID  = new SettingsModelString(FastTanimoto2NodeModel.CFGKEY_ID_COLUMN,null); 
        DialogComponent columnChooser1 = new DialogComponentColumnNameSelection(columnNameID,"Molecule Identifier, test set: ", 0, StringValue.class);
        addDialogComponent(columnChooser1);
           
        
        SettingsModelString columnNameFP  = new SettingsModelString(FastTanimoto2NodeModel.CFGKEY_FP_COLUMN,null);       
        DialogComponent columnChooser2 = new DialogComponentColumnNameSelection(columnNameFP,"Molecule Fingerprint, test set: ", 0, BitVectorValue.class);
        addDialogComponent(columnChooser2);
        
       
        SettingsModelString columnNameID2  = new SettingsModelString(FastTanimoto2NodeModel.CFGKEY_ID_COLUMN2 ,null); 
        DialogComponent columnChooser3 = new DialogComponentColumnNameSelection(columnNameID2,"Molecule Identifier, reference set: ", 0, StringValue.class);
        addDialogComponent(columnChooser3);
           
        
        SettingsModelString columnNameFP2  = new SettingsModelString(FastTanimoto2NodeModel.CFGKEY_FP_COLUMN2,null);
        DialogComponent columnChooser4 = new DialogComponentColumnNameSelection(columnNameFP2,"Molecule Fingerprint, reference set: ", 0, BitVectorValue.class);
        addDialogComponent(columnChooser4);
        
        
    }
}

