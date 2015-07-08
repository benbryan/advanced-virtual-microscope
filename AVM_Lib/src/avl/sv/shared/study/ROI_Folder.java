package avl.sv.shared.study;

import avl.sv.shared.MessageStrings;
import avl.sv.shared.study.xml.ROI_Folder_XML_Parser;
import avl.sv.shared.study.xml.ROI_Folder_XML_Writer;
import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.xml.sax.SAXException;

public class ROI_Folder extends DefaultMutableTreeTableNode implements Serializable, Cloneable {

    public ArrayList<Attribute> attributes;

    /**
     * A unique long identifier for this folder
     */
    public long id;
    private Color lineColor;
    private String name;
    public boolean visible, selected;
    HashSet<ListenerROI_Folder> listeners;
    final ListenerROI roiListener;
    public boolean initialized;
    
    private ROI_Folder() {
        this.id = (long) ((Math.pow(2, 8) * (new Date()).getTime()) + (Math.random() * 255));
        listeners = new HashSet<>();
        final ROI_Folder folder = this;
        roiListener = new ListenerROI() {
            @Override
            public void updated(ROI originalROI, ROI newROI) {
                for (ListenerROI_Folder listenerROI : listeners) {
                    listenerROI.updated(folder, originalROI, newROI);
                }
            }
        };
        initialized = false;
    }

    @Override
    public ROI_Folder clone(){
        try {
            return ROI_Folder.parse(toXML(true));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(ROI_Folder.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }    

    public String toXML(boolean includeROIs) {
        return ROI_Folder_XML_Writer.getXMLString(this, includeROIs);
    }

    public static ROI_Folder parse(String xml) throws ParserConfigurationException, SAXException, IOException {
        return ROI_Folder_XML_Parser.parse(xml);
    }

    public void addListener(ListenerROI_Folder listenerROI) {
        listeners.add(listenerROI);
    }

    public void removeListener(ListenerROI_Folder listenerROI) {
        listeners.remove(listenerROI);
    }

    public String getName() {
        return name;
    }

    public Color getLineColor() {
        return lineColor;
    }

    synchronized public void setLineColor(Color lineColor, boolean direct) {
        if (this.lineColor.equals(lineColor)){
            return;
        }
        if (direct){
            this.lineColor = lineColor;
        } else {
            for (ListenerROI_Folder listener : listeners) {
                ROI_Folder temp = (ROI_Folder)this.clone();
                temp.setLineColor(lineColor, true);
                listener.updated(temp);
            }
        }
    }
        
    synchronized public void setName(String name, boolean direct){
        if (this.name.equals(name)){
            return;
        }
        if (direct){
            this.name = name;
        } else {
            for (ListenerROI_Folder listener : listeners) {
                ROI_Folder temp = (ROI_Folder)this.clone();
                temp.setName(name, true);
                listener.updated(temp);
            }        
        }
    }

    public static ROI_Folder createDefault() {
        ROI_Folder anno = new ROI_Folder();
        anno.attributes = new ArrayList<>();
        anno.lineColor = Color.green;
        anno.name = MessageStrings.NEW_FOLDER;
        anno.visible = true;
        anno.selected = true;
        return anno;
    }  
    
    synchronized public void add(MutableTreeTableNode child, boolean direct) {
        // make sure child is of roi and that its name is unique
        if (child instanceof ROI) {
            insert((ROI) child, getChildCount(), direct);
        }
    }
    
    @Override
    synchronized public void add(MutableTreeTableNode child) {
        add(child, true);
    }

    public ArrayList<ROI> getROIs() {
        if (children == null) {
            return new ArrayList<>();
        }
        ArrayList<ROI> rois = new ArrayList<>();
        for (Object r : children) {
            if (r instanceof ROI) {
                rois.add((ROI) r);
            }
        }

        return rois;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    synchronized public void setUserObject(Object userObject) {
        setUserObject(userObject, false);
    }

    synchronized public void setUserObject(Object userObject, boolean direct) {
        if (userObject instanceof String) {
            String name = (String) userObject;
            setName(name, direct);
        } 
    }

    @Override
    synchronized public void remove(MutableTreeTableNode node) {
        remove(node, true);
    }

    synchronized public void remove(MutableTreeTableNode node, boolean direct) {
        if (node instanceof ROI) {
            ROI roi = (ROI) node;
            if (direct) {
                roi.removeListener(roiListener);
                super.remove(node);
            } else {
                for (ListenerROI_Folder listener : listeners) {
                    listener.remove(this, roi);
                }
            }
        }
    }

    @Override
    synchronized public void insert(MutableTreeTableNode child, int index) {
        insert(child, index, true); //To change body of generated methods, choose Tools | Templates.
    }

    synchronized public void insert(MutableTreeTableNode child, int index, boolean direct) {
        // make sure child is of roi and that its name is unique
        if (child instanceof ROI) {
            final ROI roi = (ROI) child;
            if (direct) {
                roi.addListener(roiListener);
                super.insert(child, index);
            } else {
                for (ListenerROI_Folder listener : listeners) {
                    listener.add(this, roi);
                }
            }
        }
    }

}
