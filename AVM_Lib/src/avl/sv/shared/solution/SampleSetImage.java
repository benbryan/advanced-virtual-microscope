package avl.sv.shared.solution;

import avl.sv.shared.image.ImageAccessException;
import avl.sv.shared.AVM_ProgressMonitor;
import avl.sv.shared.study.ROI;
import avl.sv.shared.image.ImageSource;
import avl.sv.shared.model.featureGenerator.AbstractFeatureGenerator;
import avl.sv.shared.model.featureGenerator.jocl.JOCL_Configure;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

public class SampleSetImage {

    public ArrayList<Sample> samples;
    public boolean nonGridSampling = false;
    final ImageSource imageSource;
    final private Solution solution;
    private boolean isForTest = false;
    final SolutionSource solutionSource;
    ExecutorService pool = Executors.newFixedThreadPool(20);
    private HashMap<String, HashMap<String, double[][]>> knownGeneratorsAndFeatures = new HashMap<>();

    public SampleSetImage(ImageSource imageSource, ArrayList<ROI> rois, Solution solution, SolutionSource solutionSource) {
        this.imageSource = imageSource;
        this.solution = solution;
        this.solutionSource = solutionSource;
        double tileDim = solution.getTileDim();
        double windowDim = solution.getTileWindowDim();
        findSamples(rois, tileDim, windowDim);
    }
    
