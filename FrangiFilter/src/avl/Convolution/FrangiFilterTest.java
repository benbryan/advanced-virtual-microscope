/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.Convolution;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author benbryan
 */
public class FrangiFilterTest {
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new FrangiFilterTest();
                } catch (Throwable ex) {
                    Logger.getLogger(FrangiFilterTest.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }
       
    public FrangiFilterTest() throws Throwable  {
        URL url = getClass().getResource("New ROI_20.png");
        BufferedImage inputImage = createBufferedImage(url);
        
        final JPanel mainPanel = new JPanel(new GridLayout(1,0));
        
        // Input image panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(new ImageIcon(inputImage)), BorderLayout.CENTER);
        mainPanel.add(panel);
        
        // Output image
        float sigmas[] = {2,4,6};
        ArrayList<Convolution_Kernel> kernels = new ArrayList<>();
        kernels.addAll(Gaussian2ndDerivativeKernelGenerator.generateKernels(sigmas));
        
        JOCLConvolveOp jop = new JOCLConvolveOp();
        long before = System.nanoTime();
        
        jop.filter(inputImage, kernels);
        BufferedImage outputImage = imagescGray( inputImage.getWidth(), inputImage.getHeight(), kernels.get(0).results);
        long after = System.nanoTime();
        double durationMS = (after-before)/1e6;
        String message = "JOCL: "+String.format("%.2f", durationMS)+" ms";
        System.out.println(message);
        JLabel joclTimeLabel = new JLabel(message);
        jop.finalize();
        
        // Output image panel
        panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(new ImageIcon(outputImage)), BorderLayout.CENTER);
        panel.add(joclTimeLabel, BorderLayout.NORTH);
        mainPanel.add(panel);

        // Create the main frame
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        
    }
    
    
    private static BufferedImage createBufferedImage(URL file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException ex) {
            Logger.getLogger(FrangiFilterTest.class.getName()).log(Level.SEVERE, null, ex);
        }


        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        BufferedImage result = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }
    
    static public BufferedImage imagescGray( int w, int h, float dataSrc[]) {
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        
        for (int i = 0; i < dataSrc.length; i++){
            float t = dataSrc[i];
            if (t<min){
                min = t;
            }
            if (t>max){
                max = t;
            }
        }
        
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        byte dataDst[] = ((DataBufferByte)dst.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < dataSrc.length; i++){
            dataDst[i] = (byte) (255*((dataSrc[i]-min)/(max-min)));
        }
        
        return dst;
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
