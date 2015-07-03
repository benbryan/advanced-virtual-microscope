package avl.sv.shared.image;

import avl.sv.shared.AVM_Source;
import static java.util.Objects.hash;
import java.util.Properties;
import javax.xml.bind.DatatypeConverter;

abstract public class ImageManager extends AVM_Source{
    
    public final ImageReference imageReference;

    public ImageManager(ImageReference imageReference) {
        this.imageReference = imageReference;
    }    
              
    /**
     * Initializes image location in the database
     * @return 
     * true if image was initialized
     * false if image already exists
     */
    abstract public String initUpload( );
    
    abstract public boolean exists( );

    //TODO: check below statement about TileDirecotry
    /**
     * Sets up an image directory before posting tiles 
     * @param directoryIdx
     * tile directory index from TileDirectory
     * @param props
     * props from a TileDirecotry buffer
     * @return 
     * if error 
     *    returns string begins with "error:" followed by an error description
     * else 
     *    return "success";
     * 
     */
    abstract public String setupDirectory( final int directoryIdx, 
                                final Properties props);
    abstract public String setTile( final int directoryIdx, 
                                 final int tileX, 
                                 final int tileY, 
                                 final byte tile[]);
    abstract public String delete( );
    
    abstract public void finished();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ImageManager){
            ImageManager manager = (ImageManager) obj;
            return imageReference.equals(manager.imageReference);
        } else {
            throw new IllegalArgumentException("ImageManager compaired to non ImageManager");
        }
    }

    @Override
    public String toString() {
        return imageReference.toString();
    }
    
    
    
       
}
