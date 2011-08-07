/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://developer.berlios.de/projects/cachewolf/
for more information.
Contact: 	bilbowolf@users.berlios.de
			kalli@users.berlios.de

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.Common;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.MyLocale;
import CacheWolf.STRreplace;
import CacheWolf.UrlFetcher;
import CacheWolf.utils.FileBugfix;
import ewe.fx.Point;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileInputStream;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.Convert;
import ewe.sys.Double;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.Properties;
import ewe.util.StandardComparer;
import ewe.util.Utils;
import ewe.util.Vector;
import ewe.util.mString;

/**
 *
 * start offset for language file: 4800
 */

// Um Karten zu holen!
// http://www.expedia.de/pub/agent.dll?qscr=mrdt&ID=3kQaz.&CenP=48.09901667,11.35688333&Lang=EUR0407&Alti=1&Size=600,600&Offs=0.000000,0.000000&Pins=|5748|
// oder
// http://www.expedia.de/pub/agent.dll?qscr=mrdt&ID=3kQaz.&CenP=48.15,11.5833&Alti=2&Lang=EUR0407&Size=900,900&Offs=0,0&MapS=0&Pins=|48.15,11.5833|4|48.15,11.5833&Pins=|48.15,11.5833|1|48.15,%2011.5833||

public class MapLoader {
	InfoBox progressInfobox;

	Vector onlineMapServices = new Vector();
	OnlineMapService currentOnlineMapService;
	int numMapsY;
	int numMapsX;
	double latinc;
	double loninc;
	CWPoint topleft;
	CWPoint bottomright;
	Point tilesSize;
	float tileScale;
	boolean fetchOnlyMapWithCache = false;

	/**
	 *
	 * @param prxy
	 * @param prt
	 * @param wmspath
	 *            without trailing "/"
	 */
	public MapLoader(String wmspath) {
		long start = new Time().getTime();
		InfoBox progressBox = null;
		boolean showprogress = false;

		onlineMapServices = new Vector();
		String dateien[];
		FileBugfix files = new FileBugfix(wmspath);
		String FileName;
		OnlineMapService tempOMS;
		MessageBox f = null;
		dateien = files.list("*.wms", FileBase.LIST_FILES_ONLY); // "*.xyz" doesn't work on some systems -> use FileBugFix
		for (int i = 0; i < dateien.length; i++) {
			FileName = dateien[i];
			try {
				if (!showprogress && ((i & 0) == 0) && (new Time().getTime() - start > 100)) { // reason for (i & 7 == 0): test time only after i is incremented 15 times
					showprogress = true;
					progressBox = new InfoBox(MyLocale.getMsg(327, "Info"), MyLocale.getMsg(4800, "Loading online map services"));
					progressBox.exec();
					progressBox.waitUntilPainted(500);
					ewe.sys.Vm.showWait(true);
				}
				tempOMS = new WebMapService(STRreplace.replace(wmspath + "/" + FileName, "//", "/"));
				onlineMapServices.add(tempOMS);
			} catch (Exception ex) {
				if (f == null)
					(f = new MessageBox(MyLocale.getMsg(144, "Warning"), MyLocale.getMsg(4801, "Ignoring error while \n reading web map service definition file \n") + ex.toString(), FormBase.OKB)).exec();
			}
		}
		tempOMS = new ExpediaMapService();
		onlineMapServices.add(tempOMS);
		if (progressBox != null) {
			progressBox.close(0);
			ewe.sys.Vm.showWait(false);
		}
	}

	public void setFetchOnlyMapWithCache(boolean value) {
		fetchOnlyMapWithCache = value;
	}

	public String[] getAvailableOnlineMapServices() {
		int s = onlineMapServices.size();
		String[] services = new String[s];
		for (int i = 0; i < s; i++) {
			services[i] = onlineMapServices.get(i).toString();
		}
		return services;
	}

	public void setCurrentMapService(int index) {
		if (index == -1)
			index = 0; // if no center set
		currentOnlineMapService = (OnlineMapService) onlineMapServices.get(index);
	}

	/**
	 * calculates the Expedia Alti = scale which fits in distance to its edges
	 *
	 * @param center
	 * @param distance
	 *            in meters
	 * @return meters per pixel calculatet in a way that the circle around center
	 *         is completly within the map
	 */

	public static float getScale(CWPoint center, float distance, Point size) {
		float scaleLatO = distance * 2 / size.y;
		float scaleLonO = distance * 2 / size.x;
		float scaleO = (scaleLatO < scaleLonO ? scaleLonO : scaleLatO);
		return scaleO;
	}

	/**
	 * download maps from expedia at zoomlevel alti and save the maps and the .wfl
	 * in path
	 *
	 * @param center
	 *            centre of all tiles
	 * @param radius
	 *            in meters
	 * @param scale
	 *            in "alti" value 1 alti = 3950 /2817.947378 = 1,046861280317350198581316446428 meters per pixel
	 * @param size
	 *            in pixels
	 * @param overlapping
	 *            in pixels
	 * @param path
	 *            without "/" at the end
	 *
	 */
	public void setTiles(CWPoint center, float radius, float scale, Point size, int overlapping) {
		double metersPerLat = (1000 * (new CWPoint(0, 0)).getDistance(new CWPoint(1, 0)));
		double metersPerLon = metersPerLat * java.lang.Math.cos(center.latDec / 180 * java.lang.Math.PI);
		topleft = new CWPoint(center.latDec + (radius / metersPerLat), center.lonDec - (radius / metersPerLon));
		bottomright = new CWPoint(center.latDec - (radius / metersPerLat), center.lonDec + (radius / metersPerLon));

		this.setTiles(topleft, bottomright, scale, size, overlapping);
	}

