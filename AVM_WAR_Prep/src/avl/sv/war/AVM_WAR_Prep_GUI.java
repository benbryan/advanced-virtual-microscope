package avl.sv.war;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.Timer;

public class AVM_WAR_Prep_GUI extends javax.swing.JFrame {

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AVM_WAR_Prep_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new AVM_WAR_Prep_GUI().setVisible(true);
        });
    }
    
    JFileChooser jFileChooserWar = new JFileChooser();
    JFileChooser jFileChooserJDK = new JFileChooser();
    JFileChooser jFileChooserKeystore = new JFileChooser();

    public AVM_WAR_Prep_GUI() {
        initComponents();
        Properties p = AVM_WAR_Prep.getProperties();
        jTextFieldAlias.setText(p.getProperty(AVM_WAR_Prep.KEY_ALIAS));
        jTextFieldArguments.setText(p.getProperty(AVM_WAR_Prep.KEY_ARGUMENTS));
        jTextFieldClientJarName.setText(p.getProperty(AVM_WAR_Prep.KEY_CLIENT_JAR_NAME));
        jTextFieldCodebase.setText(p.getProperty(AVM_WAR_Prep.KEY_CODEBASE));
        jTextFieldJNLP_fileName.setText(p.getProperty(AVM_WAR_Prep.KEY_JNLP_FILE_NAME));
        jTextFieldKeystore.setText(p.getProperty(AVM_WAR_Prep.KEY_KEYSTORE_PATH));
        jTextFieldJDK_Path.setText(p.getProperty(AVM_WAR_Prep.KEY_JDK_Path));
        jTextFieldStorePass.setText(p.getProperty(AVM_WAR_Prep.KEY_STOREPASS));
        jTextFieldTSA.setText(p.getProperty(AVM_WAR_Prep.KEY_TSA));
        jTextFieldWarName.setText(p.getProperty(AVM_WAR_Prep.KEY_WAR_PATH));
        
        jFileChooserWar.setSelectedFile(new File(AVM_WAR_Prep.KEY_WAR_PATH));
        jFileChooserJDK.setSelectedFile(new File(AVM_WAR_Prep.KEY_JDK_Path));
        jFileChooserKeystore.setSelectedFile(new File(AVM_WAR_Prep.KEY_KEYSTORE_PATH));
        
        setLocationByPlatform(true);
        addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextFieldClientJarName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldWarName = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldCodebase = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldJNLP_fileName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldArguments = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldTSA = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldStorePass = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldKeystore = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldAlias = new javax.swing.JTextField();
        jTextFieldJDK_Path = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Jar Signer");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("General properties"));

        jTextFieldClientJarName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldClientJarNameFocusLost(evt);
            }
        });

        jLabel5.setText("War file");

        jTextFieldWarName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextFieldWarNameMouseClicked(evt);
            }
        });

        jLabel9.setText("Arguments");

        jLabel2.setText("Codebase");

        jTextFieldCodebase.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldCodebaseFocusLost(evt);
            }
        });

        jLabel3.setText("JNLP file name");

        jTextFieldJNLP_fileName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldJNLP_fileNameFocusLost(evt);
            }
        });

        jLabel4.setText("Client JAR name");

        jTextFieldArguments.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldArgumentsFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextFieldWarName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                    .addComponent(jTextFieldClientJarName, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldJNLP_fileName, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldCodebase, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldArguments, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextFieldWarName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldClientJarName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldJNLP_fileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldCodebase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextFieldArguments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Jarsigner arguments"));

        jLabel6.setText("TSA");

        jTextFieldTSA.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldTSAFocusLost(evt);
            }
        });

        jLabel7.setText("Storepass");

        jTextFieldStorePass.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldStorePassFocusLost(evt);
            }
        });

        jLabel8.setText("Alias");

        jTextFieldKeystore.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextFieldKeystoreMouseClicked(evt);
            }
        });
        jTextFieldKeystore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldKeystoreActionPerformed(evt);
            }
        });

        jLabel1.setText("Keystore file");

        jTextFieldAlias.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldAliasFocusLost(evt);
            }
        });

        jTextFieldJDK_Path.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextFieldJDK_PathMouseClicked(evt);
            }
        });
        jTextFieldJDK_Path.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldJDK_PathActionPerformed(evt);
            }
        });

        jLabel10.setText("JDK path");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextFieldStorePass, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                            .addComponent(jTextFieldTSA, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldKeystore, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldAlias)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldJDK_Path)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldJDK_Path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldKeystore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextFieldTSA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextFieldStorePass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextFieldAlias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jToggleButton1.setText("Run");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldKeystoreMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFieldKeystoreMouseClicked
        if (evt.getClickCount() == 1) {
            int result = jFileChooserKeystore.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File f = jFileChooserKeystore.getSelectedFile();
                JTextField jTextField = (JTextField) evt.getSource();
                jTextField.setText(f.getAbsolutePath());
                AVM_WAR_Prep.setProperty(AVM_WAR_Prep.KEY_KEYSTORE_PATH, f.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_jTextFieldKeystoreMouseClicked

    private void jTextFieldWarNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFieldWarNameMouseClicked
        if (evt.getClickCount() == 1) {
            int result = jFileChooserWar.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File f = jFileChooserWar.getSelectedFile();
                JTextField jTextField = (JTextField) evt.getSource();
                jTextField.setText(f.getAbsolutePath());
                AVM_WAR_Prep.setProperty(AVM_WAR_Prep.KEY_WAR_PATH, f.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_jTextFieldWarNameMouseClicked

    Future<?> future = null;
    JFrame frameStatus = null;
    Timer timer;
    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        final JToggleButton button = (JToggleButton) evt.getSource();
        if (button.isSelected()) {
            button.setText("break execution");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream pw = new PrintStream(baos);

            frameStatus = new JFrame("WAR prep status");
            frameStatus.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setAutoscrolls(true);
            frameStatus.add(scrollPane);
            final JTextArea textArea = new JTextArea();
            scrollPane.setViewportView(textArea);
            textArea.setAlignmentX(0);
            textArea.setAlignmentY(0);
            frameStatus.setSize(400, 500);
            timer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String s = baos.toString();
                    baos.reset();
                    textArea.append(s);
                    System.out.print(s);
                }
            });
            timer.setRepeats(true);
            timer.start();
            frameStatus.setVisible(true);
            scrollPane.setVisible(true);
            textArea.setVisible(true);
            future = Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String result = new AVM_WAR_Prep(AVM_WAR_Prep.getProperties(), pw).signAVM();
                        JOptionPane.showMessageDialog(null, result);
                    } catch (Exception ex) {
                        Logger.getLogger(AVM_WAR_Prep_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                    cancelRun();
                }
            });
        } else {
            cancelRun();
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jTextFieldStorePassFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldStorePassFocusLost
        AVM_WAR_Prep.setProperty(AVM_WAR_Prep.KEY_STOREPASS, ((JTextField) evt.getSource()).getText());
    }//GEN-LAST:event_jTextFieldStorePassFocusLost

    private void jTextFieldAliasFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldAliasFocusLost
        AVM_WAR_Prep.setProperty(AVM_WAR_Prep.KEY_ALIAS, ((JTextField) evt.getSource()).getText());
    }//GEN-LAST:event_jTextFieldAliasFocusLost

    private void jTextFieldKeystoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldKeystoreActionPerformed

    }//GEN-LAST:event_jTextFieldKeystoreActionPerformed

    private void jTextFieldJDK_PathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldJDK_PathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldJDK_PathActionPerformed

    private void jTextFieldJDK_PathMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFieldJDK_PathMouseClicked
        if (evt.getClickCount() == 1) {
            jFileChooserJDK.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = jFileChooserJDK.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File f = jFileChooserJDK.getSelectedFile();
                JTextField jTextField = (JTextField) evt.getSource();
                jTextField.setText(f.getAbsolutePath());
                AVM_WAR_Prep.setProperty(AVM_WAR_Prep.KEY_JDK_Path, f.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_jTextFieldJDK_PathMouseClicked

    private void jTextFieldCodebaseFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldCodebaseFocusLost
        AVM_WAR_Prep.setProperty(AVM_WAR_Prep.KEY_CODEBASE, ((JTextField) evt.getSource()).getText());
    }//GEN-LAST:event_jTextFieldCodebaseFocusLost

    private void jTextFieldArgumentsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldArgumentsFocusLost
        AVM_WAR_Prep.setProperty(AVM_WAR_Prep.KEY_ARGUMENTS, ((JTextField) evt.getSource()).getText());
    }//GEN-LAST:event_jTextFieldArgumentsFocusLost

    private void jTextFieldJNLP_fileNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldJNLP_fileNameFocusLost
        AVM_WAR_Prep.setProperty(AVM_WAR_Prep.KEY_JNLP_FILE_NAME, ((JTextField) evt.getSource()).getText());
    }//GEN-LAST:event_jTextFieldJNLP_fileNameFocusLost

    private void jTextFieldClientJarNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldClientJarNameFocusLost
        AVM_WAR_Prep.setProperty(AVM_WAR_Prep.KEY_CLIENT_JAR_NAME, ((JTextField) evt.getSource()).getText());
    }//GEN-LAST:event_jTextFieldClientJarNameFocusLost

    private void jTextFieldTSAFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldTSAFocusLost
        AVM_WAR_Prep.setProperty(AVM_WAR_Prep.KEY_TSA, ((JTextField) evt.getSource()).getText());
    }//GEN-LAST:event_jTextFieldTSAFocusLost

    private void cancelRun(){
        jToggleButton1.setText("Run");
        jToggleButton1.setSelected(false);
        if (future != null) {
            future.cancel(true);
        }
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextFieldAlias;
    private javax.swing.JTextField jTextFieldArguments;
    private javax.swing.JTextField jTextFieldClientJarName;
    private javax.swing.JTextField jTextFieldCodebase;
    private javax.swing.JTextField jTextFieldJDK_Path;
    private javax.swing.JTextField jTextFieldJNLP_fileName;
    private javax.swing.JTextField jTextFieldKeystore;
    private javax.swing.JTextField jTextFieldStorePass;
    private javax.swing.JTextField jTextFieldTSA;
    private javax.swing.JTextField jTextFieldWarName;
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables
}
