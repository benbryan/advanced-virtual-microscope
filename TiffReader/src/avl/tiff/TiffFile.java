/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.tiff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author benbryan
 */
public class TiffFile {
        
    public static ArrayList<TiffDirectory> getTiffDirectories(File f) throws IOException {
        if (!isTiff(f)){
            return null;
        }
        
        short tiffVersion = getTiffVersion(f);
        Boolean isBigTiff = isBigTiff(tiffVersion);
        int offsetByteSize = getOffsetByteSize(f);
        
        RandomAccessFileLittleEndian s = new RandomAccessFileLittleEndian(f, "r");

        if (isBigTiff) {
            s.seek(8);
        } else {
            s.seek(4);
        }
        
        long currentDirOffset = s.readNBytes(offsetByteSize).longValue();
        ArrayList<TiffDirectory> directories = new ArrayList<TiffDirectory>();
        do {
            s.seek(currentDirOffset);

            TiffDirectory tempDir = new TiffDirectory(f, currentDirOffset);
            directories.add(tempDir);

            // get next directory offset
            if (isBigTiff) {
                s.seek(currentDirOffset + 2 + tempDir.getNumOfDirEntries() * 20);
            } else {
                s.seek(currentDirOffset + 2 + tempDir.getNumOfDirEntries() * 12);
            }
            currentDirOffset = s.readNBytes(offsetByteSize).longValue();
        } while (currentDirOffset != 0);
        s.close();
        return directories;
    }
    
    public static Boolean isBigTiff(int tiffVersion) {
        if (tiffVersion == 0x002A) {
            return false;
        } else if (tiffVersion == 0x002B) {
            return true;
        } else { 
            throw new IllegalArgumentException("not a known tiff version");
        }
    }
    
    public static short getTiffVersion(File f) throws FileNotFoundException, IOException {
        RandomAccessFileLittleEndian s = new RandomAccessFileLittleEndian(f, "r");
        s.seek(2);
        short v = s.readShort();
        s.close();
        return v;
    }
    
    public static Boolean isTiff(File f) throws FileNotFoundException, IOException{
        RandomAccessFileLittleEndian s = new RandomAccessFileLittleEndian(f, "r");
        // Is TIFF?
        int temp = s.readShort();
        s.close();
        if ((temp == 0x4949) || (temp == 0x4D4D)) {
            return true;
        } else {
            return false;
        }
    }    
    
    public static int getOffsetByteSize(File f) throws FileNotFoundException, IOException {
        short v = getTiffVersion(f);
        if (v == 0x002A) {
            return 4;
        } else if (v == 0x002B) {
            RandomAccessFileLittleEndian s = new RandomAccessFileLittleEndian(f, "r");
            s.seek(4);
            int temp = s.readShort();
            s.close();
            return temp;
        } else{
            throw new IllegalArgumentException("not a known tiff version");
        }
    }
   
}
