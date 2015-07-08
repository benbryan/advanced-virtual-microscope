package avl.sv.shared.solution;

import avl.sv.shared.model.classifier.ClassifierWeka;
import avl.sv.shared.model.featureGenerator.AbstractFeatureGenerator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import weka.core.Instances;

public class Solution extends DefaultMutableTreeTableNode implements Serializable  {
    private final ArrayList<ClassifierWeka> classifiers = new ArrayList<>();
    private final ArrayList<AbstractFeatureGenerator> featureGenerators = new ArrayList<>();
    private final Properties properties = new Properties(defaultProperties());

    public enum PropertyNames {
        TileWindowDim ("TileWindowDim"),   
        TileDim       ("TileDim"),
        ClassNames    ("ClassNames");

        private final String value;
        PropertyNames(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        } 
    }
    
    public Solution(String name) {
        super(name);   
    }

    private Properties defaultProperties(){
        Properties p = new Properties();
        p.setProperty(PropertyNames.TileDim.name(), String.valueOf(128));
        p.setProperty(PropertyNames.TileWindowDim.name(), String.valueOf(128));
        p.setProperty(PropertyNames.ClassNames.name(), "1,Class 1;2,Class 2;");
        return p;
    }
    public Properties getProperties(){
        return properties;
    }
    public void setProperty(String key, String value){
        properties.setProperty(key, value);
    }
    
    public void setTileDim(int tileDim) {
        properties.setProperty(PropertyNames.TileDim.name(), String.valueOf(tileDim));
    }
    public void setTileWindowDim(int tileWindowDim) {
        properties.setProperty(PropertyNames.TileWindowDim.name(), String.valueOf(tileWindowDim));
    }
    public int getTileDim() {
        return Integer.valueOf(properties.getProperty(PropertyNames.TileDim.name()));
    }
    public int getTileWindowDim() {
        return Integer.valueOf(properties.getProperty(PropertyNames.TileWindowDim.name()));
    }
    
    /**
     *
     * @param newName
     * Name of the new class
     * @return
     * Return -1 if failed to add class
     * else it returns an integer representing the new folder id 
     */
    public long addClassifierClassName(String newName){
        HashMap<Long, String> classes = getClassifierClassNames();
        for (String name:classes.values()){
            if (name.equals(newName)){
                return -1;
            }
        }
        Random random = new Random(new Date().getTime());
        long r = -1;
        for (int i = 0; i < 1000; i++){
            r = random.nextLong();
            if (!classes.keySet().contains(r)){
                break;
            }
        }
        if (r == -1){
            // Failed to generate a unique id;
            return -1;
        }
        classes.put(r, newName);
        setClassifierClassNames(classes);
        return r;
    }
    public void removeClassifierClassName(String nameToRemove){
        HashMap<Long, String> classes = getClassifierClassNames();
        for (Map.Entry<Long, String> entry:classes.entrySet()){
            if (entry.getValue().equals(nameToRemove)){
                classes.remove(entry.getKey());
                break;
            }
        }
        setClassifierClassNames(classes);
    }
    
    public void renameClassifierClassName(String target, String newName){
        HashMap<Long, String> classes = getClassifierClassNames();
        for (Map.Entry<Long, String> entry:classes.entrySet()){
            if (entry.getValue().equals(target)){
                entry.setValue(newName);
                break;
            }
        }
        setClassifierClassNames(classes);
    }
    
    public HashMap<Long, String> getClassifierClassNames(){
        String[] pairs = properties.getProperty(PropertyNames.ClassNames.name()).split(";");
        HashMap<Long, String> classes = new HashMap<>();
        for (String pair:pairs){
            String[] temp = pair.split(",");
            if (temp.length != 2){
                continue;
            }
            try {
                classes.put(Long.valueOf(temp[0].trim()), temp[1].trim());
            } catch (NumberFormatException ex){            }             
        }
        return classes;
    }
    private void setClassifierClassNames(HashMap<Long, String> classes){
        StringBuilder newNames = new StringBuilder();
        for (Map.Entry<Long, String> entry:classes.entrySet()){
            newNames.append(entry.getKey()).append(",").append(entry.getValue()).append(";");
        }
        properties.setProperty(PropertyNames.ClassNames.name(), newNames.toString());        
    }
    
    public void trainClassifiers(Instances instances){
        for (ClassifierWeka classifier:classifiers){
            if (classifier.isActive()){
                classifier.train(instances);
            }
        }
    }
    
    public int getNumelClassifiers(){
        return classifiers.size();
    }
    public ArrayList<ClassifierWeka> getClassifiers() {
        return classifiers;
    }
    public void setClassifier(ClassifierWeka abstractClassifier ){
        for (ClassifierWeka classifier:classifiers){
            if (abstractClassifier.getClass() == classifier.getClass()){
                classifiers.remove(classifier);
                classifiers.add(abstractClassifier);
                return;
            }
        }
        classifiers.add(abstractClassifier);
    }
    public void removeClassifier(Class c){
        for (ClassifierWeka classifier:classifiers){
            if (c == classifier.getClass()){
                classifiers.remove(classifier);
                return;
            }
        }
    }
    public ClassifierWeka getClassifier(String c){
        for (ClassifierWeka classifier:classifiers){
            if (classifier.getClass().getCanonicalName().equals(c)){
                return classifier;
            }
        }
        return null;
    }
    public boolean hasValidClassifier(){
        for(ClassifierWeka classifier:classifiers){
            if (classifier.isValid()){
                return true;
            }
        }
        return false;
    }
    
    public int getNumelFeatureGenerators(){
        return featureGenerators.size();
    }
    public ArrayList<AbstractFeatureGenerator> getFeatureGenerators() {
        return featureGenerators;
    }
    public void setFeatureGenerator(AbstractFeatureGenerator abstractFeatureGenerator){
        for (AbstractFeatureGenerator featureGenerator:featureGenerators){
            if (abstractFeatureGenerator.getClass() == featureGenerator.getClass()){
                featureGenerators.remove(featureGenerator);
                featureGenerators.add(abstractFeatureGenerator);
                return;
            }
        }
        featureGenerators.add(abstractFeatureGenerator);
    }
    public void removeFeatureGenerator(Class c){
        for (AbstractFeatureGenerator featureGenerator:featureGenerators){
            if (c == featureGenerator.getClass()){
                featureGenerators.remove(featureGenerator);
                return;
            }
        }
    }
    public AbstractFeatureGenerator getFeatureGenerator(String c){
        for (AbstractFeatureGenerator featureGenerator:featureGenerators){
            if (featureGenerator.getClass().getCanonicalName().equals(c)){
                return featureGenerator;
            }
        }
        return null;
    }
    public int getNumelFeatures(){
        int numelFeatures = 0;
        for (AbstractFeatureGenerator featureGenerator:featureGenerators){
            if (featureGenerator.isactive){
                numelFeatures += featureGenerator.getNumberOfFeatures();
            }
        }
        return numelFeatures;
    }
       
}

