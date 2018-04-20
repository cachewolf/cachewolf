package CacheWolf.utils;

import ewe.io.IOException;
import ewe.io.TextCodec;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.Hashtable;

//##################################################################
public class W1252Codec implements TextCodec {
    //##################################################################
    /**
     * This is a creation option. It specifies that CR characters should be removed when
     * encoding text into ASCII.
     **/
    public static final int STRIP_CR_ON_DECODE = 0x1;
    /**
     * This is a creation option. It specifies that CR characters should be removed when
     * decoding text from ASCII.
     **/
    public static final int STRIP_CR_ON_ENCODE = 0x2;
    /**
     * This is a creation option. It specifies that CR characters should be removed when
     * decoding text from ASCII AND encoding text to ASCII.
     **/
    public static final int STRIP_CR = STRIP_CR_ON_DECODE | STRIP_CR_ON_ENCODE;
    private static Hashtable Uni2W1252Table = new Hashtable(64);

    static {
        Uni2W1252Table.put(new Integer(0x20ac), new Integer(128));
        Uni2W1252Table.put(new Integer(0x201a), new Integer(130));

        Uni2W1252Table.put(new Integer(0x0192), new Integer(131));
        Uni2W1252Table.put(new Integer(0x201e), new Integer(132));
        Uni2W1252Table.put(new Integer(0x2026), new Integer(133));
        Uni2W1252Table.put(new Integer(0x2020), new Integer(134));
        Uni2W1252Table.put(new Integer(0x2021), new Integer(135));
        Uni2W1252Table.put(new Integer(0x02c6), new Integer(136));
        Uni2W1252Table.put(new Integer(0x2030), new Integer(137));
        Uni2W1252Table.put(new Integer(0x160), new Integer(138));
        Uni2W1252Table.put(new Integer(0x2039), new Integer(139));
        Uni2W1252Table.put(new Integer(0x152), new Integer(140));

        Uni2W1252Table.put(new Integer(0x017d), new Integer(142));

        Uni2W1252Table.put(new Integer(0x2018), new Integer(145));
        Uni2W1252Table.put(new Integer(0x2019), new Integer(146));
        Uni2W1252Table.put(new Integer(0x201c), new Integer(147));
        Uni2W1252Table.put(new Integer(0x201d), new Integer(148));
        Uni2W1252Table.put(new Integer(0x2022), new Integer(149));
        Uni2W1252Table.put(new Integer(0x2013), new Integer(150));
        Uni2W1252Table.put(new Integer(0x2014), new Integer(151));
        Uni2W1252Table.put(new Integer(0x02dc), new Integer(152));
        Uni2W1252Table.put(new Integer(0x2122), new Integer(153));
        Uni2W1252Table.put(new Integer(0x0161), new Integer(154));
        Uni2W1252Table.put(new Integer(0x203a), new Integer(155));
        Uni2W1252Table.put(new Integer(0x0153), new Integer(156));

        Uni2W1252Table.put(new Integer(0x017e), new Integer(158));
        Uni2W1252Table.put(new Integer(0x0178), new Integer(159));

        Uni2W1252Table.put(new Integer(0x2003), new Integer(32)); //
        Uni2W1252Table.put(new Integer(0x2028), new Integer(32)); //
        Uni2W1252Table.put(new Integer(0x2032), new Integer(39)); //
        Uni2W1252Table.put(new Integer(0x2640), new Integer(119)); // w weiblich
        Uni2W1252Table.put(new Integer(0x2642), new Integer(109)); // m männlich
    }

    private int flags = 0;

    //===================================================================
    public W1252Codec(int options)
    //===================================================================
    {
        flags = options;
    }

    //===================================================================
    public W1252Codec()
    //===================================================================
    {
        this(0);
    }

    //===================================================================
    public ByteArray encodeText(char[] text, int start, int length, boolean endOfData, ByteArray dest) throws IOException
    //===================================================================
    {
        if (dest == null)
            dest = new ByteArray();
        if (dest.data == null || dest.data.length < length)
            dest.data = new byte[length];
        int t = 0;
        for (int i = 0; i < length; i++) {
            if (text[i + start] == 13 && ((flags & STRIP_CR_ON_ENCODE) != 0))
                t--;
            else {
                int ix = text[i + start];
                if (ix > 255) {
                    Integer s = (Integer) Uni2W1252Table.get(new Integer(ix));
                    if (s == null) {
                        // String six = String.valueOf(text[i + start]);
                        dest.data[i + t] = '?';
                    } else {
                        dest.data[i + t] = s.byteValue();
                    }
                } else {
                    dest.data[i + t] = (byte) ix;
                }
            }
        }
        dest.length = length + t;
        return dest;
    }

    //===================================================================
    public CharArray decodeText(byte[] encoded, int start, int length, boolean endOfData, CharArray dest) throws IOException
    //===================================================================
    {
        int toPut = -1;
        if (dest == null)
            dest = new CharArray();
        if (dest.data == null || dest.data.length < length)
            dest.data = new char[length];
        int t = 0;
        for (int i = 0; i < length; i++) {
            if (encoded[i + start] == 13 && ((flags & STRIP_CR_ON_DECODE) != 0))
                t--;
            else {
                dest.data[i + t] = (char) ((int) encoded[i + start] & 0xff);
            }
        }
        dest.length = length + t;
        return dest;
    }

    //===================================================================
    public void closeCodec() throws IOException
    //===================================================================
    {
    }

    //===================================================================
    public Object getCopy()
    //===================================================================
    {
        return new W1252Codec(flags);
    }

    //##################################################################
}
//##################################################################
