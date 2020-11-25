package editor.buildingeditor2.wb;
import java.util.ArrayList;

public class AB {
    public short magic;
    public int fileSize;
    private ArrayList<ABEntry> ABEntries;
    private ArrayList<byte[]> Models;

    public AB()
    {
        ABEntries = new ArrayList<>();
        Models = new ArrayList<>();
    }

    public void add(ABEntry newEntry)
    {
        ABEntries.add(newEntry);
    }

    public void remove(int index)
    {
        ABEntries.remove(index);
    }

    public void addModel(byte[] model)
    {
        Models.add(model);
    }
}
