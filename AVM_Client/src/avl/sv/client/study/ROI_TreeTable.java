package avl.sv.client.study;

import avl.sv.shared.study.TreeTableModelStudy;
import avl.sv.client.fileFilters.XML_Filter;
import avl.sv.client.image.ImageViewer;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.study.AnnotationSet;
import avl.sv.shared.study.ROI;
import avl.sv.shared.study.ROI_Folder;
import avl.sv.shared.study.StudyChangeEvent;
import avl.sv.shared.study.StudyChangeListener;
import avl.sv.shared.study.xml.AnnotationSet_XML_Parser;
import avl.sv.shared.study.xml.AperioAnnotationXML_Writer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Message;
import javax.swing.DropMode;
import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.ParserConfigurationException;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.xml.sax.SAXException;

abstract public class ROI_TreeTable extends JXTreeTable implements MouseMotionListener, StudyChangeListener {

    protected final JFileChooser jFileChooserAnnotationExport = new JFileChooser();
    protected final JFileChooser jFileChooserAnnotationImport = new JFileChooser();
    public boolean showAllMarkers = false, showSelectedMarkers = true, highlightRegions = true;
    protected final int cornerMarkerDim = 10;

    public ROI_Folder lastSelectedROI_Folder;
    protected AnnotationSet annoSet = null;
    private TreeTableModelStudy model;

    public ROI_TreeTable() {
        init();
    }
    
    public void setModel(TreeTableModelStudy treeModel) {
        super.setTreeTableModel(treeModel);
        if (getTreeTableModel() instanceof TreeTableModelStudy) {
            model = (TreeTableModelStudy) getTreeTableModel();
            annoSet = model.getAnnotationSet();
        
            // Setting the treeTableModel above does not setup the colums properly
            // so this performes that function manually 
            TableColumnModel columnModel = new DefaultTableColumnModel();
            for (int i = 0; i < treeModel.getColumnCount(); i++) {
                TableColumn tc = new TableColumn();
                tc.setHeaderValue(treeModel.getColumnName(i));
                tc.setModelIndex(i);
                if (i > 0) {
                    tc.setMaxWidth(50);
                }
                columnModel.addColumn(tc);
            }
            setColumnModel(columnModel);
        }
        updateSelectedROIs();
        lastSelectedROI_Folder = null;
    }