	public void setTiles(CWPoint toplefti, CWPoint bottomrighti, float scale, Point size, int overlapping) {
		// if (toplefti.latDec <= bottomrighti.latDec || toplefti.lonDec >= toplefti.lonDec) throw new IllegalArgumentException("topleft must be left and above bottom right");
		topleft = new CWPoint(toplefti);
		bottomright = new CWPoint(bottomrighti);
		// veraltet: Berechnung auf Kugel: double metersPerLat = (1000.0 * (new CWPoint(0, 0)).getDistance(new CWPoint(1, 0)));
		// veraltet: Berechnung auf Kugel: double metersPerLon = metersPerLat * java.lang.Math.cos((toplefti.latDec + bottomright.latDec) / 2 / 180 * java.lang.Math.PI);

		CWPoint center = new CWPoint((topleft.latDec + bottomright.latDec)/2, (toplefti.lonDec + bottomrighti.lonDec)/2);
		CWPoint centerleft = new CWPoint((topleft.latDec + bottomright.latDec)/2, toplefti.lonDec);
		CWPoint centerbottom = new CWPoint(bottomright.latDec, (toplefti.lonDec + bottomrighti.lonDec)/2);
		double metersPerLat = 1000.0 * center.getDistance(centerbottom)/(center.latDec - centerbottom.latDec);
		double metersPerLon = 1000.0 * center.getDistance(centerleft  )/(center.lonDec - centerleft.lonDec);

		double metersperpixel = currentOnlineMapService.getMetersPerPixel(scale);
		double pixelsPerLat = metersPerLat / metersperpixel;
		double pixelsPerLon = metersPerLon / metersperpixel;

		// over all pixelsize without borders
		double pixelsY = (topleft.latDec - bottomright.latDec) * pixelsPerLat;
		double pixelsX = -(topleft.lonDec - bottomright.lonDec) * pixelsPerLon;

		// border sizes around given area and overlapping between tiles
		// int borderX = (int) java.lang.Math.round((float)size.x * (overlapping - 1.0));
		// int borderY = (int) java.lang.Math.round((float)size.y * (overlapping - 1.0));
		int borderX = overlapping;
		int borderY = overlapping;

		numMapsY = (int) java.lang.Math.ceil((pixelsY + borderY) / (size.y - borderY));
		numMapsX = (int) java.lang.Math.ceil((pixelsX + borderX) / (size.x - borderX));

		// increments calulated from pixel offset of tiles
		latinc = -(size.y - borderY) / pixelsPerLat;
		loninc = (size.x - borderX) / pixelsPerLon;

		// calculation of centre of first tile

		// additional size for borders and rounding
		double oversizeX = (numMapsX * (size.x - borderX) + borderX) - pixelsX;
		double oversizeY = (numMapsY * (size.y - borderY) + borderY) - pixelsY;

		// offset for upper left corner
		double offsetLat = -((size.y - oversizeY) / 2.0) / pixelsPerLat;
		double offsetLon = ((size.x - oversizeX) / 2.0) / pixelsPerLon;

		topleft.latDec += offsetLat;
		topleft.lonDec += offsetLon;

		this.tilesSize = new Point();
		this.tilesSize.set(size);
		this.tileScale = scale;
	}

	public void downlaodTiles(String tilesPath) {
		double lat = topleft.latDec;
		double lon = topleft.lonDec;
		CWPoint center = new CWPoint();
		for (int row = 1; row <= numMapsY; row++) {
			lon = topleft.lonDec;
			for (int col = 1; col <= numMapsX; col++) {
				center.set(lat, lon);
				if (!fetchOnlyMapWithCache || hasCache(center, latinc, loninc)) {
					if (progressInfobox != null)
						progressInfobox.setInfo(MyLocale.getMsg(4802, "Downloading calibrated (georeferenced) \n map image \n '") + currentOnlineMapService.getName() + MyLocale.getMsg(4803, "' \n Downloading tile \n row") + " " + row + " / " + numMapsY
								+ MyLocale.getMsg(4804, " column") + " " + col + " / " + numMapsX);
					try {
						downloadMap(center, tileScale, tilesSize, tilesPath);
					} catch (Exception e) {
						this.progressInfobox.addWarning(MyLocale.getMsg(4805, "Tile") + " " + row + "/" + col + MyLocale.getMsg(4806, ": Ignoring error:") + " " + e.getMessage() + "\n");
					}
					if (progressInfobox.isClosed)
						return;
				}
				lon += loninc;
			}
			lat += latinc;
		}
	}

	private boolean hasCache(CWPoint center, double latinc, double loninc) {
		double lat = center.latDec - (latinc / 2.0);
		double lon = center.lonDec - (loninc / 2.0);
		CWPoint tl = new CWPoint(lat, lon);
		lat = center.latDec + (latinc / 2.0);
		lon = center.lonDec + (loninc / 2.0);
		CWPoint br = new CWPoint(lat, lon);
		Area maparea = new Area(tl, br);
		CacheDB cacheDB = Global.getProfile().cacheDB;
		for (int i = 0; i < cacheDB.size(); i++) {
			CacheHolder ch = cacheDB.get(i);
			if (maparea.isInBound(ch.getPos())) {
				return true;
			}
		}
		return false;
	}

	public void setProgressInfoBox(InfoBox progrssInfoboxi) {
		progressInfobox = progrssInfoboxi;
	}

