package avl.sv.client;

import javax.swing.JOptionPane;

public class MessagesDefault {
  
    public static void sessionExpired(){
        JOptionPane.showMessageDialog(null, "Session expired, AVM will now close", "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }
}
