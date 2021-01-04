package editor.buildingeditor2.wb;
import java.util.*;

public class AB {
    public short magic;
    public int fileSize;
    private ArrayList<ABEntry> ABEntries;
    private ArrayList<NitroModel> Models;
    private Dictionary<Short, Integer> IDLookupTable;

    public AB()
    {
        ABEntries = new ArrayList<>();
        Models = new ArrayList<>();
        IDLookupTable = new Hashtable<>();
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

    public void setIDLookupTable(Dictionary<Short, Integer> IDLookupTable)
    {
        this.IDLookupTable = IDLookupTable;
    }

    public int getIDToModel(short ID)
    {
        if (IDLookupTable.get(ID) == null)
            return 0;
        return IDLookupTable.get(ID);
    }

    public NitroModel getModel(int index)
    {
        return this.Models.get(index);
    }
}
