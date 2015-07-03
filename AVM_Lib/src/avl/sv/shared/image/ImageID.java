/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.sv.shared.image;

import avl.sv.shared.image.ImageSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benbryan
 */
public class ImageID {
    public static byte[] get(File f){
        byte hash[] = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            FileInputStream is = new FileInputStream(f);
            byte[] b = new byte[100000];
            is.read(b);
            hash = md5.digest(b);
        } catch (IOException | NoSuchAlgorithmException ex) {
            Logger.getLogger(ImageID.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return hash;
    }

    public static boolean hashesAreEqual(byte hash1[], byte hash2[]) {
        if (hash1.length < 2) {
            return false;
        }
        boolean match = hash1.length == hash2.length;
        if (match == false) {
            return false;
        }
        for (int i = 0; i < hash1.length; i++) {
            if (hash1[i] != hash2[i]) {
                return false;
            }
        }
        return true;
    }
    
    public static ImageSource getImageSourceForHash(ArrayList<ImageSource> imageSources, byte hash[]){
        for (ImageSource imageSource:imageSources){
            if (ImageID.hashesAreEqual(imageSource.getHash(), hash)){
                return imageSource;
            }
        }
        return null;
    }
    
}
