package avl.sv.shared;

import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;

public class KVStoreRef {
    private static KVStore kvstore = null;
    public static KVStore getRef() {
        if (kvstore == null){
            String storeName = AVM_Properties.getProperty(AVM_Properties.Name.database_name);
            String hosts[] = AVM_Properties.getProperty(AVM_Properties.Name.database_hosts).split(",");
            try {
                KVStoreConfig kconfig = new KVStoreConfig(storeName, hosts );
                kvstore = KVStoreFactory.getStore(kconfig);
            } catch (Exception ex){
                StringBuilder msg = new StringBuilder("Failed to access nosql database at ");
                for (int i = 0; i < hosts.length; i++){
                    if (i>0) {
                        msg.append(" or ");
                    }
                    msg.append(hosts[i]);
                }
                
                msg.append(" with storename ").append(storeName);
                msg.append(".  Check settings in ").append(AVM_Properties.getPropertiesFile().getName());
                Logger.getLogger(KVStoreRef.class.getName()).log(Level.SEVERE, msg.toString());
                kvstore = null;
            } 
        }
        return kvstore;
    }
}
