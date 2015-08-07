package avl.sv.shared.solution;

import avl.sv.shared.AVM_ProgressMonitor;
import avl.sv.shared.image.ImageSource;
import avl.sv.shared.study.AnnotationSet;
import avl.sv.shared.model.featureGenerator.AbstractFeatureGenerator;
import avl.sv.shared.study.ROI_Folder;
import avl.sv.shared.study.StudySource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class SampleSetClass {
    public final String className;
    public ArrayList<Sample> samples;
    public ArrayList<String> featureNames;

    public SampleSetClass(String className, ArrayList<String> featureNames) {
        this.className = className;
        samples = new ArrayList<>();
        this.featureNames = featureNames;
    }   
             
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
    
    private static Instances initInstances(Collection<String> classNames, ArrayList<String> featureNames) {
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
    
    public static Instances generateInstances(ArrayList<ImageSource> imageSources, SolutionSource solutionSource, AVM_ProgressMonitor pm) throws Throwable {
//        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Solution solution = solutionSource.getSolution();
        StudySource study = solutionSource.getStudySource();       
        Collection<String> classNames = solution.getClassifierClassNames().values();
        
        ArrayList<String> featureNames = new ArrayList<>();
        for (AbstractFeatureGenerator featureGenerator:solutionSource.getSolution().getFeatureGenerators()){
            featureNames.addAll(Arrays.asList(featureGenerator.getFeatureNames()));
        }
                
        Instances instances = initInstances(classNames, featureNames);
        
        if (pm != null){
            pm.setNote("Counting samples to collect");
            int samplesToCollect = 0;
            for (ImageSource imageSource : imageSources) {       
                AnnotationSet annotationSet = study.getAnnotationSet(imageSource.imageReference);
                if (annotationSet == null){
                    continue;
                }
                for (ROI_Folder folder:annotationSet.getROI_Folders()){
                    if (!classNames.contains(folder.getName())){
                        continue;
                    }
                    if (folder.getROIs().isEmpty()){
                        continue;
                    }
                    SampleSetImage samplesSet = new SampleSetImage(imageSource, folder.getROIs(), solution, solutionSource);      
                    samplesToCollect += samplesSet.samples.size();
                }
            }
            pm.setMaximum(samplesToCollect*2);
        }
        
        for (ImageSource imageSource: imageSources) {
            if (pm != null){
                pm.setNote(imageSource.imageReference.imageSetName +"\\"+ imageSource.imageReference.imageName);
            }            
            AnnotationSet annotationSet = study.getAnnotationSet(imageSource.imageReference);
            if (annotationSet == null){
                continue;
            }
            for (ROI_Folder folder:annotationSet.getROI_Folders()){
                String folderName = folder.getName();
                if (!classNames.contains(folderName)){
                    continue;
                }
                if (folder.getROIs().isEmpty()){
                    continue;
                }
                SampleSetImage samplesSet = new SampleSetImage(imageSource, folder.getROIs(), solution, solutionSource);      
                samplesSet.generateSampleFeatures(pm);
                if (pm != null){
                    if (pm.isCanceled()){
                        return null;
                    }
                }
                
                for (Sample sample : samplesSet.samples) {
                    Instance instance = new Instance(featureNames.size() + 1);
                    for (int i = 0; i < sample.featureVector.length; i++) {
                        instance.setValue((Attribute) instances.attribute(i), sample.featureVector[i]);
                    }
                    instance.setValue((Attribute) instances.classAttribute(), folderName);
                    instances.add(instance);
                }
            }
        }
//        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        return instances;
    }

    public static int numelSamples(ArrayList<ImageSource> imageSources, SolutionSource solutionSource) {
        int count = 0; 
        Solution solution = solutionSource.getSolution();
        StudySource study = solutionSource.getStudySource();       
        Collection<String> classNames = solution.getClassifierClassNames().values();
        for (ImageSource imageSource : imageSources) {
            AnnotationSet annotationSet = study.getAnnotationSet(imageSource.imageReference);
            if (annotationSet == null){
                continue;
            }
            for (ROI_Folder folder:annotationSet.getROI_Folders()){
                if (!classNames.contains(folder.getName())){
                    continue;
                }
                SampleSetImage samplesSet = new SampleSetImage(imageSource, folder.getROIs(), solution, solutionSource);      
                count += samplesSet.samples.size();
            }
        }
        return count;
    }
   
}
