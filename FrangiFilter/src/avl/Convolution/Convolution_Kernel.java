/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.Convolution;

import java.awt.image.Kernel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author benbryan
 */
public class Convolution_Kernel {
    final private String type;
    final private Properties p = new Properties();
    final private Kernel kernel;
    float results[];

    public String getType() {
        return type;
    }

    public Kernel getKernel() {
        return kernel;
    }

    public Convolution_Kernel(String type, Kernel kernel) {
        this.type = type;
        this.kernel = kernel;
    }
    public void addProperty(String name, Object value){
        p.put(name, value);
    }
    
    public Object getProperty(String name){
        return p.get(name);
    }
    public Enumeration getPropertyNames(){
        return p.propertyNames();
    }
}
