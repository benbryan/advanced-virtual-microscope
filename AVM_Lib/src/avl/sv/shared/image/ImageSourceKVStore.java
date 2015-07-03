package avl.sv.shared.image;

import avl.sv.shared.KVStoreRef;
import avl.tiff.TiffDirectoryBuffer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.ValueVersion;

public class ImageSourceKVStore extends ImageSource {

    KVStore kvstore = KVStoreRef.getRef();
    
    private static ArrayList<String> getMajorPath(ImageReference imageReference){
        ArrayList<String> major = new ArrayList<>();
        major.add("image");
        major.add(imageReference.imageSetName);
        major.add(imageReference.imageName);
        return major;
    }
    
    public ImageSourceKVStore(ImageReference imageReference) {
        super(imageReference);
        gatherDirs();
    }
    
    private void gatherDirs() {
        ArrayList<Properties> ps = getProperties(); 
        ArrayList<TiffDirectoryBuffer> directoryBuffers = new ArrayList<>();
        for (int dirIdx = 0; dirIdx < ps.size(); dirIdx++) {
            TiffDirectoryBuffer tdb = new TiffDirectoryBuffer(ps.get(dirIdx));
            byte tables[] = getJpegTables(dirIdx);
            if (tables != null) {
                try {
                    tdb.setupDecoder(tables);
                } catch (IOException ex) {
                    Logger.getLogger(ImageSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            directoryBuffers.add(tdb);
        }
        setTiffDirectoryBuffers(directoryBuffers);
        parseDBuff();
    }

    @Override
    protected void downloadTile(TiffDirectoryBuffer dir, int i, int j) {
        byte b[];
        int dirIdx = getTiffDirectoryBufferIndexOf(dir);
        b = getTileAsByteArray(imageReference, dirIdx, i, j);
        if (b != null) {
            dir.setTile(i, j, b);
        }
        decrementAndGetActiveDownloadCount();
        startBuffering();
    }
    
    public byte[] getJpegTables(int directoryIndex) {
        ArrayList<String> minor = new ArrayList<>();
        minor.add(String.valueOf(directoryIndex));
        minor.add(String.valueOf("properties"));
        minor.add(String.valueOf("JpegTables"));
        ValueVersion vv = kvstore.get(Key.createKey(getMajorPath(imageReference), minor));
        if ((vv == null) || (vv.getValue()) == null || (vv.getValue().getValue() == null)){
            return null;
        }
        return vv.getValue().getValue();
    }
        
    public static byte[] getTileAsByteArray(ImageReference imageReference, int directoryIndex, int tileX, int tileY) {
        ArrayList<String> minor = new ArrayList<>();
        minor.add(String.valueOf(directoryIndex));
        minor.add(String.valueOf("tiles"));
        minor.add(String.valueOf(tileX));
        minor.add(String.valueOf(tileY));
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(getMajorPath(imageReference), minor));
        if (vv == null){
            return null;
        }
        return vv.getValue().getValue();
    }

    ArrayList<Properties> getProperties() {
        Iterator<KeyValueVersion> iter = kvstore.multiGetIterator(Direction.FORWARD, 0, Key.createKey(getMajorPath(imageReference)), null, Depth.CHILDREN_ONLY);
        ArrayList<Properties> out = new ArrayList<>();
        while(iter.hasNext()){
            KeyValueVersion n = iter.next();
            ArrayList<String> minor = new ArrayList<>();
            try {
                Integer.parseInt(n.getKey().getMinorPath().get(0));
            } catch (NumberFormatException ex){
                continue;
            }
            minor.addAll(n.getKey().getMinorPath());
            minor.add("properties");
            minor.add("dummy");  // this is just here to allocate space
            Properties p = new Properties();
            subFun(minor, p, "ImageWidth");
            subFun(minor, p, "ImageLength");
            subFun(minor, p, "Compression");
            subFun(minor, p, "TileLength");
            subFun(minor, p, "TileWidth");
            subFun(minor, p, "TilesAcrossW");
            subFun(minor, p, "TilesDownL");
            subFun(minor, p, "Photometric");
            subFun(minor, p, "ZoomLevel");
            out.add(p);
        }
        return out;
    }
    
    private void subFun(ArrayList<String> minor, Properties p, String target){
        minor.set(minor.size()-1, target);
        ValueVersion vv = kvstore.get(Key.createKey(getMajorPath(imageReference), minor));
        if ((vv != null) && (vv.getValue() != null) && vv.getValue().getValue() != null){
            p.put(target, new String(vv.getValue().getValue()));
        }
    }
    
    public String[] getPropertiesAsStrings() {
        try {
            ArrayList<Properties> ps = getProperties();
            String out[] = new String[ps.size()];
            for (int i = 0; i < ps.size(); i++) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ps.get(i).storeToXML(bos, "Directory " + i + " Information");
                out[i] = bos.toString();
            }
            return out;
        } catch (IOException ex) {
            Logger.getLogger(ImageSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
