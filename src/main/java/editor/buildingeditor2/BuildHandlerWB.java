package editor.buildingeditor2;

import editor.buildingeditor2.animations.BuildAnimations;
import editor.buildingeditor2.animations.MapAnimations;
import editor.buildingeditor2.animations.ModelAnimation;
import editor.buildingeditor2.areabuild.AreaBuildList;
import editor.buildingeditor2.areadata.AreaDataListHGSS;
import editor.buildingeditor2.tileset.BuildTilesetList;
import editor.buildingeditor2.wb.*;
import editor.game.GameFileSystemB2W2;
import editor.narc2.Narc;
import editor.narc2.NarcFile;
import editor.narc2.NarcIO;
import utils.BinaryReader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Dictionary;

/**
 * @author PlatinumMaster
 **/

public class BuildHandlerWB {
    private String gameFolderPath = "";
    private GameFileSystemB2W2 gameFileSystem;
    private WBBuildingList buildingList;
    private ArrayList<AB> extAB;
    private ArrayList<AB> intAB;

    public BuildHandlerWB(String gameFolderPath) {
        this.gameFolderPath = gameFolderPath;
        this.gameFileSystem = new GameFileSystemB2W2();
        extAB = new ArrayList<>();
        intAB = new ArrayList<>();
    }

    public WBBuildingList getBuildingList()
    {
        return this.buildingList;
    }

    public AB getExtAB(int number)
    {
        return this.extAB.get(number);
    }

    public AB getIntAB(int number)
    {
        return this.intAB.get(number);
    }

    public void loadAllFiles() throws Exception {
        try {
            Narc extBuildingData = NarcIO.loadNarc(getGameFilePath(gameFileSystem.exteriorBuildingPath));
            Narc intBuildingData = NarcIO.loadNarc(getGameFilePath(gameFileSystem.interiorBuildingPath));
            Narc extBuildingTextures = NarcIO.loadNarc(getGameFilePath(gameFileSystem.exteriorBuildingTilesets));
            Narc intBuildingTextures = NarcIO.loadNarc(getGameFilePath(gameFileSystem.interiorBuildingTilesets));
            for (NarcFile n : extBuildingData.getRoot().getFiles())
                extAB.add(parseAB(n.getData()));
            for (NarcFile n : intBuildingData.getRoot().getFiles())
                intAB.add(parseAB(n.getData()));
        } catch (Exception ex) {
            throw ex;
        }
    }


    public AB parseAB(byte[] str) throws Exception {
        ByteBuffer reader = ByteBuffer.wrap(str);
        reader.order(ByteOrder.LITTLE_ENDIAN);
        AB ab = new AB();

        ab.magic = reader.getShort();
        if (ab.magic != 0x4241)
            throw new Exception("Invalid AB file!");

        short nFiles = reader.getShort();
        ArrayList<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < nFiles; i++)
            offsets.add(reader.getInt());
        int fileSize = reader.getInt();
        for (int i = 0; i < nFiles / 2; i++) {
            reader.position(offsets.get(i));
            int startABEntrySection = reader.position() + 0x14;
            ABEntry e = new ABEntry(){
                {
                    id = reader.getShort();
                    unk = reader.getShort();
                    unk2 = reader.getShort();
                    x = reader.getShort();
                    y = reader.getShort();
                    z = reader.getShort();
                    unk3 = reader.getShort();
                    unk4 = reader.getShort();
                    nItems = reader.getShort();
                    flag = reader.get();
                    nAnims = reader.get();
                }
            };
            ArrayList<Integer> fileOffsets = new ArrayList<>();
            int k = 0;
            while (k < 4)
            {
                int val = reader.getInt();
                if (val == 0xFFFFFFFF)
                    break;
                fileOffsets.add(startABEntrySection + val);
                k++;
            }
            e.files = new ModelAnimation[fileOffsets.size()];
            for (int j = 0; j < fileOffsets.size(); j++)
            {
                reader.position(fileOffsets.get(j));
                byte[] buf;
                if (j == fileOffsets.size() - 1)
                    buf = new byte[offsets.get(i+1) - fileOffsets.get(j)];
                else
                    buf = new byte[fileOffsets.get(j+1) - fileOffsets.get(j)];
                k = 0;
                while (reader.position() != fileOffsets.get(j) + e.files.length - 1) {
                    buf[k] = reader.get();
                    k++;
                }
                e.files[j] = new ModelAnimation(buf, j);
            }
            ab.add(e);
        }

        for (int i = nFiles / 2; i < nFiles; i++)
        {
            reader.position(offsets.get(i));
            byte[] file;
            if (i == nFiles - 1)
                file = new byte[fileSize - offsets.get(i)];
            else
                file = new byte[offsets.get(i+1) - offsets.get(i)];
            reader.get(file);
            ab.addModel(new NitroModel(file));
        }
        return ab;
    }

    public void loadBuildingData(Path path)
    {
        try {
            byte[] bldFile = Files.readAllBytes(path);
            buildingList = parseBLD(bldFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public WBBuildingList parseBLD(byte[] bld) throws Exception
    {
        if (bld == null || bld.length == 0)
            throw new Exception("Invalid BLD!");

        ByteBuffer reader = ByteBuffer.wrap(bld);
        reader.order(ByteOrder.LITTLE_ENDIAN);
        WBBuildingList bldList = new WBBuildingList();

        int count = reader.getInt();
        for (int i = 0; i < count; i++)
        {
            WBBuildingEntry e = new WBBuildingEntry()
            {
                {
                    coords = new FX32[]{ new FX32(reader.getInt()),  new FX32(reader.getInt()), new FX32(reader.getInt()) };
                    rotation = reader.getShort();
                    reader.order(ByteOrder.BIG_ENDIAN);
                    id = reader.getShort(); // GameFreak = trolls
                    reader.order(ByteOrder.LITTLE_ENDIAN);
                }
            };
            bldList.add(e);
        }

        return bldList;
    }

    public void setGameFolderPath(String path) {
        this.gameFolderPath = path;
    }

    public String getGameFolderPath() {
        return gameFolderPath;
    }

    protected String getGameFilePath(String relativePath) {
        return gameFolderPath + File.separator + relativePath;
    }
}