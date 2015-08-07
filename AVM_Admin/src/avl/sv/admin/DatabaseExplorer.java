package avl.sv.admin;

import avl.sv.shared.Permissions;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.Value.Format;
import oracle.kv.ValueVersion;

public class DatabaseExplorer extends javax.swing.JPanel {

    KVStore kvstore;
    /**
     * Creates new form DatabaseExplorerOld
     */
    public DatabaseExplorer() {
        this.kvstore = kvstore;
        initComponents();
    }
    
    public void setkvStore(KVStore kvstore){
        this.kvstore = kvstore;
        populateTrees();
    }
    
    ArrayList<String> getMajorFromJTree() {
        ArrayList<String> major = new ArrayList<String>();
        TreePath selectionPath = jTreeMajorKeys.getSelectionPath();
        if (selectionPath == null) {
            return major;
        }
        Object[] path = selectionPath.getPath();
        for (int i = 1; i < path.length; i++) {
            major.add(((DefaultMutableTreeNode) path[i]).toString());
        }
        return major;
    }
    ArrayList<ArrayList<String>> getMajorsFromJTree() {       
        ArrayList<ArrayList<String>> majors = new ArrayList<ArrayList<String>>();
        TreePath[] selectionPaths = jTreeMajorKeys.getSelectionPaths();
        if (selectionPaths == null) {
            return majors;
        }
        for (TreePath path:selectionPaths){
            Object[] objs = path.getPath();
            ArrayList<String> major = new ArrayList<String>();
            for (int i = 1; i < objs.length; i++) {
                major.add(((DefaultMutableTreeNode) objs[i]).toString());
            }
            majors.add(major);
        }
        return majors;
    }
    
    ArrayList<String> getMinorFromJTree() {
        ArrayList<String> minor = new ArrayList<String>();
        TreePath selectionPath = jTreeMinorKeys.getSelectionPath();
        if (selectionPath == null) {
            return minor;
        }
        Object[] path = selectionPath.getPath();
        for (int i = 1; i < path.length; i++) {
            minor.add(((DefaultMutableTreeNode) path[i]).toString());
        }
        return minor;
    }
    ArrayList<ArrayList<String>> getMinorsFromJTree() {       
        ArrayList<ArrayList<String>> minors = new ArrayList<ArrayList<String>>();
        TreePath[] selectionPaths = jTreeMinorKeys.getSelectionPaths();
        if (selectionPaths == null) {
            return minors;
        }
        for (TreePath path:selectionPaths){
            Object[] objs = path.getPath();
            ArrayList<String> minor = new ArrayList<String>();
            for (int i = 1; i < objs.length; i++) {
                minor.add(((DefaultMutableTreeNode) objs[i]).toString());
            }
            minors.add(minor);
        }
        return minors;
    }
    
