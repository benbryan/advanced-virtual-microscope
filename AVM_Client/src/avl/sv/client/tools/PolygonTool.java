package avl.sv.client.tools;

import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.client.image.ImageViewer;
import avl.sv.shared.study.ROIPoly;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;

public class PolygonTool extends AbstractImageViewerTool {
    private ROIPoly roi = null;
    final ImageViewer imageViewer;
    private Point startPoint;
    private ROIPoly roiForPaint;

    public static String toolTipText = "Polygon Tool";
    public static ImageIcon imageIcon = new ImageIcon(PolygonTool.class.getResource("/avl/sv/client/icon/polygon.png"));
    public PolygonTool() {
        setToolTipText(toolTipText);
        setIcon(imageIcon);
        imageViewer = null;
    }    
    
    public PolygonTool(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }

    @Override
    public boolean canModify() {
        return true;
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        if (roi == null){
            return;
        }
        roiForPaint = (ROIPoly) roi.clone();
        Point pImg = imageViewer.displaySpaceToImageSpace(e.getPoint());
        roiForPaint.addPoint(pImg.x, pImg.y);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        Point pImg = imageViewer.displaySpaceToImageSpace(e.getPoint());
        int ix = (int) pImg.getX();
        int iy = (int) pImg.getY();

        if (roi == null) {
            roi = ROIPoly.getDefault();
            startPoint = new Point(ix, iy);
            //Iterates through the tiff dirs to find the lowest resolution in view
            roi.addPoint(ix, iy);
        } else if (new Point(ix, iy).distance(startPoint) < (5 / imageViewer.getMagnification()) || (e.getClickCount() > 1)) {
            roi.addPoint(ix, iy);
            closeROI();
        } else {
            roi.addPoint(ix, iy);
        }
        imageViewer.repaint();
    }
    
    private void closeROI() {
        roi.addPoint(startPoint.x, startPoint.y);
        imageViewer.getROI_TreeTable().addROI(roi);
        roi = null;
        roiForPaint = null;
        AdvancedVirtualMicroscope.getToolPanel().setDefaultTool();
    }

    @Override
    public void mouseReleased(MouseEvent e) {    }

    @Override
    public void mouseDragged(MouseEvent e) {    }

    @Override
    public void mousePressed(MouseEvent e) {   }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
    
    @Override
    public void paintOnImageViewer(Graphics2D g) {
        if (roiForPaint != null){
            imageViewer.concatenateImageToDisplayTransform(g);
            roiForPaint.paintROI(g, Color.GREEN);
        }
    }

    @Override
    public AbstractImageViewerTool getNewInstance(ImageViewer imageViewer) {
        return new PolygonTool(imageViewer);
    }
    
}
