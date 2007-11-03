package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.HttpConnection;
import CacheWolf.InfoBox;
import ewe.ui.*;
import ewe.io.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.sys.Double;
import ewe.net.*;
import java.lang.Math;

/**
 *
 */

//Um Karten zu holen!
//http://www.expedia.de/pub/agent.dll?qscr=mrdt&ID=3kQaz.&CenP=48.09901667,11.35688333&Lang=EUR0407&Alti=1&Size=600,600&Offs=0.000000,0.000000&Pins=|5748|
//oder
//http://www.expedia.de/pub/agent.dll?qscr=mrdt&ID=3kQaz.&CenP=48.15,11.5833&Alti=2&Lang=EUR0407&Size=900,900&Offs=0,0&MapS=0&Pins=|48.15,11.5833|4|48.15,11.5833&Pins=|48.15,11.5833|1|48.15,%2011.5833||


public class MapLoader {
	String proxy = new String();
	String port = new String();
	InfoBox progressInfobox;

	final static float downloadMapScaleFactorExpedia_east = 3950;
	final static float MAPBLAST_METERS_PER_PIXEL = 1.0f/2817.947378f;
	final static float EXPEDIA_METERS_PER_PIXEL = downloadMapScaleFactorExpedia_east * MAPBLAST_METERS_PER_PIXEL; 

	Vector onlineMapServices = new Vector();

	int numMapsY;
	int numMapsX;
	double latinc;
	double loninc;
	CWPoint topleft;
	CWPoint buttomright;
	Point tilesSize;
	int tileScale;

	public MapLoader(String prxy, String prt){
		port = prt;
		proxy = prxy;
		progressInfobox = null;
	}

	/**
	 * download maps from expedia at zoomlevel alti and save the maps and the .wfl 
	 * in path
	 * @param center centre of all tiles
	 * @param radius in meters
	 * @param scale in "alti" value 1 alti =  3950 /2817.947378 = 1,046861280317350198581316446428 meters per pixel
	 * @param size in pixels
	 * @param overlapping in pixels
	 * @param path without "/" at the end
	 * 
	 */
	public void setTiles (CWPoint center, float radius, int scale, Point size, int overlapping) {
		double metersPerLat = (1000*(new CWPoint(0,0)).getDistance(new CWPoint(1,0)));
		double metersPerLon = metersPerLat * java.lang.Math.cos(center.latDec/180*java.lang.Math.PI);
		topleft = new CWPoint(center.latDec + (radius / metersPerLat), center.lonDec - (radius / metersPerLon));
		buttomright = new CWPoint(center.latDec - (radius / metersPerLat), center.lonDec + (radius / metersPerLon));

		this.setTiles(topleft, buttomright, scale, size, overlapping);
	}

	public void setTiles(CWPoint toplefti, CWPoint buttomrighti, int scale, Point size, int overlapping) {
		//if (toplefti.latDec <= buttomrighti.latDec || toplefti.lonDec >= toplefti.lonDec) throw new IllegalArgumentException("topleft must be left and above buttom right");
		topleft = new CWPoint(toplefti);
		buttomright = new CWPoint(buttomrighti);
		double metersPerLat = (1000*(new CWPoint(0,0)).getDistance(new CWPoint(1,0)));
		double metersPerLon = metersPerLat * java.lang.Math.cos((toplefti.latDec + buttomright.latDec)/2/180*java.lang.Math.PI);

		double pixelsPerLat = metersPerLat / (EXPEDIA_METERS_PER_PIXEL * scale);
		double pixelsPerLon = metersPerLon / (EXPEDIA_METERS_PER_PIXEL * scale);

		//over all pixelsize without borders
		double pixelsY = (topleft.latDec - buttomright.latDec) * pixelsPerLat; 
		double pixelsX = -(topleft.lonDec - buttomright.lonDec) * pixelsPerLon ; 

		//border sizes around given area and overlapping between tiles
		//int borderX = (int) java.lang.Math.round((float)size.x * (overlapping - 1.0));
		//int borderY = (int) java.lang.Math.round((float)size.y * (overlapping - 1.0));
		int borderX = overlapping;
		int borderY = overlapping;

		numMapsY = (int) java.lang.Math.ceil( (pixelsY + (float)borderY) / (float)(size.y - borderY) );
		numMapsX = (int) java.lang.Math.ceil( (pixelsX + (float)borderX) / (float)(size.x - borderX) );

		//increments calulated from pixel offset of tiles
		latinc = (float)-(size.y - borderY) / pixelsPerLat;
		loninc = (float)(size.x - borderX) / pixelsPerLon;

		//calculation of centre of first tile

		//additional size for borders and rounding
		double oversizeX = (float)(numMapsX * (size.x - borderX) + borderX) - pixelsX;
		double oversizeY = (float)(numMapsY * (size.y - borderY) + borderY) - pixelsY;

		//offset for upper left corner
		double offsetLat = -( ((float)size.y - oversizeY) / 2.0 ) / pixelsPerLat;
		double offsetLon = ( ((float)size.x - oversizeX) / 2.0 ) / pixelsPerLon;

		topleft.latDec += offsetLat;
		topleft.lonDec += offsetLon;

		this.tilesSize = new Point();
		this.tilesSize.set(size);
		this.tileScale = scale;
	}

