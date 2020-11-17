package gro.bouncycastle.crypto.signers;

import ewe.io.IOException;
import ewe.math.BigInteger;
import gro.bouncycastle.asn1.ASN1EncodableVector;
import gro.bouncycastle.asn1.ASN1Encoding;
import gro.bouncycastle.asn1.ASN1Integer;
import gro.bouncycastle.asn1.ASN1Primitive;
import gro.bouncycastle.asn1.ASN1Sequence;
import gro.bouncycastle.asn1.DERSequence;
import gro.bouncycastle.crypto.CipherParameters;
import gro.bouncycastle.crypto.DSA;
import gro.bouncycastle.crypto.Digest;
import gro.bouncycastle.crypto.Signer;
import gro.bouncycastle.crypto.params.AsymmetricKeyParameter;
import gro.bouncycastle.crypto.params.ParametersWithRandom;

public class DSADigestSigner
    implements Signer
{
    private final Digest digest;
    private final DSA dsaSigner;
    private boolean forSigning;

    public DSADigestSigner(
        DSA    signer,
        Digest digest)
    {
        this.digest = digest;
        this.dsaSigner = signer;
    }

    public void init(
        boolean           forSigning,
        CipherParameters   parameters)
    {
        this.forSigning = forSigning;

        AsymmetricKeyParameter k;

        if (parameters instanceof ParametersWithRandom)
        {
            k = (AsymmetricKeyParameter)((ParametersWithRandom)parameters).getParameters();
        }
        else
        {
            k = (AsymmetricKeyParameter)parameters;
        }

        if (forSigning && !k.isPrivate())
        {
            throw new IllegalArgumentException("Signing Requires Private Key.");
        }

        if (!forSigning && k.isPrivate())
        {
            throw new IllegalArgumentException("Verification Requires Public Key.");
        }

        reset();

        dsaSigner.init(forSigning, parameters);
    }

    /**
     * update the internal digest with the byte b
     */
    public void update(
        byte input)
    {
        digest.update(input);
    }

    /**
     * update the internal digest with the byte array in
     */
    public void update(
        byte[]  input,
        int     inOff,
        int     length)
    {
        digest.update(input, inOff, length);
    }

    /**
     * Generate a signature for the message we've been loaded with using
     * the key we were initialised with.
     */
    public byte[] generateSignature()
    {
        if (!forSigning)
        {
            throw new IllegalStateException("DSADigestSigner not initialised for signature generation.");
        }

        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);

        BigInteger[] sig = dsaSigner.generateSignature(hash);

        try
        {
            return derEncode(sig[0], sig[1]);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("unable to encode signature");
        }
    }

    public boolean verifySignature(
        byte[] signature)
    {
        if (forSigning)
        {
            throw new IllegalStateException("DSADigestSigner not initialised for verification");
        }

        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);

        try
        {
            BigInteger[] sig = derDecode(signature);
            return dsaSigner.verifySignature(hash, sig[0], sig[1]);
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public void reset()
    {
        digest.reset();
    }

    private byte[] derEncode(
        BigInteger  r,
        BigInteger  s)
        throws IOException
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(r));
        v.add(new ASN1Integer(s));

        return new DERSequence(v).getEncoded(ASN1Encoding.DER);
    }

    private BigInteger[] derDecode(
        byte[] encoding)
        throws IOException
    {
        ASN1Sequence s = (ASN1Sequence)ASN1Primitive.fromByteArray(encoding);

        return new BigInteger[]
        {
            ((ASN1Integer)s.getObjectAt(0)).getValue(),
            ((ASN1Integer)s.getObjectAt(1)).getValue()
        };
    }
}
