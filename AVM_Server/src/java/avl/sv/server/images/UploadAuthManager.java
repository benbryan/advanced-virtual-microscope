package avl.sv.server.images;

import java.util.HashMap;

public class UploadAuthManager {
    private static UploadAuthManager instance = null;
    private final static HashMap<String,UploadAuth> uploadAuths = new HashMap<>();

    public UploadAuth getAuth(String key) {
        return uploadAuths.get(key);
    }

    public UploadAuth putAuth(String key, UploadAuth value) {
        return uploadAuths.put(key, value);
    }
    
    public UploadAuth removeAuth(String key){
        return uploadAuths.remove(key);
    }
    
    public static void purgeExpiredAuths(){
        if (uploadAuths.isEmpty()){
            return;
        }
        uploadAuths.keySet().stream().forEach((key) -> {
            UploadAuth auth = uploadAuths.get(key);
            if (auth.isExpired()) {
                uploadAuths.remove(key);
                auth.imageManager.delete();
            }
        });        
    }
    
    public static UploadAuthManager getInstance(){
        if(instance == null){
            instance = new UploadAuthManager();
        }
        return instance;
    }
    
}
