/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.sv.client.solution;

import java.awt.geom.Path2D;

/**
 *
 * @author benbryan
 */
public class Shapes {
    public static  Path2D.Double getPolygon(int nPoints, double r){
        nPoints*=2;
        double t[] = new double[nPoints];
        for (int i = 0; i < nPoints; i++){
            t[i] = i*(2*Math.PI/nPoints);
        }       
        
        double x[] = new double[nPoints];
        double y[] = new double[nPoints];

        for (int i = 0; i < nPoints; i+=2){
            x[i] = r*Math.cos(t[i]);
            y[i] = r*Math.sin(t[i]);         
        }    
        for (int i = 1; i < nPoints; i+=2){
            x[i] = r/3*Math.cos(t[i]);
            y[i] = r/3*Math.sin(t[i]);         
        }  
        
        Path2D.Double path = new  Path2D.Double();
        path.moveTo(r, 0);
        for (int i = 1; i < nPoints; i++){   
            path.lineTo(x[i], y[i]);
        }        
        path.closePath();
        return path;
    }

}
