package gro.bouncycastle.crypto.tls;

import ewe.io.ByteArrayOutputStream;
import gro.bouncycastle.crypto.Signer;

class SignerInputBuffer extends ByteArrayOutputStream {
    void updateSigner(Signer s) {
        s.update(this.buf, 0, count);
    }
}
