package editor.buildingeditor2.wb;

public class FX32 {
    private int val;

    public FX32(int num)
    {
        this.val = num;
    }

    public int Value()
    {
        return val;
    }

    public float toFloat()
    {
        return ((short)(val >> 0x10)) + ((val & 0xFFFF) / 65536f);
    }

    public void setVal(int val)
    {
        this.val = val;
    }

    public static int TryParse(float val)
    {
        // (uint16_t)((n - floor(n)) * 0x10000) + floor(n)
        double frac = val - Math.floor(val);
        int integer = ((int)Math.floor(val)) << 0x10;
        return (int)(frac * 0x10000) + integer;
    }
}
