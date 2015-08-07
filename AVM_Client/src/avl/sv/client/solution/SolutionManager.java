package avl.sv.client.solution;

import avl.sv.client.fileFilters.ARFF_Filter;
import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.client.image.ImageViewer;
import avl.sv.client.fileFilters.MODEL_Filter;
import avl.sv.client.tools.MouseActionLogger;
import avl.sv.shared.ProgressBarForegroundPainter;
import avl.sv.client.SearchableSelector;
import avl.sv.client.image.ImageViewerPlugin;
import avl.sv.client.image.ImageViewerPluginListener;
import avl.sv.client.study.ExportDialogROIsFullStudy;
import avl.sv.shared.study.ROI_Folder;
import avl.sv.shared.study.AnnotationSet;
import avl.sv.shared.study.ROI;
import avl.sv.shared.AVM_ProgressMonitor;
import avl.sv.shared.AVM_Source;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.solution.Sample;
import avl.sv.shared.solution.SampleSetClass;
import avl.sv.shared.solution.SampleSetImage;
import avl.sv.shared.solution.Solution;
import avl.sv.shared.model.featureGenerator.AbstractFeatureGenerator;
import avl.sv.shared.Permissions;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageManagerSet;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageSource;
import avl.sv.shared.image.ImagesSource;
import avl.sv.shared.model.classifier.ClassifierWeka;
import avl.sv.shared.solution.SolutionChangeEvent;
import avl.sv.shared.solution.SolutionChangeListener;
import avl.sv.shared.solution.SolutionSource;
import avl.sv.shared.solution.xml.SolutionXML_Parser;
import avl.sv.shared.study.StudySource;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DropMode;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerListModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;

public class SolutionManager extends JFrame implements MouseMotionListener, MouseListener, ImageViewerPlugin {

    final private String username;
    final private ImagesSource imagesSource;
    final private SolutionSource solutionSource;
    final private StudySource studySource;
    private Solution solution;
    private final MouseActionLogger mouseAction = new MouseActionLogger();
    private int overlayIdx = 0;
    final private HashMap<ImageViewer, JFrame> imageViewers = new HashMap<>();
    private ImageViewer imageViewerLastSelected;
    JFileChooser jFileChooserImportExportClassifier, jFileChooserWekaExportTrainingData;
    private DefaultTreeModel jTreeStudyModel;
    private boolean canModify;
    JSpinner jSpinnerTileDim, jSpinnerWindowDim;
    private ExecutorService executorGenerateModel;
    private ExecutorService executorSolutionChange;

    private void errorOut(String msg) {
        String str = "Failed to load solution " + solutionSource.getName() + " because of: " + msg;
        AdvancedVirtualMicroscope.setStatusText(str, 5000);
        JOptionPane.showMessageDialog(rootPane, str);
    }

