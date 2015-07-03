package avl.sv.client.tools;

import avl.sv.client.image.ImageViewer;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;

/**
 *
 * @author benbryan
 */
public class PanTool extends AbstractImageViewerTool {
    final ImageViewer imageViewer;

    public static String toolTipText = "Pan Image" ;
    public static ImageIcon imageIcon = new ImageIcon(PanTool.class.getResource("/avl/sv/client/icon/pan.png"));
    public PanTool() {
        setToolTipText(toolTipText);
        setIcon(imageIcon);
        imageViewer = null;
    }   
    
    @Override
    public boolean canModify() {
        return false;
    }
    
    public PanTool(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }

    @Override
    public void mouseMoved(MouseEvent e) {   }

    @Override
    public void mouseClicked(MouseEvent e) {    }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseDragged(MouseEvent e) {  }

    @Override
    public void mousePressed(MouseEvent e) {   }
    
    @Override
    public void mouseEntered(MouseEvent e) {    }

    @Override
    public void mouseExited(MouseEvent e) {    }

    @Override
    public void paintOnImageViewer(Graphics2D g) {    }

    @Override
    public AbstractImageViewerTool getNewInstance(ImageViewer imageViewer) {
        return new PanTool(imageViewer);
    }
    
    
}
    
    
