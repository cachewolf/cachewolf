/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
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
import CacheWolf.Common;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.MyLocale;
import CacheWolf.utils.BetterUTF8Codec;
import CacheWolf.utils.FileBugfix;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.io.DataInputStream;
import ewe.io.FileBase;
import ewe.io.FileInputStream;
import ewe.io.IOException;
import ewe.sys.Time;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.Comparer;
import ewe.util.Vector;

/**
 * class to handle a list of maps (Vector of MapListEntry)
 * it loads the list,
 * finds the best map for a given location,
 * says if a map is available for a given lat lon at a given scale
 * start offset for language file: 4700
 * 
 */
public final class MapsList extends Vector {
	// absolute deviations from this factor are seen to have the same scale
	private static float scaleTolerance = 1.15f;
	private static String MapsListVersion = "1";
	private String CustomMapsPath;

	/**
	 * loads all the maps in mapsPath in all subDirs recursive
	 * 
	 * @param lat
	 *            only for adding empty maps
	 */
	public MapsList(double lat) {
		CustomMapsPath = Global.pref.getCustomMapsPath();
		String MapsListPaN = CustomMapsPath + "/MapsList.txt";
		FileBugfix MapsListFile = new FileBugfix(MapsListPaN);
		boolean dontBuildMapsListFile = MapsListFile.exists();
		if (dontBuildMapsListFile) {
			dontBuildMapsListFile = readMapsListFile(MapsListPaN);
		}
		if (!dontBuildMapsListFile) {
			initMapsList();
			writeMapsListFile(MapsListPaN);
		}

		if (this.isEmpty()) {
			(new MessageBox(MyLocale.getMsg(4201, "Information"), MyLocale.getMsg(4204,
					"No georeferenced map available \n Please choose a scale \n to show the track and the caches. \n You can get one by the menu: Application/Maps/download calibrated"), FormBase.OKB)).execute();
		}

		// the empty maps must be added last, otherwise in method setbestMIO, when no map is available, a malfunction will happen, see there
		this.addEmptyMaps(lat);
		this.onCompletedRead();
	}

	/**
	 * 
	 * @param mapsPath
	 *            without trailing /
	 */
	private void initMapsList() {
		String dateien[];
		FileBugfix files;
		String[] dirstmp;
		Vector dirs = new Vector();
		dirs.add(""); // start with the mapsPath (only this one , without its subdirs) =  + dirs.get(0)

		files = new FileBugfix(CustomMapsPath);
		for (int j = 0; j < dirs.size(); j++) {
			String aktPath;
			//add subdirectories
			aktPath = CustomMapsPath + dirs.get(j);
			files.set(null, aktPath);
			// the options "File.LIST_DONT_SORT | File.LIST_IGNORE_DIRECTORY_STATUS" make it run about twice as fast in sun-vm.
			// The option File.LIST_IGNORE_DIRECTORY_STATUS influences only the sorting (dirs first)
			dirstmp = files.list(null, FileBase.LIST_DIRECTORIES_ONLY | FileBase.LIST_DONT_SORT | FileBase.LIST_IGNORE_DIRECTORY_STATUS);
			if (dirstmp != null) {
				for (int subDir = 0; subDir < dirstmp.length; subDir++) {
					if (!dirstmp[subDir].startsWith(".")) {
						String toAdd = dirs.get(j) + "/" + dirstmp[subDir];
						dirs.add(j + 1 + subDir, toAdd);
					}
				}
			}

			files.set(null, aktPath);
			dateien = files.list("*.wfl", FileBase.LIST_FILES_ONLY | FileBase.LIST_DONT_SORT | FileBase.LIST_IGNORE_DIRECTORY_STATUS);
			if (dateien != null) {
				if (dateien.length == 0) {
					// check if there is a tiles - structure in directories
					String p[] = ewe.util.mString.split(aktPath, '/');
					if (p.length > 3) {
						String wmsPaN = FileBase.getProgramDirectory() + "/webmapservices/" + p[p.length - 3] + ".wms";
						FileBugfix wmsFile = new FileBugfix(wmsPaN);
						// (.../Google/<zoom>/<x>/<y>.png ) und Google.wms existiert
						// Definition: there is a tiles - structure,
						// if there exist no wfl - files in the actual directory (aktPath)
						// but graphic files (at moment only png),
						// if there exists a wms - file with the same name as the directory 3 steps above the actual directory (aktPath)
						if (wmsFile.exists()) {
							String imageExtension = "*.png";
							try {
								WebMapService wms = new WebMapService(wmsPaN);
								imageExtension = "*" + wms.imageFileExt;
							}
							catch (Exception e) {
							}
							dateien = files.list(imageExtension, FileBase.LIST_FILES_ONLY | FileBase.LIST_DONT_SORT | FileBase.LIST_IGNORE_DIRECTORY_STATUS);
							int zoom = Common.parseInt(p[p.length - 2]);
							if (zoom > 0) {
								int x = Common.parseInt(p[p.length - 1]);
								if (x >= 0) {
									// int y from dateien
									for (int i = 0; i < dateien.length; i++) {
										int y = Common.parseInt(Common.getFilename(dateien[i]));
										if (y >= 0) {
											String filename = dateien[i].substring(0, dateien[i].lastIndexOf('.'));
											MapImageFileName mapImageFileName = new MapImageFileName(aktPath.substring(CustomMapsPath.length()), filename, "");
											MapInfoObject mio = new MapInfoObject(x, y, zoom, mapImageFileName);
											MapListEntry mle = new MapListEntry(mapImageFileName, "FF1" + mio.getEasyFindString(), (byte) 2);
											if (mle.sortEntryBBox != null)
												add(mle);
										}
									}
								}
							}
						}
					}
				}
				else {
					for (int i = 0; i < dateien.length; i++) {
						MapListEntry mle = new MapListEntry(aktPath.substring(CustomMapsPath.length()), dateien[i].substring(0, dateien[i].lastIndexOf('.')));
						if (mle.sortEntryBBox != null)
							add(mle);
					}
				}
			}
			dateien = files.list("*.pack", FileBase.LIST_FILES_ONLY | FileBase.LIST_DONT_SORT | FileBase.LIST_IGNORE_DIRECTORY_STATUS);
			if (dateien != null) {
				if (dateien.length > 0) {
					for (int i = 0; i < dateien.length; i++) {
						createMapListEntries(aktPath + "/" + dateien[i]);
					}
				}
			}
		}

		// if (MapListEntry.rename == 1)
		// reset static changes to initial values
		MapListEntry.loadingFinished();
	}

