package avl.sv.client.solution;

import avl.sv.shared.solution.Solution;
import avl.sv.shared.model.classifier.ClassifierInterface;
import avl.sv.shared.model.classifier.ClassifierOptionsPromptInterface;
import avl.sv.shared.model.classifier.ClassifierWeka;
import avl.sv.shared.model.featureGenerator.AbstractFeatureGenerator;
import avl.sv.shared.model.featureGenerator.InterfaceFeatureGeneratorPrompt;
import java.awt.Component;
import static java.awt.Component.LEFT_ALIGNMENT;
import static java.awt.Component.TOP_ALIGNMENT;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

public class ModelSetup extends javax.swing.JDialog {

    final Solution solution;
    private boolean iscanceled = true;

    private ArrayList<String> featureGenerators = new ArrayList<>();
    private ArrayList<String> classifiers = new ArrayList<>();

    public ModelSetup(java.awt.Frame parent, boolean modal, final Solution solution) {
        super(parent, modal);
        initComponents();
        this.solution = solution;
        classifiers.add(ClassifierWeka.class.getCanonicalName());
//        classifiers.add(ClassifierSVM.class);
//        classifiers.add(ClassifierLDA.class);
        featureGenerators.add("avl.sv.shared.model.featureGenerator.jocl.FeatureGeneratorJOCLAdapter");
        populateTabs();
    }

