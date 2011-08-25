package org.bouncycastle.asn1;

import ewe.io.IOException;
import ewe.io.InputStream;

import org.bouncycastle.util.io.Streams;

public class BEROctetStringParser
    implements ASN1OctetStringParser
{
    private ASN1StreamParser _parser;

    BEROctetStringParser(
        ASN1StreamParser parser)
    {
        _parser = parser;
    }

    public InputStream getOctetStream()
    {
        return new ConstructedOctetStream(_parser);
    }

    public DERObject getLoadedObject()
        throws IOException
    {
        return new BERConstructedOctetString(Streams.readAll(getOctetStream()));
    }

    public DERObject getDERObject()
    {
        try
        {
            return getLoadedObject();
        }
        catch (IOException e)
        {
            throw new ASN1ParsingException("IOException converting stream to byte array: " + e.getMessage(), e);
        }
    }
}
