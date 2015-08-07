package avl.sv.client;

import avl.sv.client.image.ImageViewerJFrame;
import avl.sv.client.image.ImageViewer;
import avl.sv.shared.AVM_Properties;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageSource;
import avl.sv.shared.model.featureGenerator.jocl.JOCL_Configure;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UnsupportedLookAndFeelException;

public class AdvancedVirtualMicroscope extends javax.swing.JFrame {
    
    private static final String TITLE_BASE_STRING = "AVM";

    private static LoginDialog loginDialog;

    private static Log logFrame;
    private static ArrayList<Window> windows = new ArrayList<>();
    private static AVM_Properties aVM_Properties = AVM_Properties.getInstance();

    private static JMenuItem jMenuItemLogin = new JMenuItem(new AbstractAction("Login") {
        @Override
        public void actionPerformed(ActionEvent e) {
            jMenuOptions.add(jMenuItemLogout);
            jMenuOptions.remove(jMenuItemLogin);
            loginDialog.setVisible(true);
        }
    });
    private static JMenuItem jMenuItemLogout = new JMenuItem(new AbstractAction("Logout") {
        @Override
        public void actionPerformed(ActionEvent e) {
            jMenuOptions.add(jMenuItemLogin);
            jMenuOptions.remove(jMenuItemLogout);
            jMenuFile.removeAll();
            for (Window window:windows){
                window.dispose();
            }
        }
    });
    
    static void setUsername(String username) {
        getInstance().setTitle(TITLE_BASE_STRING + ", Logged in as " + username);
    }
    
    private AdvancedVirtualMicroscope() {
        initComponents();
        setupLookAndFeel();

        java.awt.EventQueue.invokeLater(() -> {
            setupLookAndFeel();
            logFrame = new Log();
            addWindow(logFrame);
            pack();
        });
        java.awt.EventQueue.invokeLater(() -> {
            loginDialog = new LoginDialog(this); 
            if (0==loginDialog.autologin()){
                jMenuOptions.add(jMenuItemLogout);
            } else {
                jMenuOptions.add(jMenuItemLogin);
                loginDialog.setVisible(true);     
            }            
        });
        pack(); 
    }
    
    private static AdvancedVirtualMicroscope instance = null;
    
    public static void addWindow(final Frame frame) {
        addWindow(frame, frame.getTitle());
    }

    public static void addWindow(final JDialog frame) {
        addWindow(frame, frame.getTitle());
    }