    public SolutionManager(String username, SolutionSource solutionSource) {
        this.solutionSource = solutionSource;
        this.studySource = solutionSource.getStudySource();
        this.imagesSource = studySource.getImagesSource();
        this.username = username;
        setupLookAndFeel();
        initComponents();
        executorGenerateModel = Executors.newSingleThreadExecutor();
        executorSolutionChange = Executors.newSingleThreadExecutor();
        
        solutionSource.addSolutionChangeListener(new SolutionChangeListener() {
            @Override
            public void solutionChanged(SolutionChangeEvent event) {
                AccessController.doPrivileged(new PrivilegedAction<Integer>() {
                    @Override
                    public Integer run() {
                        switch (event.type) {
                            case Full:
                                try {
                                    solution = new SolutionXML_Parser().parse(event.eventData);
                                    AdvancedVirtualMicroscope.setStatusText("Solution " + solution.toString() + " updated from server", 5000);
                                } catch (ParserConfigurationException | SAXException | IOException ex) {
                                    Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                break;
                        }
                        return 0;
                    }
                });
            }
        });
                
        executorGenerateModel.submit(new Runnable() {
            @Override
            public void run() {
                if (solutionSource == null) {
                    throw new NullPointerException("SolutionSource must not be null");
                }
                try {
                    solution = solutionSource.getSolution();
                    if (solution == null) {
                        errorOut("Retrieved null solution");
                    }
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    errorOut(ex.getMessage());
                    return;
                }


                jTreeStudy.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                canModify = solutionSource.getPermissions().canModify();
                if (!canModify) {
                    jMenuClassifier.setVisible(false);
                    jMenuItemAddImages.setVisible(false);
                    jMenuItemClassAdd.setVisible(false);
                    jMenuItemClassRename.setVisible(false);
                    jMenuItemClassRemove.setVisible(false);
                    jMenuItemDelete.setVisible(false);
                    jMenuItemGenerateModel.setVisible(false);
                    jMenuItemImportClassifier.setVisible(false);
                    jMenuItemSetupModel.setVisible(false);
                    jMenuItemDelete1.setVisible(false);
                } else {
                    addTileDimSpinner();
                }

                setupStudyTable();

                jTextFieldSolutionName.setText(solutionSource.getName());
                updateGenerateModelButton();

                addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (executorGenerateModel != null) {
                            executorGenerateModel.shutdownNow();
                            executorGenerateModel = null;
                        }
                        for (ImageViewer imageViewer : imageViewers.keySet()) {
                            AdvancedVirtualMicroscope.closeImageViewer(imageViewer);
                        }
                        solutionSource.getStudySource().close();
                        solutionSource.close();
                    }
                });

                jCheckBoxMenuItemRunOnServer.setVisible(solutionSource instanceof SolutionSourcePort);

                jFileChooserImportExportClassifier = new JFileChooser();
                jFileChooserImportExportClassifier.setMultiSelectionEnabled(false);
                jFileChooserImportExportClassifier.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jFileChooserImportExportClassifier.setFileFilter(new MODEL_Filter());

                jFileChooserWekaExportTrainingData = new JFileChooser();
                jFileChooserWekaExportTrainingData.setFileFilter(new ARFF_Filter());
                jFileChooserWekaExportTrainingData.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jFileChooserWekaExportTrainingData.setMultiSelectionEnabled(false);
                countDownLatchSolution.countDown();
                pack();
            }
        });

        pack();
    }

    private void setupStudyTable() {
        String result = studySource.updateImageSets();
        if (result.contains("error:")) {
            JOptionPane.showMessageDialog(rootPane, "Permission was denied while trying to gather training data. " + result, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Permissions p = studySource.getPermissions();
        jMenuItemAddImages.setEnabled(p.canModify());

        jTreeStudy.setDragEnabled(true);
        jTreeStudy.setDropMode(DropMode.ON_OR_INSERT);
        jTreeStudy.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        jTreeStudyModel = new DefaultTreeModel(studySource);
        jTreeStudy.setModel(jTreeStudyModel);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getComponent() instanceof ImageViewer) {
            mouseAction.mousePressed(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getSource() instanceof ImageViewer) {
            mouseAction.mouseDragged(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    public void updateButtons(final ImageViewer imageViewer) {
        if (imageViewerLastSelected != imageViewer){
            return;
        }
        final Solution s = solution;
        EventQueue.invokeLater(() -> {
            try {
                ArrayList<ROI> selectedROIs = imageViewer.getROI_TreeTable().getSelectedROIs();
                if (selectedROIs.isEmpty()) {
                    jButtonClassify.setEnabled(false);
                    jButtonDistribution.setEnabled(false);
                    jButtonViewFeatures.setEnabled(false);
                } else {
                    boolean hasValiedClassifier = s.hasValidClassifier();
                    jButtonClassify.setEnabled(hasValiedClassifier);
                    jButtonDistribution.setEnabled(hasValiedClassifier);
                    jButtonViewFeatures.setEnabled(s.getNumelFeatures() > 0);
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "", ex);
            }
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mouseAction.mouseClicked(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseAction.mouseReleased(e);
    }

    private void setupLookAndFeel() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }
    }

    private ArrayList<ImageSource> createRequiredImageSources() {
        ArrayList<ImageSource> imageSources = new ArrayList<>();
        for (ImageManager imageManager : studySource.getAllImageManagers()) {
            AnnotationSet annoSet = studySource.getAnnotationSet(imageManager.imageReference);
            for (ROI_Folder folder : annoSet.getROI_Folders()) {
                if (!folder.getROIs().isEmpty()) {
                    imageSources.add(imagesSource.createImageSource(imageManager));
                    break;
                }
            }
        }
        return imageSources;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void paintOnImageViewer(ImageViewer sv, Graphics gOrig) {
        if (solution == null) {
            return;
        }
        Graphics2D g = (Graphics2D) gOrig.create();
        sv.concatenateImageToDisplayTransform(g);
        if (((ROI_ManagerPanelSolution) sv.getROI_ManagerPanel()).isShowTilesChecked()) {
            paintSampleTiles(sv, g);
        }
    }

    private void paintSampleTiles(ImageViewer imageViewer, Graphics gOrig) {
        if (solution == null) {
            return;
        }
        Graphics2D g = (Graphics2D) gOrig.create();
        int tileDim = solution.getTileDim();
        int tileWindowDim = solution.getTileWindowDim();
        ImageSource imageSource = imageViewer.getImageSource();

        if (g.getTransform().getScaleX() * tileDim < 4) {
            Graphics2D gDisp = (Graphics2D) g.create();
            gDisp.setTransform(new AffineTransform());

            Rectangle window = gDisp.getClipBounds();
            gDisp.setColor(Color.white);
            gDisp.fillRect(window.width - 150, window.height - 28, 115, 18);
            gDisp.setColor(Color.black);
            gDisp.drawRect(window.width - 150, window.height - 28, 115, 18);
            gDisp.drawString("Zoom in to see tiles", window.width - 150 + 2, window.height - 14);
            return;
        }

        Rectangle bounds = g.getClipBounds();
        bounds.x = (int) Math.max(0, Math.floor((double) bounds.x / tileDim) * tileDim);
        bounds.y = (int) Math.max(0, Math.floor((double) bounds.y / tileDim) * tileDim);
        bounds.width = Math.min(bounds.width, imageSource.getImageDimX())+tileDim;
        bounds.height = Math.min(bounds.height, imageSource.getImageDimY())+tileDim;

        for (int x = 0; x < imageSource.getImageDimX(); x += tileDim) {
            g.drawLine(x, bounds.y, x, bounds.y + bounds.height);
        }
        for (int y = 0; y < imageSource.getImageDimY(); y += tileDim) {
            g.drawLine(bounds.x, y, bounds.x + bounds.width, y);
        }
        if (tileDim != tileWindowDim) {
            g.setStroke(new BasicStroke(5, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[]{10.0f}, 0));
            g.setColor(Color.red);
            int skip = (int) Math.ceil((float) tileWindowDim / tileDim) + 2;
            for (int x = bounds.x; x < bounds.x + bounds.width; x += tileDim * skip) {
                for (int y = bounds.y; y < bounds.y + bounds.height; y += tileDim * skip) {
                    g.drawRect( x - tileWindowDim / 2+tileDim/2, 
                                y - tileWindowDim / 2+tileDim/2, 
                                tileWindowDim, 
                                tileWindowDim);
                }
            }
        }

    }

    SolutionSource getSolutionSource() {
        return solutionSource;
    }

    private void repaintOpenImageViewers() {
        imageViewers.keySet().stream().forEach(imageViewer -> {
            imageViewer.repaint();
        });
    }

    private SolutionManager getThis() {
        return this;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenuJTree = new javax.swing.JPopupMenu();
        jMenuItemDelete1 = new javax.swing.JMenuItem();
        jButtonClassify = new javax.swing.JButton();
        jButtonViewFeatures = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldSolutionName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeStudy = new javax.swing.JTree();
        jButtonDistribution = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuSolution = new javax.swing.JMenu();
        jMenuItemAddImages = new javax.swing.JMenuItem();
        jMenuItemInfo = new javax.swing.JMenuItem();
        jMenuItemDelete = new javax.swing.JMenuItem();
        jMenuItemClone = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItemExportAllROIs = new javax.swing.JMenuItem();
        jMenuItemRemoveUnusedImages = new javax.swing.JMenuItem();
        jMenuOptions = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItemWekaExportTrainingData = new javax.swing.JMenuItem();
        jMenuItemExportClassifier = new javax.swing.JMenuItem();
        jMenuItemImportClassifier = new javax.swing.JMenuItem();
        jMenuClassifier = new javax.swing.JMenu();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemClassAdd = new javax.swing.JMenuItem();
        jMenuItemClassRemove = new javax.swing.JMenuItem();
        jMenuItemClassRename = new javax.swing.JMenuItem();
        jMenuItemSetupModel = new javax.swing.JMenuItem();
        jMenuItemGenerateModel = new javax.swing.JMenuItem();
        jCheckBoxMenuItemRunOnServer = new javax.swing.JCheckBoxMenuItem();

        jPopupMenuJTree.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                jPopupMenuJTreePopupMenuWillBecomeVisible(evt);
            }
        });

        jMenuItemDelete1.setText("Delete");
        jMenuItemDelete1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDelete1ActionPerformed(evt);
            }
        });
        jPopupMenuJTree.add(jMenuItemDelete1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Solution Manager");
        setLocationByPlatform(true);

        jButtonClassify.setText("Classify");
        jButtonClassify.setToolTipText("Generate a model and select an ROI to enable");
        jButtonClassify.setEnabled(false);
        jButtonClassify.setPreferredSize(new java.awt.Dimension(100, 23));
        jButtonClassify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClassifyActionPerformed(evt);
            }
        });

        jButtonViewFeatures.setText("View Features");
        jButtonViewFeatures.setToolTipText("Select an ROI to enable");
        jButtonViewFeatures.setEnabled(false);
        jButtonViewFeatures.setPreferredSize(new java.awt.Dimension(100, 23));
        jButtonViewFeatures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonViewFeaturesActionPerformed(evt);
            }
        });

        jLabel1.setText("Solution Name");

        jTextFieldSolutionName.setEditable(false);
        jTextFieldSolutionName.setText("n/a");

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
        jScrollPane1.setViewportView(jTreeStudy);

        jButtonDistribution.setText("Distribution");
        jButtonDistribution.setToolTipText("Generate a model and select an ROI to enable");
        jButtonDistribution.setEnabled(false);
        jButtonDistribution.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDistributionActionPerformed(evt);
            }
        });

        jMenuSolution.setText("Solution");

        jMenuItemAddImages.setText("Add Image(s)");
        jMenuItemAddImages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddImagesActionPerformed(evt);
            }
        });
        jMenuSolution.add(jMenuItemAddImages);

        jMenuItemInfo.setText("Info");
        jMenuItemInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemInfoActionPerformed(evt);
            }
        });
        jMenuSolution.add(jMenuItemInfo);

        jMenuItemDelete.setText("Delete");
        jMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteActionPerformed(evt);
            }
        });
        jMenuSolution.add(jMenuItemDelete);

        jMenuItemClone.setText("Clone");
        jMenuItemClone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCloneActionPerformed(evt);
            }
        });
        jMenuSolution.add(jMenuItemClone);

        jMenuBar1.add(jMenuSolution);

        jMenu3.setText("Functions");

        jMenuItemExportAllROIs.setText("Export all ROIs");
        jMenuItemExportAllROIs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportAllROIsActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemExportAllROIs);

        jMenuItemRemoveUnusedImages.setText("Remove unused images");
        jMenuItemRemoveUnusedImages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRemoveUnusedImagesActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemRemoveUnusedImages);

        jMenuBar1.add(jMenu3);

        jMenuOptions.setText("Options");
        jMenuBar1.add(jMenuOptions);

        jMenu2.setText("Weka");

        jMenuItemWekaExportTrainingData.setText("Export training data");
        jMenuItemWekaExportTrainingData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemWekaExportTrainingDataActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemWekaExportTrainingData);

        jMenuItemExportClassifier.setText("Export classifier");
        jMenuItemExportClassifier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportClassifierActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemExportClassifier);

        jMenuItemImportClassifier.setText("Import classifier");
        jMenuItemImportClassifier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemImportClassifierActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemImportClassifier);

        jMenuBar1.add(jMenu2);

        jMenuClassifier.setText("Classifier");
        jMenuClassifier.setToolTipText("Generate a model to activate");

        jMenu1.setText("Class");

        jMenuItemClassAdd.setText("Add");
        jMenuItemClassAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClassAddActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemClassAdd);

        jMenuItemClassRemove.setText("Remove");
        jMenuItemClassRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClassRemoveActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemClassRemove);

        jMenuItemClassRename.setText("Rename");
        jMenuItemClassRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClassRenameActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemClassRename);

        jMenuClassifier.add(jMenu1);

        jMenuItemSetupModel.setText("Setup Model");
        jMenuItemSetupModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSetupModelActionPerformed(evt);
            }
        });
        jMenuClassifier.add(jMenuItemSetupModel);

        jMenuItemGenerateModel.setText("Generate Model");
        jMenuItemGenerateModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGenerateModelActionPerformed(evt);
            }
        });
        jMenuClassifier.add(jMenuItemGenerateModel);

        jCheckBoxMenuItemRunOnServer.setText("Run On Server");
        jCheckBoxMenuItemRunOnServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemRunOnServerActionPerformed(evt);
            }
        });
        jMenuClassifier.add(jCheckBoxMenuItemRunOnServer);

        jMenuBar1.add(jMenuClassifier);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldSolutionName))
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jButtonViewFeatures, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonClassify, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDistribution, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldSolutionName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButtonClassify, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonDistribution, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonViewFeatures, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateGenerateModelButton() {
        if (solution.getClassifiers().isEmpty()) {
            jMenuItemGenerateModel.setEnabled(false);
            return;
        }
        if (solution.getNumelFeatures() == 0) {
            jMenuItemGenerateModel.setEnabled(false);
            return;
        }
        jMenuItemGenerateModel.setEnabled(true);
    }

    @SuppressWarnings("unchecked")
    private void jButtonClassifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClassifyActionPerformed
        // Setup Parameters
        final ImageViewer imageViewer = imageViewerLastSelected;
        if (imageViewer == null) {
            return;
        }
        final ArrayList<ROI> selectedROIs = imageViewer.getROI_TreeTable().getSelectedROIs();
        final ImageSource imageSource = imageViewer.getImageSource();
        final Solution solutionHold = solution;
        if (selectedROIs.isEmpty()) {
            return;
        }

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                UIManager.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarForegroundPainter());
                AVM_ProgressMonitor pm = new AVM_ProgressMonitor(getThis(), "Preparing labels for display", "Locating samples", 0, 1000000);
                pm.setMillisToPopup(50);
                pm.setMillisToDecideToPopup(50);
                pm.setProgress(1);
                pm.setProgress(2);
                pm.setNote("Locating samples");
                SampleSetImage sampleSetImage = new SampleSetImage(imageSource, selectedROIs, solutionHold, solutionSource);
                sampleSetImage.setIsForTest(true);
                pm.setProgress(3);
                pm.setMaximum((int) (sampleSetImage.samples.size() * 1.5));
                try {
                    sampleSetImage.generateSampleFeatures(pm);
                } catch (OutOfMemoryError ex) {
                    pm.close();
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Java heap space out of menory", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (Throwable ex) {
                    pm.close();
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Failed to generate features", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (pm.isCanceled()) {
                    return;
                }
                ArrayList<Sample> samples = sampleSetImage.samples;
                if (samples == null) {
                    if (!pm.isCanceled()) {
                        JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Failed to generate features", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    for (ClassifierWeka classifier : solutionHold.getClassifiers()) {
                        if (classifier.isActive() && classifier.isValid()) {
                            classifier.classify(samples);
                            Collection<String> classNames = solution.getClassifierClassNames().values();
                            ClassifierResults cr = new ClassifierResults(classifier.getName(), imageViewer, samples, classNames.toArray(new String[classNames.size()]), "Overlay #" + String.valueOf(overlayIdx++));
                            imageViewer.addPlugin(cr);
                        }
                    }
                    pm.close();
                }
                pm.close();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
    }//GEN-LAST:event_jButtonClassifyActionPerformed


    private void jButtonViewFeaturesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewFeaturesActionPerformed
        if (solution.getNumelFeatures() == 0) {
            showModelSetupWindow();
        }
        if (solution.getNumelFeatures() == 0) {
            JOptionPane.showMessageDialog(this, "You must select at least one feature to view");
            return;
        }

        // Setup Parameters
        final ImageViewer imageViewer = imageViewerLastSelected;
        if (imageViewer == null) {
            return;
        }
        final ArrayList<ROI> selectedROIs = imageViewer.getROI_TreeTable().getSelectedROIs();
        final ImageSource imageSource = imageViewer.getImageSource();
        final Solution solutionHold = solution;
        if (selectedROIs.isEmpty()) {
            return;
        }
        UIManager.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarForegroundPainter());
        final AVM_ProgressMonitor pm = new AVM_ProgressMonitor(getThis(), "Preparing features for display", "", 0, 1000000);
        pm.setMillisToPopup(50);
        pm.setMillisToDecideToPopup(50);
        pm.setProgress(1);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                pm.setNote("Locating samples");
                pm.setProgress(2);
                SampleSetImage sampleSetImage = new SampleSetImage(imageSource, selectedROIs, solutionHold, solutionSource);
                sampleSetImage.setIsForTest(true);
                pm.setProgress(3);
                pm.setMaximum(sampleSetImage.samples.size() + 10);
                try {
                    sampleSetImage.generateSampleFeatures(pm);
                } catch (OutOfMemoryError ex) {
                    pm.close();
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Java heap out of menory", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (Throwable ex) {
                    pm.close();
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Failed to generate features", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                pm.setProgress(3);
                if (pm.isCanceled()) {
                    return;
                }
                if (sampleSetImage.samples == null) {
                    if (!pm.isCanceled()) {
                        JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Failed to generate features", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    ArrayList<String> featureNames = new ArrayList<>();
                    for (AbstractFeatureGenerator featureGenerator : solutionHold.getFeatureGenerators()) {
                        if (!featureGenerator.isactive) {
                            continue;
                        }
                        String[] names = featureGenerator.getFeatureNames();
                        featureNames.addAll(Arrays.asList(names));
                    }
                    if (sampleSetImage.samples.isEmpty()) {
                        JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "No samples were collected", "Error", JOptionPane.ERROR_MESSAGE);
                        pm.close();
                        return;
                    }
                    FeatureOverlay fd = new FeatureOverlay(imageViewer, sampleSetImage.samples, featureNames.toArray(new String[featureNames.size()]), "Overlay #" + String.valueOf(overlayIdx++));
                    imageViewer.addPlugin(fd);
                }
                jButtonViewFeatures.setEnabled(true);
                pm.close();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
    }//GEN-LAST:event_jButtonViewFeaturesActionPerformed

    public void promptForNewClass() {
        // TODO: this only works on open images, need to adjust full database
        HashMap<Long, String> classes = solution.getClassifierClassNames();
        String newName = null;
        long newFolderID;
        for (int i = 1; i < 1000; i++) {
            newName = "Class " + String.valueOf(i);
            if (!classes.values().contains(newName)) {
                break;
            }
        }
        newName = JOptionPane.showInputDialog(this, "Enter new classifier name", newName);
        if (newName == null) {
            return;
        }
        newFolderID = solution.addClassifierClassName(newName);
        if (newFolderID == -1) {
            return;
        }
        for (ImageViewer imageViewer : imageViewers.keySet()) {
            ROI_Folder folder = ROI_Folder.createDefault();
            folder.id = newFolderID;
            folder.setName(newName, true);
            imageViewer.getROI_TreeTable().addROI_Folder(folder);
        }
        saveSolution();
    }

    private void jMenuItemClassAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClassAddActionPerformed
        // TODO: this only works on open images, need to adjust full database
        promptForNewClass();
    }//GEN-LAST:event_jMenuItemClassAddActionPerformed

    private void saveSolution() {
        if (!canModify) {
            return;
        }
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                String result = solutionSource.setSolution(solution);
                if (result.startsWith("error:")) {
                    AdvancedVirtualMicroscope.setStatusText("error while updating solution " + solution.toString() + "\n" + result, overlayIdx);
                }
            } catch (PermissionDenied ex) {
                Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    private final CountDownLatch countDownLatchSolution = new CountDownLatch(1);
    
    public void showSetupPrompts(){
        try {
            countDownLatchSolution.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        showModelSetupWindow();
        showAddImagesPrompt();
    }
    
    private void showModelSetupWindow() {
        ModelSetup ms = new ModelSetup(this, true, solution);
        ms.setVisible(true);
        if (ms.iscanceled()) {
            return;
        }
        updateGenerateModelButton();
        saveSolution();
    }

    private void jMenuItemSetupModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSetupModelActionPerformed
        showModelSetupWindow();
    }//GEN-LAST:event_jMenuItemSetupModelActionPerformed

    private void jMenuItemGenerateModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGenerateModelActionPerformed

        // Setup Parameters
        Solution solutionHold = solution;
        // Generate model
        if (executorGenerateModel != null) {
            executorGenerateModel.shutdownNow();
            executorGenerateModel = null;
        }
        executorGenerateModel = Executors.newSingleThreadExecutor();
        executorGenerateModel.submit(() -> {
            if (jCheckBoxMenuItemRunOnServer.isSelected()) {
                SolutionSourcePort ssp = (SolutionSourcePort) solutionSource;
                String result = ssp.trainOnServer();
                if (result.startsWith("error:")) {
                    JOptionPane.showMessageDialog(this, "Failed to start training on server");
                } else {
                    long progressMonitorID = Long.valueOf(result);
                    UIManager.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarForegroundPainter());
                    AVM_ProgressMonitor pm = new AVM_ProgressMonitor(this, "Generating classifier on server", "Starting", 0, 100000);
                    final Timer t = new Timer(1000, null);
                    t.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                if (pm.isCanceled() || Thread.currentThread().isInterrupted()) {
                                    t.stop();
                                    ssp.cancelMonitor(progressMonitorID);
//                                    for (ClassifierWeka classifier : solution.getClassifiers()) {
//                                        if (classifier.isActive()) {
//                                            JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(null), classifier.getMessage(), "Classifier Results", JOptionPane.INFORMATION_MESSAGE);
//                                        }
//                                    }
                                    return;
                                }
                                Properties p = ssp.getProgress(progressMonitorID);
                                if (p==null){
                                    return;
                                }
                                pm.setMinimum(Integer.valueOf(p.getProperty("min")));
                                pm.setMaximum(Integer.valueOf(p.getProperty("max")));
                                pm.setProgress(Integer.valueOf(p.getProperty("progress")));
                                String note = p.getProperty("note");
                                pm.setNote(note);
                                if (p.getProperty("closed").equals("true")) {
                                    JOptionPane.showMessageDialog(rootPane, note);
                                    pm.close();
                                    t.stop();
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(getClass().getName()).log(Level.WARNING, null, ex);
                            }
                        }
                    });
                    t.setRepeats(true);
                    t.start();
                    System.out.println(result);
                }
            } else {
                // Run training locally
                jMenuItemGenerateModel.setEnabled(false);
                UIManager.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarForegroundPainter());
                final AVM_ProgressMonitor pm = new AVM_ProgressMonitor(getThis(), "Generating model", "", 0, 1000000);
                pm.setMillisToDecideToPopup(50);
                pm.setMillisToPopup(50);
                pm.setProgress(1);
                pm.setNote("Collecting image sources");
                final ArrayList<ImageSource> imageSources;
                imageSources = createRequiredImageSources();
                pm.setProgress(3);
                pm.setNote("Generating Features");
                try {
                    Instances instances = null;

                    ArrayList<String> featureNames = new ArrayList<>();
                    for (AbstractFeatureGenerator generator : solutionHold.getFeatureGenerators()) {
                        for (String name : generator.getFeatureNames()) {
                            featureNames.add(name);
                        }
                    }

                    ArrayList<String> classNames = new ArrayList<>();
                    classNames.addAll(solutionHold.getClassifierClassNames().values());

                    try {
                        instances = SampleSetClass.generateInstances(imageSources, solutionSource, pm);
                    } catch (OutOfMemoryError ex) {
                        pm.close();
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Java heap space out of menory", "Error", JOptionPane.ERROR_MESSAGE);
                        jMenuItemGenerateModel.setEnabled(true);
                        closeImageSources(imageSources);
                        return;
                    } catch (Throwable ex) {
                        pm.close();
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Failed to generate features", "Error", JOptionPane.ERROR_MESSAGE);
                        jMenuItemGenerateModel.setEnabled(true);
                        closeImageSources(imageSources);
                        return;
                    }

                    if (instances == null) {
                        if (!pm.isCanceled()) {
                            JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Failed to generate features", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    if (pm.isCanceled()) {
                        closeImageSources(imageSources);
                        return;
                    }

                    // Check if there are samples in each set
                    long count[] = new long[instances.numClasses()];
                    for (int i = 0; i < count.length; i++) {
                        count[i] = 0;
                    }
                    for (int i = 0; i < instances.numInstances(); i++) {
                        count[(int) instances.instance(i).classValue()]++;
                    }
                    for (int i = 0; i < count.length; i++) {
                        if (count[i] < 10) {
                            JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Not enough samples collected", "Error", JOptionPane.ERROR_MESSAGE);
                            jMenuItemGenerateModel.setEnabled(true);
                            closeImageSources(imageSources);
                            return;
                        }
                    }
                    pm.setNote("Training classifier(s)");
                    solution.trainClassifiers(instances);
                    saveSolution();
                    pm.close();
                    jMenuItemGenerateModel.setEnabled(true);
                    for (ClassifierWeka classifier : solution.getClassifiers()) {
                        if (classifier.isActive()) {
                            JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), classifier.getMessage(), "Classifier Results", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    pm.close();
                    if (!pm.isCanceled()) {
                        JOptionPane.showMessageDialog(this, "Failed to generate the classifier(s)", "Error", JOptionPane.ERROR_MESSAGE);
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "", ex);
                    }
                    jMenuItemGenerateModel.setEnabled(true);
                }
                closeImageSources(imageSources);
            }
        });
    }//GEN-LAST:event_jMenuItemGenerateModelActionPerformed

    private void jMenuItemDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteActionPerformed
        String options[] = new String[]{"Delete", "Cancel"};
        int selectedOption = JOptionPane.showOptionDialog(this, "Permanently delete this solution?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
        String solutionName = solutionSource.getName();
        if (selectedOption == 0) {
            try {
                String result = solutionSource.delete();
                if (result.startsWith("error:")) {
                    AdvancedVirtualMicroscope.setStatusText("Could not delete solution " + solutionName + ". " + result, 4 * 1000);
                } else {
                    AdvancedVirtualMicroscope.setStatusText("Solution " + solutionName + " was deleted", 4 * 1000);
                }
            } catch (PermissionDenied ex) {
                Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        dispose();
    }//GEN-LAST:event_jMenuItemDeleteActionPerformed

    private void jTreeStudyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTreeStudyMouseClicked
        if ((evt.getClickCount() == 1) && (evt.getButton() == MouseEvent.BUTTON1)) {
            TreePath path = jTreeStudy.getClosestPathForLocation(evt.getPoint().x, evt.getPoint().y);
            if (path == null) {
                return;
            }
            jTreeStudy.setSelectionPath(path);
        }
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
        final ROI_ManagerPanelSolution roiManager = new ROI_ManagerPanelSolution(imageViewer, studySource, this, canModify);
        
        imageViewer.setROI_ManagerPanel(roiManager);
        Executors.newSingleThreadExecutor().submit(() -> {
            imageViewer.addMouseListener(this);
            imageViewer.addMouseMotionListener(this);
            imageViewerLastSelected = imageViewer;
            JFrame frame = AdvancedVirtualMicroscope.addImageViewer(imageViewer);
            imageViewers.put(imageViewer, frame);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
                    imageViewerLastSelected = imageViewer;
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    imageViewers.remove(imageViewer);
                }
            });
        });
        imageViewer.addPlugin(this);
    }

    private void jMenuItemDelete1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDelete1ActionPerformed
        DefaultTreeModel model = (DefaultTreeModel) jTreeStudy.getModel();
        TreePath[] paths = jTreeStudy.getSelectionPaths();
        for (TreePath path : paths) {
            Object obj = path.getLastPathComponent();
            if (obj == null) {
                continue;
            }
            if (obj instanceof ImageManager) {
                ImageManager imageManager = (ImageManager) obj;
                StudySource ss = (StudySource) imageManager.getParent().getParent();
                try {
                    ss.removeImage(imageManager.imageReference);
                } catch (PermissionDenied ex) {
                    Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                closeImage(imageManager.imageReference);
//                jTreeStudyModel.reload();
                jTreeStudy.updateUI();
            }
            if (obj instanceof ImageManagerSet) {
                ImageManagerSet imageReferenceSet = (ImageManagerSet) obj;
                imageReferenceSet.getImageManagerSet().stream().forEach((imageManager) -> {
                    StudySource ss = (StudySource) imageManager.getParent().getParent();
                    closeImage(imageManager.imageReference);
                    try {
                        ss.removeImage(imageManager.imageReference);
                    } catch (PermissionDenied ex) {
                        Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                model.removeNodeFromParent(imageReferenceSet);
                jTreeStudy.updateUI();

//                jTreeStudyModel.reload();
            }
        }
        final ImageViewer sv = imageViewerLastSelected;
        if (sv != null) {
            sv.repaint();
        }
    }//GEN-LAST:event_jMenuItemDelete1ActionPerformed

    private void jPopupMenuJTreePopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jPopupMenuJTreePopupMenuWillBecomeVisible

    }//GEN-LAST:event_jPopupMenuJTreePopupMenuWillBecomeVisible

    private void jMenuItemAddImagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddImagesActionPerformed
        showAddImagesPrompt();
    }//GEN-LAST:event_jMenuItemAddImagesActionPerformed

    public void promptToRemoveClass(String preselectedName) {
        // TODO: this only works on open images, need to adjust full database
        HashMap<Long, String> classes = solution.getClassifierClassNames();
        Object names[] = classes.values().toArray();
        if (preselectedName == null) {
            preselectedName = (String) names[0];
        }

        String name = (String) JOptionPane.showInputDialog(this,
                "Warning! This will remove all ROIs across all images under the selected class name",
                "Remove Classifier Class",
                JOptionPane.WARNING_MESSAGE,
                null,
                names,
                preselectedName);
        if (name == null) {
            return;
        }
        long folderID = -1;
        for (Map.Entry<Long, String> entry : classes.entrySet()) {
            if (entry.getValue().equals(name)) {
                folderID = entry.getKey();
            }
        }
        solution.removeClassifierClassName(name);
        for (ImageViewer imageViewer : imageViewers.keySet()) {
            AnnotationSet annoSet = imageViewer.getROI_TreeTable().getAnnotationSet();
            ArrayList<ROI_Folder> toRemove = new ArrayList<>();
            for (ROI_Folder folder : annoSet.getROI_Folders()) {
                if (folder.id == folderID) {
                    toRemove.add(folder);
                }
            }
            toRemove.stream().forEach((folder) -> {
                annoSet.remove(folder, false);
            });
        }
        saveSolution();
    }

    private void jMenuItemClassRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClassRemoveActionPerformed
        promptToRemoveClass(null);
    }//GEN-LAST:event_jMenuItemClassRemoveActionPerformed

    public void promptToRenameClass(String preselectedName) {
        // TODO: this only works on open images, need to adjust full database
        // this one does really nothing now
        HashMap<Long, String> classes = solution.getClassifierClassNames();
        Object names[] = classes.values().toArray();
        if (preselectedName == null) {
            preselectedName = (String) names[0];
        }
        String name = (String) JOptionPane.showInputDialog(this,
                "Select a class to rename",
                "Rename classifier class",
                JOptionPane.INFORMATION_MESSAGE,
                null,
                names,
                names[0]);
        if (name != null) {
            long folderID = -1;
            for (Map.Entry<Long, String> entry : classes.entrySet()) {
                if (entry.getValue().equals(name)) {
                    folderID = entry.getKey();
                }
            }
            if (folderID == -1) {
                // This should not happen
                return;
            }
            String newName = JOptionPane.showInputDialog(this, "Enter new classifier name", name);
            if (newName == null) {
                return;
            }
            newName = newName.trim();
            if (newName.isEmpty()) {
                return;
            }
            solution.renameClassifierClassName(name, newName);
            for (ImageViewer imageViewer : imageViewers.keySet()) {
                AnnotationSet annoSet = imageViewer.getROI_TreeTable().getAnnotationSet();
                for (ROI_Folder folder : annoSet.getROI_Folders()) {
                    if (folder.id == folderID) {
                        folder.setName(newName, false);
                    }
                }
            }
        }
        saveSolution();
    }

    private void jMenuItemClassRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClassRenameActionPerformed
        promptToRenameClass(null);
    }//GEN-LAST:event_jMenuItemClassRenameActionPerformed

    private void jMenuItemWekaExportTrainingDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemWekaExportTrainingDataActionPerformed

        int jFileChooserResult = jFileChooserWekaExportTrainingData.showSaveDialog(this);
        if (jFileChooserResult != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = jFileChooserWekaExportTrainingData.getSelectedFile();
        String ext = ".arff";
        if (!f.getName().toLowerCase().endsWith(ext)) {
            f = new File(f.getAbsolutePath() + ext);
        }
        final File exportFile = f;
        Executors.newSingleThreadExecutor().submit(() -> {
            UIManager.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarForegroundPainter());
            final AVM_ProgressMonitor pm = new AVM_ProgressMonitor(getThis(), "Generating training data", "", 0, 1000000);
            pm.setMillisToDecideToPopup(50);
            pm.setMillisToPopup(50);
            pm.setProgress(1);
            pm.setProgress(2);
            pm.setNote("Collecting image sources");
            final ArrayList<ImageSource> imageSources = createRequiredImageSources();
            pm.setProgress(3);
            pm.setNote("Counting the number of samples to collect");
            int numelSamples;
            numelSamples = SampleSetClass.numelSamples(imageSources, solutionSource);
            pm.setMaximum(numelSamples);
            pm.setNote("Generating Features");
            Instances instances;
            try {
                instances = SampleSetClass.generateInstances(imageSources, solutionSource, pm);
            } catch (OutOfMemoryError ex) {
                pm.close();
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Java heap space out of menory", "Error", JOptionPane.ERROR_MESSAGE);
                jMenuItemGenerateModel.setEnabled(true);
                closeImageSources(imageSources);
                return;
            } catch (Throwable ex) {
                pm.close();
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Failed to generate features", "Error", JOptionPane.ERROR_MESSAGE);
                jMenuItemGenerateModel.setEnabled(true);
                closeImageSources(imageSources);
                return;
            }

            pm.setNote("Saving to file " + exportFile.getName());
            try {
                ArffSaver saver = new ArffSaver();
                saver.setInstances(instances);
                saver.setFile(exportFile);
                saver.writeBatch();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to write training data to file", "Error", JOptionPane.INFORMATION_MESSAGE);
                Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
                System.gc();
                closeImageSources(imageSources);
                return;
            }
            pm.close();
            JOptionPane.showMessageDialog(this, "Export completed to " + exportFile.getName(), "Completed", JOptionPane.INFORMATION_MESSAGE);
            closeImageSources(imageSources);
            System.gc();
        });
    }//GEN-LAST:event_jMenuItemWekaExportTrainingDataActionPerformed

    private void closeImageSources(ArrayList<ImageSource> imageSources) {
        for (ImageSource imageSource : imageSources) {
            imageSource.close();
        }
    }

    private void jMenuItemCloneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCloneActionPerformed
        String cloneName = JOptionPane.showInputDialog("Enter the mame of the solution clone", solutionSource.getName() + "_Clone");
        if (cloneName != null) {
            try {
                SolutionSource ss = solutionSource.cloneSolution(cloneName);
                if (ss != null) {
                    SolutionManager sm = new SolutionManager(username, ss);
                    sm.setVisible(true);
                    AdvancedVirtualMicroscope.addWindow(sm, "Solution " + ss.getName());
                }
            } catch (PermissionDenied ex) {
                Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jMenuItemCloneActionPerformed

    private void jMenuItemExportClassifierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportClassifierActionPerformed
        ClassifierWeka abstractClassifier = solution.getClassifier(ClassifierWeka.class.getCanonicalName());
        if (abstractClassifier == null) {
            JOptionPane.showMessageDialog(this, "No Weka classifiers found", "Error", JOptionPane.ERROR_MESSAGE);
        }
        if (abstractClassifier instanceof ClassifierWeka) {
            ClassifierWeka classifierWeka = (ClassifierWeka) abstractClassifier;

            int result = jFileChooserImportExportClassifier.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File f = jFileChooserImportExportClassifier.getSelectedFile();
            String ext = ".model";
            if (!f.getName().toLowerCase().endsWith(ext)) {
                f = new File(f.getAbsolutePath() + ext);
            }
            try {
                SerializationHelper.write(new FileOutputStream(f), classifierWeka.getClassifier());
            } catch (Exception ex) {
                Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Failed to export the classifer", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Classifier exported", "Finished", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemExportClassifierActionPerformed

    private void jMenuItemImportClassifierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemImportClassifierActionPerformed
        int result = jFileChooserImportExportClassifier.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = jFileChooserImportExportClassifier.getSelectedFile();
        Classifier classifier;
        try {
            classifier = (Classifier) SerializationHelper.read(new FileInputStream(f));
        } catch (Exception ex) {
            Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Failed to import the classifer", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ClassifierWeka classifierWeka = new ClassifierWeka(classifier);
        classifierWeka.setActive(true);
        classifierWeka.setValid(true);
        solution.setClassifier(classifierWeka);
        saveSolution();
        updateGenerateModelButton();
        JOptionPane.showMessageDialog(this, "Classifier imported", "Finished", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItemImportClassifierActionPerformed

    private void jMenuItemInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInfoActionPerformed
        JFrame f = new JFrame();
        JTextArea t = new JTextArea();
        f.add(t);
        t.setVisible(true);
        f.setVisible(true);
        f.setSize(400, 600);
        t.append("Solution name: " + solutionSource.getName() + "\n");
        t.append("Description: " + solutionSource.getDescription() + "\n");
        t.append("---Class names\n");
        for (Map.Entry<Long, String> entry : solution.getClassifierClassNames().entrySet()) {
            t.append("   Name: " + entry.getValue() + "  Identifier: " + String.valueOf(entry.getKey()) + "\n");
        }
        t.append("---Active feature generators\n");
        for (AbstractFeatureGenerator g : solution.getFeatureGenerators()) {
            if (g.isactive) {
                t.append("   Name: " + g.toString() + "\n");
                for (String name : g.getFeatureNames()) {
                    t.append("      " + name + "\n");
                }
            }
        }
        t.append("---Active classifiers\n");
        for (ClassifierWeka c : solution.getClassifiers()) {
            if (c.isActive()) {
                t.append("   Name: " + c.toString() + "\n");
                Date lastTrainded = c.getLastTrained();
                String s = (lastTrainded == null) ? "untrained" : "";
                t.append("   Last trained:" + s + "\n");
                t.append("   Properties\n");
                try {
                    StreamResult result = new StreamResult(new StringWriter());
                    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    doc.appendChild(c.getProperties(doc));
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.transform(new DOMSource(doc), result);
                    t.append("   " + result.getWriter().toString() + "\n");
                } catch (TransformerConfigurationException ex) {
                    Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerException | ParserConfigurationException ex) {
                    Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }//GEN-LAST:event_jMenuItemInfoActionPerformed

    private void jCheckBoxMenuItemRunOnServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemRunOnServerActionPerformed

    }//GEN-LAST:event_jCheckBoxMenuItemRunOnServerActionPerformed

    private void jButtonDistributionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDistributionActionPerformed
        // Setup Parameters
        final ImageViewer imageViewer = imageViewerLastSelected;
        if (imageViewer == null) {
            return;
        }
        final ArrayList<ROI> selectedROIs = imageViewer.getROI_TreeTable().getSelectedROIs();
        final ImageSource imageSource = imageViewer.getImageSource();
        final Solution solutionHold = solution;
        if (selectedROIs.isEmpty()) {
            return;
        }

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                UIManager.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarForegroundPainter());
                AVM_ProgressMonitor pm = new AVM_ProgressMonitor(getThis(), "Preparing distribution for display", "Locating samples", 0, 1000000);
                pm.setMillisToPopup(50);
                pm.setMillisToDecideToPopup(50);
                pm.setProgress(1);
                pm.setProgress(2);
                pm.setNote("Locating samples");
                SampleSetImage sampleSetImage = new SampleSetImage(imageSource, selectedROIs, solutionHold, solutionSource);
                sampleSetImage.setIsForTest(true);
                pm.setProgress(3);
                pm.setMaximum((int) (sampleSetImage.samples.size() * 1.5));
                try {
                    sampleSetImage.generateSampleFeatures(pm);
                } catch (OutOfMemoryError ex) {
                    pm.close();
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Java heap space out of menory", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (Throwable ex) {
                    pm.close();
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Failed to generate features", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (pm.isCanceled()) {
                    return;
                }
                ArrayList<Sample> samples = sampleSetImage.samples;
                if (samples == null) {
                    if (!pm.isCanceled()) {
                        JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Failed to generate features", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    for (ClassifierWeka classifier : solutionHold.getClassifiers()) {
                        if (classifier.isActive() && classifier.isValid()) {
                            classifier.distribution(samples, solution.getClassifierClassNames().size());
                            Collection<String> classNames = solution.getClassifierClassNames().values();
                            ClassifierResultsDistribution cr = new ClassifierResultsDistribution(classifier.getName(), imageViewer, samples, classNames.toArray(new String[classNames.size()]), "Overlay #" + String.valueOf(overlayIdx++));
                            imageViewer.addPlugin(cr);
                        }
                    }
                    pm.close();
                }
                pm.close();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
    }//GEN-LAST:event_jButtonDistributionActionPerformed

    private void jMenuItemExportAllROIsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportAllROIsActionPerformed
        new ExportDialogROIsFullStudy(this, true, studySource).setVisible(true);
    }//GEN-LAST:event_jMenuItemExportAllROIsActionPerformed

    private void jMenuItemRemoveUnusedImagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRemoveUnusedImagesActionPerformed
        ArrayList<ImageReference> imagesRemoved = studySource.removeUnusedImages();
        for (ImageReference imageRemoved : imagesRemoved) {
            closeImage(imageRemoved);
        }
        jTreeStudy.repaint();
    }//GEN-LAST:event_jMenuItemRemoveUnusedImagesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClassify;
    private javax.swing.JButton jButtonDistribution;
    private javax.swing.JButton jButtonViewFeatures;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemRunOnServer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuClassifier;
    private javax.swing.JMenuItem jMenuItemAddImages;
    private javax.swing.JMenuItem jMenuItemClassAdd;
    private javax.swing.JMenuItem jMenuItemClassRemove;
    private javax.swing.JMenuItem jMenuItemClassRename;
    private javax.swing.JMenuItem jMenuItemClone;
    private javax.swing.JMenuItem jMenuItemDelete;
    private javax.swing.JMenuItem jMenuItemDelete1;
    private javax.swing.JMenuItem jMenuItemExportAllROIs;
    private javax.swing.JMenuItem jMenuItemExportClassifier;
    private javax.swing.JMenuItem jMenuItemGenerateModel;
    private javax.swing.JMenuItem jMenuItemImportClassifier;
    private javax.swing.JMenuItem jMenuItemInfo;
    private javax.swing.JMenuItem jMenuItemRemoveUnusedImages;
    private javax.swing.JMenuItem jMenuItemSetupModel;
    private javax.swing.JMenuItem jMenuItemWekaExportTrainingData;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JMenu jMenuSolution;
    private javax.swing.JPopupMenu jPopupMenuJTree;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldSolutionName;
    private javax.swing.JTree jTreeStudy;
    // End of variables declaration//GEN-END:variables

    private void closeImage(ImageReference imageReference) {
        ArrayList<ImageViewer> toClose = new ArrayList<>();
        imageViewers.keySet().stream().filter((ImageViewer iv) -> iv.getImageSource().imageReference.equals(imageReference)).forEach((imageViewer) -> {
            toClose.add(imageViewer);
        });
        toClose.stream().forEach((v) -> {
            imageViewers.remove(v).dispose();
        });
    }

    private void addTileDimSpinner() {
        jSpinnerTileDim = new JSpinner();
        JMenuBar menuBarTileDim = new JMenuBar();
        menuBarTileDim.add(jSpinnerTileDim);
        menuBarTileDim.add(new JLabel("Tile Size"));
        menuBarTileDim.setEnabled(false);
        jMenuOptions.add(menuBarTileDim);

        jSpinnerWindowDim = new JSpinner();
        JMenuBar menuBarWindowDim = new JMenuBar();
        menuBarWindowDim.add(jSpinnerWindowDim);
        menuBarWindowDim.add(new JLabel("Window Size"));
        menuBarWindowDim.setEnabled(false);
        jMenuOptions.add(menuBarWindowDim);

        ArrayList<Integer> spinnerValues = new ArrayList<>();
        spinnerValues.add(1);
        for (int i = 2; i < 32; i += 2) {
            spinnerValues.add(i);
        }
        for (int i = 32; i < 300; i += 32) {
            spinnerValues.add(i);
        }

        jSpinnerWindowDim.setModel(new SpinnerListModel(spinnerValues));
        jSpinnerTileDim.setModel(new My_SpinnerListModel(spinnerValues));
        jSpinnerWindowDim.setValue(solution.getTileWindowDim());
        jSpinnerTileDim.setValue(solution.getTileDim());

        final Timer updateSolutionTimer = new Timer(2000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSolution();
            }
        });
        updateSolutionTimer.setRepeats(false);

        jSpinnerWindowDim.addChangeListener((ChangeEvent e) -> {
            int windowDim = (int) jSpinnerWindowDim.getValue();
            int tileDim = (int) jSpinnerTileDim.getValue();
            if (windowDim < tileDim) {
                tileDim = windowDim;
            }
            solution.setTileWindowDim(windowDim);
            solution.setTileDim(tileDim);
            jSpinnerTileDim.setValue(tileDim);
            repaintOpenImageViewers();
            updateSolutionTimer.start();
        });

        jSpinnerTileDim.addChangeListener((ChangeEvent e) -> {
            int windowDim = (int) jSpinnerWindowDim.getValue();
            int tileDim = (int) jSpinnerTileDim.getValue();
            if (windowDim < tileDim) {
                windowDim = tileDim;
            }
            solution.setTileWindowDim(windowDim);
            solution.setTileDim(tileDim);
            jSpinnerWindowDim.setValue(windowDim);
            repaintOpenImageViewers();
            updateSolutionTimer.start();
        });
    }

    @Override
    public void paintPlugin(ImageViewer imageViewer, Graphics g) {
        paintOnImageViewer(imageViewerLastSelected, g);
    }

    @Override
    public void addImageViewerPluginListener(ImageViewerPluginListener imageViewerPluginListener) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                imageViewerPluginListener.diapose();
                super.windowClosing(e);
            }
        });
    }

    @Override
    public void close() {
    }

    private class My_SpinnerListModel extends SpinnerListModel {

        private final ArrayList<Integer> spinnerValues;

        public My_SpinnerListModel(ArrayList<Integer> values) {
            super(values);
            Collections.sort(values);
            this.spinnerValues = values;
        }

        @Override
        public void setValue(Object elt) {
            int i = 0;
            if (elt instanceof Integer) {
                i = ((Integer) elt).intValue();
            } else if (elt instanceof String) {
                i = Integer.parseInt((String) elt);
            }
            for (int j = spinnerValues.size() - 1; j > 0; j--) {
                int k = spinnerValues.get(j);
                if (k <= i) {
                    super.setValue(k);
                    return;
                }
            }
            super.setValue(spinnerValues.get(0));
            return;
        }
    }

    private void showAddImagesPrompt() {
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
                            imageManager = (ImageManager) imageManager.clone();
                            try {
                                studySource.addImage((ImageManager) imageManager);
                            } catch (PermissionDenied ex) {
                                Logger.getLogger(SolutionManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            imageManagersAdded.add(imageManager);
                        }
                    }
                }
//                jTreeStudyModel.reload();
                jTreeStudy.updateUI();
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

}
