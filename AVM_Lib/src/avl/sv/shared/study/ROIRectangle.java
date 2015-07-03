package avl.sv.shared.study;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class ROIRectangle extends ROI {

    private Polygon poly;
    private int fixedSize[];
    public static final int TYPE = 1;
    
    private ROIRectangle() {    }
    
    public static ROIRectangle getDefault() {
        ROIRectangle r = new ROIRectangle();
        r.attributes = new ArrayList<>();
        r.name = "Rect";
        r.selectedPoints = new ArrayList<>();
        r.fixedSize = null;
        r.state = ROI_State.INIT;
        r.poly = new Polygon();
        return r;
    }
    
    public void setRectangle(Rectangle r){
        ROI original = (ROI) this.clone();
        attributes = new ArrayList<>();
        selectedPoints = new ArrayList<>();
        int lx = r.x;
        int ly = r.y;
        int width = r.width;
        int height = r.height;       
        int ux = lx + width;
        int uy = ly + height;
        poly = new Polygon();
        poly.addPoint(lx, ly);
        poly.addPoint(lx, uy);
        poly.addPoint(ux, uy);
        poly.addPoint(ux, ly);
        poly.invalidate();
        if (state != ROI_State.CREATED){
            state = ROI_State.CREATED;            
        } else {
            setModified(original, true);
        }
    }

    public boolean isFixedSize() {
        return fixedSize!=null;
    }

    public static int getTYPE() {
        return TYPE;
    }

    public void setPoly(Polygon poly) {
        ROI original = (ROI) this.clone();
        this.poly = poly;
        state = ROI_State.CREATED;
        if (state != ROI_State.CREATED){
            state = ROI_State.CREATED;            
        } else {
            setModified(original, true);
        }
    }

    public void setFixedSize(int fixedSize[]) {
        if (fixedSize.length!=2){
            throw new IllegalArgumentException("Fixed size parameter should be of length 2. {width,height}");
        }
        this.fixedSize = fixedSize;
    }

    @Override
    public void addPoint(int x, int y) {
        ROI original = (ROI) this.clone();
        if (state == ROI_State.CREATED) {
            return;
        }
        poly.addPoint(x, y);
        poly.invalidate();
        if (poly.npoints == 4) {
            state = ROI_State.CREATED;
        } 
        setModified(original, true);
    }
    @Override
    public void addPoint(double x, double y) {
        addPoint((int)x, (int)y);
    }
    
    public boolean addSecondPoint(int nx, int ny){
        ROI original = (ROI) this.clone();
        int ix = poly.xpoints[0];
        int iy = poly.ypoints[0];
        int[] x,y;
        if (fixedSize != null){
            ix = nx-fixedSize[0];
            iy = ny-fixedSize[1];
        }
        x = new int[]{ix,nx,nx,ix};
        y = new int[]{iy,iy,ny,ny};
        poly.xpoints = x;
        poly.ypoints = y;        
        poly.npoints = 4;
        state = ROI_State.CREATED;
        setModified(original, true);
        return true;
    }
    
    @Override
    public Polygon getPolygon(AffineTransform at){     
        return poly;
    }
    
    @Override
    public void changePoint(int index, double nx, double ny){
        changePoint(index, (int) nx, (int) ny);
    }

    @Override
    public void changePoint(int index, int nx, int ny) {
        ROI original = (ROI) this.clone();
        int ox = poly.xpoints[index];
        int oy = poly.ypoints[index];
        if (fixedSize!= null) {
            poly.translate(nx-ox, ny-oy);
        } else {
            for (int i = 0; i < poly.npoints; i++) {
                if (poly.xpoints[i] == ox) {
                    poly.xpoints[i] = nx;
                }
                if (poly.ypoints[i] == oy) {
                    poly.ypoints[i] = ny;
                }
            }
            poly.xpoints[index] = nx;
            poly.ypoints[index] = ny;
        }
        setModified(original, true);
    }

    @Override
    public boolean containsPoint(int x, int y) {
        return poly.getBounds().contains(x,y);
    }
    
    @Override
    public boolean containsPoint(Point p) {
        return containsPoint(p.x,p.y);
    }

    @Override
    public Shape getShape() {
        return poly;
    }
    
    @Override
    public int getType() {
        return TYPE;
    }
    
    @Override
    public Object clone() {
        ROIRectangle temp = (ROIRectangle) super.clone();
        temp.poly = new Polygon();
        temp.poly.npoints = poly.npoints;
        temp.poly.xpoints = new int[temp.poly.npoints];
        temp.poly.ypoints = new int[temp.poly.npoints];
        for (int i = 0; i < poly.npoints; i++){
            temp.poly.xpoints[i] = poly.xpoints[i];
            temp.poly.ypoints[i] = poly.ypoints[i];
        }
        return temp;
    }
    
}
