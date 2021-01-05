/*
 * Created by JFormDesigner on Wed Nov 25 01:11:17 EST 2020
 */

package editor.buildingeditor2;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import editor.buildingeditor2.animations.ModelAnimation;
import editor.buildingeditor2.buildfile.Build;
import editor.buildingeditor2.buildfile.BuildFile;
import editor.buildingeditor2.wb.*;
import editor.handler.MapEditorHandler;
import formats.nsbtx2.*;
import net.miginfocom.swing.*;
import nitroreader.nsbca.NSBCAreader;
import nitroreader.nsbmd.NSBMD;
import nitroreader.nsbta.NSBTA;
import nitroreader.nsbta.NSBTAreader;
import nitroreader.nsbtp.NSBTP;
import nitroreader.nsbtp.NSBTPreader;
import nitroreader.shared.ByteReader;
import renderer.*;
import utils.Utils;

/**
 * @author PlatinumMaster
 */
public class BuildingEditorDialogWB extends JDialog {
    private MapEditorHandler handler;
    private BuildHandlerWB buildHandler;
    private JList<String> jlBuildModel;
    private AB currAB;
    private ArrayList<String> BuildingNames;
    private int currEntry;
    private Utils.MutableBoolean buildPropertiesEnabled = new Utils.MutableBoolean(true);
    private Utils.MutableBoolean jlBuildFileEnabled = new Utils.MutableBoolean(true);

    public BuildingEditorDialogWB(Window owner) {
        super(owner);
        initComponents();
        nitroDisplayMap.getObjectsGL().add(new ObjectGL());
    }

    public void init(MapEditorHandler handler, BuildHandlerWB buildHandler) {
        this.handler = handler;
        this.buildHandler = buildHandler;
    }

