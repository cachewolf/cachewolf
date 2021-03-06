package gro.bouncycastle.crypto.agreement;

import ewe.math.BigInteger;
import gro.bouncycastle.crypto.BasicAgreement;
import gro.bouncycastle.crypto.CipherParameters;
import gro.bouncycastle.crypto.params.ECPrivateKeyParameters;
import gro.bouncycastle.crypto.params.ECPublicKeyParameters;
import gro.bouncycastle.math.ec.ECPoint;

/**
 * P1363 7.2.1 ECSVDP-DH
 *
 * ECSVDP-DH is Elliptic Curve Secret Value Derivation Primitive,
 * Diffie-Hellman version. It is based on the work of [DH76], [Mil86],
 * and [Kob87]. This primitive derives a shared secret value from one
 * party's private key and another party's public key, where both have
 * the same set of EC domain parameters. If two parties correctly
 * execute this primitive, they will produce the same output. This
 * primitive can be invoked by a scheme to derive a shared secret key;
 * specifically, it may be used with the schemes ECKAS-DH1 and
 * DL/ECKAS-DH2. It assumes that the input keys are valid (see also
 * Section 7.2.2).
 */
public class ECDHBasicAgreement
    implements BasicAgreement
{
    private ECPrivateKeyParameters key;

    public void init(
        CipherParameters key)
    {
        this.key = (ECPrivateKeyParameters)key;
    }

    public int getFieldSize()
    {
        return (key.getParameters().getCurve().getFieldSize() + 7) / 8;
    }

    public BigInteger calculateAgreement(
        CipherParameters pubKey)
    {
        ECPublicKeyParameters pub = (ECPublicKeyParameters)pubKey;
        if (!pub.getParameters().equals(key.getParameters()))
        {
            throw new IllegalStateException("ECDH public key has wrong domain parameters");
        }

        ECPoint P = pub.getQ().multiply(key.getD()).normalize();

        if (P.isInfinity())
        {
            throw new IllegalStateException("Infinity is not a valid agreement value for ECDH");
        }

        return P.getAffineXCoord().toBigInteger();
    }
}
