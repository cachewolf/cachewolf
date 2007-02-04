package CacheWolf;

import ewe.io.File;
import ewe.io.IOException;
import ewe.sys.Double;
import ewe.ui.MessageBox;
import ewe.util.Hashtable;
import ewe.util.Vector;
import ewe.fx.*;
/**
 * class to handle a list of maps
 * it loads the list, finds the best map for a given location,
 * says if a map is available for a given lat lon at a given scale
 * @author r
 *
 */
public class MapsList extends Vector {
	public static float scaleTolerance = 0.01f; // absolute deviations from this factor are seen to have the same scale
	public Hashtable scales2Area;

	/**
	 * loads alle the maps in mapsPath in all subDirs but not recursive, only one level 
	 * @param mapsPath
	 */
	public MapsList(String mapsPath) {
		super(); // forget already loaded maps
		//if (mmp.mapImage != null) 
		String dateien[];
		File files = new File(mapsPath);
		String rawFileName = new String();
		String[] dirstmp = files.list("*.wfl", File.LIST_ALWAYS_INCLUDE_DIRECTORIES | File.LIST_DIRECTORIES_ONLY);
		Vector dirs;
		if (dirstmp != null) dirs = new Vector(dirstmp);
		else dirs = new Vector();
		dirs.add("."); // include the mapsPath itself
		MapInfoObject tempMIO;
		MessageBox f = null;
		for (int j = dirs.size()-1; j >= 0; j--) {
			files = new File(mapsPath+"/"+dirs.get(j));
			dateien = files.list("*.wfl", File.LIST_FILES_ONLY);
			for(int i = 0; i < dateien.length;i++){
				rawFileName = dateien[i].substring(0, dateien[i].lastIndexOf("."));
				try {
					tempMIO = new MapInfoObject();
					tempMIO.loadwfl(mapsPath+"/"+dirs.get(j)+"/", rawFileName);
					add(tempMIO);
				}catch(IOException ex){ 
					if (f == null) (f=new MessageBox("Warning", "Ignoring error while \n reading calibration file \n"+ex.toString(), MessageBox.OKB)).exec();
				}catch(ArithmeticException ex){ // affine contain not allowed values 
					if (f == null) (f=new MessageBox("Warning", "Ignoring error while \n reading calibration file \n"+ex.toString(), MessageBox.OKB)).exec();
				} 
			}
		}
	}

	public void addEmptyMaps(double lat) {
		MapInfoObject tempMIO;
		tempMIO = new MapInfoObject(1.0, lat);
		add(tempMIO);
		tempMIO = new MapInfoObject(5.0, lat);
		add(tempMIO);
		tempMIO = new MapInfoObject(50.0, lat);
		add(tempMIO);
		tempMIO = new MapInfoObject(250.0, lat);
		add(tempMIO);
		tempMIO = new MapInfoObject(1000.0, lat);
		add(tempMIO);
	}

