package CacheWolf;

import ewe.ui.*;
import ewe.graphics.*;
import ewe.io.File;
import ewe.io.IOException;
import ewe.sys.*;
import ewe.fx.*;
import ewe.util.Vector;

/**
*	Class to handle a moving map.
*/
public class MovingMap extends Form {
	final static int gotFix = 4; //green
	final static int lostFix = 3; //yellow
	final static int noGPSData = 2; // red
	final static int noGPS = 1; // no GPS-Position marker, manually disconnected 
	final static int ignoreGPS = -1; // ignore even changes in GPS-signal (eg. from lost fix to gotFix) this is wanted when the map is moved manually
	
	public int GpsStatus;
	Preferences pref;
	MovingMapPanel mmp;
	//AniImage mapImage;
	Vector maps;
	Vector symbols;
	GotoPanel gotoPanel;
	Vector cacheDB;
	TrackOverlay[] TrackOverlays;
	Vector tracks;
	MapInfoObject currentMap;
	AniImage statusImageHaveSignal = new AniImage("position_green.png");
	AniImage statusImageNoSignal = new AniImage("position_yellow.png");
	AniImage statusImageNoGps = new AniImage("position_red.png");
	
	AniImage ButtonImageChooseMap = new AniImage("choose_map.gif"); 
	AniImage ButtonImageGpsOn = new AniImage("snap2gps.gif"); 
	AniImage arrowUp = new AniImage("arrow_up.png");
	AniImage arrowDown = new AniImage("arrow_down.png");
	AniImage arrowLeft = new AniImage("arrow_left.png");
	AniImage arrowRight = new AniImage("arrow_right.png");
	AniImage posCircle = new AniImage("position_green.png");
	int posCircleX = 0, posCircleY = 0, lastCompareX = Integer.MAX_VALUE, lastCompareY = Integer.MAX_VALUE;
	double posCircleLat, posCircleLon;
	
	boolean ignoreGps = false;
	boolean ignoreGpsStatutsChanges = false;
	boolean autoSelectMap = true;
	boolean forceMapLoad = true; // only needed to force updateposition to try to load the best map again after OutOfMemoryError after an repeated click on snap-to-gps
	
	public MovingMap(Preferences pref, GotoPanel gP, Vector cacheDB){
		this.cacheDB = cacheDB;
		gotoPanel = gP;
		this.pref = pref;
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		this.title = "Moving Map";
		this.backGround = Color.Black;
		currentMap = new MapInfoObject();
		mmp = new MovingMapPanel(this);
		this.addLast(mmp);
		mmp.addImage(posCircle);
		setGpsStatus(noGPS);
		ButtonImageChooseMap.setLocation(10,10);
		ButtonImageChooseMap.properties = AniImage.AlwaysOnTop;
		ButtonImageGpsOn.setLocation(pref.myAppWidth-25, 10);
		ButtonImageGpsOn.properties = AniImage.AlwaysOnTop;
		arrowUp.setLocation(pref.myAppWidth/2, 10);
		arrowDown.setLocation(pref.myAppWidth/2, pref.myAppHeight-20);
		arrowLeft.setLocation(10, pref.myAppHeight/2+7);
		arrowRight.setLocation(pref.myAppWidth-25, pref.myAppHeight/2+7);
		arrowUp.properties = AniImage.AlwaysOnTop;
		arrowDown.properties = AniImage.AlwaysOnTop;
		arrowLeft.properties = AniImage.AlwaysOnTop;
		arrowRight.properties = AniImage.AlwaysOnTop;
		mmp.addImage(arrowUp);
		mmp.addImage(arrowDown);
		mmp.addImage(arrowLeft);
		mmp.addImage(arrowRight);
		mmp.addImage(ButtonImageChooseMap);
		mmp.addImage(ButtonImageGpsOn);
		posCircle.properties = AniImage.AlwaysOnTop;
		loadMaps();
	}

