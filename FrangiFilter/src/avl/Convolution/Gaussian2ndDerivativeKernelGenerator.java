/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.Convolution;

import java.awt.image.Kernel;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benbryan
 */
public class Gaussian2ndDerivativeKernelGenerator {
    public static void main(String[] args) {
        float sigmas[] = {2f,4f,6f};
        ArrayList<Convolution_Kernel> a = generateKernels(sigmas);
    }
    
    static ArrayList<Convolution_Kernel> generateKernels(float sigma[]){
        ArrayList<Convolution_Kernel> out = new ArrayList<>();
        for (int i = 0; i < sigma.length; i++){
            out.addAll(generateKernels(sigma[i]));
        }
        return out;
    }
            
    static ArrayList<Convolution_Kernel> generateKernels(float sigma){
        int kLim = (int) Math.ceil(3*sigma);
        int kDim = kLim*2+1;
        float DGaussxx[] = new float[kDim*kDim];
        float DGaussxy[] = new float[kDim*kDim];
        float DGaussyy[] = new float[kDim*kDim];
        for (int y = -kLim; y <= kLim; y++){
            for (int x = -kLim; x <= kLim; x++){
                DGaussxx[(x+kLim)+(y+kLim)*kDim] = (float) (1/(2*Math.PI*Math.pow(sigma,4)) * (Math.pow(x,2)/Math.pow(sigma,2) - 1) * Math.exp(-(Math.pow(x,2f) + Math.pow(y,2f))/(2*Math.pow(sigma,2f))));
                DGaussxy[(x+kLim)+(y+kLim)*kDim] = (float) (1/(2*Math.PI*Math.pow(sigma,6)) * (x*y)                                  * Math.exp(-(Math.pow(x,2f) + Math.pow(y,2f))/(2*Math.pow(sigma,2f))));
            }    
        }
        
        for (int y = -kLim; y <= kLim; y++){
            for (int x = -kLim; x <= kLim; x++){
                DGaussyy[(y+kLim)+(x+kLim)*kDim] = DGaussxx[(x+kLim)+(y+kLim)*kDim];
            }
        }
        ArrayList<Convolution_Kernel> out = new ArrayList<>();
        Convolution_Kernel temp;
        
        temp = new Convolution_Kernel("Gaussian2ndDerivative",  new Kernel(kDim, kDim, DGaussxx));
        temp.addProperty("Direction", "xx");
        out.add(temp);
        temp = new Convolution_Kernel("Gaussian2ndDerivative",  new Kernel(kDim, kDim, DGaussxy));
        temp.addProperty("Direction", "xy");
        out.add(temp);        
        temp = new Convolution_Kernel("Gaussian2ndDerivative",  new Kernel(kDim, kDim, DGaussyy));
        temp.addProperty("Direction", "yy");
        out.add(temp);
        
        return out;
    }
    
    static void writeFloatArrayToFile(File f, float[] data){
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(f);
            try (DataOutputStream dos = new DataOutputStream(fo)) {
                for (int x = 0; x < data.length; x++){
                    dos.writeFloat(data[x]);
                }
            }
            fo.close();
        } catch (IOException ex) {
            Logger.getLogger(Gaussian2ndDerivativeKernelGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fo.close();
            } catch (IOException ex) {
                Logger.getLogger(Gaussian2ndDerivativeKernelGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
