package editor.buildingeditor2.wb;

import java.util.Vector;

public class WBBuildingEntry {
    FX32[] coords;
    short rotation;
    short id;

    public WBBuildingEntry(FX32[] coords, short rotation, short id) {
        this.coords = coords;
        this.rotation = rotation;
        this.id = id;
    }

    public void setCoords(FX32[] newCoords)
    {
        this.coords = newCoords;
    }

    public FX32[] getCoords() {
        return coords;
    }

    public void setCoord(int index, FX32 newVal) {
        if (index >= 2 || index < 0)
            throw new IndexOutOfBoundsException();

        coords[index] = newVal;
    }

    public void setID(short id)
    {
        this.id = id;
    }

    public short getID()
    {
        return this.id;
    }

    public void setRotation(short rotation)
    {
        this.rotation = rotation;
    }

    public short getRotation()
    {
        return this.rotation;
    }
}
