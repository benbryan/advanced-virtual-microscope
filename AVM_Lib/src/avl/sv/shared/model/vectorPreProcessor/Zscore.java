/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.sv.shared.model.vectorPreProcessor;

import avl.sv.shared.solution.Sample;
import avl.sv.shared.solution.SampleSetClass;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author benbryan
 */
public class Zscore implements Serializable {

    public double mean[], std[];

    public Zscore() {
    }

    public Zscore(double[] mean, double[] std) {
        this.mean = mean.clone();
        this.std = std.clone();
    }
    
    public Zscore(ArrayList<SampleSetClass> samplesSets) {
        int numelFeatures = samplesSets.get(0).samples.get(0).featureVector.length;
        
        // Calculate mean
        double sum[] = new double[numelFeatures];
        for(int i = 0; i < numelFeatures; i++){
            sum[i] = 0;
        }
        int numelSamples = 0;
        for (SampleSetClass sampleSet:samplesSets){
            for (Sample sample:sampleSet.samples){
                double[] f = sample.featureVector;
                for(int i = 0; i < numelFeatures; i++){
                    if (f[i] == f[i]){
                        sum[i] += f[i];
                    } else {
                        f[i] = 0;
                    }
                    numelSamples++;
                }
            }
        }
        mean = new double[numelFeatures];
        for(int i = 0; i < numelFeatures; i++){
            mean[i] = sum[i]/numelSamples;
        }
        
        // Calculate std
        for (SampleSetClass sampleSet:samplesSets){
            for (Sample sample:sampleSet.samples){
                double[] f = sample.featureVector;
                for(int i = 0; i < numelFeatures; i++){
                    double temp = f[i]-mean[i];
                    sum[i] += temp*temp;
                }
            }
        }
        std = new double[numelFeatures];
        for(int i = 0; i < numelFeatures; i++){
            std[i] = Math.sqrt(sum[i]/numelSamples);
        }
    }
       
    public void apply(ArrayList<Sample> samples){
        int numelFeatures = mean.length;
        for (Sample sample:samples){
            double[] f = sample.featureVector;
            for(int i = 0; i < numelFeatures; i++){
                f[i] = (f[i]-mean[i])/std[i];
            }
        }
    }
    
}
