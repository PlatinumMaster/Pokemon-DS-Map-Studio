package editor.buildingeditor2;

import editor.buildingeditor2.animations.ModelAnimation;
import editor.buildingeditor2.wb.*;
import editor.game.GameFileSystemB2W2;
import formats.narc2.Narc;
import formats.narc2.NarcFile;
import formats.nsbtx2.Nsbtx2;
import formats.nsbtx2.NsbtxLoader2;
import formats.narc2.Narc;
import formats.narc2.NarcIO;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author PlatinumMaster
 **/

public class BuildHandlerWB {
    private String gameFolderPath = "";
    private GameFileSystemB2W2 gameFileSystem;
    private WBBuildingList buildingList;
    private ArrayList<AB> extAB;
    private ArrayList<AB> intAB;
    private ArrayList<Nsbtx2> extABTextures;
    private ArrayList<Nsbtx2> intABTextures;

    public BuildHandlerWB(String gameFolderPath) {
        this.gameFolderPath = gameFolderPath;
        this.gameFileSystem = new GameFileSystemB2W2();
        extAB = new ArrayList<>();
        intAB = new ArrayList<>();
        extABTextures = new ArrayList<>();
        intABTextures = new ArrayList<>();

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
            for (NarcFile n : extBuildingTextures.getRoot().getFiles())
                extABTextures.add(NsbtxLoader2.loadNsbtx(n.getData()));
            for (NarcFile n : intBuildingTextures.getRoot().getFiles())
                intABTextures.add(NsbtxLoader2.loadNsbtx(n.getData()));

        } catch (Exception ex) {
            throw ex;
        }
    }

    public Nsbtx2 getExtABTextures(int num)
    {
        return extABTextures.get(num);
    }

    public Nsbtx2 getIntABTextures(int num)
    {
        return intABTextures.get(num);
    }

    public AB parseAB(byte[] str) throws Exception {
        Dictionary<Short, Integer> ModelLookupTable = new Hashtable<>();
        ArrayList<Short> IDs = new ArrayList<>();
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
            IDs.add(e.id);
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
            ModelLookupTable.put(IDs.get(nFiles - i - 1), nFiles - i - 1);
            reader.position(offsets.get(i));
            byte[] file;
            if (i == nFiles - 1)
                file = new byte[fileSize - offsets.get(i)];
            else
                file = new byte[offsets.get(i+1) - offsets.get(i)];
            reader.get(file);
            ab.addModel(new NitroModel(file));
        }
        ab.setIDLookupTable(ModelLookupTable);
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
                    System.out.println(coords[2].Value());
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

    public byte[] exportBLD(WBBuildingList l)
    {
        ByteBuffer buf = ByteBuffer.allocate(0x4 + (0x10 * l.size()));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(l.size()); // Write the number of entries
        for (int i = 0; i < l.size(); i++)
        {
            buf.putInt(getBuildingList().get(i).coords[0].Value());
            buf.putInt(getBuildingList().get(i).coords[1].Value());
            buf.putInt(getBuildingList().get(i).coords[2].Value());
            buf.putShort(getBuildingList().get(i).rotation);
            buf.order(ByteOrder.BIG_ENDIAN);
            buf.putShort(getBuildingList().get(i).id);
            buf.order(ByteOrder.LITTLE_ENDIAN);
        }
        return buf.array();
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