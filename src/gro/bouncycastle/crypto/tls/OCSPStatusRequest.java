package gro.bouncycastle.crypto.tls;

import ewe.io.ByteArrayInputStream;
import ewe.io.ByteArrayOutputStream;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.OutputStream;
import ewe.util.Vector;
import gro.bouncycastle.asn1.ASN1Encoding;
import gro.bouncycastle.asn1.ocsp.ResponderID;
import gro.bouncycastle.asn1.x509.Extensions;
import gro.bouncycastle.util.io.Streams;

/**
 * RFC 3546 3.6
 */
public class OCSPStatusRequest
{
    protected Vector responderIDList;
    protected Extensions requestExtensions;

    /**
     * @param responderIDList
     *            a {@link Vector} of {@link ResponderID}, specifying the list of trusted OCSP
     *            responders. An empty list has the special meaning that the responders are
     *            implicitly known to the server - e.g., by prior arrangement.
     * @param requestExtensions
     *            OCSP request extensions. A null value means that there are no extensions.
     */
    public OCSPStatusRequest(Vector responderIDList, Extensions requestExtensions)
    {
        this.responderIDList = responderIDList;
        this.requestExtensions = requestExtensions;
    }

    /**
     * @return a {@link Vector} of {@link ResponderID}
     */
    public Vector getResponderIDList()
    {
        return responderIDList;
    }

    /**
     * @return OCSP request extensions
     */
    public Extensions getRequestExtensions()
    {
        return requestExtensions;
    }

    /**
     * Encode this {@link OCSPStatusRequest} to an {@link OutputStream}.
     * 
     * @param output
     *            the {@link OutputStream} to encode to.
     * @throws IOException
     */
    public void encode(OutputStream output) throws IOException
    {
        if (responderIDList == null || responderIDList.isEmpty())
        {
            TlsUtils.writeUint16(0, output);
        }
        else
        {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            for (int i = 0; i < responderIDList.size(); ++i)
            {
                ResponderID responderID = (ResponderID) responderIDList.elementAt(i);
                byte[] derEncoding = responderID.getEncoded(ASN1Encoding.DER);
                TlsUtils.writeOpaque16(derEncoding, buf);
            }
            TlsUtils.checkUint16(buf.size());
            TlsUtils.writeUint16(buf.size(), output);
            Streams.writeBufTo(buf, output);
        }

        throw new UnsupportedClassVersionError();
        /*
        
        if (requestExtensions == null)
        {
            TlsUtils.writeUint16(0, output);
        }
        else
        {
            byte[] derEncoding = requestExtensions.getEncoded(ASN1Encoding.DER);
            TlsUtils.checkUint16(derEncoding.length);
            TlsUtils.writeUint16(derEncoding.length, output);
            output.write(derEncoding);
        }
*/    }

    /**
     * Parse an {@link OCSPStatusRequest} from an {@link InputStream}.
     * 
     * @param input
     *            the {@link InputStream} to parse from.
     * @return an {@link OCSPStatusRequest} object.
     * @throws IOException
     */
    public static OCSPStatusRequest parse(InputStream input) throws IOException
    {
        throw new UnsupportedClassVersionError();/*
        Vector responderIDList = new Vector();
        {
            int length = TlsUtils.readUint16(input);
            if (length > 0)
            {
                byte[] data = TlsUtils.readFully(length, input);
                ByteArrayInputStream buf = new ByteArrayInputStream(data);
                do
                {
                    byte[] derEncoding = TlsUtils.readOpaque16(buf);
                    ResponderID responderID = ResponderID.getInstance(TlsUtils.readDERObject(derEncoding));
                    responderIDList.addElement(responderID);
                }
                while (buf.available() > 0);
            }
        }

        Extensions requestExtensions = null;
        {
            int length = TlsUtils.readUint16(input);
            if (length > 0)
            {
                byte[] derEncoding = TlsUtils.readFully(length, input);
                requestExtensions = Extensions.getInstance(TlsUtils.readDERObject(derEncoding));
            }
        }

        return new OCSPStatusRequest(responderIDList, requestExtensions);
*/    }
}
