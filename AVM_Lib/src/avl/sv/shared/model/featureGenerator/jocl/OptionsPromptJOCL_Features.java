package avl.sv.shared.model.featureGenerator.jocl;

import avl.sv.shared.model.featureGenerator.jocl.feature.Feature;
import avl.sv.shared.model.featureGenerator.jocl.plane.Plane;
import avl.sv.shared.model.featureGenerator.AbstractFeatureGenerator;
import avl.sv.shared.model.featureGenerator.InterfaceFeatureGeneratorPrompt;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

public class OptionsPromptJOCL_Features extends javax.swing.JPanel implements InterfaceFeatureGeneratorPrompt {
    
    private final ArrayList<Plane> planesSelected;
    private final ArrayList<Plane> planesAll;
    
    public OptionsPromptJOCL_Features(FeatureGeneratorJOCL jocl_Features) {
        initComponents();
        
        HashMap<String,Plane> map = new HashMap<>();
        
        // clone planes selected from original generator
        for (String planeFeature:jocl_Features.getFeatureNames()){
            String[] temp = planeFeature.split("->");
            if (temp.length != 2){
                continue;
            }
            String planeName = temp[0];
            String featureName = temp[1];
            try {
                Plane plane = map.get(temp[0]);
                if (plane == null){
                    map.put(planeName, new Plane(Plane.Names.valueOf(planeName), new ArrayList<Feature>()));
                    plane = map.get(planeName);
                }
                boolean exists = false;
                for (Feature feature:plane.features){
                    if (feature.featureName.equals(Feature.Names.valueOf(featureName))){
                        exists = true;
                        break;
                    }
                }
                if (exists == false){
                    plane.features.add(new Feature(Feature.Names.valueOf(featureName)));
                }
            } catch (Exception ex){ }
        }
        
        planesSelected = new ArrayList<>(map.values());        
        planesAll = new ArrayList<>();
        for (Plane.Names planeName:Plane.Names.values()){
            ArrayList<Feature> features = new ArrayList<>();
            for (Feature.Names featureName:Feature.Names.values()){
                Feature feature = new Feature(featureName);
                features.add(feature);
            }
            planesAll.add(new Plane(planeName, features));
        }
        
        jTableFeatures.setModel(new FeatureTableModel());
        TableColumnModel columnModel = jTableFeatures.getColumnModel();
        for (int i = 1; i < columnModel.getColumnCount(); i++){
            columnModel.getColumn(i).setMaxWidth(100);
            columnModel.getColumn(i).setPreferredWidth(60);
        }
        TableColumnModel cm = jTableFeatures.getColumnModel();
        cm.getColumn(0).setPreferredWidth(300);
        for (int i = 1; i < cm.getColumnCount(); i++){
            cm.getColumn(i).setPreferredWidth(150);
        }
    }
    
    @Override
    public AbstractFeatureGenerator getFeatureGenerator() {
        FeatureGeneratorJOCLAdapter adapter = new FeatureGeneratorJOCLAdapter();
        ArrayList<String> names = new ArrayList<>();
        for (Plane plane:planesSelected){
            for (Feature feature:plane.features){
                names.add(plane.planeName + "->" + feature.featureName);
            }
        }
        String[] features = names.toArray(new String[names.size()]);
        adapter.setFeatureNames(features);
        return adapter;
    }

    private class FeatureTableModel extends AbstractTableModel {

        private final int numelRows = Feature.Names.values().length;
        private final int numelColumns = Plane.Names.values().length + 1;

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex > 0) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex > 0) {
                return planesAll.get(columnIndex-1).planeName.name();
            } else {
                return "Name";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex > 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public int getRowCount() {
            return numelRows;
        }

        @Override
        public int getColumnCount() {
            return numelColumns;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0){
                return Feature.Names.values()[rowIndex].name();
            } else {
                Plane plane = planesAll.get(columnIndex-1);
                Feature feature = plane.features.get(rowIndex);
                for (Plane p:planesSelected){
                    if (p.planeName.equals(plane.planeName)){
                        for (Feature f:p.features){
                            if (f.featureName.equals(feature.featureName)){
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0){
                return;
            } else {
                if (aValue instanceof Boolean){
                    boolean isChecked = (Boolean) aValue;
                    if (isChecked){
                        Plane plane = planesAll.get(columnIndex-1);
                        Feature feature = plane.features.get(rowIndex);
                        for (Plane p:planesSelected){
                            if (p.planeName.equals(plane.planeName)){
                                for (Feature f:p.features){
                                    if (f.featureName.equals(feature.featureName)){
                                        return;
                                    }
                                }
                                p.features.add(feature);
                                return;
                            }
                        }
                        ArrayList<Feature> newFeature = new ArrayList<>();
                        newFeature.add(feature);
                        planesSelected.add(new Plane(plane.planeName, newFeature));
                    } else {
                        Plane plane = planesAll.get(columnIndex-1);
                        Feature feature = plane.features.get(rowIndex);
                        for (Plane p:planesSelected){
                            if (p.planeName.equals(plane.planeName)){
                                for (Feature f:p.features){
                                    if (f.featureName.equals(feature.featureName)){
                                        p.features.remove(f);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                
            }
        }
        
    };


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTableFeatures = new javax.swing.JTable();

        jTableFeatures.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTableFeatures.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTableFeatures);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableFeatures;
    // End of variables declaration//GEN-END:variables

}
