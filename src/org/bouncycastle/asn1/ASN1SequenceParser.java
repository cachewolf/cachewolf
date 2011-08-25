package org.bouncycastle.asn1;

import ewe.io.IOException;

public interface ASN1SequenceParser
    extends DEREncodable, InMemoryRepresentable
{
    DEREncodable readObject()
        throws IOException;
}
