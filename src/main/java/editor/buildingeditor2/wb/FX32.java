package editor.buildingeditor2.wb;

public class FX32 {
    private double val;
    public FX32(int num)
    {
        val = (short) (num >> 16) + (short)(num & 0x10000);
    }

    public double Value()
    {
        return val;
    }

    public float toFloat()
    {
        return ((int)Math.floor(val) & 0xFFFF) + (short)((val - Math.floor(val)) * (0x10000));
    }

    public void setValue(double val)
    {
        this.val = val;
    }
}