	/**
	 *
	 * @param center
	 * @param scale
	 * @param pixelsize
	 * @param path
	 *            must include trailing "/"
	 * @throws Exception
	 */
	public void downloadMap(CWPoint center, float scale, Point pixelsize, String path) throws Exception {
		MapInfoObject mio = currentOnlineMapService.getMapInfoObject(center, scale, pixelsize);
		String filename = createFilename(mio.getCenter(), mio.scale);
		String imagename = mio.setName(path, filename);
		String imagetype = currentOnlineMapService.getImageFileExt();
		String url = currentOnlineMapService.getUrlForCenterScale(center, scale, pixelsize);
		String fName = path + imagename + imagetype;
		FileBugfix fn = new FileBugfix(path + imagename + ".wfl");
		FileBugfix fn1 = new FileBugfix(fName);
		if (!fn.exists() || fn.length() == 0 || !fn1.exists() || fn1.length() == 0) {
			if (currentOnlineMapService instanceof ExpediaMapService) {
				downloadImage(url, path + imagename + imagetype);
			} else {
				WebMapService wms = (WebMapService) currentOnlineMapService;
				if (wms.requestUrlPart.startsWith("REQUEST")) {
					downloadImage(url, path + imagename + imagetype);
				} else {
					Area maparea = wms.CenterScaleToArea(center, scale, pixelsize);
					CWPoint bottomleft = new CWPoint(maparea.bottomright.latDec, maparea.topleft.lonDec);
					CWPoint topright = new CWPoint(maparea.topleft.latDec, maparea.bottomright.lonDec);

					String mapProgramPath = wms.versionUrlPart + "/";
					mapProgramPath = mapProgramPath.replace('/', FileBase.separatorChar);
					String mapProgram = mapProgramPath + wms.MainUrl;
					File f = new FileBugfix(mapProgram);
					if (!f.exists() || !f.canRead()) {
						MessageBox mb = new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1834, "Please enter the correct path to Kosmos.Console.exe into the wms-file."), ewe.ui.MessageBox.OKB);
						mb.execute();
						return;
					}

					String mapProgramParams = "";

					if (wms.requestUrlPart.equalsIgnoreCase("Kosmos")) {
						// minx miny maxx maxy + pixelsize.x
						mapProgramParams = "bitmapgen" + " \"" + FileBase.getProgramDirectory().replace('/', File.separatorChar) + "\\" + wms.serviceTypeUrlPart + "\"" + " \"" + path.replace('/', File.separatorChar) + imagename + imagetype + "\""
								+ " -mb " + bottomleft.toString(TransformCoordinates.LAT_LON).replace(',', ' ') + " " + topright.toString(TransformCoordinates.LAT_LON).replace(',', ' ') + " -w " + pixelsize.x;
						Vm.exec(mapProgram, mapProgramParams, 0, true);
					} else {
						if (wms.requestUrlPart.equalsIgnoreCase("Maperitive")) {
							// Maperitive runs on Windows and Linux
							// generating scriptfile for Maperitive from wmsfile
							String cwPath = FileBase.getProgramDirectory().replace('/', FileBase.separatorChar) + FileBase.separatorChar;
							String scriptFileName = cwPath + "maperitive.script";

							PrintWriter outp = new PrintWriter(new BufferedWriter(new FileWriter(scriptFileName)));
							outp.println("use-ruleset alias=default");
							outp.println("clear-map");

							if (wms.serviceTypeUrlPart.equals("")) {
								outp.println("add-web-map");
							} else {
								outp.println("add-web-map provider=" + wms.serviceTypeUrlPart);
							}

							if (!wms.stylesUrlPart.equals("")) {
								String myrules = mapProgramPath + wms.stylesUrlPart.replace('/', FileBase.separatorChar);
								outp.println("use-ruleset location=" + myrules);
								// outp.println("apply-ruleset");
							}
							if (!wms.layersUrlPart.equals("")) {
								outp.println("clear-map");
								outp.println("load-source " + mapProgramPath + wms.layersUrlPart.replace('/', FileBase.separatorChar));
								// implicit does apply-ruleset
							}

							String koords = bottomleft.toString(TransformCoordinates.LON_LAT) + "," + topright.toString(TransformCoordinates.LON_LAT);
							outp.println("bounds-set " + koords);
							outp.println("zoom-bounds");
							if (path.indexOf(':') == 1) {
								outp.print("export-bitmap file=" + "\"" + fName + "\"");
							} else {
								outp.print("export-bitmap file=" + fName);
							}
							outp.print(" bounds=" + koords);
							String pxSize = " width=" + pixelsize.x + " height=" + pixelsize.y;
							outp.print(pxSize);
							outp.println(" kml=false");
							outp.close();
							// executing the generated script
							if (mapProgram.indexOf(':') == 1) {
								mapProgramParams = "-exitafter " + "\"" + scriptFileName + "\"";
							} else {
								mapProgramParams = "-exitafter " + scriptFileName;
							}
							Vm.exec(mapProgram, mapProgramParams, 0, true);
							// preparation for generating wfl from the ozi map-file
							Vector GCPs = map2wfl(path + imagename);
							mio.evalGCP(GCPs, pixelsize.x, pixelsize.y);
							// can not supress genaration of pgw,jgw-file
							FileBugfix pgwFile = new FileBugfix(path + imagename + ".pgw"); // seems to bee for png
							pgwFile.delete();
							FileBugfix jgwFile = new FileBugfix(path + imagename + ".jgw"); // seems to bee for jpg
							jgwFile.delete();
						}
					}
				}
			}
			mio.saveWFL();
		}
	}

	private Vector map2wfl(String pathAndImageName) {
		Vector GCPs = new Vector();
		File mapFile = new File(pathAndImageName + ".map");
		if (mapFile.exists()) {
			GCPoint gcp1 = new GCPoint();
			GCPoint gcp2 = new GCPoint();
			GCPoint gcp3 = new GCPoint();
			GCPoint gcp4 = new GCPoint();
			GCPoint gcpG = new GCPoint();
			String line = "";
			String[] parts;
			try {
				FileReader inMap = new FileReader(pathAndImageName + ".map");
				while ((line = inMap.readLine()) != null) {
					if (line.equals("MMPNUM,4")) {

						line = inMap.readLine();
						parts = mString.split(line, ',');
						gcp1.bitMapX = Convert.toInt(parts[2]);
						gcp1.bitMapY = Convert.toInt(parts[3]);
						if (gcp1.bitMapX == 0)
							gcp1.bitMapX = 1;
						if (gcp1.bitMapY == 0)
							gcp1.bitMapY = 1;

						line = inMap.readLine();
						parts = mString.split(line, ',');
						gcp2.bitMapX = Convert.toInt(parts[2]);
						gcp2.bitMapY = Convert.toInt(parts[3]);
						if (gcp2.bitMapX == 0)
							gcp2.bitMapX = 1;
						if (gcp2.bitMapY == 0)
							gcp2.bitMapY = 1;

						line = inMap.readLine();
						parts = mString.split(line, ',');
						gcp3.bitMapX = Convert.toInt(parts[2]);
						gcp3.bitMapY = Convert.toInt(parts[3]);
						if (gcp3.bitMapX == 0)
							gcp3.bitMapX = 1;
						if (gcp3.bitMapY == 0)
							gcp3.bitMapY = 1;
						// imageWidth = gcp3.bitMapX;
						// imageHeight = gcp3.bitMapY;

						line = inMap.readLine();
						parts = mString.split(line, ',');
						gcp4.bitMapX = Convert.toInt(parts[2]);
						gcp4.bitMapY = Convert.toInt(parts[3]);
						if (gcp4.bitMapX == 0)
							gcp4.bitMapX = 1;
						if (gcp4.bitMapY == 0)
							gcp4.bitMapY = 1;

						line = inMap.readLine();
						parts = mString.split(line, ',');
						if (MyLocale.getDigSeparator().equals(",")) {
							parts[3] = parts[3].replace('.', ',');
							parts[2] = parts[2].replace('.', ',');
						}
						gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
						gcpG.bitMapX = gcp1.bitMapX;
						gcpG.bitMapY = gcp1.bitMapY;
						GCPs.add(gcpG);

						line = inMap.readLine();
						parts = mString.split(line, ',');
						if (MyLocale.getDigSeparator().equals(",")) {
							parts[3] = parts[3].replace('.', ',');
							parts[2] = parts[2].replace('.', ',');
						}
						gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
						gcpG.bitMapX = gcp2.bitMapX;
						gcpG.bitMapY = gcp2.bitMapY;
						GCPs.add(gcpG);

						line = inMap.readLine();
						parts = mString.split(line, ',');
						if (MyLocale.getDigSeparator().equals(",")) {
							parts[3] = parts[3].replace('.', ',');
							parts[2] = parts[2].replace('.', ',');
						}
						gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
						gcpG.bitMapX = gcp3.bitMapX;
						gcpG.bitMapY = gcp3.bitMapY;
						GCPs.add(gcpG);

						line = inMap.readLine();
						parts = mString.split(line, ',');
						if (MyLocale.getDigSeparator().equals(",")) {
							parts[3] = parts[3].replace('.', ',');
							parts[2] = parts[2].replace('.', ',');
						}
						gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
						gcpG.bitMapX = gcp4.bitMapX;
						gcpG.bitMapY = gcp4.bitMapY;
						GCPs.add(gcpG);
					} // if
				} // while
				inMap.close();
			} catch (IllegalArgumentException ex) { // is thrown from Convert.toDouble and saveWFL if affine[0-5]==0 NumberFormatException is a subclass of IllegalArgumentExepction
				Global.getPref().log(MyLocale.getMsg(4117, "Error while importing .map-file: "), ex);
			} catch (IOException ex) {
				Global.getPref().log(MyLocale.getMsg(4118, "IO-Error while reading or writing calibration file"), ex);
			}
			mapFile.delete();
		} else { // if map file.exists
			Global.getPref().log(MyLocale.getMsg(4119, "No calibration file found for: "), null);
		}
		return GCPs;
	}

	public String createFilename(CWPoint center, float scale) {
		String filename = Common.ClearForFileName(currentOnlineMapService.getNameForFileSystem() + "_s" + Common.DoubleToString(scale, 0, 1) + "_c" + center.toString(TransformCoordinates.LAT_LON).replace(',', '-'));
		return filename;
	}

	/**
	 * @param url
	 *            usual URL. If a redirect is requiered (as in the case of
	 *            Expedia, add an "R" before "http://" --> Don't download the url, retry until getting a http-redirect
	 *            this is necessary for expedia as it delivers the image only after a http-redirect
	 *            and sometimes doesn't send a redirect on the first try
	 * @param datei
	 *            path and name of file to save to
	 */
	public void downloadImage(String url, String datei) throws IOException {
		String realurl;
		boolean forceredirect;
		if (url.startsWith("R")) {
			forceredirect = true;
			realurl = url.substring(1, url.length());
		} else {
			forceredirect = false;
			realurl = url;
		}
		File dateiF = new FileBugfix(datei);
		if (!dateiF.exists()) {
			if (forceredirect)
				UrlFetcher.setForceRedirect();
			UrlFetcher.fetchDataFile(realurl, datei);
			String ct = null;
			try {
				ct = (String) UrlFetcher.getDocumentProperties().getValue("content-type", "no-content-type provided");
			} catch (Exception e) {
				ct = "document property content-type does not exist!";
			}
			if (!ct.substring(0, 5).equalsIgnoreCase("image")) {
				dateiF = new FileBugfix(datei);
				dateiF.delete();
				throw new IOException(MyLocale.getMsg(4808, "downloadImage: content-type:") + " " + ct + MyLocale.getMsg(4809, " is not an image, begin of content:") + " (deleted)");
			}
		}
	}
}

