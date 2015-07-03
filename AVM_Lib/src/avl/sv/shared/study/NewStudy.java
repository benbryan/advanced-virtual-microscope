package avl.sv.shared.study;

import java.awt.Frame;

abstract public class NewStudy extends javax.swing.JDialog {
    public NewStudy(Frame parent) {
        super(parent, true);
        initComponents();
        getRootPane().setDefaultButton(jButtonCreateNewStudy);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextFieldNewStudyName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaNewStudyDescription = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jButtonCreateNewStudy = new javax.swing.JButton();
        jButtonCancelCreateNewStudy = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Study");

        jTextFieldNewStudyName.setText("default");
        jTextFieldNewStudyName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldNewStudyNameActionPerformed(evt);
            }
        });

        jLabel2.setText("Name");

        jTextAreaNewStudyDescription.setColumns(20);
        jTextAreaNewStudyDescription.setRows(5);
        jScrollPane2.setViewportView(jTextAreaNewStudyDescription);

        jLabel3.setText("Description");

        jButtonCreateNewStudy.setText("Create");
        jButtonCreateNewStudy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateNewStudyActionPerformed(evt);
            }
        });

        jButtonCancelCreateNewStudy.setText("Cancel");
        jButtonCancelCreateNewStudy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelCreateNewStudyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jButtonCreateNewStudy, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCancelCreateNewStudy, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                .addComponent(jTextFieldNewStudyName, javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldNewStudyName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonCreateNewStudy)
                    .addComponent(jButtonCancelCreateNewStudy)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldNewStudyNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldNewStudyNameActionPerformed

    }//GEN-LAST:event_jTextFieldNewStudyNameActionPerformed

    private void jButtonCreateNewStudyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateNewStudyActionPerformed
        String studyName = jTextFieldNewStudyName.getText();
        String description = jTextAreaNewStudyDescription.getText();
        create(studyName, description);
        dispose();
    }//GEN-LAST:event_jButtonCreateNewStudyActionPerformed

    abstract public void create(String studyName, String description);
    
    private void jButtonCancelCreateNewStudyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelCreateNewStudyActionPerformed
    }//GEN-LAST:event_jButtonCancelCreateNewStudyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancelCreateNewStudy;
    private javax.swing.JButton jButtonCreateNewStudy;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextAreaNewStudyDescription;
    private javax.swing.JTextField jTextFieldNewStudyName;
    // End of variables declaration//GEN-END:variables
}
