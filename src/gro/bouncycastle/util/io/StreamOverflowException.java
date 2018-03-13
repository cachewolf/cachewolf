package gro.bouncycastle.util.io;

import ewe.io.IOException;

/**
 * Exception thrown when too much data is written to an InputStream
 */
public class StreamOverflowException
        extends IOException {
    public StreamOverflowException(String msg) {
        super(msg);
    }
}
