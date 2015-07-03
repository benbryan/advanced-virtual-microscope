package avl.sv.client.tools;

import avl.intelligentScissors.IntelligentScissors;
import avl.intelligentScissors.IntelligentScissorsInterface;
import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.client.image.ImageViewer;
import avl.sv.shared.study.ROIPoly;
import avl.tiff.TiffDirectoryBuffer;
import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class IntelligentScissorsTool extends AbstractImageViewerTool {

    @Override
    public boolean canModify() {
        return true;
    }
    private final ImageViewer imageViewer;
    private TiffDirectoryBuffer workingDir;
        
    private static IntelligentScissorsToolPrompt options;
    private IntelligentScissors intelligentScissors;
    
    public static String toolTipText = "IntelligentScissors Tool";
    public static ImageIcon imageIcon = new ImageIcon(IntelligentScissorsTool.class.getResource("/avl/sv/client/icon/scissors.png"));
    
    @Override
    public AbstractImageViewerTool getNewInstance(ImageViewer imageViewer) {
        return new IntelligentScissorsTool(imageViewer);
    }
    
    public IntelligentScissorsTool() {
        imageViewer = null;
        setToolTipText(toolTipText);
        setIcon(imageIcon);
        options = new IntelligentScissorsToolPrompt(null, Dialog.ModalityType.APPLICATION_MODAL);
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem jMenuItemSetup = new JMenuItem("Setup");
        final IntelligentScissorsTool thisButton = this; 
        jMenuItemSetup.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: this probably does not work now
//                if (fixedROI != null){
//                    new MessageDialog(null, "Warning", "Cannot adjust settings with active segment");
//                    return;
//                }
                for (ActionListener listener:thisButton.getActionListeners()){
                    listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "popupmenu shown"));
                }
                options.setVisible(true);
                
            }
        });
        menu.add(jMenuItemSetup);
        jMenuItemSetup.setVisible(true);
        setComponentPopupMenu(menu);     
    }
    
    public IntelligentScissorsTool(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;  
        IntelligentScissorsInterface intelligentScissorsInterface = new IntelligentScissorsInterface() {
            @Override
            public BufferedImage getImage(Rectangle window) {
                return imageViewer.getImageSource().getSubImage(window, workingDir);
            }

            @Override
            public void roiFinished(Path2D.Double path) {
                ROIPoly roi = ROIPoly.getDefault();
                AffineTransform at = new AffineTransform();
                double zoom = workingDir.getZoomLevel();
                at.scale(1/zoom, 1/zoom);
                path.transform(at);
                roi.setPath(path);
                imageViewer.getROI_TreeTable().addROI(roi);
                AdvancedVirtualMicroscope.getToolPanel().setDefaultTool();
            }

            @Override
            public void updated() {
                imageViewer.repaint();
            }
            
        };
        intelligentScissors = new IntelligentScissors(intelligentScissorsInterface);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        convertImageSpace(e);
        intelligentScissors.mouseClicked(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        
        convertImageSpace(e);
        intelligentScissors.mouseMoved(e);
    }

    private void convertImageSpace(MouseEvent e){
        Point pImg = imageViewer.displaySpaceToImageSpace(e.getPoint());
        int x = (int) pImg.getX();
        int y = (int) pImg.getY();     
        if (workingDir == null){
            return;
        }
        x = (int)((double)x*(double)workingDir.getZoomLevel());
        y = (int)((double)y*(double)workingDir.getZoomLevel());
        e.translatePoint(x-e.getPoint().x, y-e.getPoint().y);
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        convertImageSpace(e);
        intelligentScissors.mouseDragged(e);
    }

    @Override
    public void paintOnImageViewer(Graphics2D g) {
        if (!intelligentScissors.isActive()){
            return;
        }
        imageViewer.concatenateImageToDisplayTransform(g);
        g.scale(1/workingDir.getZoomLevel(), 1/workingDir.getZoomLevel());
        intelligentScissors.paint(g);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!e.getSource().equals(imageViewer)) {
            return;
        }
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        if (!intelligentScissors.isActive()){            
            intelligentScissors.setApproximateCosInv(options.isApproximateCosInv());
            intelligentScissors.setWeights(options.weights);
            intelligentScissors.setDemo(options.isDemo());
            if (options.isWorkOnBaseResolution()){
                workingDir = imageViewer.getImageSource().getBaseDirectoryBuffer();
            } else {
                //Iterates through the tiff dirs to find the lowest resolution in view
                TreeMap<Double, TiffDirectoryBuffer> treeMap = imageViewer.getImageSource().getZoomMap();
                Map.Entry<Double, TiffDirectoryBuffer> thumbnail = treeMap.firstEntry();
                SortedMap<Double, TiffDirectoryBuffer> m = treeMap.tailMap(thumbnail.getKey());
                workingDir = m.get(m.firstKey());
                for(double zoom:m.keySet()) {
                    if (zoom <= 2 * imageViewer.getMagnification()) {
                        workingDir = m.get(zoom);
                    }
                }
            }
            int M = workingDir.getImageWidth();
            int N = workingDir.getImageLength();
            intelligentScissors.setImageSize(M,N);
        }
        convertImageSpace(e);
        intelligentScissors.mouseReleased(e);
    }
   
    @Override
    public void mousePressed(MouseEvent e) {    }

    @Override
    public void mouseEntered(MouseEvent e) {  }

    @Override
    public void mouseExited(MouseEvent e) {  } 
}
