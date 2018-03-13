package gro.bouncycastle.asn1;

import ewe.io.IOException;

/**
 * A basic parser for a SET object
 */
public interface ASN1SetParser
        extends ASN1Encodable, InMemoryRepresentable {
    /**
     * Read the next object from the underlying object representing a SET.
     *
     * @return the next object, null if we are at the end.
     * @throws IOException for bad input stream.
     */
    public ASN1Encodable readObject()
            throws IOException;
}
