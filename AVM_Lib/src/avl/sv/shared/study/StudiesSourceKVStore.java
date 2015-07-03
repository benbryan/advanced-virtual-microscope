package avl.sv.shared.study;

import avl.sv.shared.image.ImagesSource;
import avl.sv.shared.image.ImagesSourceKVStore;
import avl.sv.shared.PermissionsSet;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.solution.NameDescriptionPrompt;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudiesSourceKVStore implements StudiesSource{

    final private AVM_Session session;

    public StudiesSourceKVStore(AVM_Session session) {
        this.session = session;
    }
    
    @Override
    public ArrayList<StudySource> getStudySources() {
        ArrayList<StudySource> studySources = new ArrayList<>();
        ArrayList<PermissionsSet> permissionsSets = StudySourceKVStore.getPermissionsSets(session.username);
        for (PermissionsSet ps : permissionsSets) {
            StudySourceKVStore studySourceKVStore;
            try {
                studySourceKVStore = StudySourceKVStore.get(session, ps.getID());
            } catch (PermissionDenied ex) {
                Logger.getLogger(StudiesSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            if (studySourceKVStore == null) {
                continue;
            }
            if (studySourceKVStore.getName().isEmpty()){
                continue;
            }
            studySources.add(studySourceKVStore);
        }
        return studySources;
    }

    @Override
    public StudySource showNewStudyDialog() {
        NameDescriptionPrompt<StudySource> dialogNewStudy = new NameDescriptionPrompt<StudySource>(null, true){
            @Override
            public void create(String studyName, String description) {
                StudySourceKVStore ss = StudySourceKVStore.create(session, studyName);
                try {
                    ss.setDescription(description);
                } catch (PermissionDenied ex) {
                    Logger.getLogger(StudiesSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
                }
                setResult(ss);
                dispose();
            }
        };
        dialogNewStudy.setSize(dialogNewStudy.getPreferredSize());
        dialogNewStudy.setVisible(true);
        return dialogNewStudy.getResult();
    }
    
    public String getStudySourcesString() {
        ArrayList<PermissionsSet> permissionsSets = StudySourceKVStore.getPermissionsSets(session.username);
        StringBuilder sb = new StringBuilder();
        for (PermissionsSet ps : permissionsSets) {
            StudySourceKVStore studySourceKVStore;
            try {
                studySourceKVStore = StudySourceKVStore.get(session, ps.getID());
            } catch (PermissionDenied ex) {
                Logger.getLogger(StudiesSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            if (studySourceKVStore == null) {
                continue;
            }
            sb.append(ps.getID()).append(",")
                    .append(studySourceKVStore.getName()).append(",")
                    .append(studySourceKVStore.getPermissions().name()).append(";");
        }
        return sb.toString();
    }

    @Override
    public ImagesSource getImagesSource() {
        return new ImagesSourceKVStore(session);
    }

    @Override
    public StudySource getStudySource(int id) throws PermissionDenied {
        return StudySourceKVStore.get(session, id);
    }
    
}
