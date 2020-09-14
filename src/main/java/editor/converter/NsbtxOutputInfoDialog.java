/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package editor.converter;

import editor.handler.MapData;
import editor.imd.*;
import editor.handler.MapEditorHandler;
import editor.nsbtx2.Nsbtx2;
import editor.nsbtx2.NsbtxImd;
import editor.nsbtx2.NsbtxTexture;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import renderer.ObjectGL;
import tileset.NormalsNotFoundException;
import tileset.TextureNotFoundException;
import tileset.Tile;
import tileset.TilesetMaterial;
import utils.Utils;

/**
 *
 * @author Trifindo
 */
public class NsbtxOutputInfoDialog extends javax.swing.JDialog {

    private MapEditorHandler handler;

    private ArrayList<Integer> areaIndices;
    private String nsbtxFolderPath;

    private ArrayList<Nsbtx2> nsbtxData;
    private ArrayList<String> errorMsgs;

    private Thread convertingThread;

    private static final Color GREEN = new Color(6, 176, 37);
    private static final Color ORANGE = new Color(255, 106, 0);
    private static final Color RED = Color.red;

    private enum ConvertStatus {
        SUCCESS_STATUS("SUCCESSFULLY CONVERTED", GREEN),
        CONVERTER_NOT_FOUND_STATUS("NOT CONVERTED (CONVERTER NOT FOUND)", RED),
        CONVERSION_ERROR_STATUS("NOT CONVERTED (CONVERSION ERROR)", RED),
        IMD_NOT_FOUND_ERROR_STATUS("NOT CONVERTED (IMD NOT FOUND)", RED),
        UNKNOWN_ERROR_STATUS("NOT CONVERTED (UNKNOWN ERROR)", RED),
        MOVE_FILE_ERROR_STATUS("NOT CONVERTED (ERROR MOVING FILE)", RED),
        INTERRUPT_ERROR_STATUS("NOT CONVERTED (ERROR CONVERTING THE MODEL)", RED);

        public final String msg;
        public final Color color;

        private ConvertStatus(String msg, Color color) {
            this.msg = msg;
            this.color = color;
        }
    };

    /**
     * Creates new form ImdOutputInfoDialog
     */
    public NsbtxOutputInfoDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        getRootPane().setDefaultButton(jbAccept);
        jbAccept.requestFocus();

        jTable1.getColumnModel().getColumn(0).setPreferredWidth(250);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(250);