    @Override
    public void studyChanged(StudyChangeEvent changeEvent){
        try {
            switch (changeEvent.type) {
                case Update:
                    if (changeEvent.roiID <= 0) {
                        // ROI not modified
                        ROI_Folder newFolder = ROI_Folder.parse(changeEvent.eventData);
                        for (ROI_Folder folder : annoSet.getROI_Folders()) {
                            if (folder.id == newFolder.id) {
                                setSelectionPath(new TreePath(model.getPathToRoot(folder)));
                                return;
                            }
                        }
                    } else {
                        ROI newROI = ROI.parse(changeEvent.eventData);
                        for (ROI_Folder folder : annoSet.getROI_Folders()) {
                            if (folder.id == changeEvent.folderID) {
                                for (ROI roi : folder.getROIs()) {
                                    if (roi.id == newROI.id) {
                                        setSelectionPath(new TreePath(model.getPathToRoot(roi)));
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    break;
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }   

    private ArrayList<ActionListener> actionListeners = new ArrayList<>();

    public void addActionListener(ActionListener actionListener) {
        actionListeners.add(actionListener);
    }

    public void removeActionListener(ActionListener actionListener) {
        actionListeners.remove(actionListener);
    }

    abstract protected ROI_Folder getDefaultROI_Folder();

    private ROI_TreeTable(TreeTableModel treeModel) throws Exception {
        throw new Exception("Not implemented");
    }

    private void init() {
        setDragEnabled(true);
        setDropMode(DropMode.ON);
        setTransferHandler(new TreeTransferHandlerStudy());
        getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        jFileChooserAnnotationExport.setFileFilter(new XML_Filter());
        jFileChooserAnnotationExport.setAcceptAllFileFilterUsed(false);

        jFileChooserAnnotationImport.setFileFilter(new XML_Filter());
        jFileChooserAnnotationImport.setAcceptAllFileFilterUsed(false);

        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateSelectedROIs();
            }
        });
    }

    public void importROIs() {
        jFileChooserAnnotationImport.setMultiSelectionEnabled(false);
        int result = jFileChooserAnnotationImport.showDialog(this, "Import");
        File file = jFileChooserAnnotationImport.getSelectedFile();
        if ((file == null) || (result != JFileChooser.APPROVE_OPTION)) {
            return;
        }
        try {
            AnnotationSet annoSetNew = AnnotationSet_XML_Parser.parse(file);
            for (ROI_Folder folder : annoSetNew.getROI_Folders()) {
                ArrayList<ROI> rois = folder.getROIs();
                for (ROI roi : rois) {
                    folder.remove(roi, false);
                }                
                annoSet.add(folder, false);
                for (ROI roi : rois) {
                    folder.add(roi, false);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(ROI_ManagerPanelStudy.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateSelectedROIs();
    }

    synchronized public void removeROI_Folder(ROI_Folder folder) {
        if (folder.toString().equals(MessageStrings.Temporary)){
            model.removeNodeFromParent(folder);
        } else {            
            annoSet.remove(folder, false);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getSource().getClass().isAssignableFrom(ImageViewer.class)) {
            ImageViewer imageViewer = (ImageViewer) e.getSource();
            highlightRegion(imageViewer, e.getPoint());
            imageViewer.repaint();
        }
    }

    private void highlightRegion(ImageViewer imageViewer, Point p) {
        Point p2 = (Point) p.clone();
        Graphics2D g = (Graphics2D) imageViewer.getGraphics().create();
        imageViewer.concatenateImageToDisplayTransform(g);
        try {
            g.getTransform().inverseTransform(p, p2);
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(ROI_ManagerPanelStudy.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (annoSet == null) {
            return;
        }
        for (ROI_Folder anno : annoSet.getROI_Folders()) {
            ArrayList<ROI> rois = anno.getROIs();
            for (ROI roi : rois) {
                roi.highlighted = roi.containsPoint(p2);
            }
        }
    }

    synchronized public ArrayList<ROI> getVisibleROIs() {
        ArrayList<ROI> rois = new ArrayList<>();
        for (ROI_Folder anno : annoSet.getROI_Folders()) {
            if (!anno.visible) {
                continue;
            }
            for (ROI roi : anno.getROIs()) {
                rois.add(roi);
            }
        }
        return rois;
    }

    public void exportROIs() {
        jFileChooserAnnotationExport.setMultiSelectionEnabled(false);
        jFileChooserAnnotationExport.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = jFileChooserAnnotationExport.showDialog(this, "Export");
        File file = jFileChooserAnnotationExport.getSelectedFile();
        if ((file == null) || ("".equals(file.getName())) || (result != JFileChooser.APPROVE_OPTION)) {
            return;
        }

        // Make sure file has xml extention
        String filePath = file.getAbsolutePath();
        int lastPoint = filePath.lastIndexOf(".");
        String ext = "";
        if (lastPoint > 0) {
            ext = filePath.substring(lastPoint);
        }
        if (!ext.equalsIgnoreCase(".xml")) {
            filePath += ".xml";
            file = new File(filePath);
        }

        ArrayList<ROI_Folder> toExport = new ArrayList<>();
        for (ROI_Folder anno : annoSet.getROI_Folders()) {
            if (anno.selected) {
                toExport.add(anno);
            } else {
                ROI_Folder tempAnno = null;
                for (ROI roi : anno.getROIs()) {
                    if (roi.selected) {
                        if (tempAnno == null) {
                            tempAnno = ROI_Folder.createDefault();
                            tempAnno.id = anno.id;
                            tempAnno.setLineColor(anno.getLineColor(), true);
                            tempAnno.setName(anno.getName(), true);
                            tempAnno.selected = anno.selected;
                            tempAnno.visible = anno.visible;
                            toExport.add(tempAnno);
                        }
                        tempAnno.add(roi, true);
                    }
                }
            }
        }
        String xml = AperioAnnotationXML_Writer.getXMLString(toExport);
        FileWriter fw;
        try {
            fw = new FileWriter(file);
            fw.write(xml);
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(ROI_ManagerPanelStudy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public AnnotationSet getAnnotationSet() {
        return annoSet;
    }

    synchronized public ArrayList<ROI> getSelectedROIs() {
        ArrayList<ROI> selectedROIs = new ArrayList<>();
        TreePath[] paths = getSelectionPaths();
        if (paths == null) {
            return selectedROIs;
        }
        for (TreePath path : paths) {
            Object obj = path.getLastPathComponent();
            if (obj instanceof ROI) {
                ROI roi = (ROI) obj;
                selectedROIs.add((ROI) roi.clone());
            }
            if (obj instanceof ROI_Folder) {
                selectedROIs.addAll(((ROI_Folder) obj).getROIs());
            }
        }
        return selectedROIs;
    }

    synchronized public void addROI_Folder(ROI_Folder anno) {
        annoSet.add(anno, false);
    }

    synchronized public void removeROI(ROI roi) {
        ROI_Folder folder = (ROI_Folder) roi.getParent();
        if (folder.toString().equals(MessageStrings.Temporary)){
            model.removeNodeFromParent(roi);
        } else {
            folder.remove(roi, false);
        }
    }

    synchronized public void addROI(ROI roi) {
        if (annoSet == null) {
            return;
        }
        if (roi == null) {
            return;
        }
        ROI_Folder folder = getSelectedROI_Folder();
        if (folder == null) {
            if (lastSelectedROI_Folder != null) {
                folder = lastSelectedROI_Folder;
            } else {
                folder = getDefaultROI_Folder();
            }
        }
        roi.selected = true;
        if (folder.toString().equals(MessageStrings.Temporary)){
            model.insertNodeInto(roi, folder, model.getChildCount(folder));
        } else {
            folder.add(roi, false);
        }
    }

    public void setSelectionPaths(ArrayList<TreePath> paths) {
        getSelectionModel().clearSelection();
        for (TreePath path : paths) {
            setSelectionPath(path);
        }
        updateSelectedROIs();
    }

    public void setSelectionPath(TreePath path) {
        if (path.getLastPathComponent() instanceof ROI) {
            expandPath(path.getParentPath());
        }
        int r = getRowForPath(path);
        getSelectionModel().addSelectionInterval(r, r);
        updateSelectedROIs();
    }

    public TreePath[] getSelectionPaths() {
        int[] rows = getSelectedRows();
        TreePath paths[] = new TreePath[rows.length];
        for (int rIdx = 0; rIdx < rows.length; rIdx++) {
            paths[rIdx] = getPathForRow(rows[rIdx]);
        }
        return paths;
    }

    public TreePath getSelectionPath() {
        int row = getSelectedRow();
        return getPathForRow(row);
    }

    Object getLastSelectedPathComponent() {
        return getSelectionPath().getLastPathComponent();
    }

    synchronized public ROI_Folder getSelectedROI_Folder() {
        int r = getSelectedRow();
        if (r < 0) {
            return null;
        }
        TreePath treePath = getPathForRow(r);

        if (treePath == null) {
            return null;
        }
        try {
            Object[] path = treePath.getPath();
            for (int i = path.length - 1; i >= 0; i--) {
                if (path[i].getClass().equals(ROI_Folder.class)) {
                    return (ROI_Folder) path[i];
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    synchronized public void updateSelectedROIs() {
        for (ROI_Folder anno : annoSet.getROI_Folders()) {
            for (ROI roi : anno.getROIs()) {
                roi.selected = false;
            }
        }

        int[] rows = getSelectedRows();
        TreePath paths[] = new TreePath[rows.length];
        for (int rIdx = 0; rIdx < rows.length; rIdx++) {
            paths[rIdx] = getPathForRow(rows[rIdx]);
        }

        if (paths == null) {
            return;
        }
        for (TreePath path : paths) {
            try {
                DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) path.getLastPathComponent();
                if (node instanceof ROI_Folder) {
                    ROI_Folder anno = (ROI_Folder) node;
                    lastSelectedROI_Folder = anno;
                    ArrayList<ROI> rois = (anno).getROIs();
                    for (ROI roi : rois) {
                        roi.selected = true;
                    }
                }
                if (node instanceof ROI) {
                    ROI roi = (ROI) node;
                    lastSelectedROI_Folder = (ROI_Folder) roi.getParent();
                    roi.selected = true;
                }

            } catch (Exception ex) {
            }
        }

        for (ActionListener actionListener : actionListeners) {
            actionListener.actionPerformed(new ActionEvent(this, actionEventIdx++, "Updated"));
        }
    }
    private int actionEventIdx = 0;

    public void paintROIs(ImageViewer imageViewer, Graphics2D g) {
        if (annoSet == null) {
            return;
        }
        for (ROI_Folder anno : annoSet.getROI_Folders()) {
            if (!anno.visible) {
                continue;
            }
            for (ROI roi : anno.getROIs()) {
                paintROI(imageViewer, g, roi, anno.getLineColor());
            }
        }
    }

    private void paintROI(ImageViewer imageViewer, Graphics2D g, ROI roi, Color color) {
        Shape s = roi.getShape();

        float zFactor = (float) (1 / g.getTransform().getScaleX());
        g.setColor(color);
        g.setStroke(new BasicStroke(5 * zFactor));
        g.draw(s);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1 * zFactor));
        g.draw(s);

        g.setStroke(new BasicStroke(3 * zFactor));

        if (roi.highlighted && highlightRegions) {
            g.setColor(Color.green);
            Color c = g.getColor();
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
            g.fill(roi.getShape());
        }
        if (showSelectedMarkers || showAllMarkers) {
            if (roi.selected && showSelectedMarkers) {
                g.setColor(Color.red);
            } else if (showAllMarkers) {
                g.setColor(Color.black);
            } else {
                return;
            }

            Polygon poly = roi.getPolygon();
            if (poly != null) {
                int dim = (int) (cornerMarkerDim / imageViewer.getMagnification());
                for (int j = 0; j < poly.npoints; j++) {
                    g.drawOval(poly.xpoints[j] - dim / 2, poly.ypoints[j] - dim / 2, dim, dim);
                }
            }
        }
    }

}
