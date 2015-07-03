package avl.sv.shared.solution;

import avl.sv.shared.KVStoreRef;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;

public class FeatureGeneratorTaskManager {
    
    private static FeatureGeneratorTaskManager instance;
    private static ExecutorService executors;
    private static Future<?> future;
    private static AtomicBoolean isBusy = new AtomicBoolean(false);
    public static AtomicBoolean onHold = new AtomicBoolean(false);

    public static boolean isBusy() {
        return isBusy.get();
    }
    
    private FeatureGeneratorTaskManager() {

    }

    public static FeatureGeneratorTaskManager getInstance(){
        if(instance == null){
            instance = new FeatureGeneratorTaskManager();
            instance.start();
        }
        return instance;
    }    
    
    public void addTaskToQue(FeatureGenerateTask newTask) {
        String newTaskXML = newTask.toXML();
        KVStore kv = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        major.add("FeatureGeneratorTasks");
        kv.putIfAbsent(Key.createKey(major), Value.createValue(String.valueOf(0).getBytes()));
        ValueVersion vv = kv.get(Key.createKey(major));
        int idx = 0;
        try {
            idx = Integer.parseInt(new String(vv.getValue().getValue()));
        } catch (Exception ex) {
            kv.put(Key.createKey(major), Value.createValue(String.valueOf(idx).getBytes()));
            
        }
        idx++;
        Version v = null;
        for (int i = 0; i < 100; i++){
            v = kv.putIfVersion(Key.createKey(major), Value.createValue(String.valueOf(idx).getBytes()), vv.getVersion());
            if (v != null){
                break;
            }
            idx++;
        }
        ArrayList<String> minor = new ArrayList<>();
        minor.add(String.valueOf(idx));
        if (v != null){
            kv.put(Key.createKey(major, minor), Value.createValue(newTaskXML.getBytes()));
        }        
        start();
    }

    private FeatureGenerateTask getNext(){
        KVStore kv = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        major.add("FeatureGeneratorTasks");
        Iterator<KeyValueVersion> iter = kv.multiGetIterator(Direction.FORWARD, 1, Key.createKey(major), null, Depth.CHILDREN_ONLY);
        FeatureGenerateTask task = null;
        while (iter.hasNext()){
            KeyValueVersion kvv = iter.next();
            boolean deleted = kv.delete(kvv.getKey());
            if (deleted){
                try {
                    FeatureGenerateTask taskTemp = FeatureGenerateTask.parse(new String(kvv.getValue().getValue()));
                    for (String name:taskTemp.featureNames){
                        double[][] feature = SolutionSourceKVStore.getFeature(taskTemp.imageReference, taskTemp.featureGeneratorClassName, name, taskTemp.tileDim, taskTemp.tileWindowDim );
                        if (feature != null){
                            taskTemp.featureNames.remove(name);
                        }
                    }
                    if (taskTemp.featureNames.isEmpty()){
                        continue;
                    }
                    task = taskTemp;
                    break;
                } catch (Exception ex){
                    Logger.getLogger(FeatureGeneratorTaskManager.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }
        }        
        return task;
    }
    
    public void start() {
        if (onHold.get()){
            return;
        }
        if ((executors == null) || executors.isShutdown() || executors.isTerminated()) {
            executors = Executors.newCachedThreadPool();
        }
        if ((future == null) || future.isDone()) {
            FeatureGenerateTask next = getNext();
            if (next != null) {
                isBusy.set(true);
                future = executors.submit(next);
                executors.submit(() -> {
                    try {
                        future.get(30, TimeUnit.MINUTES);
                    } catch (Throwable ex) {
                        executors.shutdownNow();
                        executors = Executors.newSingleThreadExecutor();
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                    start();
                });
            } else {
                isBusy.set(false);
            }
        }
    }


}
