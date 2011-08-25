package org.bouncycastle.util.io;

import ewe.io.IOException;

public class StreamOverflowException
    extends IOException
{
    public StreamOverflowException(String msg)
    {
        super(msg);
    }
}
