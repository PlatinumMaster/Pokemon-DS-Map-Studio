package editor.buildingeditor2.wb;

public class FX32 {
    private int val;

    public FX32(int num)
    {
        this.val = num;
    }

    public int GetValue()
    {
        return val;
    }

    public float GetValueAsFloat()
    {
        return val * 4096;
    }

    public void SetValue(int val)
    {
        this.val = val;
    }

    public static int TryParse(float val)
    {
        return (int) (val / 4096);
    }
}