class OnlineMapService {
	/** Friendly name of the service */
	String name;
	/** Type of map (used as directory name when downloading maps. We currently have "topo" and "photo" defined as map types */
	String mapType;
	/** Esentially the same as name, but used for the file system. It will be part of the names of the downloaded images */
	String filename;
	String MainUrl; // http://www.geoserver.nrw.de/GeoOgcWms1.3/servlet/TK25?SERVICE=WMS
	/** including "." */
	String imageFileExt; // ".gif", ".jpg"...
	double recommendedScales[];
	int preselectedRecScaleIndex = 0;
	double minscale;
	double maxscale;
	Area boundingBox;

	/**
	 * This method is used in case the online map service provides only certain steps of
	 * zoomlevels. In this case the scale in meters per pixel must be returned, which
	 * will be used instead of the wished scale.
	 *
	 * @param scale
	 * @return
	 */
	public float getMetersPerPixel(float scale) {
		return scale;
	}

	public String getImageFileExt() {
		return imageFileExt;
	}

	/**
	 * Overlaod this to integrate name of layers
	 *
	 * @return friendly service name
	 */
	public String getName() {
		return name;
	}

	/**
	 * This method is called to get a name of the
	 * online map service which will be part of the filename
	 * used for the downloaded image
	 *
	 * @return friendly service name
	 */
	public String getNameForFileSystem() {
		return filename;
	}

	public String getMapType() {
		return mapType;
	}

	/**
	 * Overload this and return the URL to the map image, don't call super
	 * Alternatively overload getUrlForBoundingBoxInternal
	 * You must overload either this method or getUrlForBoundingBox
	 *
	 * @param center
	 * @param scale
	 * @param pixelsize
	 * @return
	 */
	public String getUrlForCenterScale(CWPoint center, float scale, Point pixelsize) {
		Area bbox = CenterScaleToArea(center, scale, pixelsize);
		String url = getUrlForBoundingBoxInternal(bbox, pixelsize);
		return url;
	}

	/**
	 * This is made protected and named "...Internal" because a lot of services
	 * don't work correctly when a map is requested, that is not exactly quadratic
	 * --> alway use getUrlForCenter...
	 *
	 * @param surArea
	 * @param pixelsize
	 * @return
	 */
	protected String getUrlForBoundingBoxInternal(Area surArea, Point pixelsize) {
		return null;
	}

	/**
	 * overload this if your map service uses a special projection
	 * an return an Area that is quadratic in that projection
	 *
	 * @param center
	 * @param scale
	 * @param pixelsize
	 * @return
	 */
	public Area CenterScaleToArea(CWPoint center, float scale, Point pixelsize) {
		Area bbox = new Area();
		double halfdiagonal = Math.sqrt(pixelsize.x * pixelsize.x + pixelsize.y * pixelsize.y) / 2 * scale / 1000;
		bbox.topleft = center.project(-45, halfdiagonal);
		bbox.bottomright = center.project(135, halfdiagonal);
		return bbox;
	}

