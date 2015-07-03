package avl.sv.shared.model.classifier;

import avl.sv.shared.NamedNodeMapFunc;
import avl.sv.shared.solution.Sample;
import avl.sv.shared.solution.SampleSetClass;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.xml.bind.DatatypeConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class ClassifierWeka implements ClassifierInterface{

    private boolean active = false; 
    private Date lastTrained = null;
    private boolean valid;
    private Classifier classifier;
    String message = "";

    public ClassifierWeka() {
        this.classifier = new RandomForest();
    }
    
    public ClassifierWeka(Classifier classifierWeka) {
        this.classifier = classifierWeka;
    }   
    
    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean isActive) {
        this.active = isActive;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Date getLastTrained() {
        return lastTrained;
    }

    @Override
    public String getName() {
        return "Weka";
    }
        
    @Override
    public void train(ArrayList<SampleSetClass> samplesSets) {
        if (samplesSets.isEmpty() || samplesSets.get(0).samples.isEmpty()){
            valid = false;
            return;
        }
        try {
            ArrayList<String> classNames = new ArrayList<>();
            for (int i = 0; i < samplesSets.size(); i++){
                String name = samplesSets.get(i).className;
                classNames.add(name);
            }
            
            ArrayList<String> featureNames = samplesSets.get(0).featureNames;
            Instances instances = SamplesToInstances.convert(classNames, featureNames, samplesSets);
            
            classifier.buildClassifier(instances);
            
            // try one just to see if the classifierWeka works
            classifier.classifyInstance(instances.instance(0));
            valid = true;
            lastTrained = new Date();
            
            Evaluation eval = new Evaluation(instances);
            Random rand = new Random(1);  // using seed = 1
            int folds = 10;
            eval.crossValidateModel(classifier, instances, folds, rand);
            message = "Resulting classifier's 10 fold evaluation\n" + eval.toSummaryString();

        } catch (Exception ex) {
            valid = false;
            Logger.getLogger(ClassifierWeka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
//    public static void main(String[] args) {
//        
//        JFrame f = new JFrame();
//        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        OptionsPropmptRandomForest options = new OptionsPropmptRandomForest();
//        options.setVisible(true);
//        f.add(options);
//        f.setSize(500,500);
//        f.setVisible(true);
//
//        ClassifierRandomForest classifierRandomForest = new ClassifierRandomForest();
//        SampleSetClass ssc1 = new SampleSetClass("class1");
//        ssc1.samples = new ArrayList<>();
//        ssc1.samples.add(new Sample(new double[]{0.0, 0.5}, null));
//        
//        SampleSetClass ssc2 = new SampleSetClass("class2");
//        ssc2.samples = new ArrayList<>();
//        ssc2.samples.add(new Sample(new double[]{3.0, 2.0 }, null));
//        
//        ArrayList<SampleSetClass> sampleSets = new ArrayList<SampleSetClass>();
//        sampleSets.add(ssc1);
//        sampleSets.add(ssc2);
//        classifierRandomForest.train(sampleSets);
//        
//        ArrayList<Sample> testSamples = new ArrayList<>();
//        for (SampleSetClass sampleSet:sampleSets){
//            testSamples.addAll(sampleSet.samples);
//        }
//        classifierRandomForest.predict(testSamples);
//        int i = 0;
//        
//    }
    
    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void predict(ArrayList<Sample> samples) {
        if (samples.isEmpty()){
            return;
        }
        int numelFeatures = samples.get(0).featureVector.length;      
        Instances instances = getDummyDataset(numelFeatures);

        for (Sample sample:samples){
            Instance instance = new Instance(numelFeatures);
            for (int i = 0; i < sample.featureVector.length; i++){
                instance.setValue(instances.attribute(i), sample.featureVector[i]);
            }
            instance.setDataset(instances);
            double label = -1;
            try {
                label = classifier.classifyInstance(instance);
            } catch (Exception ex) {
                Logger.getLogger(ClassifierWeka.class.getName()).log(Level.SEVERE, null, ex);
            }
            sample.classifierLabel = label;
        }
    }
    
    private Instances getDummyDataset(int numelFeatures){
        // Weka expects a dataset defining the features and class names. However it does not seem to use them so I just gave it something.
        FastVector attributes = new FastVector();
        for (int i = 0; i < numelFeatures; i++){
            attributes.addElement(new Attribute("f" + String.valueOf(i)));
        }
        FastVector classAttr = new FastVector(2);
        for (int c = 0; c < 20; c++){
            classAttr.addElement(String.valueOf(c));            
        }        
        attributes.addElement(new Attribute("class", classAttr));
        int classIdx = numelFeatures;
        Instances instances = new Instances("test", attributes, classIdx);
        instances.setClassIndex(classIdx);
        return instances;
    }

    @Override
    public JPanel getOptionsPanel() {
        OptionsPropmptWeka optionsPrompt = new OptionsPropmptWeka(classifier);
        return optionsPrompt;
    }

    @Override
    public void invalidate() {
        valid = false;
    }

    @Override
    public Element getProperties(Document doc) {
        Element propertiesNode = doc.createElement("Properties");
        propertiesNode.setAttribute("Active", String.valueOf(active));
        propertiesNode.setAttribute("Valid", String.valueOf(valid));
        Element optionsNode = doc.createElement("Options");
        for (String option:classifier.getOptions()){
            Element optionNode = doc.createElement("Option");
            optionNode.setTextContent(option);
            optionsNode.appendChild(optionNode);
        }
        propertiesNode.appendChild(optionsNode);
        
        Element classNode = doc.createElement("Class");
        classNode.setTextContent(classifier.getClass().getCanonicalName());
        propertiesNode.appendChild(classNode);
        
        Element resultsNode = doc.createElement("Results");
        resultsNode.setTextContent(getResults());
        propertiesNode.appendChild(resultsNode);
        
        return propertiesNode;        
    }

    @Override
    public void setProperties(Node element) {
        for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("Properties".equalsIgnoreCase(n.getNodeName())) {
                parseProperties(n);
            }
        }
    }
    
    private void parseProperties(Node element){
        active = NamedNodeMapFunc.getBoolean(element.getAttributes(), "Active");
        valid = NamedNodeMapFunc.getBoolean(element.getAttributes(), "Valid");
        
        String className = null, options[] = null;
        Classifier classifier = null;
        for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("Options".equalsIgnoreCase(n.getNodeName())) {
                options = parseOptionsNode(n);
            }
            if ("Class".equalsIgnoreCase(n.getNodeName())) {
                className = n.getTextContent();
            }
            if ("Results".equalsIgnoreCase(n.getNodeName())) {
                try {
                    String temp = n.getTextContent();
                    if (temp != null) {
                        byte[] results = DatatypeConverter.parseBase64Binary(temp);
                        Object obj = SerializationHelper.read(new BufferedInputStream(new ByteArrayInputStream(results)));
                        if (obj instanceof Classifier){
                            classifier = (Classifier) obj;
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ClassifierWeka.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        try {
            if (classifier == null){
                this.classifier = Classifier.forName(className, options);
            } else {
                this.classifier = classifier;
            }
        } catch (Exception ex) {
            Logger.getLogger(ClassifierWeka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String[] parseOptionsNode(Node element){
        ArrayList<String> options = new ArrayList<>();
        for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("Option".equalsIgnoreCase(n.getNodeName())) {
                if (n.getTextContent() != null){
                    String option = n.getTextContent().trim();
                    if (!option.isEmpty())
                    options.add(option);
                }
            }
        }
        return options.toArray(new String[options.size()]);
    }

    void setOptions(String[] options) throws Exception {
        classifier.setOptions(options);
    }

    private String getResults() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);
            SerializationHelper.write(bos, classifier);
            bos.flush();
            return DatatypeConverter.printBase64Binary(baos.toByteArray());
        } catch (Exception ex) {
            Logger.getLogger(ClassifierWeka.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Classifier getClassifier() {
        return classifier;
    }
    
}
