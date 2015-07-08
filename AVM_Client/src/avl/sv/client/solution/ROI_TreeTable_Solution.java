
package avl.sv.client.solution;

import avl.sv.client.study.*;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.study.ROI_Folder;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class ROI_TreeTable_Solution extends ROI_TreeTable {
    @Override
    protected ROI_Folder getDefaultROI_Folder() {
        ArrayList<ROI_Folder> folders = annoSet.getROI_Folders();
        for (ROI_Folder folder:folders){
            if (folder.toString().equals(MessageStrings.Temporary)){
                return folder;
            }
        }
        ROI_Folder folder = ROI_Folder.createDefault();
        folder.setName(MessageStrings.Temporary, true);
        model.insertNodeInto(folder, annoSet, 0);
        return folder;
    }
}