	/**
	 * find the best map for lat/lon in the list of maps
	 * currently the best map is the one, whose center is nearest to lat/lon
	 * and in Area with ist scale nearest to scale.
	 * it always return a map (if the list is not empty) 
	 * even if the map is not inbound
	 * lat/lon
	 * @param lat
	 * @param lon
	 * @param forceScale: when true, return null if no map with specified scale could be found
	 * @return
	 */
	public MapInfoObject getBestMap(double lat, double lon, Rect screen, float scale, boolean forceScale) {
		if (size() == 0) return null;
		if (scales2Area != null) scales2Area.clear();
		MapInfoObject mi;
		MapInfoObject bestMap = null; // = (MapInfoObject)get(0);
		double minDistLat = 1000000000000000000000000000000000000000000000.0;
		double minDistLon = 1000000000000000000000000000000000000000000000.0;
		boolean latNearer, lonNearer;
		boolean better = false;
		Area screenArea = null; // getAreaForScreen(screen, lat, lon, bestMap.scale, bestMap);
		float lastscale = -1;
		for (int i=size()-1; i >= 0 ;i--) { 
			better = false;
			mi = (MapInfoObject)get(i);
			if (screenArea == null || java.lang.Math.abs(mi.scale - lastscale) > scaleTolerance) {
				screenArea = getAreaForScreen(screen, lat, lon, mi.scale, mi);
				lastscale = mi.scale;
			}
			if (screenArea.isOverlapping(mi.getArea()) ) { // is on screen
				if (!forceScale || (forceScale && java.lang.Math.abs(mi.scale - scale) > scaleTolerance)) { // different scale?
					if (!forceScale && (mi.inBound(lat, lon) && (bestMap == null || (java.lang.Math.abs(mi.scale-scale) + scaleTolerance < java.lang.Math.abs(bestMap.scale-scale))))) 
						better = true; // inbound and higher resolution -> better
					else {
						if ( bestMap == null || (java.lang.Math.abs(mi.scale-scale) < java.lang.Math.abs(bestMap.scale-scale) + scaleTolerance)) {
							latNearer = java.lang.Math.abs(lat - mi.center.latDec)/mi.sizeKm < minDistLat ;
							lonNearer = java.lang.Math.abs(lon - mi.center.lonDec)/mi.sizeKm < minDistLon;
							if ( latNearer && lonNearer) better = true; // for faster processing: if lat and lon are nearer then the distancance doesn't need to be calculated
							else {
								if ( (latNearer || lonNearer )) { 
									if (bestMap == null || mi.center.getDistanceRad(lat, lon) < bestMap.center.getDistanceRad(lat, lon) ) better = true;
								}
							}
						}
					}
					if (better) {
						minDistLat = java.lang.Math.abs(lat - mi.center.latDec)/mi.sizeKm;
						minDistLon = java.lang.Math.abs(lon - mi.center.lonDec)/mi.sizeKm;
						bestMap = mi;
						// Vm.debug("better"+ i);
					}
				}
			}
		}
		return bestMap;
	}
	/*
	public MapInfoObject getBestMapNotStrictScale(double lat, double lon, Area screen, float scale) {
		MapInfoObject ret = getBestMap(lat, lon, screen, scale, true);
		if (ret == null) ret = getBestMap(lat, lon, screen, scale, false);
		return ret;
	}
	 */
	/**
	 * @return a map which includs topleft and bottomright, 
	 * if no map includes both it returns null
	 * @param if more than one map includes topleft and bottomright than the one will
	 * be returned which has its center nearest to topleft. If you have gps-pos and goto-pos
	 * as topleft and buttomright use gps as topleft.
	 * if topleft is really topleft or if it is buttomright is not relevant.  
	 */
	
