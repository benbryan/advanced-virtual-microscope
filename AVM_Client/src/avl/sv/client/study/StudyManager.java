package avl.sv.client.study;

import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.client.image.ImageViewer;
import avl.sv.client.tools.MouseActionLogger;
import avl.sv.client.SearchableSelector;
import avl.sv.shared.AVM_Source;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageManagerSet;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageSource;
import avl.sv.shared.image.ImagesSource;
import avl.sv.shared.study.ROI;
import avl.sv.shared.study.StudyChangeEvent;
import avl.sv.shared.study.StudyChangeListener;
import avl.sv.shared.study.StudySource;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DropMode;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class StudyManager extends javax.swing.JFrame {

    int cornerMarkerDim = 10;
    MouseActionLogger mouseAction = new MouseActionLogger();
    final StudySource studySource;
    final ImagesSource imagesSource;
    ImageViewer imageViewerLastSelected = null;
    private final DefaultTreeModel jTreeStudyModel;
    private final boolean canModify;
    private final String username;
    final private HashMap<ImageViewer, JFrame> imageViewers = new HashMap<>();

    public StudyManager(String username, StudySource studySource) {
        initComponents();
        this.imagesSource = studySource.getImagesSource();
        this.studySource = studySource;
        this.username = username;
        jTreeStudy.setDragEnabled(true);
        jTreeStudy.setDropMode(DropMode.ON_OR_INSERT);
        jTreeStudy.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        boolean canModifyTemp = false;
        try {
            canModifyTemp = studySource.getPermissions(username).canModify();
        } catch (PermissionDenied ex) {
            canModifyTemp = false;
        } finally {
            canModify = canModifyTemp;
        }

        if (!canModify) {
            jMenuItemAddImages.setVisible(false);
            jMenuItemDeleteStudy.setVisible(false);
            jMenuItemDeleteImage.setVisible(false);
            jMenuItemRemoveUnusedImages.setVisible(false);
            jTextAreaStudyDescription.setEditable(false);
            jTextFieldStudyName.setEditable(false);
        }

        studySource.updateImageSets();

        jTreeStudyModel = new DefaultTreeModel(studySource);
        jTreeStudy.setModel(jTreeStudyModel);
        jTextAreaStudyDescription.setText(studySource.getDescription());
        jTextFieldStudyName.setText(studySource.getName());
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                studySource.close();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                imageViewers.keySet().stream().forEach((imageViewer) -> {
                    AdvancedVirtualMicroscope.closeImageViewer(imageViewer);
                });
            }
        });
