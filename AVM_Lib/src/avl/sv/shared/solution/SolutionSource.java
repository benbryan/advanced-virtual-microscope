package avl.sv.shared.solution;

import avl.sv.shared.AVM_Source;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.Permissions;
import avl.sv.shared.solution.xml.SolutionXML_Writer;
import avl.sv.shared.study.StudySource;
import java.util.Properties;

public abstract class SolutionSource extends AVM_Source{
    public final int solutionId;
    private String name = null;
    
    public SolutionSource(int id) {
        this.solutionId = id;
    }
    
    abstract public String getUsers() throws PermissionDenied;

    abstract public String setSolution(String xml) throws PermissionDenied;

    public String setSolution(Solution solution) throws PermissionDenied{
        String xml = SolutionXML_Writer.getXMLString(solution);
        return setSolution(xml);
    }
    
    abstract public Solution getSolution();
    abstract public StudySource getStudySource();
                      
    abstract public String delete() throws PermissionDenied;
    
    @Override
    public String toString(){
        if (name == null){
            name = getName();
        }
        return name;
    }
    
    abstract public Properties getFeatures(ImageReference imageReference, int tileDim, int tileWindowDim, String featureGeneratorName, String featureNames[]);
    abstract public String generateInDatabase(ImageReference imageReference, int tileDim, int tileWindowDim, String featureGeneratorClassName, String featureNames[]);

    abstract public SolutionSource cloneSolution(String cloneName) throws PermissionDenied;
    
    abstract public Permissions getPermissions(String username) throws PermissionDenied;
    abstract public String setPermissions(String username, Permissions permission) throws PermissionDenied;
    abstract public Permissions getPermissions();
    abstract public void addSolutionChangeListener(SolutionChangeListener listener);
    abstract public void removeSolutionChangeListener(SolutionChangeListener listener);
    abstract public void removeAllSolutionChangeListeners();
    abstract public void close();

    abstract public String getName();
    abstract public void setName(String name) throws PermissionDenied;
    abstract public void setNameQuiet(String name);
    
    
}
