package avl.sv.client.tools;

import avl.sv.client.image.ImageViewer;
import java.awt.Graphics2D;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JToggleButton;

/**
 *
 * @author benbryan
 */
public abstract class AbstractImageViewerTool extends JToggleButton implements MouseMotionListener, MouseListener  {
    public abstract boolean canModify();
    protected final int cornerMarkerDim = 10;
    public abstract void paintOnImageViewer(Graphics2D g);
    public abstract AbstractImageViewerTool getNewInstance(ImageViewer imageViewer);
}