	private void createMapListEntries(String thePackFile) {
		FileInputStream stream;
		DataInputStream reader;
		try {

			FileBugfix queryFile = new FileBugfix(thePackFile);
			stream = new FileInputStream(queryFile);
			reader = new DataInputStream(stream);

			String layerName = readString(reader, 32);
			String friendlyName = readString(reader, 128);
			String url = readString(reader, 256);
			long ticks = readReverseLong(reader);
			long MaxAge = ticks;
			int numBoundingBoxes = readReverseInt(reader);
			for (int i = 0; i < numBoundingBoxes; i++) {
				try {
					int zoom = readReverseInt(reader);
					int MinX = readReverseInt(reader);
					int MaxX = readReverseInt(reader);
					int MinY = readReverseInt(reader);
					int MaxY = readReverseInt(reader);
					long OffsetToIndex = readReverseLong(reader);
					int Stride = MaxX - MinX + 1; // length of stripe
					for (int x = MinX; x <= MaxX; x++) {
						for (int y = MinY; y <= MaxY; y++) {
							MapImageFileName mapImageFileName = new MapImageFileName(thePackFile.substring(CustomMapsPath.length(), thePackFile.length() - 5), MinX + "!" + MinY + "!" + Stride + "!" + OffsetToIndex + "!" + zoom + "!" + x + "!" + y,
									"");
							MapInfoObject mio = new MapInfoObject(x, y, zoom, mapImageFileName);
							MapListEntry mle = new MapListEntry(mapImageFileName, "FF1" + mio.getEasyFindString(), (byte) 3);
							if (mle.sortEntryBBox != null)
								add(mle);
						}
					}
				}
				catch (Exception e) {
				}
			}
			reader.close();
			stream.close();
		}
		catch (Exception e) {
		}
	}

	private String readString(DataInputStream reader, int length) throws IOException {
		byte[] asciiBytes = new byte[length];
		int last = 0;
		for (int i = 0; i < length; i++) {
			asciiBytes[i] = reader.readByte();
			if (asciiBytes[i] > 32)
				last = i;
		}
		StringBuffer sb = new BetterUTF8Codec().decodeUTF8(asciiBytes, 0, last + 1);
		return sb.toString().trim();
	}

	private long readReverseLong(DataInputStream reader) throws IOException {
		byte byte8 = reader.readByte();
		byte byte7 = reader.readByte();
		byte byte6 = reader.readByte();
		byte byte5 = reader.readByte();
		byte byte4 = reader.readByte();
		byte byte3 = reader.readByte();
		byte byte2 = reader.readByte();
		byte byte1 = reader.readByte();
		return (long) (((byte1 & 0xFF) << 56) + ((byte2 & 0xFF) << 48) + ((byte3 & 0xFF) << 40) + ((byte4 & 0xFF) << 32) + ((byte5 & 0xFF) << 24) + ((byte6 & 0xFF) << 16) + ((byte7 & 0xFF) << 8) + (byte8 & 0xFF));
	}

