package avl.sv.client.image;

import avl.sv.client.AdvancedVirtualMicroscope;
import avl.sv.shared.ProgressBarForegroundPainter;
import avl.sv.shared.AVM_ProgressMonitor;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageID;

import avl.sv.shared.image.ImageManagerSet;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageSource;
import avl.sv.shared.image.ImageSourceFile;
import avl.tiff.TiffDirectory;
import avl.tiff.TiffFile;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.xml.ws.WebServiceException;

abstract public class ImageUploaderFrame extends javax.swing.JDialog {

    private final static int NUMEL_THREADS = 5;
    AtomicInteger uploadCounter = new AtomicInteger();
    ImageManagerSet CREATE_NEW = new ImageManagerSet("Create New");
    private boolean errorsOccured;
    private JFileChooser jFileChooserImage;
    private static File lastDir = null;

    abstract public ArrayList<ImageManagerSet> getImageSets();

    abstract public ImageManager getUploader(ImageReference imageReference);

    private void jFileChooserImageActionPerformed(java.awt.event.ActionEvent evt) {
        if (!evt.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
            setVisible(false);
            return;
        }

        final File files[] = jFileChooserImage.getSelectedFiles();
        if (files.length == 0) {
            setVisible(false);
            return;
        }
        ArrayList<ImageFileRef> imgRefsIncompatable = new ArrayList<ImageFileRef>();
        ArrayList<ImageFileRef> imgRefsCompatable = new ArrayList<ImageFileRef>();

        for (File f : files) {
            ImageFileRef imgRefTemp = new ImageFileRef(f);
            if (getFileExtension(f).equalsIgnoreCase("SVS")) {
                imgRefsCompatable.add(imgRefTemp);
            } else {
                imgRefsIncompatable.add(imgRefTemp);
            }
        }

        jListImagesIncompatable.setModel(new DefaultComboBoxModel<>(imgRefsIncompatable.toArray(new ImageFileRef[imgRefsIncompatable.size()])));
        jListImagesCompatable.setModel(new DefaultComboBoxModel<>(imgRefsCompatable.toArray(new ImageFileRef[imgRefsCompatable.size()])));
        jListImagesUploaded.setModel(new DefaultComboBoxModel<>());

//        jListImagesChosen.setCellRenderer(new ImageListCellRenderer());
    }

    public ImageUploaderFrame(java.awt.Frame parent) {
        super(parent, false);
        initComponents();
        jFileChooserImage = new JFileChooser();
        jFileChooserImage.setFileFilter(new FileFilter() {
            String validExt[] = {"bmp", "gif", "png", "jpg", "jpeg", "svs"};

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String ext = getFileExtension(f);
                for (int i = 0; i < validExt.length; i++) {
                    if (ext.equalsIgnoreCase(validExt[i])) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "*.bmp;*.gif;*.png;*.jpg;*.jpeg;*.svs;";
            }
        });

        jFileChooserImage.setMultiSelectionEnabled(true);
        jFileChooserImage.setApproveButtonText("Upload");
        jFileChooserImage.addActionListener((java.awt.event.ActionEvent evt) -> {
            jFileChooserImageActionPerformed(evt);
        });
    }

    private class ImageFileRef {

        final File original;
        File converted = null;

        public ImageFileRef(File original) {
            this.original = original;
        }

        @Override
        public String toString() {
            return original.getName();
        }
        
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            ArrayList<ImageManagerSet> imageSets = getImageSets();
            jComboBoxImageSets.removeAllItems();
            for (ImageManagerSet imageSet : imageSets) {
                jComboBoxImageSets.addItem(imageSet);
            }
            if (jComboBoxImageSets.getComponents().length == 0) {
                jComboBoxImageSets.addItem(new ImageManagerSet("default"));
            }
            jComboBoxImageSets.addItem(CREATE_NEW);

