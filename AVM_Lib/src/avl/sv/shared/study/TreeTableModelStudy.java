package avl.sv.shared.study;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;

public class TreeTableModelStudy extends DefaultTreeTableModel {

    private final AnnotationSet annoSet;
    private String columnNames[] = {"Name", "Visible"};
    private Class columnClass[] = {String.class, Boolean.class};  
    
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public TreeTableModelStudy(TreeTableNode root) {
        super(root);
        if (root instanceof AnnotationSet) {
            this.annoSet = (AnnotationSet) root;
        } else {
            throw new IllegalArgumentException("root must extend AnnotationSet");
        }
    }

    @Override
    public int getChildCount(Object parent) {
        // This prevents some error I could not find out how to manage otherwise
        try {
            return super.getChildCount(parent); //To change body of generated methods, choose Tools | Templates.
        } catch (Exception ex) {
            return 0;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnClass[column];
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (node instanceof ROI_Folder) {
            ROI_Folder annotation = (ROI_Folder) node;
            switch (column) {
                case 0:
                    return annotation.getName();
                case 1:
                    return annotation.visible;
                case 2:
                    return null;
            }
        } else if (node instanceof ROI) {
            ROI roi = (ROI) node;
            switch (column) {
                case 0:
                    return roi.getName();
                case 1:
                    return null;
                case 2:
                    return roi.negativeROA;
            }
        } else if (node instanceof AnnotationSet) {
            AnnotationSet annoSet = (AnnotationSet) node;
            switch (column) {
                case 0:
                    return annoSet.imageReference.imageName;
                case 1:
                    return null;
                case 2:
                    return null;
            }
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, Object node, int column) {
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(node, "", value, value));
        if (node instanceof ROI_Folder) {
            ROI_Folder folder = (ROI_Folder) node;
            switch (column) {
                case 0:
                    String newName = (String) value;
                    folder.setName(newName, false);
                    return;
                case 1:
                    folder.visible = (boolean) value;
                case 2:
            }
        } else if (node instanceof ROI) {
            ROI roi = (ROI) node;
            switch (column) {
                case 0:
                    roi.setNameDirect((String) value, false);
                    return;
                case 1:
                    return;
                case 2:
                    roi.negativeROA = (boolean) value;
            }
        }
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        if (node instanceof AnnotationSet) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isLeaf(Object node) {
        if (node instanceof ROI) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object getChild(Object parent, int index) {
        return super.getChild(parent, index); //To change body of generated methods, choose Tools | Templates.
    }

    public AnnotationSet getAnnotationSet() {
        return annoSet;
    }

}
