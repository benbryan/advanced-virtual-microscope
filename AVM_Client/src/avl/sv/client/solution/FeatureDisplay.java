package avl.sv.client.solution;

import avl.sv.client.image.ImageViewer;
import avl.sv.client.image.ImageViewerPlugin;
import avl.sv.client.image.ImageViewerPluginListener;
import avl.sv.shared.solution.Sample;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class FeatureDisplay extends javax.swing.JFrame implements ImageViewerPlugin {
    final ArrayList<Sample> samples;
    final String featureNames[];
    final ImageViewer imageViewer;
    private final DefaultTableModel model;
    private double[][] modSpace;
    private boolean tableChangedListenerEnabled = true;
    private BufferedImage overlayImage = null;
    int upperX, upperY, lowerX, lowerY;
    
    public FeatureDisplay(ImageViewer imageViewer, ArrayList<Sample> samples, String featureNames[], String title) {
        initComponents();
        this.samples = samples;
        this.featureNames = featureNames;
        this.imageViewer = imageViewer;
        
        setTitle(title);
        
        jButtonUpdateDisplayMethod.setVisible(false);
        jRadioButtonMultiFeatureLDA.setEnabled(false);
        
        model = (DefaultTableModel) new DefaultTableModel(){
            Class columnClasses[] = new Class[]{String.class, Double.class, Double.class, Double.class, Double.class, Boolean.class};
            String columnNames[] = new String[]{"Name", "Min", "Max", "Mean", "Std", "Visible"};
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnClasses[columnIndex];
            }

            @Override
            public int getColumnCount() {
                return columnClasses.length;
            }

            @Override
            public String getColumnName(int column) {
                return columnNames[column]; //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                if ((column == 1) || (column == 2) || (column == 5)){
                    return true;
                } 
                return false;
            }
             
        };
        
        double features[][] = new double[featureNames.length][samples.size()];
        double[] fMins = new double[featureNames.length];
        double[] fMaxes = new double[featureNames.length];
        double[] fMeans = new double[featureNames.length];
        double[] fStds = new double[featureNames.length];
        
        //Collect samples into one big 2d array
        for (int i = 0; i < samples.size(); i++){
            Sample s = samples.get(i);
            double[] f = s.featureVector;
            for (int j = 0; j < f.length; j++){
                features[j][i] = f[j];
            }
        }      
        
        for (int i = 0; i < features.length; i++){
            double[] s = features[i];
            double sum = 0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (int j = 0; j < s.length; j++){
                sum += s[j];
                if (min > s[j]){
                    min = s[j];
                }
                if (max < s[j]){
                    max = s[j];
                }
            }
            double mean = sum/s.length;
            sum = 0;
            for (int j = 0; j < s.length; j++){
                sum += (s[j]-mean)*(s[j]-mean);
            }
            double std = Math.sqrt(sum);
            fMins[i] = min;
            fMaxes[i] = max;
            fMeans[i] = mean;
            fStds[i] = std;
        }
        
        for (int i = 0; i < featureNames.length; i++){
            model.addRow(new Object[]{featureNames[i], fMins[i], fMaxes[i], fMeans[i], fStds[i], false });
        }
        jTableClassifierResults.setModel(model);
        
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (tableChangedListenerEnabled){
                    int selectedRow = e.getFirstRow();
                    overlayImage = null;
                    updateVisible(selectedRow);
                    imageViewer.repaint();
                }
            }
        });
        jSliderOccupacity.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                try {
                    if (overlayImage != null){
                        int alpha = jSliderOccupacity.getValue();
                        DataBuffer buff = overlayImage.getRaster().getDataBuffer();
                        for (int i = 0; i < buff.getSize(); i+=4){
                            if (buff.getElem(i)>0){
                                buff.setElem(i, alpha);
                            }
                        }
                    }
                    imageViewer.repaint();
                } catch (Exception ex) { }
            }
        });
        setVisible(true);
        jTableClassifierResults.getModel().setValueAt(true, 0, 5);
    }
    
    private void updateVisible(int selectedRow) {
        tableChangedListenerEnabled = false;
        if (jRadioButtonSingleFeature.isSelected()){
            // Make sure only one Visible checkbox is checked.  Otherwise features will display over features
            for (int c = 0; c < model.getColumnCount(); c++) {
                if (model.getColumnName(c).equalsIgnoreCase("Visible")) {
                    for (int r = 0; r < model.getRowCount(); r++) {
                        Object obj = model.getValueAt(r, c);
                        if (obj instanceof Boolean) {
                            model.setValueAt(false, r, c);
                        }
                    }
                    model.setValueAt(true, selectedRow, c);
                }
            }
        }
        tableChangedListenerEnabled = true;
    }

    @Override
    public void paintPlugin(ImageViewer imageViewer, Graphics gOrig) {
        if ((samples == null)) {
            return;
        }
        if (jRadioButtonSingleFeature.isSelected()){
            Graphics2D g = (Graphics2D) gOrig.create();
            imageViewer.concatenateImageToDisplayTransform(g);
            
            if (overlayImage == null){
                upperX = 0;
                upperY = 0;
                lowerX = Integer.MAX_VALUE;
                lowerY = Integer.MAX_VALUE;
                for (int row = 0; row < jTableClassifierResults.getRowCount(); row++) {
                    Boolean visible = (Boolean) jTableClassifierResults.getModel().getValueAt(row, 5);
                    if (visible) {
                        for (Sample s : samples) {
                            Rectangle r = s.tile;
                            if ((r.x+r.width) > upperX){
                                upperX = r.x+r.width;
                            }
                            if ((r.y+r.height) > upperY){
                                upperY = r.y+r.height;
                            }
                            if (r.x < lowerX){
                                lowerX = r.x;
                            }
                            if (r.y < lowerY){
                                lowerY = r.y;
                            }
                        }
                    }
                }                
                int tileDim = samples.get(0).tile.width;
                overlayImage = new BufferedImage((upperX-lowerX)/tileDim, (upperY-lowerY)/tileDim, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D gOverlayImg = (Graphics2D) overlayImage.getGraphics();           
                for (int row = 0; row < jTableClassifierResults.getRowCount(); row++) {
                    Boolean visible = (Boolean) jTableClassifierResults.getModel().getValueAt(row, 5);
                    if (visible) {
                        double fMin = (double) model.getValueAt(row, 1);
                        double fMax = (double) model.getValueAt(row, 2);
                        for (Sample s : samples) {
                            double f = s.featureVector[row];
                            Rectangle r = s.tile;
                            if ((f < fMin) || (f > fMax)) {
                                continue;
                            }
                            Color c;
                            if (jRadioButtonColorScaleColor.isSelected()){
                                c = ColormapJet.getColor(fMin, fMax, s.featureVector[row]);
                            } else {
                                c = ColormapGray.getColor(fMin, fMax, s.featureVector[row]);
                            }
                            Rectangle rPaint = new Rectangle((r.x-lowerX)/tileDim, (r.y-lowerY)/tileDim, 1, 1);
                            gOverlayImg.setColor(c);
                            gOverlayImg.fill(rPaint);
                        }
                    }
                }
            }
            g.drawImage(overlayImage, lowerX, lowerY, upperX-lowerX, upperY-lowerY, null);
            
            g.setColor(Color.black);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
            
            Point labelPoint = new Point(upperX, upperY);
            g.getTransform().transform(labelPoint, labelPoint);
            gOrig.drawString(getTitle(), labelPoint.x, labelPoint.y);
            
        }

    }
    
    @Override
    public void addImageViewerPluginListener(ImageViewerPluginListener imageViewerPluginListener) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                imageViewerPluginListener.diapose();
                super.windowClosing(e);
                imageViewer.repaint();
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupDisplayType = new javax.swing.ButtonGroup();
        buttonGroupColorScale = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableClassifierResults = new javax.swing.JTable();
        jSliderOccupacity = new javax.swing.JSlider();
        jPanel1 = new javax.swing.JPanel();
        jRadioButtonSingleFeature = new javax.swing.JRadioButton();
        jRadioButtonMultiFeatureLDA = new javax.swing.JRadioButton();
        jButtonUpdateDisplayMethod = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jRadioButtonColorScaleGray = new javax.swing.JRadioButton();
        jRadioButtonColorScaleColor = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);

        jTableClassifierResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Color", "Visible"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTableClassifierResults);

        jSliderOccupacity.setMaximum(255);
        jSliderOccupacity.setMinimum(1);
        jSliderOccupacity.setToolTipText("");
        jSliderOccupacity.setValue(255);
        jSliderOccupacity.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSliderOccupacityPropertyChange(evt);
            }
        });

        buttonGroupDisplayType.add(jRadioButtonSingleFeature);
        jRadioButtonSingleFeature.setSelected(true);
        jRadioButtonSingleFeature.setText("Single");
        jRadioButtonSingleFeature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonSingleFeatureActionPerformed(evt);
            }
        });

        buttonGroupDisplayType.add(jRadioButtonMultiFeatureLDA);
        jRadioButtonMultiFeatureLDA.setText("LDA");
        jRadioButtonMultiFeatureLDA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMultiFeatureLDAActionPerformed(evt);
            }
        });

        jButtonUpdateDisplayMethod.setText("Update");
        jButtonUpdateDisplayMethod.setEnabled(false);
        jButtonUpdateDisplayMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateDisplayMethodActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonSingleFeature, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonMultiFeatureLDA, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonUpdateDisplayMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButtonSingleFeature)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonMultiFeatureLDA)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonUpdateDisplayMethod)
                .addContainerGap(179, Short.MAX_VALUE))
        );

        jLabel1.setText("Alpha");

        jLabel2.setText("Color scale");

        buttonGroupColorScale.add(jRadioButtonColorScaleGray);
        jRadioButtonColorScaleGray.setText("Gray");
        jRadioButtonColorScaleGray.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonColorScaleGrayActionPerformed(evt);
            }
        });

        buttonGroupColorScale.add(jRadioButtonColorScaleColor);
        jRadioButtonColorScaleColor.setSelected(true);
        jRadioButtonColorScaleColor.setText("Color");
        jRadioButtonColorScaleColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonColorScaleColorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSliderOccupacity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jRadioButtonColorScaleGray)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButtonColorScaleColor)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonColorScaleGray)
                    .addComponent(jRadioButtonColorScaleColor)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSliderOccupacity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderOccupacityPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSliderOccupacityPropertyChange

    }//GEN-LAST:event_jSliderOccupacityPropertyChange

    private void jButtonUpdateDisplayMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateDisplayMethodActionPerformed
        if (jRadioButtonMultiFeatureLDA.isSelected()) {

        }

    }//GEN-LAST:event_jButtonUpdateDisplayMethodActionPerformed

    private void jRadioButtonMultiFeatureLDAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMultiFeatureLDAActionPerformed
        jButtonUpdateDisplayMethod.setEnabled(true);
    }//GEN-LAST:event_jRadioButtonMultiFeatureLDAActionPerformed

    private void jRadioButtonSingleFeatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonSingleFeatureActionPerformed
        jButtonUpdateDisplayMethod.setEnabled(false);
    }//GEN-LAST:event_jRadioButtonSingleFeatureActionPerformed

    private void jRadioButtonColorScaleGrayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonColorScaleGrayActionPerformed
        overlayImage = null;
        imageViewer.repaint();
    }//GEN-LAST:event_jRadioButtonColorScaleGrayActionPerformed

    private void jRadioButtonColorScaleColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonColorScaleColorActionPerformed
        overlayImage = null;
        imageViewer.repaint();
    }//GEN-LAST:event_jRadioButtonColorScaleColorActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupColorScale;
    private javax.swing.ButtonGroup buttonGroupDisplayType;
    private javax.swing.JButton jButtonUpdateDisplayMethod;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButtonColorScaleColor;
    private javax.swing.JRadioButton jRadioButtonColorScaleGray;
    private javax.swing.JRadioButton jRadioButtonMultiFeatureLDA;
    private javax.swing.JRadioButton jRadioButtonSingleFeature;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSliderOccupacity;
    private javax.swing.JTable jTableClassifierResults;
    // End of variables declaration//GEN-END:variables

    @Override
    public void close() {
        dispose();
    }

}
