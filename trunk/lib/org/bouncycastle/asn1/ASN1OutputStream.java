package org.bouncycastle.asn1;

import ewe.io.IOException;
import ewe.io.OutputStream;

public class ASN1OutputStream
    extends DEROutputStream
{
    public ASN1OutputStream(
        OutputStream    os)
    {
        super(os);
    }

    public void writeObject(
        Object    obj)
        throws IOException
    {
        if (obj == null)
        {
            writeNull();
        }
        else if (obj instanceof DERObject)
        {
            ((DERObject)obj).encode(this);
        }
        else if (obj instanceof DEREncodable)
        {
            ((DEREncodable)obj).getDERObject().encode(this);
        }
        else
        {
            throw new IOException("object not ASN1Encodable");
        }
    }
}