	public void downlaodTiles(String tilesPath) {
		double lat = topleft.latDec;
		double lon = topleft.lonDec;
		for (int row = 1; row <= numMapsY; row++) {
			lon = topleft.lonDec;
			for (int col = 1; col <= numMapsX; col++) {
				if (progressInfobox != null)
					progressInfobox.setInfo("Downloading calibrated (georeferenced) \n map image from www.expedia.com \n Downloading tile \n row "+row+" / "+numMapsY+" column "+ col + " / "+numMapsX);
				downloadMap(lat, lon, tileScale, tilesSize.x, tilesSize.y, tilesPath);
				lon += loninc;
			}
			lat += latinc;
		}
	}

	/*
	public void loadTo(String a, String b) {
		//loadTo(a, b, "50.74", "7.095");
	}
	 */

	public void setProgressInfoBox (InfoBox progrssInfoboxi) {
		progressInfobox = progrssInfoboxi;
	}
	/**
	 * calculates the Expedia Alti = scale which fits in distance to its edges
	 * @param center
	 * @param distance in meters
	 */
	public static int getExpediaAlti(CWPoint center, float distance, Point size) {
		int scaleLatO = (int) java.lang.Math.ceil(( distance * 2 / EXPEDIA_METERS_PER_PIXEL / size.y));
		int scaleLonO = (int) java.lang.Math.ceil(( distance * 2 / EXPEDIA_METERS_PER_PIXEL / size.x));
		int scaleO = (scaleLatO < scaleLonO ? scaleLonO : scaleLatO);
		//loadTo((topleft.latDec + buttomright.latDec)/2, (topleft.lonDec + buttomright.lonDec)/2, scaleO, size.x, size.y, path+"/expedia_alti"+scaleO+"_lat"+latD.toString()+"_lon"+lonD.toString());
		return scaleO;
	}

	public static String createExpediaFilename(double lat, double lon, int alti) {
		Double latD = new Double(), lonD = new Double();
		latD.decimalPlaces = 4;
		lonD.decimalPlaces = 4;
		latD.set(lat);
		lonD.set(lon);
		return "expedia_alti"+alti+"_lat"+latD.toString().replace(',', '.')+"_lon"+lonD.toString().replace(',', '.')+".gif";
	}

	public void downloadMap(double lat, double lon, int alti, int PixelWidth, int PixelHeight, String path){
		loadTo(lat, lon, alti, PixelWidth, PixelHeight, path+"/"+createExpediaFilename(lat, lon, alti));
	}

