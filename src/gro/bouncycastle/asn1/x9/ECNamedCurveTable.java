package gro.bouncycastle.asn1.x9;

import ewe.util.Enumeration;
import ewe.util.Vector;
import gro.bouncycastle.asn1.ASN1ObjectIdentifier;
import gro.bouncycastle.asn1.anssi.ANSSINamedCurves;
//import gro.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import gro.bouncycastle.asn1.gm.GMNamedCurves;
import gro.bouncycastle.asn1.nist.NISTNamedCurves;
import gro.bouncycastle.asn1.sec.SECNamedCurves;
import gro.bouncycastle.asn1.teletrust.TeleTrusTNamedCurves;

/**
 * A general class that reads all X9.62 style EC curve tables.
 */
public class ECNamedCurveTable
{
    /**
     * return a X9ECParameters object representing the passed in named
     * curve. The routine returns null if the curve is not present.
     *
     * @param name the name of the curve requested
     * @return an X9ECParameters object or null if the curve is not available.
     */
	
    public static X9ECParameters getByName(String name)
    {
        X9ECParameters ecP = X962NamedCurves.getByName(name);

        if (ecP == null)
        {
            ecP = SECNamedCurves.getByName(name);
        }

        if (ecP == null)
        {
            ecP = NISTNamedCurves.getByName(name);
        }

        if (ecP == null)
        {
            ecP = TeleTrusTNamedCurves.getByName(name);
        }

        if (ecP == null)
        {
            ecP = ANSSINamedCurves.getByName(name);
        }

        if (ecP == null)
        {
            ecP = GMNamedCurves.getByName(name);
        }

        return ecP;
    }
    /**
     * return the object identifier signified by the passed in name. Null
     * if there is no object identifier associated with name.
     *
     * @return the object identifier associated with name, if present.
     */
    public static ASN1ObjectIdentifier getOID(
        String name)
    {
    	throw new UnsupportedClassVersionError();/*
        ASN1ObjectIdentifier oid = X962NamedCurves.getOID(name);

        if (oid == null)
        {
            oid = SECNamedCurves.getOID(name);
        }

        if (oid == null)
        {
            oid = NISTNamedCurves.getOID(name);
        }

        if (oid == null)
        {
            oid = TeleTrusTNamedCurves.getOID(name);
        }

        if (oid == null)
        {
            oid = ANSSINamedCurves.getOID(name);
        }

        if (oid == null)
        {
            oid = GMNamedCurves.getOID(name);
        }

        return oid;
*/    }

/*
    /**
     * return a X9ECParameters object representing the passed in named
     * curve.
     *
     * @param oid the object id of the curve requested
     * @return a standard name for the curve.
     * /
    public static String getName(
        ASN1ObjectIdentifier oid)
    {
        String name = NISTNamedCurves.getName(oid);

        if (name == null)
        {
            name = SECNamedCurves.getName(oid);
        }

        if (name == null)
        {
            name = TeleTrusTNamedCurves.getName(oid);
        }

        if (name == null)
        {
            name = X962NamedCurves.getName(oid);
        }

        if (name == null)
        {
            name = ECGOST3410NamedCurves.getName(oid);
        }

        if (name == null)
        {
            name = GMNamedCurves.getName(oid);
        }

        return name;
    }
*/
    /**
     * return a X9ECParameters object representing the passed in named
     * curve.
     *
     * @param oid the object id of the curve requested
     * @return an X9ECParameters object or null if the curve is not available.
     */
    public static X9ECParameters getByOID(
        ASN1ObjectIdentifier oid)
    {
        X9ECParameters ecP = X962NamedCurves.getByOID(oid);

        if (ecP == null)
        {
            ecP = SECNamedCurves.getByOID(oid);
        }

        // NOTE: All the NIST curves are currently from SEC, so no point in redundant OID lookup

        if (ecP == null)
        {
            ecP = TeleTrusTNamedCurves.getByOID(oid);
        }

        if (ecP == null)
        {
        	throw new UnsupportedClassVersionError();/*
            ecP = ANSSINamedCurves.getByOID(oid);
*/        }

        if (ecP == null)
        {
        	throw new UnsupportedClassVersionError();/*
            ecP = GMNamedCurves.getByOID(oid);
*/        }

        return ecP;
    }
/*
    /**
     * return an enumeration of the names of the available curves.
     *
     * @return an enumeration of the names of the available curves.
     * /
    public static Enumeration getNames()
    {
        Vector v = new Vector();

        addEnumeration(v, X962NamedCurves.getNames());
        addEnumeration(v, SECNamedCurves.getNames());
        addEnumeration(v, NISTNamedCurves.getNames());
        addEnumeration(v, TeleTrusTNamedCurves.getNames());
        addEnumeration(v, ANSSINamedCurves.getNames());
        addEnumeration(v, GMNamedCurves.getNames());

        return v.elements();
    }

    private static void addEnumeration(
        Vector v,
        Enumeration e)
    {
        while (e.hasMoreElements())
        {
            v.addElement(e.nextElement());
        }
    }
*/}