	/**
	 * loads the list of maps
	 *
	 */
	public void loadMaps(){
		Vm.showWait(true);
		resetCenterOfMap();
		InfoBox inf = new InfoBox("Info", "Loading list of maps...");
		inf.exec();
		maps = new Vector();
		String dateien[];
		String mapsPath = new String();
		mapsPath = File.getProgramDirectory() + "/maps/";
		File files = new File(mapsPath);
		Extractor ext;
		String rawFileName = new String();
		dateien = files.list("*.png", File.LIST_FILES_ONLY);
		MapInfoObject tempMIO;
		for(int i = 0; i < dateien.length;i++){
			ext = new Extractor(dateien[i], "", ".", 0, true);
			rawFileName = ext.findNext();
			try {
				tempMIO = new MapInfoObject();
				tempMIO.loadwfl(mapsPath, rawFileName);
				maps.add(tempMIO);
			}catch(IOException ex){ } // TODO etwas genauer auch Fehlermeldung ausgeben? Bei vorhandenen .wfl-Datei mit ungültigen Werten Fehler ausgeben oder wie jetz einfach ignorieren?
		}
		if (maps.isEmpty())
			(new MessageBox(MyLocale.getMsg(327, "Information"), MyLocale.getMsg(326, "Es steht keine kalibrierte Karte zur Verfügung"), MessageBox.OKB)).execute();
		tempMIO = new MapInfoObject(1.0);
		maps.add(tempMIO);
		tempMIO = new MapInfoObject(5.0);
		maps.add(tempMIO);
		tempMIO = new MapInfoObject(50.0);
		maps.add(tempMIO);
		tempMIO = new MapInfoObject(250.0);
		maps.add(tempMIO);
		tempMIO = new MapInfoObject(1000.0);
		maps.add(tempMIO);
		inf.close(0);
		Vm.showWait(false);
	}

	public void addTrack(Track tr) {
		if (tr == null) return;
		if (tracks == null) tracks = new Vector();
		if (tracks.find(tr) >= 0 ) return; // track already in list
		tracks.add(tr);
		addOverlaySet();
	}
	
	public void addTracks(Track[] trs) {
		if (trs==null || trs.length == 0) return;
		for (int i=0; i<trs.length; i++) {
			addTrack(trs[i]);
		}
		addOverlaySet();
	}
	
	/**
	 * adds an 3x3 set of overlays to the map-window which contain the track
	 * 
	 * add tracks with addtrack(track) before
	 */
	
	public void addOverlaySet() {
		if (tracks == null) return; // no tracks
		if (TrackOverlays != null) {
			for (int i=0; i< TrackOverlays.length; i++) {	destroyOverlay(i);	}
		}
		addMissingOverlays();
	}
	

	public void addMissingOverlays() {
		Point upperleft = getMapXYPosition();
		int ww = pref.myAppWidth;
		int wh = pref.myAppHeight;
		int i;
		if (TrackOverlays == null) TrackOverlays = new TrackOverlay[9];
		for (int yi=0; yi<3; yi++) {
			for (int xi=0; xi<3; xi++) {
				i = yi*3+xi;
				if (TrackOverlays[i]==null) { 
					TrackOverlays[i]= new TrackOverlay(currentMap.calcLatLon(-upperleft.x+(xi-1)*ww, -upperleft.y+(yi-1)*wh), ww, wh, currentMap); 
					TrackOverlays[i].setLocation(0, 0);
					TrackOverlays[i].tracks = this.tracks;
					TrackOverlays[i].paintTracks();
					mmp.addImage(TrackOverlays[i]);
				}
			}
		}
		updateOverlayOnlyPos();
	}
	
