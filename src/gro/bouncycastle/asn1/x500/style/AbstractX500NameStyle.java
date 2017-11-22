package gro.bouncycastle.asn1.x500.style;

import ewe.io.IOException;
import ewe.util.Enumeration;
import ewe.util.Hashtable;
import gro.bouncycastle.asn1.ASN1Encodable;
import gro.bouncycastle.asn1.ASN1ObjectIdentifier;
import gro.bouncycastle.asn1.ASN1ParsingException;
//import gro.bouncycastle.asn1.DERUTF8String;
//import gro.bouncycastle.asn1.x500.AttributeTypeAndValue;
import gro.bouncycastle.asn1.x500.RDN;
import gro.bouncycastle.asn1.x500.X500Name;
import gro.bouncycastle.asn1.x500.X500NameStyle;

/**
 * This class provides some default behavior and common implementation for a
 * X500NameStyle. It should be easily extendable to support implementing the
 * desired X500NameStyle.
 */
public abstract class AbstractX500NameStyle
    implements X500NameStyle
{

    /**
     * Tool function to shallow copy a Hashtable.
     *
     * @param paramsMap table to copy
     * @return the copy of the table
     */
    public static Hashtable copyHashTable(Hashtable paramsMap)
    {
        Hashtable newTable = new Hashtable();

        Enumeration keys = paramsMap.keys();
        while (keys.hasMoreElements())
        {
            Object key = keys.nextElement();
            newTable.put(key, paramsMap.get(key));
        }

        return newTable;
    }
/*
    private int calcHashCode(ASN1Encodable enc)
    {
        String value = IETFUtils.valueToString(enc);
        value = IETFUtils.canonicalize(value);
        return value.hashCode();
    }
*/
    public int calculateHashCode(X500Name name)
    {
    	throw new UnsupportedClassVersionError();/*
        int hashCodeValue = 0;
        RDN[] rdns = name.getRDNs();

        // this needs to be order independent, like equals
        for (int i = 0; i != rdns.length; i++)
        {
            if (rdns[i].isMultiValued())
            {
                AttributeTypeAndValue[] atv = rdns[i].getTypesAndValues();

                for (int j = 0; j != atv.length; j++)
                {
                    hashCodeValue ^= atv[j].getType().hashCode();
                    hashCodeValue ^= calcHashCode(atv[j].getValue());
                }
            }
            else
            {
                hashCodeValue ^= rdns[i].getFirst().getType().hashCode();
                hashCodeValue ^= calcHashCode(rdns[i].getFirst().getValue());
            }
        }

        return hashCodeValue;
*/    }


    /**
     * For all string values starting with '#' is assumed, that these are
     * already valid ASN.1 objects encoded in hex.
     * <p>
     * All other string values are send to
     * {@link AbstractX500NameStyle#encodeStringValue(ASN1ObjectIdentifier, String)}.
     * </p>
     * Subclasses should overwrite
     * {@link AbstractX500NameStyle#encodeStringValue(ASN1ObjectIdentifier, String)}
     * to change the encoding of specific types.
     *
     * @param oid the DN name of the value.
     * @param value the String representation of the value.
     */

    public ASN1Encodable stringToValue(ASN1ObjectIdentifier oid, String value)
    {
    	throw new UnsupportedClassVersionError();/*
        if (value.length() != 0 && value.charAt(0) == '#')
        {
            try
            {
                return IETFUtils.valueFromHexString(value, 1);
            }
            catch (IOException e)
            {
                throw new ASN1ParsingException("can't recode value for oid " + oid.getId());
            }
        }

        if (value.length() != 0 && value.charAt(0) == '\\')
        {
            value = value.substring(1);
        }

        return encodeStringValue(oid, value);
*/    }

    /**
     * Encoded every value into a UTF8String.
     * <p>
     * Subclasses should overwrite
     * this method to change the encoding of specific types.
     * </p>
     *
     * @param oid the DN oid of the value
     * @param value the String representation of the value
     * @return a the value encoded into a ASN.1 object. Never returns <code>null</code>.
     */
     /*
    protected ASN1Encodable encodeStringValue(ASN1ObjectIdentifier oid, String value)
    {
        return new DERUTF8String(value);
    }
*/
    public boolean areEqual(X500Name name1, X500Name name2)
    {
    	throw new UnsupportedClassVersionError();/*
        RDN[] rdns1 = name1.getRDNs();
        RDN[] rdns2 = name2.getRDNs();

        if (rdns1.length != rdns2.length)
        {
            return false;
        }

        boolean reverse = false;

        if (rdns1[0].getFirst() != null && rdns2[0].getFirst() != null)
        {
            reverse = !rdns1[0].getFirst().getType().equals(rdns2[0].getFirst().getType());  // guess forward
        }

        for (int i = 0; i != rdns1.length; i++)
        {
            if (!foundMatch(reverse, rdns1[i], rdns2))
            {
                return false;
            }
        }

        return true;
*/    }
/*
    private boolean foundMatch(boolean reverse, RDN rdn, RDN[] possRDNs)
    {
        if (reverse)
        {
            for (int i = possRDNs.length - 1; i >= 0; i--)
            {
                if (possRDNs[i] != null && rdnAreEqual(rdn, possRDNs[i]))
                {
                    possRDNs[i] = null;
                    return true;
                }
            }
        }
        else
        {
            for (int i = 0; i != possRDNs.length; i++)
            {
                if (possRDNs[i] != null && rdnAreEqual(rdn, possRDNs[i]))
                {
                    possRDNs[i] = null;
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean rdnAreEqual(RDN rdn1, RDN rdn2)
    {
        return IETFUtils.rDNAreEqual(rdn1, rdn2);
    }
*/}