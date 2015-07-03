package avl.sv.client;

import avl.sv.client.image.PluginImageFile;
import avl.sv.client.image.PluginImage;
import avl.sv.client.solution.PluginSolution;
import avl.sv.client.study.PluginStudy;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageManagerKVStore;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImagesSource;
import avl.sv.shared.image.ImagesSourceKVStore;
import avl.sv.shared.solution.SolutionsSource;
import avl.sv.shared.solution.SolutionsSourceKVStore;
import avl.sv.shared.study.StudiesSource;
import avl.sv.shared.study.StudiesSourceKVStore;

public class Plugins_KVStore {

    public static AVM_Plugin[] login(AVM_Session session) {               
        ImagesSource imagesSource = new ImagesSourceKVStore(session);
        PluginImage pluginImage = new PluginImage(imagesSource) {
            @Override
            public ImageManager getImageManager(ImageReference imageReference) {
                return new ImageManagerKVStore(imageReference, session);
            }
        };
        
        StudiesSource studiesSource = new StudiesSourceKVStore(session);
        
        PluginImageFile pluginImageFile = new PluginImageFile();
        PluginStudy studyPlugin = new PluginStudy(session.username, studiesSource);
        SolutionsSource solutionSource = new SolutionsSourceKVStore(session);
        PluginSolution pluginSolution = new PluginSolution(session.username, solutionSource);

        AVM_Plugin plugins[] = new AVM_Plugin[]{pluginImage, studyPlugin, pluginSolution};       
        return plugins;
    }
    
}
