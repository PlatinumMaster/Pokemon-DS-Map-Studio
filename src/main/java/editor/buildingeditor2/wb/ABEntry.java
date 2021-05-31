package editor.buildingeditor2.wb;

import editor.buildingeditor2.animations.ModelAnimation;
import nitroreader.nsbmd.NSBMD;

import java.util.ArrayList;
import java.util.List;

public class ABEntry {
    public short ID;
    public short Count;
    public short DoorID;
    public short X;
    public short Y;
    public short Z;
    public short unk3;
    public short unk4;
    public short ItemsCount;
    public byte Flag;
    public byte AnimCount;
    private List<ModelAnimation> files = new ArrayList<>();

    public long size() {
        int fileSize = 0;
        for (ModelAnimation ma : files)
            fileSize += ma.getData().length;
        return 0x24 + fileSize;
    }

    public int numFiles() {
        return files.size();
    }

    public ModelAnimation getFile(int index) {
        return files.get(index);
    }

    public void addFile(ModelAnimation file) throws Exception {
        if (files.size() == 4)
            throw new Exception("The maximum number of files is 4.");
        files.add(file);
    }

    public void removeFile(int index) throws Exception {
        if (files.size() == 0)
            throw new Exception("There are no files in this entry.");
        if (index > 4 || index < 0)
            throw new Exception("Not a valid index.");
        files.remove(index);
    }
}