	private void destroyOverlay(int ov) {
		if (TrackOverlays[ov] == null) return; 
		mmp.removeImage(TrackOverlays[ov]);
		TrackOverlays[ov].free();
		TrackOverlays[ov]=null;
	}
	public void rearangeOverlays() {
		if (TrackOverlays[1].isOnScreen()) { // oben raus
			TrackOverlays[6]=TrackOverlays[0];
			TrackOverlays[7]=TrackOverlays[1];
			TrackOverlays[8]=TrackOverlays[2];
			destroyOverlay(0);
			destroyOverlay(1);
			destroyOverlay(2);
			destroyOverlay(3);
			destroyOverlay(4);
			destroyOverlay(5);
		} else {
			if (TrackOverlays[3].isOnScreen()) { // links raus
				TrackOverlays[2]=TrackOverlays[0];
				TrackOverlays[5]=TrackOverlays[3];
				TrackOverlays[8]=TrackOverlays[6];
				destroyOverlay(0);
				destroyOverlay(1);
				destroyOverlay(3);
				destroyOverlay(4);
				destroyOverlay(6);
				destroyOverlay(7);
			} else {
				if (TrackOverlays[5].isOnScreen()) { // rechts raus
					TrackOverlays[0]=TrackOverlays[2];
					TrackOverlays[3]=TrackOverlays[5];
					TrackOverlays[6]=TrackOverlays[8];
					destroyOverlay(1);
					destroyOverlay(2);
					destroyOverlay(4);
					destroyOverlay(5);
					destroyOverlay(7);
					destroyOverlay(8);
				} else {
					if (TrackOverlays[7].isOnScreen()) { // unten raus
						TrackOverlays[0]=TrackOverlays[6];
						TrackOverlays[1]=TrackOverlays[7];
						TrackOverlays[2]=TrackOverlays[8];
						destroyOverlay(3);
						destroyOverlay(4);
						destroyOverlay(5);
						destroyOverlay(6);
						destroyOverlay(7);
						destroyOverlay(8);
					} else { // it is important to test for diagonal only if the other didn't match
						if (TrackOverlays[0].isOnScreen()) {  // links oben raus
							destroyOverlay(0);
							destroyOverlay(1);
							destroyOverlay(2);
							destroyOverlay(3);
							destroyOverlay(4);
							destroyOverlay(5);
							destroyOverlay(6);
							destroyOverlay(7);
							TrackOverlays[8]=TrackOverlays[0];
						} else {
							if (TrackOverlays[2].isOnScreen()) { // rechts oben raus
								TrackOverlays[6]=TrackOverlays[2];
								destroyOverlay(0);
								destroyOverlay(1);
								destroyOverlay(2);
								destroyOverlay(3);
								destroyOverlay(4);
								destroyOverlay(5);
								destroyOverlay(7);
								destroyOverlay(8);
							} else {
								if (TrackOverlays[6].isOnScreen()) { // links unten raus
									TrackOverlays[2]=TrackOverlays[6];
									destroyOverlay(0);
									destroyOverlay(1);
									destroyOverlay(3);
									destroyOverlay(4);
									destroyOverlay(5);
									destroyOverlay(6);
									destroyOverlay(7);
									destroyOverlay(8);
								} else {
									if (TrackOverlays[8].isOnScreen()) { // rechts unten raus
										TrackOverlays[0]=TrackOverlays[8];
										destroyOverlay(1);
										destroyOverlay(2);
										destroyOverlay(3);
										destroyOverlay(4);
										destroyOverlay(5);
										destroyOverlay(6);
										destroyOverlay(7);
										destroyOverlay(8);
									}else
										for (int i=0; i<TrackOverlays.length; i++) {destroyOverlay(i);} // this happens if a position jump occured
								}}}}}}} // close all IFs
			Vm.debug("Overlayrearanged"+TrackOverlays.toString());
	}

	public void ShowLastAddedPoint(Track tr) {
		if (TrackOverlays == null || tr == null) return;
		for (int i=0; i<TrackOverlays.length; i++){
			TrackOverlays[i].paintLastAddedPoint(tr);
		}
	}

	public void updateOverlayOnlyPos() {
		if (TrackOverlays == null) return;
		//	Point upperleft = getMapXYPosition();
		Point posOnScreen;
		posOnScreen = getXYinMap(TrackOverlays[4].topLeft.latDec, TrackOverlays[4].topLeft.lonDec);
		int ww = pref.myAppWidth;
		int wh = pref.myAppHeight;
		//Vm.sleep(100); // this is necessary because the ewe vm ist not multi-threaded and the serial thread also needs time
		for (int yi=0; yi<3; yi++) {
			for (int xi=0; xi<3; xi++) {
		//		if( (TrackOverlays[yi*3+xi].properties & AniImage.HasChanged) != 0) TrackOverlays[yi*3+xi].draw.r
				TrackOverlays[yi*3+xi].move(posOnScreen.x+(xi-1)*ww, posOnScreen.y+(yi-1)*wh);
			}
		}
	}
	
	public void updateOverlayPos() {
		if (TrackOverlays == null) return;
		updateOverlayOnlyPos();
		if (TrackOverlays[0].location.x>pref.myAppWidth || TrackOverlays[0].location.x + 3*pref.myAppWidth < 0 || // testForNeedToRearange
				TrackOverlays[0].location.y>pref.myAppHeight || TrackOverlays[0].location.y + 3*pref.myAppHeight <0) {
			rearangeOverlays();
			addMissingOverlays();
			// updateOverlayOnlyPos(); is called from addMissingOverlays 
		}
	}

