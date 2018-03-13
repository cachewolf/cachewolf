package gro.bouncycastle.crypto.tls;

import ewe.io.OutputStream;

public class TlsNullCompression
        implements TlsCompression {
    public OutputStream compress(OutputStream output) {
        return output;
    }

    public OutputStream decompress(OutputStream output) {
        return output;
    }
}
