package exp;

import CacheWolf.*;

/**
 * Class to export the cache database (index) to an CSV File which can bei easy
 * importet bei MS AutoRoute (testet with AR 2001 German) Format of the file:
 * Name;Breitengrad;Längengrad;Typ1;Typ2;Waypoint;Datum;Hyperlink
 * 
 */
public class MSARCSVExporter extends Exporter {

	public MSARCSVExporter(Preferences p, Profile prof) {
		super();
		this.setMask("*.csv");
		this.setDecimalSeparator(',');
		this.setNeedCacheDetails(true);
		this.setHowManyParams(LAT_LON);
	}

	public String header() {
		return "Name;Breitengrad;L\u00E4ngengrad;Typ1;Typ2;Waypoint;Datum;Hyperlink\r";
	}

	public String record(CacheHolderDetail ch, String lat, String lon) {
		StringBuffer str = new StringBuffer(200);
		str.append("\"" + ch.wayPoint + " - " + ch.CacheName + "\";");
		str.append(lat + ";" + lon +";");
		str.append("\"" + CacheType.transType(ch.type)+ "\";");
		str.append("\"" + ch.CacheSize + "\";");
		str.append("\"" + ch.wayPoint + "\";");
		str.append("\"" + ch.DateHidden + "\";");
		str.append("\"" + ch.URL + "\"\r\n");

		return str.toString();
	}
}
