package avl.sv.client.tools;

import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.client.image.ImageViewer;
import avl.sv.shared.study.ROIPoly;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;

public class FreehandTool extends AbstractImageViewerTool {
    private ROIPoly workingROI = null;
    private MouseActionLogger mouseAction = new MouseActionLogger();

    final ImageViewer imageViewer;
    private Point startPoint;
    
    public static String toolTipText = "Freehand Tool";
    public static ImageIcon imageIcon = new ImageIcon(FreehandTool.class.getResource("/avl/sv/client/icon/freehand.png"));
    public FreehandTool() {
        setToolTipText(toolTipText);
        setIcon(imageIcon);
        imageViewer = null;
    }  
    
    public FreehandTool(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        mouseAction.mouseClicked(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        mouseAction.mouseReleased(e);
        if (mouseAction.getMouseButton1() == MouseActionLogger.ButtonStatus.RELEASED) {
            if (workingROI == null){
                return;
            }
            workingROI.addPoint(startPoint.x, startPoint.y);
            imageViewer.getROI_TreeTable().addROI(workingROI);
            workingROI = null;
            AdvancedVirtualMicroscope.getToolPanel().setDefaultTool();
        }
        imageViewer.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        mouseAction.mouseDragged(e);
        if (mouseAction.getMouseButton1() == MouseActionLogger.ButtonStatus.PRESSED) {
            Point pImg = imageViewer.displaySpaceToImageSpace(e.getPoint());
            addingNewRoi(pImg);
        }
        imageViewer.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        mouseAction.mousePressed(e);

        if (mouseAction.getMouseButton1() == MouseActionLogger.ButtonStatus.PRESSED) {
            Point pImg = imageViewer.displaySpaceToImageSpace(e.getPoint());
            addingNewRoi(pImg);
        }
        imageViewer.repaint();
    }

    private void addingNewRoi(Point pImg) {
        int ix = (int) pImg.getX();
        int iy = (int) pImg.getY();

        if (workingROI == null) {
            workingROI = ROIPoly.getDefault();
            startPoint = new Point(pImg);
        }
        workingROI.addPoint(ix, iy);

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    @Override
    public void paintOnImageViewer(Graphics2D g) {
        if (workingROI != null){
            imageViewer.concatenateImageToDisplayTransform(g);
            workingROI.paintROI(g, Color.GREEN);
        }
    }

    @Override
    public AbstractImageViewerTool getNewInstance(ImageViewer imageViewer) {
        return new FreehandTool(imageViewer);
    }

    @Override
    public boolean canModify() {
        return true;
    }
    
}
