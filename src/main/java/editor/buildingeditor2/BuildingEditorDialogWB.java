/*
 * Created by JFormDesigner on Wed Nov 25 01:11:17 EST 2020
 */

package editor.buildingeditor2;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
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
import editor.buildingeditor2.wb.AB;
import editor.buildingeditor2.wb.WBBuildingEntry;
import editor.buildingeditor2.wb.WBBuildingList;
import editor.handler.MapEditorHandler;
import editor.nsbtx2.*;
import net.miginfocom.swing.*;
import nitroreader.nsbca.NSBCA;
import nitroreader.nsbca.NSBCAreader;
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
            nitroDisplayMap.requestUpdate();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "There was a problem reading some of the files.",
                    "Error opening game files", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void jbOpenMapActionPerformed(ActionEvent e) {
        final JFileChooser fc = new JFileChooser();
        if (handler.getLastMapDirectoryUsed() != null) {
            fc.setCurrentDirectory(new File(handler.getLastMapDirectoryUsed()));
        }
        fc.setFileFilter(new FileNameExtensionFilter("NSBMD (*.nsbmd)", "nsbmd"));
        fc.setApproveButtonText("Open");
        fc.setDialogTitle("Open Map's NSBMD");
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
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
                nitroDisplayMap.requestUpdate();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "There was an issue while loading the building file.",
                        "Cannot import bld", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void updateViewBuildFileList(int indexSelected) {

        DefaultListModel listModel = new DefaultListModel();
        // Temporary until I get things working.
        for (int i = 0; i < buildHandler.getBuildingList().size(); i++) {
            WBBuildingEntry e = buildHandler.getBuildingList().get(i);
            listModel.addElement(buildHandler.getExtAB(0).getModel(i).getName());
        }
        jlBuildFile.setModel(listModel);
    }

    private void jsBuildIDStateChanged(ChangeEvent e) {
        // TODO add your code here
    }

    private void jbChooseModelBldActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jsBuildXStateChanged(ChangeEvent e) {
        // TODO add your code here
    }

    private void jsBuildYStateChanged(ChangeEvent e) {
        // TODO add your code here
    }

    private void jsBuildZStateChanged(ChangeEvent e) {
        // TODO add your code here
    }

    private void jsBuildScaleXStateChanged(ChangeEvent e) {
        // TODO add your code here
    }

    private void jsBuildScaleYStateChanged(ChangeEvent e) {
        // TODO add your code here
    }

    private void jsBuildScaleZStateChanged(ChangeEvent e) {
        // TODO add your code here
    }

    private void jbExportBldActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbAddBuildBldActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbRemoveBldActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void loadAnimationInNitroDisplay(NitroDisplayGL display, int objectIndex, ModelAnimation anim) {
        // TODO: Rewrite as switch case
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
                AB data = buildHandler.getExtAB(0);
                object.setNsbmdData(data.getModel(i).getData());
                buildHandler.getBuildingList().get(i).
                //buildHandler.getBuildingList().getModelsData().get(build.getModeID());

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            object.setX(e.coords[0].toFloat() * 64.0f);
            object.setY(e.coords[1].toFloat() * 64.0f);
            object.setZ(e.coords[2].toFloat() * 64.0f);

            nitroDisplayMap.requestUpdate();
        }
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
        jsBuildID = new JSpinner();
        jbChooseModelBld = new JButton();
        jLabel14 = new JLabel();
        jsBuildX = new JSpinner();
        jLabel15 = new JLabel();
        jsBuildY = new JSpinner();
        jLabel16 = new JLabel();
        jsBuildZ = new JSpinner();
        jLabel17 = new JLabel();
        jsBuildScaleX = new JSpinner();
        jsBuildScaleY = new JSpinner();
        jLabel18 = new JLabel();
        jLabel19 = new JLabel();
        jsBuildScaleZ = new JSpinner();
        jLabel20 = new JLabel();
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
                    jPanel15.setBorder(new TitledBorder("Building Editor (*.bld)"));
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
                        //jlBuildFile.addListSelectionListener(e -> jlBuildFileValueChanged(e));
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
                            "[fill]"));

                        //---- jLabel13 ----
                        jLabel13.setText("Building ID:");
                        jPanel16.add(jLabel13, "cell 0 0");

                        //---- jsBuildID ----
                        jsBuildID.setModel(new SpinnerNumberModel(0, 0, null, 1));
                        jsBuildID.addChangeListener(e -> jsBuildIDStateChanged(e));
                        jPanel16.add(jsBuildID, "cell 1 0");

                        //---- jbChooseModelBld ----
                        jbChooseModelBld.setIcon(new ImageIcon(getClass().getResource("/icons/ReplaceIcon.png")));
                        jbChooseModelBld.setText("Change Model");
                        jbChooseModelBld.addActionListener(e -> jbChooseModelBldActionPerformed(e));
                        jPanel16.add(jbChooseModelBld, "cell 3 0");

                        //---- jLabel14 ----
                        jLabel14.setForeground(new Color(204, 0, 0));
                        jLabel14.setText("X: ");
                        jPanel16.add(jLabel14, "cell 0 1");

                        //---- jsBuildX ----
                        jsBuildX.setModel(new SpinnerNumberModel(0.0F, -16.0F, 15.0F, 1.0F));
                        jsBuildX.addChangeListener(e -> jsBuildXStateChanged(e));
                        jPanel16.add(jsBuildX, "cell 1 1");

                        //---- jLabel15 ----
                        jLabel15.setForeground(new Color(51, 153, 0));
                        jLabel15.setText("Y: ");
                        jPanel16.add(jLabel15, "cell 0 2");

                        //---- jsBuildY ----
                        jsBuildY.setModel(new SpinnerNumberModel(0.0F, -16.0F, 15.0F, 1.0F));
                        jsBuildY.addChangeListener(e -> jsBuildYStateChanged(e));
                        jPanel16.add(jsBuildY, "cell 1 2");

                        //---- jLabel16 ----
                        jLabel16.setForeground(new Color(0, 0, 204));
                        jLabel16.setText("Z: ");
                        jPanel16.add(jLabel16, "cell 0 3");

                        //---- jsBuildZ ----
                        jsBuildZ.setModel(new SpinnerNumberModel(0.0F, -16.0F, 15.0F, 1.0F));
                        jsBuildZ.addChangeListener(e -> jsBuildZStateChanged(e));
                        jPanel16.add(jsBuildZ, "cell 1 3");

                        //---- jLabel17 ----
                        jLabel17.setForeground(new Color(204, 0, 0));
                        jLabel17.setText("Scale X: ");
                        jPanel16.add(jLabel17, "cell 2 1");

                        //---- jsBuildScaleX ----
                        jsBuildScaleX.setModel(new SpinnerNumberModel(0.0F, 0.0F, 15.0F, 1.0F));
                        jsBuildScaleX.addChangeListener(e -> jsBuildScaleXStateChanged(e));
                        jPanel16.add(jsBuildScaleX, "cell 3 1");

                        //---- jsBuildScaleY ----
                        jsBuildScaleY.setModel(new SpinnerNumberModel(0.0F, 0.0F, 15.0F, 1.0F));
                        jsBuildScaleY.addChangeListener(e -> jsBuildScaleYStateChanged(e));
                        jPanel16.add(jsBuildScaleY, "cell 3 2");

                        //---- jLabel18 ----
                        jLabel18.setForeground(new Color(0, 153, 0));
                        jLabel18.setText("Scale Y: ");
                        jPanel16.add(jLabel18, "cell 2 2");

                        //---- jLabel19 ----
                        jLabel19.setForeground(new Color(0, 0, 204));
                        jLabel19.setText("Scale Z: ");
                        jPanel16.add(jLabel19, "cell 2 3");

                        //---- jsBuildScaleZ ----
                        jsBuildScaleZ.setModel(new SpinnerNumberModel(0.0F, 0.0F, 15.0F, 1.0F));
                        jsBuildScaleZ.addChangeListener(e -> jsBuildScaleZStateChanged(e));
                        jPanel16.add(jsBuildScaleZ, "cell 3 3");

                        //---- jLabel20 ----
                        jLabel20.setText("*[Note: axis are rotated compared to map editor's axis]");
                        jPanel16.add(jLabel20, "cell 0 4 4 1");
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
    private JSpinner jsBuildID;
    private JButton jbChooseModelBld;
    private JLabel jLabel14;
    private JSpinner jsBuildX;
    private JLabel jLabel15;
    private JSpinner jsBuildY;
    private JLabel jLabel16;
    private JSpinner jsBuildZ;
    private JLabel jLabel17;
    private JSpinner jsBuildScaleX;
    private JSpinner jsBuildScaleY;
    private JLabel jLabel18;
    private JLabel jLabel19;
    private JSpinner jsBuildScaleZ;
    private JLabel jLabel20;
    private JPanel jPanel17;
    private JPanel jPanel18;
    private JButton jbImportBld;
    private JButton jbExportBld;
    private JPanel jPanel19;
    private JButton jbAddBuildBld;
    private JButton jbRemoveBld;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
