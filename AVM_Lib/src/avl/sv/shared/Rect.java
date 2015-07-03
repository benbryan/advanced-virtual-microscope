/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.sv.shared;

/**
 *
 * @author benbryan
 */
public class Rect {

    double lx;
    double ly;
    double ux;
    double uy;

    @Override
    public String toString() {
        return "lx = " + String.valueOf(lx) + ",ly = " + String.valueOf(ly) + ",ux = " + String.valueOf(ux) + ",uy = " + String.valueOf(uy);
    }

    public Rect(int lx, int ly, int ux, int uy) {
        this.lx = lx;
        this.ly = ly;
        this.ux = ux;
        this.uy = uy;
    }

    public Rect(double lx, double ly, double ux, double uy) {
        this.lx = lx;
        this.ly = ly;
        this.ux = ux;
        this.uy = uy;
    }

    public int getLx() {
        return (int) lx;
    }

    public int getLy() {
        return (int) ly;
    }

    public int getUx() {
        return (int) ux;
    }

    public int getUy() {
        return (int) uy;
    }

}
