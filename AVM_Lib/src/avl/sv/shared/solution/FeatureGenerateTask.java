package avl.sv.shared.solution;

import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageSourceKVStore;
import avl.sv.shared.image.ImageManagerKVStore;
import avl.sv.shared.model.featureGenerator.AbstractFeatureGenerator;
import avl.sv.shared.study.ROI;
import avl.sv.shared.study.ROIRectangle;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeatureGenerateTask implements Runnable, Serializable {

    public final ArrayList<String> featureNames;
    public final int tileDim, tileWindowDim;
    public final String featureGeneratorClassName;
    public final ImageReference imageReference;

    public FeatureGenerateTask(ArrayList<String> featureNames, int tileDim, int tileWindowDim, String featureGeneratorName, ImageReference imageReference) {
        this.featureNames = featureNames;
        this.tileDim = tileDim;
        this.tileWindowDim = tileWindowDim;
        this.featureGeneratorClassName = featureGeneratorName;
        this.imageReference = imageReference; 
    }

    public static FeatureGenerateTask parse(String xml) {
        Properties p = new Properties();
        try {
            p.loadFromXML(new ByteArrayInputStream(xml.getBytes()));
            int tileDim = Integer.parseInt(p.getProperty("tileDim"));
            int tileWindowDim = Integer.parseInt(p.getProperty("tileWindowDim"));
            String featureGeneratorClassName = p.getProperty("featureGeneratorClassName");
            ImageReference imageReference = new ImageReference(p.getProperty("ImageManager"));
            ArrayList<String> featureNames = new ArrayList<>();
            for (String name : p.getProperty("featureNames").split(",")) {
                featureNames.add(name);
            }
            FeatureGenerateTask task = new FeatureGenerateTask(featureNames, tileDim, tileWindowDim, featureGeneratorClassName, imageReference);
            return task;
        } catch (IOException ex) {
            Logger.getLogger(FeatureGenerateTask.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String toXML(){
        Properties p = new Properties();
        p.setProperty("tileDim", String.valueOf(tileDim));
        p.setProperty("tileWindowDim", String.valueOf(tileWindowDim));
        p.setProperty("featureGeneratorClassName", featureGeneratorClassName);
        p.setProperty("ImageManager", imageReference.toXML());
        StringBuilder sb = new StringBuilder();
        for (String name:featureNames){
            sb.append(name).append(",");
        }
        p.setProperty("featureNames", sb.toString());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            p.storeToXML(bos, featureGeneratorClassName);
        } catch (IOException ex) {
            Logger.getLogger(FeatureGenerateTask.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return bos.toString();
    }

    @Override
    public void run() {
        try {
            if (featureNames.size() == 0){
                return;
            }
            ImageSourceKVStore imageSource = new ImageSourceKVStore(imageReference);

            // Create the desired feature generator
            AbstractFeatureGenerator featureGenerator = (AbstractFeatureGenerator) getClass().getClassLoader().loadClass(featureGeneratorClassName).newInstance();
            featureGenerator.setFeatureNames(featureNames.toArray(new String[featureNames.size()]));

            // Create a temporary solution
            Solution solution = new Solution("dummy");
            solution.setTileDim(tileDim);
            solution.setTileWindowDim(tileWindowDim);
            solution.setFeatureGenerator(featureGenerator);

            // Define samples across the entire image
            ArrayList<ROI> samplesFullImage = new ArrayList<>();
            int samplesDimX = (int) Math.ceil((double) imageSource.getImageDimX() / (double) tileDim);
            int samplesDimY = (int) Math.ceil((double) imageSource.getImageDimY() / (double) tileDim);
            for (int x = 0; x < samplesDimX; x++) {
                for (int y = 0; y < samplesDimY; y++) {
                    ROIRectangle rect = ROIRectangle.getDefault();
                    int offset = (tileWindowDim-tileDim)/2;
                    rect.setRectangle(new Rectangle((x*tileDim)-offset, (y*tileDim)-offset, tileWindowDim, tileWindowDim));
                    samplesFullImage.add(rect);
                }
            }

            SampleSetImage sampleSetImage = new SampleSetImage(imageSource, samplesFullImage, solution, null);
            sampleSetImage.generateSampleFeatures(null);
            ArrayList<Sample> samples = sampleSetImage.samples;
            int numelFeatures = samples.get(0).featureVector.length;
            double samplesArray[][][] = new double[numelFeatures][samplesDimX][samplesDimY];
            samples.stream().forEach((sample) -> {
                int x = sample.tile.x / tileDim;
                int y = sample.tile.y / tileDim;
                for (int f = 0; f < numelFeatures; f++) {
                    samplesArray[f][x][y] = sample.featureVector[f];
                }
            });
            for (int f = 0; f < numelFeatures; f++) {
                SolutionSourceKVStore.setFeature(imageReference, featureGeneratorClassName, featureNames.get(f), tileDim, tileWindowDim, samplesArray[f]);
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (Throwable ex) {
            Logger.getLogger(FeatureGenerateTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