	protected MapInfoObject getMapInfoObjectInternal(Area maparea, Point pixelsize) {
		throw new IllegalArgumentException(MyLocale.getMsg(4811, "OnlineMapService: getMapInfoObjectInternal(Area maparea, Point pixelsize):\n This method must be overloaded in order to be able to use it"));
	}

	/**
	 * Overload this (don't call super()) or alternatively overload getMapInfoObjectInternal
	 *
	 * @param center
	 * @param scale
	 * @param pixelsize
	 * @return
	 */
	public MapInfoObject getMapInfoObject(CWPoint center, float scale, Point pixelsize) {
		return getMapInfoObjectInternal(CenterScaleToArea(center, scale, pixelsize), pixelsize);
	}

	public String toString() {
		return getName();
	}
}

class WebMapService extends OnlineMapService {
	String layersUrlPart; //
	String versionUrlPart; // VERSION=1.1.0
	String serviceTypeUrlPart; // "SERVICE=WMS"
	int coordinateReferenceSystem[]; // WGS84: 4326, German GK: 31466 /
	String coordinateReferenceSystemUrlPart[]; // &SRS=EPSG:31466
	public String requestUrlPart;
	String imageFormatUrlPart; // FORMAT=image/png
	String stylesUrlPart; // STYLES=
	double minscaleWMS;
	double maxscaleWMS;

	/**
	 *
	 * @param filename
	 *            without file extension
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public WebMapService(String filename_) throws IOException, IllegalArgumentException {
		FileInputStream in = new FileInputStream(filename_);
		Properties wms = new Properties();
		wms.load(in);
		in.close();
		String tmp = FileBase.getFileExt(filename_);
		this.filename = tmp.substring(0, tmp.lastIndexOf('.'));
		name = wms.getProperty("Name", "").trim();
		if (name == "")
			throw new IllegalArgumentException(MyLocale.getMsg(4812, "WebMapService: property >Name:< missing in file:\n") + filename);
		MainUrl = wms.getProperty("MainUrl", "").trim();
		;
		if (MainUrl == "")
			throw new IllegalArgumentException(MyLocale.getMsg(4813, "WebMapService: property >MainUrl:< missing in file:\n") + filename);
		mapType = wms.getProperty("MapType", "maptype_unknown").trim();
		serviceTypeUrlPart = wms.getProperty("ServiceTypeUrlPart", "SERVICE=WMS").trim();
		layersUrlPart = wms.getProperty("LayersUrlPart", "").trim();
		;
		versionUrlPart = wms.getProperty("VersionUrlPart", "").trim();
		;
		tmp = wms.getProperty("CoordinateReferenceSystemCacheWolf", "").trim();
		if (tmp.equals(""))
			throw new IllegalArgumentException(MyLocale.getMsg(4814, "WebMapService: no CoordinateReferenceSystemCacheWolf given"));
		String[] tmp2 = mString.split(tmp, ' ');
		coordinateReferenceSystem = new int[tmp2.length];
		for (int i = 0; i < tmp2.length; i++) {
			coordinateReferenceSystem[i] = Common.parseInt(tmp2[i].trim());
			if (!TransformCoordinates.isSupported(coordinateReferenceSystem[i]))
				throw new IllegalArgumentException(MyLocale.getMsg(4815, "Coordinate reference system not supported by CacheWolf:\n") + coordinateReferenceSystem[i]);
		}
		tmp = wms.getProperty("CoordinateReferenceSystemUrlPart", "").trim();
		if (tmp == "")
			throw new IllegalArgumentException(MyLocale.getMsg(4816, "WebMapService: property >CoordinateReferenceSystemUrlPart:< missing in file:\n") + filename);
		tmp2 = mString.split(tmp, ' ');
		if (tmp2.length != coordinateReferenceSystem.length)
			throw new IllegalArgumentException(MyLocale.getMsg(4817, "number of strings in CoordinateReferenceSystemUrlPart (") + tmp2.length + MyLocale.getMsg(4818, ") must match the number of codes in CoordinateReferenceSystemCacheWolf (")
					+ coordinateReferenceSystem.length + MyLocale.getMsg(4819, ") use normal space as separator"));
		coordinateReferenceSystemUrlPart = new String[tmp2.length];
		for (int i = 0; i < tmp2.length; i++) {
			coordinateReferenceSystemUrlPart[i] = tmp2[i].trim();
			if (coordinateReferenceSystemUrlPart[i] == "")
				throw new IllegalArgumentException(MyLocale.getMsg(4820, "WebMapService: property >CoordinateReferenceSystemUrlPart:< incorrect in file:\n") + filename);
		}
		requestUrlPart = wms.getProperty("RequestUrlPart", "REQUEST=GetMap").trim();
		imageFormatUrlPart = wms.getProperty("ImageFormatUrlPart", "").trim();
		stylesUrlPart = wms.getProperty("StylesUrlPart", "").trim();
		String topleftS = wms.getProperty("BoundingBoxTopLeftWGS84", "").trim();
		String bottomrightS = wms.getProperty("BoundingBoxBottomRightWGS84");
		// To be backward-compatible with mispelled property-name: Don't remove these lines until all wms-Files has been changed
		if (bottomrightS == null) {
			bottomrightS = wms.getProperty("BoundingBoxButtomRightWGS84", "");
		}
		bottomrightS.trim();
		CWPoint topleft = new CWPoint(topleftS);
		CWPoint bottomright = new CWPoint(bottomrightS);
		if (!topleft.isValid())
			topleft.set(90, -180);
		if (!bottomright.isValid())
			bottomright.set(-90, 180);
		boundingBox = new Area(topleft, bottomright);
		minscaleWMS = Common.parseDouble(wms.getProperty("MinScale", "0").trim());
		maxscaleWMS = Common.parseDouble(wms.getProperty("MaxScale", Convert.toString(java.lang.Double.MAX_VALUE)).trim());
		minscale = minscaleWMS / Math.sqrt(2); // in WMS scale is measured diagonal while in CacheWolf it is measured vertical
		maxscale = maxscaleWMS / Math.sqrt(2);
		imageFileExt = wms.getProperty("ImageFileExtension", "").trim();
		if (imageFileExt == "")
			throw new IllegalArgumentException(MyLocale.getMsg(4821, "WebMapService: property >ImageFileExtension:< missing in file:\n") + filename);
		String[] recommendedScalesStr = mString.split(wms.getProperty("RecommendedScale", "5").trim(), ' ');
		// convert recommended scales to doube[], sort them and set preselected recommended scale
		if (recommendedScalesStr.length > 0) {
			double preselected = Common.parseDouble(recommendedScalesStr[0]);
			Double[] recommendedScalesObj = new Double[recommendedScalesStr.length];
			for (int i = 0; i < recommendedScalesObj.length; i++) {
				recommendedScalesObj[i] = new Double();
				recommendedScalesObj[i].set(Common.parseDouble(recommendedScalesStr[i].replace(',', '.')));
			}
			Utils.sort(recommendedScalesObj, new StandardComparer(), false);
			recommendedScales = new double[recommendedScalesStr.length];
			for (int i = 0; i < recommendedScales.length; i++) {
				recommendedScales[i] = recommendedScalesObj[i].value;
				if (recommendedScales[i] == preselected)
					preselectedRecScaleIndex = i;
			}
		}
	}

	private static final int TOPLEFT_INDEX = 0;
	private static final int BOTTOMRIGHT_INDEX = 1;
	private static final int TOPRIGHT_INDEX = 2;
	private static final int BOTTOMLEFT_INDEX = 3;

	/**
	 *
	 * @param maparea
	 * @return [0] = topleft, [1] = bottomright, [2] = topright, [3] = bottomleft
	 */
	private ProjectedPoint[] getGkArea(Area maparea) {
		ProjectedPoint[] ret = new ProjectedPoint[4];
		// CWPoint topright = new CWPoint(maparea.topleft.latDec, maparea.bottomright.lonDec);
		// CWPoint bottomleft = new CWPoint(maparea.bottomright.latDec, maparea.topleft.lonDec);
		int crs = getCrs(maparea.getCenter());
		// FIXME region is never read. Needed?
		// int region = TransformCoordinates.getLocalProjectionSystem(coordinateReferenceSystem[crs]);
		ret[TOPLEFT_INDEX] = TransformCoordinates.wgs84ToEpsg(maparea.topleft, coordinateReferenceSystem[crs]);
		ret[BOTTOMRIGHT_INDEX] = TransformCoordinates.wgs84ToEpsg(maparea.bottomright, coordinateReferenceSystem[crs]);
		ret[TOPRIGHT_INDEX] = ret[BOTTOMRIGHT_INDEX].cloneIt();
		ret[TOPRIGHT_INDEX].shift(ret[TOPLEFT_INDEX].getNorthing() - ret[BOTTOMRIGHT_INDEX].getNorthing(), 0); // was: new GkPoint(ret[BUTTOMRIGHT_INDEX].getEasting(region), ret[TOPLEFT_INDEX].northing, ret[TOPLEFT_INDEX].stripewidth,
																												// ret[TOPLEFT_INDEX].lengthOfStripe0);
		ret[BOTTOMLEFT_INDEX] = ret[BOTTOMRIGHT_INDEX].cloneIt();
		ret[BOTTOMLEFT_INDEX].shift(ret[TOPLEFT_INDEX].getEasting() - ret[BOTTOMRIGHT_INDEX].getEasting(), 1); // was: new GkPoint(ret[TOPLEFT_INDEX].getEasting(region), ret[BUTTOMRIGHT_INDEX].northing, ret[TOPLEFT_INDEX].stripewidth,
																												// ret[TOPLEFT_INDEX].lengthOfStripe0);
		// ret[2] = TransformCoordinates.wgs84ToGermanGk(topright, coordinateReferenceSystem[crs]);
		// ret[3] = TransformCoordinates.wgs84ToGermanGk(bottomleft, coordinateReferenceSystem[crs]);
		return ret;
	}

