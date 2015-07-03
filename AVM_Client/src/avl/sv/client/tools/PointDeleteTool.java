package avl.sv.client.tools;

import avl.sv.client.image.ImageViewer;
import avl.sv.shared.study.ROI;
import avl.sv.shared.study.ROIPoly;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import javax.swing.ImageIcon;

public class PointDeleteTool extends AbstractImageViewerTool {

    private MouseActionLogger mouseAction = new MouseActionLogger();
    private final ImageViewer imageViewer;

    @Override
    public AbstractImageViewerTool getNewInstance(ImageViewer imageViewer) {
        return new PointDeleteTool(imageViewer);
    }
    
    @Override
    public boolean canModify() {
        return true;
    }
    
    public static String toolTipText = "Point Delete Tool";
    public static ImageIcon imageIcon = new ImageIcon(PointDeleteTool.class.getResource("/avl/sv/client/icon/pointDelete.png"));
    public PointDeleteTool() {
        setToolTipText(toolTipText);
        setIcon(imageIcon);
        imageViewer = null;
    }   
    
    public PointDeleteTool(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }

    @Override
    public void mouseMoved(MouseEvent e) { 
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        if (circleShape != null){
            circleShape.x = e.getPoint().x;
            circleShape.y = e.getPoint().y;            
        }
        mouseAction.mouseMoved(e);
        imageViewer.repaint();
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
        removePoints(e.getPoint());
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

        if (circleShape != null){
            circleShape.x = e.getPoint().x;
            circleShape.y = e.getPoint().y;            
        } else {
            return;
        }
        mouseAction.mouseDragged(e);
        removePoints(e.getPoint());
        imageViewer.repaint();
    }

    private void removePoints(Point p) {
        if (mouseAction.getMouseButton1() == MouseActionLogger.ButtonStatus.PRESSED) {
            Point pImg = imageViewer.displaySpaceToImageSpace(p);
            ArrayList<ROI> rois = imageViewer.getROI_TreeTable().getVisibleROIs();
            Graphics2D g = (Graphics2D) imageViewer.getGraphics().create();
            imageViewer.concatenateImageToDisplayTransform(g);
            for (ROI roi : rois) {
                if (!roi.selected) {
                    continue;
                }
                if (roi instanceof ROIPoly) {
                    ROIPoly roiPoly = (ROIPoly) roi;
                    ROI original = (ROI) roiPoly.clone();
                    Polygon temp = roi.getPolygon();
                    int x[] = temp.xpoints;
                    int y[] = temp.ypoints;
                    ArrayList<Integer> toRemove = new ArrayList<>();
                    for (int j = 0; j < x.length; j++) {
                        Point p2 = new Point(x[j], y[j]);
                        double dist = pImg.distance(p2);
                        if (dist < circleShape.diameter / 2 / imageViewer.getMagnification()) {
                            toRemove.add(j);
                        }
                    }
                    removePoint(roiPoly, toRemove);
                    if (!toRemove.isEmpty()){
                        roi.setModified(original, true);
                    }
                }
            }
        }
    }
        
    private void removePoint(ROIPoly roiPoly, ArrayList<Integer> idxsToRemove){
        Polygon poly = roiPoly.getPolygon();
        ArrayList<Integer> keepers = new ArrayList<>();
        for (int i = 0; i < poly.npoints; i++ ){
            keepers.add(i);
        }
        keepers.removeAll(idxsToRemove);        
        Path2D.Double tempPath = new Path2D.Double();
        if (keepers.isEmpty()){
            return;
        }
        int idx = keepers.remove(0);
        tempPath.moveTo(poly.xpoints[idx], poly.ypoints[idx]);
        for (Integer keeper:keepers) {
            tempPath.lineTo(poly.xpoints[keeper], poly.ypoints[keeper]);
        }
        roiPoly.setPath(tempPath);
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        mouseAction.mousePressed(e);
        removePoints(e.getPoint());
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private class CircleShape{
        int x = 0, y = 0, diameter = 40;
    }
    
    private CircleShape circleShape = new CircleShape();
    
    @Override
    public void paintOnImageViewer(Graphics2D g) {
        
        if (circleShape != null){
            g.drawOval(circleShape.x-circleShape.diameter/2, circleShape.y-circleShape.diameter/2, circleShape.diameter, circleShape.diameter);
        }
    }
}
