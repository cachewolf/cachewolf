package gro.bouncycastle.asn1.x500;

import gro.bouncycastle.asn1.*;

/**
 * Holding class for the AttributeTypeAndValue structures that make up an RDN.
 */
public class AttributeTypeAndValue
        extends ASN1Object {
    private ASN1ObjectIdentifier type;
    private ASN1Encodable value;

    private AttributeTypeAndValue(ASN1Sequence seq) {
        type = (ASN1ObjectIdentifier) seq.getObjectAt(0);
        value = (ASN1Encodable) seq.getObjectAt(1);
    }

    public AttributeTypeAndValue(
            ASN1ObjectIdentifier type,
            ASN1Encodable value) {
        this.type = type;
        this.value = value;
    }

    public static AttributeTypeAndValue getInstance(Object o) {
        if (o instanceof AttributeTypeAndValue) {
            return (AttributeTypeAndValue) o;
        } else if (o != null) {
            return new AttributeTypeAndValue(ASN1Sequence.getInstance(o));
        }

        throw new IllegalArgumentException("null value in getInstance()");
    }

    public ASN1ObjectIdentifier getType() {
        return type;
    }

    public ASN1Encodable getValue() {
        return value;
    }

    /**
     * <pre>
     * AttributeTypeAndValue ::= SEQUENCE {
     *           type         OBJECT IDENTIFIER,
     *           value        ANY DEFINED BY type }
     * </pre>
     *
     * @return a basic ASN.1 object representation.
     */
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(type);
        v.add(value);

        return new DERSequence(v);
    }
}