	public Area CenterScaleToArea(CWPoint center, float scale, Point pixelsize) {
		Area bbox = new Area();
		int region = TransformCoordinates.getLocalProjectionSystem(coordinateReferenceSystem[0]);
		if (region > 0) {
			int epsg = coordinateReferenceSystem[getCrs(center)];
			ProjectedPoint cgk = TransformCoordinates.wgs84ToEpsg(center, epsg);
			ProjectedPoint tlgk = cgk.cloneIt();
			tlgk.shift(-pixelsize.x * scale / 2, 1);
			tlgk.shift(pixelsize.y * scale / 2, 0);
			ProjectedPoint brgk = cgk.cloneIt();
			brgk.shift(pixelsize.x * scale / 2, 1);
			brgk.shift(-pixelsize.y * scale / 2, 0);
			bbox.topleft = TransformCoordinates.ProjectedEpsgToWgs84(tlgk, epsg); // old: (tlgk, region);
			bbox.bottomright = TransformCoordinates.ProjectedEpsgToWgs84(brgk, epsg); // TransformCoordinates.GkToWgs84(brgk, region);
		} else {
			switch (coordinateReferenceSystem[0]) {
			case TransformCoordinates.EPSG_ETRS89:
			case TransformCoordinates.EPSG_WGS84:
				bbox.topleft.set(center);
				bbox.topleft.shift(-pixelsize.x * scale / 2, 1);
				bbox.topleft.shift(pixelsize.y * scale / 2, 0);
				bbox.bottomright.set(center);
				bbox.bottomright.shift(pixelsize.x * scale / 2, 1);
				bbox.bottomright.shift(-pixelsize.y * scale / 2, 0);
				break;
			default:
				throw new IllegalArgumentException("CenterScaleToArea: epsg: " + coordinateReferenceSystem[0] + " not supported");
			}
		}
		return bbox;
	}

