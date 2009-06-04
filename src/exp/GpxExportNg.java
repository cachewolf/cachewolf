package exp;

import CacheWolf.CacheSize;
import CacheWolf.CacheType;
import CacheWolf.CacheHolder;
import CacheWolf.Global;
import com.stevesoft.ewe_pat.*;
import ewe.util.Hashtable;

import ewe.sys.Vm;

/**
 * experimental GPX exporter that should better handle the various tasks that can be accomplished with GPX
 * it is not yet linked to any menu, so if you want to play around with it, first you have to create a menu item
 *
 */
public class GpxExportNg {
	
	/** export is in compact format */
	final static int GPX_COMPACT = 0;
	/** export is PQ like */
	final static int GPX_PQLIKE = 1;
	/** export follows gc.com myfinds format */
	final static int GPX_MYFINDSPQ = 2;
	
	final static String GPXHEADER = "";
	
	final static String GPXCOMPACT = "\t<wpt lat=\"@@WPLAT@@\" lon=\"@@WPLON@@\">\n"
						.concat("\t\t<name>@@WPNAME@@</name>\n")
						.concat("\t\t<cmt>@@WPCMT@@</cmt>\n")
						.concat("\t\t<desc>@@WPDESC@@</desc>\n")
						.concat("\t\t<url>@@WPURL@@</url>\n")
						.concat("\t\t<urlname>@@WPURLNAME@@</urlname>\n")
						.concat("\t\t<sym>@@WPSYMBOL@@</sym>\n")
						.concat("\t\t<type>@@WPTYPE@@</type>\n");
	
	final static String GPXEXTENSION ="\t\t<groundspeak:cache id=\"@@CACHEID@@\" available=\"@@CACHEAVAILABLE@@\" archived=\"@@CACHEARCHIVED\" xmlns:groundspeak=\"http://www.geocaching.com/cache/1/0\">\n"
						.concat("\t\t\t<groundspeak:name>@@CACHENAME@@</groundspeak:name>\n")
						.concat("\t\t\t<groundspeak:placed_by>@@CACHEPLACEDBY@@<groundspeak:placed_by>\n")
						.concat("\t\t\t<groundspeak:owner_id id=\"@@CACHEOWNERID@@\">@@CACHEOWNER@@</groundspeak:owner_id>\n")
						.concat("\t\t\t<groundspeak:type>@@CACHETYPE@@</groundspeak:type>\n")
						.concat("\t\t\t<groundspeak:container>@@CACHECONTAINER@@</groundspeak:container>\n")
						.concat("\t\t\t<groundspeak:difficulty>@@CACHEDIFFICULTY@@</groundspeak:difficulty>\n")
						.concat("\t\t\t<groundspeak:terrain>@@CACHETERRAIN@@</groundspeak:terrain>\n")
						.concat("\t\t\t<groundspeak:country>@@CACHECOUNTRY@@</groundspeak:country>\n")
						.concat("\t\t\t<groundspeak:state>@@CACHESTATE@@</groundspeak:state>\n")
						.concat("\t\t\t<groundspeak:short_description html=\"@@CACHEHTML@@\">@@CACHESHORTDESCRIPTION@@<groundspeak:short_description>\n")
						.concat("\t\t\t<groundspeak:long_description html=\"@@CACHEHTML@@\">@@CACHELONGDESCRIPTION@@<groundspeak:long_description>\n")
						.concat("\t\t\t<groundspeak:encoded_hints>@@CACHEHINT@@</groundspeak:encoded_hints>\n");

	final static String GPXLOG = "\t\t\t\t<groundspeak:log id=\"@@LOGID@@\">\n"
						.concat("\t\t\t\t\t<groundspeak:date>@@LOGDATE@@</groundspeak:date>\n")
						.concat("\t\t\t\t\t<groundspeak:type>@@LOGTYPE@@</groundspeak:type>\n")
						.concat("\t\t\t\t\t<groundspeak:finder id=\"@@LOGFINDERID@@\">@@LOGFINDER@@</groundspeak:finder>\n")
						.concat("\t\t\t\t\t<groundspeak:text encoded=\"@@LOGENCODE@@\">@@LOGTEXT@@</groundspeak:text>\n")
						.concat("\t\t\t\t</groundspeak:log>\n");
	
	final static String GPXTB = "\t\t\t\t<groundspeak:travelbug id=\"@@TBID@@\" ref=\"@@TBREF@@\">\n"
						.concat("\t\t\t\t\t<groundspeak:name>@@TBNAME@@</groundspeak:name>\n")
						.concat("\t\t\t\t</groundspeak:travelbug>\n");
	
	static boolean smartIds;
	static boolean customIcons;
	static boolean separateFile;
	static boolean sendToGarmin;
	static int outType;
	
	public GpxExportNg() {
		GpxExportNgForm exportOptions;
		int ret;
		final String file, dirctory;
		final Hashtable fileHandles;

		exportOptions = new GpxExportNgForm();
		ret = exportOptions.execute();
		
		if (-1 == ret) {
			return;
		}
		
		outType = exportOptions.getExportType();
		smartIds = exportOptions.getSmartIds();
		separateFile = exportOptions.getSeparateFiles();
		sendToGarmin = exportOptions.getSendToGarmin();
		customIcons = exportOptions.getCustomIcons();
		
		if (separateFile) {
			//TODO: get directory
			//TODO: initialize files
		} else {
			//TODO: get file
			//TODO: initialize file
		}
		
		for(int i = 0; i<Global.getProfile().cacheDB.size(); i++){
			CacheHolder ch=Global.getProfile().cacheDB.get(i);
			Vm.debug(formatCache(ch));
		}

	}
	
