package avl.sv.shared.model.classifier;

import avl.sv.shared.NamedNodeMapFunc;
import avl.sv.shared.solution.Sample;
import avl.sv.shared.solution.SampleSetClass;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ClassifierLDA implements ClassifierInterface {

    LDA lda = null;
    
    public boolean isActive = true;
    public String message;
    public Date lastTrained;

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean isActive) {
        this.isActive = isActive;
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
    public void train(ArrayList<SampleSetClass> samplesSets) {
        try {
            //Collect all samples and labels 
            double features[][] = new double[numelSamples(samplesSets)][];
            int label[] = new int[numelSamples(samplesSets)];
            int sampleIdx = 0;
            for (int setIdx = 0; setIdx < samplesSets.size(); setIdx++) {
                SampleSetClass sampleSet = samplesSets.get(setIdx);
                for (Sample sample : sampleSet.samples) {
                    features[sampleIdx] = sample.featureVector;
                    label[sampleIdx] = setIdx;
                    sampleIdx++;
                }
            }
            
            lda = new LDA(features, label, true);
                       
            lastTrained = new Date();
            message = "LDA classifier generated";
        } catch (Exception ex) {
            message = "Failed to train LDA";
        }
    }

    private int numelSamples(ArrayList<SampleSetClass> samplesSets){
        int count = 0;
        for (SampleSetClass sampleSet:samplesSets){
            count += sampleSet.samples.size();
        }
        return count;
    }

    @Override
    public boolean isValid() {
        return lda!=null;
    }

    @Override
    public void predict(ArrayList<Sample> samples) {
        for (Sample sample:samples){
            sample.classifierLabel = lda.predict(sample.featureVector);
        }            
    }

    @Override
    public void invalidate() {
        lda = null;
    }

    @Override
    public Element getProperties(Document doc) {
        Element propertiesNode = doc.createElement("Properties");
        propertiesNode.setAttribute("Active", String.valueOf(isActive));
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

    private void parseProperties(Node element) {
        NamedNodeMap attributes = element.getAttributes();
        isActive = NamedNodeMapFunc.getBoolean(attributes, "Active");
        for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
        }
    }

    private class OptionsPrompt extends JPanel implements ClassifierOptionsPromptInterface{

        public OptionsPrompt() {
            JLabel label = new JLabel("No Options");
            setAlignmentX(TOP_ALIGNMENT);
            setAlignmentY(LEFT_ALIGNMENT);
            add(label);
            label.setVisible(true);
        }
        
        @Override
        public ClassifierInterface getClassifier() {
            return new ClassifierLDA();
        }
        @Override
        public void setNumelFeatures(int numel) {
        }
    }
    @Override
    public JPanel getOptionsPanel() {
        return new OptionsPrompt();
    }

    @Override
    public String getName() {
        return "LDA";
    }
}
