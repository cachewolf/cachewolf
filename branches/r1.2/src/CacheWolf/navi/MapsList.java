package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.Common;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.MyLocale;
import CacheWolf.utils.FileBugfix;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.IOException;
import ewe.sys.Time;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.Comparer;
import ewe.util.Vector;
import ewe.fx.*;
/**
 * class to handle a list of maps
 * it loads the list, finds the best map for a given location,
 * says if a map is available for a given lat lon at a given scale
 * start offset for language file: 4700
 *
 */
public final class MapsList extends Vector {
	public static float scaleTolerance = 1.15f; // absolute deviations from this factor are seen to have the same scale

	/**
	 * loads all the maps in mapsPath in all subDirs recursive
	 * @param mapsPath
	 */
	public MapsList(String mapsPath) {
		super(); // forget already loaded maps
		//if (mmp.mapImage != null)
		String dateien[];
		FileBugfix files;
		String rawFileName;
		String[] dirstmp;
		Vector dirs = new Vector();
		dirs.add(""); // start with the mapsPath (only this one , without its subdirs)
		MapListEntry tempMIO;
		MessageBox f = null;
		// sort(new StandardComparer(), false);

		files = new FileBugfix(mapsPath+"/"+dirs.get(0));
		for (int j = 0; j < dirs.size(); j++) {
			files.set(null, mapsPath+"/"+dirs.get(j));
			//ewe.sys.Vm.debug("mapd-Dirs:"+files);

			//add subdirectories
			if (!dirs.get(j).equals(".")) {
				dirstmp = files.list(null, FileBase.LIST_DIRECTORIES_ONLY | File.LIST_DONT_SORT | File.LIST_IGNORE_DIRECTORY_STATUS); // the options "File.LIST_DONT_SORT | File.LIST_IGNORE_DIRECTORY_STATUS" make it run about twice as fast in sun-vm. The option File.LIST_IGNORE_DIRECTORY_STATUS influences only the sorting (dirs first)
				if (dirstmp != null) {
					for (int subDir = 0; subDir < dirstmp.length; subDir++) {
						dirs.add(j+1+subDir, dirs.get(j)+"/"+dirstmp[subDir]);
					}
				}
			}

			dateien = files.list("*.wfl", FileBase.LIST_FILES_ONLY | File.LIST_DONT_SORT | File.LIST_IGNORE_DIRECTORY_STATUS); //"*.xyz" doesn't work on some systems -> use FileBugFix
			if (dateien == null) continue;
			for(int i = 0; i < dateien.length;i++){
				// if (!dateien[i].endsWith(".wfl")) continue;
				rawFileName = dateien[i].substring(0, dateien[i].lastIndexOf('.'));
				try {
					if (dirs.get(j).equals(".")) // the notation dir/./filename doesn't work on all platforms anyhow
						tempMIO = new MapListEntry(mapsPath+"/", rawFileName);
					else tempMIO = new MapListEntry(mapsPath+"/"+dirs.get(j)+"/", rawFileName);
					if (tempMIO.sortEntryBBox != null) add(tempMIO);
					//ewe.sys.Vm.debug(tempMIO.getEasyFindString() + tempMIO.mapName);
				}catch(Exception ex){ // TODO exception ist, glaub ich evtl überflüssig
					if (f == null) (f=new MessageBox(MyLocale.getMsg(144, "Warning"), MyLocale.getMsg(4700, "Ignoring error while \n reading calibration file \n")+ex.toString(), FormBase.OKB)).exec();
				} /* catch(ArithmeticException ex){ // affine contain not allowed values
					if (f == null) (f=new MessageBox("Warning", "Ignoring error while \n reading calibration file \n"+ex.toString(), MessageBox.OKB)).exec();
				} */
			}
		}
		if (MapListEntry.rename == 1) MapListEntry.loadingFinished();
		_mapsPath=mapsPath;
	}

