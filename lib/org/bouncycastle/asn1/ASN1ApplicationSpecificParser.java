package org.bouncycastle.asn1;

import ewe.io.IOException;

public interface ASN1ApplicationSpecificParser
    extends DEREncodable, InMemoryRepresentable
{
    DEREncodable readObject()
        throws IOException;
}
