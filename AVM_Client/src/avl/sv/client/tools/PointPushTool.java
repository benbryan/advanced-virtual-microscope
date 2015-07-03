package avl.sv.client.tools;

import avl.sv.client.image.ImageViewer;
import avl.sv.shared.study.ROI;
import avl.sv.shared.study.ROIPoly;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import javax.swing.ImageIcon;

public class PointPushTool extends AbstractImageViewerTool {

    private MouseActionLogger mouseAction = new MouseActionLogger();
    private final ImageViewer imageViewer;

    @Override
    public AbstractImageViewerTool getNewInstance(ImageViewer imageViewer) {
        return new PointPushTool(imageViewer);
    }
    
    public static String toolTipText = "Point Push Tool";
    public static ImageIcon imageIcon = new ImageIcon(PointPushTool.class.getResource("/avl/sv/client/icon/pointPush.png"));
    public PointPushTool() {
        setToolTipText(toolTipText);
        setIcon(imageIcon);
        imageViewer = null;
    }   
    
    public PointPushTool(ImageViewer imageViewer) {
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
        shiftPoints(e.getPoint());
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
        shiftPoints(e.getPoint());
        insertPoints(e.getPoint());
        imageViewer.repaint();
    }

    private void shiftPoints(Point p) {
        if (mouseAction.getMouseButton1() == MouseActionLogger.ButtonStatus.PRESSED) {
            Point pImg = imageViewer.displaySpaceToImageSpace(p);
            ArrayList<ROI> rois = imageViewer.getROI_TreeTable().getVisibleROIs();          
            Graphics2D g = (Graphics2D) imageViewer.getGraphics().create();
            imageViewer.concatenateImageToDisplayTransform(g);
            for (ROI roi : rois) {
                ROI original = (ROI) roi.clone();
                if (!roi.selected) {
                    continue;
                }
                Polygon temp = roi.getPolygon();
                int x[] = temp.xpoints;
                int y[] = temp.ypoints;
                for (int j = 0; j < x.length; j++) {
                    Point p2 = new Point(x[j], y[j]);
                    double dist = pImg.distance(p2);
                    if (dist < circleShape.diameter / 2/imageViewer.getMagnification()) {
                        shiftPoint(roi, j, pImg);
                        roi.setModified(original, true);
                    }
                }
                if (roi instanceof ROIPoly) {
                    cleanupROI((ROIPoly) roi);
                }
            }
        }
    }
    
    private void shiftPoint(ROI roi, int pointIdx, Point pImg) {
        int cx = (int) pImg.getX();
        int cy = (int) pImg.getY();
        Polygon temp = roi.getPolygon();
        int rx = temp.xpoints[pointIdx];
        int ry = temp.ypoints[pointIdx];
        double radius = (circleShape.diameter/2/imageViewer.getMagnification());
        double dx = (cx - rx);
        double dy = (cy - ry);
        double theta = Math.atan2(dy,dx);
        rx = (int) (cx-(radius*Math.cos(theta)));
        ry = (int) (cy-(radius*Math.sin(theta)));
        roi.changePoint(pointIdx, rx, ry);
    }
    
    private void cleanupROI(ROIPoly roiPoly){
        // Removes points 
        boolean finished = false;
        while (!finished){
            finished = true;
            Polygon poly = roiPoly.getPolygon();
            for (int i = 1; i < (poly.npoints - 1); i++) {
                int x0 = poly.xpoints[i - 1];
                int y0 = poly.ypoints[i - 1];
                int x1 = poly.xpoints[i + 0];
                int y1 = poly.ypoints[i + 0];
                int x2 = poly.xpoints[i + 1];
                int y2 = poly.ypoints[i + 1];
                
                double theta0 = Math.PI+Math.atan2(y2-y1, x2-x1);
                double theta2 = Math.PI+Math.atan2(y0-y1, x0-x1);
                double theta = theta2-theta0;
                if (Math.abs(theta) < Math.PI/10){
                    Path2D.Double tempPath = new Path2D.Double();            
                    tempPath.moveTo(poly.xpoints[0], poly.ypoints[0]);
                    for (int j = 1; j < poly.npoints; j++) {
                        if (i == j) {
                            continue;
                        }
                        tempPath.lineTo(poly.xpoints[j], poly.ypoints[j]);
                    }
                    
                    roiPoly.setPath(tempPath);
                    finished = false;
                    break;
                }
            }
        }
    }
    
    private void insertPoints(Point p){
        ArrayList<ROI> rois = imageViewer.getROI_TreeTable().getVisibleROIs();
        if (rois == null) {
            return;
        }
        double radius = (circleShape.diameter/2/imageViewer.getMagnification());
        Graphics2D g = (Graphics2D) imageViewer.getGraphics().create();
        imageViewer.concatenateImageToDisplayTransform(g);
        Point pImg = imageViewer.displaySpaceToImageSpace(p);
        for (ROI roi : rois) {
            if (!roi.selected) {
                continue;
            }
            if (roi instanceof ROIPoly) {
                ROIPoly roiPoly = (ROIPoly) roi;
                Polygon poly = roiPoly.getPolygon();
                if (!poly.getBounds().contains(pImg)) {
                    continue;
                }
                double minDist = radius*0.9;
                int minIdx = -1;
                for (int i = 0; i < (poly.npoints - 1); i++) {
                    int x0 = poly.xpoints[i];
                    int y0 = poly.ypoints[i];
                    int x1 = poly.xpoints[i + 1];
                    int y1 = poly.ypoints[i + 1];
                    if (Point.distance(x0, y0, x1, y1) < radius/4){
                        continue;
                    }
                    double dist = Line2D.ptSegDist(x0, y0, x1, y1, pImg.x, pImg.y);
                    if (dist < minDist){
                        minDist = dist;
                        minIdx = i;
                    }
                }
                if (minIdx >= 0){    
                    int x0 = poly.xpoints[minIdx];
                    int y0 = poly.ypoints[minIdx];
                    int x1 = poly.xpoints[minIdx + 1];
                    int y1 = poly.ypoints[minIdx + 1];
                    // Insert the new point into the poly and select the new point
                    double theta = Math.atan2(y1-y0, x1-x0);
                    Point pNew;
                    Point pNew0 = new Point((int)(pImg.x-radius*Math.cos(theta+Math.PI/2)), (int)(pImg.y-radius*Math.sin(theta+Math.PI/2)));
                    Point pNew1 = new Point((int)(pImg.x-radius*Math.cos(theta-Math.PI/2)), (int)(pImg.y-radius*Math.sin(theta-Math.PI/2)));
                    if (Line2D.ptSegDist(x0, y0, x1, y1, pNew0.x, pNew0.y) < Line2D.ptSegDist(x0, y0, x1, y1, pNew1.x, pNew1.y)){
                        pNew = pNew0;
                    } else {
                        pNew = pNew1;
                    }

                    Path2D.Double tempPath = new Path2D.Double();
                    tempPath.moveTo(poly.xpoints[0], poly.ypoints[0]);
                    int jump = 0;
                    for (int j = 1; j < poly.npoints + 1; j++) {
                        if ((minIdx + 1) == j) {
                            tempPath.lineTo(pNew.x, pNew.y);
                            jump = -1;
                        } else {
                            tempPath.lineTo(poly.xpoints[j + jump], poly.ypoints[j + jump]);
                        }
                    }
                    roiPoly.setPath(tempPath);
                    break;
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        mouseAction.mousePressed(e);
        shiftPoints(e.getPoint());
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public boolean canModify() {
        return true;
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
