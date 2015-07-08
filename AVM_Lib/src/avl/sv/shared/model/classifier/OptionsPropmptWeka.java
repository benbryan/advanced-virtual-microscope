
package avl.sv.shared.model.classifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LeastMedSq;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.RandomSubSpace;
import weka.classifiers.trees.FT;
import weka.classifiers.trees.RandomForest;
import weka.core.Option;

public final class OptionsPropmptWeka extends javax.swing.JPanel implements ClassifierOptionsPromptInterface{
    
    public static void main(String args[]){
//        RandomForest rf = new RandomForest();
//        JPanel panel = new OptionsPropmptWeka(rf);
//        JFrame f = new JFrame();
//        f.add(panel);
//        panel.setVisible(true);
//        f.setVisible(true);
//        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        f.setSize(500,500);
       
    }
    
    final Classifier classifierWekaInput;
    public OptionsPropmptWeka(Classifier classifierWeka) {
        this.classifierWekaInput = classifierWeka;
        initComponents();
        
        DefaultComboBoxModel<Class> comboBoxModel = new DefaultComboBoxModel<>();
        comboBoxModel.addElement(NaiveBayes.class);
        comboBoxModel.addElement(RandomForest.class);
        comboBoxModel.addElement(FT.class);
        comboBoxModel.addElement(LeastMedSq.class);
        comboBoxModel.addElement(LibSVM.class);
        comboBoxModel.addElement(RandomSubSpace.class);
        
        jComboBoxClassifiers.setModel(comboBoxModel);
        comboBoxModel.setSelectedItem(classifierWeka.getClass());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jComboBoxClassifiers = new javax.swing.JComboBox<Class>();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jComboBoxClassifiers.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxClassifiers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxClassifiersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBoxClassifiers, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBoxClassifiers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxClassifiersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxClassifiersActionPerformed
        try {
            Class c = (Class) jComboBoxClassifiers.getSelectedItem();
            if (classifierWekaInput.getClass().equals(c)){
                setClassifier(classifierWekaInput);
            } else {
                setClassifier((Classifier) c.newInstance());
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(OptionsPropmptWeka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jComboBoxClassifiersActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<Class> jComboBoxClassifiers;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    private String[] getOptions(){
        ArrayList<String> options = new ArrayList<>();
        TableModel model = jTable1.getModel();
        for (int r = 0; r < model.getRowCount(); r++){
            boolean enabled = (boolean) model.getValueAt(r, 1);
            if (enabled){
                String syn = (String) model.getValueAt(r, 2);
                syn = syn.split(" ")[0];
                String arg = (String) model.getValueAt(r, 3);
                options.add(syn);
                options.add(arg);
            }
        }
        return options.toArray(new String[options.size()]);
    }
    
    @Override
    public ClassifierWeka getClassifier() {
        try {
            Class item = (Class) jComboBoxClassifiers.getSelectedItem();
            Classifier c = Classifier.forName(item.getCanonicalName(), getOptions());
            return new ClassifierWeka(c);
        } catch (Exception ex) {
            Logger.getLogger(OptionsPropmptWeka.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public void setNumelFeatures(int numel) {}

    public void setOptions(String[] options) {
        TableModel model = jTable1.getModel();
        for (int i = 0; i < options.length; i++){
            String option = options[i];
            if (option.startsWith("-")){
                for (int r = 0; r < model.getRowCount(); r++){
                    String entry = (String) model.getValueAt(r, 2);
                    if (entry.startsWith(option)){
                        model.setValueAt(true, r, 1);
                        if (i+1<options.length){
                            String arg = options[i+1];
                            if (!arg.startsWith("-")){
                                model.setValueAt(arg, r, 3);
                            }
                        }
                    }
                }
            }
        }
    }

    private void setClassifier(Classifier classifierWeka) {
        Enumeration enumeration = classifierWeka.listOptions();
        DefaultTableModel model = new DefaultTableModel(){
            String columnNames[] = new String[]{"Name", "Enabled", "Synopsis", "arg", "Description"};
            Class columnClass[] = new Class[]{String.class, Boolean.class, String.class, String.class, String.class};
            @Override
            public String getColumnName(int column) {
                return columnNames[column];
            }

            @Override
            public int getColumnCount() {
                return columnNames.length;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnClass[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 1) || (column == 3);
            }
        };
        while (enumeration.hasMoreElements()){
            Object element = enumeration.nextElement();
            if (element instanceof Option){
                Option option = (Option) element;
                JLabel label = new JLabel(option.name());
                label.setToolTipText(option.description());
                
                JCheckBox checkBox = new JCheckBox(option.name());
                checkBox.setToolTipText(option.description());
                
                JTextArea textArea = new JTextArea();
                textArea.setToolTipText(option.description());
                model.addRow(new Object[]{option.name(), false, option.synopsis(), "", option.description()});
            }
        }
        jTable1.setModel(model);
        jTable1.getColumnModel().getColumn(0).setMinWidth(50);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(80);
        
        jTable1.getColumnModel().getColumn(1).setMaxWidth(60);
        jTable1.getColumnModel().getColumn(1).setMinWidth(60);
        
        jTable1.getColumnModel().getColumn(2).setMinWidth(100);
        jTable1.getColumnModel().getColumn(2).setMaxWidth(150);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        jTable1.getColumnModel().getColumn(3).setMinWidth(50);
        jTable1.getColumnModel().getColumn(3).setMaxWidth(100);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(70);
        
        jTable1.getColumnModel().getColumn(4).setMinWidth(100);
        
        setOptions(classifierWeka.getOptions());
      
    }
}
