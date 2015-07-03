package avl.sv.shared.solution;

import avl.sv.shared.PermissionDenied;
import avl.sv.shared.Permissions;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


abstract public class SolutionsSource {

    abstract public String getDescription(int id);

    abstract public String getPermissions(int id, String targetUsername) throws PermissionDenied;

    abstract public SolutionSource getSolutionSource(int id) throws PermissionDenied;

    abstract public String getSolutionsList();

    abstract public SolutionSource showNewSolutionDialog();
    
    abstract public ArrayList<SolutionSource> getSolutionSources();
    

}
