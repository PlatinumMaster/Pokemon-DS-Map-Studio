package editor.buildingeditor2.wb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class NitroModel {
    private ByteBuffer buf;
    public NitroModel(byte[] buf)
    {
        this.buf = ByteBuffer.wrap(buf);
        this.buf.order(ByteOrder.LITTLE_ENDIAN);
        this.buf.position(0x0);
    }

    public String getName()
    {
        this.buf.position(0x34);
        // Ugly hack. I wish ByteBuffer let you just read null-terminated strings
        String str = "";
        for (char cur = (char)this.buf.get(); cur != '\0'; cur = (char)this.buf.get())
            str += cur;
        return str;
    }

    public byte[] getData()
    {
        return this.buf.array();
    }
}
