package avl.sv.client.image;

import avl.sv.client.AVM_Plugin;
import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.client.SearchableSelector;
import avl.sv.shared.AVM_Source;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageManagerSet;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageSource;
import avl.sv.shared.image.ImagesSource;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

abstract public class PluginImage implements AVM_Plugin{
    final JMenu jMenuImage = new JMenu("Image");
    ArrayList<Window> windows = new ArrayList<>();
    public PluginImage(ImagesSource imagesSource) {
        jMenuImage.add(new JMenuItem(new AbstractAction("Open") {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Window window : windows) {
                    if (window instanceof SearchableSelector) {
                        window.toFront();
                        window.requestFocus();
                        window.setVisible(true);
                        return;
                    }
                }

                SearchableSelector frame = new SearchableSelector("Select Image", "Open") {
                    private void openImages(ArrayList<AVM_Source> selected) {
                        ArrayList<ImageManager> imageManagers = new ArrayList<>();
                        selected.stream().filter((node) -> (node instanceof ImageManager)).forEach((node) -> {
                            imageManagers.add((ImageManager) node);
                        });
                        if (imageManagers.size() > 5) {
                            String options[] = new String[]{"Open", "Cancel"};
                            Object result = JOptionPane.showInputDialog(this, "Really open " + String.valueOf(imageManagers.size()) + " images", "Warning", JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                            if (result.equals(options[1])) {
                                return;
                            }
                        }
                        imageManagers.stream().map((imageReference) -> imagesSource.createImageSource(imageReference)).forEach((imageSource) -> {
                            AdvancedVirtualMicroscope.addImageViewer((ImageSource) imageSource);
                        });
                    }

                    @Override
                    public void doubleClicked(ArrayList<AVM_Source> selected) {
                        openImages(selected);
                    }

                    @Override
                    public void buttonPressed(ArrayList<AVM_Source> selected) {
                        openImages(selected);
                    }

                    @Override
                    public ArrayList<AVM_Source> getSelectables() {
                        ArrayList<AVM_Source> nodes = new ArrayList<>();
                        nodes.addAll(imagesSource.getImageSets());
                        return nodes;
                    }

//                    @Override
//                    public ImageSource createImageSource(ImageReference imageReference) {
//                        return imagesSource.createImageSource(imageReference);
//                    }
//
//                    @Override
//                    public ImageManager getImageManager(ImageReference imageReference) {
//                        return imagesSource.getImageManager(imageReference);
//                    }
                };

                frame.setVisible(true);
                AdvancedVirtualMicroscope.addWindow(frame);
                windows.add(frame);
            }
        }));
        jMenuImage.add(new JMenuItem(new AbstractAction("Upload") {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Window window : windows) {
                    if (window instanceof ImageUploaderFrame) {
                        window.toFront();
                        window.requestFocus();
                        window.setVisible(true);
                        return;
                    }
                }
                ImageUploaderFrame uploadImageFrame = new ImageUploaderFrame(AdvancedVirtualMicroscope.getInstance()) {
                    @Override
                    public ArrayList<ImageManagerSet> getImageSets() {
                        return imagesSource.getImageSets();
                    }

                    @Override
                    public ImageManager getUploader(ImageReference imageReference) {
                        return getImageManager(imageReference);
                    }
                };
                uploadImageFrame.setVisible(true);
                AdvancedVirtualMicroscope.addWindow(uploadImageFrame);
                windows.add(uploadImageFrame);

            }
        }));
        jMenuImage.add(new JMenuItem(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                final SearchableSelector imageSelector = new SearchableSelector("Delete Image(s)", "Delete") {
                    public void deleteImage(ImageManager imageManager) {
                        AdvancedVirtualMicroscope.closeImageViewers(imageManager.imageReference);
                        String result = imageManager.delete();
                        if (result.startsWith("error:")) {
                            AdvancedVirtualMicroscope.setStatusText("Failed to delete " + imageManager.imageReference.imageName + " from image set " + imageManager.imageReference.imageSetName + " with " + result, 3000);
                        } else {
                            removeNode(imageManager);
                            AdvancedVirtualMicroscope.setStatusText(result, 1000 * 4);
                        }
                    }

                    @Override
                    public void buttonPressed(ArrayList<AVM_Source> selected) {
                        for (DefaultMutableTreeNode s : selected) {
                            if (s instanceof ImageManagerSet) {
                                ImageManagerSet imageSet = (ImageManagerSet) s;
                                for (ImageManager imageManager : imageSet.getImageManagerSet()) {
                                    deleteImage(imageManager);
                                }
                                removeNode(imageSet);
                            }
                            if (s instanceof ImageManager) {
                                ImageManager imageManager = (ImageManager) s;
                                deleteImage(imageManager);
                            }
                        }
                    }

                    @Override
                    public void doubleClicked(ArrayList<AVM_Source> selected) {
                        openImages(selected);
                    }
                    private void openImages(ArrayList<AVM_Source> selected) {
                        ArrayList<ImageManager> imageManagers = new ArrayList<>();
                        selected.stream().filter((node) -> (node instanceof ImageManager)).forEach((node) -> {
                            imageManagers.add((ImageManager) node);
                        });
                        if (imageManagers.size() > 5) {
                            String options[] = new String[]{"Open", "Cancel"};
                            Object result = JOptionPane.showInputDialog(this, "Really open " + String.valueOf(imageManagers.size()) + " images", "Warning", JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                            if (result.equals(options[1])) {
                                return;
                            }
                        }
                        imageManagers.stream().map((imageReference) -> imagesSource.createImageSource(imageReference)).forEach((imageSource) -> {
                            AdvancedVirtualMicroscope.addImageViewer((ImageSource) imageSource);
                        });
                    }
                    
                    @Override
                    public ArrayList<AVM_Source> getSelectables() {
                        ArrayList<AVM_Source> selectables = new ArrayList<>();
                        ArrayList<ImageManagerSet> owned = imagesSource.getOwnedImages();
                        selectables.addAll(owned);
                        return selectables;
                    }
                };
                imageSelector.setVisible(true);
            }

        }));
//        jMenuImage.add(new JSeparator());        
    }

    @Override
    public JMenu getMenu() {
        return jMenuImage;
    }
    
    abstract public ImageManager getImageManager(ImageReference imageReference);
}
