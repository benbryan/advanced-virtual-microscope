package avl.sv.client;

import avl.sv.shared.AVM_Source;
import avl.sv.shared.PermissionDenied;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

abstract public class SearchableSelector extends JFrame {

    private class AVM_SourceDummy extends AVM_Source{

        private final String name;

        public AVM_SourceDummy(String name) {
            this.name = name;
        }       
        
        @Override
        public String getDescription() {
            return name;
        }

        @Override
        public String setDescription(String description) {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
        
    }
    
    private AVM_SourceDummy originalNode;
    
    public SearchableSelector(final String title, final String buttonText) {
        initComponents();
        this.jButtonSelect.setText(buttonText);
        setTitle(title);
        
        // Add a loading note
        DefaultMutableTreeNode loadingNode = new DefaultMutableTreeNode();
        loadingNode.add(new DefaultMutableTreeNode("Loading data from server..."));
        jTree.setModel(new DefaultTreeModel(loadingNode));
        
        getRootPane().setDefaultButton(jButtonSelect);
        
        update();
    }
    
    abstract public void doubleClicked(ArrayList<AVM_Source> selected);
    abstract public void buttonPressed(ArrayList<AVM_Source> selected);
    abstract public ArrayList<AVM_Source> getSelectables();
    
    final public void update(){
        EventQueue.invokeLater(() -> {
            // populate image list
            ArrayList<AVM_Source> selectables = getSelectables();
            if (selectables == null) {
                DefaultMutableTreeNode loadingNode1 = new DefaultMutableTreeNode();
//                    loadingNode.add(new AVM_Source("Failed to get image sets from server"));
//                    AdvancedVirtualMicroscope.setStatusText("Failed to get image sets from server", 2000);
                jTree.setModel(new DefaultTreeModel(loadingNode1));
                return ;
            }
            originalNode = new AVM_SourceDummy("Top");
            for (AVM_Source node:selectables){
                originalNode.add(node);
            }            
            sortNode(originalNode);
            jTree.setModel(new DefaultTreeModel(originalNode));
        });
    }
    
    private void sortNode(DefaultMutableTreeNode top){
        ArrayList<AVM_Source> subs = new ArrayList<>();
        Enumeration e = top.children();
        while (e.hasMoreElements()){
            AVM_Source node = (AVM_Source)e.nextElement();
            subs.add(node);
        }
        top.removeAllChildren();
        Collections.sort(subs,comp);
        for (AVM_Source node:subs){
            top.add(node);
            sortNode(node);
        }
    }
    
    Comparator<AVM_Source> comp = (AVM_Source o1, AVM_Source o2) -> o1.toString().compareTo(o2.toString());
    
    public void removeNode(AVM_Source node){
        DefaultTreeModel model = (DefaultTreeModel) jTree.getModel();
        model.removeNodeFromParent(node);
        jTree.invalidate();
    }
        
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextFieldSearch = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaDescription = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jButtonSelect = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Image Selector");
        setLocationByPlatform(true);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("JTree");
        jTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree.setRootVisible(false);
        jTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTreeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTree);

        jLabel1.setText("Images");

        jTextFieldSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldSearchKeyTyped(evt);
            }
        });

        jLabel3.setText("Search");

        jTextAreaDescription.setColumns(20);
        jTextAreaDescription.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextAreaDescriptionKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(jTextAreaDescription);

        jLabel2.setText("Description");

        jButtonSelect.setText("Select");
        jButtonSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectActionPerformed(evt);
            }
        });

        jButton1.setText("Refresh");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTextFieldSearch, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSelect)
                    .addComponent(jButton1)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
 
    private AVM_Source filterNode(AVM_Source top, String filterString){
        AVM_Source filteredNode = (AVM_Source) top.clone();
        ArrayList<AVM_Source> subs = new ArrayList<>();
        Enumeration e = top.children();
        while (e.hasMoreElements()){
            AVM_Source node = (AVM_Source)e.nextElement();
            if (node.getChildCount() == 0){
                if (node.toString().toLowerCase().startsWith(filterString)) {
                    subs.add(node);
                }                
            } else {
                subs.add(node);
            }
        }
        Collections.sort(subs,comp);
        for (AVM_Source node:subs){
            filteredNode.add(filterNode(node, filterString));
        }   
        return filteredNode;
    }
    
    private void jTextFieldSearchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldSearchKeyTyped
        String filterString = (jTextFieldSearch.getText() + evt.getKeyChar()).trim().toLowerCase();
        if (filterString.isEmpty()) {
            jTree.setModel(new DefaultTreeModel(originalNode));
            return;
        }
 
        AVM_Source filteredNode = filterNode(originalNode, filterString);
        jTree.setModel(new DefaultTreeModel(filteredNode));
        Enumeration nodes = filteredNode.children();
        while (nodes.hasMoreElements()){
            jTree.expandPath(new TreePath(new Object[]{filteredNode,nodes.nextElement()}));
        }
        
        if (jTree.getModel().getChildCount(jTree.getModel().getRoot()) > 0) {
            jTree.setSelectionRow(0);
        }
    }//GEN-LAST:event_jTextFieldSearchKeyTyped

    private void jButtonSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectActionPerformed
        ArrayList<AVM_Source> selected = getSelected();
        if (!getSelected().isEmpty()){
            buttonPressed(selected);
        }
    }//GEN-LAST:event_jButtonSelectActionPerformed

    private ArrayList<AVM_Source> getSelected() {
        ArrayList<AVM_Source> selected = new ArrayList<>();
        TreePath[] paths = jTree.getSelectionPaths();
        if (paths == null) {
            return selected;
        }

        for (TreePath path : paths) {
            Object node = path.getLastPathComponent();
            selected.add((AVM_Source) node);
        }
        return selected;
    }
    
    private void jTreeImagesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTreeSlidesMouseClicked

    }//GEN-LAST:event_jTreeSlidesMouseClicked

    private void jTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTreeMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1) {
            ArrayList<AVM_Source> selected = getSelected();
            switch (evt.getClickCount()) {
                case 1:
                    if (selected.size() == 1){
                        AVM_Source s = selected.get(0);
                        String txt = s.getDescription();
                        jTextAreaDescription.setText(txt);
                    } else {
                        jTextAreaDescription.setText("");
                    }
                    break;
                case 2:
                    if (!getSelected().isEmpty()) {
                        doubleClicked(selected);
                    }
                    break;
            }
        }       
    }//GEN-LAST:event_jTreeMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        update();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextAreaDescriptionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextAreaDescriptionKeyReleased
        ArrayList<AVM_Source> selected = getSelected();
        if (selected.size() == 1){
            AVM_Source s = selected.get(0);
            String txt = jTextAreaDescription.getText();
            try {
                txt = s.setDescription(txt);
            } catch (PermissionDenied ex) {
                JOptionPane.showMessageDialog(rootPane, "Permission denied");
            }
            jTextAreaDescription.setText(txt);
        }
    }//GEN-LAST:event_jTextAreaDescriptionKeyReleased
          
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonSelect;
    private final javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextAreaDescription;
    private javax.swing.JTextField jTextFieldSearch;
    private final javax.swing.JTree jTree = new javax.swing.JTree();
    // End of variables declaration//GEN-END:variables
}
