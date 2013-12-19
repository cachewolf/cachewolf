package org.bouncycastle.asn1;

import ewe.util.Enumeration;
import ewe.io.IOException;

class LazyDERConstructionEnumeration
    implements Enumeration
{
    private ASN1InputStream aIn;
    private Object          nextObj;

    public LazyDERConstructionEnumeration(byte[] encoded)
    {
        aIn = new ASN1InputStream(encoded, true);
        nextObj = readObject();
    }

    public boolean hasMoreElements()
    {
        return nextObj != null;
    }

    public Object nextElement()
    {
        Object o = nextObj;

        nextObj = readObject();

        return o;
    }

    private Object readObject()
    {
        try
        {
            return aIn.readObject();
        }
        catch (IOException e)
        {
            throw new ASN1ParsingException("malformed DER construction: " + e, e);
        }
    }
}
