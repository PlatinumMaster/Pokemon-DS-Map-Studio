
package editor.buildingeditor2;

import editor.handler.MapEditorHandler;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * @author Trifindo
 */
public class BuildingEditorChooser {

    public static void loadGame(MapEditorHandler handler) {
        final JFileChooser fc = new JFileChooser();
        if (handler.getLastBuildDirectoryUsed() != null) {
            fc.setCurrentDirectory(new File(handler.getLastBuildDirectoryUsed()));
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setApproveButtonText("Select folder");
        fc.setDialogTitle("Select the game's main folder that was generated with SDSME or similar");
        int returnVal = fc.showOpenDialog(handler.getMainFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String folderPath = fc.getSelectedFile().getPath();
            if (isDPPtFolder(folderPath)) {
                handler.setLastBuildDirectoryUsed(folderPath);
                BuildHandlerDPPt buildHandler = new BuildHandlerDPPt(folderPath);
                final BuildingEditorDialogDPPt dialogDPPt = new BuildingEditorDialogDPPt(handler.getMainFrame());
                dialogDPPt.init(handler, buildHandler);
                dialogDPPt.loadGame(fc.getSelectedFile().getPath());
                dialogDPPt.loadCurrentNsbmd();
                dialogDPPt.updateView();
                dialogDPPt.setLocationRelativeTo(handler.getMainFrame());
                dialogDPPt.setVisible(true);
            } else if (isHGSSFolder(folderPath)) {
                handler.setLastBuildDirectoryUsed(folderPath);
                BuildHandlerHGSS buildHandler = new BuildHandlerHGSS(folderPath);
                final BuildingEditorDialogHGSS dialogHGSS = new BuildingEditorDialogHGSS(handler.getMainFrame());
                dialogHGSS.init(handler, buildHandler);
                dialogHGSS.loadGame(fc.getSelectedFile().getPath());
                dialogHGSS.loadCurrentNsbmd();
                dialogHGSS.updateView();
                dialogHGSS.setLocationRelativeTo(handler.getMainFrame());
                dialogHGSS.setVisible(true);
            } else if (isBWFolder(folderPath)) {
                handler.setLastBuildDirectoryUsed(folderPath);
                BuildHandlerWB buildHandler = new BuildHandlerWB(folderPath);
                final BuildingEditorDialogWB dialogWB = new BuildingEditorDialogWB(handler.getMainFrame());
                dialogWB.init(handler, buildHandler);
                dialogWB.loadGame(fc.getSelectedFile().getPath());
                dialogWB.setLocationRelativeTo(handler.getMainFrame());
                dialogWB.setVisible(true);
                JOptionPane.showMessageDialog(handler.getMainFrame(),
                        "PlatinumMaster will be adding support for this momentarily.",
                        "Coming soon.", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(handler.getMainFrame(),
                        "The selected folder is not the game's main folder generated by SDSME",
                        "Error selecting folder", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static boolean isDPPtFolder(String folderPath) {
        File folder = new File(folderPath + File.separator + "data" + File.separator + "resource");
        return folder.exists();
    }

    private static boolean isHGSSFolder(String folderPath) {
        File folder = new File(folderPath + File.separator + "data" + File.separator + "pbr");
        return folder.exists();
    }

    private static boolean isBWFolder(String folderPath) {
        File folder = new File(folderPath + File.separator + "data" + File.separator + "dl_rom");
        return folder.exists();
    }

}
