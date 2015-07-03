package avl.sv.shared.study;

import avl.sv.shared.PermissionDenied;
import avl.sv.shared.image.ImagesSource;
import java.util.ArrayList;

public interface StudiesSource {    
    public ArrayList<StudySource> getStudySources();
    public StudySource showNewStudyDialog();
    public ImagesSource getImagesSource();
    public StudySource getStudySource(int id) throws PermissionDenied;
}
