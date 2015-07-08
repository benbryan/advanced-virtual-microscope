package avl.sv.client.image;

import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.client.tools.MouseActionLogger;
import avl.sv.client.Thumbnail;
import avl.sv.client.study.ROI_ManagerPanel;
import avl.sv.client.study.ROI_TreeTable;
import avl.sv.client.tools.AbstractImageViewerTool;
import avl.sv.client.tools.PanTool;
import avl.sv.client.tools.SelectTool;
import avl.sv.shared.Rect;
import avl.sv.shared.image.ImageSource;
import avl.tiff.TiffDirectoryBuffer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSlider;
import javax.swing.KeyStroke;

public class ImageViewer extends javax.swing.JPanel implements MouseMotionListener, MouseListener, MouseWheelListener {

    private final MouseActionLogger mouseAction = new MouseActionLogger();
    private double imageOffsetX = 0, imageOffsetY = 0;
    private double magnification = 1;
    private final double objectiveLensesMagnification = 100;//20;
    private final Thumbnail thumbnailPannel;
    private final ImageSource imageSource;
    private JSlider zoolSlider;
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> hideZoomImagerTaskFuture;
    private ArrayList<ImageViewerPlugin> plugins = new ArrayList<>();
    private ROI_TreeTable roi_TreeTable;
    private AbstractImageViewerTool activeTool;
    public boolean readOnly = true;
    private final int panelIndicatorWidth = 20;   
    private ROI_ManagerPanel roiManagerPanel;

    public ROI_TreeTable getROI_TreeTable() {
        return roi_TreeTable;
    }
    
    public ROI_ManagerPanel getROI_ManagerPanel(){
        return roiManagerPanel;
    }
        
    public void setROI_ManagerPanel(ROI_ManagerPanel roiManagerPanel){
        this.roiManagerPanel = roiManagerPanel;
        this.roi_TreeTable = this.roiManagerPanel.getROI_TreeTable();
        roi_TreeTable.addActionListener((ActionEvent e) -> {
            repaint();
        });
        add(roiManagerPanel);
    }  

    public ImageViewer(final ImageSource imageSource) {
        this.imageSource = imageSource;
        imageSource.setPostBufferFillEvent(new Repaint());
        initComponents();
       
        thumbnailPannel = new Thumbnail(this);
        add(thumbnailPannel);
        thumbnailPannel.setSize(10, 10);
        thumbnailPannel.setLocation(0,0);
        
        setupKeyStrokes();
        setupZoomImager();
        
        setVisible(true);
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        
        setFocusable(true);
    }
        
    Runnable hideZoomImagerTask = new Runnable() {
        @Override
        public void run() {
            zoolSlider.setVisible(false);
        }
    };
    
    public void setImageRegion(Rectangle bounds){
        bounds.grow(bounds.width/4, bounds.height/4);
        double mW = (double) getWidth() / bounds.width;
        double mH = (double) getHeight() / bounds.height;
        double mag = Math.min(mW, mH);
        magnification = mag;
        imageOffsetX = (-bounds.x+(getWidth()/mag-bounds.width)/2);
        imageOffsetY = (-bounds.y+(getHeight()/mag-bounds.height)/2);
        repaint();
    }
    
    public void setImageOffsetX(double imageOffsetX) {
        this.imageOffsetX = imageOffsetX;
    }

    public void setImageOffsetY(double imageOffsetY) {
        this.imageOffsetY = imageOffsetY;
    }

    public void close() {
        imageSource.close();
        for (ImageViewerPlugin plugin : plugins) {            
            plugin.close();
        }
    }
            
    public ImageSource getImageSource() {
        return imageSource;
    }
       
