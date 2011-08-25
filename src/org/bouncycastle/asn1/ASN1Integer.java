package org.bouncycastle.asn1;

import ewe.math.BigInteger;

public class ASN1Integer
    extends DERInteger
{
    ASN1Integer(byte[] bytes)
    {
        super(bytes);
    }

    public ASN1Integer(BigInteger value)
    {
        super(value);
    }

    public ASN1Integer(int value)
    {
        super(value);
    }
}