	private int getBestMap(double lat, double lon) { // finds the map which is next (center of the map) to the gps-position / could be a good idea to seachr only maps which show the current position (use InBound)
		// maps, gotoPanel.gpsPosition.latDec != 0, gotoPanel.gpsPosition
		MapInfoObject mi = new MapInfoObject();
		MapInfoObject bestMap = new MapInfoObject();
		double minDistLat = 1000000000000000000000000000000000000000000000.0;
		double minDistLon = 1000000000000000000000000000000000000000000000.0;
		boolean latNearer, lonNearer;
		int minDistMap = -1;
		boolean better = false;
		for (int i=0; i<maps.size() ;i++) {
			better = false;
			mi=(MapInfoObject)maps.get(i);
			latNearer=java.lang.Math.abs(lat - mi.center.latDec)/mi.sizeKm < minDistLat ;
			lonNearer=java.lang.Math.abs(lon - mi.center.lonDec)/mi.sizeKm < minDistLon;
			if ( latNearer && lonNearer) better = true;
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
	//	tmp = secBestMap.center.getDistance(gotoPanel.gpsPosition.latDec, gotoPanel.gpsPosition.lonDec)/secBestMap.sizeKm; // quasi second return value 
		return minDistMap ;
	}
	
	
	
	public void resetCenterOfMap() {
		posCircleX = pref.myAppWidth/2; // maybe this could /should be repleced to windows size
		posCircleY = pref.myAppHeight/2;
		posCircle.setLocation(posCircleX-posCircle.getWidth()/2, posCircleY-posCircle.getHeight()/2);
	}

	/**
	 * get upper left corner of map on window
	 * returns the same as mmp.mapImage.getLocation(mapPos);
	 * but also works if mmp == null and is used to move the map to the correct point
	 * @return
	 */
	public Point getMapXYPosition() {
		Point mapPos = new Point(); 
		//if (mmp.mapImage != null) mmp.mapImage.getLocation(mapPos);
		//else {
			Point mapposint = currentMap.calcMapXY(posCircleLat, posCircleLon);
			mapPos.x = posCircleX - mapposint.x;
			mapPos.y = posCircleY - mapposint.y;
		//}
		return mapPos;
	}

	public Point getXYinMap(double lat, double lon){
		Point coords = new Point();
		Point mapPos = new Point();
		coords = currentMap.calcMapXY(lat, lon);
		mapPos = getMapXYPosition();
	//		Vm.debug("getXYinMap, posCiLat: "+posCircleLat+"poscLOn: "+ posCircleLon+"gotoLat: "+ lat + "gotoLon: "+ lon+" mapPosX: "+mapPos.x+"mapposY"+mapPos.y);
		return new Point(coords.x + mapPos.x, coords.y + mapPos.y);
	}
	
	public void updateSymbolPositions() {
		if (symbols == null) return;
		Point pOnScreen;
		MapSymbol symb;
		for (int i=symbols.size()-1; i>=0; i--) {
			symb = (MapSymbol)symbols.get(i);
			pOnScreen = getXYinMap(symb.lat, symb.lon);
			symb.pic.setLocation(pOnScreen.x-symb.pic.getWidth()/2, pOnScreen.y-symb.pic.getHeight()/2);
		}
	}
	
	public void addSymbol(String name, String filename, double lat, double lon) {
		if (symbols==null) symbols=new Vector();
		MapSymbol ms = new MapSymbol(name, filename, lat, lon);
		ms.loadImage();
		Point pOnScreen=getXYinMap(lat, lon);
		ms.pic.setLocation(pOnScreen.x-ms.pic.getWidth()/2, pOnScreen.y-ms.pic.getHeight()/2);
		symbols.add(ms);
		mmp.addImage(ms.pic);
	}
	public void addSymbol(String name, AniImage imSymb, double lat, double lon) {
		if (symbols==null) symbols=new Vector();
		MapSymbol ms = new MapSymbol(name, " ", lat, lon);
		ms.pic = imSymb;
		ms.pic.properties = AniImage.AlwaysOnTop;
		Point pOnScreen=getXYinMap(lat, lon);
		ms.pic.setLocation(pOnScreen.x-ms.pic.getWidth()/2, pOnScreen.y-ms.pic.getHeight()/2);
		symbols.add(ms);
		mmp.addImage(ms.pic);
	}
	
	public void setGotoPosition(double lat, double lon) {
		removeMapSymbol("goto");
		addSymbol("goto", "goto_map.png", lat, lon); 
	}
	
	public CWPoint getGotoPos(){
		int symbNr;
		symbNr = findMapSymbol("goto");
		if (symbNr == -1) return null;
		MapSymbol ms = (MapSymbol) symbols.get(symbNr);
		return new CWPoint(ms.lat, ms.lon);
	}
	
	public void removeAllMapSymbolsButGoto(){
		for (int i=symbols.size()-1; i>=0; i--) {
			if (((MapSymbol)symbols.get(i)).name != "goto") removeMapSymbol(i);
		}
	}
	
	public void removeMapSymbol(String name) {
		int symbNr = findMapSymbol(name);
		if (symbNr != -1) removeMapSymbol(symbNr);
	}

	public void removeMapSymbol(int SymNr) {
		mmp.removeImage(((MapSymbol)symbols.get(SymNr)).pic);
		symbols.removeElementAt(SymNr);
	}

	public int findMapSymbol(String name) {
		if (symbols == null) return -1;
		MapSymbol ms;
		for (int i = 0; i < symbols.size(); i++) {
			ms= (MapSymbol)symbols.get(i);
			if (ms.name == name) return i;
		}
		return -1;
	}

	/**
	* Move the map so that the posCircle is at lat/lon
	* 
	* @param lat && lon == -361 -> ignore lat/lon, set map position to upperleft corner of window 
	*/
	public void updateOnlyPosition(double lat, double lon, boolean updateOverlay){
		Point mapPos = new Point(0,0);
		Point oldMapPos = getMapXYPosition();
		if (lat != -361.0 || lon != -361.0) {
			posCircleLat = lat;
			posCircleLon = lon;
			mapPos = getMapXYPosition();
		}
		//Vm.debug("mapx = " + mapx);
		//Vm.debug("mapy = " + mapy);
		//if (java.lang.Math.abs(oldMapPos.x - mapPos.x) > 1 || java.lang.Math.abs(oldMapPos.y - mapPos.y) > 1) {
			if (mmp.mapImage != null) 	mmp.mapImage.move(mapPos.x,mapPos.y);
			updateSymbolPositions();
			if (updateOverlay && TrackOverlays != null) updateOverlayPos();
		//}
		mmp.repaintNow(); // TODO test if the "if" above can be used
		//Vm.debug("update only position");			
	}
	/**
	* Method to laod the best map for lat/lon and move the map so that the posCircle is at lat/lon
	*/
	public void updatePosition(double lat, double lon){
		if(!ignoreGps){
			updateOnlyPosition(lat, lon, true);
			if (autoSelectMap) {
				Point mapPos = getMapXYPosition();
				if (forceMapLoad || (mmp.mapImage != null && ( mapPos.y > 0 || mapPos.x > 0 || mapPos.y+mmp.mapImage.getHeight()<this.height	|| mapPos.x+mmp.mapImage.getWidth()<this.width) 
					|| 	mmp.mapImage == null )) 	{
					forceMapLoad = false;
					//Vm.debug("Screen not completly covered by map");
					if (java.lang.Math.abs(lastCompareX-mapPos.x) > MyLocale.getScreenWidth()/10 || java.lang.Math.abs(lastCompareY-mapPos.y) > MyLocale.getScreenHeight()/10) {
						// more then 1/10 of screen moved since last time we tried to find a better map
						lastCompareX = mapPos.x;
						lastCompareY = mapPos.y;
//						Vm.debug("look for a bettermap");
						int newMapN=getBestMap(lat, lon); // this is independet of the Position of the PosCircle on the windows -> may be it would be better to call it with the coos of the center of the window?, nein, es könnte stören, wenn man manuell die Karte bewegt und er ständig ne neue läd... bleibt erstmal so
						MapInfoObject newmap ;
						newmap = (MapInfoObject) maps.get(newMapN);
						if (!(currentMap.mapName == newmap.mapName)) {
							setMap(newmap, lat, lon);
//							Vm.debug("better map found");
							// use new map
						}
					}
				}
			}
		}
	}

	public void setGpsStatus (int status) {
		if ((status == GpsStatus) || ignoreGpsStatutsChanges) return; // if ignoreGpsStatutsChanges == true than the Map is in manual-mode
		GpsStatus = status;
		ignoreGps = false;
		switch (status) {
		case noGPS: 	{ posCircle.change(null); ignoreGps = true; break; }
		case gotFix:    { posCircle.change(statusImageHaveSignal); break; }
		case lostFix:   { posCircle.change(statusImageNoSignal); break; }
		case noGPSData: { posCircle.change(statusImageNoGps); break; }
		}
		posCircle.refreshNow();
	}
	
	public void SnapToGps() {
		resetCenterOfMap();
		ignoreGps = false;
		ignoreGpsStatutsChanges = false;
		lastCompareX = Integer.MAX_VALUE; // neccessary to make updateposition to test if the current map is the best one for the GPS-Position
		lastCompareY = Integer.MAX_VALUE;
		autoSelectMap = true;
		forceMapLoad = true;
//		updatePosition(gotoPanel.gpsPosition.latDec, gotoPanel.gpsPosition.latDec); is called from GotoPanel.ticked
	}

	/** sets and displays the map
	 * 
	 * @param newmap
	 * @param lat move map so that lat/lon is in the center / -361: don't adust to lat/lon
	 * @param lon -361: don't adust to lat/lon
	 */
	public void setMap(MapInfoObject newmap, double lat, double lon) {
		if (newmap.mapName == currentMap.mapName) {
			updateOnlyPosition(lat, lon, true); 
			return;
		}
		Vm.showWait(true);
		boolean saveIgnoreStatus;
		saveIgnoreStatus = ignoreGps;
		ignoreGps = true;  // make updatePosition ignore calls during loading new map
		InfoBox inf;
		inf = new InfoBox("Info", "Loading map...");
		inf.show();
		try {
			this.currentMap = newmap; 
			this.title = currentMap.mapName;
//transXlat
			
			lastCompareX = Integer.MAX_VALUE; // neccessary to make updateposition to test if the current map is the best one for the GPS-Position
			lastCompareY = Integer.MAX_VALUE;
			if (! (mmp.mapImage == null) ) {
				//Vm.debug("free: "+Vm.getUsedMemory(false)+"classMemory: "+Vm.getClassMemory()+ "after garbage collection: "+Vm.getUsedMemory(false));
				mmp.removeImage(mmp.mapImage); mmp.mapImage.free(); mmp.mapImage = null;
				//Vm.debug("free: "+Vm.getUsedMemory(false)+"classMemory: "+Vm.getClassMemory()+ "after garbage collection: "+Vm.getUsedMemory(false));
				Vm.getUsedMemory(true); // calls the garbage collection
				} // give memory free before loading the new map to avoid out of memory error  
			if (currentMap.fileName.length()>0) mmp.mapImage = new AniImage(currentMap.fileName); // attention: when running in native java-vm, no exception will be thrown, not even OutOfMemeoryError
			else mmp.mapImage = new AniImage();
			mmp.mapImage.properties = mmp.mapImage.properties | AniImage.IsMoveable; 
			mmp.mapImage.move(0,0);
			mmp.addImage(mmp.mapImage);
			mmp.images.moveToBack(mmp.mapImage);
			addOverlaySet();
			updateOnlyPosition(lat, lon, false);
			inf.close(0);  // this doesn't work in a ticked-thread in the ewe-vm. That's why i made a new mThread in gotoPanel for ticked
			Vm.showWait(false);
			ignoreGps = saveIgnoreStatus;
		} catch (IllegalArgumentException e) { // thrown by new AniImage() in ewe-vm if file not found;
			if (mmp.mapImage != null) {
				mmp.removeImage(mmp.mapImage); 
				mmp.mapImage.free();
				mmp.mapImage = null;
			}
			addOverlaySet();
			updateOnlyPosition(lat, lon, false);
			inf.close(0);
			Vm.showWait(false);
			(new MessageBox("Error", "Could not load map: "+ newmap.fileName, MessageBox.OKB)).execute();
			ignoreGps = saveIgnoreStatus;
		} catch (OutOfMemoryError e) {
			if (mmp.mapImage != null) {
				mmp.removeImage(mmp.mapImage); 
				mmp.mapImage.free();
				mmp.mapImage = null;
			}
			addOverlaySet();
			updateOnlyPosition(lat, lon, false);
			inf.close(0);
			Vm.showWait(false);
			(new MessageBox("Error", "Not enough memory to load map: "+ newmap.fileName+"\nYou can try to close\n all prgrams and \nrestart CacheWolf", MessageBox.OKB)).execute();
			ignoreGps = saveIgnoreStatus;
		}catch (SystemResourceException e) {
			if (mmp.mapImage != null) {
				mmp.removeImage(mmp.mapImage); 
				mmp.mapImage.free();
				mmp.mapImage = null;
			}
			addOverlaySet();
			updateOnlyPosition(lat, lon, false);
			inf.close(0);
			Vm.showWait(false);
			(new MessageBox("Error", "Not enough ressources to load map: "+ newmap.fileName+"\nYou can try to close\n all prgrams and \nrestart CacheWolf", MessageBox.OKB)).execute();
			ignoreGps = saveIgnoreStatus;
		}
	}
	
	
	public void onEvent(Event ev){
		if(ev instanceof FormEvent && (ev.type == FormEvent.CLOSED )){
			gotoPanel.runMovingMap = false;
		}
		super.onEvent(ev);
	}
}

/**
*	Class to display the map bitmap and to select another bitmap to display.
*/
class MovingMapPanel extends InteractivePanel{
	MovingMap mm;
	AniImage mapImage;
	Point saveMapLoc = null;
	boolean saveGpsIgnoreStatus;
	public MovingMapPanel(MovingMap f){
		this.mm = f;
		//this.autoMoveToTop = false;
	}
	public boolean imageBeginDragged(AniImage which,Point pos) {
		if (! (which == mapImage || which instanceof TrackOverlay) ) return false;
		saveGpsIgnoreStatus = mm.ignoreGps; 
		mm.ignoreGps = true;
		saveMapLoc = mapImage.getLocation(null);
		return super.imageBeginDragged(mapImage, pos);
		}