	private String formatCache(CacheHolder ch) {
		// no addis or custom in MyFindsPq - and of course only finds
		if ((GPX_MYFINDSPQ == outType) && 
				((ch.getType() == CacheType.CW_TYPE_CUSTOM) || ch.isAddiWpt() || ! ch.is_found())) 
				return "";
		
		StringBuffer ret = new StringBuffer();

		ret.append(formatCompact(ch));
		
//		if (outType != GPX_COMPACT && !(ch.getType() == CacheType.CW_TYPE_CUSTOM || ch.isAddiWpt())) {
//			ret.append(formatPqExtensions(ch));
//		}
		ret.append("\t</wpt>\n");
		return ret.toString();
	}
	
	private String formatCompact(CacheHolder ch) {
		Transformer trans = new Transformer(true);
		
		trans.add(new Regex("@@WPLAT@@", 
				((ch.pos.latDec >= -90) && (ch.pos.latDec <= 90)?String.valueOf(ch.pos.latDec):"")
			));
		
		trans.add(new Regex("@@WPLON@@",
				((ch.pos.lonDec >= -180) && (ch.pos.lonDec <= 180)?String.valueOf(ch.pos.lonDec):"")
			));
		
		if (smartIds && ch.getType() != CacheType.CW_TYPE_CUSTOM) {
			if (ch.isAddiWpt()) {
				trans.add(new Regex("@@WPNAME@@",ch.mainCache.getWayPoint()
						.concat(" ")
						.concat(ch.getWayPoint().substring(0,2))));
			} else {
				trans.add(new Regex("@@WPNAME@@",ch.getWayPoint()
						.concat(" ")
						.concat(CacheType.getExportShortId(ch.getType()))
						.concat(String.valueOf(ch.getTerrain()))
						.concat(String.valueOf(ch.getHard()))
						.concat(CacheSize.getExportShortId(ch.getCacheSize()))
					));
			}
		} else {
			trans.add(new Regex("@@WPNAME@@",ch.getWayPoint()));
		}
		
		if (ch.isAddiWpt()) {
			trans.add(new Regex("@@WPCMT@@",ch.getFreshDetails().LongDescription));
		} else {
			trans.add(new Regex("@@WPCMT@@",""));
		}
		
		trans.add(new Regex("@@WPDESC@@",""));
		
		if (ch.getType() == CacheType.CW_TYPE_CUSTOM) {
			trans.add(new Regex("@@WPURL@@",""));
		} else {
			if (ch.isAddiWpt()) {
				//TODO: find out URL schema for additional waypoints
				//TODO: check for OC caches
				trans.add(new Regex("@@WPURL@@",""));
			} else {
				//TODO: check for OC caches
				trans.add(new Regex("@@WPURL@@","http://www.geocaching.com/seek/cache_details.aspx?wp=".concat(ch.getWayPoint())));
			}
		}
		
		if (ch.getType() == CacheType.CW_TYPE_CUSTOM) {
			trans.add(new Regex("@@WPURLNAME@@",""));
		} else {
			trans.add(new Regex("@@WPURLNAME@@",ch.getCacheName()));
		}
		
		if (customIcons) {
			//TODO: replace with SKGs custom symbol code
			trans.add(new Regex("@@WPSYMBOL@@","Geocache"));
		} else {
			if (ch.is_found()) {
				trans.add(new Regex("@@WPSYMBOL@@","Geocache found"));
			} else {
				trans.add(new Regex("@@WPSYMBOL@@","Geocache"));
			}
		}
		
		trans.add(new Regex("@@WPTYPE@@",CacheType.cw2ExportString(ch.getType())));
		
		return trans.replaceFirst(GPXCOMPACT);
	}
	
	private String formatPqExtensions(CacheHolder ch) {
		// no details pq details for addis or custom waypoints
		if (ch.getType() == CacheType.CW_TYPE_CUSTOM || ch.isAddiWpt()) return "";
		
		StringBuffer ret = new StringBuffer();
		
		ret.append(GPXEXTENSION);
		
		ret.append("\t\t\t<groundspeak:logs>\n");
		ret.append(formatLogs(ch));
		ret.append("\t\t\t</groundspeak:logs>\n");
		
		ret.append("\t\t\t<groundspeak:travelbugs>\n");
		ret.append(formatTbs(ch));
		ret.append("\t\t\t</groundspeak:travelbugs>\n");
		
		ret.append("\t\t</groundspeak:cache>\n");
		return ret.toString();
	}
	
	public void doit() {
		
	}
	
	public String formatTbs(CacheHolder ch) {
		Transformer trans = new Transformer(true);
		
		return trans.replaceFirst(GPXTB);
	}
	
	public String formatLogs(CacheHolder ch) {
		Transformer trans = new Transformer(true);
		
		return trans.replaceFirst(GPXLOG);
	}
	
	public String formatHeader() {
		Transformer trans = new Transformer(true);
		
		return trans.replaceFirst(GPXHEADER);
	}
}
