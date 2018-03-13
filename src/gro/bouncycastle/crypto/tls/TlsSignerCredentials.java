package gro.bouncycastle.crypto.tls;

import ewe.io.IOException;

public interface TlsSignerCredentials
        extends TlsCredentials {
    byte[] generateCertificateSignature(byte[] hash)
            throws IOException;

    SignatureAndHashAlgorithm getSignatureAndHashAlgorithm();
}