	private int readReverseInt(DataInputStream reader) throws IOException {
		byte byte4 = reader.readByte();
		byte byte3 = reader.readByte();
		byte byte2 = reader.readByte();
		byte byte1 = reader.readByte();
		return (int) (((byte1 & 0xFF) << 24) + ((byte2 & 0xFF) << 16) + ((byte3 & 0xFF) << 8) + (byte4 & 0xFF));
	}

	public String getMapsPath() {
		return CustomMapsPath;
	}

	private void writeMapsListFile(String PathAndName) {
		try {
			ewe.io.TextWriter w;
			w = new ewe.io.TextWriter(PathAndName, false);
			w.codec = new BetterUTF8Codec();
			w.println(MapsListVersion);
			for (int z = 0; z < this.size(); z++) {
				MapListEntry mle = (MapListEntry) this.get(z);
				w.println(mle.getMapImageFileName().getPath() + ";" + mle.getMapImageFileName().getMapName() + ";" + mle.sortEntryBBox + ";" + mle.mapType);
			}
			w.println();
			w.close();
		}
		catch (IOException e) {

		}
	}

	private boolean readMapsListFile(String PathAndName) {
		// structure: <relative path to CustomMapsPath>;<mapName>;<sortEntryBBox>;<mapType>
		boolean ret = true;
		try {
			ewe.io.TextReader r;
			r = new ewe.io.TextReader(PathAndName);
			r.codec = new BetterUTF8Codec();
			String s; // structure read
			String[] S; // structure splitted by ";"
			s = r.readLine();
			// check version (changes during development and intermediate svn - uploads)
			if (s.equals(MapsListVersion)) {
				// check first entry, directory could have been moved				
				s = r.readLine();
				S = ewe.util.mString.split(s, ';');
				if (S[3].equals("2")) {
					if (Common.getImageName(CustomMapsPath + S[0] + "/" + S[1]) == null) {
						ret = false;
					}
				}
				else {
					FileBugfix test;
					if (S[3].equals("3")) {
						test = new FileBugfix(CustomMapsPath + S[0] + ".pack");
					}
					else { // if (S[3].equals("0")) {
						test = new FileBugfix(CustomMapsPath + S[0] + S[1] + ".wfl");
					}
					if (!test.exists()) {
						ret = false;
					}
				}
			}
			else
				ret = false;

			while (s != null) {
				S = ewe.util.mString.split(s, ';');
				if (S.length == 4) {
					MapListEntry mle = new MapListEntry(new MapImageFileName(S[0], S[1], ""), S[2], (byte) Common.parseInt(S[3]));
					this.add(mle);
				}
				else {
					if (s.length() > 0) {
						r.close();
						return false;
					}
				}
				try {
					s = r.readLine();
				}
				catch (Exception e) {
					//NPE
				}
			}
			r.close();
		}
		catch (IOException e) {
			ret = false;
		}
		return ret;
	}

	public void addEmptyMaps(double lat) {
		MapListEntry tempMIO;
		tempMIO = new MapListEntry(1.0, lat);
		add(tempMIO);
		tempMIO = new MapListEntry(5.0, lat); // this one ( the 4th last) is automatically used when no real map is available, see MovingMap.setbestMIO
		add(tempMIO);
		tempMIO = new MapListEntry(50.0, lat);
		add(tempMIO);
		tempMIO = new MapListEntry(250.0, lat);
		add(tempMIO);
		tempMIO = new MapListEntry(1000.0, lat);
		add(tempMIO);
	}

