/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.testDouble;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benbryan
 */
public class ArrayOp {
    static void writeDoubleArrayToFile(File f, double[] data){
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(f);
            try (DataOutputStream dos = new DataOutputStream(fo)) {
                for (int x = 0; x < data.length; x++){
                    dos.writeDouble(data[x]);
                }
            }
            fo.close();
        } catch (IOException ex) {
            Logger.getLogger(ArrayOp.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fo.close();
            } catch (IOException ex) {
                Logger.getLogger(ArrayOp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }   
    
    static public BufferedImage imagescGray( int w, int h, double src[]) {       
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        byte data[] = ((DataBufferByte)dst.getRaster().getDataBuffer()).getData();
        mat2gray(src, data);
        return dst;
    }
    
    static public BufferedImage imagescGray( int w, int h, byte src[]) {   
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        byte data[] = ((DataBufferByte)dst.getRaster().getDataBuffer()).getData();
        System.arraycopy(src, 0, data, 0, data.length);
        return dst;
    }
    
    static void mat2gray(double src[], byte dest[]){ // This could be faster, but I did it quick
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        
        for (int i = 0; i < src.length; i++){
            double t = src[i];
            if (t<min){
                min = t;
            }
            if (t>max){
                max = t;
            }
        }      
        
        for (int i = 0; i < dest.length; i++){
            dest[i] = (byte) (255*((src[i]-min)/(max-min)));
        }
    }
    
    public static byte[] getColorChannel(BufferedImage inputImage, int channel){
        int sizeX = inputImage.getWidth();
        int sizeY = inputImage.getHeight();
        
        double[] samples = new double[sizeX*sizeY];
        inputImage.getData().getSamples(0, 0, sizeX, sizeY, channel, samples);
                
        byte samplesByte[] = new byte[samples.length];
        for (int i = 0; i < samplesByte.length; i++){
            samplesByte[i] = (byte) samples[i];
        }        
        return samplesByte;
    }
    
}
