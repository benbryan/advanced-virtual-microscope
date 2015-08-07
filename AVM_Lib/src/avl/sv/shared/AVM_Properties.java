package avl.sv.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AVM_Properties {
    
    public enum Name {
     upload_session_timeout_minutes,
     auto_login_password,
     username,
     server_url_,
     database_hosts,
     database_name,
     auto_login_destination,
     auto_login_server,
     CL_platform_Index,
     CL_device_Index
    }
        
    private static File propertiesFile = null;
    
    private static AVM_Properties instance = null;
    public static AVM_Properties getInstance(){
        if (instance == null){
            instance = new AVM_Properties();
            propertiesFile = getPropertiesFile();
        }
        return instance;
    }
    
    private static Properties getDefaultProperties(){
        Properties p = new Properties();
        p.setProperty(Name.database_hosts.name(), "localhost:5000");
        p.setProperty(Name.database_name.name(), "kvstore");
        p.setProperty(Name.upload_session_timeout_minutes.name(), "120");
        p.setProperty(Name.CL_platform_Index.name(), "0");
        p.setProperty(Name.CL_device_Index.name(), "0");
        return p;
    }
        
    private static Properties getProperties(){
        try {
            InputStream is = new FileInputStream(getPropertiesFile());
            Properties p = new Properties();
            p.load(is);
            return p;
        } catch (IOException ex) {
            Logger.getLogger(AVM_Properties.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    private static void setProperties(Properties p) {
        try (FileOutputStream fos = new FileOutputStream(getPropertiesFile())) {
            p.store(fos, null);
        } catch (IOException ex) {
            Logger.getLogger(AVM_Properties.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static String defaultDirectory() {
        String OS = System.getProperty("os.name").toUpperCase();
        if (OS.contains("WIN")){
            return System.getenv("APPDATA");
        }
        else if (OS.contains("MAC")){
            return System.getProperty("user.home") + "/Library/Application Support";
        }
        else if (OS.contains("NUX")){
            return System.getProperty("user.home");
        }
        return System.getProperty("user.dir");
    }

    public static File getPropertiesFile() {
        if (propertiesFile != null){
            return propertiesFile;
        }
        File f = new File(defaultDirectory() + File.separator + "AVM");
        if (!f.exists()){
            f.mkdirs();
        }
        f = new File(f.getPath() + File.separator + "AdvancedVirtualMicroscope.properties");
        if (!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException ex) {
                System.out.println("Failed to create " + f.getAbsolutePath());
                Logger.getLogger(AVM_Properties.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return f;
    }
    
    public static String getProperty(Name name){
        return getProperty(name.name());
    }
    
    public static String getProperty(String key){
        Properties p = getProperties();
        String s = p.getProperty(key);
        if (s == null){
            s = getDefaultProperties().getProperty(key);
            if (s == null){
                return null;
            }
            p.setProperty(key, s);
            setProperties(p);
        } 
        return s;
    }
    
    public static void setProperty(Name name, String value){
        setProperty(name.name(), value);
    }    
    public static void setProperty(String key, String value){
        Properties p = getProperties();
        p.setProperty(key, value);
        setProperties(p);
    }    
    
}
