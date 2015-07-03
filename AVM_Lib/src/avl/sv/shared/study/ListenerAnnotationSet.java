package avl.sv.shared.study;

public interface ListenerAnnotationSet {

    public void add(AnnotationSet annoSet, ROI_Folder folder);
    public void remove(AnnotationSet annoSet, ROI_Folder folder);

    public void add(AnnotationSet annoSet, ROI_Folder folder, ROI roi);
    public void remove(AnnotationSet annoSet, ROI_Folder folder, ROI roi);

    public void updated(AnnotationSet annoSet, ROI_Folder folder, ROI original, ROI newROI);

    public void updated(AnnotationSet annoSet, ROI_Folder folder);
    
}