	/* diese Routine wird gegenwärtig für 3 ZWecke verwendet:
	 * a) normal - keep given resolution --> Lösung: übergebene scale nutzen für screen
	 * b) highest res: Ziel: Karte mit höchster Auflösung, die im screen ist und möglichst nah an lat/lon -> ich muss auflösung noch in Dateinamen schreiben
	 * c) gegenteil von b)
	 */
	/**
	 * the best map in the list of maps is the one
	 * whose center is nearest to ll.latDec/ll.lonDec and
	 * on screen and
	 * with its scale nearest to scale.
	 * 
	 * @param ll
	 *            CWPoint: ll.latDec/ll.lonDec a point to be inside the map
	 * @param screen
	 *            Rect: width, height of the screen. The map must overlap the screen.
	 * @param scale
	 *            float: scale wanted.
	 * @param forceScale
	 *            : when true, return null if no map with specified scale could be found
	 * @param withProgressBox
	 *            boolean: true -> with ProgressBox
	 * @return MapInfoObject
	 *         if a Map in this list (mapsList Vector) overlaps the screen
	 */
	public MapInfoObject getBest(CWPoint ll, Rect screen, float scale, boolean forceScale, boolean withProgressBox) {
		if (size() == 0)
			return null;
		long start = new Time().getTime();
		InfoBox progressBox = null;
		boolean showprogress = false;
		String cmp = "FF1" + Area.getEasyFindString(ll, MAXDIGITS_IN_FF);
		int guess = -1;
		MapListEntry mle;
		MapInfoObject mio;
		MapInfoObject bestMIO = null;
		double minDistLat = 1000000000000000000000000000000000000000000000.0;
		double minDistLon = 1000000000000000000000000000000000000000000000.0;
		boolean latNearer, lonNearer;
		boolean better = false;
		Area screenArea = null; // getAreaForScreen(screen, lat, lon, bestMIO.scale, bestMIO);
		float lastscale = -1;
		for (int digitlenght = 0; digitlenght < maxDigits; digitlenght++) {
			guess = quickfind(cmp, this.numDigitsStartIndex[digitlenght], this.numDigitsStartIndex[digitlenght + 1] - 1);
			for (int i = guess; i >= numDigitsStartIndex[digitlenght]; i--) {
				if (withProgressBox) {
					if (!showprogress && ((i & 31) == 0) && (new Time().getTime() - start > 100)) {
						showprogress = true;
						progressBox = new InfoBox(MyLocale.getMsg(327, "Info"), MyLocale.getMsg(4701, "Searching for best map"));
						progressBox.exec();
						progressBox.waitUntilPainted(100);
						ewe.sys.Vm.showWait(true);
						Global.pref.log(MyLocale.getMsg(4701, "Searching for best map"));
					}
				}
				mle = (MapListEntry) get(i);
				if (!Area.containsRoughly(mle.sortEntryBBox, cmp))
					break; // TODO if no map available
				else {
					mio = mle.getMap();
				}
				better = false;
				if (screenArea == null || !scaleEquals(lastscale, mio)) {
					screenArea = getAreaForScreen(screen, ll, mio.scale, mio);
					lastscale = mio.scale;
				}

				if (screenArea.isOverlapping(mio)) {
					// is on screen
					if (!forceScale || (forceScale && scaleEquals(scale, mio))) {
						// different scale?
						// inbound and resolution nearer at wanted resolution
						// or old one is on screen but lat/long not inbound 
						// -> better
						if (!forceScale && (mio.isInBound(ll) && (bestMIO == null || scaleNearer(mio.scale, bestMIO.scale, scale) || !bestMIO.isInBound(ll))))
							better = true;
						else {
							if (bestMIO == null || scaleNearerOrEuqal(mio.scale, bestMIO.scale, scale)) {
								latNearer = java.lang.Math.abs(ll.latDec - mio.center.latDec) / mio.sizeKm < minDistLat;
								lonNearer = java.lang.Math.abs(ll.lonDec - mio.center.lonDec) / mio.sizeKm < minDistLon;
								// for faster processing:
								// if lat and lon are nearer then the distancance doesn't need to be calculated
								if (latNearer && lonNearer)
									better = true;
								else {
									if ((latNearer || lonNearer)) {
										if (bestMIO == null || mio.center.getDistanceRad(ll) < bestMIO.center.getDistanceRad(ll)) {
											better = true;
										}
									}
								}
							}
						}
						if (better) {
							minDistLat = java.lang.Math.abs(ll.latDec - mio.center.latDec) / mio.sizeKm;
							minDistLon = java.lang.Math.abs(ll.lonDec - mio.center.lonDec) / mio.sizeKm;
							bestMIO = mio;
						}
					}
				}
			}
		}
		if (progressBox != null) {
			progressBox.close(0);
			ewe.sys.Vm.showWait(false);
		}
		if (bestMIO == null)
			return null;
		// return a copy of the MapInfoObject so that zooming won't change the MapInfoObject in the list
		return new MapInfoObject(bestMIO);
	}

	/*
	public MapInfoObject getBestNotStrictScale(double lat, double lon, Area screen, float scale) {
		MapInfoObject ret = getBest(lat, lon, screen, scale, true);
		if (ret == null) ret = getBest(lat, lon, screen, scale, false);
		return ret;
	}
	 */

	private final static int MAXDIGITS_IN_FF = 30;
	/**
	 * after calling onCompletedRead() this will contain a list of indexes at which a new number of digits in the sortEntryBBox start
	 */
	private int[] numDigitsStartIndex = new int[MAXDIGITS_IN_FF];
	private int maxDigits = -1;

	public void onCompletedRead() {
		sort(new Comparer() {
			public int compare(Object o1, Object o2) {
				String s1 = ((MapListEntry) o1).sortEntryBBox;
				String s2 = ((MapListEntry) o2).sortEntryBBox;
				int ret = s1.length() - s2.length(); // sort shorter sortEntryBBox at the beginning
				if (ret == 0)
					ret = s1.compareTo(s2);
				return ret;
			}
		}, false);
		int digits_index = 0;
		int numdigits = 0;
		int s = size();
		for (int i = 0; i < s; i++) {
			if (((MapListEntry) get(i)).sortEntryBBox.length() > numdigits) {
				numDigitsStartIndex[digits_index] = i;
				digits_index++;
				numdigits = ((MapListEntry) get(i)).sortEntryBBox.length();
			}
		}
		numDigitsStartIndex[digits_index] = s;
		maxDigits = digits_index;
	}

