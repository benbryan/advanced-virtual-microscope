/*
 * from http://www.coderanch.com/t/346509/GUI/java/JTree-drag-drop-tree-Java
 */
package avl.sv.client.study;

import avl.sv.shared.MessageStrings;
import avl.sv.shared.study.ROI_Folder;
import avl.sv.shared.study.AnnotationSet;
import avl.sv.shared.study.ROI;
import avl.sv.shared.image.ImageID;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

public class TreeTransferHandlerStudy extends TransferHandler {

    DataFlavor nodesFlavor;
    DataFlavor[] flavors = new DataFlavor[1];
    DefaultMutableTreeTableNode[] nodesToRemove;
    ROI_TreeTable jTreeTableSrc, jTreeTableDest;   
        
    public TreeTransferHandlerStudy() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType
                    + ";class=\""
                    + DefaultMutableTreeTableNode[].class.getName()
                    + "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFound: ");
            e.printStackTrace(System.out);
        }
    }

    @Override
    public boolean canImport(TransferSupport support) {
        jTreeTableDest = (ROI_TreeTable) support.getComponent();
        
        if ((jTreeTableDest == null) || (jTreeTableSrc == null)){
            return false;
        }
        
        if (!support.isDrop()) {
            return false;
        }
        support.setShowDropLocation(true);
        if (!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }
        // Check for dummy node created in this class
        DefaultMutableTreeTableNode[] transferData;
        try {
            Transferable t = support.getTransferable();
            transferData = (DefaultMutableTreeTableNode[]) t.getTransferData(nodesFlavor);
            if (transferData.length == 0){
                return false;
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace(System.out);
        }
        
        JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
        // false if dest is null 
        if ( dl.getRow() == -1 ){
            return false;
        }
        
        // Do not allow a drop on the drag source selections.    
        if (jTreeTableDest.equals(jTreeTableSrc)){
            TreePath destNode = jTreeTableDest.getPathForLocation(dl.getDropPoint().x, dl.getDropPoint().y);            
            for (TreePath path:jTreeTableSrc.getSelectionPaths()) {
                if (destNode.getLastPathComponent().equals(path.getLastPathComponent())){
                    return false;
                }
            }        
        }
        
        // only allow transfer from within one catagory/group     
        if (jTreeTableSrc.getSelectionPath() != null){
            Object[] firstPath = jTreeTableSrc.getSelectionPath().getPath();
            for (TreePath tPath:jTreeTableSrc.getSelectionPaths()){
                Object[] path = tPath.getPath();
                boolean same = path.length == firstPath.length;
                for (int i = 0; (i < path.length-1) && (i < firstPath.length-1); i++){
                    same  &= path[i].equals(firstPath[i]);
                }
                if (!same){
                    return false;
                }
            }
        }        
        // Make sure AnnotationSets are the same
        
        AnnotationSet srcAnnoSet = jTreeTableSrc.getAnnotationSet();
        AnnotationSet destAnnoSet = jTreeTableDest.getAnnotationSet();
        
        TreePath destNode = jTreeTableDest.getPathForLocation(dl.getDropPoint().x, dl.getDropPoint().y);
        if (destNode == null){
            return false;
        }

        byte[] srcHash = srcAnnoSet.imageReference.hash;
        byte[] destHash = destAnnoSet.imageReference.hash;
        if (!ImageID.hashesAreEqual(srcHash, destHash)){
            return false;
        }
                
        // Do not allow MOVE-action drops if a non-leaf node is
        // selected unless all of its children are also selected.
        int action = support.getDropAction();
        if (action == MOVE) {
            return haveCompleteNode(jTreeTableSrc);
        }
        return true;
    }

    private boolean haveCompleteNode(ROI_TreeTable tree) {
        ArrayList<MutableTreeTableNode> selectedNodes = new ArrayList<>();
        for (TreePath path:tree.getSelectionPaths()){
            selectedNodes.add((MutableTreeTableNode)path.getLastPathComponent());
        }
        if (selectedNodes.isEmpty()){
            return true;
        }
        
        for ( MutableTreeTableNode node:selectedNodes){
            Enumeration<? extends MutableTreeTableNode> children = node.children();
            while (children.hasMoreElements()){
                MutableTreeTableNode child = children.nextElement();
                if (!selectedNodes.contains(child)){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean containsROIsAndAnnotations(TreePath[] paths){
        Boolean containsROIs = false, containsAnnotations = false;
        for (TreePath p:paths){
            Object comp = p.getLastPathComponent();
            if (comp instanceof ROI_Folder){
                containsAnnotations = true;
            }
            if (comp instanceof ROI){
                containsROIs = true;
            }
        }
        if (containsROIs && containsAnnotations){
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    protected Transferable createTransferable(JComponent c) {
        jTreeTableSrc = (ROI_TreeTable) c;
        TreePath[] paths = jTreeTableSrc.getSelectionPaths();
        if (paths == null) {
            return null;
        }
        
        // Check if contains rois and annotations. Don't want to handle this case
        if (containsROIsAndAnnotations( paths )){
            return new NodesTransferable(new DefaultMutableTreeTableNode[0]);
        }
        
        ArrayList<DefaultMutableTreeTableNode> nodesArrayList = new ArrayList<>();
        for (TreePath path:paths){
            nodesArrayList.add((DefaultMutableTreeTableNode)path.getLastPathComponent());
        }

        DefaultMutableTreeTableNode[] nodes = nodesArrayList.toArray(new DefaultMutableTreeTableNode[nodesArrayList.size()]);
        nodesToRemove = nodes;
        return new NodesTransferable(nodes);
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (jTreeTableSrc == null || jTreeTableDest == null){
            return;
        }
        if (jTreeTableDest.equals(jTreeTableSrc)){
            if ((action & MOVE) == MOVE) {
                JXTreeTable tree = (JXTreeTable) source;
                DefaultTreeTableModel model = (DefaultTreeTableModel) tree.getTreeTableModel();
                // Remove nodes saved in nodesToRemove in createTransferable.
                for (DefaultMutableTreeTableNode nodeToRemove : nodesToRemove) {
                    TreeTableNode[] path = model.getPathToRoot(nodeToRemove);
                    if (path.length == 3){
                        Object o1 = path[1];
                        Object o2 = path[2];
                        if ((o1 instanceof ROI_Folder) && (o2 instanceof ROI)){
                            ROI_Folder folder = (ROI_Folder) o1;
                            ROI roi = (ROI) o2;
                            if (folder.toString().equals(MessageStrings.Temporary)) {
                                model.removeNodeFromParent(roi);
                            } else {
                                folder.remove(roi, false);
                            }
                        }
                    }
                    if (path.length == 2){
                        Object o0 = path[0];
                        Object o1 = path[1];
                        if ((o0 instanceof AnnotationSet) && (o1 instanceof ROI_Folder)){
                            AnnotationSet annoSet =  (AnnotationSet) o0;
                            ROI_Folder folder =  (ROI_Folder) o1;
                            if (folder.toString().equals(MessageStrings.Temporary)) {
                                model.removeNodeFromParent(folder);
                            } else {
                                annoSet.remove(folder, false);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        jTreeTableSrc = (ROI_TreeTable) c;
        return COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        jTreeTableDest = (ROI_TreeTable) support.getComponent();
        if (!canImport(support)) {
            return false;
        }
        // Extract transfer data.
        DefaultMutableTreeTableNode[] nodes = null;
        try {
            Transferable t = support.getTransferable();
            nodes = (DefaultMutableTreeTableNode[]) t.getTransferData(nodesFlavor);
        } catch (UnsupportedFlavorException ufe) {
            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
            ufe.printStackTrace(System.out);
        } catch (java.io.IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
            ioe.printStackTrace(System.out);
        }
        // Get drop location info.
        JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
        TreePath dest = jTreeTableDest.getPathForRow(dl.getRow());
        DefaultMutableTreeTableNode parent = (DefaultMutableTreeTableNode) dest.getLastPathComponent();
                
        // Add data to model.
        for (DefaultMutableTreeTableNode node:nodes) {
            AnnotationSet srcAnnoSet;   
            ROI_Folder srcFolder       = null;
            ROI srcROI               = null;

            AnnotationSet   destAnnoSet         = null;
            ROI_Folder      destFolder            = null;               

            if (node instanceof ROI_Folder) {
                srcFolder = (ROI_Folder) node;
            } else if (node instanceof ROI) {
                srcROI = (ROI) node;
                srcFolder = (ROI_Folder) srcROI.getParent();                    
            }
            srcAnnoSet = (AnnotationSet) srcFolder.getParent();

            if (srcAnnoSet == null){
                return false;
            }
            if (parent instanceof AnnotationSet){
                destAnnoSet = (AnnotationSet)parent;
            } else if (parent instanceof ROI_Folder){
                destFolder = (ROI_Folder)parent;   
            } else if (parent instanceof ROI){
                destFolder = (ROI_Folder)parent.getParent();
            }
           
            if (destFolder == null) {
                destFolder = getDefaultAnno(destAnnoSet);
                if (destFolder == null) {
                    destFolder = ROI_Folder.createDefault();
                    destFolder.setName("Label", true);
                    destAnnoSet.add(destFolder, false);
                }
            } 
            // Configure for drop mode.
            int index;
            if (parent instanceof ROI){
                index = ((DefaultMutableTreeTableNode)dest.getLastPathComponent()).getIndex((DefaultMutableTreeTableNode)dest.getLastPathComponent());// DropMode.INSERT
                if (index == -1) {              // DropMode.ON
                    index = parent.getChildCount();
                }
            } else {
                index = destFolder.getChildCount();
            }
            
            destFolder.insert((MutableTreeTableNode) srcROI.clone(), index, false);

        }
        return true;
    }
    
    private ROI_Folder getDefaultAnno(AnnotationSet annoSet){
        for (ROI_Folder anno:annoSet.getROI_Folders()){
            if ("Default".equals(anno.getName())){
                return anno;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return getClass().getName();
    }

    public class NodesTransferable implements Transferable {

        DefaultMutableTreeTableNode[] nodes;

        public NodesTransferable(DefaultMutableTreeTableNode[] nodes) {
            this.nodes = nodes;
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return nodes;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }
}
