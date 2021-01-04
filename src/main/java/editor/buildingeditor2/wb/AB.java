package editor.buildingeditor2.wb;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.*;
import java.util.stream.Collectors;

public class AB {
    public short magic;
    public int fileSize;
    private ArrayList<ABEntry> ABEntries;
    private ArrayList<NitroModel> Models;
    private Map<Short, Integer> IDLookupTable;
    private Map<Integer, Short> ModelToID;

    public AB()
    {
        ABEntries = new ArrayList<>();
        Models = new ArrayList<>();
        IDLookupTable = new HashMap<>();
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

    public void setIDLookupTable(Map<Short, Integer> IDLookupTable)
    {
        this.IDLookupTable = IDLookupTable;
        ModelToID = invert(IDLookupTable);
    }

    public static <V, K> Map<V, K> invert(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public int getIDToModel(short ID)
    {
        if (IDLookupTable.get(ID) == null)
            return -1;
        return IDLookupTable.get(ID);
    }

    public short getModelToID(int ID)
    {
        return ModelToID.get(ID);
    }

    public NitroModel getModel(int index)
    {
        return this.Models.get(index);
    }
}