            jListImagesIncompatable.setModel(new DefaultListModel<>());
            EventQueue.invokeLater(() -> {
                if (lastDir != null) {
                    jFileChooserImage.setCurrentDirectory(lastDir);
                }
                jFileChooserImage.showDialog(getThis(), "Select");
                lastDir = jFileChooserImage.getCurrentDirectory();
            });
        } else {
            for (int i = 0; i < jListImagesIncompatable.getModel().getSize(); i++) {
                final ImageFileRef imageFileRef = jListImagesIncompatable.getModel().getElementAt(i);
                if (imageFileRef.converted != null) {
                    imageFileRef.converted.delete();
                }
            }
        }
        super.setVisible(b);
    }

    private Dialog getThis() {
        return this;
    }

    public boolean uploadImageFile(AVM_ProgressMonitor pm, File toUpload, String imageSetName, String imageName) {

        String result = uploadSVS(pm, imageSetName, imageName, toUpload);
        if (result.startsWith("error:")) {
            final String msg = result + "\n" + "while uploading image " + imageName + " in imageSet " + imageSetName;
            errorsOccured = true;
            AdvancedVirtualMicroscope.setStatusText(msg, 5000);
        }

        return pm.isCanceled();
    }

    public String uploadSVS(AVM_ProgressMonitor pm, String imageSetName, String imageName, File file) {
        String result;
        ImageReference imageReference = new ImageReference(imageSetName, imageName, ImageID.get(file));
        ImageManager imageUpload = getUploader(imageReference);
        result = imageUpload.initUpload();
        if ((result == null)) {
            return "Failed to initialize upload";
        }
        if (result.startsWith("error:")) {
            return result;
        }
        try {
            final ArrayList<TiffDirectory> tds = TiffFile.getTiffDirectories(file);
            for (int dirIdx = 0; dirIdx < tds.size(); dirIdx++) {
                pm.setNote(file.getName() + " Directory " + String.valueOf(dirIdx + 1) + " of " + String.valueOf(tds.size()));
                pm.setProgress(0);
                final TiffDirectory dir = tds.get(dirIdx);

                Properties props = dir.getProperties();
                result = imageUpload.setupDirectory(dirIdx, props);
                if (result.startsWith("error")) {
                    imageUpload.delete();
                    return result;
                }

                pm.setMaximum((int) ((dir.getTilesAcrossW() * dir.getTilesDownL()) + 1));

                ArrayList<UploadElement> toUpload = new ArrayList<>();
                for (int x = 0; x < dir.getTilesAcrossW(); x++) {
                    for (int y = 0; y < dir.getTilesDownL(); y++) {
                        toUpload.add(new UploadElement(imageUpload, pm, dir, dirIdx, x, y));
                    }
                }
                try {
                    ExecutorService pool = Executors.newFixedThreadPool(NUMEL_THREADS);
                    uploadCounter.set(0);
                    List<Future<Boolean>> resultPool = pool.invokeAll(toUpload);
                    pool.shutdown();
                    boolean executionFinished = false;
                    for (int iter = 0; iter < 3; iter++) {
                        executionFinished = pool.awaitTermination(1, TimeUnit.SECONDS);
                        if (pm.isCanceled()) {
                            imageUpload.delete();
                            return "upload cancelled";
                        }
                        if (executionFinished) {
                            ArrayList<UploadElement> toUploadAgain = new ArrayList<>();
                            for (int i = 0; i < resultPool.size(); i++) {
                                boolean uploaded = resultPool.get(i).get();
                                if (!uploaded) {
                                    toUploadAgain.add(toUpload.get(i));
                                }
                            }
                            if (toUploadAgain.isEmpty()) {
                                break;
                            }
                            toUpload = toUploadAgain;
                            pool = Executors.newFixedThreadPool(NUMEL_THREADS);
                            resultPool = pool.invokeAll(toUpload);
                            pool.shutdown();
                        }
                    }
                    if (!executionFinished) {
                        imageUpload.delete();
                        return "error: processes timeout during upload";
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    imageUpload.delete();
                    Logger.getLogger(ImageUploaderFrame.class.getName()).log(Level.SEVERE, null, ex);
                    return "error: somthing failed during upload";
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ImageUploaderFrame.class.getName()).log(Level.SEVERE, null, ex);
            imageUpload.delete();
            return "error: somthing failed during upload";
        }
        imageUpload.finished();
        return "image uploaded";
    }

    private class UploadElement implements Callable<Boolean> {

        private TiffDirectory dir;
        private int dirIdx, x, y;
        AVM_ProgressMonitor pm;
        final ImageManager imageUpload;

        public UploadElement(ImageManager imageUpload, AVM_ProgressMonitor pm, TiffDirectory dir, int dirIdx, int x, int y) {
            this.imageUpload = imageUpload;
            this.pm = pm;
            this.dir = dir;
            this.dirIdx = dirIdx;
            this.x = x;
            this.y = y;
        }

        @Override
        public Boolean call() throws Exception {
            //return true if successfull
            try {
                byte img[] = dir.getTileAsByteArray(x, y);
                String result = imageUpload.setTile(dirIdx, x, y, img);
                if (result.startsWith("error")) {
                    System.err.println(result);
                    return false;
                }
            } catch (WebServiceException | IOException ex) {
                return false;
            }
            int progress = uploadCounter.incrementAndGet();
            pm.setProgress(progress);
            return true;
        }
    }

    private static String getFileExtension(File file) {
        String extension = "";
        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            extension = file.getName().substring(i + 1);
        }
        return extension;
    }

//    public static void main(final String args[]) {
//        File input = new File("C:\\Users\\benbryan\\Desktop\\New folder\\input.jpg");
//        File output = convertToSVS_StyleTiff(input, 0.95f);
//        output = null;
//    }
    private File convertToSVS_StyleTiff(File originalImg, float jpegQuality) {
        FileOutputStream fos;
        File tempImg;
        UIManager.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarForegroundPainter());
        AVM_ProgressMonitor pm = new AVM_ProgressMonitor(this, "Converting image", "", 0, 100);
        try {
            tempImg = File.createTempFile(originalImg.getName() + "converted", ".tif");
            fos = new FileOutputStream(tempImg);
            int tileSize = 256;
            pm.setNote("Reading original image");
            pm.setProgress(5);
            BufferedImage originalImage = ImageIO.read(originalImg);
            if (originalImage == null) {
                return null;
            }
            TiffWriter.write(pm, originalImage, fos, tileSize, jpegQuality);
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(ImageUploaderFrame.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        pm.close();
        return tempImg;
    }

    private void checkJPEG_Quality() {
        int v = 0;
        try {
            v = Integer.parseInt(jTextFieldJPEG_Quality.getText());
        } catch (Exception ex) {

        }
        if (v <= 0) {
            v = 75;
        } else if (v > 100) {
            v = 100;
        }
        jTextFieldJPEG_Quality.setText(String.valueOf(v));
    }

    private Frame getParentFrame() {
        Object obj = SwingUtilities.getWindowAncestor(this);
        if (obj instanceof Frame) {
            return (Frame) obj;
        } else {
            return null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextFieldJPEG_Quality = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxImageSets = new javax.swing.JComboBox<avl.sv.shared.image.ImageManagerSet>();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListImagesIncompatable = new javax.swing.JList<ImageFileRef>();
        jLabel2 = new javax.swing.JLabel();
        jButtonStartUpload = new javax.swing.JButton();
        jButtonConvertImages = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListImagesUploaded = new javax.swing.JList<ImageFileRef>();
        jScrollPane4 = new javax.swing.JScrollPane();
        jListImagesCompatable = new javax.swing.JList<ImageFileRef>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setTitle("Image Uploader");

        jTextFieldJPEG_Quality.setText("75");
        jTextFieldJPEG_Quality.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldJPEG_QualityFocusLost(evt);
            }
        });
        jTextFieldJPEG_Quality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldJPEG_QualityActionPerformed(evt);
            }
        });

        jLabel5.setText("Conversion JPEG Quality");

        jComboBoxImageSets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxImageSetsActionPerformed(evt);
            }
        });

        jLabel1.setText("Target Image Set");

        jListImagesIncompatable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListImagesIncompatableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jListImagesIncompatable);

        jLabel2.setText("Images needing conversion");

        jButtonStartUpload.setText("<html><font size=\"5\">-></font><br><br>Upload<br><br><font size=\"5\">-></font></html>");
        jButtonStartUpload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartUploadActionPerformed(evt);
            }
        });

        jButtonConvertImages.setText("<html><font size=\"5\">-></font><br><br>Convert<br><br><font size=\"5\">-></font></html>");
        jButtonConvertImages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConvertImagesActionPerformed(evt);
            }
        });

        jListImagesUploaded.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListImagesUploadedMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jListImagesUploaded);

        jListImagesCompatable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListImagesCompatableMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(jListImagesCompatable);

        jLabel3.setText("Images ready for upload");

        jLabel4.setText("Images Uploaded");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonConvertImages, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBoxImageSets, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(87, 87, 87)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonStartUpload, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(6, 6, 6))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldJPEG_Quality, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxImageSets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldJPEG_Quality, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jLabel3))
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonConvertImages, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonStartUpload, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldJPEG_QualityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldJPEG_QualityActionPerformed
        checkJPEG_Quality();
    }//GEN-LAST:event_jTextFieldJPEG_QualityActionPerformed

    private void jTextFieldJPEG_QualityFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldJPEG_QualityFocusLost
        checkJPEG_Quality();
    }//GEN-LAST:event_jTextFieldJPEG_QualityFocusLost

    class ImageListCellRenderer extends JLabel implements ListCellRenderer<ImageFileRef> {

        public ImageListCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ImageFileRef> list, ImageFileRef imgFileRef, int index, boolean isSelected, boolean cellHasFocus) {

            setText(imgFileRef.original.getName());

            Color background;
            Color foreground;

            if (isSelected) {
                background = Color.BLUE;
                foreground = Color.WHITE;
            } else if (true) {
                background = Color.GREEN;
                foreground = Color.WHITE;
            } else {
                background = Color.RED;
                foreground = Color.WHITE;
            }

            setBackground(background);
            setForeground(foreground);

            return this;
        }

    }

    String newImageSetName = "";
    private void jComboBoxImageSetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxImageSetsActionPerformed
        if ((jComboBoxImageSets.getSelectedItem() != null) && (jComboBoxImageSets.getSelectedItem().equals(CREATE_NEW))) {
            String temp = JOptionPane.showInputDialog(this, "Enter New Image Set Name: ", newImageSetName);
            if (temp == null) {
                jComboBoxImageSets.setSelectedIndex(0);
                return;
            }
            newImageSetName = temp;
//            for (byte b:newImageSetName.getBytes()){
//                if ((b < 48) || (b>122) || ((b>57)&&(b<65)) || ((b>90)&&(b<97)) ){
//                    JOptionPane.showMessageDialog(this, "Invalid Name\nName must contain alphanumeric characters only");
//                    jComboBoxImageSets.setSelectedIndex(0);
//                    return;
//                }
//            }
            ImageManagerSet newItem = new ImageManagerSet(newImageSetName);
            jComboBoxImageSets.addItem(newItem);
            jComboBoxImageSets.setSelectedItem(newItem);
        }

    }//GEN-LAST:event_jComboBoxImageSetsActionPerformed

    private void jButtonStartUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartUploadActionPerformed
        final Frame superFrame = getParentFrame();
