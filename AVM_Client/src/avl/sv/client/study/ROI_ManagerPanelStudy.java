package avl.sv.client.study;

import avl.sv.client.image.ImageViewer;
import avl.sv.shared.Permissions;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageSourceFile;
import avl.sv.shared.study.AnnotationSet;
import avl.sv.shared.study.TreeTableModelStudy;
import avl.sv.shared.study.ROI;
import avl.sv.shared.study.ROI_Folder;
import avl.sv.shared.study.StudySource;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

public class ROI_ManagerPanelStudy extends ROI_ManagerPanel implements MouseMotionListener {

    protected final ImageReference imageReference;
    protected final boolean canModify;
    protected final ImageViewer imageViewer;
    
    public ROI_ManagerPanelStudy(final ImageViewer imageViewer, StudySource studySource, boolean canModify) {
        this.imageReference = imageViewer.getImageSource().imageReference;
        this.imageViewer = imageViewer;
        this.canModify = canModify;
        imageViewer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                setSize(getPreferredSize().width, imageViewer.getHeight());
            }
        });
        if (imageViewer.getImageSource() instanceof ImageSourceFile) {
            setAllComponentsEnabled(this, false);
        }
        initComponents();
        jTextFieldStudyName.setText(studySource.getName());
        imageViewer.addMouseMotionListener(this);

        if (!canModify) {
            jButtonAddAnnotationSet.setEnabled(false);
            jButtonImport.setEnabled(false);
            jXTreeTableROIs.setEditable(false);
        }

        populateAnnotationSet(studySource);
        studySource.addStudyChangeListener(jXTreeTableROIs);
        setupPopupMenu();
        setVisible(true);
        addMouseMotionListener(jXTreeTableROIs);
    }

    public final void setAllComponentsEnabled(JPanel panel, boolean state) {
        Component[] comps = panel.getComponents();
        for (Component c : comps) {
            c.setEnabled(state);
            if (c instanceof JPanel) {
                setAllComponentsEnabled((JPanel) c, state);
            }
        }
    }

    private void setupPopupMenu() {
        jXTreeTableROIs.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    final TreePath path = jXTreeTableROIs.getPathForLocation(e.getX(), e.getY());
                    if (path == null) {
                        return;
                    }
                    Object selectedItem = path.getLastPathComponent();
                    if (selectedItem instanceof ROI_Folder) {
                        ROI_Folder folder = (ROI_Folder) selectedItem;
                        if (!folder.selected) {
                            jXTreeTableROIs.setSelectionPath(jXTreeTableROIs.getPathForLocation(e.getX(), e.getY()));
                        }
                    }
                    if (selectedItem instanceof ROI) {
                        ROI roi = (ROI) selectedItem;
                        if (!roi.selected) {
                            jXTreeTableROIs.setSelectionPath(jXTreeTableROIs.getPathForLocation(e.getX(), e.getY()));
                        }
                    }
                    jXTreeTableROIs.repaint();
                    JPopupMenu menu = new JPopupMenu();
                    if (selectedItem instanceof ROI) {
                        menu.add(new JMenuItem(new AbstractAction("Goto") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                ROI roi = (ROI) selectedItem;
                                Rectangle bounds = roi.getPolygon().getBounds();
                                imageViewer.setImageRegion(bounds);
                            }
                        }));
                    }
                    if (canModify) {
                        menu.add(new JMenuItem(new AbstractAction("Delete") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                int[] rows = jXTreeTableROIs.getSelectedRows();
                                Object objs[] = new Object[rows.length];
                                for (int rIdx = 0; rIdx < rows.length; rIdx++) {
                                    objs[rIdx] = jXTreeTableROIs.getPathForRow(rows[rIdx]).getLastPathComponent();
                                }
                                for (Object obj : objs) {
                                    if (obj instanceof ROI_Folder) {
                                        ROI_Folder anno = (ROI_Folder) obj;
                                        
                                        if (anno.equals(jXTreeTableROIs.lastSelectedROI_Folder)) {
                                            jXTreeTableROIs.lastSelectedROI_Folder = null;
                                        }
                                        jXTreeTableROIs.removeROI_Folder(anno);
                                    } else if (obj instanceof ROI) {
                                        ROI roi = (ROI) obj;
                                        jXTreeTableROIs.removeROI(roi);
                                    }
                                }
                                jXTreeTableROIs.updateUI();
                                imageViewer.repaint();
                                repaint();
                            }
                        }));
                        if (selectedItem instanceof ROI_Folder) {
                            final ROI_Folder folder = (ROI_Folder) selectedItem;
                            menu.add(new JMenuItem(new AbstractAction("Line Color") {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    Color result = JColorChooser.showDialog(null, "Select folder color", folder.getLineColor());
                                    if (result != null) {
                                        folder.setLineColor(result, false);
                                    }
                                }
                            }));
                            final JCheckBoxMenuItem visible = new JCheckBoxMenuItem(new AbstractAction("Visible") {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    folder.visible = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                                    imageViewer.repaint();
                                }
                            });
                            visible.setSelected(folder.visible);
                            menu.add(visible);
                        }
                    }

                    JMenuItem export = new JMenuItem("Export");
                    export.addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            int[] rows = jXTreeTableROIs.getSelectedRows();
                            Object objs[] = new Object[rows.length];
                            for (int i = 0; i < rows.length; i++) {
                                objs[i] = jXTreeTableROIs.getPathForRow(rows[i]).getLastPathComponent();
                            }
                            ExportDialogROIs exportDialog = new ExportDialogROIs(null, true, imageViewer.getImageSource());
                            exportDialog.promptForExport(objs);
                        }
                    });
                    menu.add(export);

                    menu.show(jXTreeTableROIs, e.getX(), e.getY());
                }
            }
        });
    }



    private void populateAnnotationSet(final StudySource studySource) {
        final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    TreeTableModelStudy annoSetModel = studySource.getAnnotationSetModel(imageReference);
                    if (annoSetModel == null) {
                        JOptionPane.showMessageDialog(null, "Failed to load annotations", "Error", JOptionPane.ERROR_MESSAGE);
                        throw new NullPointerException("Failed to get annotationSet");
                    }
                    jXTreeTableROIs.setModel(annoSetModel);
                    imageViewer.repaint();
                    annoSetModel.addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            imageViewer.repaint();
                        }
                    });
                    Permissions p = studySource.getPermissions();
                    jXTreeTableROIs.setEditable(p.canModify());
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "", ex);
                }
            }
        };
        worker.submit(task);
    }

    protected Timer popupTimer = new Timer(250, new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(true);
            requestFocus();
        }
    });

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldStudyName = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jButtonAddAnnotationSet = new javax.swing.JButton();
        jButtonImport = new javax.swing.JButton();
        jButtonExport = new javax.swing.JButton();
        jPanelAddRoi = new javax.swing.JPanel();
        jCheckBoxRegionHighlight = new javax.swing.JCheckBox();
        jCheckBoxShowSelectedMarkers = new javax.swing.JCheckBox();
        jCheckBoxShowAllMarkers = new javax.swing.JCheckBox();
        jToggleButtonVisablePin = new javax.swing.JToggleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTreeTableROIs = new avl.sv.client.study.ROI_TreeTable_Study();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                formMouseExited(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        jLabel1.setText("Selected Study");

        jTextFieldStudyName.setEditable(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Annotations"));

        jButtonAddAnnotationSet.setText("New Folder");
        jButtonAddAnnotationSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddAnnotationSetActionPerformed(evt);
            }
        });

        jButtonImport.setText("Import");
        jButtonImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImportActionPerformed(evt);
            }
        });

        jButtonExport.setText("Export");
        jButtonExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButtonAddAnnotationSet, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonImport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonExport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jButtonAddAnnotationSet)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonImport)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonExport))
        );

        jCheckBoxRegionHighlight.setSelected(true);
        jCheckBoxRegionHighlight.setText("Region Highlight");
        jCheckBoxRegionHighlight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxRegionHighlightActionPerformed(evt);
            }
        });

        jCheckBoxShowSelectedMarkers.setSelected(true);
        jCheckBoxShowSelectedMarkers.setText("Show Selected Markers");
        jCheckBoxShowSelectedMarkers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxShowSelectedMarkersActionPerformed(evt);
            }
        });

        jCheckBoxShowAllMarkers.setText("Show All Markers");
        jCheckBoxShowAllMarkers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxShowAllMarkersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelAddRoiLayout = new javax.swing.GroupLayout(jPanelAddRoi);
        jPanelAddRoi.setLayout(jPanelAddRoiLayout);
        jPanelAddRoiLayout.setHorizontalGroup(
            jPanelAddRoiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAddRoiLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelAddRoiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxShowAllMarkers)
                    .addComponent(jCheckBoxShowSelectedMarkers)
                    .addComponent(jCheckBoxRegionHighlight)))
        );
        jPanelAddRoiLayout.setVerticalGroup(
            jPanelAddRoiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAddRoiLayout.createSequentialGroup()
                .addComponent(jCheckBoxRegionHighlight)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxShowSelectedMarkers)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxShowAllMarkers))
        );

        jToggleButtonVisablePin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avl/sv/client/icon/pin.png"))); // NOI18N
        jToggleButtonVisablePin.setSelected(true);
        jToggleButtonVisablePin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonVisablePinActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(jXTreeTableROIs);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldStudyName))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanelAddRoi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButtonVisablePin, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldStudyName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addComponent(jToggleButtonVisablePin, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelAddRoi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxShowAllMarkersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxShowAllMarkersActionPerformed
        this.jXTreeTableROIs.showAllMarkers = (((JCheckBox) evt.getSource()).isSelected());
        imageViewer.repaint();
    }//GEN-LAST:event_jCheckBoxShowAllMarkersActionPerformed

    private void jCheckBoxShowSelectedMarkersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxShowSelectedMarkersActionPerformed
        jXTreeTableROIs.showSelectedMarkers = (((JCheckBox) evt.getSource()).isSelected());
        imageViewer.repaint();
    }//GEN-LAST:event_jCheckBoxShowSelectedMarkersActionPerformed

    private void jCheckBoxRegionHighlightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxRegionHighlightActionPerformed
        jXTreeTableROIs.highlightRegions = (((JCheckBox) evt.getSource()).isSelected());
        imageViewer.repaint();
    }//GEN-LAST:event_jCheckBoxRegionHighlightActionPerformed

    private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportActionPerformed
        jXTreeTableROIs.exportROIs();
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void jButtonImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImportActionPerformed
        jXTreeTableROIs.importROIs();
    }//GEN-LAST:event_jButtonImportActionPerformed

    private void jButtonAddAnnotationSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddAnnotationSetActionPerformed
        ROI_Folder anno = ROI_Folder.createDefault();
        jXTreeTableROIs.addROI_Folder(anno);
    }//GEN-LAST:event_jButtonAddAnnotationSetActionPerformed

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown

    }//GEN-LAST:event_formComponentShown

    private void formMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseExited
        popupTimer.stop();
    }//GEN-LAST:event_formMouseExited

    private void jToggleButtonVisablePinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonVisablePinActionPerformed
        if (!jToggleButtonVisablePin.isSelected()) {
            setVisible(false);
        }
    }//GEN-LAST:event_jToggleButtonVisablePinActionPerformed

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getSource().equals(imageViewer)) {
            if (jToggleButtonVisablePin.isSelected()) {
                setVisible(true);
            } else {
                Point p = e.getPoint();
                if (isVisible()) {
                    if (p.x > getWidth()) {
                        popupTimer.stop();
                        setVisible(false);
                    }
                } else {
                    if (p.x < imageViewer.getROI_PanelIndicatorWidth()) {
                        popupTimer.setRepeats(false);
                        popupTimer.restart();
                        popupTimer.start();
                    } else {
                        popupTimer.stop();
                    }
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddAnnotationSet;
    private javax.swing.JButton jButtonExport;
    private javax.swing.JButton jButtonImport;
    private javax.swing.JCheckBox jCheckBoxRegionHighlight;
    private javax.swing.JCheckBox jCheckBoxShowAllMarkers;
    private javax.swing.JCheckBox jCheckBoxShowSelectedMarkers;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelAddRoi;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldStudyName;
    private javax.swing.JToggleButton jToggleButtonVisablePin;
    private avl.sv.client.study.ROI_TreeTable_Study jXTreeTableROIs;
    // End of variables declaration//GEN-END:variables
    @Override
    public ROI_TreeTable getROI_TreeTable() {
        return jXTreeTableROIs;
    }
}
