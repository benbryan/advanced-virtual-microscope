/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.sv.client.image;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JFrame;

public final class ImageViewerJFrame extends JFrame{
    private final ImageViewer imageViewer;

    public ImageViewerJFrame(ImageViewer imageViewer, String title) {
        super(title);
        this.imageViewer = imageViewer;
    }

    public ImageViewer getImageViewer() {
        return imageViewer;
    }

    @Override
    public void dispose() {
        imageViewer.close();
        super.dispose(); //To change body of generated methods, choose Tools | Templates.
    }

}