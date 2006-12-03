package CacheWolf;

import ewe.ui.*;
import ewe.graphics.*;
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
	MapInfoObject currentMap;
	double transLatX, transLatY, transLonX, transLonY;
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
	
	public MovingMap(Preferences pref, Vector maps, GotoPanel gP, Vector cacheDB){
		this.cacheDB = cacheDB;
		gotoPanel = gP;
		this.maps = maps;
		this.pref = pref;
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		this.title = "Moving Map";
		currentMap = new MapInfoObject();
		mmp = new MovingMapPanel(this, maps, gotoPanel, cacheDB);
		this.addLast(mmp);
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
	
	
	/**
	 * Constructs the map panel and initializes everything that is neccessary
	 *
	 */
	public void loadMap(double lat, double lon){
		//Create index of all world files
		//Create form
//		if(gotoPanel.toPoint.latDec == 0 && gotoPanel.toPoint.latDec == 0 && maps.size()>0){
		try{
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
			resetCenterOfMap();
			posCircleLat = lat;
			posCircleLon = lon;
			// GPS has been switched on
			//This means we display the correct map if we have a fix
			//if(gotoPanel.displayTimer != 0){
			//Vm.debug("Und: " +gotoPanel.gpsPosition.latDec);
			if (!maps.isEmpty()){ // are calibrated maps available at all? - geht so nicht, muss erstmal überhaupt gefüllt werden die Liste
/*				ListBox l; 
				// Position? --> gibt es Karten? --> falls ja: auswählen, falls nein: GPS-Position ignoerieren
				// nein: alle Karten auswählen
				try { // was wenn nur 1 Karte existiert, die nicht im GPS-Position liegt?
					l = new ListBox(maps, gotoPanel.gpsPosition.latDec != 0, gotoPanel.gpsPosition);
				} catch (IndexOutOfBoundsException ex) { // wird von ListBox (darin maps.get) geworfen, wenn die Liste der Maps leer istif (l.execute()==FormBase.IDOK){
					ignoreGps = true;
					if (gotoPanel.gpsPosition.latDec != 0) {
						LocalResource lr = Vm.getLocale().getLocalResource("cachewolf.Languages",true);
						(new MessageBox((String)lr.get(321, "Information"), (String)lr.get(326, "Es steht keine kalibrierte Karte für die aktuelle GPS-Position zur Verfügung, bitte wählen Sie aus allen verfügbaren Karten eine zur Anzeige aus, GPS-Signal wird ignoriert"), MessageBox.OKB)).execute();
						l = new ListBox(maps, false, gotoPanel.gpsPosition);
					} else l = new ListBox(maps, false, gotoPanel.gpsPosition); // cannot happen but is neccessary to compile
				}
				if (l.myExecute()== FormBase.IDOK) {
					posCircle.setLocation(pref.myAppWidth/2-10,pref.myAppHeight/2-10);
					posCircle.properties = AniImage.AlwaysOnTop;
					mmp.addImage(posCircle);

					mapImage = new AniImage(l.selectedMap.fileName);
					this.title = l.selectedMap.mapName;
					this.currentMap = l.selectedMap;
					updatePosition(gotoPanel.gpsPosition.latDec, gotoPanel.gpsPosition.lonDec);
					mmp.addImage(mapImage);
					mmp.setMap(mapImage);
					this.repaintNow();
				}
*/				
				try {
					posCircle.properties = AniImage.AlwaysOnTop;
					int bestmap = getBestMap(posCircleLat, posCircleLon);
					setMap((MapInfoObject)maps.get(bestmap), posCircleLat, posCircleLon);
					mmp.addImage(posCircle);
					setGpsStatus(noGPS);
				} catch (IndexOutOfBoundsException ex) { // wird von maps.get geworfen, wenn die Liste der Maps leer ist, sollte eigentlich nicht vorkommen, solange bestmaps immer eine gültige Antwort liefert
					LocalResource lr = Vm.getLocale().getLocalResource("cachewolf.Languages",true);
					(new MessageBox((String)lr.get(321, "Error"), (String)lr.get(326, "Es steht keine kalibrierte Karte zur Verfügung"), MessageBox.OKB)).execute();
					throw new IndexOutOfBoundsException("no calibrated maps available"); 
				}
				
				/* else{ //Default: display the first map in the list.
					try {
						MapInfoObject mo = (MapInfoObject)maps.get(0);
						currentMap = mo;
						mapImage = new AniImage(mo.fileName);
						this.title = "Mov. Map: " + mo.mapName;
						mapImage.setLocation(0,0);
						mmp.addImage(mapImage);
						mmp.setMap(mapImage);
				 */					
				} else { // catch (IndexOutOfBoundsException ex) { // wird von maps.get geworfen, wenn die Liste der Maps leer ist
					 LocalResource lr = Vm.getLocale().getLocalResource("cachewolf.Languages",true);
					 (new MessageBox((String)lr.get(321, "Error"), (String)lr.get(326, "Es steht keine kalibrierte Karte zur Verfügung"), MessageBox.OKB)).execute();
					 throw new IndexOutOfBoundsException("no calibrated maps available"); 
				 }
		}catch (NumberFormatException ex){ // veraltet - hier vielleicht auch einen MemoryError behandlung hin?
			Vm.debug("Problem loading map image file!");
		}
		//	}
	}
	
	public void resetCenterOfMap() {
		posCircleX = pref.myAppWidth/2; // maybe this could /should be repleced to windows size
		posCircleY = pref.myAppHeight/2;
		posCircle.setLocation(posCircleX-posCircle.getWidth()/2, posCircleY-posCircle.getHeight()/2);
	}

	/**
	* Method to calculate bitmap x,y of the current map using
	* lat and lon target coordinates
	*/
	public int[] calcMapXY(double lat, double lon){
		int coords[] = new int[2];
		double b[] = new double[2];
		b[0] = lat - currentMap.affine[4];
		b[1] = lon - currentMap.affine[5];
		double mapx=transLatX* b[0] + transLonX*b[1];
		double mapy=transLatY* b[0] + transLonY*b[1];
		coords[0] = (int)mapx;
		coords[1] = (int)mapy;
		//Vm.debug("mapX=mapx2: "+mapx+"="+mapx2+"; mapy=mapy2: "+mapy+"="+mapy2);
		return coords;
	}
	/**
	 * get upper left corner of map on window
	 * returns the same as mmp.mapImage.getLocation(mapPos);
	 * but also works if mmp == null
	 * @return
	 */
	public Point getMapXYPosition() {
		Point mapPos = new Point(); 
		//if (mmp.mapImage != null) mmp.mapImage.getLocation(mapPos);
		//else {
			int [] mapposint = calcMapXY(posCircleLat, posCircleLon);
			mapPos.x = posCircleX - mapposint[0];
			mapPos.y = posCircleY - mapposint[1];
		//}
		return mapPos;
	}

	public Point getXYinMap(double lat, double lon){
		int coords[] = new int[2];
		Point mapPos = new Point();
		coords = calcMapXY(lat, lon);
		mapPos = getMapXYPosition();
	//		Vm.debug("getXYinMap, posCiLat: "+posCircleLat+"poscLOn: "+ posCircleLon+"gotoLat: "+ lat + "gotoLon: "+ lon+" mapPosX: "+mapPos.x+"mapposY"+mapPos.y);
		return new Point(coords[0] + mapPos.x, coords[1] + mapPos.y);
	}
	
	public void updateSymbolPositions() {
		if (symbols == null) return;
		Point pOnScreen;
		MapSymbol symb;
		for (int i=0; i<symbols.size(); i++) {
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
		this.mmp.addImage(ms.pic);
		symbols.add(ms);
//		repaintNow();
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
	public void updateOnlyPosition(double lat, double lon){
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
		//}
		mmp.repaintNow();
		//Vm.debug("update only position");			
	}
	/**
	* Method to laod the best map for lat/lon and move the map so that the posCircle is at lat/lon
	*/
	public void updatePosition(double lat, double lon){
		if(!ignoreGps){
			updateOnlyPosition(lat, lon);
			if (autoSelectMap) {
				Point mapPos = getMapXYPosition();
				if (mmp.mapImage != null && ( mapPos.y > 0 || mapPos.x > 0 || mapPos.y+mmp.mapImage.getHeight()<this.height	|| mapPos.x+mmp.mapImage.getWidth()<this.width) 
					|| 	mmp.mapImage == null ) 	{
					//Vm.debug("Screen not completly covered by map");
					if (java.lang.Math.abs(lastCompareX-mapPos.x) > MyLocale.getScreenWidth()/10 || java.lang.Math.abs(lastCompareY-mapPos.y) > MyLocale.getScreenHeight()/10) {
						// more then 1/10 of screen moved since last time we tried to find a better map
						lastCompareX = mapPos.x;
						lastCompareY = mapPos.y;
//						Vm.debug("look for a bettermap");
						int newMapN=getBestMap(lat, lon);
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
			updateOnlyPosition(lat, lon); 
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
			double nenner=(-currentMap.affine[1]*currentMap.affine[2]+currentMap.affine[0]*currentMap.affine[3]);
			transLatX = currentMap.affine[3]/nenner; // nenner == 0 cannot happen as long als affine is correct
			transLonX = -currentMap.affine[2]/nenner;
			transLatY = -currentMap.affine[1]/nenner;
			transLonY = currentMap.affine[0]/nenner;
			lastCompareX = Integer.MAX_VALUE; // neccessary to make updateposition to test if the current map is the best one for the GPS-Position
			lastCompareY = Integer.MAX_VALUE;
			if (! (mmp.mapImage == null) ) {
				Vm.debug("free: "+Vm.getUsedMemory(false)+"used: "+Vm.getClassMemory());
				mmp.removeImage(mmp.mapImage); mmp.mapImage.free(); mmp.mapImage = null;
				Vm.debug("free: "+Vm.getUsedMemory(false)+"used: "+Vm.getClassMemory());
				} // give memory free before loading the new map to avoid out of memory error  
			mmp.mapImage = new AniImage(currentMap.fileName); // attention: when running in native java-vm, no exception will be thrown, not even OutOfMemeoryError
			mmp.mapImage.setLocation(0,0);
			mmp.addImage(mmp.mapImage);
			updateOnlyPosition(lat, lon);
			inf.close(0);  // this doesn't work in a ticked-thread in the ewe-vm. That's why i made a new mThread in gotoPanel for ticked
			Vm.showWait(false);
			ignoreGps = saveIgnoreStatus;
		} catch (IllegalArgumentException e) { // thrown by new AniImage() in ewe-vm if file not found;
			if (mmp.mapImage != null) {
				mmp.removeImage(mmp.mapImage); 
				mmp.mapImage.free();
				mmp.mapImage = null;
			}
			updateOnlyPosition(lat, lon);
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
			inf.close(0);
			Vm.showWait(false);
			(new MessageBox("Error", "Not enough ressources to load map: "+ newmap.fileName+"\nYou can try to close\n all prgrams and \nrestart CacheWolf", MessageBox.OKB)).execute();
			ignoreGps = saveIgnoreStatus;
		}
	}
	
	
	public void onEvent(Event ev){
		if(ev instanceof FormEvent && (ev.type == FormEvent.CLOSED )){
			gotoPanel.runMovingMap = false;
			ignoreGps = true;
			//setGpsStatus(noGPS);
			//gotoPanel.stopTheTimer();
		}
		super.onEvent(ev);
	}
}

/**
*	Class to display the map bitmap and to select another bitmap to display.
*/
class MovingMapPanel extends InteractivePanel{
	MovingMap mm;
	Vector maps;
	CellPanel gotoPanel;
	AniImage mapImage;
	Vector cacheDB;
	public MovingMapPanel(MovingMap f, Vector maps, GotoPanel gP, Vector cacheDB){
		this.cacheDB = cacheDB;
		gotoPanel = gP;
		this.mm = f;
		this.maps = maps;
	}
	
	public void moveMap(int diffX, int diffY) {
		Point p = new Point();
		if (mapImage!= null) {
			p = mapImage.getLocation(null);
			mapImage.move(p.x+diffX,p.y+diffY);
		}
		p = mm.posCircle.getLocation(null);
		mm.posCircle.move(p.x+diffX, p.y+diffY);
		mm.posCircleX = mm.posCircleX+diffX;
		mm.posCircleY = mm.posCircleY+diffY;
		//mm.ignoreGpsStatutsChanges = false;
		//mm.setGpsStatus(MovingMap.noGPS);   // TODO mm.posCircle.move(, y)
		//mm.ignoreGpsStatutsChanges = true;
		// for debugging: mm.updatePosition(10, 10);
		mm.updateSymbolPositions();
		this.repaintNow();

	}

	public void chooseMap() {
		ListBox l = new ListBox(maps, false, mm.posCircleLat, mm.posCircleLon, mm.getGotoPos());
		if(l.execute() == FormBase.IDOK){
//			Vm.debug("Trying map: " + l.selectedMap.fileName);
			mm.autoSelectMap = false;
			if (l.selectedMap.inBound(mm.posCircleLat, mm.posCircleLon)) {
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
				mm.updateOnlyPosition(mm.currentMap.affine[4], mm.currentMap.affine[5]);
			}
			//Go through cache db to paint caches that are in bounds of the map
			/*
				CWPoint tempPoint;
				CacheHolder ch = new CacheHolder();
				Graphics g = new Graphics(mapImage);
				for(int i = 0; i < cacheDB.size();i++){
					ch = (CacheHolder)cacheDB.get(i);
					tempPoint = new CWPoint(ch.LatLon, CWPoint.CW);
					if(mm.currentMap.inBound(tempPoint) == true) { //yes cache is on map!

					}
				}
				g.free();
			 */
		}

	}
	
	/**
	*	Method to react to user.
	*/
	public void imageClicked(AniImage which, Point pos){
		if (which == mm.ButtonImageChooseMap){ chooseMap();	}
		if (which == mm.ButtonImageGpsOn) {
			mm.gotoPanel.startGps();
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
	
	public ListBox(Vector maps, boolean showInBoundOnly, double latGps, double lonGps, CWPoint gotopos){
		this.title = "Maps";
		// if (Gui.screenIs(Gui.PDA_SCREEN)) this.setPreferredSize(200,100); else 
		this.setPreferredSize(MyLocale.getScreenWidth()*3/4, MyLocale.getScreenHeight()*3/4);
		this.maps = maps;
		MapInfoObject map;
		ScrollBarPanel scb;
		if (gotopos != null) {
			list.addItem("--- Karten von akt. Position und Ziel ---");
			for(int i = 0; i<maps.size();i++){
				map = new MapInfoObject();
				map = (MapInfoObject)maps.get(i);
				if( map.inBound(latGps, lonGps) && map.inBound(gotopos) ) list.addItem(i + ": " + map.mapName);
			}
		}
		list.addItem("--- Karten der aktuellen Position ---");
		for(int i = 0; i<maps.size();i++){
			map = new MapInfoObject();
			map = (MapInfoObject)maps.get(i);
			if(map.inBound(latGps, lonGps) == true) list.addItem(i + ": " + map.mapName);
		}
		if (gotopos != null) {
			list.addItem("--- Karten des Ziels ---");
			for(int i = 0; i<maps.size();i++){
				map = new MapInfoObject();
				map = (MapInfoObject)maps.get(i);
				if(map.inBound(gotopos)) list.addItem(i + ": " + map.mapName);
			}
		}
		list.addItem("--- andere Karten ---");
		for(int i = 0; i<maps.size();i++){
			map = new MapInfoObject();
			map = (MapInfoObject)maps.get(i);
			if(!mapIsInList(i)) list.addItem(i + ": " + map.mapName);
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
	private boolean mapIsInList(int mapNr){
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
			this.selectedMap = new MapInfoObject();
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