	public void addEmptyMaps(double lat) {
		MapListEntry tempMIO;
		tempMIO = new MapListEntry(1.0, lat);
		add(tempMIO);
		tempMIO = new MapListEntry(5.0, lat); // this one ( the 4th last) is automatically used when no real map is available, see MovingMap.setBestMap
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
	 * find the best map for lat/lon in the list of maps
	 * @param lat a point to be inside the map
	 * @param lon
	 * @param screen: width, height of the screen. The map must overlap the screen. xy: where is lat/lon on screen
	 * @param scale scale wanted
	 * currently the best map is the one, whose center is nearest to lat/lon
	 * and in Area with its scale nearest to scale.
	 * it always returns a map (if the list is not empty) as long as it overlaps the screen
	 * @param forceScale: when true, return null if no map with specified scale could be found
	 */
	public MapInfoObject getBestMap(CWPoint ll, Rect screen, float scale, boolean forceScale,boolean withProgressBox) {
		if (size() == 0) return null;
		long start = new Time().getTime();
		InfoBox progressBox = null;
		boolean showprogress = false;
		String cmp = "FF1"+Area.getEasyFindString(ll, MAXDIGITS_IN_FF);
		int guess = -1;
//		int foundkw = -1;
		MapListEntry ml;
		MapInfoObject mi;
		MapInfoObject bestMap = null; // = (MapInfoObject)get(0);
		double minDistLat = 1000000000000000000000000000000000000000000000.0;
		double minDistLon = 1000000000000000000000000000000000000000000000.0;
		boolean latNearer, lonNearer;
		boolean better = false;
		Area screenArea = null; // getAreaForScreen(screen, lat, lon, bestMap.scale, bestMap);
		float lastscale = -1;
		// int testkw = 0;
		// int testdkw = 0;
		for (int digitlenght = 0; digitlenght < maxDigits; digitlenght++) {
			guess = quickfind(cmp, this.numDigitsStartIndex[digitlenght], this.numDigitsStartIndex[digitlenght+1]-1);
			for (int i=guess; i >= numDigitsStartIndex[digitlenght] ;i--) {
				// testdkw++;
				if (withProgressBox) {
					if (!showprogress && ((i & 31) == 0) && (new Time().getTime()-start  > 100) ) { // reason for (i & 7 == 0): test time only after i is incremented 15 times
						showprogress = true;
						progressBox = new InfoBox(MyLocale.getMsg(327,"Info"), MyLocale.getMsg(4701,"Searching for best map"));
						progressBox.exec();
						progressBox.waitUntilPainted(100);
						ewe.sys.Vm.showWait(true);
						Global.getPref().log(MyLocale.getMsg(4701,"Searching for best map"));
					}
				}
				ml = (MapListEntry)get(i);
				try {
					if (!Area.containsRoughly(ml.sortEntryBBox, cmp)) break; // TODO if no map available
					else { mi = ml.getMap(); /* testkw++; */}
				} catch (IOException ex) {continue; } // could not read .wfl-file
				better = false;
				//			mi = (MapInfoObject)get(i);
				if (screenArea == null || !scaleEquals(lastscale, mi) ) {
					screenArea = getAreaForScreen(screen, ll, mi.scale, mi);
					lastscale = mi.scale;
				}

				if (screenArea.isOverlapping(mi) ) { // is on screen
					if (!forceScale || (forceScale && scaleEquals(scale, mi))) { // different scale?
						if (!forceScale && (mi.isInBound(ll) && (bestMap == null || scaleNearer(mi.scale, bestMap.scale, scale) || !bestMap.isInBound(ll))))
							better = true; // inbound and resolution nearer at wanted resolution or old one is on screen but lat/long not inbound-> better
						else {
							if ( bestMap == null || scaleNearerOrEuqal(mi.scale, bestMap.scale, scale)) {
								latNearer = java.lang.Math.abs(ll.latDec - mi.center.latDec)/mi.sizeKm < minDistLat ;
								lonNearer = java.lang.Math.abs(ll.lonDec - mi.center.lonDec)/mi.sizeKm < minDistLon;
								if ( latNearer && lonNearer) better = true; // for faster processing: if lat and lon are nearer then the distancance doesn't need to be calculated
								else {
									if ( (latNearer || lonNearer )) {
										if (bestMap == null || mi.center.getDistanceRad(ll) < bestMap.center.getDistanceRad(ll) ){
											better = true;
										}
									}
								}
							}
						}
						if (better) {
							minDistLat = java.lang.Math.abs(ll.latDec - mi.center.latDec)/mi.sizeKm;
							minDistLon = java.lang.Math.abs(ll.lonDec - mi.center.lonDec)/mi.sizeKm;
							bestMap = mi;
							//foundkw = i;
							// Vm.debug("better"+ i);
						}
					}
				}
			}
		}
		if (progressBox != null) {
			progressBox.close(0);
			ewe.sys.Vm.showWait(false);
		}
		if (bestMap == null) return null;
		//Vm.debug("getBestMap: guess: " + guess + ", bestmap: " + foundkw + ", durchläufe: "+ testdkw);
		return new MapInfoObject(bestMap); // return a copy of the MapInfoObject so that zooming won't change the MapInfoObject in the list
	}
	/*
	public MapInfoObject getBestMapNotStrictSciale(double lat, double lon, Area screen, float scale) {
		MapInfoObject ret = getBestMap(lat, lon, screen, scale, true);
		if (ret == null) ret = getBestMap(lat, lon, screen, scale, false);
		return ret;
	}
	 */

	private final static int MAXDIGITS_IN_FF = 30;
	/** after calling onCompletedRead() this will contain a list of
	 * indexes at which a new number of digits in the sortEntryBBox start
	 */
	private int[] numDigitsStartIndex = new int[MAXDIGITS_IN_FF];
	private int maxDigits = -1;
	public void onCompletedRead() {
		sort(
				new Comparer()
				{
					public int compare(Object o1, Object o2){
						String s1 = ((MapListEntry)o1).sortEntryBBox;
						String s2 = ((MapListEntry)o2).sortEntryBBox;
						int ret = s1.length() - s2.length(); // sort shorter sortEntryBBox at the beginning
						if (ret == 0) ret = s1.compareTo(s2);
						return ret;
					}
				}
				, false);
		int digits_index = 0;
		int numdigits = 0;
		int s = size(); // avoid calling size() for each MaplistEntry (could be some thousand times)
		for (int i = 0; i < s; i++) {
			if ( ((MapListEntry)get(i)).sortEntryBBox.length() > numdigits) {
				numDigitsStartIndex[digits_index] = i;
				digits_index++;
				numdigits = ((MapListEntry)get(i)).sortEntryBBox.length();
			}
		}
		numDigitsStartIndex[digits_index] = s;
		maxDigits = digits_index;
	}

	/**
	 * @param llimitorig lower limit to start the search from
	 * @param ulimit upper limit to stop the search. llimit and ulimit must be set in a way that
	 * the sortEntryBBox of each entry betwenn the limits have the same length
	 * @param searchfor String starting with FF1, it should be longer than any sortEntryBoxString in the list
	 * @return the highest index which matches the searchfor-String. Look downward from there to find the best map, returns llimit if there is no match
	 */
	private int quickfind(String searchfor, int llimitorig, int ulimit) {
		int llimit = llimitorig;
		int test;
		String cmp = ((MapListEntry)this.get(llimit)).sortEntryBBox;
		String sshort = searchfor.substring(0, cmp.length());
		int comp;
//		int testskw = 0;
		if ( cmp.compareTo(sshort) > 0 ||
			 ((MapListEntry)this.get(ulimit)).sortEntryBBox.compareTo(sshort) < 0)
			return llimit; // if searchfor is not in the range, return llimit (llimit because getBestMap counts downward, so returning llimit will cause it to do only 1 test
		while (ulimit - llimit > 1) {
	//		testskw++;
			test = (ulimit + llimit) / 2;
			cmp = ((MapListEntry)this.get(test)).sortEntryBBox;
			comp = cmp.compareTo(searchfor);
			if ( comp > 0 ) { // test > searchfor
				ulimit = test;
			} else { // test <= searchfor
				if (comp < 0) llimit = test; // test < serachfor
				else { // test == searchfor
					llimit = test;
					ulimit = test;
				}
			}
		} // searchfor is between llimit and ulimit OR is at ulimit or higher OR at llimit or lower 
		// we want to return the highest index of the map which starts with searchfor 
		comp = ((MapListEntry)this.get(ulimit)).sortEntryBBox.compareTo(searchfor);
		if ( (comp <= 0) && searchfor.startsWith(((MapListEntry)this.get(ulimit)).sortEntryBBox) ) 
			llimit = ulimit; // search for is on ulimit or higher
	     
		if (!searchfor.startsWith(((MapListEntry)this.get(llimit)).sortEntryBBox)) 
			llimit = llimitorig; // if the found mapListEntry doesn't contain the searchfor, then there is no map containing it.
		// Vm.debug("quickfind: testskw: " + testskw + ", searched for: " + searchfor);
		return llimit;
	}

		/**
	 * @return a map which includs topleft and bottomright,
	 * if no map includes both it returns null
	 * @param if more than one map includes topleft and bottomright than the one will
	 * be returned which has its center nearest to topleft. If you have gps-pos and goto-pos
	 * as topleft and buttomright use gps as topleft.
	 * if topleft is really topleft or if it is buttomright is not relevant.
	 */

	public final MapInfoObject getMapForArea(CWPoint topleft, CWPoint bottomright){
		long start = new Time().getTime();
		InfoBox progressBox = null;
		boolean showprogress = false;
		MapListEntry ml;
		MapInfoObject mi;
		String cmp = "FF1"+(new Area(topleft, bottomright)).getEasyFindString();
		String cmppadded = Common.rightPad(cmp, 30);
		MapInfoObject fittingmap = null;
		int guess;
		boolean latNearer, lonNearer;
		boolean better;
		double minDistLat = 10000000000000000000000.0;
		double minDistLon = 10000000000000000000000.0;
		for (int digitlength = 0; digitlength < maxDigits; digitlength++) {
			guess = quickfind(cmppadded, this.numDigitsStartIndex[digitlength], this.numDigitsStartIndex[digitlength+1]-1);
			if ( ((MapListEntry)get(guess)).sortEntryBBox.length() > cmp.length() ) break; // if the sortEntryBBox indicates that it cannot contain both points, stop searching
			for (int i=guess; i >= numDigitsStartIndex[digitlength] ;i--) {
				if (!showprogress && ((i & 31) == 0) && (new Time().getTime()-start  > 100) ) { // reason for (i & 7 == 0): test time only after i is incremented 15 times
					showprogress = true;
					progressBox = new InfoBox(MyLocale.getMsg(327,"Info"), MyLocale.getMsg(4701,"Searching for best map"));
					progressBox.exec();
					progressBox.waitUntilPainted(100);
					ewe.sys.Vm.showWait(true);
				}
				ml = (MapListEntry)get(i);
				try {
					if (!Area.containsRoughly(ml.sortEntryBBox, cmp)) continue; // TODO if no map available
					else { mi = ml.getMap();}
				} catch (IOException ex) {continue; } // could not read .wfl-file
				better = false;
				if (mi.isInBound(topleft) && mi.isInBound(bottomright)) { // both points are inside the map
					if (fittingmap == null || fittingmap.scale > mi.scale * scaleTolerance) {
						better = true; // mi map has a better (lower) scale than the last knwon good map
					} else {
						if (scaleEquals(mi, fittingmap)) { // same scale as bestmap till now -> test if its center is nearer to the gps-point = topleft
							latNearer = java.lang.Math.abs(topleft.latDec- mi.center.latDec)/mi.sizeKm < minDistLat ;
							lonNearer = java.lang.Math.abs(topleft.lonDec - mi.center.lonDec)/mi.sizeKm < minDistLon;
							if ( latNearer && lonNearer) better = true; // for faster processing: if lat and lon are nearer then the distancance doesn't need to be calculated
							else {
								if ( (latNearer || lonNearer )) {
									if (mi.center.getDistanceRad(topleft) < fittingmap.center.getDistanceRad(topleft) ) better = true;
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
		if (fittingmap == null) return null;
		return new MapInfoObject(fittingmap); // TODO in case that this one and the old one are identical this instantiation could eventually be avoided as it is done at every greater shift of the map
	}

	/**
	 *
	 * @param lat a point to be inside the map
	 * @param lon
	 * @param screen: width, height of the screen. The map must overlap the screen. xy: where is lat/lon on screen
	 * @param curScale reference scale to be changed
	 * @param moreDetails true: find map with more details == higher resolustion = lower scale / false find map with less details = better overview
	 * @return
	 */
	public MapInfoObject getMapChangeResolution(CWPoint ll, Rect screen, float curScale, boolean moreDetails){
		if (size() == 0) return null;
		long start = new Time().getTime();
		InfoBox progressBox = null;
		boolean showprogress = false;
		MapListEntry ml;
		MapInfoObject mi;
		MapInfoObject bestMap = null; // = (MapInfoObject)get(0);
		double minDistLat = 1000000000000000000000000000000000000000000000.0;
		double minDistLon = 1000000000000000000000000000000000000000000000.0;
		boolean latNearer, lonNearer;
		boolean better = false;
		Area screenArea = null; // getAreaForScreen(screen, lat, lon, bestMap.scale, bestMap);
		float lastscale = -1;
		String cmp = "FF1"+Area.getEasyFindString(ll, MAXDIGITS_IN_FF);
		for (int i=size()-1; i >= 0 ;i--) {
			if (!showprogress && ((i & 31) == 0) && (new Time().getTime()-start  > 100) ) { // reason for (i & 7 == 0): test time only after i is incremented 15 times
				showprogress = true;
				progressBox = new InfoBox(MyLocale.getMsg(327,"Info"), MyLocale.getMsg(4701,"Searching for best map"));
				progressBox.exec();
				progressBox.waitUntilPainted(100);
				ewe.sys.Vm.showWait(true);
			}
			better = false;
			ml = (MapListEntry)get(i);
			try {
				if (!Area.containsRoughly(ml.sortEntryBBox, cmp)) continue; // TODO if no map available
				else { mi = ml.getMap();}
			} catch (IOException ex) {continue; } // could not read .wfl-file
			if (mi.fileNameWFL == "") continue; // exclude "maps" without image // TODO make this a boolean in MapInfoObject
			if (screenArea == null || !scaleEquals(lastscale, mi)) {
				screenArea = getAreaForScreen(screen, ll, mi.scale, mi);
				lastscale = mi.scale;
			}
			if (screenArea.isOverlapping(mi)) { // is on screen
				if (bestMap == null || !scaleEquals(mi, bestMap)) { // different scale than known bestMap?
					if (mi.isInBound(ll) && (      // more details wanted and this map has more details?                                // less details than bestmap
							(moreDetails && (curScale > mi.scale * scaleTolerance) && (bestMap == null || mi.scale > bestMap.scale * scaleTolerance ) ) // higher resolution wanted and mi has higher res and a lower res than bestmap, because we dont want to overjump one resolution step
							|| (!moreDetails && (curScale *  scaleTolerance < mi.scale) && (bestMap == null || mi.scale * scaleTolerance < bestMap.scale) ) // lower resolution wanted and mi has lower res and a higher res than bestmap, because we dont want to overjump one resolution step
					) )	better = true;	// inbound and higher resolution if higher res wanted -> better
				} else { // same scale as bestmap -> look if naerer
					latNearer = java.lang.Math.abs(ll.latDec - mi.center.latDec)/mi.sizeKm < minDistLat ;
					lonNearer = java.lang.Math.abs(ll.lonDec - mi.center.lonDec)/mi.sizeKm < minDistLon;
					if ( latNearer && lonNearer) better = true; // for faster processing: if lat and lon are nearer then the distancance doesn't need to be calculated
					else {
						if ( (latNearer || lonNearer )) {
							if (mi.center.getDistanceRad(ll) < bestMap.center.getDistanceRad(ll) ) better = true;
						}
					}
				} // same scale
				if (better) {
					minDistLat = java.lang.Math.abs(ll.latDec - mi.center.latDec)/mi.sizeKm;
					minDistLon = java.lang.Math.abs(ll.lonDec - mi.center.lonDec)/mi.sizeKm;
					bestMap = mi;
					// Vm.debug("better"+ i);
				}
			}
		}
		if (progressBox != null) {
			progressBox.close(0);
			ewe.sys.Vm.showWait(false);
		}
		if (bestMap == null) return null;
		return new MapInfoObject(bestMap);
	}
	/**
	 * returns an area in lat/lon of the screen
	 * @param a screen width / height and position of lat/lon on the screen
	 * @param lat a (reference) point on the screen
	 * @param lon
	 * @param scale scale (meters per pixel) of the map for which the screen edges are wanted
	 * @param map map for which the screen edges are wanted
	 * @return
	 */
	private Area getAreaForScreen(Rect a, CWPoint ll, float scale, MapInfoObject map) {
		Area ret = null;
		Point xy = map.calcMapXY(ll);
		Point topleft = new Point(xy.x - a.x, xy.y - a.y);
		ret = new Area(map.calcLatLon(topleft), map.calcLatLon(topleft.x+a.width, topleft.y+a.height));
		return ret;
	}
	public static boolean scaleEquals(MapInfoObject a, MapInfoObject b) {
		//return java.lang.Math.abs(a.scale - b.scale) < scaleTolerance;
		if (a.scale > b.scale) return a.scale / b.scale < scaleTolerance;
		else return b.scale / a.scale < scaleTolerance;
	}
	public static boolean scaleEquals(float s, MapInfoObject b) {
		//return java.lang.Math.abs(s - b.scale) < scaleTolerance;
		if (s > b.scale) return s / b.scale < scaleTolerance;
		else return b.scale / s < scaleTolerance;
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
		} else {
			testa = wanted;
			wanta = test;
		}
		if (old > wanted) { // ensure that second term is greater than 1
			olda = old;
			wantb = wanted;
		} else {
			olda = wanted;
			wantb = old;
		}
		return testa/wanta * scaleTolerance < olda/wantb;
	}

	public static boolean scaleNearerOrEuqal(float test, float old, float wanted) {
		float testa, wanta, wantb, olda;
		if (test > wanted) { // ensure that first term is greater than 1
			testa = test;
			wanta = wanted;
		} else {
			testa = wanted;
			wanta = test;
		}
		if (old > wanted) { // ensure that second term is greater than 1
			olda = old;
			wantb = wanted;
		} else {
			olda = wanted;
			wantb = old;
		}
		return testa/wanta < olda/wantb * scaleTolerance ;
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
	/** for determining if a new map should be downloaded
	public boolean isInAmap(CWPoint topleft, CWPoint buttomright) {
		if (!latRangeList.isInRange(topleft.latDec) || !latRangeList.isInRange(buttomright.latDec)) ||
			!lonRangeList.inInRange(topleft.lonDec) || !lonRangeList.isInRange(buttomright.lonDec)
			return false;
	}
	 */
	private String _mapsPath="";
	public String getMapsPath() {
		return _mapsPath;
	}
	
}

final class MapListEntry /*implements Comparable */ {
	public String sortEntryBBox;
	//String sortEntry;
	public String filename;
	String path;
	MapInfoObject map;
	
	static int rename = 0;
	static int renameCounter = 0;
	static InfoBox renameProgressInfoB = null;

	public MapListEntry (String pathi, String filenamei) {
		filename = new String(filenamei);
		path = new String(pathi);
		sortEntryBBox = null;
		map = null;
		/*
		try {map = new MapInfoObject(path, filename); } catch (Exception e) {
		}

		ewe.sys.Vm.debug("centerID: "+map.getCenterID());
		ewe.sys.Vm.debug("PxID: "+map.getPxSizeID());
		ewe.sys.Vm.debug("scaleID: "+map.getScaleID()+"scale: "+map.scale);
		*/
		try {
			if (filenamei.startsWith("FF1")) sortEntryBBox = filenamei.substring(0, filenamei.indexOf("E-"));
		} catch (IndexOutOfBoundsException ex) {
			// Global.getPref().log("Ignored Exception", ex, true);
		}
		if (sortEntryBBox == null ) { //|| sortEntryScaleCenterPx.length() < 16) {
			try {
				map = new MapInfoObject(path, filename);
				sortEntryBBox = "FF1"+map.getEasyFindString();
				ewe.sys.Vm.debug(sortEntryBBox + ": "+filename);
				if (rename == 0) { // never asked before
					if ( (new MessageBox(MyLocale.getMsg(4702,"Optimisation"), MyLocale.getMsg(4703,"Cachewolf can make loading maps much faster by adding a identification mark to the filename. Do you want me to do this now?\n It can take several minutes"),
							FormBase.YESB | FormBase.NOB)).execute() == FormBase.IDYES)
					{
						renameProgressInfoB = new InfoBox(MyLocale.getMsg(327,"Info"), MyLocale.getMsg(4704,"\nRenaming file:")+"    \n");
						renameProgressInfoB.exec();
						renameProgressInfoB.waitUntilPainted(100);
						rename = 1; // rename
					} else rename = 2; // don't rename
				}
				if (rename == 1) {
					renameCounter++;
					renameProgressInfoB.setInfo(MyLocale.getMsg(4704,"\nRenaming file:")+" " + renameCounter+"\n");
					String f = path+filename+".wfl";
					String to = sortEntryBBox+"E-"+filename+".wfl";
					if (!new File(f).rename(to))
						(new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(4705,"Failed to rename:\n")+f+".wfl"+MyLocale.getMsg(4706,"\nto:\n")+to, FormBase.OKB)).exec();
					f = Common.getImageName(path+filename);
					if (f != null) {
						to = sortEntryBBox+"E-"+filename+Common.getFilenameExtension(f);
						if (!new File(f).rename(to)) {
							Global.getPref().log("MapListEntry (String pathi, String filenamei): Failed to rename: "+path+filename+": "+f+".wfl to: "+to);
							(new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(4705,"Failed to rename:\n")+f+".wfl"+MyLocale.getMsg(4706,"\nto:\n")+to, FormBase.OKB)).exec();
						}
					} else {
						Global.getPref().log("MapListEntry (String pathi, String filenamei): Could not find image assiciated to: "+path+filename+".wfl");
						(new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(4709,"Could not find image assiciated to:\n")+path+filename+".wfl", FormBase.OKB)).exec();
					}
					filename = sortEntryBBox+"E-"+filename;
					map.mapName = sortEntryBBox+"E-"+map.mapName;
					map.fileNameWFL = path + filename + ".wfl";
				}
			} catch (IOException ioex) { // this should not happen
				(new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(4707,"I/O-Error while reading:")+" "+path+filename+": "+ ioex.getMessage(), FormBase.OKB)).exec();
				Global.getPref().log("MapListEntry (String pathi, String filenamei): I/O-Error while reading: "+path+filename+": ", ioex);
			} catch (Exception ex) {
				(new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(4706,"Error while reading:")+" "+path+filename+": "+ ex.getMessage(), FormBase.OKB)).exec();
				Global.getPref().log("MapListEntry (String pathi, String filenamei): Error while reading: "+path+filename+": ", ex);
			}
		}
	}

	public MapListEntry(double scale, double lat) {
		map = new MapInfoObject(scale, lat);
		filename = map.mapName;
		sortEntryBBox = "FF1";
	}

	public MapInfoObject getMap() throws IOException {
		if (map == null) map = new MapInfoObject(path, filename);
		return map;
	}

	public static void loadingFinished() {
		if (renameProgressInfoB != null) renameProgressInfoB.close(0);
		renameProgressInfoB = null;
		rename = 0;
	}

	/*
	// this maybe needed some time later to further enhance the speed of finding the best map
	public int compareTo(Object other) {
		if (other == null) return 1;
		return this.sortEntryBBox.compareTo(((MapListEntry)other).sortEntryBBox);
	} */
}