	protected String getUrlForBoundingBoxInternal(Area maparea, Point pixelsize) {
		if (!boundingBox.isOverlapping(maparea))
			throw new IllegalArgumentException(MyLocale.getMsg(4822, "area:") + " " + maparea.toString() + MyLocale.getMsg(4823, " not covered by service:") + " " + name + MyLocale.getMsg(4824, ", service area:") + " " + boundingBox.toString());
		// http://www.geoserver.nrw.de/GeoOgcWms1.3/servlet/TK25?SERVICE=WMS&VERSION=1.1.0&REQUEST=GetMap&SRS=EPSG:31466&BBOX=2577567.0149,5607721.7566,2578567.0077,5608721.7602&WIDTH=500&HEIGHT=500&LAYERS=Raster:TK25_KMF:Farbkombination&STYLES=&FORMAT=image/png
		CWPoint bottomleft = new CWPoint(maparea.bottomright.latDec, maparea.topleft.lonDec);
		CWPoint topright = new CWPoint(maparea.topleft.latDec, maparea.bottomright.lonDec);
		double scaleh = maparea.bottomright.getDistance(bottomleft) * 1000 / pixelsize.x;
		double scalev = maparea.topleft.getDistance(topright) * 1000 / pixelsize.y;
		double scale = Math.sqrt(scaleh * scaleh + scalev * scalev); // meters per pixel measured diagonal
		if (scale < minscaleWMS || scale > maxscaleWMS)
			throw new IllegalArgumentException(MyLocale.getMsg(4825, "scale") + " " + scale / Math.sqrt(2) + MyLocale.getMsg(4826, " not supported by online map service, supported scale range:") + " " + minscale + " - " + maxscale
					+ MyLocale.getMsg(4827, " (measured in meters per pixel vertically)"));
		int crs = 0;
		String bbox = "BBOX=";
		int localsystem = TransformCoordinates.getLocalProjectionSystem(coordinateReferenceSystem[0]);
		if (localsystem > 0) {
			crs = getCrs(maparea.getCenter());
			ProjectedPoint[] gk = getGkArea(maparea);
			bottomleft = TransformCoordinates.ProjectedEpsgToWgs84(gk[BOTTOMLEFT_INDEX], coordinateReferenceSystem[crs]);
			topright = TransformCoordinates.ProjectedEpsgToWgs84(gk[TOPRIGHT_INDEX], coordinateReferenceSystem[crs]);
			bbox += TransformCoordinates.wgs84ToEpsg(bottomleft, coordinateReferenceSystem[crs]).toString(2, "", ",");
			bbox += "," + TransformCoordinates.wgs84ToEpsg(topright, coordinateReferenceSystem[crs]).toString(2, "", ",");
		} else if (coordinateReferenceSystem[0] == TransformCoordinates.EPSG_WGS84)
			bbox += bottomleft.toString(TransformCoordinates.LON_LAT) + "," + topright.toString(TransformCoordinates.LON_LAT);
		else
			throw new IllegalArgumentException(MyLocale.getMsg(4828, "Coordinate system not supported by cachewolf:") + " " + coordinateReferenceSystem.toString());
		String ret = MainUrl + serviceTypeUrlPart + "&" + versionUrlPart + "&" + requestUrlPart + "&" + coordinateReferenceSystemUrlPart[crs] + "&" + bbox + "&WIDTH=" + pixelsize.x + "&HEIGHT=" + pixelsize.y + "&" + layersUrlPart + "&" + stylesUrlPart
				+ "&" + imageFormatUrlPart;
		Global.getPref().log(ret + " WGS84: Bottom left: " + bottomleft.toString(TransformCoordinates.DD) + "top right: " + topright.toString(TransformCoordinates.DD));
		return ret;
	}

	/**
	 * This method gives the number in the array of coordinateReferenceSystems, which should be used
	 * a) if only one is in the array 0 is returned
	 * b) if there are more, find out which one matches the correct zone (e.g. Gau�-K�ger stripe)
	 * Call this routine with center of the area (use Area.getcenter())
	 *
	 * @param p
	 *            Point for which the epsg code is searched for
	 * @return
	 */
	private int getCrs(TrackPoint p) {
		int crsindex = 0;
		if (coordinateReferenceSystem.length > 1) {
			int ls = TransformCoordinates.getLocalProjectionSystem(coordinateReferenceSystem[0]);
			ProjectedPoint gkbl = TransformCoordinates.wgs84ToLocalsystem(p, ls); // TODO: think / read about what to do if bottom left and top right are not in the same Gau�-Kr�ger stripe?
			int wantepsg = gkbl.getEpsgCode();
			for (crsindex = 0; crsindex < coordinateReferenceSystem.length; crsindex++) {
				if (coordinateReferenceSystem[crsindex] == wantepsg)
					break;
			}
			if (crsindex >= coordinateReferenceSystem.length) { // not match
				for (crsindex = 0; crsindex < coordinateReferenceSystem.length; crsindex++) {
					if (Math.abs(coordinateReferenceSystem[crsindex] - wantepsg) == 1)
						break; // accept 1 zone deviation
				}
				if (crsindex >= coordinateReferenceSystem.length)
					crsindex = -1;

			}
			if (crsindex < 0)
				throw new IllegalArgumentException(MyLocale.getMsg(4829, "getUrlForBoundingBox: Point:") + " " + gkbl.toString() + MyLocale.getMsg(4830, "no matching Gau�-Kr�ger-Stripe in the EPSG-code list in the .wms"));
		}
		return crsindex;
	}

