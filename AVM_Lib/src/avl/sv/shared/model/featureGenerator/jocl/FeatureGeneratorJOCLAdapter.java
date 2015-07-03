package avl.sv.shared.model.featureGenerator.jocl;

import avl.sv.shared.model.featureGenerator.AbstractFeatureGenerator;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.swing.JPanel;

public class FeatureGeneratorJOCLAdapter extends AbstractFeatureGenerator  {

    private FeatureGeneratorJOCL generator;
    
    public FeatureGeneratorJOCLAdapter() {
//            ArrayList<String> classFilter = new ArrayList<>();
//            classFilter.add("avl.sv.shared.model.featureGenerator.jocl.GeneratorReloadable");
//            classFilter.add("org.jocl.");
//            AVM_ClassLoader loader = new AVM_ClassLoader(getClass().getClassLoader(), classFilter);            
            
            final int platformIndex = JOCL_Configure.getSelectedPlatformIndex();
            final int deviceIndex = JOCL_Configure.getSelectedDeviceIndex();            

//            Class c = loader.loadClass("avl.sv.shared.model.featureGenerator.jocl.GeneratorReloadable");
//            generator = (FeatureGeneratorJOCL) c.getConstructor(new Class[]{int.class, int.class}).newInstance(new Object[]{platformIndex, deviceIndex});
            generator = new FeatureGeneratorJOCL(platformIndex, deviceIndex);

    }
    
    @Override
    public JPanel getOptionsPanel() {
        return new OptionsPromptJOCL_Features(generator);
    }
    
    @Override
    public String toString() {
        return "JOCL";
    }
    
    @Override
    public int getNumberOfFeatures() {
        return generator.getNumberOfFeatures();
    }
    
    @Override
    public void setFeatureNames(String[] featureNames) {
        generator.setFeatureNames(featureNames);
    }
    
        @Override
    public String[] getFeatureNames() {
        return generator.getFeatureNames();
    }
    
    @Override
    public double[][] getFeaturesForImages(BufferedImage[] imgs) throws Throwable {
        return generator.getFeaturesForImages(imgs);
    }
}
