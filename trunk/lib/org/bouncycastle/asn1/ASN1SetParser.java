package org.bouncycastle.asn1;

import ewe.io.IOException;

public interface ASN1SetParser
    extends DEREncodable, InMemoryRepresentable
{
    public DEREncodable readObject()
        throws IOException;
}