	/**
	 * @param llimitorig
	 *            lower limit to start the search from
	 * @param ulimit
	 *            upper limit to stop the search. llimit and ulimit must be set in a way that the sortEntryBBox of each entry betwenn the limits have the same length
	 * @param searchfor
	 *            String starting with FF1, it should be longer than any sortEntryBoxString in the list
	 * @return the highest index which matches the searchfor-String. Look downward from there to find the best map, returns llimit if there is no match
	 */
	private int quickfind(String searchfor, int llimitorig, int ulimit) {
		int llimit = llimitorig;
		int test;
		String cmp = ((MapListEntry) this.get(llimit)).sortEntryBBox;
		String sshort = searchfor.substring(0, cmp.length());
		int comp;
		if (cmp.compareTo(sshort) > 0 || ((MapListEntry) this.get(ulimit)).sortEntryBBox.compareTo(sshort) < 0)
			// if searchfor is not in the range, return llimit (llimit because getBest counts downward, so returning llimit will cause it to do only 1 test
			return llimit;
		while (llimit < ulimit - 1) {
			test = (ulimit + llimit) / 2;
			cmp = ((MapListEntry) this.get(test)).sortEntryBBox;
			comp = cmp.compareTo(searchfor);
			if (comp > 0) {
				// test > searchfor
				ulimit = test;
			}
			else {
				// test <= searchfor
				if (comp < 0)
					// test < serachfor
					llimit = test;
				else {
					// test == searchfor
					llimit = test;
					ulimit = test;
				}
			}
		}
		// searchfor is between llimit and ulimit 
		// OR is at ulimit or higher
		// OR at llimit or lower
		// we want to return the highest index of the map which starts with searchfor
		comp = ((MapListEntry) this.get(ulimit)).sortEntryBBox.compareTo(searchfor);
		if ((comp <= 0) && searchfor.startsWith(((MapListEntry) this.get(ulimit)).sortEntryBBox))
			llimit = ulimit; // search for is on ulimit or higher

		if (!searchfor.startsWith(((MapListEntry) this.get(llimit)).sortEntryBBox))
			llimit = llimitorig; // if the found mapListEntry doesn't contain the searchfor, then there is no map containing it.
		return llimit;
	}

	/**
	 * @return a map which includs topleft and bottomright, if no map includes both it returns null
	 * @param if more than one map includes topleft and bottomright than the one will be returned which has its center nearest to topleft. If you have gps-pos and goto-pos as topleft and bottomright use gps as topleft. if topleft is really topleft or
	 *        if it is bottomright is not relevant.
	 */
	public final MapInfoObject getMapForArea(CWPoint topleft, CWPoint bottomright) {
		long start = new Time().getTime();
		InfoBox progressBox = null;
		boolean showprogress = false;
		MapListEntry ml;
		MapInfoObject mi;
		String cmp = "FF1" + (new Area(topleft, bottomright)).getEasyFindString();
		String cmppadded = Common.rightPad(cmp, 30);
		MapInfoObject fittingmap = null;
		int guess;
		boolean latNearer, lonNearer;
		boolean better;
		double minDistLat = 10000000000000000000000.0;
		double minDistLon = 10000000000000000000000.0;
		for (int digitlength = 0; digitlength < maxDigits; digitlength++) {
			guess = quickfind(cmppadded, this.numDigitsStartIndex[digitlength], this.numDigitsStartIndex[digitlength + 1] - 1);
			if (((MapListEntry) get(guess)).sortEntryBBox.length() > cmp.length())
				break; // if the sortEntryBBox indicates that it cannot contain both points, stop searching
			for (int i = guess; i >= numDigitsStartIndex[digitlength]; i--) {
				if (!showprogress && ((i & 31) == 0) && (new Time().getTime() - start > 100)) { // reason for (i & 7 == 0): test time only after i is incremented 15 times
					showprogress = true;
					progressBox = new InfoBox(MyLocale.getMsg(327, "Info"), MyLocale.getMsg(4701, "Searching for best map"));
					progressBox.exec();
					progressBox.waitUntilPainted(100);
					ewe.sys.Vm.showWait(true);
				}
				ml = (MapListEntry) get(i);
				if (!Area.containsRoughly(ml.sortEntryBBox, cmp))
					// TODO if no map available
					continue;
				else {
					mi = ml.getMap();
				}
				better = false;
				if (mi.isInBound(topleft) && mi.isInBound(bottomright)) { // both points are inside the map
					if (fittingmap == null || fittingmap.scale > mi.scale * scaleTolerance) {
						better = true; // mi map has a better (lower) scale than the last knwon good map
					}
					else {
						if (scaleEquals(mi, fittingmap)) { // same scale as bestMIO till now -> test if its center is nearer to the gps-point = topleft
							latNearer = java.lang.Math.abs(topleft.latDec - mi.center.latDec) / mi.sizeKm < minDistLat;
							lonNearer = java.lang.Math.abs(topleft.lonDec - mi.center.lonDec) / mi.sizeKm < minDistLon;
							if (latNearer && lonNearer)
								better = true; // for faster processing: if lat and lon are nearer then the distancance doesn't need to be calculated
							else {
								if ((latNearer || lonNearer)) {
									if (mi.center.getDistanceRad(topleft) < fittingmap.center.getDistanceRad(topleft))
										better = true;
								}
							}

						}
					}
					if (better) {
						fittingmap = mi;
						minDistLat = java.lang.Math.abs(topleft.latDec - mi.center.latDec);
						minDistLon = java.lang.Math.abs(topleft.lonDec - mi.center.lonDec);
					}
				}
			} // for i
		} // for digitlength
		if (progressBox != null) {
			progressBox.close(0);
			ewe.sys.Vm.showWait(false);
		}
		if (fittingmap == null)
			return null;
		// TODO in case that this one and the old one are identical this instantiation could eventually be avoided as it is done at every greater shift of the map
		return new MapInfoObject(fittingmap);
	}