//        studySource.addStudyChangeListener(new StudyChangeListener() {
//            @Override
//            public void studyChanged(StudyChangeEvent event) {
//                for (ImageViewer imageViewer:imageViewers.keySet()){
//                    if (imageViewer.getImageSource().imageReference.equals(event.imageReference)){
//                        imageViewer.repaint();
//                    }
//                }
//            }
//        });
    }

    private void openImage(final ImageManager imageManager) {
        for (Map.Entry<ImageViewer, JFrame> entry : imageViewers.entrySet()) {
            if (entry.getKey().getImageSource().imageReference.equals(imageManager.imageReference)) {
                JFrame frame = entry.getValue();
                frame.setVisible(true);
                frame.toFront();
                return;
            }
        }
        final ImageSource imageSource = imagesSource.createImageSource(imageManager);
        final ImageViewer imageViewer = new ImageViewer(imageSource);
        imageViewer.readOnly = !studySource.getPermissions().canModify();
        ROI_ManagerPanelStudy roiManager = new ROI_ManagerPanelStudy(imageViewer, studySource, canModify);
        imageViewer.setROI_ManagerPanel(roiManager);
        JFrame frame = AdvancedVirtualMicroscope.addImageViewer(imageViewer);
        imageViewers.put(imageViewer, frame);
        frame.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                imageViewerLastSelected = imageViewer;
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                AdvancedVirtualMicroscope.closeImageViewer(imageViewer);
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenuJTree = new javax.swing.JPopupMenu();
        jMenuItemDeleteImage = new javax.swing.JMenuItem();
        jMenuItemOpenImage = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeStudy = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaStudyDescription = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldStudyName = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemAddImages = new javax.swing.JMenuItem();
        jMenuItemDeleteStudy = new javax.swing.JMenuItem();
        jMenuItemCloneStudy = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItemRemoveUnusedImages = new javax.swing.JMenuItem();
        jMenuItemExportAllROIs = new javax.swing.JMenuItem();
        jMenuItemReferenceImage = new javax.swing.JMenuItem();

        jPopupMenuJTree.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                jPopupMenuJTreePopupMenuWillBecomeVisible(evt);
            }
        });

        jMenuItemDeleteImage.setText("Delete");
        jMenuItemDeleteImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteImageActionPerformed(evt);
            }
        });
        jPopupMenuJTree.add(jMenuItemDeleteImage);

        jMenuItemOpenImage.setText("Open");
        jMenuItemOpenImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOpenImageActionPerformed(evt);
            }
        });
        jPopupMenuJTree.add(jMenuItemOpenImage);

        setTitle("Study Manager");
        setLocationByPlatform(true);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTreeStudy.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTreeStudy.setComponentPopupMenu(jPopupMenuJTree);
        jTreeStudy.setDragEnabled(true);
        jTreeStudy.setDropMode(javax.swing.DropMode.INSERT);
        jTreeStudy.setEditable(true);
        jTreeStudy.setRootVisible(false);
        jTreeStudy.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTreeStudyMouseClicked(evt);
            }
        });
        jTreeStudy.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeStudyValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jTreeStudy);

        jTextAreaStudyDescription.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextAreaStudyDescriptionKeyTyped(evt);
            }
        });
        jScrollPane3.setViewportView(jTextAreaStudyDescription);

        jLabel5.setText("Description");

        jLabel6.setText("Slides in selected study");

        jLabel7.setText("Selected Study");

        jTextFieldStudyName.setEditable(false);
        jTextFieldStudyName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldStudyNameKeyTyped(evt);
            }
        });

        jMenu1.setText("Study");

        jMenuItemAddImages.setText("Add Images");
        jMenuItemAddImages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddImagesActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemAddImages);

        jMenuItemDeleteStudy.setText("Delete");
        jMenuItemDeleteStudy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteStudyActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemDeleteStudy);

        jMenuItemCloneStudy.setText("Clone");
        jMenuItemCloneStudy.setToolTipText("This is a good way to backup your work");
        jMenuItemCloneStudy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCloneStudyActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemCloneStudy);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Functions");

        jMenuItemRemoveUnusedImages.setText("Remove unused images");
        jMenuItemRemoveUnusedImages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRemoveUnusedImagesActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemRemoveUnusedImages);

        jMenuItemExportAllROIs.setText("Export all ROIs");
        jMenuItemExportAllROIs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportAllROIsActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemExportAllROIs);

        jMenuItemReferenceImage.setText("Locate Image");
        jMenuItemReferenceImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemReferenceImageActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemReferenceImage);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addComponent(jScrollPane3)
            .addComponent(jTextFieldStudyName)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldStudyName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                .addGap(29, 29, 29))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTreeStudyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTreeStudyMouseClicked
        if ((evt.getClickCount() == 2) && (evt.getButton() == MouseEvent.BUTTON1)) {
            TreePath path = jTreeStudy.getClosestPathForLocation(evt.getPoint().x, evt.getPoint().y);
            if (path == null) {
                return;
            }
            Object obj = path.getLastPathComponent();
            if (obj instanceof ImageManager) {
                openImage((ImageManager) obj);
            }
        }
    }//GEN-LAST:event_jTreeStudyMouseClicked

    private void jTreeStudyValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTreeStudyValueChanged

    }//GEN-LAST:event_jTreeStudyValueChanged

    public void showAddImagesPrompt() {
            final SearchableSelector imageSelector = new SearchableSelector("Select Image", "Add") {
                @Override
                public void doubleClicked(ArrayList<AVM_Source> selected) {
                    selected(selected);
                }

                @Override
                public void buttonPressed(ArrayList<AVM_Source> selected) {
                    selected(selected);
                }

                public void selected(ArrayList<AVM_Source> sources) {
                    ArrayList<ImageManager> existingImageManagers = studySource.getAllImageManagers();
                    ArrayList<ImageManager> imageManagersAdded = new ArrayList<>();
                    for (AVM_Source source : sources) {
                        if (source instanceof ImageManager) {
                            ImageManager imageManager = (ImageManager) source;
                            if (!existingImageManagers.contains(imageManager)) {
                                try {
                                    imageManager = (ImageManager) imageManager.clone();
                                    studySource.addImage((ImageManager) imageManager);
                                    imageManagersAdded.add(imageManager);
                                } catch (PermissionDenied ex) {
                                    Logger.getLogger(StudyManager.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        }
                    };
                    jTreeStudyModel.reload();
                    jTreeStudy.setExpandsSelectedPaths(true);
                    ArrayList<TreePath> paths = new ArrayList<>();
                    for (ImageManager imageManager : imageManagersAdded) {
                        TreePath path = new TreePath(jTreeStudyModel.getPathToRoot(imageManager));
                        paths.add(path);
                        jTreeStudy.expandPath(path.getParentPath());
                    }
                    jTreeStudy.setSelectionPaths(paths.toArray(new TreePath[paths.size()]));
                }

                @Override
                public ArrayList<AVM_Source> getSelectables() {
                    ArrayList<AVM_Source> selectables = new ArrayList<>();
                    selectables.addAll(imagesSource.getImageSets());
                    return selectables;
                }
            };
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    imageSelector.dispose();
                }
            });
            imageSelector.setTitle("Select images to add");
            imageSelector.setVisible(true);
  
    }

    private void jPopupMenuJTreePopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jPopupMenuJTreePopupMenuWillBecomeVisible

    }//GEN-LAST:event_jPopupMenuJTreePopupMenuWillBecomeVisible

    private void jMenuItemDeleteImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteImageActionPerformed
        try {
            TreePath[] paths = jTreeStudy.getSelectionPaths();
            for (TreePath path : paths) {
                Object obj = path.getLastPathComponent();
                if (obj == null) {
                    continue;
                }
                if (obj instanceof ImageManager) {
                    ImageManager imageManager = (ImageManager) obj;
                    StudySource ss = (StudySource) imageManager.getParent().getParent();
                    ss.removeImage(imageManager.imageReference);
                    closeImage(imageManager.imageReference);
                    jTreeStudy.updateUI();
                }
                if (obj instanceof ImageManagerSet) {
                    ImageManagerSet imageManagerSet = (ImageManagerSet) obj;
                    for (ImageManager imageManager : imageManagerSet.getImageManagerSet()) {

                        StudySource ss = (StudySource) imageManager.getParent().getParent();
                        closeImage(imageManager.imageReference);
                        ss.removeImage(imageManager.imageReference);

                    }
                    jTreeStudyModel.removeNodeFromParent(imageManagerSet);
                    jTreeStudy.updateUI();
                }
            }
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        ImageViewer iv = imageViewerLastSelected;
        if (iv != null) {
            iv.repaint();
        }

    }//GEN-LAST:event_jMenuItemDeleteImageActionPerformed

    private void closeImage(ImageReference imageReference) {
        ArrayList<ImageViewer> toClose = new ArrayList<>();
        for (Map.Entry<ImageViewer, JFrame> entry : imageViewers.entrySet()) {
            ImageViewer imageViewer = entry.getKey();
            if (imageViewer.getImageSource().imageReference.equals(imageReference)) {
                toClose.add(imageViewer);
            }
        }
        for (ImageViewer imageViewer : toClose) {
            imageViewers.remove(imageViewer).dispose();
        };
    }

    private void jMenuItemOpenImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOpenImageActionPerformed
        TreePath[] paths = jTreeStudy.getSelectionPaths();
        if (paths == null) {
            return;
        }
        for (TreePath path : paths) {
            Object obj = path.getLastPathComponent();
            if (obj instanceof ImageManager) {
                openImage((ImageManager) obj);
            }
        }
    }//GEN-LAST:event_jMenuItemOpenImageActionPerformed

    private void jMenuItemAddImagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddImagesActionPerformed
        showAddImagesPrompt();
    }//GEN-LAST:event_jMenuItemAddImagesActionPerformed

    private void jMenuItemDeleteStudyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteStudyActionPerformed
        String options[] = new String[]{"Delete", "Cancel"};
        int selectedOption = JOptionPane.showOptionDialog(this, "Permanently delete this study?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
        final String studyName = studySource.getName();
        if (selectedOption == 0) {
            String result;
            try {
                result = studySource.delete();
                if (result.startsWith("error:")) {
                    AdvancedVirtualMicroscope.setStatusText("Could not delete study " + studyName + ". " + result, 4 * 1000);
                } else {
                    AdvancedVirtualMicroscope.setStatusText("Study " + studyName + " was deleted", 4 * 1000);
                    dispose();
                }
            } catch (PermissionDenied ex) {
                AdvancedVirtualMicroscope.setStatusText("Could not delete study " + studyName + ". " + "Permission Denied", 4 * 1000);
            }
        }
    }//GEN-LAST:event_jMenuItemDeleteStudyActionPerformed

    private void jMenuItemCloneStudyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCloneStudyActionPerformed
        String result = JOptionPane.showInputDialog("Input cloned study's name", studySource.getName() + "_clone");
        if (result != null) {
            StudySource ss = studySource.cloneStudy(result, null);
            if (ss != null) {
                StudyManager sm = new StudyManager(username, ss);
                AdvancedVirtualMicroscope.addWindow(sm, "Study " + sm.getName());
                sm.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to clone this study");
            }
        }
    }//GEN-LAST:event_jMenuItemCloneStudyActionPerformed

    private void closeImageViewer(ImageReference imageReference) {
        for (Map.Entry<ImageViewer, JFrame> entry : imageViewers.entrySet()) {
            ImageViewer imageViewer = entry.getKey();
            JFrame f = entry.getValue();
            if (imageViewer.getImageSource().imageReference.equals(imageReference)) {
                imageViewer.close();
                f.dispose();
                return;
            }
        }
    }

    private void jMenuItemRemoveUnusedImagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRemoveUnusedImagesActionPerformed
        ArrayList<ImageReference> imagesRemoved = studySource.removeUnusedImages();
        for (ImageReference imageRemoved:imagesRemoved){
            closeImageViewer(imageRemoved);
        }
        jTreeStudy.repaint();
    }//GEN-LAST:event_jMenuItemRemoveUnusedImagesActionPerformed

    private void jMenuItemExportAllROIsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportAllROIsActionPerformed
        new ExportDialogROIsFullStudy(this, true, studySource).setVisible(true);
    }//GEN-LAST:event_jMenuItemExportAllROIsActionPerformed

    private void jMenuItemReferenceImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemReferenceImageActionPerformed
