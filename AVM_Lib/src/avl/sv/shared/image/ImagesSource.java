package avl.sv.shared.image;

import java.util.ArrayList;

public interface ImagesSource {
    public ImageSource createImageSource(ImageManager imageManager);
    public ArrayList<ImageManagerSet> getImageSets();
    public ArrayList<ImageManagerSet> getOwnedImages();
}
