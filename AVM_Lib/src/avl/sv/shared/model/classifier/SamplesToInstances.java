package avl.sv.shared.model.classifier;

import avl.sv.shared.solution.Sample;
import avl.sv.shared.solution.SampleSetClass;
import java.util.ArrayList;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;


public class SamplesToInstances {
    
    public static Instances convert(ArrayList<String> classNames, ArrayList<String> featureNames, ArrayList<SampleSetClass> samplesSets){
        Instances instances = initInstances(classNames, featureNames);
        for (SampleSetClass samplesSet : samplesSets) {
            for (Sample sample : samplesSet.samples) {
                Instance instance = new Instance(featureNames.size() + 1);
                for (int i = 0; i < sample.featureVector.length; i++) {
                    instance.setValue((Attribute) instances.attribute(i), sample.featureVector[i]);
                }
                instance.setValue((Attribute) instances.classAttribute(), samplesSet.className);
                instances.add(instance);
            }
        }
        return instances;
    }
    
    private static Instances initInstances(ArrayList<String> classNames, ArrayList<String> featureNames) {
        FastVector attributes = new FastVector();
        for (String name : featureNames) {
            attributes.addElement(new Attribute(name));
        }
        FastVector classAttr = new FastVector(2);
        for (String className : classNames) {
            classAttr.addElement(className);
        }
        attributes.addElement(new Attribute("class", classAttr));
        int classIdx = featureNames.size();
        Instances instances = new Instances("test", attributes, classIdx);
        instances.setClassIndex(classIdx);
        return instances;
    }
}
