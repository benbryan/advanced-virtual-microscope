package avl.sv.admin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;

public class AdminFrame extends javax.swing.JFrame {

    KVStore kvstore;
    DatabaseExplorer databaseExplorer1;
    JFileChooser slideChooser = new JFileChooser();

    private void setupKvstore() {
        String[] hhosts = {jTextFieldDatabaseURL.getText()};
        String name = jTextFieldDatabaseName.getText();
        KVStoreConfig kconfig = new KVStoreConfig(name, hhosts);
        try {
            kvstore = KVStoreFactory.getStore(kconfig);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to database");
        }
        if (kvstore != null) {
            databaseExplorer1.setkvStore(kvstore);
        }
        jToggleButtonConnect.setSelected(kvstore != null);
        jTextFieldDatabaseURL.setEnabled(kvstore == null);
        jTextFieldDatabaseName.setEnabled(kvstore == null);
    }

    private class Sample{
        final int x,y;
        final byte b[];

        public Sample(int x, int y, byte[] b) {
            this.x = x;
            this.y = y;
            this.b = b;
        }
        
    }
    
    public AdminFrame() throws IOException {
        initComponents();
        databaseExplorer1 = new DatabaseExplorer();
        setupKvstore();
        jTabbedPaneMain.add("Database Explorer", databaseExplorer1);
        setLocationByPlatform(true);
        
//        final ArrayList<TiffDirectory> buff = TiffFile.getTiffDirectories(new File("C:\\V265629H1_1_1.svs"));
//        final TiffDirectory dir = buff.get(0);
//        final TiffDirectoryBuffer tdbuff = new TiffDirectoryBuffer(dir.getProperties());
//        tdbuff.setupDecoder(dir.getJpegTables());
//        ArrayList<Sample> samples = new ArrayList<Sample>();
//        for (int x = 0; x < dir.getTilesAcrossW(); x++) {
//            for (int y = 0; y < dir.getTilesDownL(); y++) {
//                try {
//                    byte b[] = dir.getTileAsByteArray(x, y);
//                    samples.add(new Sample(x, y, b));
//                } catch (IOException ex) {
//                    Logger.getLogger(AdminFrame.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//        for (int i = 0; i < 10; i++){
//            Sample s = samples.get(i);
//            if (s.b == null){
//                continue;
//            }
//            tdbuff.setTile(s.x, s.y, s.b);
//        }
//        
//        final ExecutorService executor = Executors.newFixedThreadPool(8);
//        final ExecutorCompletionService<Integer> com = new ExecutorCompletionService(executor);
//        final JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(dir.getJpegTables()));
//        decoder.decodeAsBufferedImage();
//        final JPEGDecodeParam param = decoder.getJPEGDecodeParam();
//        
//        long tic = new Date().getTime();
//        int selector = 1;
//        switch (selector) {
//            case 0:
//                for (Sample s : samples) {
//                    tdbuff.setTile(s.x, s.y, s.b);
//                };
//                break;
//            case 1:
//                for (final Sample s : samples) {
//                    executor.submit(new Runnable() {
//                        @Override
//                        public void run() {
//                            tdbuff.setTile(s.x, s.y, s.b);
//                            tdbuff.clearTile(s.x, s.y);
//                        }
//                    }, 0);
//                }
//                try {
//                    executor.shutdown();
//                    executor.awaitTermination(5, TimeUnit.MINUTES);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(AdminFrame.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                break;
//            case 2:
//                for (final Sample s : samples) {
//                    com.submit(new Runnable() {
//                        @Override
//                        public void run() {
//                            tdbuff.setTile(s.x, s.y, s.b);
//                        }
//                    },0);
//                }
//
//                try {
//                    for (int i = 0 ; i < samples.size(); i++){
//                        com.poll(5, TimeUnit.MINUTES);
//                    }
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(AdminFrame.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                break;
//            case 3:
//                for (final Sample s : samples) {
//                    BufferedImage img = JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(s.b), param).decodeAsBufferedImage();
//                    tdbuff.setTile(s.x, s.y, img);
//                }
//                break;
//            case 4:
//                for (final Sample s : samples) {
//                    com.submit(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                BufferedImage img = JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(s.b), param).decodeAsBufferedImage();
////                                tdbuff.setTile(s.x, s.y, img);
//                            } catch (IOException ex) {
//                                Logger.getLogger(AdminFrame.class.getName()).log(Level.SEVERE, null, ex);
//                            } catch (ImageFormatException ex) {
//                                Logger.getLogger(AdminFrame.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                        }
//                    }, 0);
//                }
//
//                try {
//                    for (int i = 0 ; i < samples.size(); i++){
//                        com.poll(5, TimeUnit.MINUTES);
//                    }
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(AdminFrame.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                break;
//                
//        }
//
//        long toc = new Date().getTime();
//        System.out.println("Runtime = " + String.valueOf(toc - tic));
//
//        System.exit(0);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPaneMain = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jTextFieldDatabaseURL = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jToggleButtonConnect = new javax.swing.JToggleButton();
        jTextFieldDatabaseName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Advanced Virtual Microscope Admin");

        jTabbedPaneMain.setPreferredSize(new java.awt.Dimension(832, 460));
        jTabbedPaneMain.setRequestFocusEnabled(false);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTextFieldDatabaseURL.setText("localhost:5000");
        jTextFieldDatabaseURL.setToolTipText("");
        jTextFieldDatabaseURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDatabaseURLActionPerformed(evt);
            }
        });

        jLabel4.setText("URL");

        jToggleButtonConnect.setText("Connect");
        jToggleButtonConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonConnectActionPerformed(evt);
            }
        });

        jTextFieldDatabaseName.setText("svStore");

        jLabel6.setText("Name");

        jLabel7.setText("Database settings");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                        .addGap(36, 36, 36))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jToggleButtonConnect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextFieldDatabaseURL, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldDatabaseName))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldDatabaseURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldDatabaseName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButtonConnect)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(513, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(240, Short.MAX_VALUE))
        );

        jTabbedPaneMain.addTab("Main", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 748, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void databaseExplorer1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_databaseExplorer1FocusGained
    }//GEN-LAST:event_databaseExplorer1FocusGained

    private void databaseExplorer1ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_databaseExplorer1ComponentShown
        databaseExplorer1.populateTrees();
    }//GEN-LAST:event_databaseExplorer1ComponentShown

    private String getFileExtension(File file) {
        String extension = "";
        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            extension = file.getName().substring(i + 1);
        }
        return extension;
    }

    private void jTextFieldDatabaseURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDatabaseURLActionPerformed
    }//GEN-LAST:event_jTextFieldDatabaseURLActionPerformed

    private void jToggleButtonConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonConnectActionPerformed
        if (!jToggleButtonConnect.isSelected()) {
            kvstore.close();
            kvstore = null;
            jToggleButtonConnect.setSelected(kvstore != null);
            jTextFieldDatabaseURL.setEnabled(kvstore == null);
            jTextFieldDatabaseName.setEnabled(kvstore == null);
        } else {
            setupKvstore();
        }

    }//GEN-LAST:event_jToggleButtonConnectActionPerformed

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
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new AdminFrame().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(AdminFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPaneMain;
    private javax.swing.JTextField jTextFieldDatabaseName;
    private javax.swing.JTextField jTextFieldDatabaseURL;
    private javax.swing.JToggleButton jToggleButtonConnect;
    // End of variables declaration//GEN-END:variables
}