	/**
	 * 
	 * @param lat
	 *            a point to be inside the map
	 * @param lon
	 * @param screen
	 *            : width, height of the screen. The map must overlap the screen. xy: where is lat/lon on screen
	 * @param curScale
	 *            reference scale to be changed
	 * @param moreDetails
	 *            true: find map with more details == higher resolustion = lower scale / false find map with less details = better overview
	 * @return
	 */
	public MapInfoObject getMapChangeResolution(CWPoint ll, Rect screen, float curScale, boolean moreDetails) {
		if (size() == 0)
			return null;
		long start = new Time().getTime();
		InfoBox progressBox = null;
		boolean showprogress = false;
		MapListEntry ml;
		MapInfoObject mi;
		MapInfoObject bestMIO = null; // = (MapInfoObject)get(0);
		double minDistLat = 1000000000000000000000000000000000000000000000.0;
		double minDistLon = 1000000000000000000000000000000000000000000000.0;
		boolean latNearer, lonNearer;
		boolean better = false;
		Area screenArea = null; // getAreaForScreen(screen, lat, lon, bestMIO.scale, bestMIO);
		float lastscale = -1;
		String cmp = "FF1" + Area.getEasyFindString(ll, MAXDIGITS_IN_FF);
		for (int i = size() - 1; i >= 0; i--) {

			// test time only after i is incremented 31 times
			if (!showprogress && ((i & 31) == 0) && (new Time().getTime() - start > 100)) {
				showprogress = true;
				progressBox = new InfoBox(MyLocale.getMsg(327, "Info"), MyLocale.getMsg(4701, "Searching for best map"));
				progressBox.exec();
				progressBox.waitUntilPainted(100);
				ewe.sys.Vm.showWait(true);
			}

			better = false;
			ml = (MapListEntry) get(i);
			if (!Area.containsRoughly(ml.sortEntryBBox, cmp))
				// TODO if no map available
				continue;
			else {
				mi = ml.getMap();
			}
			if (mi.getMapType() == 1)
				continue; // leeres image
			if (screenArea == null || !scaleEquals(lastscale, mi)) {
				screenArea = getAreaForScreen(screen, ll, mi.scale, mi);
				lastscale = mi.scale;
			}
			if (screenArea.isOverlapping(mi)) { // is on screen
				if (bestMIO == null || !scaleEquals(mi, bestMIO)) { // different scale than known bestMIO?
					if (mi.isInBound(ll) && ( // more details wanted and this map has more details?                                // less details than bestMIO
							(moreDetails && (curScale > mi.scale * scaleTolerance) && (bestMIO == null || mi.scale > bestMIO.scale * scaleTolerance)) // higher resolution wanted and mi has higher res and a lower res than bestMIO, because we dont want to overjump one resolution step
							|| (!moreDetails && (curScale * scaleTolerance < mi.scale) && (bestMIO == null || mi.scale * scaleTolerance < bestMIO.scale)) // lower resolution wanted and mi has lower res and a higher res than bestMIO, because we dont want to overjump one resolution step
							))
						better = true; // inbound and higher resolution if higher res wanted -> better
				}
				else { // same scale as bestMIO -> look if naerer
					latNearer = java.lang.Math.abs(ll.latDec - mi.center.latDec) / mi.sizeKm < minDistLat;
					lonNearer = java.lang.Math.abs(ll.lonDec - mi.center.lonDec) / mi.sizeKm < minDistLon;
					if (latNearer && lonNearer)
						better = true; // for faster processing: if lat and lon are nearer then the distancance doesn't need to be calculated
					else {
						if ((latNearer || lonNearer)) {
							if (mi.center.getDistanceRad(ll) < bestMIO.center.getDistanceRad(ll))
								better = true;
						}
					}
				} // same scale
				if (better) {
					minDistLat = java.lang.Math.abs(ll.latDec - mi.center.latDec) / mi.sizeKm;
					minDistLon = java.lang.Math.abs(ll.lonDec - mi.center.lonDec) / mi.sizeKm;
					bestMIO = mi;
				}
			}
		}
		if (progressBox != null) {
			progressBox.close(0);
			ewe.sys.Vm.showWait(false);
		}
		if (bestMIO == null)
			return null;
		return new MapInfoObject(bestMIO);
	}

