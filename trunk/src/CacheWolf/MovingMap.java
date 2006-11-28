package CacheWolf;

import ewe.ui.*;
import ewe.graphics.*;
import ewe.io.IOException;
import ewe.sys.*;
import ewe.sys.Double;
import ewe.database.RestoreException;
import ewe.fx.*;
import ewe.util.Vector;

/**
*	Class to handle a moving map.
*/
public class MovingMap extends Form {
	final static int gotFix = 4;
	final static int lostFix = 3;
	final static int noGPSData = 2;
	final static int noGPS = 1; // manually disconnected or GPS-Position not wanted
	
	public int GpsStatus;
	Preferences pref;
	MovingMapPanel mmp;
	//AniImage mapImage;
	Vector maps;
	GotoPanel gotoPanel;
	Vector cacheDB;
	MapInfoObject currentMap;
	AniImage statusImageHaveSignal = new AniImage("position_green.png");
	AniImage statusImageNoSignal = new AniImage("position_yellow.png");
	AniImage statusImageNoGps = new AniImage("position_red.png");
	
	AniImage ButtonImageChooseMap = new AniImage("center_blue.png"); // TODO make/use better icon
	AniImage ButtonImageGpsOn = new AniImage("center_blue.png"); // TODO make/use better icon
	AniImage arrowUp = new AniImage("arrow_up.png");
	AniImage arrowDown = new AniImage("arrow_down.png");
	AniImage arrowLeft = new AniImage("arrow_left.png");
	AniImage arrowRight = new AniImage("arrow_right.png");
	AniImage posCircle = new AniImage("position_green.png");
	int centerx = 0, centery = 0, lastCompareX = Integer.MAX_VALUE, lastCompareY = Integer.MAX_VALUE;
	
	boolean ignoreGps = false;
	
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
	
