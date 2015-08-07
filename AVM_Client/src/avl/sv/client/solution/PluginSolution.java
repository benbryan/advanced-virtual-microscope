package avl.sv.client.solution;

import avl.sv.shared.solution.SolutionsSource;
import avl.sv.client.AVM_Plugin;
import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.client.PermissionsManager;
import avl.sv.client.SearchableSelector;
import avl.sv.shared.AVM_Source;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.Permissions;
import avl.sv.shared.solution.SolutionSource;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

public class PluginSolution implements AVM_Plugin {
    final private SolutionsSource solutionsSource;
    final private String username;
    
    public PluginSolution(String username, SolutionsSource solutionsSource) {
        this.username = username;
        this.solutionsSource = solutionsSource;
    }
    
    @Override
    public JMenu getMenu(){
        JMenu jMenuSolution = new JMenu("Solution");
        jMenuSolution.add(new JMenuItem(new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SolutionSource solutionSource = solutionsSource.showNewSolutionDialog();
                if (solutionSource == null){
                    return;
                }
                SolutionManager solutionManager = new SolutionManager(username, solutionSource);
                solutionManager.setVisible(true);
                solutionManager.toFront();
                solutionManager.showSetupPrompts();

                AdvancedVirtualMicroscope.addWindow(solutionManager, "Solution: " + solutionSource.getName());
            }
        }));
        jMenuSolution.add(new JMenuItem(new AbstractAction("Open") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SearchableSelector selector = new SearchableSelector("Select Solution", "Open") {
                    public void openSolution(ArrayList<AVM_Source> solutionSources) {
                        for (DefaultMutableTreeNode node:solutionSources){
                            if (node instanceof SolutionSource){
                                SolutionSource solutionSource = (SolutionSource) node;
                                SolutionManager solutionManager = new SolutionManager(username, solutionSource);
                                solutionManager.setSize(solutionManager.getPreferredSize());
                                solutionManager.setVisible(true);
                                solutionManager.toFront();
                                AdvancedVirtualMicroscope.addWindow(solutionManager, "Solution: "+ solutionSource.getName());
                                dispose();
                            }
                        }
                    }

                    @Override
                    public void doubleClicked(ArrayList<AVM_Source> selected) {
                        openSolution(selected);
                    }

                    @Override
                    public void buttonPressed(ArrayList<AVM_Source> selected) {
                        openSolution(selected);
                    }

                    @Override
                    public ArrayList<AVM_Source> getSelectables() {
                        ArrayList<AVM_Source> solutionSources = new ArrayList<>();
                        solutionSources.addAll(solutionsSource.getSolutionSources());
                        return solutionSources;
                    }

                };
                selector.setVisible(true);
            }
        }));
        jMenuSolution.add(new JMenuItem(new AbstractAction("Permissions") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SearchableSelector solutionSelector = new SearchableSelector("Select Solution",  "Open") {
                    public void openSolution( ArrayList<AVM_Source> solutionSources) {
                        if (solutionSources != null && solutionSources.size() == 0) {
                            return;
                        }
                        DefaultMutableTreeNode node = solutionSources.get(0);
                        if (node instanceof SolutionSource) {
                            SolutionSource solutionSource = (SolutionSource) node;
                            new PermissionsManager(this) {
                                @Override
                                public String getUsers() {
                                    try {
                                        return solutionSource.getUsers();
                                    } catch (PermissionDenied ex) {
                                        Logger.getLogger(PluginSolution.class.getName()).log(Level.SEVERE, null, ex);
                                        return MessageStrings.PERMISSION_DENIED;
                                    }
                                }

                                @Override
                                public String setPermissions(String username, Permissions permission) {
                                    try {
                                        return solutionSource.setPermissions(username, permission);
                                    } catch (PermissionDenied ex) {
                                        Logger.getLogger(PluginSolution.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    return MessageStrings.PERMISSION_DENIED;
                                }

                                @Override
                                public Permissions getPermissions(String username) {
                                    try {
                                        return solutionSource.getPermissions(username);
                                    } catch (PermissionDenied ex) {
                                        JOptionPane.showMessageDialog(rootPane, MessageStrings.PERMISSION_DENIED);
                                        return null;
                                    }
                                }
                            }.setVisible(true);
                        }
                    }

                    @Override
                    public void doubleClicked(ArrayList<AVM_Source> selected) {
                        setVisible(false);
                        openSolution(selected);
                    }

                    @Override
                    public void buttonPressed(ArrayList<AVM_Source> selected) {
                        setVisible(false);
                        openSolution(selected);
                    }

                    @Override
                    public ArrayList<AVM_Source> getSelectables() {
                        ArrayList<AVM_Source> solutionSources = new ArrayList<>();
                        solutionSources.addAll(solutionsSource.getSolutionSources());
                        return solutionSources;
                    }
                };
                solutionSelector.setTitle("Select a solution to manage permissions");
                solutionSelector.setVisible(true);
            }
        }));
        return jMenuSolution;
    }
}
