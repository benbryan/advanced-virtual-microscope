package avl.sv.shared;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;

public class User_KVStore {
    public static String changePassword( final String username, final String oldPassword, final String newPassword) {
        KVStore kvstore = KVStoreRef.getRef();
        try {           
            ArrayList<String> major = new ArrayList<>();
            major.add("user");
            major.add(username.toLowerCase());
            ArrayList<String> minor = new ArrayList<>();
            minor.add("password");   
                        
            String currentHash = new String(kvstore.get(Key.createKey(major, minor)).getValue().getValue());
            if (PasswordHash.validatePassword(oldPassword, currentHash)){
                String newHash = PasswordHash.createHash(newPassword);  
                kvstore.put(Key.createKey(major, minor), Value.createValue(newHash.getBytes()));
                return "password changed";
            } else {
                return "error: incorrect password";
            }                     
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(User_KVStore.class.getName()).log(Level.SEVERE, null, ex);
            return "error: something failed on the server while changing password " + username;
        }
    }
    

    
    public static boolean isValid(String username, String password) {
        try {
            ArrayList<String> major = new ArrayList<>();
            major.add("user");
            major.add(username.toLowerCase());
            ArrayList<String> minor = new ArrayList<>();
            minor.add("password");
            ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
            if (vv == null) {
                // Not a user
                return false;
            }
            String storeHash = new String(vv.getValue().getValue());
            return PasswordHash.validatePassword(password, storeHash); 
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            return false;
        }
    }
    public static String registerUser(final String username, final String password) {
        KVStore kvstore = KVStoreRef.getRef();
        try {           
            ArrayList<String> major = new ArrayList<>();
            major.add("user");
            kvstore.putIfAbsent(Key.createKey(major), Value.EMPTY_VALUE);
            major.add(username.toLowerCase());
            
            ValueVersion vv = kvstore.get(Key.createKey(major));
            if (vv != null){
                return "error: username already exists";
            }
            
            kvstore.putIfAbsent(Key.createKey(major), Value.EMPTY_VALUE);
            ArrayList<String> minor = new ArrayList<>();
            minor.add("password");   
                        
            String hash = PasswordHash.createHash(password);
            kvstore.put(Key.createKey(major, minor), Value.createValue(hash.getBytes()));
                        
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(User_KVStore.class.getName()).log(Level.SEVERE, null, ex);
            return "error: something failed on the server while registering user " + username;
        }
        return "user created";
    }

}
