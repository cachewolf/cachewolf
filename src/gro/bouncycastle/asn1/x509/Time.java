package gro.bouncycastle.asn1.x509;

//import ewe.text.ParseException;
//import ewe.text.SimpleDateFormat;
import ewe.sys.Date;
import ewe.sys.Locale;
//import ewe.util.SimpleTimeZone;

import gro.bouncycastle.asn1.ASN1Choice;
import gro.bouncycastle.asn1.ASN1GeneralizedTime;
import gro.bouncycastle.asn1.ASN1Object;
import gro.bouncycastle.asn1.ASN1Primitive;
import gro.bouncycastle.asn1.ASN1TaggedObject;
import gro.bouncycastle.asn1.ASN1UTCTime;
//import gro.bouncycastle.asn1.DERGeneralizedTime;
//import gro.bouncycastle.asn1.DERUTCTime;

public class Time
    extends ASN1Object
    implements ASN1Choice
{
    ASN1Primitive time;
    public static Time getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject()); // must be explicitly tagged
    }

    public Time(
        ASN1Primitive   time)
    {
        if (!(time instanceof ASN1UTCTime)
            && !(time instanceof ASN1GeneralizedTime))
        {
            throw new IllegalArgumentException("unknown object passed to Time");
        }

        this.time = time; 
    }

    /**
     * Creates a time object from a given date - if the date is between 1950
     * and 2049 a UTCTime object is generated, otherwise a GeneralizedTime
     * is used.
     *
     * @param time a date object representing the time of interest.
     */
    public Time(
        Date    time)
    {
    	throw new UnsupportedClassVersionError();/*
        SimpleTimeZone      tz = new SimpleTimeZone(0, "Z");
        SimpleDateFormat    dateF = new SimpleDateFormat("yyyyMMddHHmmss");

        dateF.setTimeZone(tz);

        String  d = dateF.format(time) + "Z";
        int     year = Integer.parseInt(d.substring(0, 4));

        if (year < 1950 || year > 2049)
        {
            this.time = new DERGeneralizedTime(d);
        }
        else
        {
            this.time = new DERUTCTime(d.substring(2));
        }
*/    }

    /**
     * Creates a time object from a given date and locale - if the date is between 1950
     * and 2049 a UTCTime object is generated, otherwise a GeneralizedTime
     * is used. You may need to use this constructor if the default locale
     * doesn't use a Gregorian calender so that the GeneralizedTime produced is compatible with other ASN.1 implementations.
     *
     * @param time a date object representing the time of interest.
     * @param locale an appropriate Locale for producing an ASN.1 GeneralizedTime value.
     */
    public Time(
        Date    time,
        Locale locale)
    {
    	throw new UnsupportedClassVersionError();/*
        SimpleTimeZone      tz = new SimpleTimeZone(0, "Z");
        SimpleDateFormat    dateF = new SimpleDateFormat("yyyyMMddHHmmss", locale);

        dateF.setTimeZone(tz);

        String  d = dateF.format(time) + "Z";
        int     year = Integer.parseInt(d.substring(0, 4));

        if (year < 1950 || year > 2049)
        {
            this.time = new DERGeneralizedTime(d);
        }
        else
        {
            this.time = new DERUTCTime(d.substring(2));
        }
*/    }

    public static Time getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof Time)
        {
            return (Time)obj;
        }
        else if (obj instanceof ASN1UTCTime)
        {
            return new Time((ASN1UTCTime)obj);
        }
        else if (obj instanceof ASN1GeneralizedTime)
        {
            return new Time((ASN1GeneralizedTime)obj);
        }

        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
    }

    public String getTime()
    {
        if (time instanceof ASN1UTCTime)
        {
        	throw new UnsupportedClassVersionError();/*
            return ((ASN1UTCTime)time).getAdjustedTime();
*/        }
        else
        {
        	throw new UnsupportedClassVersionError();/*
            return ((ASN1GeneralizedTime)time).getTime();
*/        }
    }

    public Date getDate()
    {
    	throw new UnsupportedClassVersionError();/*
        try
        {
            if (time instanceof ASN1UTCTime)
            {
                return ((ASN1UTCTime)time).getAdjustedDate();
            }
            else
            {
                return ((ASN1GeneralizedTime)time).getDate();
            }
        }
        catch (ParseException e)
        {         // this should never happen
            throw new IllegalStateException("invalid date string: " + e.getMessage());
        }
*/    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     * Time ::= CHOICE {
     *             utcTime        UTCTime,
     *             generalTime    GeneralizedTime }
     * </pre>
     */
    public ASN1Primitive toASN1Primitive()
    {
        return time;
    }

    public String toString()
    {
        return getTime();
    }
}
