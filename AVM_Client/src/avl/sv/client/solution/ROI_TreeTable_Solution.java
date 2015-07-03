
package avl.sv.client.solution;

import avl.sv.client.study.*;
import avl.sv.shared.study.ROI_Folder;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class ROI_TreeTable_Solution extends ROI_TreeTable {
    @Override
    protected ROI_Folder getDefaultROI_Folder() {
        ArrayList<ROI_Folder> folders = annoSet.getROI_Folders();
        if ((folders == null) || (folders.size() <= 0)) {
            JOptionPane.showMessageDialog(null, "Add at least one Classifier Class to store ROIs\nLook in the Solution Manager", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return folders.get(0);
    }
}
