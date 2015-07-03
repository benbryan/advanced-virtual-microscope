
package avl.sv.client.study;

import avl.sv.shared.study.ROI_Folder;
import avl.sv.shared.study.StudyChangeEvent;


public class ROI_TreeTable_Study extends ROI_TreeTable {
    @Override
    protected ROI_Folder getDefaultROI_Folder() {
        for (ROI_Folder anno : annoSet.getROI_Folders()) {
            if (anno.getName().equals("Label")) {
                return anno;
            }
        }
        ROI_Folder folder = ROI_Folder.createDefault();
        addROI_Folder(folder);
        return folder;
    }




}
