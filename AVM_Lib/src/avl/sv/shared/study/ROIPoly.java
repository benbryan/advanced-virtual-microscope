package avl.sv.shared.study;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.Serializable;
import java.util.ArrayList;

public class ROIPoly extends ROI implements Serializable  {
    private Path2D.Double path;
    public static final int TYPE = 0;

    public final void append(PathIterator pi, boolean connect) {
        if (path == null){
            path = new Path2D.Double();
        }
        path.append(pi, connect);
    }
    
    private ROIPoly() {
    }
    
    public static ROIPoly getDefault() {
        ROIPoly r = new ROIPoly();
        r.attributes = new ArrayList<>();
        r.name = "Polygon";
        r.selectedPoints = new ArrayList<>();
        r.state = ROI_State.INIT;
        r.setModified(null, false);
        return r;
    }
    
    @Override
    public void addPoint(double x, double y){
        ROI original = (ROI) this.clone();
        if (path == null){
            path = new Path2D.Double();
        }
        if (path.getCurrentPoint() == null){
            path.moveTo(x, y);
        } else {
            path.lineTo(x, y);
        }
        
        setModified(original, true);
    }

    @Override
    public void addPoint(int x, int y) {
        addPoint((double)x, (double)y);
    }
    
    @Override
    public Polygon getPolygon(AffineTransform at){     
        if (path == null){
            path = new Path2D.Double();
        }
        PathIterator iter = path.getPathIterator(at);
        Polygon poly = new Polygon();
        while(!iter.isDone()){
            float coords[] = new float[6];
            switch (iter.currentSegment(coords)){
                case PathIterator.SEG_MOVETO:
                    poly.addPoint((int)coords[0], (int)coords[1]);
                    break; 
                case PathIterator.SEG_LINETO:
                    poly.addPoint((int)coords[0], (int)coords[1]);
                    break; 
                case PathIterator.SEG_CLOSE:
                    break;
            }
            iter.next();
        }
        return poly;
    }
    
    @Override
    public void changePoint(int index, int nx, int ny){
        changePoint(index, (double)nx, (double)ny);
    }
    
    @Override
    public void changePoint(int index, double nx, double ny){
        ROI original = (ROI) this.clone();
        PathIterator iter = path.getPathIterator(new AffineTransform());
        Path2D.Double newPath = new Path2D.Double();
        int newPathIdx = 0;
        while(!iter.isDone()){
            float coords[] = new float[6];
            switch (iter.currentSegment(coords)){
                case PathIterator.SEG_MOVETO:
                    if (newPathIdx++ == index){
                        newPath.moveTo(nx, ny);
                    } else {
                        newPath.moveTo(coords[0], coords[1]);
                    }
                    break; 
                case PathIterator.SEG_LINETO:
                    if (newPathIdx++ == index){
                        newPath.lineTo(nx, ny);
                    } else {
                        newPath.lineTo(coords[0], coords[1]);
                    }
                    break; 
                case PathIterator.SEG_CLOSE:
                    newPathIdx++;
                    newPath.closePath();
                    break;
            }
            iter.next();
        }
        path = newPath;
        setModified(original, true);
    }

    @Override
    public boolean containsPoint(int x, int y) {
        return path.contains(x, y);
    }

    @Override
    public boolean containsPoint(Point p) {
        return path.contains(p);
    }
    
    @Override
    public Shape getShape() {
        return path;
    }
    
    @Override
    public int getType() {
        return TYPE;
    }
    
    @Override
    public Object clone() {
        ROIPoly temp = (ROIPoly) super.clone();
        if (path == null){
            temp.path = null;
        } else {
            temp.path = (Path2D.Double) path.clone();
        }
        return temp;
    }

    @Override
    public boolean isFixedSize() {
        return false;
    }

    public void setPath(Path2D.Double path) {
        ROI original = (ROI) this.clone();
        this.path = path;
        setModified(original, true);
    }
    
}