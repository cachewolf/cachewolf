package gro.bouncycastle.crypto.tls;

import ewe.io.OutputStream;

public interface TlsCompression {
    OutputStream compress(OutputStream output);

    OutputStream decompress(OutputStream output);
}