    public static void addWindow(final Window window, final String title) {
        final JMenuItem menuItem = new JMenuItem(new AbstractAction(title) {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.setVisible(true);
                window.toFront();
            }
        });
        if (window instanceof ImageViewerJFrame) {
            jMenuWindowsImages.add(menuItem);
        } else {
            jMenuWindows.add(menuItem);
        }
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (window instanceof ImageViewerJFrame) {
                    jMenuWindowsImages.remove(menuItem);
                    
                } else {
                    jMenuWindows.remove(menuItem);
                }
                windows.remove(window);
                super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
            }
        });
        windows.add(window);
    }

    private static Timer statusTimer = new Timer(5000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            jTextField1.setText("");
        }
    });

    public static ToolPanel getToolPanel() {
        return imageViewerToolPanel;
    }

    public static void main(final String args[]) {
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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdvancedVirtualMicroscope.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                AdvancedVirtualMicroscope avm = AdvancedVirtualMicroscope.getInstance();
                Dimension d = avm.getPreferredSize();
                avm.setSize(d);
                avm.pack();
                avm.setVisible(true);
//                for (int i = 0; i < args.length; i++) {
//                    switch (args[i]) {
//                        case "-server":
//                            if ((i + 1) < args.length) {
//                                try {
//                                    AdvancedVirtualMicroscope.setServerURL(new URL(args[i + 1]));
//                                } catch (MalformedURLException ex) {
//                                    Logger.getLogger(AdvancedVirtualMicroscope.class.getName()).log(Level.SEVERE, null, ex);
//                                }
//                            }
//                            break;
//                    }
//                }
            }
        });
    }

    public static void showLogWindow() {
        logFrame.setVisible(true);
    }

    public static void closeImageViewer(ImageViewer imageViewer) {
        imageViewer.close();
        for (Window window : windows) {
            if (window instanceof ImageViewerJFrame) {
                ImageViewerJFrame imageViewerJFrame = (ImageViewerJFrame) window;
                if (imageViewerJFrame.getImageViewer().equals(imageViewer)){
                    imageViewerJFrame.dispose();
                }
            }
        }
    }
    public static void closeImageViewers(ImageReference imageReference) {
        for (Window window : windows) {
            if (window instanceof ImageViewerJFrame) {
                ImageViewerJFrame imageViewerJFrame = (ImageViewerJFrame) window;
                if (imageViewerJFrame.getImageViewer().getImageSource().imageReference.equals(imageReference)){
                    imageViewerJFrame.getImageViewer().close();
                    imageViewerJFrame.dispose();
                }
            }
        }
    }
    public static void addPlugins(AVM_Plugin[] plugins) {
        if (plugins == null){
            return;
        }
        jMenuFile.removeAll();
        for (AVM_Plugin plugin:plugins){
            jMenuFile.add(plugin.getMenu());
        }        
    }
    
    public static AdvancedVirtualMicroscope getInstance() {
        if (instance == null) {
            instance = new AdvancedVirtualMicroscope();
        }
        return instance;
    }

    public static void setStatusText(String status, int timeoutInMilliseconds) {
        statusTimer.stop();
        jTextField1.setText(status);
        System.out.println(status);
        if (timeoutInMilliseconds > 0) {
            statusTimer.setDelay(timeoutInMilliseconds);
            statusTimer.restart();
        }
        if (!status.endsWith("\n")) {
            status += "\n";
        }
        logFrame.insert(status, 0);
    }
       
    public static void addImageViewer(final ImageSource imageSource) {
        final ImageViewer imageViewer = new ImageViewer(imageSource);
        addImageViewer(imageViewer);
    }

    public static JFrame addImageViewer(final ImageViewer imageViewer) {
        ImageReference imageReference = imageViewer.getImageSource().imageReference;
        String title;
        if (imageReference.imageSetName == null) {
            title = imageReference.imageName;
        } else {
            title = imageReference.imageSetName + "\\" + imageReference.imageName;
        }

        ImageViewerJFrame imageViewerJFrame = new ImageViewerJFrame(imageViewer, title);
        addWindow(imageViewerJFrame, title);
        imageViewerJFrame.setLocationByPlatform(true);
        imageViewerJFrame.add(imageViewer);
        imageViewerJFrame.pack();
        imageViewerJFrame.setVisible(true);
        imageViewerJFrame.toFront();
        imageViewerJFrame.requestFocus();
        imageViewerJFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        EventQueue.invokeLater(imageViewer::centerImage);

        return imageViewerJFrame;
    }

    private void setupLookAndFeel() {
        try {
            //javax.swing.UIManager.setLookAndFeel(new NimbusLookAndFeel());
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    SwingUtilities.updateComponentTreeUI(this);
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdvancedVirtualMicroscope.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    }

    public static ArrayList<ImageViewer> getOpenImageViewers() {
        ArrayList<ImageViewer> svList = new ArrayList<>();
        for (Window window : windows) {
            if (window instanceof ImageViewerJFrame) {
                ImageViewerJFrame viewerJFrame = (ImageViewerJFrame) window;
                ImageViewer imageViewer = viewerJFrame.getImageViewer();
                svList.add(imageViewer);
            }
        }
        return svList;
    }

    public static void setTopImageViewer(ImageViewer sv) {
        if (sv == null) {
            return;
        }
        for (Window window : windows) {
            if (window instanceof ImageViewerJFrame) {
                ImageViewerJFrame viewerJFrame = (ImageViewerJFrame) window;
                if (viewerJFrame.getImageViewer().equals(sv));
                viewerJFrame.toFront();
                viewerJFrame.requestFocus();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelStatus = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        imageViewerToolPanel = new avl.sv.client.ToolPanel();
        jMenuBarMain = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuOptions = new javax.swing.JMenu();
        jMenuItemOptionsOpenCL = new javax.swing.JMenuItem();
        jMenuItemOptionsChagePassword = new javax.swing.JMenuItem();
        jMenuWindows = new javax.swing.JMenu();
        jMenuWindowsImages = new javax.swing.JMenu();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemHelpViewLog = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(TITLE_BASE_STRING);
        setLocationByPlatform(true);
        setResizable(false);

        jLabelStatus.setText("status:");
        jLabelStatus.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelStatusMouseClicked(evt);
            }
        });

        jTextField1.setEditable(false);
        jTextField1.setMinimumSize(new java.awt.Dimension(8, 20));
        jTextField1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField1MouseClicked(evt);
            }
        });
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jMenuFile.setText("File");
        jMenuBarMain.add(jMenuFile);

        jMenuOptions.setText("Options");

        jMenuItemOptionsOpenCL.setText("OpenCL");
        jMenuItemOptionsOpenCL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOptionsOpenCLActionPerformed(evt);
            }
        });
        jMenuOptions.add(jMenuItemOptionsOpenCL);

        jMenuItemOptionsChagePassword.setText("Change Password");
        jMenuItemOptionsChagePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOptionsChagePasswordActionPerformed(evt);
            }
        });
        jMenuOptions.add(jMenuItemOptionsChagePassword);

        jMenuBarMain.add(jMenuOptions);

        jMenuWindows.setText("Windows");

        jMenuWindowsImages.setText("Images");
        jMenuWindows.add(jMenuWindowsImages);

        jMenuBarMain.add(jMenuWindows);

        jMenuHelp.setText("Help");

        jMenuItemHelpViewLog.setText("View Log");
        jMenuItemHelpViewLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHelpViewLogActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemHelpViewLog);

        jMenuBarMain.add(jMenuHelp);

        setJMenuBar(jMenuBarMain);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabelStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(imageViewerToolPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(imageViewerToolPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelStatus)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemOptionsOpenCLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOptionsOpenCLActionPerformed
//        try {
//            ArrayList<String> classFilter = new ArrayList<>();
//            classFilter.add("CL.");
//            classFilter.add("org.jocl.");
//            classFilter.add("avl.sv.shared.model.featureGenerator.jocl.");
//            String name = "avl.sv.shared.model.featureGenerator.jocl.JOCL_Configure";
//            AVM_ClassLoader loader = new AVM_ClassLoader(getClass().getClassLoader(), classFilter);
            JFrame config = new JOCL_Configure();
            config.setVisible(true);
            addWindow(config);
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
//            Logger.getLogger(AdvancedVirtualMicroscope.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }//GEN-LAST:event_jMenuItemOptionsOpenCLActionPerformed

    private void jMenuItemHelpViewLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHelpViewLogActionPerformed
        logFrame.setVisible(true);
    }//GEN-LAST:event_jMenuItemHelpViewLogActionPerformed

    private void jMenuItemOptionsChagePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOptionsChagePasswordActionPerformed
        
