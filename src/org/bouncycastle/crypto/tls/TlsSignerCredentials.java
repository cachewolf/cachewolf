package org.bouncycastle.crypto.tls;

import ewe.io.IOException;

public interface TlsSignerCredentials extends TlsCredentials
{
    byte[] generateCertificateSignature(byte[] md5andsha1) throws IOException;
}