    public void populateTrees() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                if (kvstore == null){
                    jTreeMajorKeys.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("")));
                    jTreeMinorKeys.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("")));
                } else {
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode("major");

                    root.add(new DefaultMutableTreeNode("user"));
                    root.add(new DefaultMutableTreeNode("study"));
                    root.add(new DefaultMutableTreeNode("image"));
                    root.add(new DefaultMutableTreeNode("solution"));
                    root.add(new DefaultMutableTreeNode("FeatureGeneratorTasks"));

                    jTreeMajorKeys.setModel(new DefaultTreeModel(root));
                    jTreeMinorKeys.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("")));
                }
                                } catch (Exception ex){
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void insertIntoTree(DefaultMutableTreeNode top, int depth, List<String> keys){
        if (depth >= keys.size()){
            return;
        }
        String s = keys.get(depth);
        if (top.getChildCount()>0){
            for (DefaultMutableTreeNode node = (DefaultMutableTreeNode) top.getFirstChild(); node != null ; node = node.getNextSibling()){
                if (((String)node.getUserObject()).equals(s)){
                    insertIntoTree(node, depth+1, keys);
                    return;
                }
            }
        }
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(keys.get(depth));
        top.add(newNode);
        insertIntoTree(top, depth, keys);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenuMajor = new javax.swing.JPopupMenu();
        jPopupMenuMinor = new javax.swing.JPopupMenu();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeMajorKeys = new javax.swing.JTree();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTreeMinorKeys = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaAsString = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextAreaRaw = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        jPopupMenuMajor.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                jPopupMenuMajorPopupMenuWillBecomeVisible(evt);
            }
        });

        jPopupMenuMinor.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                jPopupMenuMinorPopupMenuWillBecomeVisible(evt);
            }
        });

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTreeMajorKeys.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTreeMajorKeys.setComponentPopupMenu(jPopupMenuMajor);
        jTreeMajorKeys.setRootVisible(false);
        jTreeMajorKeys.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeMajorKeysValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jTreeMajorKeys);

        treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTreeMinorKeys.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTreeMinorKeys.setComponentPopupMenu(jPopupMenuMinor);
        jTreeMinorKeys.setRootVisible(false);
        jTreeMinorKeys.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeMinorKeysValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jTreeMinorKeys);

        jTextAreaAsString.setColumns(20);
        jTextAreaAsString.setLineWrap(true);
        jTextAreaAsString.setRows(5);
        jScrollPane3.setViewportView(jTextAreaAsString);

        jTextAreaRaw.setColumns(20);
        jTextAreaRaw.setLineWrap(true);
        jTextAreaRaw.setRows(5);
        jScrollPane4.setViewportView(jTextAreaRaw);

        jLabel4.setText("Raw");

        jLabel1.setText("Major Keys");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel2.setText("Minor Keys");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel3.setText("As String");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addMajorNodes(){
        ArrayList<String> major = getMajorFromJTree();
        if (major.isEmpty()){
            return;
        }
        Key key = Key.createKey(major);
        Iterator<Key> iter = kvstore.storeKeysIterator(Direction.UNORDERED, 0, key, null, Depth.CHILDREN_ONLY);
        while (iter.hasNext()) {
            insertIntoTree((DefaultMutableTreeNode)jTreeMajorKeys.getModel().getRoot(), 0, iter.next().getMajorPath());
        }
        
        jTreeMinorKeys.updateUI();
    }
    
    private void addMinorNodes(){
        ArrayList<String> major = getMajorFromJTree();
        if (major.isEmpty()){
            return;
        }
        Key key = Key.createKey(major);
        Iterator<Key> iter = kvstore.multiGetKeysIterator(Direction.FORWARD, 0, key, null, Depth.DESCENDANTS_ONLY);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("minor");
        while (iter.hasNext()) {
            insertIntoTree(root, 0, iter.next().getMinorPath());
        }
        TreeModel tree = new DefaultTreeModel(root);
        jTreeMinorKeys.setModel(tree);
        
        ValueVersion vv = kvstore.get(key);
        if (vv == null){
            jTextAreaRaw.setText("");
            jTextAreaAsString.setText("");
        } else {
            jTextAreaRaw.setText(vv.getValue().getFormat().toString());
            jTextAreaAsString.setText(vv.getValue().toString());
        }        
    }

    private void jTreeMajorKeysValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTreeMajorKeysValueChanged
        addMajorNodes();
        addMinorNodes();
        ArrayList<String> major = getMajorFromJTree();
        if (major.isEmpty()){
            return;
        }
        Key key = Key.createKey(major);
        ValueVersion vv = kvstore.get(key);
        if (vv == null){
            jTextAreaRaw.setText("");
            jTextAreaAsString.setText("");
            return;
        }
        Value v = vv.getValue();
        Format f = v.getFormat();
        jTextAreaRaw.setText(f.toString());
        jTextAreaAsString.setText(v.toString());
        
    }//GEN-LAST:event_jTreeMajorKeysValueChanged

    private void jTreeMinorKeysValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTreeMinorKeysValueChanged
        ArrayList<String> major = getMajorFromJTree();
        ArrayList<String> minor = getMinorFromJTree();
        if (minor.isEmpty()|| major.isEmpty()){
            return;
        }
        Key key = Key.createKey(major, minor);
        ValueVersion vv = kvstore.get(key);
        if (vv == null){
            jTextAreaRaw.setText("");
            jTextAreaAsString.setText("");
            return;
        }
        Value v = vv.getValue();
        jTextAreaRaw.setText(v.toString());
        jTextAreaAsString.setText(new String(v.getValue()));
    }//GEN-LAST:event_jTreeMinorKeysValueChanged

    private void jPopupMenuMajorPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jPopupMenuMajorPopupMenuWillBecomeVisible

        jPopupMenuMajor.removeAll();
        jPopupMenuMajor.add(new JMenuItem(new AbstractAction("Delete Key") {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ArrayList<String> major:getMajorsFromJTree()){
                    Key key = Key.createKey(major);
                    Iterator<Key> iter = kvstore.storeKeysIterator(Direction.UNORDERED, 0, key, null, Depth.PARENT_AND_DESCENDANTS);
                    while (iter.hasNext()){
                        key = iter.next();
                        kvstore.multiDelete(key, null, Depth.PARENT_AND_DESCENDANTS);
                    }        
                }
                populateTrees();
            }
        }));
        
        boolean validForPermissions = false;
        {
//            ArrayList<String> major = getMajorFromJTree();
//            switch (major.get(0)) {
//                case "image":
//                    if (major.size() == 2) {
//                        validForPermissions = true;
//                    }
//                    break;
//            }
        }
        if (!validForPermissions){
            return;
        }
        
        JMenu permissionSetMenu = new JMenu("Permission Set");
        ArrayList<String> major = new ArrayList<String>();
        major.add("user");
        Iterator<Key> iter = kvstore.storeKeysIterator(Direction.UNORDERED, 0, Key.createKey(major), null, Depth.CHILDREN_ONLY);
        while (iter.hasNext()) {
            Key key = iter.next();
            List<String> m = key.getMajorPath();
            final String userName = m.get(m.size()-1);
            JMenu userItem = new JMenu(userName);
            for (final Permissions p:Permissions.values()){
                JMenuItem permissionItem = new JMenuItem(new AbstractAction(p.name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ArrayList<String> minor = new ArrayList<String>();
                        minor.add("permissions");
                        kvstore.putIfAbsent(Key.createKey(getMajorFromJTree(),minor), Value.EMPTY_VALUE);
                        minor.add(userName);
                        kvstore.put(Key.createKey(getMajorFromJTree(),minor), Value.createValue(p.toString().getBytes()));
//                        byte[] bytes = ByteBuffer.allocate(4).putInt(p.getValue()).array();
//                        kvstore.put(Key.createKey(getMajorFromJTree(),minor), Value.createValue(bytes));
                        addMinorNodes();
                    }
                });
                userItem.add(permissionItem);
            }          
            permissionSetMenu.add(userItem);
        }
           
        jPopupMenuMajor.add(permissionSetMenu);               

    }//GEN-LAST:event_jPopupMenuMajorPopupMenuWillBecomeVisible

    private void jPopupMenuMinorPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jPopupMenuMinorPopupMenuWillBecomeVisible
        jPopupMenuMinor.removeAll();
        jPopupMenuMinor.add(new JMenuItem(new AbstractAction("Delete Key") {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> major = getMajorFromJTree();
                for (ArrayList<String> minor:getMinorsFromJTree()){
                    Key key = Key.createKey(major,minor);
                    Iterator<Key> iter = kvstore.multiGetKeysIterator(Direction.FORWARD, 0, key, null, Depth.PARENT_AND_DESCENDANTS);
                    while (iter.hasNext()){
                        key = iter.next();
                        kvstore.multiDelete(key, null, Depth.PARENT_AND_DESCENDANTS);
                    }        
                }
                addMinorNodes();
            }
        }));
        
        boolean validForPermissions = false;
        {
            ArrayList<String> major = getMajorFromJTree();
            ArrayList<String> minor = getMinorFromJTree();
            if ( (major.size() == 2) && 
                  major.get(0).equals("study") && 
                  minor.get(0).equals("annotations") && 
                 (minor.size() > 1)){
                validForPermissions = true;
            }
        }
        if (!validForPermissions){
            return;
        }
        
        JMenu permissionSetMenu = new JMenu("Permission Set");
        ArrayList<String> major = new ArrayList<String>();
        major.add("user");
        Iterator<Key> iter = kvstore.storeKeysIterator(Direction.UNORDERED, 0, Key.createKey(major), null, Depth.CHILDREN_ONLY);
        while (iter.hasNext()) {
            Key key = iter.next();
            List<String> m = key.getMajorPath();
            final String userName = m.get(m.size()-1);
            JMenu userItem = new JMenu(userName);
            for (final Permissions p:Permissions.values()){
                JMenuItem permissionItem = new JMenuItem(new AbstractAction(p.name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ArrayList<String> minor = getMinorFromJTree();
                        minor.set(0,"permissions");
                        kvstore.putIfAbsent(Key.createKey(getMajorFromJTree(),minor), Value.EMPTY_VALUE);
                        minor.add(userName);
                        kvstore.put(Key.createKey(getMajorFromJTree(),minor), Value.createValue(p.toString().getBytes()));
                        addMinorNodes();
                    }
                });
                userItem.add(permissionItem);
            }          
            permissionSetMenu.add(userItem);
        }
        jPopupMenuMinor.add(permissionSetMenu);               
    }//GEN-LAST:event_jPopupMenuMinorPopupMenuWillBecomeVisible

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPopupMenu jPopupMenuMajor;
    private javax.swing.JPopupMenu jPopupMenuMinor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea jTextAreaAsString;
    private javax.swing.JTextArea jTextAreaRaw;
    private javax.swing.JTree jTreeMajorKeys;
    private javax.swing.JTree jTreeMinorKeys;
    // End of variables declaration//GEN-END:variables
}
