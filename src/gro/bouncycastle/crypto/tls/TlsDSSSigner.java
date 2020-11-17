package gro.bouncycastle.crypto.tls;

import gro.bouncycastle.crypto.DSA;
import gro.bouncycastle.crypto.params.AsymmetricKeyParameter;
import gro.bouncycastle.crypto.params.DSAPublicKeyParameters;
import gro.bouncycastle.crypto.signers.DSASigner;
import gro.bouncycastle.crypto.signers.HMacDSAKCalculator;

public class TlsDSSSigner
    extends TlsDSASigner
{
    public boolean isValidPublicKey(AsymmetricKeyParameter publicKey)
    {
        return publicKey instanceof DSAPublicKeyParameters;
    }

    protected DSA createDSAImpl(short hashAlgorithm)
    {
        return new DSASigner(new HMacDSAKCalculator(TlsUtils.createHash(hashAlgorithm)));
    }

    protected short getSignatureAlgorithm()
    {
        return SignatureAlgorithm.dsa;
    }
}
