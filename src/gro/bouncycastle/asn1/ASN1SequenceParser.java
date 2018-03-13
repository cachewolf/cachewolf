package gro.bouncycastle.asn1;

import ewe.io.IOException;

/**
 * A basic parser for a SEQUENCE object
 */
public interface ASN1SequenceParser
        extends ASN1Encodable, InMemoryRepresentable {
    /**
     * Read the next object from the underlying object representing a SEQUENCE.
     *
     * @return the next object, null if we are at the end.
     * @throws IOException for bad input stream.
     */
    ASN1Encodable readObject()
            throws IOException;
}