//        TODO: change password disabled
//        JPanel panel = new JPanel();
//
//        panel.setLayout(new GridLayout(3, 2));
//
//        JPasswordField oldPass = new JPasswordField(20);
//        panel.add(oldPass);
//        panel.add(new JLabel("Old password"));
//
//        JPasswordField newPass0 = new JPasswordField(20);
//        panel.add(newPass0);
//        panel.add(new JLabel("New password"));
//
//        JPasswordField newPass1 = new JPasswordField(20);
//        panel.add(newPass1);
//        panel.add(new JLabel("New password again"));
//
//        String[] options = new String[]{"OK", "Cancel"};
//        int option = JOptionPane.showOptionDialog(null, panel, "The title",
//                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
//                null, options, options[1]);
//        if (option == 0) { // pressing OK button
//            char[] oldPassord = oldPass.getPassword();
//            char[] newPassword0 = newPass0.getPassword();
//            char[] newPassword1 = newPass1.getPassword();
//            boolean equal = true;
//            if (newPassword0.length == newPassword1.length) {
//                for (int i = 0; i < newPassword0.length; i++) {
//                    if (newPassword0[i] != newPassword1[i]) {
//                        equal = false;
//                        break;
//                    }
//                }
//            } else {
//                equal = false;
//            }
//            if (equal) {
//                String result = loginPort.changePassword(new String(oldPassord), new String(newPassword0));
//                if (result == null) {
//                    AdvancedVirtualMicroscope.setStatusText("Failed to submit password change request to server", 3000);
//                } else if (result.startsWith("error:")) {
//                    AdvancedVirtualMicroscope.setStatusText("Failed to submit password change request to server with " + result, 3000);
//                    JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "New passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
//                } else {
//                    JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Password changed", "Success", JOptionPane.INFORMATION_MESSAGE);
//                }
//            } else {
//                JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "New passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
    }//GEN-LAST:event_jMenuItemOptionsChagePasswordActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField1MouseClicked
        if (evt.getClickCount() > 1){
            logFrame.setVisible(true);
        }
    }//GEN-LAST:event_jTextField1MouseClicked

    private void jLabelStatusMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelStatusMouseClicked
        if (evt.getClickCount() > 1){
            logFrame.setVisible(true);
        }
    }//GEN-LAST:event_jLabelStatusMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private static avl.sv.client.ToolPanel imageViewerToolPanel;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JMenuBar jMenuBarMain;
    private static javax.swing.JMenu jMenuFile;
    private static javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemHelpViewLog;
    private javax.swing.JMenuItem jMenuItemOptionsChagePassword;
    private javax.swing.JMenuItem jMenuItemOptionsOpenCL;
    private static javax.swing.JMenu jMenuOptions;
    private static javax.swing.JMenu jMenuWindows;
    private static javax.swing.JMenu jMenuWindowsImages;
    private static javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

}
