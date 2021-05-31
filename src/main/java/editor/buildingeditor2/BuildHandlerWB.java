package editor.buildingeditor2;

import editor.buildingeditor2.animations.ModelAnimation;
import editor.buildingeditor2.wb.*;
import editor.game.GameFileSystemB2W2;
import formats.narc2.Narc;
import formats.narc2.NarcFile;
import formats.nsbtx2.Nsbtx2;
import formats.nsbtx2.NsbtxLoader2;
import formats.narc2.NarcIO;
import nitroreader.nsbtx.NSBTX;
import nitroreader.nsbtx.NSBTXreader;
import nitroreader.shared.ByteReader;
import utils.BinaryArrayReader;
import utils.BinaryReader;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private ArrayList<byte[]> extABTextures;
    private ArrayList<byte[]> intABTextures;

    public BuildHandlerWB(String gameFolderPath) {
        this.gameFolderPath = gameFolderPath;
        this.gameFileSystem = new GameFileSystemB2W2();
        extAB = new ArrayList<>();
        intAB = new ArrayList<>();
        extABTextures = new ArrayList<>();
        intABTextures = new ArrayList<>();
        buildingList = new WBBuildingList();
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
                extAB.add(ParseAB(new BinaryArrayReader(n.getData(), 0)));
            for (NarcFile n : intBuildingData.getRoot().getFiles())
                intAB.add(ParseAB(new BinaryArrayReader(n.getData(), 0)));
            for (NarcFile n : extBuildingTextures.getRoot().getFiles())
                extABTextures.add(n.getData());
            for (NarcFile n : intBuildingTextures.getRoot().getFiles())
                intABTextures.add(n.getData());

        } catch (Exception ex) {
            throw ex;
        }
    }

    public byte[] getExtABTextures(int num)
    {
        return extABTextures.get(num);
    }

    public byte[] getIntABTextures(int num)
    {
        return intABTextures.get(num);
    }

    public AB ParseAB(BinaryArrayReader reader) throws Exception {
        Map<Short, Integer> ModelLookupTable = new HashMap<>();
        ArrayList<Short> IDs = new ArrayList<>();
        AB ab = new AB();

        ab.magic = (short) reader.readUInt16();
        if (ab.magic != 0x4241)
            throw new Exception("Invalid AB file!");

        short nFiles = (short) reader.readUInt16();
        ArrayList<Integer> offsets = new ArrayList<>();

        for (int i = 0; i < nFiles; i++)
            offsets.add((int) reader.readUInt32());

        int fileSize = (int) reader.readUInt32();

        for (int i = 0; i < nFiles / 2; i++) {
            ArrayList<Integer> fileOffsets = new ArrayList<>();
            int startABEntrySection = offsets.get(i) + 0x14;
            reader.jumpAbs(offsets.get(i));
            ABEntry e = new ABEntry() {{
                    ID = (short) reader.readUInt16();
                    Count = (short) reader.readUInt16();
                    DoorID = (short) reader.readUInt16();
                    X = (short) reader.readUInt16();
                    Y = (short) reader.readUInt16();
                    Z = (short) reader.readUInt16();
                    unk3 = (short) reader.readUInt16();
                    unk4 = (short) reader.readUInt16();
                    ItemsCount = (short) reader.readUInt16();
                    Flag = (byte) reader.readUInt8();
                    AnimCount = (byte) reader.readUInt8();
            }};
            IDs.add(e.ID);

            while (reader.peekUInt32() != -1 && fileOffsets.size() < 4)
                fileOffsets.add(startABEntrySection + (int) reader.readUInt32());

            for (int j = 0; j < fileOffsets.size(); j++)
            {
                reader.jumpAbs(fileOffsets.get(j) + 0x4);
                int subFileSize = (int) reader.peekUInt32();
                reader.jumpAbs(fileOffsets.get(j) - 0x4);
                e.addFile(new ModelAnimation(reader.readBytes(subFileSize), 0));
            }
            ab.add(e);
        }

        for (int i = nFiles / 2; i < nFiles; i++)
        {
            ModelLookupTable.put(IDs.get(nFiles - i - 1), nFiles - i - 1);
            reader.jumpAbs(offsets.get(i) + 0x8);
            int subFileSize = (int) reader.peekUInt32();
            reader.jumpAbs(offsets.get(i));
            ab.addModel(new NitroModel(reader.readBytes(subFileSize)));
        }
        ab.setIDLookupTable(ModelLookupTable);
        return ab;
    }

    public void loadBuildingData(Path path)
    {
        try {
            buildingList = parseBLD(path);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public WBBuildingList parseBLD(Path input) throws Exception
    {
        BinaryReader reader = new BinaryReader(input.toString());
        if (reader.size() == 0)
            throw new Exception("Invalid BLD!");

        WBBuildingList bldList = new WBBuildingList();
        int count = (int) reader.readUInt32();
        for (int i = 0; i < count; i++)
        {
            WBBuildingEntry e = new WBBuildingEntry()
            {
                {
                    coords = new FX32[] {
                            new FX32((int) reader.readUInt32()),
                            new FX32((int) reader.readUInt32()),
                            new FX32((int) reader.readUInt32())
                    };
                    System.out.println(coords[2].GetValue());
                    rotation = (short) reader.readUInt16();
                    id = (short) ((reader.readUInt8() << 0x8) + reader.readUInt8()); // Reverse endian. GameFreak = trolls
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
            buf.putInt(getBuildingList().get(i).coords[0].GetValue());
            buf.putInt(getBuildingList().get(i).coords[1].GetValue());
            buf.putInt(getBuildingList().get(i).coords[2].GetValue());
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