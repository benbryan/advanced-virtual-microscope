/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.sv.client.solution;

import avl.sv.client.image.ImageViewer;
import avl.sv.client.image.ImageViewerPlugin;
import avl.sv.client.image.ImageViewerPluginListener;
import avl.sv.shared.solution.Sample;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class ClassifierResultsDistribution extends javax.swing.JFrame implements ImageViewerPlugin {

    final ArrayList<Sample> samples;
    final String names[];
    final ImageViewer imageViewer;
    private final DefaultTableModel model;
    BufferedImage overlayImage = null;
    int lowerX, lowerY, upperX, upperY;
    int classIdxSelected;
    private float alpha;

    public ClassifierResultsDistribution(String classifierName, ImageViewer imageViewer, ArrayList<Sample> samples, String names[], String title) {
        initComponents();
        this.samples = samples;
        this.names = names;
        this.imageViewer = imageViewer;
        setTitle(title + ", " + classifierName + " Results");

        model = (DefaultTableModel) new DefaultTableModel() {
            Class columnClasses[] = new Class[]{String.class, ImageIcon.class, Boolean.class};
            String columnNames[] = new String[]{"Name", "Color", "Visible"};

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnClasses[columnIndex]; //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int getColumnCount() {
                return columnClasses.length;
            }

            @Override
            public String getColumnName(int column) {
                return columnNames[column]; //To change body of generated methods, choose Tools | Templates.
            }

        };

        for (int i = 0; i < names.length; i++) {
            BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
            Graphics g = img.getGraphics();
            g.setColor(ColormapJet.getColor(i, names.length));
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
            model.addRow(new Object[]{names[i], new ImageIcon(img), false});
        }
        classIdxSelected = 0;
        model.setValueAt(true, classIdxSelected, 2);
        jTableClassifierResults.setModel(model);

        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                model.removeTableModelListener(this);
                for (int i = 0; i < model.getRowCount(); i++) {
                    model.setValueAt(false, i, 2);
                }
                classIdxSelected = e.getFirstRow();
                model.setValueAt(true, classIdxSelected, 2);
                model.addTableModelListener(this);
                overlayImage = null;
                imageViewer.repaint();
            }
        });
        jSliderOccupacity.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                try {
                    updateAlpha();
                    if (overlayImage != null) {
                        int alpha = jSliderOccupacity.getValue();
                        DataBuffer buff = overlayImage.getRaster().getDataBuffer();
                        for (int i = 0; i < buff.getSize(); i += 4) {
                            if (buff.getElem(i) > 0) {
                                buff.setElem(i, alpha);
                            }
                        }
                    }
                    imageViewer.repaint();
                } catch (Exception ex) {
                }
            }
        });
        setVisible(true);
        updateAlpha();
    }

    private void updateAlpha() {
        alpha = (float) jSliderOccupacity.getValue() / (float) jSliderOccupacity.getMaximum();
    }

    @Override
    public void paintPlugin(ImageViewer imageViewer, Graphics gOrig) {
        if ((samples == null)) {
            return;
        }
        Graphics2D g = (Graphics2D) gOrig.create();
        imageViewer.concatenateImageToDisplayTransform(g);

        if (samples.size() > 1000) {
            if (overlayImage == null) {
                createImageOverlay();
            }
            g.drawImage(overlayImage, lowerX, lowerY, upperX - lowerX, upperY - lowerY, null);
        } else {
            drawSamplesDirect(g);
        }

        g.setColor(Color.black);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

        Point labelPoint = new Point(upperX, upperY);
        g.getTransform().transform(labelPoint, labelPoint);
        gOrig.drawString(getTitle(), labelPoint.x, labelPoint.y);

    }

    private void createImageOverlay() {
        upperX = 0;
        upperY = 0;
        lowerX = Integer.MAX_VALUE;
        lowerY = Integer.MAX_VALUE;
        for (Sample sample : samples) {
            Rectangle r = sample.tile;
            if ((r.x + r.width) > upperX) {
                upperX = r.x + r.width;
            }
            if ((r.y + r.height) > upperY) {
                upperY = r.y + r.height;
            }
            if (r.x < lowerX) {
                lowerX = r.x;
            }
            if (r.y < lowerY) {
                lowerY = r.y;
            }
        }
        int tileDim = samples.get(0).tile.width;
        overlayImage = new BufferedImage((upperX - lowerX) / tileDim, (upperY - lowerY) / tileDim, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D gOverlayImg = (Graphics2D) overlayImage.getGraphics();

        for (Sample sample : samples) {
            double v = sample.distribution[classIdxSelected];
            gOverlayImg.setColor(ColormapGray.getColor((int) (v * 255), 255));
            Rectangle r = sample.tile;
            Rectangle rPaint = new Rectangle((r.x - lowerX) / tileDim, (r.y - lowerY) / tileDim, 1, 1);
            gOverlayImg.fill(rPaint);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTableClassifierResults = new javax.swing.JTable();
        jSliderOccupacity = new javax.swing.JSlider();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Classifier Results");
        setLocationByPlatform(true);

        jTableClassifierResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Color", "Visible"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTableClassifierResults);

        jSliderOccupacity.setMaximum(255);
        jSliderOccupacity.setMinimum(1);
        jSliderOccupacity.setToolTipText("");
        jSliderOccupacity.setValue(255);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
            .addComponent(jSliderOccupacity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSliderOccupacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jImagerOccupacityPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jImagerOccupacityPropertyChange

    }//GEN-LAST:event_jImagerOccupacityPropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSliderOccupacity;
    private javax.swing.JTable jTableClassifierResults;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addImageViewerPluginListener(ImageViewerPluginListener imageViewerPluginListener) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                imageViewerPluginListener.diapose();
                super.windowClosing(e);
                imageViewer.repaint();
            }
        });
    }

    @Override
    public void close() {
        dispose();
    }

    private void drawSamplesDirect(Graphics2D g) {
        upperX = 0;
        upperY = 0;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int tileDim = samples.get(0).tile.width;
        for (Sample sample : samples) {
            Rectangle r = sample.tile;
            if ((r.x + r.width) > upperX) {
                upperX = r.x + r.width;
            }
            if ((r.y + r.height) > upperY) {
                upperY = r.y + r.height;
            }
            double v = sample.distribution[classIdxSelected];
            g.setColor(ColormapGray.getColor((int) (v * 255), 255));            
            g.fill(r);
        }
    }

}
