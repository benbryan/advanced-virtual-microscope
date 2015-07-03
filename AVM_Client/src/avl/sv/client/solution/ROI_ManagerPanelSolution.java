package avl.sv.client.solution;

import avl.sv.shared.study.TreeTableModelStudy;
import avl.sv.client.study.*;
import avl.sv.client.study.ExportDialogROIs;
import avl.sv.client.image.ImageViewer;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.study.ROI_Folder;
import avl.sv.shared.study.ROI;
import avl.sv.shared.image.ImageSourceFile;
import avl.sv.shared.study.AnnotationSet;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
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
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;

public class ROI_ManagerPanelSolution extends ROI_ManagerPanel implements MouseMotionListener {

    protected final ImageReference imageReference;
    protected final boolean canModify;
    protected final ImageViewer imageViewer;
    
    private final SolutionManager solutionManager;

    public ROI_ManagerPanelSolution(final ImageViewer imageViewer, StudySource studySource, SolutionManager solutionManager, boolean canModify) {
        this.imageReference = imageViewer.getImageSource().imageReference;
        this.imageViewer = imageViewer;
        this.canModify = canModify;
        this.solutionManager = solutionManager;
        initComponents();
        imageViewer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                setSize(getPreferredSize().width, imageViewer.getHeight());
            }
        });
        if (imageViewer.getImageSource() instanceof ImageSourceFile) {
            setAllComponentsEnabled(this, false);
        }
        
        jTextFieldStudyName.setText(studySource.getName());
        imageViewer.addMouseMotionListener(this);

        if (!canModify) {
            jXTreeTableROIs.setEditable(false);
            jButtonAddAnnotationSet1.setEnabled(false);
        }

        populateAnnotationSet(studySource);
        setupPopupMenu();
        studySource.addStudyChangeListener(jXTreeTableROIs);
        setVisible(true);
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

    public boolean isShowTilesChecked() {
        return jCheckBoxShowTiles.isSelected();
    }
        
    protected Timer popupTimer = new Timer(250, new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(true);
            requestFocus();
        }
    });

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

    @Override
    public void mouseDragged(MouseEvent e) {
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
                    JPopupMenu menu = new JPopupMenu();
                    final Object node = path.getLastPathComponent();
                    if (node instanceof ROI) {
                        final ROI roi = (ROI) node;
                        JMenuItem gotoROI = new JMenuItem("Goto");
                        gotoROI.addActionListener(new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Rectangle bounds = roi.getPolygon().getBounds();
                                imageViewer.setImageRegion(bounds);
                            }
                        });
                        menu.add(gotoROI);
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
                                        if (obj instanceof ROI) {
                                            ROI roi = (ROI) obj;
                                            jXTreeTableROIs.removeROI(roi);
                                        }
                                    }
                                    jXTreeTableROIs.updateUI();
                                    imageViewer.repaint();
                                    repaint();
                                    solutionManager.updateButtons(imageViewer);
                                }
                            }));
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

                    if (node instanceof ROI_Folder) {
                        final ROI_Folder folder = (ROI_Folder) node;
                        menu.add(new JMenuItem(new AbstractAction("Line Color") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Color result = JColorChooser.showDialog(null, "Select folder color", folder.getLineColor());
                                if (result != null) {
                                    folder.setLineColor(result, false);
                                }
                            }
                        }));

                        if (canModify) {
                            menu.add(new JMenuItem(new AbstractAction("Remove") {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (!solutionManager.getSolutionSource().getSolution().getClassifierClassNames().containsKey(folder.id)) {
                                        jXTreeTableROIs.removeROI_Folder(folder);
                                    } else {
                                        solutionManager.promptToRemoveClass(folder.getName());
                                    }
                                }
                            }));

                            menu.add(new JMenuItem(new AbstractAction("Rename") {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    solutionManager.promptToRenameClass(folder.getName());
                                }
                            }));

                            final JCheckBoxMenuItem visible = new JCheckBoxMenuItem(new AbstractAction("Visible") {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                                    folder.visible = item.isSelected();
                                    imageViewer.repaint();
                                }
                            });
                            visible.setSelected(folder.visible);
                            menu.add(visible);
                        }
                    }
                    menu.show(jXTreeTableROIs, e.getX(), e.getY());
                }
            }
        });
    }

    private void populateAnnotationSet(StudySource studySource) {
        final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new Runnable() {
            public void run() {
                try {
                    TreeTableModelStudy annoSetModel = studySource.getAnnotationSetModel(imageReference);
                    jXTreeTableROIs.setModel(annoSetModel);
                    AnnotationSet annoSet = annoSetModel.getAnnotationSet();
                    if (annoSet == null) {
                        JOptionPane.showMessageDialog(null, "Failed to load annotations", "Error", JOptionPane.ERROR_MESSAGE);
                        throw new NullPointerException("Failed to get annotationSet");
                    }
                    ArrayList<ROI_Folder> classFoldersFound = new ArrayList<>();
                    
                    // Make sure class folders exist in the annotationSet
                    for (Map.Entry<Long, String> classEntry : solutionManager.getSolutionSource().getSolution().getClassifierClassNames().entrySet()) {
                        ROI_Folder foundFolder = null;
                        for (ROI_Folder folder : annoSet.getROI_Folders()) {
                            if (folder.id == classEntry.getKey()) {
                                if (foundFolder == null) {
                                    if (folder.getName() != classEntry.getValue()) {
                                        folder.setName(classEntry.getValue(), false);
                                    }
                                    foundFolder = folder;
                                    classFoldersFound.add(folder);
                                } else {
                                    for (ROI roi : folder.getROIs()) {
                                        foundFolder.add(roi, false);
                                    }
                                    annoSet.remove(folder, false);
                                }
                            }
                        }
                        if (foundFolder == null) {
                            // If a folder was not found for this class then add one
                            ROI_Folder folder = ROI_Folder.createDefault();
                            folder.id = classEntry.getKey();
                            folder.setName(classEntry.getValue(), true);
                            annoSet.add(folder, false);
                            classFoldersFound.add(folder);
                        }
                    }
                    for (ROI_Folder folder: annoSet.getROI_Folders()){
                        boolean classIdFound = false;
                        if (folder.getName().equals(MessageStrings.Temporary)){
                            continue;
                        }
                        for (Long classKey:solutionManager.getSolutionSource().getSolution().getClassifierClassNames().keySet()){
                            if (classKey.longValue() == folder.id){
                                classIdFound = true;
                                break;
                            }
                        }
                        if (!classIdFound){
                            if (folder.getROIs().isEmpty()){
                                annoSet.remove(folder, false);
                            } else {
                                folder.setName("Unknown", true);
                                annoSet.add(folder, false);
                                for (ROI roi:folder.getROIs()){
                                    folder.add(roi, false);
                                }
                            }
                        }
                    }
                    
                    // if annoset does not contain temporary folder then add one
                    ROI_Folder tempFolder = null;
                    for (ROI_Folder folder : annoSet.getROI_Folders()) {
                        if (folder.getName().equals(MessageStrings.Temporary)) {
                            tempFolder = folder;
                            break;
                        }
                    }
                    if (tempFolder == null) {
                        tempFolder = ROI_Folder.createDefault();
                        tempFolder.setName(MessageStrings.Temporary, true);
                        annoSetModel.insertNodeInto(tempFolder, annoSet, annoSetModel.getChildCount(annoSet));
                    }
                    
                    jXTreeTableROIs.lastSelectedROI_Folder = tempFolder;
                    jXTreeTableROIs.setSelectionPath(new TreePath(new Object[]{annoSetModel.getRoot(), tempFolder}));
                    annoSetModel.addPropertyChangeListener((PropertyChangeEvent evt) -> {
                        imageViewer.repaint();
                    });
                    imageViewer.repaint();
                    
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "", ex);
                }
            }
        };
        worker.submit(task);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextFieldStudyName = new javax.swing.JTextField();
        jPanelAddRoi = new javax.swing.JPanel();
        jCheckBoxRegionHighlight = new javax.swing.JCheckBox();
        jCheckBoxShowSelectedMarkers = new javax.swing.JCheckBox();
        jCheckBoxShowAllMarkers = new javax.swing.JCheckBox();
        jCheckBoxShowTiles = new javax.swing.JCheckBox();
        jToggleButtonVisablePin = new javax.swing.JToggleButton();
        jPanel3 = new javax.swing.JPanel();
        jButtonAddAnnotationSet1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTreeTableROIs = new avl.sv.client.solution.ROI_TreeTable_Solution();

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

        jLabel1.setText("Solution name: ");

        jTextFieldStudyName.setEditable(false);

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

        jCheckBoxShowTiles.setText("Show Tiles");
        jCheckBoxShowTiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxShowTilesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelAddRoiLayout = new javax.swing.GroupLayout(jPanelAddRoi);
        jPanelAddRoi.setLayout(jPanelAddRoiLayout);
        jPanelAddRoiLayout.setHorizontalGroup(
            jPanelAddRoiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAddRoiLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelAddRoiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxShowSelectedMarkers)
                    .addComponent(jCheckBoxRegionHighlight)
                    .addComponent(jCheckBoxShowAllMarkers)
                    .addComponent(jCheckBoxShowTiles))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelAddRoiLayout.setVerticalGroup(
            jPanelAddRoiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAddRoiLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxRegionHighlight)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxShowSelectedMarkers)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxShowAllMarkers)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxShowTiles)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jToggleButtonVisablePin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avl/sv/client/icon/pin.png"))); // NOI18N
        jToggleButtonVisablePin.setSelected(true);
        jToggleButtonVisablePin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonVisablePinActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Class"));

        jButtonAddAnnotationSet1.setText("Add");
        jButtonAddAnnotationSet1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddAnnotationSet1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButtonAddAnnotationSet1, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jButtonAddAnnotationSet1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jXTreeTableROIs.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jXTreeTableROIsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jXTreeTableROIs);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldStudyName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButtonVisablePin, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanelAddRoi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelAddRoi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jXTreeTableROIsValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jXTreeTableROIsValueChanged
        jXTreeTableROIs.updateSelectedROIs();
        solutionManager.updateButtons(imageViewer);
        imageViewer.repaint();
    }//GEN-LAST:event_jXTreeTableROIsValueChanged

    private void jCheckBoxShowAllMarkersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxShowAllMarkersActionPerformed
        jXTreeTableROIs.showAllMarkers = (((JCheckBox) evt.getSource()).isSelected());
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

    private void jButtonAddAnnotationSet1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddAnnotationSet1ActionPerformed
        solutionManager.promptForNewClass();
    }//GEN-LAST:event_jButtonAddAnnotationSet1ActionPerformed

    private void jCheckBoxShowTilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxShowTilesActionPerformed
        imageViewer.repaint();
    }//GEN-LAST:event_jCheckBoxShowTilesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddAnnotationSet1;
    private javax.swing.JCheckBox jCheckBoxRegionHighlight;
    private javax.swing.JCheckBox jCheckBoxShowAllMarkers;
    private javax.swing.JCheckBox jCheckBoxShowSelectedMarkers;
    private javax.swing.JCheckBox jCheckBoxShowTiles;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAddRoi;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldStudyName;
    private javax.swing.JToggleButton jToggleButtonVisablePin;
    private avl.sv.client.solution.ROI_TreeTable_Solution jXTreeTableROIs;
    // End of variables declaration//GEN-END:variables

    @Override
    public ROI_TreeTable getROI_TreeTable() {
        return jXTreeTableROIs;
    }

}
