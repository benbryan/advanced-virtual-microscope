/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.tiff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author benbryan
 */
public class RandomAccessFileLittleEndian  {
    RandomAccessFile ranf;

    RandomAccessFileLittleEndian(File f, String r) throws FileNotFoundException {
        ranf = new RandomAccessFile(f,r);
    }
    

    short readShort() throws IOException {
        byte[] b = new byte[2];
        ranf.read(b);
        short out = (short) (b[0] & 0xFF | (b[1] & 0xFF) << 8);
        return out;
    }
    
    private byte[] bigToLittleEndian(byte in[]){
        byte[] out = new byte[in.length];
        for (int i = 0; i < in.length; i++){
            out[out.length-1-i] = in[i];
        }
        return out;
    }
            

    void close() throws IOException {
        ranf.close();
    }

    void seek(long i) throws IOException {
        ranf.seek(i);
    }

    byte[] readLittleEndian(byte[] in) throws IOException {
        ranf.read(in);
        return bigToLittleEndian(in);
    }
    byte[] readBigEndian(byte[] in) throws IOException {
        ranf.read(in);
        return in;
    }
    
    Number readNBytes(int nBytes) throws IOException {
        byte b[] = new byte[nBytes];
        ranf.read(b);
        
        long out = 0;
        for (int i = 0; i < nBytes; i++) {
            out |= ((b[i] & 0xFF) << (8 * i));
        }
        
        return out;
    }

   
    
}
