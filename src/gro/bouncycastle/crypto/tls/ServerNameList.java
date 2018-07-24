package gro.bouncycastle.crypto.tls;

import ewe.io.ByteArrayInputStream;
import ewe.io.ByteArrayOutputStream;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.OutputStream;
import ewe.util.Vector;
import gro.bouncycastle.util.Arrays;
import gro.bouncycastle.util.io.Streams;

public class ServerNameList
{
    protected Vector serverNameList;

    /**
     * @param serverNameList a {@link Vector} of {@link ServerName}.
     */
    public ServerNameList(Vector serverNameList)
    {
        if (serverNameList == null)
        {
            throw new IllegalArgumentException("'serverNameList' must not be null");
        }

        this.serverNameList = serverNameList;
    }

    /**
     * @return a {@link Vector} of {@link ServerName}.
     */
    public Vector getServerNameList()
    {
        return serverNameList;
    }

    /**
     * Encode this {@link ServerNameList} to an {@link OutputStream}.
     * 
     * @param output
     *            the {@link OutputStream} to encode to.
     * @throws IOException
     */
    public void encode(OutputStream output) throws IOException
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        short[] nameTypesSeen = new short[0];
        for (int i = 0; i < serverNameList.size(); ++i)
        {
            ServerName entry = (ServerName)serverNameList.elementAt(i);

            nameTypesSeen = checkNameType(nameTypesSeen, entry.getNameType());
            if (nameTypesSeen == null)
            {
            	throw new UnsupportedClassVersionError();/*
                throw new TlsFatalAlert(AlertDescription.internal_error);
*/            }

        	throw new UnsupportedClassVersionError();/*
            entry.encode(buf);
*/        }
        throw new UnsupportedClassVersionError();/*

        TlsUtils.checkUint16(buf.size());
        TlsUtils.writeUint16(buf.size(), output);
        Streams.writeBufTo(buf, output);
*/    }

    /**
     * Parse a {@link ServerNameList} from an {@link InputStream}.
     * 
     * @param input
     *            the {@link InputStream} to parse from.
     * @return a {@link ServerNameList} object.
     * @throws IOException
     */
    public static ServerNameList parse(InputStream input) throws IOException
    {
    	throw new UnsupportedClassVersionError();/*
        int length = TlsUtils.readUint16(input);
        if (length < 1)
        {
            throw new TlsFatalAlert(AlertDescription.decode_error);
        }

        byte[] data = TlsUtils.readFully(length, input);

        ByteArrayInputStream buf = new ByteArrayInputStream(data);

        short[] nameTypesSeen = new short[0];
        Vector server_name_list = new Vector();
        while (buf.available() > 0)
        {
            ServerName entry = ServerName.parse(buf);

            nameTypesSeen = checkNameType(nameTypesSeen, entry.getNameType());
            if (nameTypesSeen == null)
            {
                throw new TlsFatalAlert(AlertDescription.illegal_parameter);
            }

            server_name_list.addElement(entry);
        }

        return new ServerNameList(server_name_list);
*/    }

    private static short[] checkNameType(short[] nameTypesSeen, short nameType)
    {
        /*
         * RFC 6066 3. The ServerNameList MUST NOT contain more than one name of the same
         * name_type.
         */
    	throw new UnsupportedClassVersionError();/*
        if (!NameType.isValid(nameType) || Arrays.contains(nameTypesSeen, nameType))
        {
            return null;
        }
        return Arrays.append(nameTypesSeen, nameType);
*/    }
}
