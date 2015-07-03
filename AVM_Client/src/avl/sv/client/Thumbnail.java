package avl.sv.client;

import avl.sv.client.tools.MouseActionLogger;
import avl.sv.client.image.ImageViewer;
import avl.sv.shared.Rect;
import avl.sv.shared.image.ImageSource;
import avl.tiff.TiffDirectoryBuffer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class Thumbnail extends JPanel implements MouseMotionListener, MouseListener {

    MouseActionLogger mouseAction = new MouseActionLogger();
    ImageViewer imageViewer;
    ImageSource imageSource;
    boolean atDefaultLocation = true;

    public Thumbnail(ImageViewer sv) {
        this.imageViewer = sv;
        imageSource = sv.getImageSource();
        init();
    }

    private void init() {
        setLayout(null);
        setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
        setBackground(Color.BLACK);
        addMouseMotionListener(this);
        addMouseListener(this);
        //addMouseWheelListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintThumbnail(g);
        paintVisableRegionOnThumbnail(g);
    }

    public void adjustPosition() {
        int lx = imageViewer.getSize().width - getWidth()-5;
        int ly = imageViewer.getSize().height - getHeight();
        if (atDefaultLocation) {
            setLocation(lx, 0);
        } else {
            setLocation(Math.min(getLocation().x, lx), Math.min(getLocation().y, ly));
        }
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        adjustPosition();
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        adjustPosition();
    }

    private void paintThumbnail(Graphics gOrig) {
        if (imageSource.getZoomMap().isEmpty()){
            return;
        }
        Graphics2D g = (Graphics2D) gOrig.create();
        TiffDirectoryBuffer dir = imageSource.getThumbnailDirectoryBuffer();

        int w = dir.getImageWidth();
        int l = dir.getImageLength();
        double a = Math.sqrt(w * w + l * l);
        double m = Math.sqrt(getParent().getWidth() * getParent().getWidth() + getParent().getHeight() * getParent().getHeight());
        double z = m / a / 5;

        setSize((int) Math.ceil(z * w), (int) Math.ceil(z * l));

        Rect imgInView = new Rect(0, 0, w, l);
        AffineTransform at = g.getTransform();
        at.scale(z, z);
        g.setTransform(at);
        
        Point tilesToPlot[] = imageSource.findTilesRect(dir, imgInView);
        imageSource.paintTiles(dir, g, tilesToPlot);
    }

    private void paintVisableRegionOnThumbnail(Graphics gOrig) {
        Graphics2D g = (Graphics2D) gOrig.create();

        double ref[] = {0, 0, imageViewer.getWidth(), imageViewer.getHeight()};
        Graphics2D gTemp = (Graphics2D) g.create();
        gTemp.setTransform(new AffineTransform());
        imageViewer.concatenateImageToDisplayTransform(gTemp);
        try {
            gTemp.getTransform().inverseTransform(ref, 0, ref, 0, 2);
        } catch (NoninvertibleTransformException ex) {
        }

        int lx = (int) (ref[0] / imageSource.getImageDimX() * getWidth());
        int ly = (int) (ref[1] / imageSource.getImageDimY() * getHeight());
        int ux = (int) (ref[2] / imageSource.getImageDimX() * getWidth());
        int uy = (int) (ref[3] / imageSource.getImageDimY() * getHeight());

        g.drawRect(lx, ly, ux - lx, uy - ly);
    }

    private void shiftParent(MouseEvent e) {
        double percentX = (double) e.getX() / getWidth();
        double percentY = (double) e.getY() / getHeight();      
        imageViewer.setImagePosistion(percentX, percentY);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseAction.mouseDragged(e);
        if (mouseAction.getMouseButton1() == MouseActionLogger.ButtonStatus.PRESSED) {
            shiftParent(e);
        } else if (mouseAction.getMouseButton3() == MouseActionLogger.ButtonStatus.PRESSED) {
            atDefaultLocation = false;
            int dx = mouseAction.deltaX, dy = mouseAction.deltaY;
            Point p = getLocation();
            setLocation(p.x + dx, p.y + dy);
            mouseAction.translate(-dx, -dy);
            getParent().repaint();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseAction.mousePressed(e);
        if (mouseAction.getMouseButton1() == MouseActionLogger.ButtonStatus.PRESSED) {
            shiftParent(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseAction.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
//    @Override
//    public void mouseWheelMoved(MouseWheelEvent e) {
//        // This will zoom in on on the point over which the mouse is hovering (in the thumbnail)
//        Point p = e.getPoint();
//        double delta = 1 - e.getWheelRotation() * 0.2;
//        
//        int imageX = (int) (((double) e.getX() / (double)getWidth()) * imageSource.getImageDimX());
//        int imageY = (int) (((double) e.getY() / (double)getHeight()) * imageSource.getImageDimY());    
//        Graphics2D g = (Graphics2D) imageViewer.getGraphics();
//        imageViewer.concatenateImageToDisplayTransform(g);
//        Point displayPoint = new Point();
//        g.getTransform().transform(new Point(imageX, imageY), displayPoint);
//        
//        imageViewer.zoomInOnPoint(displayPoint, delta);
//    }
    
}
