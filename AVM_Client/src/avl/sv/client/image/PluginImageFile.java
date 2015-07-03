package avl.sv.client.image;

import avl.sv.client.AVM_Plugin;
import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.shared.ProgressBarForegroundPainter;
import avl.sv.shared.AVM_ProgressMonitor;
import avl.sv.shared.image.ImageSource;
import avl.sv.shared.image.ImageSourceFile;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

public class PluginImageFile implements AVM_Plugin{
    final JMenu jMenuImage = new JMenu("Image");
    ArrayList<Window> windows = new ArrayList<>();
    public PluginImageFile() {       
        // TODO: put the line below somewhere else
        jMenuImage.add(new JMenuItem(new AbstractAction("Open ImageFile") {
            JFileChooser jFileChooserLocalImage;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (jFileChooserLocalImage == null) {
                    jFileChooserLocalImage = new JFileChooser();
                    jFileChooserLocalImage.setFileFilter(new ImageFilter());
                }
                try {
                    int result = jFileChooserLocalImage.showDialog(AdvancedVirtualMicroscope.getInstance(), "Open");
                    if (result == JFileChooser.CANCEL_OPTION) {
                        return;
                    }
                    File f = jFileChooserLocalImage.getSelectedFile();
                    if (f == null) {
                        return;
                    }

                    ImageSource imageSource = new ImageSourceFile(f);
                    if (imageSource.isTiffDirectoryBuffersEmpty()) {
                        File svsFile = convertToSVS_StyleTiff(f, 0.95f);
                        if (svsFile == null) {
                            return;
                        }
                        imageSource = new ImageSourceFile(svsFile);
                        svsFile.deleteOnExit();
                    }

                    AdvancedVirtualMicroscope.addImageViewer(imageSource);
                } catch (IOException ex) {
                    Logger.getLogger(AdvancedVirtualMicroscope.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }));
    }    
    
    private File convertToSVS_StyleTiff(File originalImg, float jpegQuality) {
        FileOutputStream fos;
        File tempImg;
        UIManager.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarForegroundPainter());
        AVM_ProgressMonitor pm = new AVM_ProgressMonitor(null, "Converting image", "", 0, 100);
        try {
            tempImg = new File("./" + originalImg.getName() + String.valueOf(Math.random()) + ".tif");
            tempImg.createNewFile();
            fos = new FileOutputStream(tempImg);
            int tileSize = 256;
            pm.setNote("Reading original image");
            pm.setProgress(5);
            BufferedImage originalImage = ImageIO.read(originalImg);
            if (originalImage == null) {
                return null;
            }
            TiffWriter.write(pm, originalImage, fos, tileSize, jpegQuality);
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(ImageUploaderFrame.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        pm.close();
        return tempImg;
    }

    @Override
    public JMenu getMenu() {
        return jMenuImage;
    }
    
}
