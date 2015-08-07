package sandbox;

import avl.sv.shared.solution.Sample;
import avl.sv.shared.study.ROI;
import avl.sv.shared.study.ROIOval;
import avl.sv.shared.study.ROIRectangle;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewJFrame extends javax.swing.JFrame {
    
//    public String importROI(File out) {
//        try {
//            if (out.getParentFile().canWrite()) {
//                BufferedImage temp = getSubImage(rec);
//                TiledImage img = new TiledImage(0, 0, temp.getWidth(), temp.getHeight(), 0, 0, temp.getSampleModel(), temp.getColorModel());
//                img.createGraphics().drawImage(temp, null, null);
//                FileOutputStream fos = new FileOutputStream(out);
//                int tileSize = 256;
//
//                TIFFEncodeParam tiffParam = new TIFFEncodeParam();
//                tiffParam.setCompression(TIFFEncodeParam.COMPRESSION_JPEG_TTN2);
//                tiffParam.setWriteTiled(true);
//                tiffParam.setTileSize(tileSize, tileSize);
//                tiffParam.setLittleEndian(true);
//
//                JPEGEncodeParam jpegParam = tiffParam.getJPEGEncodeParam();
//                jpegParam.setQuality(jpegQuality);
//
//                ROI_Folder folder;
//                if (roi.getParent() != null){
//                    folder = ((ROI_Folder)roi.getParent()).clone();
//                } else {
//                    folder  = ROI_Folder.createDefault();
//                }
//                folder.add((ROI) roi.clone());
//                Attribute boundingBoxAttribute = new Attribute();
//                boundingBoxAttribute.id = 0;
//                boundingBoxAttribute.name = "Annotation_BoundingBox";
//                boundingBoxAttribute.value
//                        = "X=" + String.valueOf(rec.x) + "|"
//                        + "Y=" + String.valueOf(rec.y) + "|"
//                        + "Width=" + String.valueOf(rec.width) + "|"
//                        + "Height=" + String.valueOf(rec.height);
//                if (folder.attributes == null) {
//                    folder.attributes = new ArrayList<>();
//                }
//                folder.attributes.add(boundingBoxAttribute);
//
//                Attribute imageSetName = new Attribute();
//                imageSetName.id = 1;
//                imageSetName.name = "ImageSetName";
//                imageSetName.value = imageReference.imageSetName;
//                folder.attributes.add(imageSetName);
//                
//                Attribute imageName = new Attribute();
//                imageName.id = 2;
//                imageName.name = "ImageName";
//                imageName.value = imageReference.imageName;
//                folder.attributes.add(imageName);
//                
//                Attribute imageID = new Attribute();
//                imageSetName.id = 3;
//                imageSetName.name = "ImageID";
//                imageSetName.value = imageReference.hashString;
//                folder.attributes.add(imageID);
//                
//                TIFFField[] extraFields = new TIFFField[1];
//                extraFields[0] = new TIFFField(TiffTag.ImageDescription.getShort(), 2, 1, new String[]{folder.toXML()});
//                tiffParam.setExtraFields(extraFields);
//
//                TIFFImageEncoder encoder = new TIFFImageEncoder(fos, tiffParam);
//                encoder.encode(img);
//                fos.close();
//                return MessageStrings.SUCCESS;
//            }
//            return "error: permission denied to write file";
//        } catch (IOException | ImageAccessException ex) {
//            return "error: Failed to get subimage from " + imageReference.imageSetName + "\"" + imageReference.imageName + " for export of ROI " + roi.getName();
//        } catch (OutOfMemoryError ex) {
//            return "error: OutOfMemoryError while attempting to get subimage from " + imageReference.imageSetName + "\"" + imageReference.imageName + " for export of ROI " + roi.getName();
//        } catch (CloneNotSupportedException ex) {
//            Logger.getLogger(ImageSource.class.getName()).log(Level.SEVERE, null, ex);
//            return "error:" + ex.getMessage();
//        }
//    }

    
    BufferedImage img = null;
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);       //To change body of generated methods, choose Tools | Templates.
        if (img != null){
            g.drawImage(img, 0, 20, getWidth(), getHeight(), null); 
        }        
    }
    
    public ArrayList<Sample> samples = new ArrayList<>();
    public boolean nonGridSampling = false;
    long imageDimX = 1000, imageDimY = 500;  

    private void mkImg(ArrayList<ROI> rois, double tileDim, double windowDim) throws Throwable{
        int tilesX = (int) Math.floor((double) imageDimX / tileDim);
        int tilesY = (int) Math.floor((double) imageDimY / tileDim);
        BufferedImage tileRep = new BufferedImage(tilesX, tilesY, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = (Graphics2D) tileRep.getGraphics();
        AffineTransform at = new AffineTransform();
        at.scale(1 / tileDim, 1 / tileDim);
        g.setTransform(at);

        for (ROI roi : rois) {
            Shape s = roi.getShape();
            g.setColor(Color.WHITE);
//            g.setClip(s);
            g.fill(s);
            g.setColor(Color.BLACK);
            g.draw(s);
        }
        setSize(tilesX*10, tilesY*10);
        img = tileRep;
        
        // First try to locate samples from a grid layout
        WritableRaster raster = tileRep.getRaster();
        for (int x = 0; x < tilesX - 1; x++ ) {
            for (int y = 0; y < tilesY - 1; y++ ) {
                byte b[] = (byte[]) raster.getDataElements(x, y, null);
                if (b[0] != 0) {
                    int offset = ((int) windowDim - (int) tileDim) / 2;
                    Rectangle tile = new Rectangle( ((int) (x / 1 * tileDim)), 
                                                    ((int) (y / 1 * tileDim)), 
                                                    (int) tileDim, 
                                                    (int) tileDim);
                    Rectangle window = new Rectangle(   ((int) (x / 1 * tileDim)) - offset, 
                                                        ((int) (y / 1 * tileDim)) - offset, 
                                                        (int) windowDim, 
                                                        (int) windowDim);
                    samples.add(new Sample(tile, window));
                }
            }
        }
        System.out.println(String.valueOf(samples.size()));
        
//         If not that many samples were found, try shifting the sample locations off the grid
        if (true){
            return;
        }
        
        if (samples.size() < 10) {
            nonGridSampling = true;
            for (ROI roi : rois) {
                sampleOffGrid(roi, tileDim, windowDim);
            }
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
        double upSampleFactor = 4;
        Shape shape = roi.getShape();
        Rectangle bounds = shape.getBounds();
        int tilesX = (int) (Math.floor((double) bounds.width  / tileDim) * upSampleFactor);
        int tilesY = (int) (Math.floor((double) bounds.height / tileDim) * upSampleFactor);
        BufferedImage tileRep = new BufferedImage(tilesX, tilesY, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = (Graphics2D) tileRep.getGraphics();
        AffineTransform at = new AffineTransform();
        at.scale(upSampleFactor / tileDim, upSampleFactor / tileDim);
        at.translate(-bounds.x, -bounds.y);
        g.setTransform(at);
        g.setColor(Color.white);
        
        g.setClip(shape);
        g.fill(shape);
        
        img = tileRep;

        for (int x = 0; x < tilesX - 1 - upSampleFactor; x++) {
            for (int y = 0; y < tilesY - 1 - upSampleFactor; y++) {
                byte b[] = (byte[]) tileRep.getRaster().getDataElements(x, y, (int)upSampleFactor, (int)upSampleFactor, null);
                boolean allSet = true;
                for (int k = 0; k < b.length; k++) {
                    allSet &= b[k] != 0;
                }
                if (allSet) {
                    int offset = ((int) windowDim - (int) tileDim) / 2;
                    Rectangle tile = new Rectangle( bounds.x + ((int) (x / upSampleFactor * tileDim)), 
                                                    bounds.y + ((int) (y / upSampleFactor * tileDim)), 
                                                    (int) tileDim, 
                                                    (int) tileDim);
                    Rectangle window = new Rectangle(  bounds.x + ((int) (x / upSampleFactor * tileDim)) - offset, 
                                                       bounds.y + ((int) (y / upSampleFactor * tileDim)) - offset, 
                                                        (int) windowDim, 
                                                        (int) windowDim);
                    samples.add(new Sample(tile, window));
                }
            }
        }
    }

    public NewJFrame() throws Throwable {
        initComponents();     
        Executors.newSingleThreadExecutor().submit(()->{
            double tileDim = 10, windowDim = 10;

            for (int i = 0; i < 1; i++){
                ArrayList<ROI> rois = new ArrayList<>();
                ROIRectangle rect = ROIRectangle.getDefault();
                rect.setRectangle(new Rectangle(100+i, 100, 18, 8));
                rois.add(rect);
                try {
                    //                ROIOval oval = ROIOval.getDefault();
//                oval.setOval(110+i, 110, 11, 11);
//                rois.add(oval);
                    mkImg(rois, tileDim, windowDim);
                } catch (Throwable ex) {
                    Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                repaint();
            }
            System.exit(0);
        });
    }
    
//    private void testFeatureGenerator() throws Throwable{
//        AbstractFeatureGenerator abstractFeatureGenerator = new FeatureGeneratorJOCLAdapter();
//        System.out.println("");
//        for (String s:abstractFeatureGenerator.getFeatureNames()){
//            System.out.print(s + ", ");
//        }
//        System.out.println("");
//  
//        while (true){
//            int dim = (int) (10+Math.random()*200);
//            int numelImages = (int) (1+Math.random()*200);
//            System.out.println("dim = "+String.valueOf(dim)+", numelImages = " + String.valueOf(numelImages));
//            BufferedImage images[] = new BufferedImage[numelImages];
//            for (int i = 0; i < images.length; i++){
//                images[i] =  new BufferedImage(dim, dim, BufferedImage.TYPE_3BYTE_BGR);
//            }      
//            double[][] features = abstractFeatureGenerator.getFeaturesForImages(images);
//            System.out.println(features != null);
//        }
//        
//    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new NewJFrame().setVisible(true);
            } catch (Throwable ex) {
                Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


}
