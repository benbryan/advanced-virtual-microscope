package avl.sv.client.tools;

import avl.sv.client.image.ImageViewer;
import avl.sv.shared.study.ROI;
import avl.sv.shared.study.ROIPoly;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import javax.swing.ImageIcon;

public class PointTool extends AbstractImageViewerTool {

    private MouseActionLogger mouseAction = new MouseActionLogger();
    private ROI mouse1SelectedROI;
    private int mouse1selectedPointIndex;
    private final ImageViewer imageViewer;
    
    public static String toolTipText = "Point Tool";
    public static ImageIcon imageIcon = new ImageIcon(PointTool.class.getResource("/avl/sv/client/icon/point.png"));
    public PointTool() {
        setToolTipText(toolTipText);
        setIcon(imageIcon);
        imageViewer = null;
    }   
    
    @Override
    public boolean canModify() {
        return true;
    }
    
    public PointTool(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }

    @Override
    public void mouseMoved(MouseEvent e) { }

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
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        mouseAction.mouseDragged(e);

        if (mouseAction.getMouseButton1() == MouseActionLogger.ButtonStatus.PRESSED) {
            Point pImg = imageViewer.displaySpaceToImageSpace(e.getPoint());

            // For editing markers, dragging them around
            if (mouse1SelectedROI != null) {
                int ix = (int) pImg.getX();
                int iy = (int) pImg.getY();
                if (mouse1selectedPointIndex >= 0) {
                    mouse1SelectedROI.changePoint(mouse1selectedPointIndex, ix, iy);
                } else {
                    if (mouse1SelectedROI instanceof ROIPoly) {
                        ROIPoly roiPoly = (ROIPoly) mouse1SelectedROI;
                        Polygon poly = roiPoly.getPolygon();
                        for (int i = 0; i < (poly.npoints - 1); i++) {
                            int x0 = poly.xpoints[i];
                            int y0 = poly.ypoints[i];
                            int x1 = poly.xpoints[i + 1];
                            int y1 = poly.ypoints[i + 1];
                            if ((3 / imageViewer.getMagnification()) > Line2D.ptSegDist(x0, y0, x1, y1, ix, iy)) {
                                // Insert the new point into the poly and select the new point
                                Path2D.Double tempPath = new Path2D.Double();
                                tempPath.moveTo(poly.xpoints[0], poly.ypoints[0]);
                                int jump = 0;
                                for (int j = 1; j < poly.npoints + 1; j++) {
                                    if ((i + 1) == j) {
                                        tempPath.lineTo(ix, iy);
                                        jump = -1;
                                    } else {
                                        tempPath.lineTo(poly.xpoints[j + jump], poly.ypoints[j + jump]);
                                    }
                                }
                                roiPoly.setPath(tempPath);
                                mouse1SelectedROI = roiPoly;
                                mouse1selectedPointIndex = i + 1;
                                break;
                            }
                        }
                    }
                }
            }
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
            findMarkerClosestToCursor(e.getPoint());
        }
        imageViewer.repaint();
    }     
    
    private void findMarkerClosestToCursor(Point p) {
        ArrayList<ROI> rois = imageViewer.getROI_TreeTable().getVisibleROIs();
        ROI bestROI = null;
        int bestPointIdx = 0;
        double lowestDist = Double.MAX_VALUE;
        Graphics2D g = (Graphics2D) imageViewer.getGraphics().create();
        imageViewer.concatenateImageToDisplayTransform(g);
        AffineTransform at = g.getTransform();
        for (ROI roi : rois) {
            if (!roi.selected){
                continue;
            }
            Polygon temp = roi.getPolygon();
            int x[] = temp.xpoints;
            int y[] = temp.ypoints;
            for (int j = 0; j < x.length; j++) {
                Point p2 = new Point(x[j], y[j]);
                at.transform(p2, p2);
                double dist = p.distance(p2);
                if (dist < lowestDist) {
                    lowestDist = dist;
                    bestROI = roi;
                    bestPointIdx = j;
                }
            }
        }
        mouse1SelectedROI = bestROI;
        if (lowestDist < (cornerMarkerDim / 2)) {
            mouse1selectedPointIndex = bestPointIdx;
        } else {
            mouse1selectedPointIndex = -1;
        }
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
        return new PointTool(imageViewer);  
    }
}
