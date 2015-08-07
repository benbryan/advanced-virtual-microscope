/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.testDouble;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author benbryan
 */
public class SandboxFrame extends javax.swing.JFrame {

    private BufferedImage inputImage, outputImage;
    JOCL_FrangiFilter jop;
    OtsuThresholder ot;

    public SandboxFrame() {
        jop = new JOCL_FrangiFilter();
        ot = new OtsuThresholder();
        
        initComponents();
        runImage(new File("C:\\Users\\benbryan\\Documents\\School\\Current\\vision_lab\\histology\\Dr Wachtel\\Zfiles_cut\\Z016547b9.bmp"));
//        try {
//            URL url = getClass().getResource("New ROI_20.png");
//            BufferedImage inputImage = ImageIO.read(url);
//            runImage(inputImage);
//        } catch (IOException ex) {
//            Logger.getLogger(SandboxFrame.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
    } 
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        jPanelImageInput.getGraphics().drawImage(inputImage, 0, 0, jPanelImageInput.getWidth(), jPanelImageInput.getHeight(), rootPane);
        jPanelImageOutput.getGraphics().drawImage(outputImage, 0, 0, jPanelImageOutput.getWidth(), jPanelImageOutput.getHeight(), rootPane);
    }
    
    private void runImage(BufferedImage inputImage){
        this.inputImage = inputImage;
        try {                       
            double sigmas[] = {2,4,6};

            byte[] img = ArrayOp.getColorChannel(inputImage, 0);
            long before = System.nanoTime();
            double dataDouble[] = jop.filter(img, inputImage.getWidth(), inputImage.getHeight(), sigmas);
            double durationMS = (System.nanoTime() - before) / 1e6;
            byte data[] = new byte[dataDouble.length];
            ArrayOp.mat2gray(dataDouble, data);
            data = ot.doThreshold(data);
            outputImage = ArrayOp.imagescGray(inputImage.getWidth(), inputImage.getHeight(), data);

            System.out.println("JOCL: "+String.format("%.2f", durationMS)+" ms");

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            setLayout(new BorderLayout());
        } catch (Throwable ex) {
            Logger.getLogger(SandboxFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        repaint();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButtonOpenImage = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanelImageInput = new javax.swing.JPanel();
        jPanelImageOutput = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jButtonOpenImage.setText("OpenImage");
        jButtonOpenImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenImageActionPerformed(evt);
            }
        });

        jButton2.setText("jButton2");

        javax.swing.GroupLayout jPanelImageInputLayout = new javax.swing.GroupLayout(jPanelImageInput);
        jPanelImageInput.setLayout(jPanelImageInputLayout);
        jPanelImageInputLayout.setHorizontalGroup(
            jPanelImageInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 299, Short.MAX_VALUE)
        );
        jPanelImageInputLayout.setVerticalGroup(
            jPanelImageInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanelImageOutputLayout = new javax.swing.GroupLayout(jPanelImageOutput);
        jPanelImageOutput.setLayout(jPanelImageOutputLayout);
        jPanelImageOutputLayout.setHorizontalGroup(
            jPanelImageOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 303, Short.MAX_VALUE)
        );
        jPanelImageOutputLayout.setVerticalGroup(
            jPanelImageOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 279, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jButtonOpenImage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanelImageInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelImageOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonOpenImage)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelImageOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelImageInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    JFileChooser imageChooser = new JFileChooser();
    private void jButtonOpenImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenImageActionPerformed
        imageChooser.showOpenDialog(this);
        File f = imageChooser.getSelectedFile();
        if (f != null){
            runImage(f);
        }
    }//GEN-LAST:event_jButtonOpenImageActionPerformed

    private void runImage(File f) {
        try {
            BufferedImage temp = ImageIO.read(f);
            BufferedImage newImg = new BufferedImage(temp.getWidth(), temp.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            newImg.getGraphics().drawImage(temp, 0, 0, null);
            runImage(newImg);
        } catch (IOException ex) {
            Logger.getLogger(SandboxFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
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
            java.util.logging.Logger.getLogger(SandboxFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SandboxFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SandboxFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SandboxFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SandboxFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButtonOpenImage;
    private javax.swing.JPanel jPanelImageInput;
    private javax.swing.JPanel jPanelImageOutput;
    // End of variables declaration//GEN-END:variables
}
