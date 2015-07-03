
package avl.sv.shared;

import javax.swing.tree.DefaultMutableTreeNode;

abstract public class AVM_Source extends DefaultMutableTreeNode {   
    abstract public String getDescription();
    abstract public String setDescription(String description) throws PermissionDenied;    
}