	/**
	 * returns an area in lat/lon of the screen
	 * 
	 * @param a
	 *            screen width / height and position of lat/lon on the screen
	 * @param lat
	 *            a (reference) point on the screen
	 * @param lon
	 * @param scale
	 *            scale (meters per pixel) of the map for which the screen edges are wanted
	 * @param map
	 *            map for which the screen edges are wanted
	 * @return Area
	 */
	private Area getAreaForScreen(Rect a, CWPoint ll, float scale, MapInfoObject map) {
		Area ret = null;
		Point xy = map.calcMapXY(ll);
		Point topleft = new Point(xy.x - a.x, xy.y - a.y);
		ret = new Area(map.calcLatLon(topleft), map.calcLatLon(topleft.x + a.width, topleft.y + a.height));
		return ret;
	}

	public static boolean scaleEquals(MapInfoObject a, MapInfoObject b) {
		//return java.lang.Math.abs(a.scale - b.scale) < scaleTolerance;
		if (a.scale > b.scale)
			return a.scale / b.scale < scaleTolerance;
		else
			return b.scale / a.scale < scaleTolerance;
	}

	public static boolean scaleEquals(float s, MapInfoObject b) {
		//return java.lang.Math.abs(s - b.scale) < scaleTolerance;
		if (s > b.scale)
			return s / b.scale < scaleTolerance;
		else
			return b.scale / s < scaleTolerance;
	}

	/**
	 * 
	 * @param test
	 * @param old
	 * @param wanted
	 * @return true if test is nearer to wanted than old, false if the change in the scale is lower than scaleTolerance
	 */
	public static boolean scaleNearer(float test, float old, float wanted) {
		float testa, wanta, wantb, olda;
		if (test > wanted) { // ensure that first term is greater than 1
			testa = test;
			wanta = wanted;
		}
		else {
			testa = wanted;
			wanta = test;
		}
		if (old > wanted) { // ensure that second term is greater than 1
			olda = old;
			wantb = wanted;
		}
		else {
			olda = wanted;
			wantb = old;
		}
		return testa / wanta * scaleTolerance < olda / wantb;
	}

	public static boolean scaleNearerOrEuqal(float test, float old, float wanted) {
		float testa, wanta, wantb, olda;
		if (test > wanted) { // ensure that first term is greater than 1
			testa = test;
			wanta = wanted;
		}
		else {
			testa = wanted;
			wanta = test;
		}
		if (old > wanted) { // ensure that second term is greater than 1
			olda = old;
			wantb = wanted;
		}
		else {
			olda = wanted;
			wantb = old;
		}
		return testa / wanta < olda / wantb * scaleTolerance;
	}

	/* may be the following code is used same time later to further enhance the speed of finding the best map
	public int getQuickMap(String search){
		boolean found = false; // TODO unfertig
		int upperbound = 0;
		int downbound = size();
		int test;
		while (!found) {
			test = (upperbound + downbound)/2;
			if ( ((Comparable)(get(test))).compareTo(search) < 0) downbound = test;
			else upperbound = test;
		}
		return 1;
	}
	*/

	/**
	 * for determining if a new map should be downloaded public boolean isInAmap(CWPoint topleft, CWPoint bottomright) { if (!latRangeList.isInRange(topleft.latDec) || !latRangeList.isInRange(bottomright.latDec)) ||
	 * !lonRangeList.inInRange(topleft.lonDec) || !lonRangeList.isInRange(boxsttomright.lonDec) return false; }
	 */

}

final class MapListEntry {
	public String sortEntryBBox;
	private MapImageFileName mapImageFileName;
	public MapInfoObject map = null;
	public byte mapType = 0; // 0 = has wfl-file (from wms - Server or Maperitive), 1 = with zoom/x/y.imageExtension (from Tile - Server)  

	static int rename = 0;
	static InfoBox renameProgressInfoB = null;

	/**
	 * constructes a MapListEntry with given sortEntryBBox
	 */
	public MapListEntry(MapImageFileName mapImageFileName, String sortEntryBBox, byte mapType) {
		this.mapImageFileName = mapImageFileName;
		this.sortEntryBBox = sortEntryBBox;
		this.mapType = mapType;
	}

