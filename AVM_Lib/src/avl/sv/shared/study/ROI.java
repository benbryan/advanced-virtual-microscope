package avl.sv.shared.study;

import avl.sv.shared.study.xml.ROI_XML_Parser;
import avl.sv.shared.study.xml.ROI_XML_Writer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.xml.parsers.ParserConfigurationException;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.xml.sax.SAXException;

public abstract class ROI extends DefaultMutableTreeTableNode implements Serializable, Cloneable {
    public ArrayList<Attribute> attributes;
    protected String name;
    public long id;
    public int displayId;
    public boolean selected = false, negativeROA, highlighted;
    private boolean analyze;
    private Timer updateTimer;
    private boolean isModified;   
    protected ROI_State state;
    private Date lastModified = new Date();
        
    HashSet<ListenerROI> listeners;
    private ExecutorService updateExecutor;
    public boolean isPosted = false;
    
    public void addListener(ListenerROI listenerROI){
        listeners.add(listenerROI);
    }
    public void removeListener(ListenerROI listenerROI){
        listeners.remove(listenerROI);
    }
    
    public boolean isAnalyze() {
        return analyze;
    }

    public void setAnalyze(boolean analyze) {
        setAnalyze(analyze, true);
    }

    public void setAnalyze(boolean analyze, boolean direct) {
        if (this.analyze == analyze) {
            return;
        }
        if (direct) {
            this.analyze = analyze;
        } else {
            for (ListenerROI listener : listeners) {
                ROI temp = (ROI)this.clone();
                temp.analyze = analyze;
                listener.updated(this, temp);
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setNameDirect(String name, boolean direct) {
        if (this.name.equals(name)){
            return;
        }
        if (direct){
            this.name = name;
            super.setUserObject(name);
        } else {
            for(ListenerROI listener:listeners){
                ROI temp = (ROI)this.clone();
                temp.setNameDirect(name, true);
                listener.updated(this, temp);
            }
        }
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }
    
    public ROI() {
        this.id = (long) ((Math.pow(2, 8)*(new Date()).getTime()) + (Math.random()*255));
        state = ROI_State.INIT;
        isModified = false;
        listeners = new HashSet<>();      
        updateTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTimer.stop();
                if (state.equals(ROI_State.CREATED)) {
                    if (updateExecutor != null){
                        updateExecutor.shutdownNow();
                    }
                    updateExecutor = Executors.newSingleThreadExecutor();
                    if (updateToRun != null){
                        updateExecutor.submit(updateToRun);
                    }
                }
            }
        });
        updateTimer.setRepeats(false);
    }
    
    public static ROI parse(String xml) throws ParserConfigurationException, SAXException, IOException {
        return ROI_XML_Parser.parse(xml);
    }
    public String toXML(){
        return ROI_XML_Writer.getXMLString(this);
    }
    
    public boolean isModified() {
        return isModified;
    }
    
    Updater updateToRun = null;

    abstract public boolean isFixedSize();

    public void setCreated(){
        state = ROI_State.CREATED;
    }
    
    public void setParsing(){
        state = ROI_State.PARSING;
    }
    
    class Updater implements Runnable{
        public final ROI originalROI, newROI;

        public Updater(ROI origianlROI, ROI newROI) {
            this.originalROI = origianlROI;
            this.newROI = newROI;
        }
        
        @Override
        public void run() {
            for (ListenerROI listener:listeners){
                listener.updated(originalROI, newROI);
            }
        }
    }

    public void setModified(ROI original, boolean isModified) {
        if (isModified && state.equals(ROI_State.CREATED)){
            lastModified = new Date();
            if (updateToRun != null){
                original = updateToRun.originalROI;
            }
            updateToRun = new Updater(original, this);
            updateTimer.restart();
        }
        this.isModified = isModified;
    }

    public Date getLastModified() {
        return lastModified;
    }
    
    public ArrayList<Integer> selectedPoints;
    
    public ArrayList<Integer> getSelectedPoints() {
        return selectedPoints;
    }

    public void setSelectedPoints(ArrayList<Integer> selectedPoints) {
        this.selectedPoints = selectedPoints;
    }
        
    public abstract int getType();
            
    public Polygon getPolygon(){
        return getPolygon(new AffineTransform());
    }
    public abstract Polygon getPolygon(AffineTransform at);

    abstract public void changePoint(int index, int nx, int ny);
    abstract public void changePoint(int index, double nx, double ny);
    
    public abstract boolean containsPoint(int x, int y);
    public abstract boolean containsPoint(Point p);
    
    public abstract Shape getShape();
    
    @Override
    public String toString() {
        return name;
    }
       
    public abstract void addPoint(int x, int y);
    public abstract void addPoint(double x, double y);
  
    @Override
    public void setUserObject(Object userObject) {
        setUserObject(userObject, false);
    }

    public void setUserObject(Object userObject, boolean direct) {
        if (userObject instanceof String){
            setNameDirect(name, direct);
        }
    }
    
    @Override
    public Object getUserObject() {
        return this;
    }   

    @Override
    public Object clone(){
        try {
            ROI r = (ROI) super.clone();
            r.updateToRun = null;
            r.attributes = new ArrayList<>();
            attributes.stream().map((attr) -> {
                Attribute a = new Attribute();
                a.id = attr.id;
                a.name = attr.name;
                a.value = attr.value;
                return a;
            }).forEach((a) -> {
                r.attributes.add(a);
            });             
            return r;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(ROI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void paintROI(Graphics2D g, Color color) {
        g = (Graphics2D) g.create();
        Shape s = getShape();
        if (s == null){
            return;
        }

        float zFactor = (float) (1 / g.getTransform().getScaleX());
        g.setColor(color);
        g.setStroke(new BasicStroke(5 * zFactor));
        g.draw(s);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1 * zFactor));
        g.draw(s);

        g.setStroke(new BasicStroke(3 * zFactor));

        // Fill the new roi
        g.setColor(Color.green);
        Color c = g.getColor();
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
        g.fill(s);

        g.setColor(Color.red);

    }

}