	public void loadTo(double lat, double lon, int alti, int PixelWidth, int PixelHeight, String datei){
		HttpConnection connImg, conn2;
		Socket sockImg, sock2;
		InputStream is;
		FileOutputStream fos;
		ByteArray daten;
		String quelle = new String();
		String zone;
		// prepare MapInfoObject and get fileprefix
		File dateiF = new File(datei); // change!!!
		String tmp = dateiF.getName(); // contains the name and the extension
		String name = tmp.substring(0, tmp.lastIndexOf("."));
		float metersPerPixel = (float) (alti)*EXPEDIA_METERS_PER_PIXEL;
		MapInfoObject cal = new MapInfoObject(metersPerPixel, new CWPoint(lat,lon),  PixelWidth, PixelHeight, name);
		String pref = cal.getFfPrefix();
		cal.mapName = pref + cal.mapName;
		cal.fileNameWFL = pref + cal.fileNameWFL;
		datei = dateiF.getPath()+"/"+ pref + tmp;
		// download image
		if (lon <= -10) zone = "USA0409";
		else zone = "EUR0809";

		/*
		 * information from: DownloadMouseMode.properties in project GPSylon ( in directory gpsylon_src-0.5.2\plugins\downloadmousemode\auxiliary\org\dinopolis\gpstool\plugin\downloadmousemode and DownloadMapCalculator.java in Dir gpsylon_src-0.5.2\plugins\downloadmousemode\src\org\dinopolis\gpstool\plugin\downloadmousemode 
		 * download.map.url.expedia_east=http\://www.expedia.com/pub/agent.dll?qscr=mrdt&ID=3XNsF.&CenP={0,number,#.########},{1,number,#.########}&Lang=EUR0809&Alti={2,number,#}&Size={3,number,#},{4,number,#}&Offs=0.000000,0.000000\&BCheck=1
		 * download.map.url.expedia_east.title=Url of Expedia Europe
		 * download.map.scale_factor.expedia_east=3950
		 */
		Double latD = new Double();
		latD.decimalPlaces = 8;
		latD.set(lat);
		Double lonD = new Double();
		lonD.decimalPlaces = 8;
		lonD.set(lon);
		quelle = "http://www.expedia.de/pub/agent.dll?qscr=mrdt";
		quelle = quelle + "&ID=3kQaz.";
		quelle = quelle + "&CenP=" + latD.toString().replace(',', '.') + "," + lonD.toString().replace(',', '.');
		quelle = quelle + "&Alti="+Convert.toString(alti)+"&Lang="+zone+"&Size="+Convert.toString(PixelWidth)+","+Convert.toString(PixelHeight)+"&Offs=0,0&MapS=0"; //&Pins=|" + latD.toString().replace(',', '.') + "," + lonD.toString().replace(',', '.') + "|5|";
		//Vm.debug(lat + "," + lon);
		if(proxy.length()>0){
			connImg = new HttpConnection(proxy, Convert.parseInt(port), quelle);
			//Vm.debug("Loading quelle: " + quelle);
		}else{
			connImg = new HttpConnection(quelle);
		}
		//datei = "d:\\temp\\test_map.bmp";
		connImg.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
		connImg.setRequestorProperty("Connection", "close");
		connImg.setRequestorProperty("Cookie", "jscript=1; path=/;");
		connImg.documentIsEncoded = true;
		try{
			dateiF = new File(datei);
			if(!dateiF.exists()){
				int i=0;
				quelle = null;
				while (quelle == null && i < 5) { // this is necessary because expedia sometimes doesn't directly anser with the redirect to the map-image, but give a page in between. Solved the problem by retrying see also: http://www.geoclub.de/viewtopic.php?p=305071#305071
					sockImg = connImg.connect();
					//Vm.debug("Redirect: " + i + connImg.getRedirectTo());
					quelle = connImg.getRedirectTo();
					sockImg.close();
					i++;
				}
				if (i > 4) throw new IOException("loadTo: failed to download map: didn't get http-redirect");
				if(proxy.length()>0){
					connImg = new HttpConnection(proxy, Convert.parseInt(port), quelle);
				}else{
					connImg = new HttpConnection(quelle);
				}
				connImg.setRequestorProperty("USER_AGENT", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
				connImg.setRequestorProperty("Connection", "close");
				connImg.setRequestorProperty("Cookie", "jscript=1; path=/;");
				connImg.documentIsEncoded = true;
				sock2 = connImg.connect();
				daten = connImg.readData(sock2);
				fos = new FileOutputStream(dateiF);
				fos.write(daten.toBytes());
				fos.close();
				sock2.close();
			}
			//Vm.debug("done");
		}catch(IOException e){
			(new MessageBox("Error", "Error while downloading or saving map:\n"+e.getMessage(), MessageBox.OKB)).exec();
		}
		try {
			cal.saveWFL(dateiF.getDrivePath(), cal.fileNameWFL);
		} catch (IOException e) {
			(new MessageBox("Error", "Error saving calibration file:\n"+e.getMessage(), MessageBox.OKB)).exec();
		}
	}
}

class OnlineMapService {
	String name;
	String MainUrl; //http://www.geoserver.nrw.de/GeoOgcWms1.3/servlet/TK25?SERVICE=WMS
}

class WebMapService extends OnlineMapService {
	String layersName;
	String layersUrlPart; //
	String versionUrlPart; // VERSION=1.1.0
	int coordinateReferenceSystem; // WGS84: 4326, German GK: 31466 / 
	String coordinateReferenceSystemUrlPart; //&SRS=EPSG:31466
	String requestUrlPart;
	String imageFormatUrlPart; // FORMAT=image/png
	String stylesUrlPart; // STYLES=
	Area boundingBox;
	double minscale;
	double maxscale;
	
