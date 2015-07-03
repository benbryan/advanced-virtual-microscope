package avl.sv.shared;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NamedNodeMapFunc {
    public static boolean getBoolean(NamedNodeMap map, String itemName ){
       Node n = map.getNamedItem(itemName);
       if (n == null){
           return false;
       }else {
            String s = n.getNodeValue();
            if (s.contains("1") || Boolean.parseBoolean(s)){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static int getInteger(NamedNodeMap map, String itemName ){
       Node n = map.getNamedItem(itemName);
       if (n == null){
           return 0;
       }else {
            return Integer.parseInt(n.getNodeValue());
        }
    }
    
    public static String getString(NamedNodeMap map, String itemName ){
       Node n = map.getNamedItem(itemName);
       if (n == null){
           return "";
       }else {
            return n.getNodeValue();
        }
    }
    
    public static double getDouble(NamedNodeMap map, String itemName ){
       Node n = map.getNamedItem(itemName);
       if (n == null){
           return 0;
       }else {
            return Double.parseDouble(n.getNodeValue());
        }
    }
    public static long getLong(NamedNodeMap map, String itemName ){
       Node n = map.getNamedItem(itemName);
       if (n == null){
           return 0;
       }else {
            return Long.parseLong(n.getNodeValue());
        }
    }
}
