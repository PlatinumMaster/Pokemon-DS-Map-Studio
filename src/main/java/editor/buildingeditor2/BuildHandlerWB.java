package editor.buildingeditor2;

import editor.buildingeditor2.animations.BuildAnimations;
import editor.buildingeditor2.animations.MapAnimations;
import editor.buildingeditor2.areabuild.AreaBuildList;
import editor.buildingeditor2.areadata.AreaDataListHGSS;
import editor.buildingeditor2.tileset.BuildTilesetList;
import editor.buildingeditor2.wb.ABEntry;
import editor.buildingeditor2.wb.WBBuildingEntry;
import editor.buildingeditor2.wb.WBBuildingList;
import editor.buildingeditor2.wb.AB;
import editor.game.GameFileSystemB2W2;
import editor.narc2.Narc;
import editor.narc2.NarcIO;
import utils.BinaryReader;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author PlatinumMaster
**/

public class BuildHandlerWB {
    private String gameFolderPath = "";
    private GameFileSystemB2W2 gameFileSystem;
    private WBBuildingList buildingList;

    public BuildHandlerWB(String gameFolderPath) {
        this.gameFolderPath = gameFolderPath;
        this.gameFileSystem = new GameFileSystemB2W2();
    }

    public void loadAllFiles() throws Exception {
        try {
            Narc extBuildingData = NarcIO.loadNarc(getGameFilePath(gameFileSystem.exteriorBuildingPath));
            Narc intBuildingData = NarcIO.loadNarc(getGameFilePath(gameFileSystem.interiorBuildingPath));
            Narc extBuildingTextures = NarcIO.loadNarc(getGameFilePath(gameFileSystem.exteriorBuildingTilesets));
            Narc intBuildingTextures = NarcIO.loadNarc(getGameFilePath(gameFileSystem.interiorBuildingTilesets));
            Narc areaData = NarcIO.loadNarc(getGameFilePath(gameFileSystem.areaDataPath));
        } catch (Exception ex) {
            throw ex;
        }
    }

    public AB parseAB(String path) throws Exception {
        BinaryReader reader = new BinaryReader(path);
        AB ab = new AB();

        ab.magic = (short)reader.readUInt16();
        if (ab.magic != 0x4241)
            throw new Exception(String.format("%s is an invalid AB file!", path));

        short nFiles = (short)reader.readUInt16();
        int[] offsets = new int[nFiles];
        for (int i = 0; i < nFiles; i++)
            offsets[i] = (int)reader.readUInt32();
        int fileSize = (int)reader.readUInt32();

        for (int i = 0; i < nFiles / 2; i++) {
            reader.setPos(offsets[i]);
            ABEntry e = new ABEntry(){
                {
                    id = (short) reader.readUInt16();
                    unk = (short) reader.readUInt16();
                    unk2 = (short) reader.readUInt16();
                    x = (short) reader.readUInt16();
                    y = (short) reader.readUInt16();
                    z = (short) reader.readUInt16();
                    unk3 = (short) reader.readUInt16();
                    unk4 = (short) reader.readUInt16();
                    nItems = (short) reader.readUInt16();
                    flag = (byte) reader.readUInt8();
                    nAnims = (byte) reader.readUInt8();
                    files = new byte[nFiles][];
                }
            };
            int[] entryOffsets = new int[e.nItems];
            for (int j = 0; j < e.nItems; j++)
                entryOffsets[j] = (int)reader.readUInt32();
            for (int j = 0; j < entryOffsets.length; j++)
            {
                int size = entryOffsets[j+1] != 0xFFFFFFFF || entryOffsets[j+1] != 0x7FFFFFFF ?
                        (int)entryOffsets[j+1] - entryOffsets[j] : offsets[i+1] - entryOffsets[j];
                e.files[j] = reader.readBytes(size);
            }
            ab.add(e);
        }

        for (int i = nFiles / 2; i < nFiles; i++)
        {
            byte[] file;
            if (i != nFiles - 1)
                file = reader.readBytes(offsets[i+1] - offsets[i]);
            else
                file = reader.readBytes((int)reader.size() - offsets[i]);
            ab.addModel(file);
        }
        reader.close();
        return ab;
    }

    private String getGameFilePath(String relativePath) {
        return gameFolderPath + File.separator + relativePath;
    }
}
