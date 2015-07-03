package avl.sv.client;

import avl.sv.client.tools.FreehandTool;
import avl.sv.client.tools.OvalTool;
import avl.sv.client.tools.PanTool;
import avl.sv.client.tools.PointTool;
import avl.sv.client.tools.PolygonTool;
import avl.sv.client.tools.RectangleTool;
import avl.sv.client.tools.SelectTool;
import avl.sv.client.tools.AbstractImageViewerTool;
import avl.sv.client.tools.IntelligentScissorsTool;
import avl.sv.client.tools.PointDeleteTool;
import avl.sv.client.tools.PointPushTool;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class ToolPanel extends javax.swing.JPanel {
    private  AbstractImageViewerTool defaultTool;
    final int toolDim = 40;
    final int toolPad = 2;
    private int x = toolPad;    
            
    public ToolPanel() {
        initComponents();
        setSize(0,0);
        defaultTool = new PanTool();
        addTool(defaultTool);
        addSpace();
        addTool( new SelectTool() );
        addTool( new PointTool() );
        addTool( new PointPushTool());
        addTool( new PointDeleteTool());
        addSpace();
        addTool( new FreehandTool() );
        addTool( new RectangleTool() );
        addTool( new OvalTool() );
        addTool( new PolygonTool() );
        addTool( new IntelligentScissorsTool() );

        buttonPress(defaultTool);
    }
        
    public void setEditingToolsState(boolean state){
        for (Component c:getComponents()){
            if (c instanceof AbstractImageViewerTool){
                AbstractImageViewerTool tool = (AbstractImageViewerTool) c;
                if (tool.canModify()){
                    tool.setEnabled(state);
                }
            }
        }
        getSelectedTool();
    }
    
    private void addSpace(){
        JPanel space = new JPanel();
        add(space);
        space.setSize(toolDim/10, toolDim);
        space.setLocation(x, 0);
        space.setVisible(true);
        space.setBorder(new BevelBorder(BevelBorder.RAISED));
        x += space.getSize().width + toolPad;
        setPreferredSize(new Dimension(x, toolDim));
    }
    
    final public void addTool(final AbstractImageViewerTool tool){
        add(tool);
        tool.setSize(toolDim, toolDim);
        tool.setLocation(x, 0);
        tool.setVisible(true);
        x += tool.getSize().width + toolPad;
        tool.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonPress(tool);
            }
        });
        setPreferredSize(new Dimension(x, toolDim));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 305, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 53, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    public AbstractImageViewerTool getSelectedTool(){
        for (Component c:getComponents()){
            if (c instanceof AbstractImageViewerTool){
                AbstractImageViewerTool tool = (AbstractImageViewerTool) c;
                if (tool.isSelected()){
                    return tool;
                }
            }
        }
        buttonPress(defaultTool);
        return defaultTool;
    }
            
    public void setDefaultTool(){
        buttonPress(defaultTool);
    }
    
    private void buttonPress(AbstractImageViewerTool src){
        for (Component c:getComponents()){
            if (c instanceof AbstractImageViewerTool){
                AbstractImageViewerTool tool = (AbstractImageViewerTool) c;
                tool.setSelected(false);
            }
        }
        src.setSelected(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
