package avl.sv.shared.study;

public interface ListenerROI_Folder {

    public void updated(ROI_Folder folder);
    public void updated(ROI_Folder folder, ROI originalROI, ROI newROI);    
    public void add(ROI_Folder folder, ROI roi);
    public void remove(ROI_Folder folder, ROI roi);
    
}