    private void findSamples(ArrayList<ROI> rois, double tileDim, double windowDim){
        samples = new ArrayList<>();
        int tilesX = (int) Math.floor((double) imageSource.getImageDimX() / tileDim);
        int tilesY = (int) Math.floor((double) imageSource.getImageDimY() / tileDim);
        long upperMemLimit = Math.min(50000000, Runtime.getRuntime().freeMemory()/2);
        if (((long)tilesX)*((long)tilesY) < upperMemLimit){
            BufferedImage tileRep = new BufferedImage(tilesX, tilesY, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = (Graphics2D) tileRep.getGraphics();
            AffineTransform at = new AffineTransform();
            at.scale(1 / tileDim, 1 / tileDim);
            at.translate(-tileDim/2, -tileDim/2);
            g.setTransform(at);
            for (ROI roi : rois) {
                Shape s = roi.getShape();
                g.setColor(Color.WHITE);
                g.fill(s);
                g.setColor(Color.BLACK);
                g.draw(s);
            }

            // First try to locate samples from a grid layout
            WritableRaster raster = tileRep.getRaster();
            for (int x = 0; x < tilesX - 1; x++ ) {
                for (int y = 0; y < tilesY - 1; y++ ) {
                    byte b[] = (byte[]) raster.getDataElements(x, y, null);
                    if (b[0] != 0) {
                        int offset = ((int) windowDim - (int) tileDim) / 2;
                        Rectangle tile = new Rectangle( ((int) (x * tileDim)), 
                                                        ((int) (y * tileDim)), 
                                                        (int) tileDim, 
                                                        (int) tileDim);
                        Rectangle window = new Rectangle(   ((int) (x * tileDim)) - offset, 
                                                            ((int) (y * tileDim)) - offset, 
                                                            (int) windowDim, 
                                                            (int) windowDim);
                        samples.add(new Sample(tile, window));
                    }
                }
            }            
        }

        // If not that many samples were found, try shifting the sample locations off the grid
        if (samples.size() < 10) {
            nonGridSampling = true;
            for (ROI roi : rois) {
                sampleOffGrid(roi, tileDim, windowDim);
            }
            removeCloseSamples(tileDim);
        }
    }
    
    private void removeCloseSamples(double tileDim){
        if (samples.size() > 10) {
            ArrayList<Sample> toCheck = new ArrayList<>();
            toCheck.addAll(samples);
            while (true) {
                if (toCheck.isEmpty()){
                    break;
                }
                Sample s1 = toCheck.remove((int) ((toCheck.size() - 1) * Math.random()));
                ArrayList<Sample> toRemove = new ArrayList<>();
                for (Sample s2:samples){
                    if (s1.equals(s2)){
                        continue;
                    }
                    double dist = Math.sqrt(Math.pow(s1.tile.x-s2.tile.x,2) + Math.pow(s1.tile.y-s2.tile.y,2));
                    if (dist < tileDim){
                        toRemove.add(s2);
                    } else {
//                        System.out.println();
                    }
                }
                toCheck.removeAll(toRemove);
                samples.removeAll(toRemove);
            }
        }
    }
    
    private void sampleOffGrid(ROI roi, double tileDim, double windowDim){       
        int upSampleFactor = 16;
        Shape shape = roi.getShape();
        Rectangle bounds = shape.getBounds();
        int tilesX = (int) Math.floor((double) bounds.width  / tileDim) * upSampleFactor;
        int tilesY = (int) Math.floor((double) bounds.height / tileDim) * upSampleFactor;

        BufferedImage tileRep = new BufferedImage(tilesX, tilesY, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = (Graphics2D) tileRep.getGraphics();
        AffineTransform at = new AffineTransform();
        at.scale(upSampleFactor / tileDim, upSampleFactor / tileDim);
        at.translate(-bounds.x, -bounds.y);
        g.setTransform(at);
        g.setColor(Color.white);
        g.fill(shape);
        g.setStroke(new BasicStroke(3));
        g.setColor(Color.BLACK);
        g.draw(shape);
        
        for (int x = 0; x <= tilesX - upSampleFactor; x+=1) {
            for (int y = 0; y <= tilesY - upSampleFactor; y+=1) {
                byte b[] = (byte[]) tileRep.getRaster().getDataElements(x, y, upSampleFactor, upSampleFactor, null);
                boolean allSet = true;
                for (int k = 0; k < b.length; k++) {
                    allSet &= b[k] != 0;
                }
                if (allSet) {
                    int offset = ((int) windowDim - (int) tileDim) / 2;
                    Rectangle tile = new Rectangle( bounds.x + ((int) ((double)x / (double)upSampleFactor * (double)tileDim)), 
                                                    bounds.y + ((int) ((double)y / (double)upSampleFactor * (double)tileDim)), 
                                                    (int) tileDim, 
                                                    (int) tileDim);
                    Rectangle window = new Rectangle(  bounds.x + ((int) ((double)x / (float)upSampleFactor * (double)tileDim)) - offset, 
                                                       bounds.y + ((int) ((double)y / (float)upSampleFactor * (double)tileDim)) - offset, 
                                                        (int) windowDim, 
                                                        (int) windowDim);
                    samples.add(new Sample(tile, window));
                }
            }
        }
    }

    public void setIsForTest(boolean isForTest) {
        this.isForTest = isForTest;
    }

    public void prefetchFeatures(AVM_ProgressMonitor pm) {
        knownGeneratorsAndFeatures = new HashMap<>();
        double tileDim = solution.getTileDim();
        double windowDim = solution.getTileWindowDim();
        double totalNumberOfFeatures = solution.getNumelFeatures();
        if (solutionSource != null) {
            for (AbstractFeatureGenerator featureGenerator : solution.getFeatureGenerators()) {
                if (!featureGenerator.isactive) {
                    continue;
                }
                String featureGeneratorClassName = featureGenerator.getClass().getCanonicalName();
                HashMap<String, double[][]> featureMap = new HashMap<>();
                for (String featureName : featureGenerator.getFeatureNames()) {
                    try {
                        Properties tempFeature = solutionSource.getFeatures(imageSource.imageReference, (int) tileDim, (int) windowDim, featureGeneratorClassName, new String[]{featureName});
                        if (tempFeature.isEmpty()){
                            continue;
                        }
                        String d1 = (String) tempFeature.getProperty(featureName);
                        if (d1 == null){
                            continue;
                        }
                        byte d2[] = DatatypeConverter.parseBase64Binary(d1);
                        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(d2));
                        int width = (int) Math.ceil((double) imageSource.getImageDimX() / tileDim);
                        int height = (int) Math.ceil((double) imageSource.getImageDimY() / tileDim);
                        double feature[][] = new double[width][height];
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                feature[x][y] = dis.readDouble();
                            }
                        }
                        if (dis.available() > 0){
                            continue;
                        }
                        featureMap.put(featureName, feature);
                        double approxNumelSamples = (double)pm.getMaximum()/1.5;
                        pm.setProgress((int) (approxNumelSamples/totalNumberOfFeatures*featureMap.size()));
                    } catch (Exception ex) {
                        Logger.getLogger(SampleSetImage.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                knownGeneratorsAndFeatures.put(featureGeneratorClassName, featureMap);
                if (!isForTest){
                    String result = solutionSource.generateInDatabase(imageSource.imageReference, (int) tileDim, (int) windowDim, featureGeneratorClassName, featureGenerator.getFeatureNames());
                }
            }
        }
    }

    public void generateSampleFeatures(AVM_ProgressMonitor pm) throws Throwable {
        if ((samples == null) || samples.isEmpty()) {
            return;
        }
        int tileDim = solution.getTileDim();
        int windowDim = solution.getTileWindowDim();
        if ((pm != null) && pm.isCanceled()) {
            return;
        }
        if (!nonGridSampling) {
            prefetchFeatures(pm);
        }
        // check if all features already exist
        boolean allFeaturesCollected = true;
        CheckingKnownFeatures:
        for (AbstractFeatureGenerator featureGenerator : solution.getFeatureGenerators()) {
            if (!featureGenerator.isactive) {
                continue;
            }
            HashMap<String, double[][]> knownGeneratorsAndFeature = knownGeneratorsAndFeatures.get(featureGenerator.getClass().getCanonicalName());
            if (knownGeneratorsAndFeature != null) {
                for (String featureName : featureGenerator.getFeatureNames()) {
                    if (!knownGeneratorsAndFeature.containsKey(featureName)) {
                        allFeaturesCollected = false;
                        break CheckingKnownFeatures;
                    }
                }
            } else {
                allFeaturesCollected = false;
                break CheckingKnownFeatures;
            }
        }

        if (allFeaturesCollected) {
            //Collect all features across the whole image 
            int numelFeatures = solution.getNumelFeatures();
            int idx = 0;
            double allFeatures[][][] = new double[solution.getNumelFeatures()][][];
            for (AbstractFeatureGenerator featureGenerator : solution.getFeatureGenerators()) {
                if (!featureGenerator.isactive) {
                    continue;
                }
                HashMap<String, double[][]> knownGeneratorsAndFeature = knownGeneratorsAndFeatures.get(featureGenerator.getClass().getCanonicalName());
                for (String featureName : featureGenerator.getFeatureNames()) {
                    allFeatures[idx++] = knownGeneratorsAndFeature.get(featureName);
                }
            }

            for (Sample sample : samples) {
                int x = sample.tile.x / tileDim;
                int y = sample.tile.y / tileDim;
                double f[] = new double[numelFeatures];
                pm.incProgress();
                for (int i = 0; i < numelFeatures; i++) {
                    f[i] = allFeatures[i][x][y];
                }
                sample.featureVector = f;
            }
            return;
        }
        if (pm != null){
            pm.setProgress(3);
        }
        //TODO: maybe optimize this to use those features that are already known?
        
        long cpuFreeMem = Runtime.getRuntime().freeMemory();
        long joclmem = JOCL_Configure.getMemoryTotal();
        final long memoryToUseInBytes = Math.min(joclmem, cpuFreeMem)/4;
        
        int bytesPerSample = solution.getNumelFeatures()*8+(windowDim*windowDim*4*2);
        int tilesInChunk = (int) ((memoryToUseInBytes)/bytesPerSample);
        int numelChunks = (int) Math.ceil((double) samples.size() / tilesInChunk);
        ArrayList<ChunkFetcher> chunkFetchers = new ArrayList<>();
        for (int i = 0; i < numelChunks; i++) {
            List<Sample> subSamples = samples.subList(i * tilesInChunk, Math.min(samples.size(), (i + 1) * tilesInChunk));
            chunkFetchers.add(new ChunkFetcher(subSamples, pm));
        }
        ArrayList<Future<List<Sample>>> chunkFetcherFuture = new ArrayList<>();
        chunkFetcherFuture.add(pool.submit(chunkFetchers.get(0)));
        for (int i = 0; i < chunkFetchers.size(); i++) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            List<Sample> subSamples;
            try {
                System.out.println("Working on chunk " + String.valueOf(i + 1) + " of " + chunkFetchers.size());
                subSamples = chunkFetcherFuture.get(i).get(5, TimeUnit.MINUTES);
                for (Sample sample : subSamples){
                    if (sample.img == null){
                        System.out.println("Null image");
                    }
                }
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new ImageAccessException("Failed to load images samples");
            }        
            if (pm != null) {
                if (pm.isCanceled()) {
                    return;
                }
                pm.setNote("Generating Features");
            }
            if (subSamples == null){
                throw new ImageAccessException("Failed to load images samples");
            }
            if ((i+1) < chunkFetchers.size()){
                //imageSource.clearBuffers();
                chunkFetcherFuture.add(pool.submit(chunkFetchers.get(i+1)));
            }
            double[][] features = getFeaturesForSamples(subSamples);
            for (int s = 0; s < subSamples.size(); s++) {
                subSamples.get(s).featureVector = features[s];
            }

            //TODO; optimise the use of below
            if (pm != null) {
                pm.setNote("Freeing sample images");
            }
            for (Sample sample : subSamples) {
                sample.img = null;
            }
            pool.submit(() -> { System.gc();});
        }
        imageSource.clearBuffers();
    }
    
    private class ChunkFetcher implements Callable<List<Sample>>{
        final List<Sample> subSamples;
        final AVM_ProgressMonitor pm;

        public ChunkFetcher(List<Sample> subSamples, AVM_ProgressMonitor pm) {
            this.subSamples = subSamples;
            this.pm = pm;
        }

        @Override
        public List<Sample> call() {
            if (pm != null) {
                pm.setNote("Loading sample images");
            }
            System.out.println("Number of samples = " + String.valueOf(subSamples.size()));

            CompletionService<Integer> completionService = new ExecutorCompletionService<>(pool);
            for (final Sample sample : subSamples) {
                completionService.submit(() -> {
                    sample.img = imageSource.getSubImage(sample.window);
                    if (sample.img == null) {
                        throw new ImageAccessException("Could not collect a sample image");
                    }
                    return 0;
                });
            }

            int numelToDo = subSamples.size();
            while (numelToDo > 0) {
                try {
                    Future<Integer> resultFuture = completionService.poll(10, TimeUnit.SECONDS); //blocks if none available
                    if (resultFuture == null){
                        continue;
                    }
                    numelToDo--;
                    if (pm != null) {
                        pm.incProgress();
                        if (pm.isCanceled()) {
                            return null;
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(SampleSetImage.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }
            return subSamples;
        }
    }

    public double[][] getFeaturesForSamples(List<Sample> samples) throws Throwable {
        double features[][] = new double[samples.size()][solution.getNumelFeatures()];
        BufferedImage imgs[] = new BufferedImage[samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            imgs[i] = samples.get(i).img;
        }
        int fIdx = 0;
        for (AbstractFeatureGenerator featureGenerator : solution.getFeatureGenerators()) {
            if (!featureGenerator.isactive) {
                continue;
            }
            double temp[][] = featureGenerator.getFeaturesForImages(imgs);
            if (temp == null){
                return null;
            }
            if (temp.length != features.length) {
                throw new ArrayStoreException("Failure genererating features for image tiles");
            }
            for (int i = 0; i < features.length; i++) {
                System.arraycopy(temp[i], 0, features[i], fIdx, featureGenerator.getNumberOfFeatures());
            }
            fIdx += featureGenerator.getNumberOfFeatures();
        }
        return features;
    }

}
