/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.sv.shared.solution;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Sample {

    public double featureVector[];

    /**
     * tile represents the image area this sample represents
     */
    public Rectangle tile;

    /**
     *window represents the area of the image used to generate this sample
     */
    public Rectangle window;
    public double classifierLabel;
    public BufferedImage img;

    /**
     *
     * @param tile
     * represents the image area this sample represents
     * @param window
     * window represents the area of the image used to generate this sample
     */
    public Sample(Rectangle tile, Rectangle window) {
        this.tile = tile;
        this.window = window;
    }

    @Override
    public String toString() {
        return "x="+tile.x+", y="+tile.y+", width="+tile.width+" height="+tile.height;
    }
    
    
    
}
