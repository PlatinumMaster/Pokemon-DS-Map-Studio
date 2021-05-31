/*
 * Created by JFormDesigner on Wed Nov 25 01:11:17 EST 2020
 */

package editor.buildingeditor2;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import editor.buildingeditor2.animations.ModelAnimation;
import editor.buildingeditor2.buildfile.BuildFile;
import editor.buildingeditor2.wb.*;
import editor.handler.MapEditorHandler;
import net.miginfocom.swing.*;
import nitroreader.nsbca.NSBCA;
import nitroreader.nsbca.NSBCAreader;
import nitroreader.nsbmd.NSBMD;
import nitroreader.nsbmd.NSBMDreader;
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
    private AB currAB;
    private int currEntry;
    private ArrayList<ModelAnimation> animationList = new ArrayList<>();
    private Utils.MutableBoolean jlBuildModelEnabled = new Utils.MutableBoolean(true);
    private Utils.MutableBoolean buildPropertiesEnabled = new Utils.MutableBoolean(true);
    private Utils.MutableBoolean jlBuildFileEnabled = new Utils.MutableBoolean(true);
    private Utils.MutableBoolean jcbAnimationTypeEnabled = new Utils.MutableBoolean(true);

    public BuildingEditorDialogWB(Window owner) {
        super(owner);
        initComponents();
        nitroDisplayMap.getObjectsGL().add(new ObjectGL());
        nitroDisplayGL.getObjectsGL().add(new ObjectGL());
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
            nitroDisplayGL.getObjectGL(0).setNsbtxData(buildHandler.getExtABTextures(0x34));
            updateBuildingPack();
            nitroDisplayMap.requestUpdate();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "There was a problem reading some of the files.",
                    "Error opening game files", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateBuildingPack() {
        DefaultListModel m = new DefaultListModel();
        for (int i = 0; i < currAB.nModels(); ++i) {
            m.addElement(String.format("%d: %s", currAB.getModelToID(i), currAB.getModel(i).getName()));
        }
        jlBuildModel.setModel(m);
        loadCurrentNsbmd();
        updateBuildingNames();
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
            buildHandler.getBuildingList().get(currEntry).id = currAB.getModelToID(jcBuildID.getSelectedIndex());
            updateViewNitroDisplayMap();
            updateViewBuildFileList(jlBuildFile.getSelectedIndex());
        }
    }

    private void jsBuildXStateChanged(ChangeEvent e) {
        if (buildPropertiesEnabled.value) {
            float val = ((Double)jsBuildX.getValue()).floatValue();
            buildHandler.getBuildingList().get(currEntry).coords[0].SetValue(FX32.TryParse(val));
            updateViewNitroDisplayMap();
        }
    }

    private void jsBuildYStateChanged(ChangeEvent e) {
        if (buildPropertiesEnabled.value) {
            float val = ((Double)jsBuildY.getValue()).floatValue();
            buildHandler.getBuildingList().get(currEntry).coords[2].SetValue(FX32.TryParse(-val));
            updateViewNitroDisplayMap();
        }
    }

    private void jsBuildZStateChanged(ChangeEvent e) {
        if (buildPropertiesEnabled.value) {
            float val = ((Double)jsBuildZ.getValue()).floatValue();
            buildHandler.getBuildingList().get(currEntry).coords[1].SetValue(FX32.TryParse(val));
            updateViewNitroDisplayMap();
        }
    }

    private void jsRotationChanged(ChangeEvent e) {
        if (buildPropertiesEnabled.value) {
            buildHandler.getBuildingList().get(currEntry).rotation = (short)jsRotation.getValue();
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
                JOptionPane.showMessageDialog(this, "There was a problem writing to the BLD file.",
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
                coords = new FX32[]{
                        new FX32(0x0),
                        new FX32(0x0),
                        new FX32(0x0)
                };
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
            object.setX(e.coords[0].GetValueAsFloat() * 16f); // X (Left to Right)
            object.setY(e.coords[1].GetValueAsFloat() * 16f); // Z (Up and Down)
            object.setZ(-e.coords[2].GetValueAsFloat() * 16f); // Y (Forward and Backward)
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
            jsBuildX.setValue(entry.coords[0].GetValueAsFloat());
            jsBuildY.setValue(-entry.coords[2].GetValueAsFloat());
            jsBuildZ.setValue(entry.coords[1].GetValueAsFloat());
            jsRotation.setValue(entry.rotation);
            jcBuildID.setSelectedIndex(currAB.getIDToModel(entry.id));
            buildPropertiesEnabled.value = true;
        }
        updateViewNitroDisplayMap();
    }

    private void jlBuildModelValueChanged(ListSelectionEvent e) {
        if (jlBuildModelEnabled.value) {
            updateViewSelectedBuildAnimationsList(jlBuildModel.getSelectedIndex());
            loadCurrentNsbmd();
            nitroDisplayGL.fitCameraToModel(0);
            nitroDisplayGL.requestUpdate();
        }
    }

    public void loadCurrentNsbmd() {
        try {
            byte[] data = currAB.getModel(jlBuildModel.getSelectedIndex()).getData();
            nitroDisplayGL.getObjectGL(0).setNsbmdData(data);
            nitroDisplayGL.getObjectGL(0).setNsbca(null);
            nitroDisplayGL.getObjectGL(0).setNsbtp(null);
            nitroDisplayGL.getObjectGL(0).setNsbta(null);
            nitroDisplayGL.getObjectGL(0).setNsbva(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateViewSelectedBuildAnimationsList(int indexSelected) {
        jcbAnimationTypeEnabled.value = false;
        ABEntry entry = currAB.getABEntryByID(currAB.getModelToID(indexSelected));
        DefaultListModel m = new DefaultListModel();
        for (int i = 0; i < entry.numFiles(); ++i) {
            m.addElement(entry.getFile(i).getName());
            animationList.add(entry.getFile(i));
        }
        jlSelectedAnimationsList.setModel(m);
        jcbAnimationTypeEnabled.value = true;
    }

    private void jbAddBuildingActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbReplaceBuildingActionPerformed(ActionEvent e) {
        final JFileChooser fc = new JFileChooser();
        if (handler.getLastMapDirectoryUsed() != null) {
            fc.setCurrentDirectory(new File(handler.getLastMapDirectoryUsed()));
        }
        fc.setFileFilter(new FileNameExtensionFilter("NSBMD (*.nsbmd)", "nsbmd"));
        fc.setApproveButtonText("Open");
        fc.setDialogTitle("Load model");
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                handler.setLastMapDirectoryUsed(fc.getSelectedFile().getParent());
                currAB.replaceModel(jlBuildModel.getSelectedIndex(), new NitroModel(Files.readAllBytes(fc.getSelectedFile().toPath())));
                updateBuildingPack();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "There was an issue while replacing the model.",
                        "Cannot import model", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void jbExportBuildingActionPerformed(ActionEvent e) {
        final JFileChooser fc = new JFileChooser();
        if (handler.getLastMapDirectoryUsed() != null) {
            fc.setCurrentDirectory(new File(handler.getLastMapDirectoryUsed()));
        }
        fc.setFileFilter(new FileNameExtensionFilter("NSBMD (*.nsbmd)", "nsbmd"));
        fc.setApproveButtonText("Save");
        fc.setDialogTitle("Save Model");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                handler.setLastMapDirectoryUsed(fc.getSelectedFile().getParent());
                byte[] buf = currAB.getModel(jlBuildModel.getSelectedIndex()).getData();
                try (FileOutputStream fos = new FileOutputStream(fc.getSelectedFile())) {
                    fos.write(buf);
                } catch (Exception ex) {
                    throw new IOException();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "There was a problem writing to the BLD file.",
                        "Error writing model.", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void jbRemoveBuildingActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbFindBuildingActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbAddMaterialActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbRemoveMaterialActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbImportMaterialsFromNsbmdActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbSetAnimationActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbMoveMaterialUpActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbMoveMaterialDownActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbAddAnimToBuildActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbReplaceAnimToBuildActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbRemoveAnimToBuildActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbPlayActionPerformed(ActionEvent e) {
        if (jlSelectedAnimationsList.getSelectedIndex() != -1) {
            loadAnimationInNitroDisplay(nitroDisplayGL, 0, animationList.get(jlSelectedAnimationsList.getSelectedIndex()));
        }
    }

    private void jcbAnimType1ActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jcbLoopTypeActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jcbAnimType2ActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jcbNumAnimsActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jcbUnknown1ActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void loadAnimationInNitroDisplay(NitroDisplayGL display, int objectIndex, ModelAnimation anim) {
        if (anim.getAnimationType() == ModelAnimation.TYPE_NSBCA) {
            NSBCAreader reader = new NSBCAreader(new ByteReader(anim.getData()));
            display.getObjectGL(objectIndex).setNsbca((NSBCA) reader.readFile());
            display.requestUpdate();
        } else if (anim.getAnimationType() == ModelAnimation.TYPE_NSBTA) {
            NSBTAreader reader = new NSBTAreader(new ByteReader(anim.getData()));
            display.getObjectGL(objectIndex).setNsbta((NSBTA) reader.readFile());
            display.requestUpdate();
        } else if (anim.getAnimationType() == ModelAnimation.TYPE_NSBTP) {
            NSBTPreader reader = new NSBTPreader(new ByteReader(anim.getData()));
            display.getObjectGL(objectIndex).setNsbtp((NSBTP) reader.readFile());
            display.requestUpdate();
        }
        /*else if (anim.getAnimationType() == BuildAnimation.TYPE_NSBMA) {
            NSBMAreader reader = new NSBMAreader(new ByteReader(anim.getData()));
            nitroDisplayGL1.getHandler().setNsbma((NSBMA) reader.readFile());
            nitroDisplayGL1.requestUpdate();
        }*/
    }

    private void button1ActionPerformed(ActionEvent e) throws IOException {
        currAB.Serialize("testing.ab");
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        jTabbedPane1 = new JTabbedPane();
        jPanel3 = new JPanel();
        jPanel1 = new JPanel();
        jLabel4 = new JLabel();
        nitroDisplayGL = new NitroDisplayGL();
        jScrollPane1 = new JScrollPane();
        jlBuildModel = new JList<>();
        panel2 = new JPanel();
        jbAddBuilding = new JButton();
        jbReplaceBuilding = new JButton();
        jbExportBuilding = new JButton();
        jbRemoveBuilding = new JButton();
        jbFindBuilding = new JButton();
        button1 = new JButton();
        panel3 = new JPanel();
        jPanel8 = new JPanel();
        jLabel3 = new JLabel();
        jScrollPane3 = new JScrollPane();
        jlSelectedAnimationsList = new JList<>();
        panel5 = new JPanel();
        jbAddAnimToBuild = new JButton();
        jbReplaceAnimToBuild = new JButton();
        jbRemoveAnimToBuild = new JButton();
        jbPlay = new JButton();
        jPanel13 = new JPanel();
        jPanel14 = new JPanel();
        nitroDisplayMap = new NitroDisplayGL();
        jbOpenMap = new JButton();
        jPanel15 = new JPanel();
        jPanel17 = new JPanel();
        jPanel18 = new JPanel();
        jbImportBld = new JButton();
        jbExportBld = new JButton();
        jPanel19 = new JPanel();
        jbAddBuildBld = new JButton();
        jbRemoveBld = new JButton();
        jScrollPane8 = new JScrollPane();
        jlBuildFile = new JList<>();
        jPanel16 = new JPanel();
        jLabel13 = new JLabel();
        jcBuildID = new JComboBox();
        jLabel14 = new JLabel();
        jsBuildX = new JSpinner();
        jLabel16 = new JLabel();
        jsBuildY = new JSpinner();
        jLabel15 = new JLabel();
        jsBuildZ = new JSpinner();
        jLabel17 = new JLabel();
        jsRotation = new JSpinner();

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

            //======== jPanel3 ========
            {
                jPanel3.setLayout(new MigLayout(
                    "insets 5,hidemode 3,gap 5 5",
                    // columns
                    "[grow,fill]" +
                    "[grow,fill]",
                    // rows
                    "[grow,fill]"));

                //======== jPanel1 ========
                {
                    jPanel1.setBorder(new TitledBorder("Building Selector (build_model.narc)"));
                    jPanel1.setLayout(new MigLayout(
                        "insets 5,hidemode 3,gap 5 5",
                        // columns
                        "[462,grow,fill]" +
                        "[164,fill]" +
                        "[fill]",
                        // rows
                        "[fill]" +
                        "[grow,fill]"));

                    //---- jLabel4 ----
                    jLabel4.setIcon(new ImageIcon(getClass().getResource("/icons/BuildingIcon.png")));
                    jLabel4.setText("Building List:");
                    jLabel4.setToolTipText("");
                    jPanel1.add(jLabel4, "cell 1 0");

                    //======== nitroDisplayGL ========
                    {
                        nitroDisplayGL.setBorder(new LineBorder(new Color(102, 102, 102)));

                        GroupLayout nitroDisplayGLLayout = new GroupLayout(nitroDisplayGL);
                        nitroDisplayGL.setLayout(nitroDisplayGLLayout);
                        nitroDisplayGLLayout.setHorizontalGroup(
                            nitroDisplayGLLayout.createParallelGroup()
                                .addGap(0, 657, Short.MAX_VALUE)
                        );
                        nitroDisplayGLLayout.setVerticalGroup(
                            nitroDisplayGLLayout.createParallelGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                        );
                    }
                    jPanel1.add(nitroDisplayGL, "cell 0 0 1 2");

                    //======== jScrollPane1 ========
                    {
                        jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                        jScrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                        //---- jlBuildModel ----
                        jlBuildModel.setModel(new AbstractListModel<String>() {
                            String[] values = {

                            };
                            @Override
                            public int getSize() { return values.length; }
                            @Override
                            public String getElementAt(int i) { return values[i]; }
                        });
                        jlBuildModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        jlBuildModel.addListSelectionListener(e -> jlBuildModelValueChanged(e));
                        jScrollPane1.setViewportView(jlBuildModel);
                    }
                    jPanel1.add(jScrollPane1, "cell 1 1");

                    //======== panel2 ========
                    {
                        panel2.setLayout(new GridBagLayout());
                        ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
                        ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
                        ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
                        ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                        //---- jbAddBuilding ----
                        jbAddBuilding.setIcon(new ImageIcon(getClass().getResource("/icons/AddIcon.png")));
                        jbAddBuilding.setText("Add Building");
                        jbAddBuilding.setHorizontalAlignment(SwingConstants.LEFT);
                        jbAddBuilding.addActionListener(e -> jbAddBuildingActionPerformed(e));
                        panel2.add(jbAddBuilding, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbReplaceBuilding ----
                        jbReplaceBuilding.setIcon(new ImageIcon(getClass().getResource("/icons/ReplaceIcon.png")));
                        jbReplaceBuilding.setText("Replace Building");
                        jbReplaceBuilding.setHorizontalAlignment(SwingConstants.LEFT);
                        jbReplaceBuilding.addActionListener(e -> jbReplaceBuildingActionPerformed(e));
                        panel2.add(jbReplaceBuilding, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbExportBuilding ----
                        jbExportBuilding.setIcon(new ImageIcon(getClass().getResource("/icons/ExportIcon.png")));
                        jbExportBuilding.setText("Export Building");
                        jbExportBuilding.setHorizontalAlignment(SwingConstants.LEFT);
                        jbExportBuilding.addActionListener(e -> jbExportBuildingActionPerformed(e));
                        panel2.add(jbExportBuilding, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbRemoveBuilding ----
                        jbRemoveBuilding.setIcon(new ImageIcon(getClass().getResource("/icons/RemoveIcon.png")));
                        jbRemoveBuilding.setText("Remove Building");
                        jbRemoveBuilding.setEnabled(false);
                        jbRemoveBuilding.setHorizontalAlignment(SwingConstants.LEFT);
                        jbRemoveBuilding.addActionListener(e -> jbRemoveBuildingActionPerformed(e));
                        panel2.add(jbRemoveBuilding, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbFindBuilding ----
                        jbFindBuilding.setIcon(new ImageIcon(getClass().getResource("/icons/SearchIcon.png")));
                        jbFindBuilding.setText("Find Usages");
                        jbFindBuilding.setHorizontalAlignment(SwingConstants.LEFT);
                        jbFindBuilding.addActionListener(e -> jbFindBuildingActionPerformed(e));
                        panel2.add(jbFindBuilding, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- button1 ----
                        button1.setText("pack test");
                        button1.addActionListener(e -> {
                            try {
                                button1ActionPerformed(e);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        });
                        panel2.add(button1, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                    }
                    jPanel1.add(panel2, "cell 2 1");
                }
                jPanel3.add(jPanel1, "cell 0 0");

                //======== panel3 ========
                {
                    panel3.setLayout(new MigLayout(
                        "hidemode 3",
                        // columns
                        "[grow,fill]",
                        // rows
                        "[grow,fill]" +
                        "[grow,fill]"));

                    //======== jPanel8 ========
                    {
                        jPanel8.setBorder(new TitledBorder("Selected Building Animations (bm_anime_list.narc)"));
                        jPanel8.setLayout(new MigLayout(
                            "insets 05 5 5 5,hidemode 3,gap 5 5",
                            // columns
                            "[156,grow,fill]" +
                            "[fill]",
                            // rows
                            "[fill]" +
                            "[grow,fill]" +
                            "[fill]"));

                        //---- jLabel3 ----
                        jLabel3.setIcon(new ImageIcon(getClass().getResource("/icons/AnimationIcon.png")));
                        jLabel3.setText("Animations:");
                        jLabel3.setToolTipText("");
                        jPanel8.add(jLabel3, "cell 0 0 2 1");

                        //======== jScrollPane3 ========
                        {
                            jScrollPane3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                            jScrollPane3.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                            //---- jlSelectedAnimationsList ----
                            jlSelectedAnimationsList.setModel(new AbstractListModel<String>() {
                                String[] values = {

                                };
                                @Override
                                public int getSize() { return values.length; }
                                @Override
                                public String getElementAt(int i) { return values[i]; }
                            });
                            jlSelectedAnimationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            jScrollPane3.setViewportView(jlSelectedAnimationsList);
                        }
                        jPanel8.add(jScrollPane3, "cell 0 1");

                        //======== panel5 ========
                        {
                            panel5.setLayout(new GridBagLayout());
                            ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0};
                            ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
                            ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                            ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

                            //---- jbAddAnimToBuild ----
                            jbAddAnimToBuild.setIcon(new ImageIcon(getClass().getResource("/icons/AddIcon.png")));
                            jbAddAnimToBuild.setText("Add Animation");
                            jbAddAnimToBuild.setHorizontalAlignment(SwingConstants.LEFT);
                            jbAddAnimToBuild.addActionListener(e -> jbAddAnimToBuildActionPerformed(e));
                            panel5.add(jbAddAnimToBuild, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 0, 5, 0), 0, 0));

                            //---- jbReplaceAnimToBuild ----
                            jbReplaceAnimToBuild.setIcon(new ImageIcon(getClass().getResource("/icons/ReplaceIcon.png")));
                            jbReplaceAnimToBuild.setText("Replace Animation");
                            jbReplaceAnimToBuild.setHorizontalAlignment(SwingConstants.LEFT);
                            jbReplaceAnimToBuild.addActionListener(e -> jbReplaceAnimToBuildActionPerformed(e));
                            panel5.add(jbReplaceAnimToBuild, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 0, 5, 0), 0, 0));

                            //---- jbRemoveAnimToBuild ----
                            jbRemoveAnimToBuild.setIcon(new ImageIcon(getClass().getResource("/icons/RemoveIcon.png")));
                            jbRemoveAnimToBuild.setText("Remove Animation");
                            jbRemoveAnimToBuild.setHorizontalAlignment(SwingConstants.LEFT);
                            jbRemoveAnimToBuild.addActionListener(e -> jbRemoveAnimToBuildActionPerformed(e));
                            panel5.add(jbRemoveAnimToBuild, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 0, 5, 0), 0, 0));

                            //---- jbPlay ----
                            jbPlay.setIcon(new ImageIcon(getClass().getResource("/icons/AnimationIcon.png")));
                            jbPlay.setText("Play Animation");
                            jbPlay.setHorizontalAlignment(SwingConstants.LEFT);
                            jbPlay.addActionListener(e -> jbPlayActionPerformed(e));
                            panel5.add(jbPlay, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 0, 0, 0), 0, 0));
                        }
                        jPanel8.add(panel5, "cell 1 1");
                    }
                    panel3.add(jPanel8, "cell 0 0 1 2");
                }
                jPanel3.add(panel3, "cell 1 0");
            }
            jTabbedPane1.addTab("Building Pack Editor", jPanel3);

            //======== jPanel13 ========
            {
                jPanel13.setLayout(new GridLayout());

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
                        nitroDisplayMap.setLayout(new BoxLayout(nitroDisplayMap, BoxLayout.X_AXIS));
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
                }
                jPanel13.add(jPanel14);

                //======== jPanel15 ========
                {
                    jPanel15.setBorder(new TitledBorder("Building List Editor"));
                    jPanel15.setLayout(new MigLayout(
                        "insets 0,hidemode 3,gap 0 5",
                        // columns
                        "[grow,fill]" +
                        "[fill]",
                        // rows
                        "[fill]" +
                        "[grow,fill]"));

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
                    jPanel15.add(jPanel17, "cell 0 0 2 1");

                    //======== jScrollPane8 ========
                    {
                        jScrollPane8.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                        jScrollPane8.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                        jScrollPane8.setViewportBorder(new TitledBorder("Building List"));

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
                        jlBuildFile.setBorder(new TitledBorder("text"));
                        jlBuildFile.addListSelectionListener(e -> jlBuildFileValueChanged(e));
                        jScrollPane8.setViewportView(jlBuildFile);
                    }
                    jPanel15.add(jScrollPane8, "cell 0 0 2 1");

                    //======== jPanel16 ========
                    {
                        jPanel16.setBorder(new TitledBorder("Properties of the Selected Building"));
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
                        jsBuildX.setModel(new SpinnerNumberModel(0.0F, -16.0F, 16.0F, 0.5F));
                        jsBuildX.addChangeListener(e -> jsBuildXStateChanged(e));
                        jPanel16.add(jsBuildX, "cell 1 1");

                        //---- jLabel16 ----
                        jLabel16.setForeground(new Color(0, 0, 204));
                        jLabel16.setText("Y (Forwards and Backwards):");
                        jPanel16.add(jLabel16, "cell 0 2");

                        //---- jsBuildY ----
                        jsBuildY.setModel(new SpinnerNumberModel(0.0F, -16.0F, 16.0F, 0.5F));
                        jsBuildY.addChangeListener(e -> jsBuildYStateChanged(e));
                        jPanel16.add(jsBuildY, "cell 1 2");

                        //---- jLabel15 ----
                        jLabel15.setForeground(new Color(51, 153, 0));
                        jLabel15.setText("Z (Up and Down) ");
                        jPanel16.add(jLabel15, "cell 0 3");

                        //---- jsBuildZ ----
                        jsBuildZ.setModel(new SpinnerNumberModel(0.0F, -16.0F, 16.0F, 0.5F));
                        jsBuildZ.addChangeListener(e -> jsBuildZStateChanged(e));
                        jPanel16.add(jsBuildZ, "cell 1 3");

                        //---- jLabel17 ----
                        jLabel17.setForeground(new Color(51, 51, 255));
                        jLabel17.setText("Rotation");
                        jPanel16.add(jLabel17, "cell 0 4");

                        //---- jsRotation ----
                        jsRotation.setModel(new SpinnerNumberModel(0, null, null, 0));
                        jsRotation.addChangeListener(e -> jsRotationChanged(e));
                        jPanel16.add(jsRotation, "cell 1 4");
                    }
                    jPanel15.add(jPanel16, "cell 0 1");
                }
                jPanel13.add(jPanel15);
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
    private JPanel jPanel3;
    private JPanel jPanel1;
    private JLabel jLabel4;
    private NitroDisplayGL nitroDisplayGL;
    private JScrollPane jScrollPane1;
    private JList<String> jlBuildModel;
    private JPanel panel2;
    private JButton jbAddBuilding;
    private JButton jbReplaceBuilding;
    private JButton jbExportBuilding;
    private JButton jbRemoveBuilding;
    private JButton jbFindBuilding;
    private JButton button1;
    private JPanel panel3;
    private JPanel jPanel8;
    private JLabel jLabel3;
    private JScrollPane jScrollPane3;
    private JList<String> jlSelectedAnimationsList;
    private JPanel panel5;
    private JButton jbAddAnimToBuild;
    private JButton jbReplaceAnimToBuild;
    private JButton jbRemoveAnimToBuild;
    private JButton jbPlay;
    private JPanel jPanel13;
    private JPanel jPanel14;
    private NitroDisplayGL nitroDisplayMap;
    private JButton jbOpenMap;
    private JPanel jPanel15;
    private JPanel jPanel17;
    private JPanel jPanel18;
    private JButton jbImportBld;
    private JButton jbExportBld;
    private JPanel jPanel19;
    private JButton jbAddBuildBld;
    private JButton jbRemoveBld;
    private JScrollPane jScrollPane8;
    private JList<String> jlBuildFile;
    private JPanel jPanel16;
    private JLabel jLabel13;
    private JComboBox jcBuildID;
    private JLabel jLabel14;
    private JSpinner jsBuildX;
    private JLabel jLabel16;
    private JSpinner jsBuildY;
    private JLabel jLabel15;
    private JSpinner jsBuildZ;
    private JLabel jLabel17;
    private JSpinner jsRotation;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