    private void setupKeyStrokes() {
        
        final Action zoomOut = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double delta = 1-0.3;
                Point p = new Point(getWidth()/2, getHeight()/2);
                zoomInOnPoint(p, delta);
            }
        };
        getInputMap().put(KeyStroke.getKeyStroke('-'), "zoomOut");
        getInputMap().put(KeyStroke.getKeyStroke('_'), "zoomOut");
        getActionMap().put("zoomOut", zoomOut);    
        
        final Action zoomIn = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double delta = 1+0.3;
                Point p = new Point(getWidth()/2, getHeight()/2);
                zoomInOnPoint(p, delta);
            }
        };
        getInputMap().put(KeyStroke.getKeyStroke('='), "zoomIn");
        getInputMap().put(KeyStroke.getKeyStroke('+'), "zoomIn");
        getActionMap().put("zoomIn", zoomIn);
        
        
        final int panStep = 20;
        final Action panLeft = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageOffsetX += panStep / magnification;
                repaint();
            }
        };
        getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "panLeft");
        getActionMap().put("panLeft", panLeft);

        final Action panRight = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageOffsetX -= panStep / magnification;
                repaint();
            }
        };
        getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "panRight");
        getActionMap().put("panRight", panRight);
        
        final Action panUp = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageOffsetY += panStep / magnification;
                repaint();
            }
        };
        getInputMap().put(KeyStroke.getKeyStroke("UP"), "panUp");
        getActionMap().put("panUp", panUp);
        
        final Action panDown = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageOffsetY -= panStep / magnification;
                repaint();
            }
        };
        getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "panDown");
        getActionMap().put("panDown", panDown);        
    }
    
    private void setupZoomImager(){
        zoolSlider = new JSlider(JSlider.VERTICAL);
        zoolSlider.setMinimum((int)(0));
        zoolSlider.setMaximum((int)(objectiveLensesMagnification));
        zoolSlider.setSize(50, 100);
        zoolSlider.setVisible(false);
        double numOfTicks = 5;
        zoolSlider.setMajorTickSpacing((int)(objectiveLensesMagnification/numOfTicks));
        zoolSlider.setMinorTickSpacing((int)(objectiveLensesMagnification/numOfTicks));
        zoolSlider.setPaintTicks(true);
        zoolSlider.setPaintLabels(true);     
        add(zoolSlider);     
        zoolSlider.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                zoolSlider.setVisible(false);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
            }
        });
    }

    public Point displaySpaceToImageSpace(Point p1) {
        Point2D p2 = new Point2D.Double(p1.x, p1.y);
        p2 = displaySpaceToImageSpace(p2);
        return new Point((int)p2.getX(), (int)p2.getY());
    }
    
    public Point2D displaySpaceToImageSpace(Point2D p1) {
        Point2D p2 = null;
        try {
            p2 = (Point2D) p1.clone();
            Graphics2D g = (Graphics2D) getGraphics().create();
            concatenateImageToDisplayTransform(g);
            g.getTransform().inverseTransform(p1, p2);
        } catch (NoninvertibleTransformException ex) {
        }
        return p2;
    }
        
    public void centerImage(){
        magnification = getDefaultMagnification();
        imageOffsetX = getWidth()/2/magnification-imageSource.getImageDimX()/2;
        imageOffsetY = getHeight()/2/magnification-imageSource.getImageDimY()/2;
    }

    public double getDefaultMagnification() {
        double mW = (double) getWidth() / imageSource.getImageDimX();
        double mH = (double) getHeight() / imageSource.getImageDimY();
        double mag = Math.min(mW, mH);
        return mag; 
    }
       
    public double getMagnification() {
        return magnification;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        super.paintComponent(g);
        thumbnailPannel.adjustPosition();
        g.setClip(0, 0, getWidth(), getHeight());
        if (imageSource != null) {
            paintDirsInOrder(g);
        }
        if (roi_TreeTable != null){
            Graphics2D gTemp = (Graphics2D) g.create();
            concatenateImageToDisplayTransform(gTemp);
            roi_TreeTable.paintROIs(this, gTemp);
        }
        
        if (plugins != null){
            for (ImageViewerPlugin result:plugins){
                result.paintPlugin(this, g);
            }
        }
        if (activeTool != null){
            activeTool.paintOnImageViewer((Graphics2D) g.create());
        }
        if (roi_TreeTable != null){
            paintIndicator(g);
        }
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    }
    
    private void paintIndicator(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, panelIndicatorWidth, getHeight());
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, panelIndicatorWidth, getHeight());
        AffineTransform at = new AffineTransform();
        at.concatenate(((Graphics2D) g).getTransform());
        at.translate(15, getHeight() / 2);
        at.rotate(-Math.PI / 2);
        g2d.setTransform(at);
        g2d.drawString("ROI Manager", 0, 0);
    }

    
    private BufferedImage imageNotFound = null;
    private void paintDirsInOrder(Graphics g){     
        TreeMap<Double, TiffDirectoryBuffer> treeMap = imageSource.getZoomMap();
        if (treeMap.isEmpty()){
            InputStream is = getClass().getResourceAsStream("imageNotFound.png");
            if (roi_TreeTable.isVisible()){
                roi_TreeTable.setVisible(false);
            }
            try {
                if (imageNotFound == null){
                    imageNotFound = ImageIO.read(is);
                }
                g.drawImage(imageNotFound, 0, 0, null);
                return;
            } catch (IOException ex) {
                Logger.getLogger(ImageViewer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Entry<Double, TiffDirectoryBuffer> thumbnail = treeMap.firstEntry();
        paintDir(thumbnail.getValue(),g);
        
        SortedMap<Double, TiffDirectoryBuffer> m = treeMap.tailMap(thumbnail.getKey());
        Iterator<TiffDirectoryBuffer> dirIt = m.values().iterator();
        while(dirIt.hasNext()){
            TiffDirectoryBuffer dir = dirIt.next();
            if (dir.getZoomLevel() <= 4*magnification){
                paintDir(dir,g);
                continue;
            } 
            dir.setTilesInView(null);
        }
    }
    
    public void concatenateImageToDisplayTransform(Graphics2D g) {
        concatenateImageToDisplayTransform(g, 1.0);
    }
    public void concatenateImageToDisplayTransform(Graphics2D g, double zoomLevel) {
        g.scale(magnification, magnification);
        g.translate(imageOffsetX, imageOffsetY);
        g.scale(1/zoomLevel, 1/zoomLevel);
    }
           
    private void paintDir(TiffDirectoryBuffer dir, Graphics gOrig) {
        Graphics2D g = (Graphics2D)gOrig.create();

        concatenateImageToDisplayTransform(g, dir.getZoomLevel());
        Shape temp = g.getClip();
        if (temp == null){ //This may never happen
            return;
        }
        Rectangle2D clip = g.getClip().getBounds2D();      
        clip.setRect( clip.getMinX(), 
                      clip.getMinY(), 
                      Math.min(clip.getMaxX(), dir.getImageWidth())-clip.getMinX(), 
                      Math.min(clip.getMaxY(), dir.getImageLength())-clip.getMinY());
        g.setClip(clip);
        Rect imgInView = findImageInView(dir, g);
        Point tilesToPlot[] = imageSource.findTilesRect(dir, imgInView);
        imageSource.paintTiles(dir, g, tilesToPlot);
    }
            
    public Rect findImageInView(TiffDirectoryBuffer dir, Graphics2D g){
        Rectangle cb = g.getClipBounds();
        Rect out = new Rect( Math.max(0, cb.x), 
                             Math.max(0, cb.y), 
                             cb.x+cb.width, 
                             cb.y+cb.height);
        return out;
    }

    public void addPlugin(final ImageViewerPlugin plugin) {
        if (plugins == null){
            plugins = new ArrayList<>();
        }
        plugin.addImageViewerPluginListener(new ImageViewerPluginListener() {
            @Override
            public void diapose() {
                plugins.remove(plugin);
            }
        });
        plugins.add(plugin);
        repaint();
    }
    public double getImageOffsetX() {
        return imageOffsetX;
    }

    public double getImageOffsetY() {
        return imageOffsetY;
    }

    public int getROI_PanelIndicatorWidth() {
        return panelIndicatorWidth;
    }
         
    private class Repaint implements Callable {
        @Override
        public Object call() throws Exception {
            repaint();
            return 0;
        }
    }
        
    public void setImagePosistion(double percentX, double percentY) {
        imageOffsetX = (-percentX * imageSource.getImageDimX() + getWidth() /2/getMagnification());
        imageOffsetY = (-percentY * imageSource.getImageDimY() + getHeight()/2/getMagnification());
        forceImageIntoBounds();
        repaint();
    }
    
    private void forceImageIntoBounds(){
        imageOffsetX = Math.min(imageOffsetX, (getWidth()*0.9)/magnification);
        imageOffsetX = Math.max(imageOffsetX, -imageSource.getImageDimX()+(getWidth()*0.1)/magnification);        
        imageOffsetY = Math.min(imageOffsetY, (getHeight()*0.9)/magnification);
        imageOffsetY = Math.max(imageOffsetY, -imageSource.getImageDimY()+(getHeight()*0.1)/magnification);        
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseAction.mouseDragged(e);
        if (mouseAction.getMouseButton3() == MouseActionLogger.ButtonStatus.PRESSED){
            imageOffsetX += (mouseAction.deltaX) / magnification;       
            imageOffsetY += (mouseAction.deltaY) / magnification;
            forceImageIntoBounds();
        }
        if (mouseAction.getMouseButton1() == MouseActionLogger.ButtonStatus.PRESSED){
            if (activeTool != null){
                if (activeTool instanceof PanTool){
                    imageOffsetX += (mouseAction.deltaX) / magnification;       
                    imageOffsetY += (mouseAction.deltaY) / magnification;
                    forceImageIntoBounds();
                }
            }
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseAction.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseAction.mouseReleased(e);
    }

    @Override
    public void mouseEntered(MouseEvent e){
       
    }

    @Override
    public void mouseExited(MouseEvent e){}
        
    @Override
    public void mouseMoved(MouseEvent e) {   
        mouseAction.mouseMoved(e);
        AdvancedVirtualMicroscope.getToolPanel().setEditingToolsState(!readOnly);
        AbstractImageViewerTool selectedTool = AdvancedVirtualMicroscope.getToolPanel().getSelectedTool();
        if (readOnly){
            if ((selectedTool.getClass() != PanTool.class) && (selectedTool.getClass() != SelectTool.class)){
                activeTool = null;
                return;
            }
        }
            
        if (activeTool == null){
            activeTool = selectedTool.getNewInstance(this);
            addMouseListener(activeTool);
            addMouseMotionListener(activeTool);
        } else if (activeTool.getClass() != selectedTool.getClass()){
            removeMouseListener(activeTool);
            removeMouseMotionListener(activeTool);
            activeTool = selectedTool.getNewInstance(this); 
            addMouseListener(activeTool);
            addMouseMotionListener(activeTool);
        }        
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        requestFocus();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Point p = e.getPoint();
        double delta = 1 - e.getWheelRotation() * 0.2;
        zoomInOnPoint(p, delta);
    }
    
    public void zoomInOnPoint(Point p, double delta){
        if (p == null){
            return;
        }
        double ref[] = {p.getX(),   p.getY()};
        double marksPre[] = new double[ref.length];
        double marksPost[] = new double[ref.length];

        Graphics2D gTemp = (Graphics2D) getGraphics().create();
        try {
            gTemp.setTransform(new AffineTransform());
            concatenateImageToDisplayTransform(gTemp);
            gTemp.getTransform().inverseTransform(ref, 0, marksPre, 0, 1);
        } catch (NoninvertibleTransformException ex) {       }

        changeMagnification(delta);

        try {
            gTemp.setTransform(new AffineTransform());
            concatenateImageToDisplayTransform(gTemp);
            gTemp.getTransform().inverseTransform(ref, 0, marksPost, 0, 1);
        } catch (NoninvertibleTransformException ex) {       }
        
        imageOffsetX += marksPost[0]-marksPre[0];
        imageOffsetY += marksPost[1]-marksPre[1];  
        forceImageIntoBounds();
    }

    public void changeMagnification(double delta) {
        if ((imageSource == null) || (delta == 0)) {
            return;
        }
        double minMagnification = getDefaultMagnification()/2;
        double maxMagnification = 1;
        double newMag = magnification * delta;
        if (newMag > maxMagnification) {
            magnification = maxMagnification;
        } if (newMag < minMagnification){
            magnification = minMagnification;
        } else {
            magnification *= delta;
        }
        
        zoolSlider.setValue((int)(magnification*objectiveLensesMagnification));
        Point p = mouseAction.getLastMouseLocation();
        Dimension s = zoolSlider.getSize();
        zoolSlider.setLocation(p.x-s.width,p.y-s.height/2);
        zoolSlider.setVisible(true);
        if (hideZoomImagerTaskFuture != null){
            hideZoomImagerTaskFuture.cancel(true);
        }
        hideZoomImagerTaskFuture = worker.schedule(hideZoomImagerTask, 1, TimeUnit.SECONDS);
        
        repaint();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                formMouseExited(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 786, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 504, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseExited
        
    }//GEN-LAST:event_formMouseExited

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
