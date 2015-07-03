package avl.sv.client.study;

import avl.sv.client.image.ImagesSourcePort;
import avl.sv.server.study.StudyPort;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.Permissions;
import avl.sv.shared.image.ImagesSource;
import avl.sv.shared.solution.NameDescriptionPrompt;
import avl.sv.shared.study.StudiesSource;
import avl.sv.shared.study.StudySource;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class StudiesSourcePort implements StudiesSource{

    final private StudyPort studyPort;
    final private ImagesSourcePort imagesSource;
    private final AVM_Session avmSession;

    public StudiesSourcePort(ImagesSourcePort imagesSource, StudyPort studyPort, AVM_Session avmSession) {
        this.imagesSource = imagesSource;
        this.studyPort = studyPort;
        this.avmSession = avmSession;
    }
    
    @Override
    public ArrayList<StudySource> getStudySources() {
        ArrayList<StudySource> studySources = new ArrayList<>();
        String setRefs = studyPort.getList();
        if (!setRefs.isEmpty()) {
            for (final String pair : setRefs.split(";")) {
                final String temp[] = pair.split(",");
                if (temp.length == 3) {
                    int id = Integer.valueOf(temp[0]);
                    String name = temp[1];
                    Permissions permissions = Permissions.valueOf(temp[2]);
                    StudySourcePort studySource = new StudySourcePort(imagesSource, studyPort, id, avmSession, permissions);
                    studySource.setNameQuiet(name);
                if (studySource.getPermissions().canRead()) {
                    studySources.add(studySource);
                }
            }
        }
        }
        return studySources;
    }

    @Override
    public StudySource showNewStudyDialog() {
        NameDescriptionPrompt<StudySource> dialogNewStudy = new NameDescriptionPrompt<StudySource>(null, true) {
            @Override
            public void create(String studyName, String description) {
                String result = studyPort.create(studyName);
                int id = Integer.valueOf(result);
                if (result.startsWith("error:") || (id < 0)) {
                    JOptionPane.showMessageDialog(null, "A study by that name already exists", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                StudySourcePort newStudy = new StudySourcePort(imagesSource, studyPort, id, avmSession);
                try {
                    newStudy.setName(studyName);
                    newStudy.setDescription(description);
                } catch (PermissionDenied ex) {
                    Logger.getLogger(StudiesSourcePort.class.getName()).log(Level.SEVERE, null, ex);
                }
                setResult(newStudy);
                dispose();
            }
        };
        dialogNewStudy.setSize(dialogNewStudy.getPreferredSize());
        dialogNewStudy.setVisible(true);
        return dialogNewStudy.getResult();
    }

    @Override
    public ImagesSource getImagesSource() {
        return imagesSource;
    }

    @Override
    public StudySource getStudySource(int id) {
        ArrayList<StudySource> studySources = getStudySources();
        for (StudySource s:studySources){
            if (s.studyID==id){
                return s;
            }
        }
        return null;
    }
    
}
