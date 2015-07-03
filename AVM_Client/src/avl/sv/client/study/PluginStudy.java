package avl.sv.client.study;

import avl.sv.client.AVM_Plugin;
import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.client.PermissionsManager;
import avl.sv.client.SearchableSelector;
import avl.sv.shared.AVM_Source;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.image.ImagesSource;
import avl.sv.shared.Permissions;
import avl.sv.shared.study.StudiesSource;
import avl.sv.shared.study.StudySource;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

public class PluginStudy implements AVM_Plugin{

    private final StudiesSource studiesSource;
    private final ImagesSource imagesSource;
    private final String username;

    public PluginStudy(String username, StudiesSource studiesSource) {
        this.studiesSource = studiesSource;
        this.imagesSource = studiesSource.getImagesSource();
        this.username = username;

    }
    
    @Override
    public JMenu getMenu() {
        JMenu jMenuStudy = new JMenu("Study");
        jMenuStudy.add(new JMenuItem(new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent e) {
                StudySource studySource = studiesSource.showNewStudyDialog();
                StudyManager studyManager = new StudyManager(username, studySource);
                studyManager.setVisible(true);
                studyManager.toFront();
                studyManager.showAddImagesPrompt();
                AdvancedVirtualMicroscope.addWindow(studyManager, "Solution: " + studyManager.getName());
            }
        }));
        jMenuStudy.add(new JMenuItem(new AbstractAction("Open") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SearchableSelector ss = new SearchableSelector("Select Study", "Open") {
                    @Override
                    public void doubleClicked(ArrayList<AVM_Source> selected) {
                        if (selected != null && selected.size()>0){
                            DefaultMutableTreeNode s = selected.get(0);
                            if (s instanceof StudySource){
                                openStudy((StudySource) s);
                            }
                        }
                    }
                    @Override
                    public void buttonPressed(ArrayList<AVM_Source> selected) {
                        if (selected != null && selected.size()>0){
                            DefaultMutableTreeNode s = selected.get(0);
                            if (s instanceof StudySource){
                                openStudy((StudySource) s);
                            }
                        }
                    }
                    @Override
                    public ArrayList<AVM_Source> getSelectables() {
                        ArrayList<AVM_Source> nodes = new ArrayList<>();
                        ArrayList<StudySource> sources = studiesSource.getStudySources();
                        for (StudySource source:sources){
                            if ((source.getName() == null) || source.getName().isEmpty()){
                                continue;
                            }
                            nodes.add(source);
                        }
                        return nodes;
                    }
//                    @Override
//                    public ImageSource getImageSource(ImageReference imageReference) {
//                        return studiesSource.getImagesSource().getImageSource(imageReference);
//                    }
                    private void openStudy(StudySource studySource) {
                        StudyManager sm = new StudyManager(username, studySource);
                        sm.setVisible(true);
                        AdvancedVirtualMicroscope.addWindow(sm, "Study: "+ studySource.getName());
                        dispose();
                    }

//                    @Override
//                    public ImageManager getImageManager(ImageReference imageReference) {
//                        return imagesSource.getImageManager(imageReference);
//                    }
                };
                ss.setVisible(true);
            }
        }));
        jMenuStudy.add(new JMenuItem(new AbstractAction("Permissions") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SearchableSelector ss = new SearchableSelector("Select Study", "Open") {
                    @Override
                    public void doubleClicked(ArrayList<AVM_Source> selected) {
                        if (selected != null && selected.size()>0){
                            DefaultMutableTreeNode s = selected.get(0);
                            if (s instanceof StudySource){
                                openStudy((StudySource) s);
                            }
                        }
                    }
                    @Override
                    public void buttonPressed(ArrayList<AVM_Source> selected) {
                        if (selected != null && selected.size()>0){
                            DefaultMutableTreeNode s = selected.get(0);
                            if (s instanceof StudySource){
                                openStudy((StudySource) s);
                            }
                        }
                    }

                    @Override
                    public ArrayList<AVM_Source> getSelectables() {
                        ArrayList<AVM_Source> adminList = new ArrayList<>();
                        for(StudySource ss:studiesSource.getStudySources()){
                            if ((ss.getName() == null) || ss.getName().isEmpty()){
                                continue;
                            }
                            if (ss.getPermissions().isAdmin()){
                                adminList.add(ss);
                            }
                        }          
                        return adminList; 
                    }

                    private void openStudy(StudySource studySource) {
                        new PermissionsManager(null) {
                            @Override
                            public String getUsers() {
                                return studySource.getUsers();
                            }

                            @Override
                            public String setPermissions(String username, Permissions permission) {
                                try {
                                    return studySource.setPermission(username, permission);
                                } catch (PermissionDenied ex) {
                                    JOptionPane.showMessageDialog(rootPane, MessageStrings.PERMISSION_DENIED);
                                    return MessageStrings.PERMISSION_DENIED;
                                }
                            }

                            @Override
                            public Permissions getPermissions(String username) {
                                try {
                                    return studySource.getPermissions(username);
                                } catch (PermissionDenied ex) {
                                    JOptionPane.showMessageDialog(rootPane, MessageStrings.PERMISSION_DENIED);
                                    return null;
                                }
                            }
                            
                        }.setVisible(true);
                        dispose();
                    }

//                    @Override
//                    public ImageSource getImageSource(ImageReference imageReference) {
//                        return imagesSource.getImageSource(imageReference);
//                    }
//
//                    @Override
//                    public ImageManager getImageManager(ImageReference imageReference) {
//                        return imagesSource.getImageManager(imageReference);
//                    }
                    
                };
                ss.setTitle("Select a study to manage permissions");
                ss.setVisible(true);
            }
        }));
        return jMenuStudy;
    }
    

}