	public WebMapService (String filename) throws IOException, IllegalArgumentException{
		FileInputStream in = new FileInputStream(filename + ".wms");
		Properties wms = new Properties();
		wms.load(in);
		in.close();
		name = wms.getProperty("Name", null);
		if (name == null) throw new IllegalArgumentException("WebMapService: property >Name:< missing in file:\n" + filename);
		MainUrl = wms.getProperty("MainUrl", null);
		if (MainUrl == null) throw new IllegalArgumentException("WebMapService: property >MainUrl:< missing in file:\n" + filename);
		layersName = wms.getProperty("LayersName", "missing layersname");
		layersUrlPart = wms.getProperty("LayersUrlPart", "");
		versionUrlPart = wms.getProperty("VersionUrlPart", "");
		coordinateReferenceSystem = Convert.toInt(wms.getProperty("CoordinateReferenceSystemCacheWolf", "0"));
		if (!TransformCoordinates.isSupported(coordinateReferenceSystem)) throw new IllegalArgumentException("Coordinate reference system not supported by CacheWolf:\n" + coordinateReferenceSystem);
		coordinateReferenceSystemUrlPart = wms.getProperty("CoordinateReferenceSystemUrlPart", null);
		if (coordinateReferenceSystemUrlPart == null) throw new IllegalArgumentException("CoordinateReferenceSystemUrlPart: property >MainUrl:< missing in file:\n" + filename);
		requestUrlPart = wms.getProperty("RequestUrlPart", "");
		imageFormatUrlPart = wms.getProperty("ImageFormatUrlPart", "REQUEST=GetMap");
		stylesUrlPart = wms.getProperty("StylesUrlPart", "");
		String topleftS = wms.getProperty("BoundingBoxTopLeftWGS84", "");
		String buttomrightS = wms.getProperty("BoundingBoxButtomRightWGS84", "");
		CWPoint topleft = new CWPoint(topleftS);
		CWPoint buttomright = new CWPoint(buttomrightS);
		if (!topleft.isValid()) topleft.set(90, -180);
		if (!buttomright.isValid()) buttomright.set(-90, 180);
		boundingBox = new Area (topleft, buttomright);
		minscale = Convert.toDouble(wms.getProperty("MinScale", "0"));
		maxscale = Convert.toDouble(wms.getProperty("MaxScale", Convert.toString(java.lang.Double.MAX_VALUE)));
	}

