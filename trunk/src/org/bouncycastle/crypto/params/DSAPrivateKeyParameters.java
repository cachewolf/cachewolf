package org.bouncycastle.crypto.params;

import ewe.math.BigInteger;

public class DSAPrivateKeyParameters
    extends DSAKeyParameters
{
    private BigInteger      x;

    public DSAPrivateKeyParameters(
        BigInteger      x,
        DSAParameters   params)
    {
        super(true, params);

        this.x = x;
    }   

    public BigInteger getX()
    {
        return x;
    }
}
