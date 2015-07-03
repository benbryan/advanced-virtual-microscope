package avl.sv.shared;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import javax.swing.JProgressBar;
import javax.swing.Painter;

public class ProgressBarForegroundPainter implements Painter<JProgressBar> {
    @Override
    public void paint(Graphics2D g, JProgressBar pbar, int width, int height) {
        int w = (int) ((double)(pbar.getWidth())*pbar.getPercentComplete());
        int h = pbar.getHeight();               
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new LinearGradientPaint(0, 0, 0, h/2, new float[]{0,1}, new Color[]{new Color(1f, 1f, 1f, 0), Color.blue}, MultipleGradientPaint.CycleMethod.REFLECT));
        g2d.fillRect(0, 0, w, h);
    }
}
