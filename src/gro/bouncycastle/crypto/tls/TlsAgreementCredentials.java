package gro.bouncycastle.crypto.tls;

import ewe.io.IOException;
import gro.bouncycastle.crypto.params.AsymmetricKeyParameter;

public interface TlsAgreementCredentials
        extends TlsCredentials {
    byte[] generateAgreement(AsymmetricKeyParameter peerPublicKey)
            throws IOException;
}
