package avl.sv.shared.solution;

import avl.sv.shared.MessageStrings;
import avl.sv.shared.Permissions;
import avl.sv.shared.PermissionsSet;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.PermissionDenied;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolutionsSourceKVStore extends SolutionsSource{
       
    private final AVM_Session session;

    public SolutionsSourceKVStore(AVM_Session session) {
        this.session = session;
    }    
    
    @Override
    public String getDescription(int id) {
        SolutionSourceKVStore ss;
        try {
            ss = SolutionSourceKVStore.get(session, id);
            return ss.getDescription();
        } catch (PermissionDenied ex) {
            Logger.getLogger(SolutionsSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @Override
    public String getPermissions(int id, String targetUsername) {
        SolutionSourceKVStore ss;
        try {
            ss = SolutionSourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(SolutionsSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        if ((targetUsername == null) || (targetUsername.isEmpty())) {
            targetUsername = session.username;
        }
        if (ss.getPermissions(session.username).canRead()){
            Permissions p = ss.getPermissions(targetUsername);
            return p.name();
        } else {
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @Override
    public SolutionSource getSolutionSource(int id) throws PermissionDenied {
        return SolutionSourceKVStore.get(session, id);
    }

    @Override
    public String getSolutionsList() {
        ArrayList<PermissionsSet> permissionsSets = SolutionSourceKVStore.getPermissionsSets(session.username);
        StringBuilder sb = new StringBuilder();
        for (PermissionsSet ps : permissionsSets) {
            SolutionSourceKVStore solutionSourceKVStore;
            try {
                solutionSourceKVStore = SolutionSourceKVStore.get(session, ps.getID());
            } catch (PermissionDenied ex) {
                continue;
            }
            String name = solutionSourceKVStore.getName();
            sb.append(ps.getID()).append(",").append(name).append(",").append(solutionSourceKVStore.getPermissions().name()).append(";");
        }
        return sb.toString();
    }

    @Override
    public SolutionSource showNewSolutionDialog() {
        NameDescriptionPrompt<SolutionSource> dialogNewSolution = new NameDescriptionPrompt<SolutionSource>(null, true) {
            @Override
            public void create(String name, String description) {
                try {
                    SolutionSource solutionSource = SolutionSourceKVStore.create(session, name);
                    solutionSource.setDescription(description);
                    setResult(solutionSource);
                    dispose();
                } catch (PermissionDenied ex) {
                    Logger.getLogger(SolutionsSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
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
        String setRefs = getSolutionsList();
        if (!setRefs.isEmpty()) {
            for (final String pair : setRefs.split(";")) {
                final String temp[] = pair.split(",");
                if (temp.length == 3) {
                    int id = Integer.valueOf(temp[0]);
                    String name = temp[1];
                    SolutionSource solutionSource;
                    try {
                        solutionSource = SolutionSourceKVStore.get(session, id);
                        solutionSource.setNameQuiet(name);
                        solutionSources.add(solutionSource);
                    } catch (PermissionDenied ex) {
                        Logger.getLogger(SolutionsSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    }
                }
            }
        }
        return solutionSources;
    }
    
}
