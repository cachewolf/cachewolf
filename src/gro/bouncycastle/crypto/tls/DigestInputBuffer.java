package gro.bouncycastle.crypto.tls;

import ewe.io.ByteArrayOutputStream;

import gro.bouncycastle.crypto.Digest;

class DigestInputBuffer extends ByteArrayOutputStream
{
    void updateDigest(Digest d)
    {
        d.update(this.buf, 0, count);
    }
}