	public boolean imageNotDragged(ImageDragContext dc,Point pos){
		boolean ret = super.imageNotDragged(dc, pos);
		mapMoved(mapImage.location.x - saveMapLoc.x, mapImage.location.y - saveMapLoc.y);
		mm.ignoreGps = saveGpsIgnoreStatus;
		return ret;
	 
	}
	public void moveMap(int diffX, int diffY) {
		Point p = new Point();
		if (mapImage!= null) {
			p = mapImage.getLocation(null);
			mapImage.move(p.x+diffX,p.y+diffY);
		}
		mapMoved(diffX, diffY);
	}
	
	public void mapMoved(int diffX, int diffY){
		Point p = mm.posCircle.getLocation(null);
		mm.posCircle.move(p.x+diffX, p.y+diffY);
		mm.posCircleX = mm.posCircleX+diffX;
		mm.posCircleY = mm.posCircleY+diffY;
		mm.updateSymbolPositions();
		mm.updateOverlayPos();
		this.repaintNow();
		
	}
	public void chooseMap() {
		CWPoint gpspos;
		if (mm.gotoPanel.gpsPosition.Fix > 0) gpspos = new CWPoint(mm.gotoPanel.gpsPosition.latDec, mm.gotoPanel.gpsPosition.lonDec);
		else gpspos = null;
		ListBox l = new ListBox(mm.maps, gpspos, mm.getGotoPos());
		if(l.execute() == FormBase.IDOK){
//			Vm.debug("Trying map: " + l.selectedMap.fileName);
			mm.autoSelectMap = false;
			if (l.selectedMap.inBound(mm.posCircleLat, mm.posCircleLon) || l.selectedMap.fileName.length()==0) {
				mm.setMap(l.selectedMap, mm.posCircleLat, mm.posCircleLon);
				mm.ignoreGpsStatutsChanges = false;
			} else {
				mm.ignoreGpsStatutsChanges = false;
				mm.setGpsStatus(MovingMap.noGPS);
				mm.ignoreGpsStatutsChanges = true;
				mm.setMap(l.selectedMap, -361, -361); // don't adjust Image to lat/lon
//				Point posCXY = new Point (0,0); mm.getXYinMap(mm.posCircleLat, mm.posCircleLat);
	//			double lat = mm.currentMap.affine[0]*posCXY.x + mm.currentMap.affine[2]*posCXY.y + mm.currentMap.affine[4]; 
				mm.posCircleX = 0; // place map to the upper left corner of windows
				mm.posCircleY = 0;
				mm.updateOnlyPosition(mm.currentMap.affine[4], mm.currentMap.affine[5], true);
			}
		}
	}
	
