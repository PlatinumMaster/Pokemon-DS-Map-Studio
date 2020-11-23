package editor.buildingeditor2.wb;
import java.util.ArrayList;

public class WBBuildingList {
    private ArrayList<WBBuildingEntry> buildings;

    public WBBuildingList()
    {
        buildings = new ArrayList<WBBuildingEntry>();
    }

    public void add(WBBuildingEntry newEntry)
    {
        buildings.add(newEntry);
    }

    public void remove(int index)
    {
        buildings.remove(index);
    }
}