	/**
	 * constructes a MapListEntry with given
	 * sortEntryBBox from Filename (start with FF1, end with E-)
	 * or is calculated using the MapInfoObject created from wfl - File
	 */
	public MapListEntry(String subPath, String filenamei) {
		this.mapImageFileName = new MapImageFileName(subPath, filenamei, "");
		this.sortEntryBBox = null;
		try {
			if (filenamei.startsWith("FF1"))
				sortEntryBBox = filenamei.substring(0, filenamei.indexOf("E-"));
		}
		catch (IndexOutOfBoundsException ex) {
			Global.pref.log("[MapsList:MapListEntry] Bad File in maps: " + filenamei);
		}

		String mapsPath = Global.pref.getCustomMapsPath();

		if (sortEntryBBox == null) {
			try {
				this.map = new MapInfoObject(mapImageFileName);
				sortEntryBBox = "FF1" + this.map.getEasyFindString();

				if (rename == 0) { // never asked before
					if ((new MessageBox(MyLocale.getMsg(4702, "Optimisation"), MyLocale.getMsg(4703,
							"Cachewolf can make loading maps much faster by adding a identification mark to the filename. Do you want me to do this now?\n It can take several minutes"), FormBase.YESB | FormBase.NOB)).execute() == FormBase.IDYES) {
						renameProgressInfoB = new InfoBox(MyLocale.getMsg(327, "Info"), MyLocale.getMsg(4704, "\nRenaming file:") + "    \n");
						renameProgressInfoB.exec();
						renameProgressInfoB.waitUntilPainted(100);
						rename = 1; // rename
					}
					else
						rename = 2; // don't rename
				}

				if (rename == 1) {
					String imageExtension = "";
					String f = mapsPath + subPath + filenamei + ".wfl";
					renameProgressInfoB.setInfo(MyLocale.getMsg(4704, "\nRenaming file: ") + f + "\n");
					String to = sortEntryBBox + "E-" + filenamei + ".wfl";
					if (!new FileBugfix(f).rename(to))
						(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(4705, "Failed to rename:\n") + f + MyLocale.getMsg(4706, "\nto:\n") + to, FormBase.OKB)).exec();
					f = Common.getImageName(mapsPath + subPath + filenamei);
					if (f != null) {
						imageExtension = Common.getFilenameExtension(f);
						to = sortEntryBBox + "E-" + filenamei + imageExtension;
						if (!new FileBugfix(f).rename(to)) {
							Global.pref.log("MapListEntry Failed to rename: " + mapsPath + subPath + filenamei + ": " + f + " to: " + to, null);
							(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(4705, "Failed to rename:\n") + f + MyLocale.getMsg(4706, "\nto:\n") + to, FormBase.OKB)).exec();
						}
					}
					else {
						Global.pref.log("MapListEntry: Could not find image assiciated to: " + mapsPath + subPath + filenamei + ".wfl", null);
						(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(4709, "Could not find image assiciated to:\n") + mapsPath + subPath + filenamei + ".wfl", FormBase.OKB)).exec();
					}
					this.mapImageFileName.setMapName(sortEntryBBox + "E-" + filenamei);
					this.mapImageFileName.setImageExtension(imageExtension);
				}
			}
			catch (IOException ioex) { // this should not happen
				(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(4707, "I/O-Error while reading:") + " " + mapsPath + subPath + filenamei + ": " + ioex.getMessage(), FormBase.OKB)).exec();
				Global.pref.log("MapListEntry: I/O-Error while reading: " + mapsPath + subPath + filenamei + ": ", ioex);
			}
			catch (Exception ex) {
				(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(4706, "Error while reading:") + " " + mapsPath + subPath + filenamei + ": " + ex.getMessage(), FormBase.OKB)).exec();
				Global.pref.log("MapListEntry: Error while reading: " + mapsPath + subPath + filenamei + ": ", ex);
			}
		}
	}

	/**
	 * constructes a MapListEntry with a MapInfoObject without an associated map but with 1 Pixel = scale meters
	 */
	public MapListEntry(double scale, double lat) {
		this.mapImageFileName = new MapImageFileName("", MyLocale.getMsg(4300, "empty 1 Pixel = ") + scale + MyLocale.getMsg(4301, "meters"), "");
		map = new MapInfoObject(scale, lat, mapImageFileName);
		this.sortEntryBBox = "FF1";
		this.mapType = 1;
	}

	public MapInfoObject getMap() {
		if (this.map != null)
			return this.map;
		else
			// implicit sets this.map
			return new MapInfoObject(this);
	}

	public static void loadingFinished() {
		if (renameProgressInfoB != null)
			renameProgressInfoB.close(0);
		renameProgressInfoB = null;
		rename = 0;
	}

	public MapImageFileName getMapImageFileName() {
		return mapImageFileName;
	}

	public void setMapImageFileName(MapImageFileName mapImageFileName) {
		this.mapImageFileName = mapImageFileName;
	}

	public byte getMapType() {
		return mapType;
	}
}
