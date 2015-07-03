package avl.sv.shared.model.classifier;

import avl.sv.shared.solution.Sample;
import avl.sv.shared.solution.SampleSetClass;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface ClassifierInterface {

    public boolean isActive();
    public void setActive(boolean isActive);

    public String getMessage();
            
    public Date getLastTrained();
    
    public String getName();
        
    public void train(ArrayList<SampleSetClass> samplesSets);
    
    public boolean isValid();
    
    public void predict(ArrayList<Sample> samples);   
   
    public JPanel getOptionsPanel();

    public void invalidate();

    public Element getProperties(Document doc);
    public void setProperties(Node element);
    
}
