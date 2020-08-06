/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package editor.layerselector;

import editor.handler.MapEditorHandler;
import editor.handler.MapGrid;
import editor.state.MapLayerState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;

/**
 *
 * @author Trifindo
 */
public class ThumbnailLayerSelector extends javax.swing.JPanel {

    private MapEditorHandler handler;

    private BufferedImage[] layerThumbnails = new BufferedImage[MapGrid.numLayers];
    private static final int layerWidth = 64, layerHeight = 64;
    private static final int smallTileSize = 2;
    private static final Color backColor = new Color(0, 127, 127, 255);

    /**
     * Creates new form LayerSelector2
     */
    public ThumbnailLayerSelector() {
        initComponents();

        for (int i = 0; i < layerThumbnails.length; i++) {
            BufferedImage img = new BufferedImage(
                    layerWidth, layerHeight, BufferedImage.TYPE_INT_RGB);
            layerThumbnails[i] = img;
            Graphics g = img.getGraphics();
            g.setColor(backColor);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
        }

        setPreferredSize(new Dimension(layerWidth,
                layerHeight * MapGrid.numLayers));

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        if (handler != null) {
            int index = evt.getY() / layerHeight;
            if (index >= 0 && index < MapGrid.numLayers) {
                if (SwingUtilities.isLeftMouseButton(evt)) {
                    if(!handler.isLayerChanged()){
                        handler.setLayerChanged(true);
                        handler.addMapState(new MapLayerState("Layer change", handler));
                    }
                    handler.setActiveTileLayer(index);
                } else if (SwingUtilities.isRightMouseButton(evt)) {
                    handler.invertLayerState(index);
                } else if (SwingUtilities.isMiddleMouseButton(evt)) {
                    handler.setOnlyActiveTileLayer(index);
                }
                handler.getMainFrame().repaintMapDisplay();
                repaint();
            }
        }
    }//GEN-LAST:event_formMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (handler != null) {
            for (int i = 0; i < layerThumbnails.length; i++) {
                if (layerThumbnails[i] != null) {
                    g.drawImage(layerThumbnails[i], 0, i * layerHeight, null);
                }

                if (!handler.renderLayers[i]) {
                    g.setColor(new Color(0, 0, 0, 100));
                } else {
                    g.setColor(new Color(100, 100, 100, 0));
                }
                if (handler.getActiveLayerIndex() == i) {
                    //g.setColor(new Color(255, 100, 100, 100));
                }
                g.fillRect(0, i * layerHeight, layerWidth - 1, layerHeight - 1);

                g.setColor(Color.white);
                g.drawRect(0, i * layerHeight, layerWidth - 1, layerHeight - 1);
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g.setColor(Color.red);
            g.drawRect(1, handler.getActiveLayerIndex() * layerHeight + 1,
                    layerWidth - 2, layerHeight - 2);

        }

    }

    public void drawAllLayerThumbnails() {
        for (int i = 0; i < MapGrid.numLayers; i++) {
            drawLayerThumbnail(i);
        }
    }

    public void drawLayerThumbnail(int index) {
        BufferedImage imgLayer = layerThumbnails[index];
        Graphics g = imgLayer.getGraphics();
        g.setColor(backColor);
        g.fillRect(0, 0, layerWidth, layerHeight);
        int[][] grid = handler.getGrid().tileLayers[index];
        for (int i = 0; i < MapGrid.cols; i++) {
            for (int j = 0; j < MapGrid.rows; j++) {
                int tileIndex = grid[i][j];
                if (tileIndex != -1) {
                    if (tileIndex < handler.getTileset().size()) {
                        BufferedImage tileThumbnail = handler.getTileset().get(tileIndex).getSmallThumbnail();

                        g.drawImage(tileThumbnail,
                                i * smallTileSize,
                                (MapGrid.cols - j - 1) * smallTileSize - (tileThumbnail.getHeight() - smallTileSize), //+ tileThumbnail.getHeight(), 
                                null);
                    }
                }
            }
        }
    }

    public void init(MapEditorHandler handler) {
        this.handler = handler;
    }
}