	// TODO if more than one map contains both -> select the best one of them
	public MapInfoObject getMapForArea(CWPoint topleft, CWPoint bottomright){
		MapInfoObject mi;
		MapInfoObject fittingmap = null;
		boolean latNearer, lonNearer;
		boolean better;
		double minDistLat = 10000000000000000000000.0;
		double minDistLon = 10000000000000000000000.0;
		for (int i=size() -1; i>=0 ;i--) {
			better = false;
			mi = (MapInfoObject)get(i);
			if (mi.inBound(topleft) && mi.inBound(bottomright)) { // both points are inside the map
				if (fittingmap == null || fittingmap.scale > mi.scale + scaleTolerance) {
					better = true; // mi map has a better (lower) scale than the last knwon good map
				} else {
					if (fittingmap != null && java.lang.Math.abs(mi.scale - fittingmap.scale) < scaleTolerance) { // same scale as bestmap till now -> test if its center is nearer to the gps-point = topleft
						latNearer = java.lang.Math.abs(topleft.latDec- mi.center.latDec)/mi.sizeKm < minDistLat ;
						lonNearer = java.lang.Math.abs(topleft.lonDec - mi.center.lonDec)/mi.sizeKm < minDistLon;
						if ( latNearer && lonNearer) better = true; // for faster processing: if lat and lon are nearer then the distancance doesn't need to be calculated
						else {
							if ( (latNearer || lonNearer )) { 
								if (mi.center.getDistanceRad(topleft.latDec, topleft.lonDec) < fittingmap.center.getDistanceRad(topleft.latDec, topleft.lonDec) ) better = true;
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
		} // for
		return fittingmap;
	}

	/**
	 * 
	 * @param lat a point to be inside the map
	 * @param lon
	 * @param screen
	 * @param curScale reference scale to be changed
	 * @param moreDetails true: find map with more details == higher resolustion = lower scale / false find map with less details = better overview
	 * @return
	 */
	public MapInfoObject getMapChangeResolution(double lat, double lon, Rect screen, float curScale, boolean moreDetails){
		if (size() == 0) return null;
		if (scales2Area != null) scales2Area.clear();
		MapInfoObject mi;
		MapInfoObject bestMap = null; // = (MapInfoObject)get(0);
		double minDistLat = 1000000000000000000000000000000000000000000000.0;
		double minDistLon = 1000000000000000000000000000000000000000000000.0;
		boolean latNearer, lonNearer;
		boolean better = false;
		Area screenArea = null; // getAreaForScreen(screen, lat, lon, bestMap.scale, bestMap);
		float lastscale = -1;
		for (int i=size()-1; i >= 0 ;i--) { 
			better = false;
			mi = (MapInfoObject)get(i);
			if (mi.fileNameWFL == "") continue; // exclude "maps" without image
			if (screenArea == null || java.lang.Math.abs(mi.scale - lastscale) > scaleTolerance) {
				screenArea = getAreaForScreen(screen, lat, lon, mi.scale, mi);
				lastscale = mi.scale;
			}
			if (screenArea.isOverlapping(mi.getArea())) { // is on screen
				if (bestMap == null || java.lang.Math.abs(mi.scale - bestMap.scale) > scaleTolerance) { // different scale then known bestMap?
					if (mi.inBound(lat, lon) && (      // more details                                 // less details than bestmap
							(moreDetails && (curScale > mi.scale + scaleTolerance) && (bestMap == null || mi.scale-scaleTolerance > bestMap.scale) ) // higher resolution wanted and mi has higher res and a lower res than bestmap, because we dont want to overjump one resolution step
							|| (!moreDetails && (curScale < mi.scale - scaleTolerance) && (bestMap == null || mi.scale + scaleTolerance < bestMap.scale) ) // lower resolution wanted and mi has lower res and a higher res than bestmap, because we dont want to overjump one resolution step
					) )	better = true;	// inbound and higher resolution if higher res wanted -> better
				} else { // same scale as bestmap -> look if naerer 
					latNearer = java.lang.Math.abs(lat - mi.center.latDec)/mi.sizeKm < minDistLat ;
					lonNearer = java.lang.Math.abs(lon - mi.center.lonDec)/mi.sizeKm < minDistLon;
					if ( latNearer && lonNearer) better = true; // for faster processing: if lat and lon are nearer then the distancance doesn't need to be calculated
					else {
						if ( (latNearer || lonNearer )) { 
							if (bestMap == null || mi.center.getDistanceRad(lat, lon) < bestMap.center.getDistanceRad(lat, lon) ) better = true;
						}
					}
				} // same scale
				if (better) {
					minDistLat = java.lang.Math.abs(lat - mi.center.latDec)/mi.sizeKm;
					minDistLon = java.lang.Math.abs(lon - mi.center.lonDec)/mi.sizeKm;
					bestMap = mi;
					// Vm.debug("better"+ i);
				}
			}
		}
		return bestMap;
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
	private Area getAreaForScreen(Rect a, double lat, double lon, float scale, MapInfoObject map) {
		Area ret = null;
/*		if (scales2Area == null) scales2Area = new Hashtable();
		else ret = (Area)scales2Area.get(scale);
		if (ret != null) return ret;
	*/	// calculate screen Area
		Point xy = map.calcMapXY(lat, lon);
		Point topleft = new Point(xy.x - a.x, xy.y - a.y);
		ret = new Area(map.calcLatLon(topleft), map.calcLatLon(topleft.x+a.width, topleft.y+a.height));
		//scales2Area.put(new Float(scale), ret);
		return ret; 
	}
	public static boolean scaleEquals(MapInfoObject a, MapInfoObject b) {
		return java.lang.Math.abs(a.scale - b.scale) < scaleTolerance; 
	}

	/** for determining if a new map should be downloaded
	public boolean isInAmap(CWPoint topleft, CWPoint buttomright) {
		if (!latRangeList.isInRange(topleft.latDec) || !latRangeList.isInRange(buttomright.latDec)) ||
			!lonRangeList.inInRange(topleft.lonDec) || !lonRangeList.isInRange(buttomright.lonDec)
			return false;
	}
	 */
}