	public void loadMap(){
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
			centerx = pref.myAppWidth/2;
			centery = pref.myAppHeight/2;
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
					posCircle.setLocation(pref.myAppWidth/2-10,pref.myAppHeight/2-10);
					posCircle.properties = AniImage.AlwaysOnTop;
					int bestmap = getBestMap(gotoPanel.gpsPosition.latDec, gotoPanel.gpsPosition.lonDec);
					setMap((MapInfoObject)maps.get(bestmap), gotoPanel.gpsPosition.latDec, gotoPanel.gpsPosition.lonDec);
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

	/**
	* Method to calculate bitmap x,y of the current map using
	* lat and lon target coordinates
	*/
	public int[] calcMapXY(double lat, double lon){
		//x_ = affine[0]*x + affine[2]*y + affine[4]; lat
		//y_ = affine[1]*x + affine[3]*y + affine[5]; lon
		
		// Benutze Cramersche Regel: http://de.wikipedia.org/wiki/Cramersche_Regel
		Matrix matrix = new Matrix(2,2);
		double mapx,mapy;
		int coords[] = new int[2];
		double a[][] = new double[2][2];
		double b[] = new double[2];
		double a1[][] = new double[2][2];
		double a2[][] = new double[2][2];
		a[0][0] = currentMap.affine[0]; a[0][1] = currentMap.affine[2];
		a[1][0] = currentMap.affine[1]; a[1][1] = currentMap.affine[3];
		b[0] = lat - currentMap.affine[4];
		b[1] = lon - currentMap.affine[5];
		a1[0][0] = b[0]; a1[0][1] = a[0][1];
		a1[1][0] = b[1]; a1[1][1] = a[1][1];
		a2[0][0] = a[0][0]; a2[0][1] = b[0];
		a2[1][0] = a[1][0]; a2[1][1] = b[1];
		mapx = matrix.Determinant(a1)/matrix.Determinant(a);
		mapy = matrix.Determinant(a2)/matrix.Determinant(a);
		coords[0] = (int)mapx;
		coords[1] = (int)mapy;
		return coords;
	}
	
	/**
	* Method to reset the position of the moving map.
	*/
	public void updatePosition(double lat, double lon){
		if(!ignoreGps && lat != 0 && lon != 0 && currentMap != null && mmp.mapImage != null){
			int pos[] = new int[2];
			int posy,posx = 0;
			pos = calcMapXY(lat, lon);
			posy = centery - pos[1];
			posx = centerx - pos[0];
			//Vm.debug("mapx = " + mapx);
			//Vm.debug("mapy = " + mapy);
			mmp.mapImage.move(posx,posy);
			mmp.repaintNow();
			//Vm.debug("update position");			
			// if (! ignoreGPS) {...
			if (posy > 0 || posx > 0 || posy+mmp.mapImage.getHeight()<MyLocale.getScreenHeight() 
					|| posx+mmp.mapImage.getWidth()<MyLocale.getScreenWidth()) 	{
				//Vm.debug("Screen not completly covered by map");
				if (java.lang.Math.abs(lastCompareX-posx) > MyLocale.getScreenWidth()/10 || java.lang.Math.abs(lastCompareY-posy) > MyLocale.getScreenHeight()/10) {
					// more then 1/10 of screen moved since last time we tried to find a better map
					lastCompareX = posx;
					lastCompareY = posy;
//					Vm.debug("look for a bettermap");
					int newMapN=getBestMap(lat, lon);
					MapInfoObject newmap ;
					newmap = (MapInfoObject) maps.get(newMapN);
					if (!(currentMap.mapName == newmap.mapName)) {
						setMap(newmap, lat, lon);
//						Vm.debug("better map found");
						// use new map
					}
				}
			}
		}
	}

	public void setGpsStatus (int status) {
		if ((status == GpsStatus) || ignoreGps) return;
		GpsStatus = status;
		switch (status) {
		case noGPS: 	{ posCircle.change(null); ignoreGps = true; break; }
		case gotFix:    { posCircle.change(statusImageHaveSignal); break; }
		case lostFix:   { posCircle.change(statusImageNoSignal); break; }
		case noGPSData: { posCircle.change(statusImageNoGps); break; }
		}
		posCircle.refreshNow();
	}
	
	/** sets and displays the map
	 * 
	 * @param newmap
	 * @param lat move map so that lat/lon is in the center / -361: don't adust to lat/lon
	 * @param lon -361: don't adust to lat/lon
	 */
	public void setMap(MapInfoObject newmap, double lat, double lon) {
		Vm.showWait(true);
		boolean saveIgnoreStatus;
		saveIgnoreStatus = ignoreGps;
		ignoreGps = true;  // make updatePosition ignore calls during loading new map
		InfoBox inf;
		inf = new InfoBox("Info", "Loading map...");
		inf.show();
		try {
			if (! (mmp.mapImage == null) ) {mmp.removeImage(mmp.mapImage); mmp.mapImage.free(); } // give memory free before loading the new map to avoid out of memory error  
			this.currentMap = newmap; 
			mmp.mapImage = new AniImage(currentMap.fileName);
			this.title = currentMap.mapName;
			mmp.mapImage.setLocation(0,0);
			mmp.addImage(mmp.mapImage);
			int posy = 0, posx = 0;
			if (lat != -361.0 || lon != -361.0) {
				int pos[] = new int[2];
				pos = calcMapXY(lat, lon);
				posy = centery - pos[1];
				posx = centerx - pos[0];
			}
			mmp.mapImage.move(posx,posy);
			mmp.repaintNow();
			inf.close(0);  // this doesn't work in a ticked-thread in the ewe-vm. That's why i made a new mThread in gotoPanel for ticked
			Vm.showWait(false);
			ignoreGps = saveIgnoreStatus;
		} catch (IllegalArgumentException e) { // thrown by new AniImage() if file not found;
			inf.close(0);
			Vm.showWait(false);
			(new MessageBox("Eroor", "Could not load map: "+ newmap.fileName, MessageBox.OKB)).execute();
			ignoreGps = saveIgnoreStatus;
		} catch (OutOfMemoryError e) {
			inf.close(0);
			Vm.showWait(false);
			ignoreGps = saveIgnoreStatus;
			(new MessageBox("Eroor", "Not enough memory to load map: "+ newmap.fileName, MessageBox.OKB)).execute();
			ignoreGps = saveIgnoreStatus;
		}
	}
	
	public void onEvent(Event ev){
		if(ev instanceof FormEvent && (ev.type == FormEvent.CLOSED )){
			gotoPanel.runMovingMap = false;
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
	
	/**
	*	Method to react to user.
	*/
	public void imageClicked(AniImage which, Point pos){
		if(which == mm.ButtonImageChooseMap){
			ListBox l = new ListBox(maps, false, null);
			if(l.execute() == FormBase.IDOK){
					Vm.debug("Trying map: " + l.selectedMap.fileName);
					mm.setGpsStatus(MovingMap.noGPS);
					mm.setMap(l.selectedMap, -361, -361); // don't adjust Image to lat/lon 
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
		if (which == mm.ButtonImageGpsOn) {
			mm.ignoreGps = false;
			mm.lastCompareX = Integer.MAX_VALUE; // neccessary to make updateposition to test if the current map is the best one for the GPS-Position
			mm.lastCompareY = Integer.MAX_VALUE;
			
		}
		if(which == mm.arrowRight){
			Point p = new Point();
			p = mapImage.getLocation(null);
			mapImage.move(p.x-10,p.y);
			mm.setGpsStatus(MovingMap.noGPS);   // TODO mm.posCircle.move(, y)
			// for debugging: mm.updatePosition(10, 10);
			this.repaintNow();
		}
		if(which == mm.arrowLeft){
			Point p = new Point();
			p = mapImage.getLocation(null);
			mapImage.move(p.x+10,p.y);
			mm.setGpsStatus(MovingMap.noGPS);   // TODO mm.posCircle.move(, y)
			this.repaintNow();
		}
		if(which == mm.arrowDown){
			Point p = new Point();
			p = mapImage.getLocation(null);
			mapImage.move(p.x,p.y-10);
			mm.setGpsStatus(MovingMap.noGPS);   // TODO mm.posCircle.move(, y)
			this.repaintNow();
		}
		if(which == mm.arrowUp){
			Point p = new Point();
			p = mapImage.getLocation(null);
			mapImage.move(p.x,p.y+10);
			mm.setGpsStatus(MovingMap.noGPS);   // TODO mm.posCircle.move(, y)
			this.repaintNow();
		}
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
	
	public ListBox(Vector maps, boolean showInBoundOnly, CWGPSPoint position){
		this.title = "Maps";
		this.setPreferredSize(200,100);
		this.maps = maps;
		MapInfoObject map;
		ScrollBarPanel scb;
		for(int i = 0; i<maps.size();i++){
			map = new MapInfoObject();
			map = (MapInfoObject)maps.get(i);
			if(showInBoundOnly == true) {
				if(map.inBound(position) == true) list.addItem(i + ": " + map.mapName);
			} else list.addItem(i + ": " + map.mapName);
		}
		this.addLast(scb = new ScrollBarPanel(list),this.STRETCH, this.FILL);
		this.addNext(cancelButton = new mButton("Cancel"),this.STRETCH, this.FILL);
		this.addLast(okButton = new mButton("Select"),this.STRETCH, this.FILL);
		
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
				selectedMap = null;
				int i,mapNum = 0;
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
				
			}
		}
		super.onEvent(ev);
	}
}