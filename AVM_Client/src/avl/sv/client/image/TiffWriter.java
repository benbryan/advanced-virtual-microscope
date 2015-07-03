package avl.sv.client.image;

import avl.sv.shared.AVM_ProgressMonitor;
import avl.tiff.TiffTag;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codec.TIFFField;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.TiledImage;

public class TiffWriter {
    public static void write(AVM_ProgressMonitor pm, BufferedImage originalImage, OutputStream os, double tileSize, float jpegQuality){
        try {
            ArrayList<Object[]> layers = collectSrcLayers(pm, originalImage, tileSize, jpegQuality);
            Iterator<Object[]> it = layers.iterator();
            Object[] temp = it.next();
            RenderedImage firstImage = (RenderedImage) temp[0];
            TIFFEncodeParam param = (TIFFEncodeParam) temp[1];
            param.setExtraImages(it);
            TIFFImageEncoder encoder = new TIFFImageEncoder(os, param);
            encoder.encode(firstImage);
            pm.setNote("Writing temporary image to disk");
        } catch (IOException ex) {
            Logger.getLogger(TiffWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
        
    private static TIFFEncodeParam getParam(RenderedImage originalImage, double tileSize, float jpegQuality){
        TIFFEncodeParam tiffParam = new TIFFEncodeParam();
        tiffParam.setCompression(TIFFEncodeParam.COMPRESSION_JPEG_TTN2);
        tiffParam.setWriteTiled(true);
        tiffParam.setTileSize((int) tileSize, (int) tileSize);
        tiffParam.setLittleEndian(true);
        //tiffParam.setJPEGCompressRGBToYCbCr(false);
        
        JPEGEncodeParam jpegParam = tiffParam.getJPEGEncodeParam();
        jpegParam.setQuality(jpegQuality);

        TIFFField[] extraFields = new TIFFField[1];
        extraFields[0] = new TIFFField(TiffTag.ImageDescription.getShort(), 2, 1, new String[]{String.valueOf(originalImage.getWidth()) + "x" + String.valueOf(originalImage.getHeight())});
        tiffParam.setExtraFields(extraFields);

        return tiffParam;
    }

    private static ArrayList<Object[]> collectSrcLayers(AVM_ProgressMonitor pm, BufferedImage originalImage, double tileSize, float jpegQuality) {
        ArrayList<Object[]> layers = new ArrayList<>();
        AffineTransform at = new AffineTransform();
        
        int numelLayers = 0;
        boolean firstPass = true;
        for (double x = 1; x > 0.01; x/=4 ){
            int width = (int) (originalImage.getWidth()*x);
            int height = (int) (originalImage.getHeight()*x);
            if ((width < 1000) && (height < 1000) && (!firstPass)){
                break;
            }
            numelLayers++;
            firstPass = false;
        }
        int currentLayer = 0;
        firstPass = true;
        for (double x = 1; x > 0.01; x/=4 ){
            pm.setNote("Generating layer " + String.valueOf(currentLayer+1) + " of " + String.valueOf(numelLayers));
            pm.setProgress((int) (15+(float)75*(float)currentLayer/(float)numelLayers));

            int width = (int) (originalImage.getWidth()*x);
            int height = (int) (originalImage.getHeight()*x);
           
            if ((width < 1000) && (height < 1000) && (!firstPass)){
                break;
            }
            
            TiledImage tempImg = new TiledImage(0, 0, width, height, 0, 0, originalImage.getSampleModel(), originalImage.getColorModel());
            Graphics2D g = (Graphics2D) tempImg.createGraphics();
            at.setToScale(x, x);
            //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawRenderedImage(originalImage, at);
            
//            try {
//                BufferedImage bImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
//                AffineTransform at1 = new AffineTransform();
//                bImg.createGraphics().drawImage(originalImage, at1, null);
//                ImageIO.write(bImg, "jpeg", new FileOutputStream("D:\\default.tif"));
//            } catch (IOException ex) {
//                Logger.getLogger(TiffWriter.class.getName()).log(Level.SEVERE, null, ex);
//            }
            
            layers.add(new Object[]{tempImg, getParam(originalImage, tileSize, jpegQuality)});
            firstPass = false;
        }

        return layers;
    }
}
