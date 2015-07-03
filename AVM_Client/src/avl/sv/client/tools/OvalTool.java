package avl.sv.client.tools;

import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.client.image.ImageViewer;
import avl.sv.shared.study.ROIOval;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class OvalTool extends AbstractImageViewerTool {
    private static OvalToolPrompt prompt;
    private static JPopupMenu popupMenu;
    @Override
    public boolean canModify() {
        return true;
    }
    private ROIOval workingROI = null;

    final ImageViewer imageViewer;

    public static String toolTipText = "Oval Tool";
    public static ImageIcon imageIcon = new ImageIcon(OvalTool.class.getResource("/avl/sv/client/icon/oval.png"));
    public OvalTool() {
        setToolTipText(toolTipText);
        setIcon(imageIcon);
        imageViewer = null;
        Window parentWindow = SwingUtilities.windowForComponent(this);
        prompt = new OvalToolPrompt(parentWindow, Dialog.ModalityType.APPLICATION_MODAL);
        initPopupMenu();
        setComponentPopupMenu(popupMenu); 
    }  
    
    private void initPopupMenu(){        
        final JMenuItem jMenuItemSetup = new JMenuItem("Setup");
        final OvalTool thisButton = this; 
        jMenuItemSetup.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ActionListener listener:thisButton.getActionListeners()){
                    listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "popupmenu shown"));
                }
                prompt.setVisible(true);
            }
        });
        final JPopupMenu menu = new JPopupMenu();
        menu.add(jMenuItemSetup);
        popupMenu = menu;
    }
    
    public OvalTool(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;  
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        if (workingROI != null){
            Point pImg = imageViewer.displaySpaceToImageSpace(e.getPoint());
            int ix = (int) pImg.getX();
            int iy = (int) pImg.getY();
            workingROI.addPoint(ix, iy);
            imageViewer.repaint();     
        } else {
            int constraints[] = prompt.getConstraints();
            if (constraints != null) {
                workingROI = ROIOval.getDefault();
                workingROI.setFixedSize(constraints);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            popupMenu.show(imageViewer, e.getX(), e.getY());
            return;
        }
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        if (workingROI == null) {
            Point pImg = imageViewer.displaySpaceToImageSpace(e.getPoint());
            int ix = (int) pImg.getX();
            int iy = (int) pImg.getY();
            workingROI = ROIOval.getDefault();
            int[] constraints = prompt.getConstraints();
            if (constraints != null){
                workingROI.setFixedSize(constraints);
            }
            workingROI.start(ix, iy);
        } else {
            imageViewer.getROI_TreeTable().addROI(workingROI);
            workingROI = null;
            AdvancedVirtualMicroscope.getToolPanel().setDefaultTool();
        }
        imageViewer.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
    
    @Override
    public void paintOnImageViewer(Graphics2D g) {
        if (workingROI != null){
            imageViewer.concatenateImageToDisplayTransform(g);
            workingROI.paintROI(g, Color.GREEN);
        }
    }

    @Override
    public AbstractImageViewerTool getNewInstance(ImageViewer imageViewer) {
        return new OvalTool(imageViewer);
    }
}
