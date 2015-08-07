/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.testDouble;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benbryan
 */
public class Gaussian2ndDerivativeKernelSet {
    public final KernelDouble xx,xy,yy;
    private final double sigma;
    public Gaussian2ndDerivativeKernelSet(KernelDouble xx, KernelDouble xy, KernelDouble yy, double sigma) throws Exception {
        if (((xx.getWidth() != xy.getWidth()) || (xx.getWidth() != yy.getWidth())) || (xx.getHeight() != xy.getHeight()) || (xx.getHeight() != yy.getHeight())){
            throw new Exception("all kernels should be the same size");
        }
        this.xx = xx;
        this.xy = xy;
        this.yy = yy;
        this.sigma = sigma;
    }
    public int getWidth(){
        return xx.getWidth();
    }
    public int getHeight(){
        return xx.getHeight();
    }
    public int getXOrigin(){
        return xx.getXOrigin();
    }
    public int getYOrigin(){
        return xx.getYOrigin();
    }
    public double getSigma(){
        return sigma;
    }
    
    static Gaussian2ndDerivativeKernelSet generateKernels(double sigma){
        try {
            int kLim = (int) Math.ceil(3*sigma);
            int kDim = kLim*2+1;
            double DGaussxx[] = new double[kDim*kDim];
            double DGaussxy[] = new double[kDim*kDim];
            double DGaussyy[] = new double[kDim*kDim];
            for (int y = -kLim; y <= kLim; y++){
                for (int x = -kLim; x <= kLim; x++){
                    DGaussxx[(x+kLim)+(y+kLim)*kDim] = (double) (1/(2*Math.PI*Math.pow(sigma,4)) * (Math.pow(x,2)/Math.pow(sigma,2) - 1) * Math.exp(-(Math.pow(x,2) + Math.pow(y,2))/(2*Math.pow(sigma,2))));
                    DGaussxy[(x+kLim)+(y+kLim)*kDim] = (double) (1/(2*Math.PI*Math.pow(sigma,6)) * (x*y)                                  * Math.exp(-(Math.pow(x,2) + Math.pow(y,2))/(2*Math.pow(sigma,2))));
                }
            }
            
            for (int y = -kLim; y <= kLim; y++){
                for (int x = -kLim; x <= kLim; x++){
                    DGaussyy[(y+kLim)+(x+kLim)*kDim] = DGaussxx[(x+kLim)+(y+kLim)*kDim];
                }
            }
            //File f = new File("D:\\SkyDrive\\histo\\trunk\\matlab\\training\\nuclei\\frangi\\temp.dat");
            //writeDoubleArrayToFile(f,DGaussyy);
            
            Gaussian2ndDerivativeKernelSet out =    new Gaussian2ndDerivativeKernelSet(
                                                    new KernelDouble(kDim, kDim, DGaussxx),
                                                    new KernelDouble(kDim, kDim, DGaussxy),
                                                    new KernelDouble(kDim, kDim, DGaussyy), 
                                                    sigma);
            return out;
        } catch (Exception ex) {
            Logger.getLogger(Gaussian2ndDerivativeKernelSet.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
                
    }
    
}