        jTable1.getColumnModel().getColumn(1).setCellRenderer(new StatusColumnCellRenderer());

        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                int index = jTable1.getSelectedRow();
                updateView(index);
            }
        });

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jbAccept = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel2 = new javax.swing.JLabel();
        jlFilesProcessed = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jlFilesConverted = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jlFilesNotConverted = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jlStatus = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jlResult = new javax.swing.JLabel();
        jpCard = new javax.swing.JPanel();
        jpDisplay = new javax.swing.JPanel();
        nsbtxPanel1 = new editor.nsbtx2.NsbtxPanel();
        jpErrorInfo = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Resulting NSBTX files info (Experimental)");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jbAccept.setText("OK");
        jbAccept.setEnabled(false);
        jbAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAcceptActionPerformed(evt);
            }
        });
        jPanel1.add(jbAccept);

        jSplitPane1.setDividerLocation(550);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setText("NSBMD exporting progress:");

        jLabel2.setText("Files processed:");

        jlFilesProcessed.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jlFilesProcessed.setText("N/N");

        jLabel4.setText("Files converted into IMD:");

        jlFilesConverted.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jlFilesConverted.setText("N");

        jLabel8.setText("Files not converted:");

        jlFilesNotConverted.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jlFilesNotConverted.setText("N");

        jLabel3.setText("Status:");

        jlStatus.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jlStatus.setText("Converting...");

        jLabel5.setText("Result:");

        jlResult.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jlResult.setText(" ");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jlResult, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jlFilesConverted, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jlFilesNotConverted, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jlStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jlFilesProcessed, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jlStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jlFilesProcessed))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlFilesConverted))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlFilesNotConverted))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jlResult))
                .addGap(9, 9, 9))
        );

        jSplitPane1.setLeftComponent(jPanel2);

        jpCard.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout jpDisplayLayout = new javax.swing.GroupLayout(jpDisplay);
        jpDisplay.setLayout(jpDisplayLayout);
        jpDisplayLayout.setHorizontalGroup(
            jpDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDisplayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nsbtxPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpDisplayLayout.setVerticalGroup(
            jpDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDisplayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nsbtxPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jpCard.add(jpDisplay, "CardDisplay");

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setTabSize(3);
        jScrollPane2.setViewportView(jTextArea1);

        jLabel6.setText("Error info:");

        javax.swing.GroupLayout jpErrorInfoLayout = new javax.swing.GroupLayout(jpErrorInfo);
        jpErrorInfo.setLayout(jpErrorInfoLayout);
        jpErrorInfoLayout.setHorizontalGroup(
            jpErrorInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpErrorInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpErrorInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                    .addGroup(jpErrorInfoLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jpErrorInfoLayout.setVerticalGroup(
            jpErrorInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpErrorInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                .addContainerGap())
        );

        jpCard.add(jpErrorInfo, "CardErrorInfo");

        jSplitPane1.setRightComponent(jpCard);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSplitPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        if (convertingThread == null) {
            convertingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    saveAllNsbtx();
                }
            });
            convertingThread.start();
        }
    }//GEN-LAST:event_formWindowActivated

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        convertingThread.interrupt();
    }//GEN-LAST:event_formWindowClosed

    private void jbAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAcceptActionPerformed
        dispose();
    }//GEN-LAST:event_jbAcceptActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton jbAccept;
    private javax.swing.JLabel jlFilesConverted;
    private javax.swing.JLabel jlFilesNotConverted;
    private javax.swing.JLabel jlFilesProcessed;
    private javax.swing.JLabel jlResult;
    private javax.swing.JLabel jlStatus;
    private javax.swing.JPanel jpCard;
    private javax.swing.JPanel jpDisplay;
    private javax.swing.JPanel jpErrorInfo;
    private editor.nsbtx2.NsbtxPanel nsbtxPanel1;
    // End of variables declaration//GEN-END:variables

    public void init(MapEditorHandler handler, ArrayList<Integer> areaIndices,
            String nsbtxFolderPath) {
        this.handler = handler;
        this.areaIndices = areaIndices;
        this.nsbtxFolderPath = nsbtxFolderPath;
    }

    public void saveAllNsbtx() {
        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();

        nsbtxData = new ArrayList<>(areaIndices.size());
        errorMsgs = new ArrayList<>(areaIndices.size());
        for (int i = 0; i < areaIndices.size(); i++) {
            nsbtxData.add(null);
            errorMsgs.add(null);
        }

        int nFilesProcessed = 0;
        int nFilesConverted = 0;
        int nFilesNotConverted = 0;
        for (Integer areaIndex : areaIndices) {
            ConvertStatus exportStatus;
            if (!Thread.currentThread().isInterrupted()) {

                try {
                    HashSet<Integer> usedMaterialIndices = new HashSet<>();

                    // Add materials always included
                    for (int i = 0; i < handler.getTileset().getMaterials().size(); i++) {
                        if (handler.getTileset().getMaterials().get(i).alwaysIncludeInImd()) {
                            usedMaterialIndices.add(i);
                        }
                    }

                    HashSet<Integer> usedTileIndices = new HashSet<>();
                    for (MapData mapData : handler.getMapMatrix().getMatrix().values()) {
                        if (mapData.getAreaIndex() == areaIndex) {
                            mapData.getGrid().addTileIndicesUsed(usedTileIndices);
                        }
                    }

                    for (Integer tileIndex : usedTileIndices) {
                        ArrayList<Integer> texIDs = handler.getTileset().get(tileIndex).getTextureIDs();
                        for (Integer texID : texIDs) {
                            usedMaterialIndices.add(texID);
                        }
                    }

                    Nsbtx2 nsbtx = new Nsbtx2();
                    for (Integer matIndex : usedMaterialIndices) {
                        TilesetMaterial mat = handler.getTileset().getMaterial(matIndex);

                        boolean isTransparent = Utils.hasTransparentColor(mat.getTextureImg());

                        if ((!nsbtx.isTextureNameUsed(mat.getTextureNameImd())
                                && (!nsbtx.isPaletteNameUsed(mat.getPaletteNameImd())))) {
                            nsbtx.addTextureAndPalette(-1, -1,
                                    mat.getTextureImg(),
                                    Nsbtx2.jcbToFormatLookup[mat.getColorFormat()],
                                    isTransparent,
                                    mat.getTextureNameImd(),
                                    mat.getPaletteNameImd()
                            );
                        }
                    }

                    NsbtxImd imd = new NsbtxImd(nsbtx);

                    String pathSave = nsbtxFolderPath + File.separator + "Area_" + String.valueOf(areaIndex) + ".imd";
                    imd.saveToFile(pathSave);

                    File file = new File(pathSave);

                    if (file.exists()) {
                        String filename = new File(pathSave).getName();
                        filename = Utils.removeExtensionFromPath(filename);
                        try {
                            String converterPath = "converter/g3dcvtr.exe";
                            String[] cmd;
                            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                                cmd = new String[]{converterPath, pathSave, "-etex", "-o", filename};
                            } else {
                                cmd = new String[]{"wine", converterPath, pathSave, "-etex", "-o", filename};
                                // NOTE: wine call works only with relative path
                            }

                            if (!Files.exists(Paths.get(converterPath))) {
                                throw new IOException();
                            }

                            Process p = new ProcessBuilder(cmd).start();

                            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                            String outputString = "";
                            String line = null;
                            while ((line = stdError.readLine()) != null) {
                                outputString += line + "\n";
                            }

                            p.waitFor();
                            p.destroy();

                            String nsbPath = Utils.removeExtensionFromPath(pathSave);
                            nsbPath = Utils.addExtensionToPath(nsbPath, "nsbtx");

                            filename = Utils.removeExtensionFromPath(filename);
                            filename = Utils.addExtensionToPath(filename, "nsbtx");

                            System.out.println(System.getProperty("user.dir"));
                            File srcFile = new File(System.getProperty("user.dir") + File.separator + filename);
                            File dstFile = new File(nsbPath);
                            if (srcFile.exists()) {
                                try {
                                    Files.move(srcFile.toPath(), dstFile.toPath(),
                                            StandardCopyOption.REPLACE_EXISTING);
                                    //srcFile.renameTo(new File(nsbPath));
                                    exportStatus = ConvertStatus.SUCCESS_STATUS;
                                    nFilesConverted++;
                                    nsbtxData.set(nFilesProcessed, nsbtx);
                                } catch (IOException ex) {
                                    nFilesNotConverted++;
                                    exportStatus = ConvertStatus.MOVE_FILE_ERROR_STATUS;
                                    errorMsgs.set(nFilesProcessed, "File was not moved to the save directory. \n"
                                            + "Reopen Pokemon DS Map Studio and try again.");
                                }

                                if (file.exists()) {
                                    try {
                                        Files.delete(file.toPath());
                                    } catch (IOException ex) {

                                    }
                                }
                            } else {
                                nFilesNotConverted++;
                                exportStatus = ConvertStatus.CONVERSION_ERROR_STATUS;
                                errorMsgs.set(nFilesProcessed, "There was a problem creating the NSBMD file. \n"
                                        + "The output from the converter is:\n"
                                        + outputString);
                            }
                        } catch (IOException ex) {
                            nFilesNotConverted++;
                            exportStatus = ConvertStatus.CONVERTER_NOT_FOUND_STATUS;
                            errorMsgs.set(nFilesProcessed,
                                    "The program \"g3dcvtr.exe\" is not found in the \"converter\" folder.\n"
                                    + "Put the program and its *.dll files in the folder and try again.");

                        } catch (InterruptedException ex) {
                            nFilesNotConverted++;
                            exportStatus = ConvertStatus.INTERRUPT_ERROR_STATUS;
                            errorMsgs.set(nFilesProcessed,
                                    "The model was not converted (InterruptedException)");
                        }
                    } else {
                        nFilesNotConverted++;
                        exportStatus = ConvertStatus.UNKNOWN_ERROR_STATUS;
                        errorMsgs.set(nFilesProcessed, "Unknown error");
                    }
                } catch (Exception ex) {
                    nFilesNotConverted++;
                    exportStatus = ConvertStatus.UNKNOWN_ERROR_STATUS;
                    errorMsgs.set(nFilesProcessed, "Unknown error");
                }

                if (nFilesConverted > 0) {
                    jlFilesConverted.setForeground(GREEN);
                }

                if (nFilesNotConverted > 0) {
                    jlFilesNotConverted.setForeground(RED);
                }

                if (nFilesNotConverted > 0) {
                    jlStatus.setForeground(RED);
                    jlStatus.setText("Finished with errors");

                    jlResult.setForeground(RED);
                    jlResult.setText(String.valueOf(nFilesNotConverted) + " Area(s) could not be converted into NSBTX");
                } else {
                    jlStatus.setForeground(GREEN);
                    jlStatus.setText("Finished");

                    jlResult.setForeground(GREEN);
                    jlResult.setText("All the Areas files have been converted into NSBTX");
                }

                tableModel.addRow(new Object[]{
                    "Area_" + String.valueOf(areaIndex),
                    exportStatus
                });

                nFilesProcessed++;

                jlFilesProcessed.setText(String.valueOf(nFilesProcessed) + "/" + String.valueOf(areaIndices.size()));
                jlFilesConverted.setText(String.valueOf(nFilesConverted));
                jlFilesNotConverted.setText(String.valueOf(nFilesNotConverted));

                jProgressBar1.setValue((nFilesProcessed * 100) / areaIndices.size());
            }

        }
        jTable1.setRowSelectionInterval(0, 0);
        updateView(0);

        jbAccept.setEnabled(true);

        getRootPane().setDefaultButton(jbAccept);
        jbAccept.requestFocus();
    }

    public void updateView(int index) {
        try {
            CardLayout card = (CardLayout) jpCard.getLayout();
            if (nsbtxData.get(index) != null) {
                card.show(jpCard, "CardDisplay");
                jTextArea1.setText("");

                nsbtxPanel1.setNsbtx(nsbtxData.get(index));
                nsbtxPanel1.updateViewTextureNameList(0);
                nsbtxPanel1.updateViewPaletteNameList(0);
                nsbtxPanel1.updateView();
            } else if (errorMsgs.get(index) != null) {
                card.show(jpCard, "CardErrorInfo");
                jTextArea1.setText(errorMsgs.get(index));
            } else {
                card.show(jpCard, "CardDisplay");
                jTextArea1.setText("The NSBMD has been exported but it can't be displayed here.");

            }
        } catch (Exception ex) {

        }
    }

    private class StatusColumnCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            //Cells are by default rendered as a JLabel.
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            ConvertStatus status = (ConvertStatus) value;
            l.setForeground(status.color);
            l.setText(status.msg);

            Font font = l.getFont();
            font = font.deriveFont(
                    Collections.singletonMap(
                            TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD));
            l.setFont(font);

            setHorizontalAlignment(JLabel.CENTER);

            //Return the JLabel which renders the cell.
            return l;

        }
    }

}