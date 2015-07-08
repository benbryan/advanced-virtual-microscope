package avl.sv.shared.study;

import avl.sv.shared.MessageStrings;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.study.xml.AnnotationSet_XML_Parser;
import avl.sv.shared.study.xml.AnnotationSet_XML_Writer;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import javax.xml.parsers.ParserConfigurationException;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.xml.sax.SAXException;

public class AnnotationSet extends DefaultMutableTreeTableNode implements Serializable {

    public final ImageReference imageReference;
    ListenerROI_Folder folderListener;
    HashSet<ListenerAnnotationSet> listeners;

    public AnnotationSet(ImageReference imageReference) {
        super(imageReference.imageName);
        this.imageReference = imageReference; 
        listeners = new HashSet<>();
        final AnnotationSet annoSet = this;
        folderListener = new ListenerROI_Folder() {
            @Override
            public void add(ROI_Folder folder, ROI roi) {
                for (ListenerAnnotationSet listener : listeners) {
                    listener.add(annoSet, folder, roi);
                }
            }

            @Override
            public void remove(ROI_Folder folder, ROI roi) {
                for (ListenerAnnotationSet listener : listeners) {
                    listener.remove(annoSet, folder, roi);
                }
            }

            @Override
            public void updated(ROI_Folder folder) {
                for (ListenerAnnotationSet listener : listeners) {
                    listener.updated(annoSet, folder);
                }
            }

            @Override
            public void updated(ROI_Folder folder, ROI originalROI, ROI newROI) {
                for (ListenerAnnotationSet listener : listeners) {
                    listener.updated(annoSet, folder, originalROI, newROI);
                }
            }
        };
    }

    public String toXML() {
        return AnnotationSet_XML_Writer.getXMLString(this);
    }

    public static AnnotationSet parse(String xml, ImageReference imageReference) throws ParserConfigurationException, SAXException, IOException {
        return AnnotationSet_XML_Parser.parse(xml, imageReference);
    }

    public void addListener(ListenerAnnotationSet listenerAnnotationSet) {
        listeners.add(listenerAnnotationSet);
    }

    public void removeListener(ListenerAnnotationSet listenerAnnotationSet) {
        listeners.remove(listenerAnnotationSet);
    }

    public ArrayList<ROI_Folder> getROI_Folders() {
        if (children == null) {
            return new ArrayList<>();
        }
        ArrayList<ROI_Folder> roiFolder = new ArrayList<>();
        for (Object r : children) {
            if (r instanceof ROI_Folder) {
                roiFolder.add((ROI_Folder) r);
            }
        }

        return roiFolder;
    }

    synchronized public void add(MutableTreeTableNode child, boolean direct) {
        // make sure child is of annotation and that its name is unique
        if (child instanceof ROI_Folder) {
            insert((ROI_Folder) child, getChildCount(), direct);
        }
    }

    @Override
    synchronized public void add(MutableTreeTableNode child) {
        add(child, true);
    }

    synchronized public void remove(MutableTreeTableNode child, boolean direct) {
        if (child instanceof ROI_Folder) {
            ROI_Folder folder = (ROI_Folder) child;
            if (direct) {
                folder.removeListener(folderListener);
                super.remove(child);
            } else {
                for (ListenerAnnotationSet listener : listeners) {
                    listener.remove(this, folder);
                }
            }
        }
    }

    @Override
    synchronized public void remove(MutableTreeTableNode child) {
        remove(child, true);
    }
   
    @Override
    synchronized public void insert(MutableTreeTableNode child, int index) {
        insert(child, index, true); //To change body of generated methods, choose Tools | Templates.
    }

    synchronized public void insert(MutableTreeTableNode child, int index, boolean direct) {
        // make sure child is of annotation and that its name is unique
        if (child instanceof ROI_Folder) {
            final AnnotationSet annoSet = this;
            ROI_Folder folder = (ROI_Folder) child;
            if (direct) {
                super.insert(folder, index);
                folder.addListener(folderListener);
            } else {
                for (ListenerAnnotationSet listener : listeners) {
                    listener.add(annoSet, folder);
                }
            }
        }
    }
    
}
