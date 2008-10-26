package cachewolf.exp;
import cachewolf.*;

/**
*	Class to export cache database to an ASCII (CSV!) file.
*   This file can be used by I2C's POI Converter to generate
*   POIs for different routing programmes, especially for
*	Destinator ;-) !
*/
public class ASCExporter extends Exporter{
	
	public ASCExporter(){
		super();
		this.setMask("*.csv");
		this.setHowManyParams(LAT_LON);
	}
	
	public ASCExporter(Preferences p,Profile prof){
		this();
	}
	
	public String record (CacheHolderDetail chD, String lat, String lon){
		StringBuffer strBuf = new StringBuffer(100);
		String dummy;
		dummy = chD.cacheName;
		dummy = dummy.replace(',', ' ');
		strBuf.append(dummy);
		strBuf.append(",");
		strBuf.append(dummy);
		strBuf.append(",");
		strBuf.append(lon);
		strBuf.append(",");
		strBuf.append(lat);
		strBuf.append(",,,,\r\n");
		return strBuf.toString();
	}
}