//        if (jListImagesChosen.getSelectedValuesList().isEmpty()) {
//            JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select images for upload first", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
        Executors.newSingleThreadExecutor().submit(() -> {

            errorsOccured = false;
            final long startTime = new Date().getTime();
            final ImageManagerSet imageSet = ((ImageManagerSet) jComboBoxImageSets.getSelectedItem());
            UIManager.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarForegroundPainter());
            AVM_ProgressMonitor pm = new AVM_ProgressMonitor(superFrame, "Uploading Image(s)", "Starting...", 0, 100);
            pm.setMillisToDecideToPopup(100);
            pm.setMillisToPopup(500);
            int i = 0;
            while (i < jListImagesCompatable.getModel().getSize()) {
                try {
                    ImageFileRef imageFileRef = jListImagesCompatable.getModel().getElementAt(i);
                    boolean cancelled;
                    if (imageFileRef.converted != null) {
                        cancelled = uploadImageFile(pm, imageFileRef.converted, imageSet.getName(), imageFileRef.original.getName());
                    } else {
                        cancelled = uploadImageFile(pm, imageFileRef.original, imageSet.getName(), imageFileRef.original.getName());
                    }
                    if (cancelled) {
                        break;
                    }
                    transfer(jListImagesCompatable, jListImagesUploaded, imageFileRef);
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                } finally {
                    jListImagesCompatable.repaint();
                    jListImagesUploaded.repaint();
                }
            }
            
            pm.close();
            if ((new Date().getTime() - startTime) > 2000) {
                Toolkit.getDefaultToolkit().beep();
            }
            if (errorsOccured) {
                int result = JOptionPane.showConfirmDialog(superFrame, "Problems occurred during upload\nOpen log?", "Error", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    AdvancedVirtualMicroscope.showLogWindow();
                }
            }
            errorsOccured = false;

        });
    }//GEN-LAST:event_jButtonStartUploadActionPerformed

    private void jButtonConvertImagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConvertImagesActionPerformed
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                final long startTime = new Date().getTime();
                UIManager.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarForegroundPainter());
                AVM_ProgressMonitor pm = new AVM_ProgressMonitor(getThis(), "Converting images", "", 0, 100);
                int i = 0;
                while (i < jListImagesIncompatable.getModel().getSize()) {
                    try {
                        final ImageFileRef imageFileRef = jListImagesIncompatable.getModel().getElementAt(i);
                        pm.setMaximum(10);
                        pm.setMaximum(2);
                        pm.setNote("Converting image...");
                        File tempImg = convertToSVS_StyleTiff(imageFileRef.original, Float.valueOf(jTextFieldJPEG_Quality.getText()) / 100);
                        if (tempImg == null) {
                            AdvancedVirtualMicroscope.setStatusText("Error: Failed to convert image " + imageFileRef.original.toString() + "to server required format", 5000);
                            return;
                        }
                        imageFileRef.converted = tempImg;
                        transfer(jListImagesIncompatable, jListImagesCompatable, imageFileRef);
                    } catch (Exception ex) {
                        i++;
                        Logger.getLogger(ImageUploaderFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    } finally {
                        jListImagesIncompatable.repaint();
                        jListImagesCompatable.repaint();
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
    }//GEN-LAST:event_jButtonConvertImagesActionPerformed

    private void transfer(JList<ImageFileRef> src, JList<ImageFileRef> dest, ImageFileRef target) {
        DefaultListModel<ImageFileRef> srcModelNew = new DefaultListModel<>();
        DefaultListModel<ImageFileRef> destModelNew = new DefaultListModel<>();
        for (int i = 0; i < src.getModel().getSize(); i++) {
            ImageFileRef temp = (ImageFileRef) src.getModel().getElementAt(i);
            if (temp.equals(target)) {
                destModelNew.addElement(temp);
            } else {
                srcModelNew.addElement(temp);
            }
        }
        for (int i = 0; i < dest.getModel().getSize(); i++) {
            ImageFileRef temp = (ImageFileRef) dest.getModel().getElementAt(i);
            destModelNew.addElement(temp);
        }
        src.setModel(srcModelNew);
        dest.setModel(destModelNew);
    }

    private void jListImagesIncompatableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListImagesIncompatableMouseClicked

    }//GEN-LAST:event_jListImagesIncompatableMouseClicked

    private void jListImagesCompatableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListImagesCompatableMouseClicked
        if (evt.getClickCount() != 2) {
            return;
        }
        ImageFileRef imageFileRef = jListImagesCompatable.getSelectedValue();
        openImage(imageFileRef);
    }//GEN-LAST:event_jListImagesCompatableMouseClicked

    private void jListImagesUploadedMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListImagesUploadedMouseClicked
        if (evt.getClickCount() != 2) {
            return;
        }
        ImageFileRef imageFileRef = jListImagesUploaded.getSelectedValue();
        openImage(imageFileRef);
    }//GEN-LAST:event_jListImagesUploadedMouseClicked

    private void openImage(ImageFileRef imageFileRef){
        try {
            if (imageFileRef == null) {
                return;
            }
            ImageSource imageSource;
            if (imageFileRef.converted != null) {
                imageSource = new ImageSourceFile(imageFileRef.converted);
            } else {
                imageSource = new ImageSourceFile(imageFileRef.original);
            }
            AdvancedVirtualMicroscope.addImageViewer(imageSource);
        } catch (IOException ex) {
            Logger.getLogger(AdvancedVirtualMicroscope.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConvertImages;
    private javax.swing.JButton jButtonStartUpload;
    private javax.swing.JComboBox<avl.sv.shared.image.ImageManagerSet> jComboBoxImageSets;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JList<ImageFileRef> jListImagesCompatable;
    private javax.swing.JList<ImageFileRef> jListImagesIncompatable;
    private javax.swing.JList<ImageFileRef> jListImagesUploaded;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jTextFieldJPEG_Quality;
    // End of variables declaration//GEN-END:variables
}