//        ImageSource imageSource;
//        imageSource.exportROI(null, null, TOP_ALIGNMENT)
    }//GEN-LAST:event_jMenuItemReferenceImageActionPerformed

    private void jTextAreaStudyDescriptionKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextAreaStudyDescriptionKeyTyped
        try {
            jTextAreaStudyDescription.setText(studySource.setDescription(jTextAreaStudyDescription.getText()));
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jTextAreaStudyDescriptionKeyTyped

    private void jTextFieldStudyNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldStudyNameKeyTyped
        try {
            jTextFieldStudyName.setText(studySource.setName(jTextFieldStudyName.getText()));
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jTextFieldStudyNameKeyTyped

    @Override
    public void dispose() {
        studySource.close();
        super.dispose(); //To change body of generated methods, choose Tools | Templates.
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItemAddImages;
    private javax.swing.JMenuItem jMenuItemCloneStudy;
    private javax.swing.JMenuItem jMenuItemDeleteImage;
    private javax.swing.JMenuItem jMenuItemDeleteStudy;
    private javax.swing.JMenuItem jMenuItemExportAllROIs;
    private javax.swing.JMenuItem jMenuItemOpenImage;
    private javax.swing.JMenuItem jMenuItemReferenceImage;
    private javax.swing.JMenuItem jMenuItemRemoveUnusedImages;
    private javax.swing.JPopupMenu jPopupMenuJTree;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea jTextAreaStudyDescription;
    private javax.swing.JTextField jTextFieldStudyName;
    private javax.swing.JTree jTreeStudy;
    // End of variables declaration//GEN-END:variables

}
