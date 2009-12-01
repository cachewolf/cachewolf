package CacheWolf.exp;

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

	public String record(CacheHolder ch, String lat, String lon) {
		StringBuffer str = new StringBuffer(200);
		str.append("\"" + ch.getWayPoint() + " - " + ch.getCacheName() + "\";");
		str.append(lat + ";" + lon +";");
		str.append("\"" + CacheType.sym2GpxString(ch.getType())+ "\";");
		str.append("\"" + CacheSize.cw2ExportString(ch.getCacheSize()) + "\";");
		str.append("\"" + ch.getWayPoint() + "\";");
		str.append("\"" + ch.getDateHidden() + "\";");
		str.append("\"" + ch.getExistingDetails().URL + "\"\r\n");

		return str.toString();
	}
}
