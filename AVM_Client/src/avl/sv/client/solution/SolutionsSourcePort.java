package avl.sv.client.solution;

import avl.sv.shared.solution.NameDescriptionPrompt;
import avl.sv.shared.solution.SolutionsSource;
import static avl.sv.client.AdvancedVirtualMicroscope.getInstance;
import avl.sv.client.image.ImagesSourcePort;
import avl.sv.server.solution.SolutionPort;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.AVM_Source;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.Permissions;
import avl.sv.shared.solution.SolutionSource;
import avl.sv.shared.study.StudiesSource;
import avl.sv.shared.study.StudySource;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


public class SolutionsSourcePort extends SolutionsSource {

    private final ImagesSourcePort imagesSource;
    private final StudiesSource studiesSource;
    private final SolutionPort solutionPort;
    private final AVM_Session avmSession;

    public SolutionsSourcePort(String username, ImagesSourcePort imagesSource, StudiesSource studiesSource, SolutionPort solutionPort, AVM_Session avmSessin) {
        this.imagesSource = imagesSource;
        this.studiesSource = studiesSource;
        this.solutionPort = solutionPort;
        this.avmSession = avmSessin;
    }

    @Override
    public String getDescription(int id) {
        return solutionPort.getDescription(id);
    }

    @Override
    public String getPermissions(int id, String targetUsername) {
        return solutionPort.getPermissions(id, targetUsername);
    }

    @Override
    public SolutionSource getSolutionSource(int id) {
        try {
            StudySource studySource = studiesSource.getStudySource(id);
            return SolutionSourcePort.get(imagesSource, solutionPort, studySource, id, avmSession);
        } catch (PermissionDenied ex) {
            Logger.getLogger(SolutionsSourcePort.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String getSolutionsList() {
        return solutionPort.getSolutionsList();
    }

    @Override
    public SolutionSource showNewSolutionDialog() {
        NameDescriptionPrompt<SolutionSource> dialogNewSolution = new NameDescriptionPrompt<SolutionSource>(getInstance(), true) {
            @Override
            public void create(String name, String description) {
                try {
                    String result = solutionPort.create(name);
                    if (result.startsWith("error:")) {
                        JOptionPane.showMessageDialog(null, result, "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int id = Integer.valueOf(result);
                    StudySource studySource = studiesSource.getStudySource(id);
                    SolutionSource solutionSource = SolutionSourcePort.get(imagesSource, solutionPort, studySource, id, avmSession);
                    try {
                        solutionSource.setDescription(description);
                        solutionSource.setName(name);
                    } catch (PermissionDenied ex) {
                        Logger.getLogger(SolutionsSourcePort.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    setResult(solutionSource);
                    dispose();
                } catch (PermissionDenied ex) {
                    Logger.getLogger(SolutionsSourcePort.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        dialogNewSolution.setSize(dialogNewSolution.getPreferredSize());
        dialogNewSolution.setVisible(true);
        return dialogNewSolution.getResult();
    }

    @Override
    public ArrayList<SolutionSource> getSolutionSources() {
        ArrayList<SolutionSource> solutionSources = new ArrayList<>();
        ArrayList<StudySource> studySources = studiesSource.getStudySources();
        String setRefs = getSolutionsList();
        if (!setRefs.isEmpty()) {
            for (final String pair : setRefs.split(";")) {
                final String temp[] = pair.split(",");
                if (temp.length == 3) {
                    int id = Integer.valueOf(temp[0]);
                    String name = temp[1];
                    Permissions permissions = Permissions.valueOf(temp[2]);
                    for (StudySource studySource : studySources) {
                        if (studySource.studyID == id) {
                            SolutionSource solutionSource = SolutionSourcePort.get(imagesSource, solutionPort, studySource, id, avmSession, permissions);
                            solutionSource.setNameQuiet(name);
                            solutionSources.add(solutionSource);
                        }
                    }
                }
            }
        }
        return solutionSources;
    }


    
}