    private void populateTabs() {
        {
            boolean classifierActive = false;
            jPanelClassifiers.removeAll();
            jPanelClassifiers.setLayout(new BoxLayout(jPanelClassifiers, BoxLayout.Y_AXIS));
            for (String c : classifiers) {
                boolean enabled;
                ClassifierInterface classifier = solution.getClassifier(c);
                if (classifier == null) {
                    try {                        
                        classifier = (ClassifierInterface) getClass().getClassLoader().loadClass(c).newInstance();
                    } catch (InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(ModelSetup.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ModelSetup.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    }
                    enabled = false;
                } else {
                    enabled = classifier.isActive();
                }
                classifierActive |= enabled;
                final JPanel panel = classifier.getOptionsPanel();
                panel.setVisible(true);
                panel.setSize(panel.getPreferredSize());
                panel.setAlignmentX(LEFT_ALIGNMENT);
                panel.setAlignmentY(TOP_ALIGNMENT);
                JLabel labelTitle = new JLabel(classifier.getName());
                labelTitle.setFont(new Font(labelTitle.getFont().getName(), labelTitle.getFont().getStyle(), (int) (labelTitle.getFont().getSize() * 1.5)));
                jPanelClassifiers.add(labelTitle);
                JCheckBox checkBoxEnabled = new JCheckBox(new AbstractAction("Include in Solution") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JCheckBox checkBox = (JCheckBox) e.getSource();
                        if (checkBox.isSelected()) {
                            panel.setVisible(true);
                        } else {
                            panel.setVisible(false);
                        }
                        jPanelClassifiers.updateUI();
                    }
                });
                checkBoxEnabled.setSelected(enabled);
                panel.setVisible(enabled);
                checkBoxEnabled.putClientProperty("panel", panel);
                jPanelClassifiers.add(checkBoxEnabled);
                jPanelClassifiers.add(panel);
                JPopupMenu.Separator seperator = new JPopupMenu.Separator();
                seperator.setMaximumSize(new Dimension(seperator.getMaximumSize().width, 10));
                jPanelClassifiers.add(seperator);
            }
            jPanelClassifiers.add(new JPanel());
            if (!classifierActive) {
                for (Component comp : jPanelClassifiers.getComponents()) {
                    if (comp instanceof JCheckBox) {
                        JCheckBox checkBox = (JCheckBox) comp;
                        checkBox.doClick();
                        break;
                    }
                }
            }
            jPanelClassifiers.updateUI();
        }
        {
            boolean featureGeneratorActive = false;
            jPanelFeatures.removeAll();
            jPanelFeatures.setLayout(new BoxLayout(jPanelFeatures, BoxLayout.Y_AXIS));
            for (String c : featureGenerators) {
                boolean enabled;
                AbstractFeatureGenerator featureGenerator = solution.getFeatureGenerator(c);
                if (featureGenerator == null) {
                    try {
                        featureGenerator = (AbstractFeatureGenerator) getClass().getClassLoader().loadClass(c).newInstance();
                    } catch (InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(ModelSetup.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ModelSetup.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    }
                    enabled = false;
                } else {
                    enabled = featureGenerator.isactive;
                }
                featureGeneratorActive |= enabled;
                final JPanel panel = featureGenerator.getOptionsPanel();
                panel.setVisible(true);
                panel.setSize(panel.getPreferredSize());
                panel.setAlignmentX(LEFT_ALIGNMENT);
                panel.setAlignmentY(TOP_ALIGNMENT);
                JLabel labelTitle = new JLabel(featureGenerator.toString());
                labelTitle.setFont(new Font(labelTitle.getFont().toString(), labelTitle.getFont().getStyle(), (int) (labelTitle.getFont().getSize() * 1.5)));
                jPanelFeatures.add(labelTitle);
                JCheckBox checkBoxEnabled = new JCheckBox(new AbstractAction("Include in Solution") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JCheckBox checkBox = (JCheckBox) e.getSource();
                        if (checkBox.isSelected()) {
                            panel.setVisible(true);
                        } else {
                            panel.setVisible(false);
                        }
                        jPanelFeatures.updateUI();
                    }
                });
                checkBoxEnabled.setSelected(enabled);
                panel.setVisible(enabled);
                checkBoxEnabled.putClientProperty("panel", panel);
                jPanelFeatures.add(checkBoxEnabled);
                jPanelFeatures.add(panel);
                JPopupMenu.Separator seperator = new JPopupMenu.Separator();
                seperator.setMaximumSize(new Dimension(seperator.getMaximumSize().width, 10));
                jPanelFeatures.add(seperator);
            }
            jPanelFeatures.add(new JPanel());
            if (!featureGeneratorActive) {
                for (Component comp : jPanelFeatures.getComponents()) {
                    if (comp instanceof JCheckBox) {
                        JCheckBox checkBox = (JCheckBox) comp;
                        checkBox.doClick();
                        break;
                    }
                }
            }
            jPanelFeatures.updateUI();
        }

        pack();
    }

    public boolean iscanceled() {
        return iscanceled;
    }

    private void collectTabData() {
        for (Component comp : jPanelClassifiers.getComponents()) {
            if (comp instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) comp;
                JPanel panel = (JPanel) checkBox.getClientProperty("panel");
                ClassifierInterface classifier = ((ClassifierOptionsPromptInterface) panel).getClassifier();
                classifier.setActive(checkBox.isSelected());
                classifier.invalidate();
                if ((null == solution.getClassifier(classifier.getClass().getCanonicalName())) && !checkBox.isSelected()) {
                } else {
                    solution.setClassifier(classifier);
                }
            }
        }

        for (Component comp : jPanelFeatures.getComponents()) {
            if (comp instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) comp;
                JPanel panel = (JPanel) checkBox.getClientProperty("panel");
                AbstractFeatureGenerator featureGenerator = ((InterfaceFeatureGeneratorPrompt) panel).getFeatureGenerator();
                featureGenerator.isactive = checkBox.isSelected();
                if ((null == solution.getFeatureGenerator(featureGenerator.getClass().getCanonicalName())) && !checkBox.isSelected()) {
                } else {
                    solution.setFeatureGenerator(featureGenerator);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPaneFeatures = new javax.swing.JScrollPane();
        jPanelFeatures = new javax.swing.JPanel();
        jScrollPaneClassifiers = new javax.swing.JScrollPane();
        jPanelClassifiers = new javax.swing.JPanel();
        jButtonAccept = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setTitle("Setup Model");
        setLocationByPlatform(true);

        jTabbedPaneMain.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jScrollPaneFeatures.setBorder(null);
        jScrollPaneFeatures.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        javax.swing.GroupLayout jPanelFeaturesLayout = new javax.swing.GroupLayout(jPanelFeatures);
        jPanelFeatures.setLayout(jPanelFeaturesLayout);
        jPanelFeaturesLayout.setHorizontalGroup(
            jPanelFeaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 685, Short.MAX_VALUE)
        );
        jPanelFeaturesLayout.setVerticalGroup(
            jPanelFeaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 405, Short.MAX_VALUE)
        );

        jScrollPaneFeatures.setViewportView(jPanelFeatures);

        jTabbedPaneMain.addTab("Features", jScrollPaneFeatures);

        javax.swing.GroupLayout jPanelClassifiersLayout = new javax.swing.GroupLayout(jPanelClassifiers);
        jPanelClassifiers.setLayout(jPanelClassifiersLayout);
        jPanelClassifiersLayout.setHorizontalGroup(
            jPanelClassifiersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 683, Short.MAX_VALUE)
        );
        jPanelClassifiersLayout.setVerticalGroup(
            jPanelClassifiersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 403, Short.MAX_VALUE)
        );

        jScrollPaneClassifiers.setViewportView(jPanelClassifiers);

        jTabbedPaneMain.addTab("Classifiers", jScrollPaneClassifiers);

        jButtonAccept.setText("Accept");
        jButtonAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAcceptActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 692, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAccept)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPaneMain)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonAccept)))
        );

        jTabbedPaneMain.getAccessibleContext().setAccessibleName("");
        jTabbedPaneMain.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        iscanceled = true;
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAcceptActionPerformed
        iscanceled = false;
        collectTabData();
        setVisible(false);
        dispose();
    }//GEN-LAST:event_jButtonAcceptActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAccept;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JPanel jPanelClassifiers;
    private javax.swing.JPanel jPanelFeatures;
    private javax.swing.JScrollPane jScrollPaneClassifiers;
    private javax.swing.JScrollPane jScrollPaneFeatures;
    private final javax.swing.JTabbedPane jTabbedPaneMain = new javax.swing.JTabbedPane();
    // End of variables declaration//GEN-END:variables
}
