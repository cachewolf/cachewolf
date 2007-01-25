package CacheWolf;

import ewe.io.File;
import ewe.io.IOException;
import ewe.ui.MessageBox;
import ewe.util.Vector;
/**
 * class to handle a list of maps
 * it loads the list, finds the best map for a given location
 * says if a map is available for a given lat lon at a given scale
 * @author r
 *
 */
public class MapsList extends Vector {

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
	 * currently the best map is the one, whose center is nearest to
	 * it always return a map (if the list is not empty) 
	 * even if the map is not inbound
	 * lat/lon
	 * @param lat
	 * @param lon
	 * @return
	 */
public MapInfoObject getBestMap(double lat, double lon) {
		if (size() == 0) return null;
		MapInfoObject mi;
		MapInfoObject bestMap = (MapInfoObject)get(0);
		double minDistLat = 1000000000000000000000000000000000000000000000.0;
		double minDistLon = 1000000000000000000000000000000000000000000000.0;
		boolean latNearer, lonNearer;
		int minDistMap = -1; // return this value if you are interested in the number of the map
		boolean better = false;
		for (int i=0; i < size() ;i++) {
			better = false;
			mi = (MapInfoObject)get(i);
			latNearer = java.lang.Math.abs(lat - mi.center.latDec)/mi.sizeKm < minDistLat ;
			lonNearer = java.lang.Math.abs(lon - mi.center.lonDec)/mi.sizeKm < minDistLon;
			if ( latNearer && lonNearer) better = true; // for faster processing: if lat and lon are nearer then the distancance doesn't need to be calculated
			if ( !better && (latNearer || lonNearer )) { 
				if ( mi.center.getDistanceRad(lat, lon) < bestMap.center.getDistanceRad(lat, lon) ) better = true;
			}
			if (better) {
				minDistLat = java.lang.Math.abs(lat - mi.center.latDec)/mi.sizeKm;
				minDistLon = java.lang.Math.abs(lon - mi.center.lonDec)/mi.sizeKm;
				minDistMap = i;
				bestMap = mi;
				// Vm.debug("better"+ i);
			}
		}
		return bestMap; // return minDistMap
	}

	public MapInfoObject getMapForRect(CWPoint topleft, CWPoint bottomright){
		MapInfoObject mi;
		MapInfoObject fittingmap = null;
		for (int i=0; i < size() ;i++) {
			mi = (MapInfoObject)get(i);
			if (mi.inBound(topleft) && mi.inBound(bottomright)) {
				if (fittingmap == null || fittingmap.scaleX > mi.scaleX) fittingmap = mi;
			}
		} // for
		return fittingmap;
	}
	/*
	public boolean isInAmap(CWPoint topleft, CWPoint buttomright) {
		if (!latRangeList.isInRange(topleft.latDec) || !latRangeList.isInRange(buttomright.latDec)) ||
			!lonRangeList.inInRange(topleft.lonDec) || !lonRangeList.isInRange(buttomright.lonDec)
			return false;
	}
	*/
}
