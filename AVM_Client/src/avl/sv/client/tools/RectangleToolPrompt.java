package avl.sv.client.tools;

import java.awt.Window;

public class RectangleToolPrompt extends javax.swing.JDialog {

    public RectangleToolPrompt(Window owner, ModalityType modalityType) {
        super(owner, modalityType);
        initComponents();
        getRootPane().setDefaultButton(jButtonOk);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextFieldWidth = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldHeight = new javax.swing.JTextField();
        jButtonOk = new javax.swing.JButton();
        jCheckBoxEnabled = new javax.swing.JCheckBox();

        setTitle("Set fixed rectangle size");
        setLocationByPlatform(true);
        setResizable(false);

        jTextFieldWidth.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldWidth.setText("256");
        jTextFieldWidth.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldWidthKeyReleased(evt);
            }
        });

        jLabel2.setText("x");

        jTextFieldHeight.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldHeight.setText("256");
        jTextFieldHeight.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldHeightKeyReleased(evt);
            }
        });

        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        jCheckBoxEnabled.setText("Enabled");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButtonOk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 10, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jTextFieldWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(jLabel2)
                                .addGap(6, 6, 6)
                                .addComponent(jTextFieldHeight, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jCheckBoxEnabled, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxEnabled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOk)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldHeightKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldHeightKeyReleased
        try {
            jTextFieldHeight.setText(String.valueOf(Integer.parseInt(jTextFieldHeight.getText())));
        } catch (Exception ex){
            jTextFieldHeight.setText("");
        }
    }//GEN-LAST:event_jTextFieldHeightKeyReleased

    private void jTextFieldWidthKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldWidthKeyReleased
        try {
            jTextFieldWidth.setText(String.valueOf(Integer.parseInt(jTextFieldWidth.getText())));
        } catch (Exception ex) {
            jTextFieldWidth.setText("");
        }
    }//GEN-LAST:event_jTextFieldWidthKeyReleased

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButtonOkActionPerformed

    public int[] getConstraints(){
        if ( !jCheckBoxEnabled.isSelected() || 
             jTextFieldWidth.getText().equals("") || 
             jTextFieldHeight.getText().equals("")) {
            return null;
        } else {
            return new int[]{Integer.parseInt(jTextFieldWidth.getText()),Integer.parseInt(jTextFieldHeight.getText())};
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonOk;
    private javax.swing.JCheckBox jCheckBoxEnabled;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextFieldHeight;
    private javax.swing.JTextField jTextFieldWidth;
    // End of variables declaration//GEN-END:variables
}
