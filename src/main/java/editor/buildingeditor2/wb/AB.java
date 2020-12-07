package editor.buildingeditor2.wb;
import java.util.ArrayList;

public class AB {
    public short magic;
    public int fileSize;
    private ArrayList<ABEntry> ABEntries;
    private ArrayList<NitroModel> Models;

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

    public void addModel(NitroModel model)
    {
        Models.add(model);
    }

    public NitroModel getModel(int index)
    {
        return this.Models.get(index);
    }
}
