package avl.sv.client.tools;

import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.client.image.ImageViewer;
import avl.sv.client.study.ROI_ManagerPanel;
import avl.sv.shared.study.ROIRectangle;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class RectangleTool extends AbstractImageViewerTool {
    private static RectangleToolPrompt rectanglePrompt;
    private ROIRectangle workingROI = null;
    private final ImageViewer imageViewer;
    private static JPopupMenu popupMenu;

    public static String toolTipText = "Rectangle Tool";
    public static ImageIcon imageIcon = new ImageIcon(RectangleTool.class.getResource("/avl/sv/client/icon/rectangle.png"));
    
    public RectangleTool() {
        imageViewer = null;
        setToolTipText(toolTipText);
        setIcon(imageIcon);
        
        Window parentWindow = SwingUtilities.windowForComponent(this);
        rectanglePrompt = new RectangleToolPrompt(parentWindow, Dialog.ModalityType.APPLICATION_MODAL);
        initPopupMenu();
        setComponentPopupMenu(popupMenu);        
    }    
    
    private void initPopupMenu(){
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem jMenuItemSetup = new JMenuItem("Setup");
        final RectangleTool thisButton = this; 
        jMenuItemSetup.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ActionListener listener:thisButton.getActionListeners()){
                    listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "popupmenu shown"));
                }
                rectanglePrompt.setVisible(true);
            }
        });
        menu.add(jMenuItemSetup);
        jMenuItemSetup.setVisible(true);
        popupMenu = menu;
    }
    
    public RectangleTool(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }

    @Override
    public boolean canModify() {
        return true;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            popupMenu.show(imageViewer, e.getX(), e.getY());
            return;
        }
        
        if (workingROI != null) {
            Point pImg = imageViewer.displaySpaceToImageSpace(e.getPoint());
            int ix = (int) pImg.getX();
            int iy = (int) pImg.getY();
            workingROI.addSecondPoint(ix, iy);
            imageViewer.repaint();
        } else {
            int constraints[] = rectanglePrompt.getConstraints();
            if (constraints != null) {
                workingROI = ROIRectangle.getDefault();
                workingROI.setFixedSize(constraints);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        if (e.getButton() != MouseEvent.BUTTON1){
            return;
        }
        if (workingROI == null){
            Point pImg = imageViewer.displaySpaceToImageSpace(e.getPoint());
            int ix = (int) pImg.getX();
            int iy = (int) pImg.getY();            
            int constraints[] = rectanglePrompt.getConstraints();
            if (constraints != null) {
                workingROI = ROIRectangle.getDefault();
                workingROI.setFixedSize(constraints);
                workingROI.setRectangle(new Rectangle(ix-constraints[0], iy-constraints[1], constraints[0], constraints[1]));
            } else {
                if (workingROI == null) {
                    workingROI = ROIRectangle.getDefault();
                    workingROI.addPoint(ix, iy);
                }
            }            
        } else {
            imageViewer.getROI_TreeTable().addROI(workingROI);
            workingROI = null;
            AdvancedVirtualMicroscope.getToolPanel().setDefaultTool();
        }
        imageViewer.repaint();
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {    }
    
    @Override
    public void mouseDragged(MouseEvent e) {    }
    
    @Override
    public void mousePressed(MouseEvent e) {    }
    
    @Override
    public void mouseEntered(MouseEvent e) {    }
    
    @Override
    public void mouseExited(MouseEvent e) {    }

    @Override
    public void paintOnImageViewer(Graphics2D g) {
        if (workingROI != null){
            imageViewer.concatenateImageToDisplayTransform(g);
            workingROI.paintROI(g, Color.GREEN);
        }
    }

    @Override
    public AbstractImageViewerTool getNewInstance(ImageViewer imageViewer) {
        return new RectangleTool(imageViewer);
    }
}
