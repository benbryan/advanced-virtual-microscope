package avl.sv.shared.study;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.ArrayList;

public class ROIOval extends ROI implements Serializable, Cloneable {
    public Ellipse2D.Double oval;
    public static final int TYPE = 2;
    public ArrayList<Point> parsingPoints;
    private int[] fixedSize;

    private ROIOval() {  }

    public void setOval(int centerX, int centerY, int width, int height) {
        ROI original = (ROI) this.clone();
        oval = new Ellipse2D.Double();
        oval.x = centerX-width/2;
        oval.y = centerY-height/2;
        oval.height = height;
        oval.width = width;
        if (state.equals(ROI_State.CREATED)){
            setModified(original, true);
        }
        state = ROI_State.CREATED;
    }
    
    public void start(int centerX, int centerY) {
        oval = new Ellipse2D.Double();
        oval.x = centerX;
        oval.y = centerY;
        if (fixedSize != null){
            oval.x -= fixedSize[0];
            oval.y -= fixedSize[1];
            oval.width = fixedSize[0];
            oval.height = fixedSize[1];
        }
        state = ROI_State.MANUAL;
    }
    
    public static ROIOval getDefault() {
        ROIOval r = new ROIOval();
        r.attributes = new ArrayList<>();
        r.name = "Oval";
        r.selectedPoints = new ArrayList<>();
        r.oval = new Ellipse2D.Double();
        r.state = ROI_State.PARSING;
        r.setModified(null, false);
        return r;
    }
    
    @Override
    public void addPoint(double x, double y) {
        addPoint((int)x, (int)y);
    }
                  
    @Override
    public void addPoint(int nx, int ny) {
        ROI original = (ROI) this.clone();
        if ((state == ROI_State.MANUAL) || (state == ROI_State.CREATED)) {
            if (fixedSize != null){
                oval.x = nx- fixedSize[0];
                oval.y = ny - fixedSize[1];
                oval.width = fixedSize[0];
                oval.height = fixedSize[1];
            } else {
                double centerX = oval.x + oval.width / 2;
                double centerY = oval.y + oval.height / 2;
                double width = 2*Math.abs(centerX-nx);
                double height = 2*Math.abs(centerY-ny);
                oval.x = centerX-width/2;
                oval.y = centerY-height/2;
                oval.height = height;
                oval.width = width;
            }
            state = ROI_State.CREATED;
            setModified(original, true);
        } else if (state == ROI_State.PARSING) {
            if (parsingPoints == null){
                parsingPoints = new ArrayList<>();
            }
            parsingPoints.add(new Point(nx, ny));
            if (parsingPoints.size() == 4){
                oval.x = parsingPoints.get(2).x;
                oval.y = parsingPoints.get(0).y;
                oval.width = parsingPoints.get(1).x - oval.x;
                oval.height = parsingPoints.get(3).y - oval.y;
                state = ROI_State.CREATED;
                setModified(original, true);
                return;
            }
        } 
    }
        
    @Override
    public Polygon getPolygon(AffineTransform at){  
        Polygon p = new Polygon();
        double x = oval.x;
        double y = oval.y;
        double w = oval.width;
        double h = oval.height;
        p.addPoint((int)(x+w/2), (int)(y));
        p.addPoint((int)(x+w), (int)(y+h/2));
        p.addPoint((int)(x), (int)(y+h/2));
        p.addPoint((int)(x+w/2), (int)(y+h));
        return p;
    }
    
    @Override
    public void changePoint(int index, double nx, double ny){
        changePoint(index, (int) nx, (int) ny);
    }
    
    @Override
    public void changePoint(int index, int nx, int ny) {
        ROI original = (ROI) this.clone();
        double x = oval.x;
        double y = oval.y;
        double w = oval.width;
        double h = oval.height;
        Point pn = new Point(nx, ny);
        Point ps[] = new Point[]{
            new Point((int)(x+w/2), (int)(y)),
            new Point((int)(x+w), (int)(y+h/2)),
            new Point((int)(x), (int)(y+h/2)),
            new Point((int)(x+w/2), (int)(y+h))};
        double bestDist = java.lang.Double.MAX_VALUE;
        int bestIdx = -1;
        for (int i = 0; i < ps.length; i++){
            double dist = pn.distance(ps[i]);
            if (bestDist > dist){
                bestDist = dist;
                bestIdx = i;
            }
        }
        double dy, dx;
        if (fixedSize != null){
            dy = ps[bestIdx].y - ny;
            dx = ps[bestIdx].x - nx;
            oval.y = y-dy;
            oval.x = x-dx;
        } else {
            switch (bestIdx){
                case 0:
                    //Fix 3, Mod y & h
                    dy = ps[0].y - ny;
                    oval.height += dy;
                    oval.y = y-dy;
                    break;
                case 1:
                    //Fix 2, Mod x & w
                    dx = ps[1].x - nx;
                    oval.width -= dx;
                    break;
                case 2:
                    //Fix 1, Mod x & w
                    dx = ps[2].x - nx;
                    oval.width += dx;
                    oval.x = x-dx;
                    break;
                case 3:
                    //Fix 0, Mod y & h
                    dy = ps[3].y - ny;
                    oval.height -= dy;
                    break;
            }
        }
        setModified(original, true);
    }

    @Override
    public boolean containsPoint(int x, int y) {
        return oval.contains(x, y);
    }    
    
    @Override
    public boolean containsPoint(Point p) {
        return oval.contains(p);
    }

    @Override
    public Shape getShape() {
        return oval;
    }

    @Override
    public int getType() {
        return TYPE;
    }
    
    
    @Override
    public Object clone() {
        ROIOval temp = (ROIOval) super.clone();
        temp.oval = (Ellipse2D.Double) oval.clone();
        return temp;
    }

    public void setFixedSize(int[] constraints) {
        if (constraints.length != 2){
            throw new IllegalArgumentException("Oval constraints should have length 2. {width, height}");
        }
        this.fixedSize = constraints;
    }

    @Override
    public boolean isFixedSize() {
        return fixedSize != null;
    }
    
}