    public void loadGame(String folderPath) {
        buildHandler.setGameFolderPath(folderPath);
        try {
            buildHandler.loadAllFiles();
            currAB = buildHandler.getExtAB(0x34);
            updateBuildingNames();
            nitroDisplayMap.requestUpdate();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "There was a problem reading some of the files.",
                    "Error opening game files", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateBuildingNames()
    {
        DefaultComboBoxModel m = new DefaultComboBoxModel();
        for (int i = 0; i < currAB.nModels(); i++)
            m.addElement(String.format("%d: %s", currAB.getModelToID(i), currAB.getModel(i).getName()));
        jcBuildID.setModel(m);
    }

    private void jbOpenMapActionPerformed(ActionEvent e) {
        final JFileChooser fc = new JFileChooser();
        if (handler.getLastMapDirectoryUsed() != null) {
            fc.setCurrentDirectory(new File(handler.getLastMapDirectoryUsed()));
        }
        fc.setFileFilter(new FileNameExtensionFilter("NSBMD (*.nsbmd)", "nsbmd"));
        fc.setApproveButtonText("Open");
        fc.setDialogTitle("Open Map's NSBMD");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                handler.setLastMapDirectoryUsed(fc.getSelectedFile().getParent());
                byte[] mapData = Files.readAllBytes(fc.getSelectedFile().toPath());
                nitroDisplayMap.getObjectGL(0).setNsbmdData(mapData);
                nitroDisplayMap.requestUpdate();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "There was problem importing the map's NSBMD",
                        "Can't import map", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void jbImportBldActionPerformed(ActionEvent e) {
        final JFileChooser fc = new JFileChooser();
        if (handler.getLastMapDirectoryUsed() != null) {
            fc.setCurrentDirectory(new File(handler.getLastMapDirectoryUsed()));
        }
        fc.setFileFilter(new FileNameExtensionFilter("WB Building File (*.bld)", "bld"));
        fc.setApproveButtonText("Open");
        fc.setDialogTitle("Open the BLD");
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                handler.setLastMapDirectoryUsed(fc.getSelectedFile().getParent());
                // Parse Gen V BLD
                buildHandler.loadBuildingData(fc.getSelectedFile().toPath());
                updateViewBuildFileList(0);
                updateViewNitroDisplayMap();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "There was an issue while loading the building file.",
                        "Cannot import bld", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void updateViewBuildFileList(int indexSelected) {
        jlBuildFileEnabled.value = false;
        DefaultListModel listModel = new DefaultListModel();
        for (int i = 0; i < buildHandler.getBuildingList().size(); i++) {
            WBBuildingEntry e = buildHandler.getBuildingList().get(i);
            int num = currAB.getIDToModel(e.id);
            if (num != -1)
                listModel.addElement(String.format("%d: %s", e.id, currAB.getModel(num).getName()));
            else
                listModel.addElement(String.format("%d: Unknown", e.id));
        }
        jlBuildFile.setModel(listModel);
        jlBuildFileEnabled.value = true;

        jlBuildFile.setSelectedIndex(indexSelected);
        jlBuildFile.ensureIndexIsVisible(indexSelected);

    }

    private void jcBuildIDStateChanged(ActionEvent e) {
        if (buildPropertiesEnabled.value) {
            // May be buggy, haven't throughly tested.
            buildHandler.getBuildingList().get(currEntry).id = currAB.getModelToID(jcBuildID.getSelectedIndex());
            updateViewNitroDisplayMap();
            updateViewBuildFileList(jlBuildFile.getSelectedIndex());
        }
    }

    private void jsBuildXStateChanged(ChangeEvent e) {
        if (buildPropertiesEnabled.value) {
            float val = ((Double)jsBuildX.getValue()).floatValue();
            buildHandler.getBuildingList().get(currEntry).coords[0].setVal(FX32.TryParse(val / 16f));
            updateViewNitroDisplayMap();
        }
    }

    private void jsBuildYStateChanged(ChangeEvent e) {
        if (buildPropertiesEnabled.value) {
            float val = ((Double)jsBuildY.getValue()).floatValue();
            buildHandler.getBuildingList().get(currEntry).coords[1].setVal(FX32.TryParse(val / 16f));
            updateViewNitroDisplayMap();
        }
    }

    private void jsBuildZStateChanged(ChangeEvent e) {
        if (buildPropertiesEnabled.value) {
            float val = ((Double)jsBuildZ.getValue()).floatValue();
            buildHandler.getBuildingList().get(currEntry).coords[2].setVal(FX32.TryParse(val / 16f));
            updateViewNitroDisplayMap();
        }
    }

    private void jbExportBldActionPerformed(ActionEvent e) {
        final JFileChooser fc = new JFileChooser();
        if (handler.getLastMapDirectoryUsed() != null) {
            fc.setCurrentDirectory(new File(handler.getLastMapDirectoryUsed()));
        }
        fc.setFileFilter(new FileNameExtensionFilter("BLD (*.bld)", "bld"));
        fc.setApproveButtonText("Save");
        fc.setDialogTitle("Save Building File");
        try {
            //TODO: Replace this with some index bounds cheking?
            File file = new File(handler.getMapMatrix().filePath);
            String fileName = Utils.removeExtensionFromPath(file.getName()) + "." + BuildFile.fileExtension;
            fc.setSelectedFile(new File(fileName));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                handler.setLastMapDirectoryUsed(fc.getSelectedFile().getParent());

                byte[] buf = buildHandler.exportBLD(buildHandler.getBuildingList());
                try (FileOutputStream fos = new FileOutputStream(fc.getSelectedFile())) {
                    fos.write(buf);
                } catch (Exception ex) {
                    throw new IOException();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "There was a problem writing the BLD file",
                        "Error writing BLD file", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void setBoundingBoxes() {
        for (ObjectGL object : nitroDisplayMap.getObjectsGL()) {
            object.setDrawBounds(false);
        }

        try {
            nitroDisplayMap.getObjectsGL().get(1 + jlBuildFile.getSelectedIndex()).setDrawBounds(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbAddBuildBldActionPerformed(ActionEvent e) {
        buildHandler.getBuildingList().add(new WBBuildingEntry(){
            {
                coords = new FX32[]{new FX32(0x0), new FX32(0x0), new FX32(0x0)};
                id = 0x0;
                rotation = 0x0;
            }
        });
        updateViewBuildFileList(buildHandler.getBuildingList().size() - 1);
        updateViewNitroDisplayMap();
    }

    private void jbRemoveBldActionPerformed(ActionEvent e) {
        if (buildHandler.getBuildingList().size() == 0) {
            JOptionPane.showMessageDialog(handler.getMainFrame(),
                    "There's nothing to remove.",
                    "You good bro?", JOptionPane.ERROR_MESSAGE);
            return;
        }
        buildHandler.getBuildingList().remove(jlBuildFile.getSelectedIndex());
        DefaultListModel m = (DefaultListModel) jlBuildFile.getModel();
        m.remove(jlBuildFile.getSelectedIndex());
        jlBuildFile.setModel(m);
        updateViewNitroDisplayMap();
        updateViewBuildFileList(0);
    }

    public void updateViewNitroDisplayMap() {
        for (int i = 1, size = nitroDisplayMap.getObjectsGL().size(); i < size; i++) {
            nitroDisplayMap.getObjectsGL().remove(nitroDisplayMap.getObjectsGL().size() - 1);
        }

        for (int i = 0; i < buildHandler.getBuildingList().size(); i++) {
            WBBuildingEntry e = buildHandler.getBuildingList().get(i);
            ObjectGL object;
            try {
                object = nitroDisplayMap.getObjectGL(1 + i);
            } catch (Exception ex) {
                nitroDisplayMap.getObjectsGL().add(new ObjectGL());
                object = nitroDisplayMap.getObjectGL(1 + i);
            }
            try {
                int num = currAB.getIDToModel(e.id);
                if (num != -1)
                    object.setNsbmdData(currAB.getModel(num).getData());
                else
                    object.setNsbmdData(currAB.getModel(0).getData());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Technically: X, Z, Y
            object.setX(e.coords[0].toFloat() * 16f); // X (Left to Right)
            object.setY(e.coords[1].toFloat() * 16f); // Z (Up and Down)
            object.setZ(-e.coords[2].toFloat() * 16f); // Y (Forward and Backward)
            // Negate Y value for proper placement.
            nitroDisplayMap.requestUpdate();
        }
        setBoundingBoxes();
    }

    private void jlBuildFileValueChanged(ListSelectionEvent e) {
        if (jlBuildFile.getSelectedIndex() != -1) {
            buildPropertiesEnabled.value = false;
            currEntry = jlBuildFile.getSelectedIndex();
            WBBuildingEntry entry = buildHandler.getBuildingList().get(currEntry);
            jlBuildFile.setSelectedIndex(currEntry);
            jsBuildX.setValue(entry.coords[0].toFloat() * 16f);
            jsBuildY.setValue(entry.coords[1].toFloat() * 16f);
            jsBuildZ.setValue(-entry.coords[2].toFloat() * 16f);
            jcBuildID.setSelectedIndex(currAB.getIDToModel(entry.id));
            buildPropertiesEnabled.value = true;
        }
        updateViewNitroDisplayMap();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        jTabbedPane1 = new JTabbedPane();
        jPanel13 = new JPanel();
        jPanel14 = new JPanel();
        nitroDisplayMap = new NitroDisplayGL();
        jbOpenMap = new JButton();
        jLabel26 = new JLabel();
        jPanel15 = new JPanel();
        jScrollPane8 = new JScrollPane();
        jlBuildFile = new JList<>();
        jPanel16 = new JPanel();
        jLabel13 = new JLabel();
        jcBuildID = new JComboBox();
        jLabel14 = new JLabel();
        jsBuildX = new JSpinner();
        jLabel15 = new JLabel();
        jsBuildY = new JSpinner();
        jLabel16 = new JLabel();
        jsBuildZ = new JSpinner();
        jPanel17 = new JPanel();
        jPanel18 = new JPanel();
        jbImportBld = new JButton();
        jbExportBld = new JButton();
        jPanel19 = new JPanel();
        jbAddBuildBld = new JButton();
        jbRemoveBld = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Building Editor for Generation V (Experimental)");
        setModal(true);
        var contentPane = getContentPane();
        contentPane.setLayout(new MigLayout(
            "insets 0,hidemode 3,gap 5 5",
            // columns
            "[grow,fill]",
            // rows
            "[grow,fill]" +
            "[]"));

        //======== jTabbedPane1 ========
        {

            //======== jPanel13 ========
            {
                jPanel13.setLayout(new MigLayout(
                    "insets 0,hidemode 3,gap 5 5",
                    // columns
                    "[grow,fill]" +
                    "[grow,fill]",
                    // rows
                    "[grow,fill]"));

                //======== jPanel14 ========
                {
                    jPanel14.setBorder(new TitledBorder("Map Display"));
                    jPanel14.setLayout(new MigLayout(
                        "insets 0,hidemode 3,gap 5 5",
                        // columns
                        "[fill]" +
                        "[grow,fill]",
                        // rows
                        "[fill]" +
                        "[grow,fill]"));

                    //======== nitroDisplayMap ========
                    {
                        nitroDisplayMap.setBorder(new LineBorder(new Color(102, 102, 102)));

                        GroupLayout nitroDisplayMapLayout = new GroupLayout(nitroDisplayMap);
                        nitroDisplayMap.setLayout(nitroDisplayMapLayout);
                        nitroDisplayMapLayout.setHorizontalGroup(
                            nitroDisplayMapLayout.createParallelGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                        );
                        nitroDisplayMapLayout.setVerticalGroup(
                            nitroDisplayMapLayout.createParallelGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                        );
                    }
                    jPanel14.add(nitroDisplayMap, "cell 0 1 2 1");

                    //---- jbOpenMap ----
                    jbOpenMap.setIcon(new ImageIcon(getClass().getResource("/icons/ImportTileIcon.png")));
                    jbOpenMap.setText("Open Map");
                    jbOpenMap.addActionListener(e -> {
			jbOpenMapActionPerformed(e);
			jbOpenMapActionPerformed(e);
		});
                    jPanel14.add(jbOpenMap, "cell 0 0");

                    //---- jLabel26 ----
                    jLabel26.setText("*[Note: This map is used as a visual help for placing the buildings easily]");
                    jPanel14.add(jLabel26, "cell 1 0");
                }
                jPanel13.add(jPanel14, "cell 0 0");

                //======== jPanel15 ========
                {
                    jPanel15.setBorder(new TitledBorder("Building List"));
                    jPanel15.setLayout(new MigLayout(
                        "insets 0,hidemode 3,gap 5 5",
                        // columns
                        "[grow,fill]" +
                        "[fill]",
                        // rows
                        "[fill]" +
                        "[grow,fill]"));

                    //======== jScrollPane8 ========
                    {
                        jScrollPane8.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                        jScrollPane8.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                        //---- jlBuildFile ----
                        jlBuildFile.setModel(new AbstractListModel<String>() {
                            String[] values = {

                            };
                            @Override
                            public int getSize() { return values.length; }
                            @Override
                            public String getElementAt(int i) { return values[i]; }
                        });
                        jlBuildFile.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        jlBuildFile.addListSelectionListener(e -> jlBuildFileValueChanged(e));
                        jScrollPane8.setViewportView(jlBuildFile);
                    }
                    jPanel15.add(jScrollPane8, "cell 0 0 1 2");

                    //======== jPanel16 ========
                    {
                        jPanel16.setBorder(new TitledBorder("Selected Building"));
                        jPanel16.setLayout(new MigLayout(
                            "insets 0,hidemode 3,gap 5 5",
                            // columns
                            "[fill]" +
                            "[grow,fill]" +
                            "[fill]" +
                            "[grow,fill]",
                            // rows
                            "[fill]" +
                            "[fill]" +
                            "[fill]" +
                            "[fill]" +
                            "[fill]" +
                            "[]"));

                        //---- jLabel13 ----
                        jLabel13.setText("Building ID:");
                        jPanel16.add(jLabel13, "cell 0 0");

                        //---- jcBuildID ----
                        jcBuildID.addActionListener(e -> jcBuildIDStateChanged(e));
                        jPanel16.add(jcBuildID, "cell 1 0");

                        //---- jLabel14 ----
                        jLabel14.setForeground(new Color(204, 0, 0));
                        jLabel14.setText("X (Left and Right): ");
                        jPanel16.add(jLabel14, "cell 0 1");

                        //---- jsBuildX ----
                        jsBuildX.setModel(new SpinnerNumberModel(0.0F, -256.0F, 256.0F, 8.0F));
                        jsBuildX.addChangeListener(e -> jsBuildXStateChanged(e));
                        jPanel16.add(jsBuildX, "cell 1 1");

                        //---- jLabel15 ----
                        jLabel15.setForeground(new Color(51, 153, 0));
                        jLabel15.setText("Y (Up and Down) ");
                        jPanel16.add(jLabel15, "cell 0 2");

                        //---- jsBuildY ----
                        jsBuildY.setModel(new SpinnerNumberModel(0.0F, -256.0F, 256.0F, 8.0F));
                        jsBuildY.addChangeListener(e -> jsBuildYStateChanged(e));
                        jPanel16.add(jsBuildY, "cell 1 2");

                        //---- jLabel16 ----
                        jLabel16.setForeground(new Color(0, 0, 204));
                        jLabel16.setText("Z (Forwards and Backwards):");
                        jPanel16.add(jLabel16, "cell 0 3");

                        //---- jsBuildZ ----
                        jsBuildZ.setModel(new SpinnerNumberModel(0.0F, -256.0F, 256.0F, 8.0F));
                        jsBuildZ.addChangeListener(e -> jsBuildZStateChanged(e));
                        jPanel16.add(jsBuildZ, "cell 1 3");
                    }
                    jPanel15.add(jPanel16, "cell 1 1");

                    //======== jPanel17 ========
                    {
                        jPanel17.setBorder(new TitledBorder("Building File"));
                        jPanel17.setLayout(new GridLayout(2, 2));

                        //======== jPanel18 ========
                        {
                            jPanel18.setLayout(new GridLayout(1, 0, 5, 0));

                            //---- jbImportBld ----
                            jbImportBld.setIcon(new ImageIcon(getClass().getResource("/icons/ImportTileIcon.png")));
                            jbImportBld.setText("Import BLD File");
                            jbImportBld.setToolTipText("");
                            jbImportBld.addActionListener(e -> jbImportBldActionPerformed(e));
                            jPanel18.add(jbImportBld);

                            //---- jbExportBld ----
                            jbExportBld.setIcon(new ImageIcon(getClass().getResource("/icons/ExportIcon.png")));
                            jbExportBld.setText("Export BLD File");
                            jbExportBld.addActionListener(e -> jbExportBldActionPerformed(e));
                            jPanel18.add(jbExportBld);
                        }
                        jPanel17.add(jPanel18);

                        //======== jPanel19 ========
                        {
                            jPanel19.setLayout(new GridLayout(1, 0, 5, 0));

                            //---- jbAddBuildBld ----
                            jbAddBuildBld.setIcon(new ImageIcon(getClass().getResource("/icons/AddIcon.png")));
                            jbAddBuildBld.setText("Add Building");
                            jbAddBuildBld.setToolTipText("");
                            jbAddBuildBld.addActionListener(e -> jbAddBuildBldActionPerformed(e));
                            jPanel19.add(jbAddBuildBld);

                            //---- jbRemoveBld ----
                            jbRemoveBld.setIcon(new ImageIcon(getClass().getResource("/icons/RemoveIcon.png")));
                            jbRemoveBld.setText("Remove Building");
                            jbRemoveBld.addActionListener(e -> jbRemoveBldActionPerformed(e));
                            jPanel19.add(jbRemoveBld);
                        }
                        jPanel17.add(jPanel19);
                    }
                    jPanel15.add(jPanel17, "cell 1 0");
                }
                jPanel13.add(jPanel15, "cell 1 0");
            }
            jTabbedPane1.addTab("Map Buildings Editor", jPanel13);
        }
        contentPane.add(jTabbedPane1, "cell 0 0");
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JTabbedPane jTabbedPane1;
    private JPanel jPanel13;
    private JPanel jPanel14;
    private NitroDisplayGL nitroDisplayMap;
    private JButton jbOpenMap;
    private JLabel jLabel26;
    private JPanel jPanel15;
    private JScrollPane jScrollPane8;
    private JList<String> jlBuildFile;
    private JPanel jPanel16;
    private JLabel jLabel13;
    private JComboBox jcBuildID;
    private JLabel jLabel14;
    private JSpinner jsBuildX;
    private JLabel jLabel15;
    private JSpinner jsBuildY;
    private JLabel jLabel16;
    private JSpinner jsBuildZ;
    private JPanel jPanel17;
    private JPanel jPanel18;
    private JButton jbImportBld;
    private JButton jbExportBld;
    private JPanel jPanel19;
    private JButton jbAddBuildBld;
    private JButton jbRemoveBld;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
