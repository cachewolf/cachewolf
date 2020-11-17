package gro.bouncycastle.crypto.tls;

import gro.bouncycastle.crypto.DSA;
import gro.bouncycastle.crypto.params.AsymmetricKeyParameter;
import gro.bouncycastle.crypto.params.ECPublicKeyParameters;
import gro.bouncycastle.crypto.signers.ECDSASigner;
import gro.bouncycastle.crypto.signers.HMacDSAKCalculator;

public class TlsECDSASigner
    extends TlsDSASigner
{
    public boolean isValidPublicKey(AsymmetricKeyParameter publicKey)
    {
        return publicKey instanceof ECPublicKeyParameters;
    }

    
    protected DSA createDSAImpl(short hashAlgorithm)
    {
        return new ECDSASigner(new HMacDSAKCalculator(TlsUtils.createHash(hashAlgorithm)));
    }

    protected short getSignatureAlgorithm()
    {
        return SignatureAlgorithm.ecdsa;
    }
}
