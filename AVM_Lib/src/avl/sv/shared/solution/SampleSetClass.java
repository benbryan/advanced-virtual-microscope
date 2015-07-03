package avl.sv.shared.solution;

import avl.sv.shared.AVM_ProgressMonitor;
import avl.sv.shared.image.ImageManager;
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

public class SampleSetClass {
    public final String className;
    public ArrayList<Sample> samples;
    public ArrayList<String> featureNames;

    public SampleSetClass(String className, ArrayList<String> featureNames) {
        this.className = className;
        samples = new ArrayList<>();
        this.featureNames = featureNames;
    }   
          
    public static ArrayList<SampleSetClass> generateTrainingSets(ArrayList<ImageSource> imageSources, SolutionSource solutionSource, AVM_ProgressMonitor pm) throws Throwable {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Solution solution = solutionSource.getSolution();
        StudySource study = solutionSource.getStudySource();       
        Collection<String> classNames = solution.getClassifierClassNames().values();
        HashMap<String,SampleSetClass> sampleSetMap = new HashMap<>();
        
        ArrayList<String> featureNames = new ArrayList<>();
        for (AbstractFeatureGenerator featureGenerator:solutionSource.getSolution().getFeatureGenerators()){
            featureNames.addAll(Arrays.asList(featureGenerator.getFeatureNames()));
        }
        
        for (String className:classNames){
            sampleSetMap.put(className, new SampleSetClass(className, featureNames));
        }
        
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
        
        for (ImageSource imageSource : imageSources) {
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
                sampleSetMap.get(folderName).samples.addAll(samplesSet.samples);
            }
        }
        ArrayList<SampleSetClass> samplesSets = new ArrayList<>();
        Iterator<String> iter = classNames.iterator();
        while (iter.hasNext()){
            samplesSets.add(sampleSetMap.get(iter.next()));
        }
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        return samplesSets;
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