	protected MapInfoObject getMapInfoObjectInternal(Area maparea, Point pixelsize) {
		if (!boundingBox.isOverlapping(maparea))
			throw new IllegalArgumentException(MyLocale.getMsg(4822, "area:") + " " + maparea.toString() + MyLocale.getMsg(4823, " not covered by service:") + " " + name + MyLocale.getMsg(4824, ", service area:") + " " + boundingBox.toString());
		Vector georef = new Vector(4);

		// calculate a rectangle in the according coordinate reference system
		CWPoint bottomleft = new CWPoint(maparea.bottomright.latDec, maparea.topleft.lonDec);
		CWPoint topright = new CWPoint(maparea.topleft.latDec, maparea.bottomright.lonDec);
		CWPoint topleft = new CWPoint(maparea.topleft);
		CWPoint bottomright = new CWPoint(maparea.bottomright);
		double metersperpixalhorizontal = (bottomright.getDistance(bottomleft) + topleft.getDistance(topright)) / 2 * 1000 / pixelsize.x;
		double metersperpixalvertical = (bottomright.getDistance(topright) + topleft.getDistance(bottomleft)) / 2 * 1000 / pixelsize.y;
		int region = TransformCoordinates.getLocalProjectionSystem(coordinateReferenceSystem[0]);
		if (region > 0) {
			ProjectedPoint[] gk = getGkArea(maparea);
			// bounding box in WMS is defined around the pixels, not exactly on the pixels --> the bounding box must be reduced on all edges by half a pixel
			gk[TOPLEFT_INDEX].shift(metersperpixalhorizontal / 2, 1);
			gk[TOPLEFT_INDEX].shift(-metersperpixalvertical / 2, 0);
			gk[BOTTOMRIGHT_INDEX].shift(-metersperpixalhorizontal / 2, 1);
			gk[BOTTOMRIGHT_INDEX].shift(metersperpixalvertical / 2, 0);
			gk[TOPRIGHT_INDEX].shift(-metersperpixalhorizontal / 2, 1);
			gk[TOPRIGHT_INDEX].shift(-metersperpixalvertical / 2, 0);
			gk[BOTTOMLEFT_INDEX].shift(metersperpixalhorizontal / 2, 1);
			gk[BOTTOMLEFT_INDEX].shift(metersperpixalvertical / 2, 0);

			topleft.set(gk[TOPLEFT_INDEX].getNorthing(), gk[TOPLEFT_INDEX].getEasting());
			bottomright.set(gk[BOTTOMRIGHT_INDEX].getNorthing(), gk[BOTTOMRIGHT_INDEX].getEasting());
			topright.set(gk[TOPRIGHT_INDEX].getNorthing(), gk[TOPRIGHT_INDEX].getEasting());
			bottomleft.set(gk[BOTTOMLEFT_INDEX].getNorthing(), gk[BOTTOMLEFT_INDEX].getEasting());
		} else if (coordinateReferenceSystem[0] == TransformCoordinates.EPSG_WGS84) {
			// bounding box in WMS is defined around the pixels, not exactly on the pixels --> the bounding box must be reduced on all edges by half a pixel
			topleft.shift(metersperpixalhorizontal / 2, 1);
			topleft.shift(-metersperpixalvertical / 2, 0);
			bottomright.shift(-metersperpixalhorizontal, 1);
			bottomright.shift(metersperpixalhorizontal, 0);
			topright = new CWPoint(topleft.latDec, bottomright.lonDec);
			bottomleft = new CWPoint(bottomright.latDec, topleft.lonDec);
		} else
			throw new IllegalArgumentException(MyLocale.getMsg(4831, "getMapInfoObject: Coordinate system not supported by cachewolf:") + " " + coordinateReferenceSystem);
		georef.add(new GCPoint(topleft, new Point(0, 0)));
		georef.add(new GCPoint(bottomright, new Point(pixelsize.x, pixelsize.y)));
		georef.add(new GCPoint(topright, new Point(pixelsize.x, 0)));
		georef.add(new GCPoint(bottomleft, new Point(0, pixelsize.y)));

		MapInfoObject ret = new MapInfoObject();
		ret.evalGCP(georef, pixelsize.x, pixelsize.y, coordinateReferenceSystem[getCrs(maparea.getCenter())]);
		return ret;
	}
}

class ExpediaMapService extends OnlineMapService {
	/*
	 * information from: DownloadMouseMode.properties in project GPSylon ( in directory gpsylon_src-0.5.2\plugins\downloadmousemode\auxiliary\org\dinopolis\gpstool\plugin\downloadmousemode and DownloadMapCalculator.java in Dir
	 * gpsylon_src-0.5.2\plugins\downloadmousemode\src\org\dinopolis\gpstool\plugin\downloadmousemode
	 * download.map.url.expedia_east=http\://www.expedia.com/pub/agent.dll?qscr=mrdt&ID=3XNsF.&CenP={0,number,#.########},{1,number,#.########}&Lang=EUR0809&Alti={2,number,#}&Size={3,number,#},{4,number,#}&Offs=0.000000,0.000000\&BCheck=1
	 * download.map.url.expedia_east.title=Url of Expedia Europe
	 * download.map.scale_factor.expedia_east=3950
	 */
	final static float downloadMapScaleFactorExpedia_east = 3950;
	final static float MAPBLAST_METERS_PER_PIXEL = 1.0f / 2817.947378f;
	final static float EXPEDIA_METERS_PER_PIXEL = downloadMapScaleFactorExpedia_east * MAPBLAST_METERS_PER_PIXEL;

	public ExpediaMapService() {
		name = "Expedia";
		filename = "expedia";
		MainUrl = "Rhttp://www.expedia.de/pub/agent.dll?qscr=mrdt&ID=3kQaz."; // "Rhttp://" forces doenloadUrl to retry the URL until it gets an http-redirect and then downloads from there
		imageFileExt = ".gif";
		mapType = "expedia";
		recommendedScales = new double[] { 5 };
		minscale = getMetersPerPixel(0.00000000000000000000001f);
		maxscale = getMetersPerPixel((float) new CWPoint(0, 0).getDistance(new CWPoint(0, 180)) * 2 * 1000 / 1000); // whole world * 1000 because of km -> m. /1000 because we have 1000x1000 Pixel usually
		boundingBox = new Area(new CWPoint(90, -180), new CWPoint(-90, 180));
	}

	public float getMetersPerPixel(float scale) {
		return EXPEDIA_METERS_PER_PIXEL * getZoomlevel(scale);
	}

	private int getZoomlevel(float scale) {
		return (int) (Math.ceil(scale / EXPEDIA_METERS_PER_PIXEL));
	}

	public String getUrlForCenterScale(CWPoint center, float scale, Point pixelsize) {
		int zoomlevel = getZoomlevel(scale);
		String zone;
		if ((center.lonDec <= -30 && center.lonDec >= -170) || (center.lonDec > 360 - 30 && center.lonDec <= 360 - 170))
			zone = "USA0409"; // TODO test which zone-code ist best for asia
		else
			zone = "EUR0809";
		String quelle = MainUrl + "&CenP=" + center.toString(TransformCoordinates.LAT_LON);
		quelle = quelle + "&Alti=" + Convert.toString(zoomlevel) + "&Lang=" + zone + "&Size=" + Convert.toString(pixelsize.x) + "," + Convert.toString(pixelsize.y) + "&Offs=0,0&MapS=0"; // &Pins=|" + latD.toString().replace(',', '.') + "," + lonD.toString().replace(',', '.') + "|5|";
		return quelle;
	}

	public MapInfoObject getMapInfoObject(CWPoint center, float scale, Point pixelsize) {
		float metersPerPixel2 = getMetersPerPixel(scale);
		float metersPerPixel = metersPerPixel2;
		MapInfoObject cal = new MapInfoObject(metersPerPixel, center, pixelsize.x, pixelsize.y, name);
		return cal;
	}

}
