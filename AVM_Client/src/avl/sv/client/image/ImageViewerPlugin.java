package avl.sv.client.image;

import java.awt.Graphics;
import java.awt.event.WindowAdapter;

public interface ImageViewerPlugin {
    public void paintPlugin(ImageViewer imageViewer, Graphics g);
    public void close();
    public void addImageViewerPluginListener(ImageViewerPluginListener imageViewerPluginListener);
}
