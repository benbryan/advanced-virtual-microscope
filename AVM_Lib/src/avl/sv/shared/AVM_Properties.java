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
    public static final String UPLOAD_SESSION_TIMEOUT_MINUTES = "upload_session_timeout_minutes";
    public static final String AUTO_LOGIN_PASSWORD_KEY = "auto_login_password";
    public static final String USERNAME_KEY = "username";
    public static String SERVER_URL_BASE_KEY = "server_url_";
    public static String DATABASE_HOSTS_KEY = "database_hosts";
    public static String DATABASE_NAME_KEY = "database_name";
    public static String AUTO_LOGIN_DESTINATION_KEY = "auto_login_destination";
    public static String AUTO_LOGIN_SERVER_KEY = "auto_login_server";
        
    private static Properties getDefaultProperties(){
        Properties p = new Properties();
        p.setProperty(DATABASE_HOSTS_KEY, "localhost:5000");
        p.setProperty(DATABASE_NAME_KEY, "kvstore");
        p.setProperty(UPLOAD_SESSION_TIMEOUT_MINUTES, "120");
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
    
    public static void setProperty(String key, String value){
        Properties p = getProperties();
        p.setProperty(key, value);
        setProperties(p);
    }    
    
}
