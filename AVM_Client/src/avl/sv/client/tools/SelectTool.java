package avl.sv.client.tools;

import avl.sv.client.study.ExportDialogROIs;
import avl.sv.client.image.ImageViewer;
import avl.sv.shared.study.ROI;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

public class SelectTool extends AbstractImageViewerTool {

    private MouseActionLogger mouseAction = new MouseActionLogger();

    final ImageViewer imageViewer;
    
    public static String toolTipText = "Select ROIs. Click multiple times on the same spot to cycle through overlapping ROIs";
    public static ImageIcon imageIcon = new ImageIcon(SelectTool.class.getResource("/avl/sv/client/icon/select.png"));
    public SelectTool() {
        setToolTipText(toolTipText);
        setIcon(imageIcon);
        imageViewer = null;
    }    
    
    public SelectTool(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }
    
    @Override
    public boolean canModify() {
        return false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (!e.getSource().equals(imageViewer)) {
            return;
        }

        mouseAction.mouseClicked(e);
        ArrayList<ROI> rois = imageViewer.getROI_TreeTable().getVisibleROIs();
        if (rois == null) {
            return;
        }
        Point pImg = imageViewer.displaySpaceToImageSpace(e.getPoint());
        if (e.getButton() == MouseEvent.BUTTON1) {
            ArrayList<TreePath> paths = new ArrayList<>();
            for (ROI roi : rois) {
                if (roi.containsPoint(pImg)) {
                    Object o[] = new Object[3];
                    o[0] = roi.getParent().getParent();
                    o[1] = roi.getParent();
                    o[2] = roi;
                    paths.add(new TreePath(o));
                }
            }
            if ((paths.size()) > 0 && (mouseAction.getClickCount() > 0)) {
                // if click count is greater than one, then select a single ROI to highlight
                int idx = mouseAction.getClickCount() % paths.size();
                if (idx < paths.size()) {
                    ArrayList<TreePath> pathsTemp = new ArrayList<>();
                    pathsTemp.add(paths.get(idx));
                    paths = pathsTemp;
                }
            }
            imageViewer.getROI_TreeTable().setSelectionPaths(paths);
            imageViewer.repaint();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            final ArrayList<ROI> selectedROIs = new ArrayList<>();
            for (ROI roi:rois){
                if (roi.selected){
                    selectedROIs.add(roi);
                }
            }
            if (selectedROIs.isEmpty()){
                return;
            }
            JPopupMenu menu = new JPopupMenu();
            menu.add(new JMenuItem(new AbstractAction("Goto") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int lx = Integer.MAX_VALUE, ux = Integer.MIN_VALUE;
                    int ly = Integer.MAX_VALUE, uy = Integer.MIN_VALUE;
                    for (ROI roi:selectedROIs){
                        Rectangle bounds = roi.getPolygon().getBounds();
                        lx = Math.min(lx, bounds.x);
                        ly = Math.min(ly, bounds.y);
                        ux = Math.max(ux, bounds.x+bounds.width);
                        uy = Math.max(uy, bounds.y+bounds.height);
                    }                                       
                    imageViewer.setImageRegion(new Rectangle(lx, ly, ux-lx, uy-ly));
                }
            }));
            menu.add(new JMenuItem(new AbstractAction("Delete") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (ROI roi:selectedROIs){
                        imageViewer.getROI_TreeTable().removeROI(roi);
                        imageViewer.repaint();
                    }
                }
            }));
            menu.add(new JMenuItem(new AbstractAction("Export") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ExportDialogROIs exportDialogROIs = new ExportDialogROIs(null, true, imageViewer.getImageSource());
                    exportDialogROIs.promptForExport(selectedROIs.toArray());
                }
            }));

            menu.show(imageViewer, e.getX(), e.getY());
            return;
        }
        imageViewer.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        mouseAction.mouseReleased(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        mouseAction.mouseDragged(e);

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        mouseAction.mousePressed(e);

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void paintOnImageViewer(Graphics2D g) {
    }

    @Override
    public AbstractImageViewerTool getNewInstance(ImageViewer imageViewer) {
        return new SelectTool(imageViewer);
    }

}
