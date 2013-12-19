package org.bouncycastle.crypto.tls;

import ewe.io.IOException;

public interface TlsCipherFactory
{
    /**
     * See enumeration classes EncryptionAlgorithm and DigestAlgorithm for appropriate argument values
     */
    TlsCipher createCipher(TlsClientContext context, int encryptionAlgorithm, int digestAlgorithm) throws IOException;
}
