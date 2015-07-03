package avl.sv.shared.model.featureGenerator;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import javax.swing.JPanel;
import org.w3c.dom.Element;

abstract public class AbstractFeatureGenerator implements Serializable {
    public boolean isactive = true;
    abstract public double[][] getFeaturesForImages(BufferedImage img[]) throws Throwable;
    abstract public String[] getFeatureNames();
    abstract public void setFeatureNames(String[] names);
    
    abstract public int getNumberOfFeatures();
    
    public void addAttributes(Element featuresNode){ }
    public void parseAttributs(Element featuresNode){ }
    
    abstract public JPanel getOptionsPanel();

}
