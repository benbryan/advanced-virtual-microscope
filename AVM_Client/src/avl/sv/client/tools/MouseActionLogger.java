package avl.sv.client.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;

public class MouseActionLogger {
    private ButtonStatus mouseButton1 = ButtonStatus.RELEASED, mouseButton2 = ButtonStatus.RELEASED, mouseButton3 = ButtonStatus.RELEASED;
    private int mouseLastX = Integer.MAX_VALUE, mouseLastY = Integer.MAX_VALUE;
    public int deltaX = 0, deltaY = 0;
    private int clickCount = 0;
    private Point lastClickPoint = new Point(0, 0);
    public Point getLastMouseLocation(){
        return new Point(mouseLastX,mouseLastY);
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getPoint().distance(lastClickPoint) <= 1){
            clickCount++;
        } else {
            clickCount = 0;
        }
        lastClickPoint = e.getPoint();
    }
    
    public int getClickCount(){
        return clickCount;
    }
    
    public enum ButtonStatus{ PRESSED, RELEASED
    }
    
    public ButtonStatus getMouseButton1() {
        return mouseButton1;
    }

    public ButtonStatus getMouseButton2() {
        return mouseButton2;
    }

    public ButtonStatus getMouseButton3() {
        return mouseButton3;
    }
    
    public void translate(int x, int y){
        mouseLastX += x;
        mouseLastY += y;
    }
    
    public void mouseDragged(MouseEvent e) {   
        if (mouseLastX != Integer.MAX_VALUE) {
            deltaX = e.getX() - mouseLastX;
            deltaY = e.getY() - mouseLastY;
            mouseLastX = e.getX();
            mouseLastY = e.getY();
        } else {
            deltaX = 0;
            deltaY = 0;
            mouseLastX = e.getX();
            mouseLastY = e.getY();
        }
    }
    
    public void mouseMoved(MouseEvent e) {   
        mouseDragged(e);
    }

    public void mousePressed(MouseEvent e) {
        mouseLastX = e.getX();
        mouseLastY = e.getY(); 
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                mouseButton1 = ButtonStatus.PRESSED;
                break;
            case MouseEvent.BUTTON2:
                mouseButton2 = ButtonStatus.PRESSED;
                break;
            case MouseEvent.BUTTON3:
                mouseButton3 = ButtonStatus.PRESSED;
                break;
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                mouseButton1 = ButtonStatus.RELEASED;
                break;
            case MouseEvent.BUTTON2:
                mouseButton2 = ButtonStatus.RELEASED;
                break;
            case MouseEvent.BUTTON3:
                mouseButton3 = ButtonStatus.RELEASED;
                break;
        }
    }


}
