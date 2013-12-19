package org.bouncycastle.crypto.tls;

import ewe.io.IOException;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public interface TlsAgreementCredentials extends TlsCredentials
{
    byte[] generateAgreement(AsymmetricKeyParameter serverPublicKey) throws IOException;
}