	/**
	*	Method to react to user.
	*/
	public void imageClicked(AniImage which, Point pos){
		if (which == mm.ButtonImageChooseMap){ chooseMap();	}
		if (which == mm.ButtonImageGpsOn) {
			if (mm.gotoPanel.serThread == null || !mm.gotoPanel.serThread.isAlive()) {
				mm.gotoPanel.startGps();
				mm.addTrack(mm.gotoPanel.currTrack); // use new track when gps now started
			} 
			mm.SnapToGps();
		}
		if (which == mm.arrowRight)	{	moveMap(-10,0);	}
		if (which == mm.arrowLeft)	{	moveMap(+10,0);	}
		if (which == mm.arrowDown)	{	moveMap(0,-10);	}
		if (which == mm.arrowUp)	{	moveMap(0,+10);	}
	}
}

/**
*	Class to display maps to choose from
*/
class ListBox extends Form{
	public MapInfoObject selectedMap = new MapInfoObject();
	mButton cancelButton, okButton;
	mList list = new mList(4,1,false);
	public boolean selected = false;
	Vector maps;
	
	public ListBox(Vector maps, CWPoint Gps, CWPoint gotopos){
		this.title = "Maps";
		// if (Gui.screenIs(Gui.PDA_SCREEN)) this.setPreferredSize(200,100); else 
		this.setPreferredSize(MyLocale.getScreenWidth()*3/4, MyLocale.getScreenHeight()*3/4);
		this.maps = maps;
		MapInfoObject map;
		ScrollBarPanel scb;
		boolean[] inList = new boolean[maps.size()];
		if (gotopos != null && Gps != null) {
			list.addItem("--- Karten von akt. Position und Ziel ---");
			for(int i = 0; i<maps.size();i++){
				map = new MapInfoObject();
				map = (MapInfoObject)maps.get(i);
				if( map.inBound(Gps.latDec, Gps.lonDec) && map.inBound(gotopos) ) 
					{
					list.addItem(i + ": " + map.mapName);
					inList[i] = true;
					} else inList[i] = false;
			}
		}
		if (Gps != null) {
			list.addItem("--- Karten der aktuellen Position ---");
			for(int i = 0; i<maps.size();i++){
				map = new MapInfoObject();
				map = (MapInfoObject)maps.get(i);
				if(map.inBound(Gps.latDec, Gps.lonDec) == true) 
					{
					list.addItem(i + ": " + map.mapName);
					inList[i] = true;
					}
			}
		}
		if (gotopos != null) {
			list.addItem("--- Karten des Ziels ---");
			for(int i = 0; i<maps.size();i++){
				map = new MapInfoObject();
				map = (MapInfoObject)maps.get(i);
				if(map.inBound(gotopos)) {
					list.addItem(i + ": " + map.mapName);
					inList[i] = true;
				}
			}
		}
		list.addItem("--- andere Karten ---");
		for(int i = 0; i<maps.size();i++){
			map = new MapInfoObject();
			map = (MapInfoObject)maps.get(i);
			if(!inList[i]) list.addItem(i + ": " + map.mapName);
		}
		
		this.addLast(scb = new ScrollBarPanel(list),CellConstants.STRETCH, CellConstants.FILL);
		cancelButton = new mButton("Cancel");
		cancelButton.setHotKey(0, KeyEvent.getCancelKey(true));
		this.addNext(cancelButton,CellConstants.STRETCH, CellConstants.FILL);
		okButton = new mButton("Select");
		okButton.setHotKey(0, KeyEvent.getActionKey(true));
		this.addLast(okButton,CellConstants.STRETCH, CellConstants.FILL);
		okButton.setHotKey(0, KeyEvent.getActionKey(true));
	}
	private boolean mapIsInList(int mapNr){ // it is not used  anymore could be deleted
		String testitem = new String();
		int testitemnr;
		for (int i=0; i<list.countListItems(); i++) {
			try { 
				testitem = ((MenuItem)list.items.get(i)).label;
				testitemnr = Convert.toInt(testitem.substring(0,testitem.indexOf(':')));
				if ( testitemnr == mapNr) return true;
			} catch (IndexOutOfBoundsException e) {} // happens on a seperator line because it doesn't contain ":"
			catch (NegativeArraySizeException e) {} // happens on a seperator line because it doesn't contain ":"
		}
		return false;
	}

	
	public int myExecute() {
		if (this.maps.size()==1) {
			//this.selectedMap = 1;
			this.selectedMap = (MapInfoObject) maps.get(0);
			return FormBase.IDOK;
		}
		return execute();
	}
	
	
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelButton){
				selectedMap = null;
				selected = false;
				this.close(FormBase.IDCANCEL);
			}
			if (ev.target == okButton){
				try {
					selectedMap = null;
					int mapNum = 0;
					String it = new String();
					it = list.getText();
					if (it != ""){
						it = it.substring(0,it.indexOf(':'));
						mapNum = Convert.toInt(it);
						//	Vm.debug("Kartennummer: " + mapNum);
						selectedMap = (MapInfoObject)maps.get(mapNum);
						selected = true;
						this.close(FormBase.IDOK);
					}
					else {
						selected = false;
						this.close(FormBase.IDCANCEL);
					}
				}catch (NegativeArraySizeException e) {} // happens in substring when a dividing line selected 
			}
		}
		super.onEvent(ev);
	}
}

class MapSymbol {
	String name;
	String filename;
	double lat, lon;
	AniImage pic;
	public MapSymbol(String namei, String filenamei, double lati, double loni) {
		name = namei;
		filename = filenamei;
		lat = lati;
		lon = loni;
	}
	public void loadImage(){
		pic = new AniImage(filename);
		pic.properties = AniImage.AlwaysOnTop;
	}
}

