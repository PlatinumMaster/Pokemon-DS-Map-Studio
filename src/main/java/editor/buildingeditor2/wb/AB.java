package editor.buildingeditor2.wb;
import utils.BinaryWriter;
import java.io.IOException;
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

    public void replaceModel(int index, NitroModel model) {
        Models.set(index, model);
    }

    public void setIDLookupTable(Map<Short, Integer> IDLookupTable)
    {
        this.IDLookupTable = IDLookupTable;
        ModelToID = InvertKV(IDLookupTable);
    }

    public static <V, K> Map<V, K> InvertKV(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public ABEntry getABEntry(int id) {
        return ABEntries.get(id);
    }

    public ABEntry getABEntryByID(int id) {
        for (ABEntry e : ABEntries)
            if (e.ID == id)
                return e;
        return ABEntries.get(0);
    }

    public int getIDToModel(short ID)
    {
        return IDLookupTable.get(ID) == null ? -1 : IDLookupTable.get(ID);
    }

    public int nModels()
    {
        return this.Models.size();
    }

    public short getModelToID(int ID)
    {
        return ModelToID.get(ID);
    }

    public NitroModel getModel(int index)
    {
        return this.Models.get(index);
    }

    public void Serialize(String output) throws IOException {
        BinaryWriter b = new BinaryWriter(output);
        int base = (ABEntries.size() * 4) + (Models.size() * 4) + 4;
        b.writeUInt16(0x4241); // Magic
        b.writeUInt16(ABEntries.size() + Models.size());

        // Determine the offsets.
        for (int i = 0; i < ABEntries.size(); ++i) {
            b.writeUInt32(base + 4);
            base += ABEntries.get(i).size();
        }

        for (int i = 0; i < Models.size(); ++i) {
            b.writeUInt32(base + 4);
            base += Models.get(i).getData().length;
        }

        // Write fileSize
        b.writeUInt32(base + 4);

        // Write AB entries.
        for (int i = 0; i < ABEntries.size(); ++i) {
            // Write the header.
            ABEntry entry = ABEntries.get(i);
            b.writeUInt16(entry.ID);
            b.writeUInt16(entry.Count);
            b.writeUInt16(entry.DoorID);
            b.writeUInt16(entry.X);
            b.writeUInt16(entry.Y);
            b.writeUInt16(entry.Z);
            b.writeUInt16(entry.unk3);
            b.writeUInt16(entry.unk4);
            b.writeUInt16(entry.ItemsCount);
            b.writeUInt8(entry.Flag);
            b.writeUInt8(entry.AnimCount);

            // Write the file header.
            int ABFileBase = 0x14;
            for (int j = 0; j < entry.numFiles(); ++j) {
                b.writeUInt32(ABFileBase);
                ABFileBase += entry.getFile(j).getData().length;
            }
            for (int j = 4 - entry.numFiles(); j > 0; j--)
                b.writeUInt32(0xFFFFFFFF);

            // Write the files.
            for (int j = 0; j < entry.numFiles(); j++) {
                b.writeBytes(entry.getFile(j).getData());
            }
        }

        // Write the models.
        for (NitroModel nsbmd : Models) {
            b.writeBytes(nsbmd.getData());
        }
        b.close();
        // Done.
    }
}