	public String getUrlForBoundingBox(Area maparea, Point pixelsize) {
		if (!boundingBox.isOverlapping(maparea)) throw new IllegalArgumentException("area: " + maparea.toString() + "not covered by service: " + name + "service area: " + boundingBox.toString());
		double scale = maparea.buttomright.getDistance(maparea.topleft) * 1000 / Math.sqrt(pixelsize.x * pixelsize.x + pixelsize.y * pixelsize.y); // meters per pixel measured diagonal
		if ( scale < minscale || scale > maxscale) throw new IllegalArgumentException("scale not support by online map service: " + scale + "\n supported scale range: " + minscale + "-" + maxscale);
		// http://www.geoserver.nrw.de/GeoOgcWms1.3/servlet/TK25?SERVICE=WMS&VERSION=1.1.0&REQUEST=GetMap&SRS=EPSG:31466&BBOX=2577567.0149,5607721.7566,2578567.0077,5608721.7602&WIDTH=500&HEIGHT=500&LAYERS=Raster:TK25_KMF:Farbkombination&STYLES=&FORMAT=image/png
		CWPoint buttomleft = new CWPoint (maparea.buttomright.latDec, maparea.topleft.lonDec);
		CWPoint topright = new CWPoint (maparea.topleft.latDec, maparea.buttomright.lonDec);
		String bbox = "BBOX=";
		if (TransformCoordinates.isGermanGk(coordinateReferenceSystem)) 
			bbox += buttomleft.getGermanGkCoordinates(3, "",",") + "," + topright.getGermanGkCoordinates(3, "", ",");
		else if (coordinateReferenceSystem == TransformCoordinates.EPSG_WGS84) 
			bbox += buttomleft.toString(CWPoint.LON_LAT)  + "," + topright.toString(CWPoint.LON_LAT);
		else throw new IllegalArgumentException("Coordinate system not supported by cachewolf: " + coordinateReferenceSystem);
		String ret = MainUrl + "&"+ versionUrlPart + "&" + requestUrlPart + "&" + 
		coordinateReferenceSystemUrlPart + "&" + bbox + 
		"&WIDTH=" + pixelsize.x + "&HEIGHT=" + pixelsize.y + "&" + 
		layersUrlPart + "&" + stylesUrlPart + "&" + imageFormatUrlPart; 
		return ret;
	}

	public MapInfoObject getMapInfoObject(Area maparea, Point pixelsize) {
		Vector georef = new Vector(4);

		// calculate a rectangle in the according coordinate reference system
		CWPoint buttomleft = new CWPoint (maparea.buttomright.latDec, maparea.topleft.lonDec);
		CWPoint topright = new CWPoint (maparea.topleft.latDec, maparea.buttomright.lonDec);
		CWPoint topleft;
		CWPoint buttomright;
		if (coordinateReferenceSystem == 31466 || coordinateReferenceSystem == 31467) {
			GkPoint buttomleftgk = TransformCoordinates.wgs84ToGkGermany(buttomleft);
			GkPoint toprightgk = TransformCoordinates.wgs84ToGkGermany(topright);
			GkPoint topleftgk = new GkPoint(buttomleftgk.easting, toprightgk.northing);
			GkPoint buttomrightgk = new GkPoint(toprightgk.easting, buttomleftgk.northing);
			topleft = TransformCoordinates.gkGermanyToWgs84(topleftgk);
			buttomright = TransformCoordinates.gkGermanyToWgs84(buttomrightgk);
		} else if (coordinateReferenceSystem == 4326) {
			topleft = maparea.topleft;
			buttomright = maparea.buttomright;
		} else throw new IllegalArgumentException("getMapInfoObject: Coordinate system not supported by cachewolf: " + coordinateReferenceSystem);

		georef.add(new GCPoint(topleft, new Point(0, 0)));
		georef.add(new GCPoint(buttomright, new Point(pixelsize.x, pixelsize.y)));
		georef.add(new GCPoint(buttomleft, new Point(0, pixelsize.y)));
		georef.add(new GCPoint(topright, new Point(pixelsize.x, 0)));
		
		MapInfoObject ret = new MapInfoObject();
		ret.evalGCP(georef, pixelsize.x, pixelsize.y);
		return ret;
	}
}