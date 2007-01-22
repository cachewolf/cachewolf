package CacheWolf;

import ewe.ui.*;
import ewe.graphics.*;
import ewe.io.File;
import ewe.io.IOException;
import ewe.sys.*;
import ewe.filechooser.FileChooser;
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

	public MapSymbol gotoPos = null;
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
	MapInfoObject currentMap = null;
	String mapPath;

	MapImage mapImage1to1;
	ArrowsOnMap directionArrows = new ArrowsOnMap();
	AniImage statusImageHaveSignal = new AniImage("position_green.png");
	AniImage statusImageNoSignal = new AniImage("position_yellow.png");
	AniImage statusImageNoGps = new AniImage("position_red.png");
	AniImage bottonImageClose;
	AniImage bottonImageChooseMap = new AniImage("choose_map.gif"); 
	AniImage buttonImageGpsOn = new AniImage("snap2gps.gif");
	AniImage buttonImageLens = new AniImage("lupe.png");
	AniImage buttonImageLensActivated = new AniImage("lupe_activated.png");
	AniImage buttonImageZoom1to1 = new AniImage("zoom1to1.png");
	/*AniImage arrowUp = new AniImage("arrow_up.png");
	AniImage arrowDown = new AniImage("arrow_down.png");
	AniImage arrowLeft = new AniImage("arrow_left.png");
	AniImage arrowRight = new AniImage("arrow_right.png"); */
	MapImage posCircle = new MapImage("position_green.png");
	int posCircleX = 0, posCircleY = 0, lastCompareX = Integer.MAX_VALUE, lastCompareY = Integer.MAX_VALUE;
	double posCircleLat, posCircleLon;

	boolean ignoreGps = false;
	boolean ignoreGpsStatutsChanges = false;
	boolean autoSelectMap = true;
	boolean forceMapLoad = true; // only needed to force updateposition to try to load the best map again after OutOfMemoryError after an repeated click on snap-to-gps
	CWPoint lastUpatePosition = new CWPoint();
	boolean mapHidden = false;
	boolean noMapsAvailable;
	boolean zoomingMode = false;
	boolean mapsloaded = false;

	public MovingMap(Preferences pref, GotoPanel gP, Vector cacheDB){
		this.cacheDB = cacheDB;
		this.gotoPanel = gP;
		this.pref = pref;
		this.windowFlagsToSet = Window.FLAG_FULL_SCREEN;
		this.windowFlagsToClear = Window.FLAG_HAS_TITLE | Window.BDR_NOBORDER;
		this.hasTopBar = false;
		this.noBorder = true;
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		this.title = "Moving Map";
		this.backGround = Color.Black;
		this.mapPath = Global.getPref().getMapLoadPath()+"/";
		mmp = new MovingMapPanel(this);
		this.addLast(mmp);
		DrawnIcon closeX = new DrawnIcon(DrawnIcon.CROSS,15,15,new Color(0,0,0));
		bottonImageClose = new AniImage(new Image(closeX.getWidth(), closeX.getHeight()));
		Graphics tmp = new Graphics(bottonImageClose.image);
		tmp.setColor(255, 255, 255);
		tmp.fillRect(0, 0, closeX.getWidth(), closeX.getHeight());
		closeX.doDraw(tmp, 0);
		bottonImageClose.properties |= AniImage.AlwaysOnTop;
		bottonImageClose.setLocation(Global.getPref().myAppWidth - bottonImageClose.getWidth()- 5, 5);
		mmp.addImage(bottonImageClose);
		buttonImageGpsOn.setLocation(pref.myAppWidth - bottonImageChooseMap.getWidth()-5, bottonImageClose.getHeight() + 20);
		buttonImageGpsOn.properties = AniImage.AlwaysOnTop;
		mmp.addImage(buttonImageGpsOn);
		bottonImageChooseMap.setLocation(10,10);
		bottonImageChooseMap.properties = AniImage.AlwaysOnTop;
		mmp.addImage(bottonImageChooseMap);
		directionArrows.properties = AniImage.AlwaysOnTop;
		directionArrows.setLocation(Global.getPref().myAppWidth/2-directionArrows.getWidth()/2, 10);
		mmp.addImage(directionArrows);
		buttonImageLens.setLocation(Global.getPref().myAppWidth - buttonImageLens.getWidth()-10, Global.getPref().myAppHeight/2 - buttonImageLens.getHeight()/2 );
		buttonImageLens.properties = AniImage.AlwaysOnTop;
		buttonImageLensActivated.setLocation(Global.getPref().myAppWidth - buttonImageLens.getWidth()-10, Global.getPref().myAppHeight/2 - buttonImageLens.getHeight()/2 );
		buttonImageLensActivated.properties = AniImage.AlwaysOnTop;
		mmp.addImage(buttonImageLens);
		buttonImageZoom1to1.setLocation(Global.getPref().myAppWidth - buttonImageZoom1to1.getWidth()-10, Global.getPref().myAppHeight/2 - buttonImageLens.getHeight()/2 - buttonImageZoom1to1.getHeight() -10);
		buttonImageZoom1to1.properties = AniImage.AlwaysOnTop;
		mmp.addImage(buttonImageZoom1to1);
		/*		arrowUp.setLocation(pref.myAppWidth/2, 10);
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
		 */		
//		currentMap = new MapInfoObject();
		setGpsStatus(noGPS);
		posCircle.properties = AniImage.AlwaysOnTop;
		mmp.addImage(posCircle);
		mapsloaded = false;
		posCircleLat = -361;
		posCircleLon = -361; // make them invalid
		//loadMaps(Global.getPref().baseDir+"maps/standard/");
		MapImage.setScreenSize(pref.myAppWidth, pref.myAppHeight);
	}

	/**
	 * loads the list of maps
	 *
	 */
	public void loadMaps(String mapsPath, double lat){
		this.mapPath = mapsPath;
		Vm.showWait(true);
		resetCenterOfMap();
		InfoBox inf = new InfoBox("Info", "Loading list of maps...");
		inf.exec();
		maps = new Vector(); // forget already loaded maps
		//if (mmp.mapImage != null) 
		String dateien[];
		File files = new File(mapsPath);
		String rawFileName = new String();
		String[] dirstmp = files.list("*.wfl", File.LIST_ALWAYS_INCLUDE_DIRECTORIES | File.LIST_DIRECTORIES_ONLY);
		Vector dirs = new Vector(dirstmp);
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
					tempMIO.loadwfl(mapsPath+dirs.get(j)+"/", rawFileName);
					maps.add(tempMIO);
				}catch(IOException ex){ 
					if (f == null) (f=new MessageBox("Warning", "Ignoring error while \n reading calibration file \n"+ex.toString(), MessageBox.OKB)).exec();
				}catch(ArithmeticException ex){ // affine contain not allowed values 
					if (f == null) (f=new MessageBox("Warning", "Ignoring error while \n reading calibration file \n"+mapsPath+dirs.get(j)+"/" + rawFileName+".wfl \n"+ex.toString(), MessageBox.OKB)).exec();
				} 
			}
		}
		if (maps.isEmpty())
		{
			(new MessageBox(MyLocale.getMsg(327, "Information"), MyLocale.getMsg(326, "Es steht keine kalibrierte Karte zur Verfügung"), MessageBox.OKB)).execute();
			noMapsAvailable = true;
		} else noMapsAvailable = false;
		tempMIO = new MapInfoObject(1.0, lat);
		maps.add(tempMIO);
		tempMIO = new MapInfoObject(5.0, lat);
		maps.add(tempMIO);
		tempMIO = new MapInfoObject(50.0, lat);
		maps.add(tempMIO);
		tempMIO = new MapInfoObject(250.0, lat);
		maps.add(tempMIO);
		tempMIO = new MapInfoObject(1000.0, lat);
		maps.add(tempMIO);
		inf.close(0);
		Vm.showWait(false);
		this.mapsloaded = true;
	}

	public void forceMapLoad() {
		forceMapLoad = true;
		updatePosition(lastUpatePosition.latDec, lastUpatePosition.lonDec); // this sets forceMapLoad to false after loading a map
	}

	public final FormFrame myExec() {
		//addOverlaySet(); // neccessary to draw points which were added when the MovingMap was not running, so that these pixels are not stored in the not-immediately-drawing-work-around
		// doShowExec(null,null,true,Gui.NEW_WINDOW & ~Form.PageHigher);
		return exec();
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
		destroyOverlaySet();
		if (tracks == null) return; // no tracks
		addMissingOverlays();
	}

	public void destroyOverlaySet() {
		if (TrackOverlays != null) {
			for (int i=0; i< TrackOverlays.length; i++) {	destroyOverlay(i);	}
		}
		Vm.getUsedMemory(true); // call garbage collection
		System.gc();
	}


	public void addMissingOverlays() {
		if (currentMap == null || posCircleLat < -360) return;
		boolean saveGPSIgnoreStatus = ignoreGps; // avoid multi-threading problems
		ignoreGps = true;
		int ww = pref.myAppWidth;
		int wh = pref.myAppHeight;
		Point upperleftOf4 = new Point(posCircleX, posCircleY); //ScreenXY2LatLon(0, 0); // TrackOverlay[4] == center of Trackoverlays 
		upperleftOf4.x = upperleftOf4.x % ww;
		upperleftOf4.y = upperleftOf4.y % wh;
		int i;
		if (TrackOverlays == null) TrackOverlays = new TrackOverlay[9];
		for (int yi=0; yi<3; yi++) {
			for (int xi=0; xi<3; xi++) {
				i = yi*3+xi;
				if (TrackOverlays[i]==null) { 
					TrackOverlays[i]= new TrackOverlay(ScreenXY2LatLon(upperleftOf4.x+(xi-1)*ww, upperleftOf4.y+(yi-1)*wh), ww, wh, currentMap); 
					TrackOverlays[i].setLocation(ww+1, wh+1); // outside of the screen will hide it automatically it will get the correct position in upadteOverlayposition 
					TrackOverlays[i].tracks = this.tracks;
					TrackOverlays[i].paintTracks();
					mmp.addImage(TrackOverlays[i]);
				}
			}
		}
		updateOverlayOnlyPos();
		if (mmp.mapImage != null) mmp.images.moveToBack(mmp.mapImage);
		ignoreGps = saveGPSIgnoreStatus;
	}

	private void destroyOverlay(int ov) {
		if (TrackOverlays[ov] == null) return; 
		mmp.removeImage(TrackOverlays[ov]);
		TrackOverlays[ov].free();
		TrackOverlays[ov]=null;
	}
	public void rearangeOverlays() {
		if (TrackOverlays[1].isOnScreen()) { // oben raus
			destroyOverlay(6);
			destroyOverlay(7);
			destroyOverlay(8);
			mmp.removeImage(TrackOverlays[0]);
			mmp.removeImage(TrackOverlays[1]);
			mmp.removeImage(TrackOverlays[2]);
			TrackOverlays[6]=TrackOverlays[0];
			TrackOverlays[7]=TrackOverlays[1];
			TrackOverlays[8]=TrackOverlays[2];
			mmp.addImage(TrackOverlays[6]);
			mmp.addImage(TrackOverlays[7]);
			mmp.addImage(TrackOverlays[8]);
			TrackOverlays[0] = null;
			TrackOverlays[1] = null;
			TrackOverlays[2] = null;
			destroyOverlay(3);
			destroyOverlay(4);
			destroyOverlay(5);
		} else {
			if (TrackOverlays[3].isOnScreen()) { // links raus
				destroyOverlay(2);
				destroyOverlay(5);
				destroyOverlay(8);
				mmp.removeImage(TrackOverlays[0]);
				mmp.removeImage(TrackOverlays[3]);
				mmp.removeImage(TrackOverlays[6]);
				TrackOverlays[2]=TrackOverlays[0];
				TrackOverlays[5]=TrackOverlays[3];
				TrackOverlays[8]=TrackOverlays[6];
				mmp.addImage(TrackOverlays[2]);
				mmp.addImage(TrackOverlays[5]);
				mmp.addImage(TrackOverlays[8]);
				TrackOverlays[0] = null;
				TrackOverlays[3] = null;
				TrackOverlays[6] = null;
				destroyOverlay(1);
				destroyOverlay(4);
				destroyOverlay(7);
			} else {
				if (TrackOverlays[5].isOnScreen()) { // rechts raus
					destroyOverlay(0);
					destroyOverlay(3);
					destroyOverlay(6);
					mmp.removeImage(TrackOverlays[2]);
					mmp.removeImage(TrackOverlays[5]);
					mmp.removeImage(TrackOverlays[8]);
					TrackOverlays[0]=TrackOverlays[2];
					TrackOverlays[3]=TrackOverlays[5];
					TrackOverlays[6]=TrackOverlays[8];
					mmp.addImage(TrackOverlays[0]);
					mmp.addImage(TrackOverlays[3]);
					mmp.addImage(TrackOverlays[6]);
					TrackOverlays[2] = null;
					TrackOverlays[5] = null;
					TrackOverlays[8] = null;
					destroyOverlay(1);
					destroyOverlay(4);
					destroyOverlay(7);
				} else {
					if (TrackOverlays[7].isOnScreen()) { // unten raus
						destroyOverlay(0);
						destroyOverlay(1);
						destroyOverlay(2);
						mmp.removeImage(TrackOverlays[6]);
						mmp.removeImage(TrackOverlays[7]);
						mmp.removeImage(TrackOverlays[8]);
						TrackOverlays[0]=TrackOverlays[6];
						TrackOverlays[1]=TrackOverlays[7];
						TrackOverlays[2]=TrackOverlays[8];
						mmp.addImage(TrackOverlays[0]);
						mmp.addImage(TrackOverlays[1]);
						mmp.addImage(TrackOverlays[2]);
						TrackOverlays[6] = null;
						TrackOverlays[7] = null;
						TrackOverlays[8] = null;
						destroyOverlay(3);
						destroyOverlay(4);
						destroyOverlay(5);
					} else { // it is important to test for diagonal only if the other didn't match
						if (TrackOverlays[0].isOnScreen()) {  // links oben raus
							destroyOverlay(8);
							mmp.removeImage(TrackOverlays[0]);
							TrackOverlays[8]=TrackOverlays[0];
							mmp.addImage(TrackOverlays[8]);
							TrackOverlays[0] = null;
							destroyOverlay(1);
							destroyOverlay(2);
							destroyOverlay(3);
							destroyOverlay(4);
							destroyOverlay(5);
							destroyOverlay(6);
							destroyOverlay(7);
						} else {
							if (TrackOverlays[2].isOnScreen()) { // rechts oben raus
								destroyOverlay(6);
								mmp.removeImage(TrackOverlays[2]);
								TrackOverlays[6]=TrackOverlays[2];
								mmp.addImage(TrackOverlays[6]);
								TrackOverlays[2] = null;
								destroyOverlay(0);
								destroyOverlay(1);
								destroyOverlay(3);
								destroyOverlay(4);
								destroyOverlay(5);
								destroyOverlay(7);
								destroyOverlay(8);
							} else {
								if (TrackOverlays[6].isOnScreen()) { // links unten raus
									destroyOverlay(2);
									mmp.removeImage(TrackOverlays[6]);
									TrackOverlays[2]=TrackOverlays[6];
									mmp.addImage(TrackOverlays[2]);
									TrackOverlays[6] = null;
									destroyOverlay(0);
									destroyOverlay(1);
									destroyOverlay(3);
									destroyOverlay(4);
									destroyOverlay(5);
									destroyOverlay(7);
									destroyOverlay(8);
								} else {
									if (TrackOverlays[8].isOnScreen()) { // rechts unten raus
										destroyOverlay(0);
										mmp.removeImage(TrackOverlays[8]);
										TrackOverlays[0]=TrackOverlays[8];
										mmp.addImage(TrackOverlays[0]);
										TrackOverlays[8] = null;
										destroyOverlay(1);
										destroyOverlay(2);
										destroyOverlay(3);
										destroyOverlay(4);
										destroyOverlay(5);
										destroyOverlay(6);
										destroyOverlay(7);
									}else
										for (int i=0; i<TrackOverlays.length; i++) {destroyOverlay(i);} // this happens if a position jump occured
								}}}}}}} // close all IFs
		Vm.getUsedMemory(true); // call garbage collection
		System.gc();
		Vm.debug("Overlayrearanged"+TrackOverlays.toString());
	}

	public void ShowLastAddedPoint(Track tr) {
		if (TrackOverlays == null || tr == null) return;
		for (int i=0; i<TrackOverlays.length; i++){
			TrackOverlays[i].paintLastAddedPoint(tr);
		}
	}

	public void updateOverlayOnlyPos() {
		if (TrackOverlays == null || TrackOverlays[4] == null) return;
		//	Point upperleft = getMapXYPosition();
		Point posOnScreen;
		posOnScreen = getXYonScreen(TrackOverlays[4].topLeft.latDec, TrackOverlays[4].topLeft.lonDec);
		Dimension ws = mmp.getSize(null);
		int ww = ws.width;
		int wh = ws.height;
		//Vm.sleep(100); // this is necessary because the ewe vm ist not multi-threaded and the serial thread also needs time
		int num, x, y;
		for (int yi=0; yi<3; yi++) {
			for (int xi=0; xi<3; xi++) {
				num = yi*3+xi;
				x = posOnScreen.x+(xi-1)*ww;
				y = posOnScreen.y+(yi-1)*wh; 
				TrackOverlays[num].setLocation(x, y);
			}
		}
	}

	public void updateOverlayPos() {
		if (tracks == null || tracks.size() == 0) return;
		if (TrackOverlays == null || TrackOverlays[4] == null) addMissingOverlays();
		else {
			updateOverlayOnlyPos();
			if (TrackOverlays[0].locAlways.x > 0 || TrackOverlays[2].locAlways.x < 0
					|| TrackOverlays[0].locAlways.y > 0 || TrackOverlays[8].locAlways.y < 0) { // testForNeedToRearange
				rearangeOverlays();
				addMissingOverlays();
				// updateOverlayOnlyPos(); is called from addMissingOverlays 
			}
		}
	}

	/**
	 * find the best map for lat/lon in the list of maps
	 * currently the best map is the one, whose center is nearest to
	 * lat/lon
	 * @param lat
	 * @param lon
	 * @return
	 */
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
	 * move posCircle to the Center of the Screen
	 *
	 */
	public void resetCenterOfMap() {
		posCircleX = pref.myAppWidth/2; // maybe this could /should be repleced to windows size
		posCircleY = pref.myAppHeight/2;
		posCircle.properties &= ~AniImage.IsInvisible;
		posCircle.setLocation(posCircleX-posCircle.getWidth()/2, posCircleY-posCircle.getHeight()/2);
	}

	public void movePosCircleToLatLon(CWPoint p) {
		moveScreenXYtoLatLon(new Point(posCircleX, posCircleY), p);
	}

	public void setCenterOfScreen (CWPoint c) {
		moveScreenXYtoLatLon(new Point (this.width/2, this.height/2), c);
	}

	public void moveScreenXYtoLatLon(Point s, CWPoint c) {
		Point mappos = getMapPositionOnScreen();
		Point onscreenpos = getXYonScreen(c.latDec, c.lonDec);
		if (mmp != null && mmp.mapImage != null) mmp.mapImage.move(mappos.x - onscreenpos.x + s.x, mappos.y - onscreenpos.y + s.y);
		mapMoved(s.x - onscreenpos.x, s.y - onscreenpos.y);

	}

	public void mapMoved(int diffX, int diffY) {
		int w = posCircle.getWidth();
		int h = posCircle.getHeight();
		int npx = posCircleX-w/2+diffX; 
		int npy = posCircleY-h/2+diffY;
		posCircle.move(npx, npy);
		posCircleX = posCircleX+diffX;
		posCircleY = posCircleY+diffY;
		updateSymbolPositions();
		updateOverlayPos();
	}

	/**
	 * get upper left corner of map on window
	 * this is called when the map needs to be moved / the position of the map is wanted
	 * the map-position is calculated relativ to posCircle (x,y and lat/lon)
	 * returns the same as mmp.mapImage.getLocation(mapPos);
	 * but also works if mmp == null and is used to move the map to the correct point
	 * @return
	 */
	public Point getMapPositionOnScreen() {
		if (currentMap == null || posCircleLon < -360) return new Point(pref.myAppWidth +1, pref.myAppHeight +1); // in case no calculation is possible return somthing outside of the screen
		Point mapPos = new Point(); 
		//if (mmp.mapImage != null) mmp.mapImage.getLocation(mapPos);
		//else {
		Point mapposint = currentMap.calcMapXY(posCircleLat, posCircleLon);
		mapPos.x = posCircleX - mapposint.x;
		mapPos.y = posCircleY - mapposint.y;
		//}
		return mapPos;
	}

	/**
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public Point getXYonScreen(double lat, double lon){
		if (currentMap == null) return null;
		Point coords = currentMap.calcMapXY(lat, lon);
		Point mapPos = getMapPositionOnScreen();
		//		Vm.debug("getXYinMap, posCiLat: "+posCircleLat+"poscLOn: "+ posCircleLon+"gotoLat: "+ lat + "gotoLon: "+ lon+" mapPosX: "+mapPos.x+"mapposY"+mapPos.y);
		return new Point(coords.x + mapPos.x, coords.y + mapPos.y);
	}

	public CWPoint ScreenXY2LatLon (int x, int y){
		Point mapPos = getMapPositionOnScreen();
		return currentMap.calcLatLon(x - mapPos.x, y - mapPos.y);
	}


	public void updateSymbolPositions() {
		if (symbols == null) return;
		Point pOnScreen;
		MapSymbol symb;
		Dimension ws = mmp.getSize(null);
		int ww = ws.width;
		int wh = ws.height;
		int w, h;
		for (int i=symbols.size()-1; i>=0; i--) {
			symb = (MapSymbol)symbols.get(i);
			pOnScreen = getXYonScreen(symb.lat, symb.lon);
			w=symb.getWidth();
			h=symb.getHeight();
			if (pOnScreen.x+w >= 0 && pOnScreen.x <= ww && pOnScreen.y+h >= 0 &&  pOnScreen.y <= wh) 
			{
				symb.properties &= ~mImage.IsInvisible;
				symb.move(pOnScreen.x-w/2, pOnScreen.y-h/2);
			}
			else 
			{symb.properties |= mImage.IsInvisible;
			symb.move(30, 30);
			}
			//symb.pic.move(ww+1, wh+1);
		}
	}

	public MapSymbol addSymbol(String name, String filename, double lat, double lon) {
		if (symbols==null) symbols=new Vector();
		MapSymbol ms = new MapSymbol(name, filename, lat, lon);
		ms.loadImage();
		Point pOnScreen=getXYonScreen(lat, lon);
		ms.setLocation(pOnScreen.x-ms.getWidth()/2, pOnScreen.y-ms.getHeight()/2);
		symbols.add(ms);
		mmp.addImage(ms);
		return ms;
	}
	public void addSymbol(String name, Object mapObject, Image imSymb, double lat, double lon) {
		if (symbols==null) symbols=new Vector();
		MapSymbol ms = new MapSymbol(name, mapObject, imSymb, lat, lon);
		ms.properties = AniImage.AlwaysOnTop;
		Point pOnScreen=getXYonScreen(lat, lon);
		if (pOnScreen != null) ms.setLocation(pOnScreen.x-ms.getWidth()/2, pOnScreen.y-ms.getHeight()/2);
		symbols.add(ms);
		mmp.addImage(ms);
	}

	public void setGotoPosition(double lat, double lon) {
		removeGotoPosition();
		gotoPos=addSymbol("goto", "goto_map.png", lat, lon);
	}

	public void removeGotoPosition() {
		removeMapSymbol("goto");
	}

	public CWPoint getGotoPos(){
		if (gotoPos == null) return null;
		return new CWPoint(gotoPos.lat, gotoPos.lon);
	}

	public void removeAllMapSymbolsButGoto(){
		if (symbols == null) return;
		symbols.removeAllElements();
		if (gotoPos != null) symbols.add(gotoPos);
		/*			for (int i=symbols.size()-1; i>=0; i--) {
				if (((MapSymbol)symbols.get(i)).name != "goto") removeMapSymbol(i);
			}
		 */	}

	public void removeMapSymbol(String name) {
		int symbNr = findMapSymbol(name);
		if (symbNr != -1) removeMapSymbol(symbNr);
	}

	public void removeMapSymbol(int SymNr) {
		mmp.removeImage(((MapSymbol)symbols.get(SymNr)));
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
		Point oldMapPos = getMapPositionOnScreen();
		if (lat != -361.0 || lon != -361.0) {
			posCircleLat = lat;
			posCircleLon = lon;
			mapPos = getMapPositionOnScreen();
		}
		//Vm.debug("mapx = " + mapx);
		//Vm.debug("mapy = " + mapy);
		if (forceMapLoad || (java.lang.Math.abs(oldMapPos.x - mapPos.x) > 1 || java.lang.Math.abs(oldMapPos.y - mapPos.y) > 1)) {
			if (mmp.mapImage != null) 	mmp.mapImage.move(mapPos.x,mapPos.y);
			updateSymbolPositions();
			if (updateOverlay ) updateOverlayPos(); // && TrackOverlays != null
			//}
			mmp.repaintNow(); // TODO test if the "if" above can be used
		}
		//Vm.debug("update only position");			
	}
	/**
	 * Method to laod the best map for lat/lon and move the map so that the posCircle is at lat/lon
	 */
	public void updatePosition(double lat, double lon){
		if (!mapsloaded) {
			loadMaps(mapPath, lat);
			lastCompareX = Integer.MAX_VALUE;
			lastCompareY = Integer.MAX_VALUE;
			setBestMap(lat, lon);
			forceMapLoad = false;
			return;

		}
		lastUpatePosition.latDec=lat;
		lastUpatePosition.lonDec=lon;
		if(!ignoreGps || forceMapLoad){
			updateOnlyPosition(lat, lon, true);
			if (autoSelectMap || forceMapLoad) {
				Point mapPos = getMapPositionOnScreen();
				if (forceMapLoad || (mmp.mapImage != null && ( mapPos.y > 0 || mapPos.x > 0 || mapPos.y+mmp.mapImage.getHeight()<this.height	|| mapPos.x+mmp.mapImage.getWidth()<this.width) 
						|| 	mmp.mapImage == null )) 	{
					//Vm.debug("Screen not completly covered by map");
					if (forceMapLoad || (java.lang.Math.abs(lastCompareX-mapPos.x) > MyLocale.getScreenWidth()/10 || java.lang.Math.abs(lastCompareY-mapPos.y) > MyLocale.getScreenHeight()/10)) {
						// more then 1/10 of screen moved since last time we tried to find a better map
						lastCompareX = mapPos.x;
						lastCompareY = mapPos.y;
						setBestMap(lat, lon);
						forceMapLoad = false;
					}
				}
			}
		}
	}
	
	public void setBestMap(double lat, double lon) {
		int newMapN=getBestMap(lat, lon); // this is independet of the Position of the PosCircle on the windows -> may be it would be better to call it with the coos of the center of the window?, nein, es könnte stören, wenn man manuell die Karte bewegt und er ständig ne neue läd... bleibt erstmal so
		MapInfoObject newmap ;
		newmap = (MapInfoObject) maps.get(newMapN);
		if (currentMap == null || currentMap.mapName != newmap.mapName) {
			setMap(newmap, lat, lon);
			Vm.debug("better map found");
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
		showMap();
//		updatePosition(gotoPanel.gpsPosition.latDec, gotoPanel.gpsPosition.latDec); is called from GotoPanel.ticked
	}

	/** sets and displays the map
	 * 
	 * @param newmap
	 * @param lat move map so that lat/lon is in the center / -361: don't adust to lat/lon
	 * @param lon -361: don't adust to lat/lon
	 */
	public void setMap(MapInfoObject newmap, double lat, double lon) {
		if (currentMap != null && newmap.mapName == currentMap.mapName && !forceMapLoad) {
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
			lastCompareX = Integer.MAX_VALUE; // neccessary to make updateposition to test if the current map is the best one for the GPS-Position
			lastCompareY = Integer.MAX_VALUE;
			if (mmp.mapImage != null ) {
				//Vm.debug("free: "+Vm.getUsedMemory(false)+"classMemory: "+Vm.getClassMemory()+ "after garbage collection: "+Vm.getUsedMemory(false));
				mmp.removeImage(mmp.mapImage); mmp.mapImage.free(); mmp.mapImage = null; mapImage1to1 = mmp.mapImage;

				//Vm.debug("free: "+Vm.getUsedMemory(false)+"classMemory: "+Vm.getClassMemory()+ "after garbage collection: "+Vm.getUsedMemory(false));
				Vm.getUsedMemory(true); // calls the garbage collection
			} // give memory free before loading the new map to avoid out of memory error
			String ImageFilename = currentMap.getImageFilename(); 
			if (ImageFilename == null ) {
				mmp.mapImage = new MapImage();
				(new MessageBox("Error", "Could not find image associated with: \n"+currentMap.fileNameWFL, MessageBox.OKB)).execute();
			}
			else { 
				if (ImageFilename.length() > 0) mmp.mapImage = new MapImage(ImageFilename); // attention: when running in native java-vm, no exception will be thrown, not even OutOfMemeoryError
				else mmp.mapImage = new MapImage();
			}
			mapImage1to1 = mmp.mapImage;
			mmp.mapImage.properties = mmp.mapImage.properties | AniImage.IsMoveable;
			if (mapHidden) mmp.mapImage.properties |= AniImage.IsInvisible;
			mmp.mapImage.move(0,0);
			mmp.addImage(mmp.mapImage);
			mmp.images.moveToBack(mmp.mapImage);
			addOverlaySet();
			forceMapLoad = true; // forces updateOnlyPosition to redraw
			updateOnlyPosition(lat, lon, false);
			forceMapLoad = false;
			directionArrows.setMap(currentMap);
			inf.close(0);  // this doesn't work in a ticked-thread in the ewe-vm. That's why i made a new mThread in gotoPanel for ticked
			Vm.showWait(false);
			ignoreGps = saveIgnoreStatus;
		} catch (IllegalArgumentException e) { // thrown by new AniImage() in ewe-vm if file not found;
			if (mmp.mapImage != null) {
				mmp.removeImage(mmp.mapImage); 
				mmp.mapImage.free();
				mmp.mapImage = null; mapImage1to1 = mmp.mapImage;
			}
			addOverlaySet();
			updateOnlyPosition(lat, lon, false);
			inf.close(0);
			Vm.showWait(false);
			(new MessageBox("Error", "Could not load map: "+ newmap.getImageFilename(), MessageBox.OKB)).execute();
			ignoreGps = saveIgnoreStatus;
		} catch (OutOfMemoryError e) {
			if (mmp.mapImage != null) {
				mmp.removeImage(mmp.mapImage); 
				mmp.mapImage.free();
				mmp.mapImage = null; mapImage1to1 = mmp.mapImage;
			}
			addOverlaySet();
			updateOnlyPosition(lat, lon, false);
			inf.close(0);
			Vm.showWait(false);
			(new MessageBox("Error", "Not enough memory to load map: "+ newmap.getImageFilename()+"\nYou can try to close\n all prgrams and \nrestart CacheWolf", MessageBox.OKB)).execute();
			ignoreGps = saveIgnoreStatus;
		}catch (SystemResourceException e) {
			if (mmp.mapImage != null) {
				mmp.removeImage(mmp.mapImage); 
				mmp.mapImage.free();
				mmp.mapImage = null; mapImage1to1 = mmp.mapImage;
			}
			addOverlaySet();
			updateOnlyPosition(lat, lon, false);
			inf.close(0);
			Vm.showWait(false);
			(new MessageBox("Error", "Not enough ressources to load map: "+ newmap.getImageFilename()+"\nYou can try to close\n all prgrams and \nrestart CacheWolf", MessageBox.OKB)).execute();
			ignoreGps = saveIgnoreStatus;
		}
	}

	public void hideMap() {
		if (mmp != null && mmp.mapImage != null)
			mmp.mapImage.properties |= AniImage.IsInvisible;
		mapHidden = true;
		repaintNow();
	}

	public void showMap() {
		if (mmp != null && mmp.mapImage != null)
			mmp.mapImage.properties &= ~AniImage.IsInvisible;
		mapHidden = false;
		repaintNow();
	}

	public void setZoomingMode() {
		mmp.removeImage(buttonImageLens);
		mmp.addImage(buttonImageLensActivated);
		repaintNow();
		zoomingMode = true;
	}

	/**
	 * zommes in if w>0 and out if w<0
	 * @param firstclickpoint
	 * @param w
	 * @param h
	 */
	public void zoomScreenRect(Point firstclickpoint, int w, int h) {
		int newImageWidth = (int) (this.width *  (this.width  < 481 ? 2 : 1.6)); // (maximal) size of the zoomed image 
		int newImageHeight= (int) (this.height * (this.width < 481 ?  2 : 1.6)); // dont make this to big, otherwise it causes out of memory errors 
		CWPoint center = ScreenXY2LatLon(firstclickpoint.x + w/2, firstclickpoint.y + h/2);
		float zoomFactor;
		if (w > 0)  zoomFactor = (float)this.width / (float)w; // zoom in
		else {
			w = java.lang.Math.abs(w);
			firstclickpoint.x = firstclickpoint.x - w; // make firstclickedpoint the upper left corner
			zoomFactor = (float)w / (float)this.width;
		}
		if (h < 0) {
			h = java.lang.Math.abs(h);
			firstclickpoint.y = firstclickpoint.y - h;
		}
		// calculate rect in unzoomed image in a way that the center of the new image is the center of selected area but give priority to the prefered image size of the scaled image
		newImageHeight = (int) (newImageHeight / zoomFactor / currentMap.zoomFactor);
		newImageWidth = (int) (newImageWidth / zoomFactor / currentMap.zoomFactor);
		Point mappos = getMapPositionOnScreen();
		int xinunscaledimage = (int) ((firstclickpoint.x - mappos.x + w/2) / currentMap.zoomFactor + currentMap.shift.x - newImageWidth /2);
		int yinunscaledimage = (int) ((firstclickpoint.y - mappos.y + h/2) / currentMap.zoomFactor + currentMap.shift.y - newImageHeight /2);
		Rect newImageRect = new Rect(xinunscaledimage , yinunscaledimage, newImageWidth, newImageHeight);
		if (mapImage1to1 != null && mmp.mapImage != null && mapImage1to1.image != null)
		{
			// try to avoid overlapping by shifting
			if (newImageRect.x < 0) 
				newImageRect.x = 0; // align left if left overlapping
			if (newImageRect.y < 0) 
				newImageRect.y = 0;
			if (newImageRect.x + newImageRect.width >= mapImage1to1.getWidth()) 
				newImageRect.x = mapImage1to1.getWidth()- newImageWidth; // align right if right overlaping
			if (newImageRect.y + newImageRect.height >= mapImage1to1.getHeight()) 
				newImageRect.y = mapImage1to1.getHeight()- newImageHeight;
			// crop if after shifting still overlapping
			if (newImageRect.x < 0) 
				newImageRect.x = 0;
			if (newImageRect.y < 0) 
				newImageRect.y = 0;
			if (newImageRect.x + newImageRect.width >= mapImage1to1.getWidth()) 
				newImageRect.width = mapImage1to1.getWidth() - newImageRect.x;
			if (newImageRect.y + newImageRect.height >= mapImage1to1.getHeight()) 
				newImageRect.height= mapImage1to1.getHeight()- newImageRect.y;
		}
		zoomFromUnscaled(zoomFactor * currentMap.zoomFactor, newImageRect, center);
	}

	public void zoom1to1() {
		CWPoint center = ScreenXY2LatLon(this.width /2 , this.height/2);
		if (mapImage1to1 != null) zoomFromUnscaled(1, new Rect(0,0,mapImage1to1.getWidth(), mapImage1to1.getHeight()), center);
		else zoomFromUnscaled(1, new Rect(0,0, 1,1), center);
	}

	/**
	 * do the actual scaling
	 * @param zoomFactor relative to original image
	 * @param newImageRect Rect in the 1:1 image that contains the area to be zoomed into
	 * @param center
	 */		
	public void zoomFromUnscaled (float zoomFactor, Rect newImageRect, CWPoint center) {
		Vm.showWait(true);
		boolean savegpsstatus = ignoreGps;
		if (mapImage1to1 != null) {
			ignoreGps = true; // avoid multi-thread problems
			int saveprop = AniImage.IsMoveable;
			MapImage tmp = null; // = mmp.mapImage;
			if (mmp.mapImage != null) {
				tmp = mmp.mapImage;
				saveprop = mmp.mapImage.properties;
				mmp.removeImage(mmp.mapImage);
				if (mmp.mapImage != mapImage1to1) {
					mmp.mapImage.free();
					mmp.mapImage = null;
				} else tmp = mapImage1to1;
			}
			Vm.getUsedMemory(true);
			try {
				if (zoomFactor == 1) tmp = mapImage1to1;
				else tmp = new MapImage(mapImage1to1.scale((int) (newImageRect.width*zoomFactor), (int)(newImageRect.height*zoomFactor), newImageRect, 0));
				currentMap.zoom(zoomFactor, newImageRect.x, newImageRect.y);
			} catch (OutOfMemoryError e) {
				(new MessageBox("Error", "Out of memory error", MessageBox.OKB)).execute();
				//tmp = mapImage1to1;
			} //if (tmp != null) currentMap.zoom();}
			Vm.getUsedMemory(true);
			mmp.mapImage = tmp; // use unscaled or no image in case of OutOfMemoryError
			mmp.mapImage.properties = saveprop;
			mmp.addImage(mmp.mapImage);
			mmp.images.moveToBack(mmp.mapImage);
			if (mapImage1to1 != null && mmp.mapImage != null && mapImage1to1.image != null)
			{
				Point mappos = getMapPositionOnScreen();
				mmp.mapImage.move(mappos.x,mappos.y);
			}
		} else // no map image loaded 
		{ currentMap.zoom(zoomFactor, newImageRect.x, newImageRect.y); }

		destroyOverlaySet();
		Vm.getUsedMemory(true); // call garbage collection
		setCenterOfScreen(center);
		addOverlaySet();
		this.repaintNow();
		Vm.showWait(false);
		ignoreGps = savegpsstatus;
	}


	public void onEvent(Event ev){
		if(ev instanceof FormEvent && (ev.type == FormEvent.CLOSED )){
			gotoPanel.runMovingMap = false;
		}  
		if(ev instanceof KeyEvent && ev.target == this && ((KeyEvent)ev).key == IKeys.ESCAPE) {
			this.close(0);
			ev.consumed = true;
		}
		super.onEvent(ev);
	}
}

/**
 *	Class to display the map bitmap and to select another bitmap to display.
 */
class MovingMapPanel extends InteractivePanel implements EventListener {
	Menu mapsMenu;
	Menu kontextMenu;
	MenuItem gotoMenuItem;
	MenuItem openCacheDescMenuItem;
	CacheHolder clickedCache;
	MovingMap mm;
	MapImage mapImage;
	Point saveMapLoc = null;
	boolean saveGpsIgnoreStatus;
	boolean paintingZoomArea;
	ImageList saveImageList = null;
	int lastZoomWidth , lastZoomHeight;
	public MovingMapPanel(MovingMap f){
		this.mm = f;
		set(Control.WantHoldDown, true); // want to get simulated right-clicks
		gotoMenuItem = new MenuItem("Goto here", 0, null);

	}

	public boolean imageBeginDragged(AniImage which,Point pos) {
		if (mm.zoomingMode == true) {
			saveMapLoc = pos;
			mm.ignoreGps = true;
			return false;
		}
		if (!(which == null || which == mapImage || which instanceof TrackOverlay) ) return false;
		saveGpsIgnoreStatus = mm.ignoreGps; 
		mm.ignoreGps = true;
		saveMapLoc = pos;
		bringMapToTop();
		if (mapImage.isOnScreen()) return super.imageBeginDragged(mapImage, pos);
		else return super.imageBeginDragged(null, pos);
	}

	public boolean imageNotDragged(ImageDragContext dc,Point pos){
		boolean ret = super.imageNotDragged(dc, pos);
		bringMaptoBack();
		if (dc.image == null) moveMap(pos.x - saveMapLoc.x, pos.y - saveMapLoc.y);
		else mapMoved(pos.x - saveMapLoc.x, pos.y - saveMapLoc.y);
		mm.ignoreGps = saveGpsIgnoreStatus;
		this.repaintNow();
		return ret;
	}

	public void onPenEvent(PenEvent ev) {
		if (!mm.zoomingMode && ev.type == PenEvent.PEN_DOWN) {
			saveMapLoc = new Point (ev.x, ev.y);
		}
		if (mm.zoomingMode && ev.type == PenEvent.PEN_DOWN) {
			saveMapLoc = new Point (ev.x, ev.y);
			paintingZoomArea = true;
			mm.zoomingMode = true;
		}
		if (!mm.zoomingMode && ev.type == PenEvent.PEN_DOWN && ev.modifiers == PenEvent.RIGHT_BUTTON) {
			penHeld(new Point (ev.x, ev.y));
		}
		if (mm.zoomingMode && ev.type == PenEvent.PEN_UP ) {
			paintingZoomArea = false;
			mm.zoomingMode = false;
			removeImage(mm.buttonImageLensActivated);
			addImage(mm.buttonImageLens);
			if (java.lang.Math.abs(lastZoomWidth) < 15 || java.lang.Math.abs(lastZoomHeight) < 15)  {
				repaintNow();
				return; // dont make to big zoom jumps - it is most probable not an intentional zoom
			}
			mm.zoomScreenRect(saveMapLoc, lastZoomWidth, lastZoomHeight);
		}

		if (mm.zoomingMode && paintingZoomArea && (ev.type == PenEvent.PEN_MOVED_ON || ev.type == PenEvent.PEN_MOVE || ev.type == PenEvent.PEN_DRAG)) {
			int left, top;
			Graphics dr = this.getGraphics();
			if (lastZoomWidth < 0)left = saveMapLoc.x + lastZoomWidth;
			else left = saveMapLoc.x;
			if (lastZoomHeight < 0)top = saveMapLoc.y + lastZoomHeight;
			else top = saveMapLoc.y;
			this.repaintNow(dr, new Rect(left, top, java.lang.Math.abs(lastZoomWidth), java.lang.Math.abs(lastZoomHeight)));
			dr.setColor(Color.LightGreen);
			lastZoomWidth = ev.x - saveMapLoc.x;
			lastZoomHeight =  ev.y - saveMapLoc.y;
			if (lastZoomWidth < 0) left = saveMapLoc.x + lastZoomWidth;
			else left = saveMapLoc.x;
			if (lastZoomHeight < 0)top = saveMapLoc.y + lastZoomHeight;
			else top = saveMapLoc.y;
			dr.drawRect(left, top, java.lang.Math.abs(lastZoomWidth) , java.lang.Math.abs(lastZoomHeight), 2);
		}
		super.onPenEvent(ev);
	}

	private void bringMapToTop() {
		if (mapImage == null || (mapImage.properties & AniImage.IsInvisible) > 0 ) return;
		saveImageList = new ImageList();
		saveImageList.copyFrom(images);
		images.removeAllElements();
		//images.remove(mapImage);
		//mapImage.properties |= AniImage.AlwaysOnTop;
		images.add(mapImage);
	}
	private void bringMaptoBack() {
		//mapImage.properties &= ~AniImage.AlwaysOnTop;
		//images.moveToBack(mapImage);
		if (saveImageList == null) return;
		images=saveImageList;
		saveImageList = null;
	}

	public void moveMap(int diffX, int diffY) {
		Point p = new Point();
		if (mapImage!= null) {
			p = mapImage.locAlways;
			mapImage.move(p.x+diffX,p.y+diffY);
		}
		mapMoved(diffX, diffY);
	}

	public void mapMoved(int diffX, int diffY){
		mm.mapMoved(diffX, diffY);
		this.repaintNow();
	}

	public void doPaint(Graphics g,Rect area) {
		super.doPaint(g, area);
		if (mm.gotoPos != null) {
			Point dest = mm.getXYonScreen(mm.gotoPos.lat, mm.gotoPos.lon);
			g.setPen(new Pen(Color.MediumBlue, Pen.SOLID, 3));
			g.drawLine(mm.posCircleX, mm.posCircleY, dest.x, dest.y);
		}
	}

	public void chooseMap() {
		CWPoint gpspos;
		if (mm.gotoPanel.gpsPosition.Fix > 0) gpspos = new CWPoint(mm.gotoPanel.gpsPosition.latDec, mm.gotoPanel.gpsPosition.lonDec);
		else gpspos = null;
		ListBox l = new ListBox(mm.maps, gpspos, mm.getGotoPos());
		if(l.execute() == FormBase.IDOK){
//			Vm.debug("Trying map: " + l.selectedMap.fileName);
			mm.autoSelectMap = false;
			if (l.selectedMap.inBound(mm.posCircleLat, mm.posCircleLon) || l.selectedMap.getImageFilename().length()==0) {
				mm.setMap(l.selectedMap, mm.posCircleLat, mm.posCircleLon);
				mm.ignoreGpsStatutsChanges = false;
			} else {
				mm.ignoreGpsStatutsChanges = false;
				mm.setGpsStatus(MovingMap.noGPS);
				mm.ignoreGpsStatutsChanges = true;
				mm.setMap(l.selectedMap, mm.posCircleLat, mm.posCircleLon); // don't adjust Image to lat/lon
				mm.setCenterOfScreen(l.selectedMap.center);
				//Point posCXY = new Point (0,0); mm.getXYinMap(mm.posCircleLat, mm.posCircleLat);
				//double lat = mm.currentMap.affine[0]*posCXY.x + mm.currentMap.affine[2]*posCXY.y + mm.currentMap.affine[4]; 
				//mm.posCircleX = 0; // place map to the upper left corner of windows
				//mm.posCircleY = 0;
				//mm.updateOnlyPosition(mm.currentMap.affine[4], mm.currentMap.affine[5], true);
			}
		}
	}

	/**
	 *	Method to react to user.
	 */
	public void imageClicked(AniImage which, Point pos){
		if (which == mm.bottonImageChooseMap){
			mapsMenu = new Menu(new String[]{"Select a map manually$s", "Change map directory$c"}, "map choice");
			if (!mm.noMapsAvailable) 
			{
				if (mm.mapHidden) mapsMenu.addItem("show map");
				else mapsMenu.addItem("hide map");
			}
			//m.set(Menu., status)
			mapsMenu.exec(this, new Point(which.location.x, which.location.y), this);
		}
		if (which == mm.buttonImageGpsOn) {
			if (mm.gotoPanel.serThread == null || !mm.gotoPanel.serThread.isAlive()) {
				mm.gotoPanel.startGps();
				mm.addTrack(mm.gotoPanel.currTrack); // use new track when gps now started
			} 
			mm.SnapToGps();
		}
		if (which == mm.buttonImageLens) {
			mm.setZoomingMode();
		}
		if (which == mm.buttonImageZoom1to1) {
			mm.zoom1to1();
		}
		if (which == mm.bottonImageClose) {
			WindowEvent tmp = new WindowEvent();
			tmp.type = WindowEvent.CLOSE;
			mm.postEvent(tmp);
		}

		/*if (which == mm.arrowRight)	{	moveMap(-10,0);	}
		if (which == mm.arrowLeft)	{	moveMap(+10,0);	}
		if (which == mm.arrowDown)	{	moveMap(0,-10);	}
		if (which == mm.arrowUp)	{	moveMap(0,+10);	} */
	}

	public void penHeld(Point p){
		//	if (!menuIsActive()) doMenu(p);
		if (!mm.zoomingMode) // && ev instanceof PenEvent && (
			//( (ev.type == PenEvent.PEN_DOWN) && ((PenEvent)ev).modifiers == PenEvent.RIGHT_BUTTON)
		{ //|| ((ev.type == PenEvent.RIGHT_BUTTON) ) )){		
			kontextMenu = new Menu();
			kontextMenu.addItem(gotoMenuItem);
			AniImage clickedOnImage = images.findHotImage(p);
			if (clickedOnImage != null && clickedOnImage instanceof MapSymbol) {
				clickedCache = ((CacheHolder)((MapSymbol)clickedOnImage).mapObject);
				openCacheDescMenuItem = new MenuItem("Open "+clickedCache.CacheName);
				kontextMenu.addItem(openCacheDescMenuItem);
			}
			kontextMenu.exec(this, new Point(p.x, p.y), this);
		}
	}

	public void onEvent(Event ev){
		if (mapsMenu != null && ev instanceof PenEvent && ev.type == PenEvent.PEN_DOWN && ev.target == this) {mapsMenu.close(); mapsMenu = null;}
		if (kontextMenu != null && ev instanceof PenEvent && ev.type == PenEvent.PEN_DOWN && ev.target == this) {kontextMenu.close(); kontextMenu = null; }

		if (ev instanceof MenuEvent) { 
			if (ev.target == mapsMenu) {
				if (ev.type == MenuEvent.ABORTED || ev.type == MenuEvent.CANCELLED || ev.type == MenuEvent.FOCUS_OUT) mapsMenu.close(); // TODO menuIsActive() benutzen? 
				if (ev.type == MenuEvent.SELECTED ) {
					if (mapsMenu.getSelectedItem() != null) {
						if (mapsMenu.getSelectedItem().toString().equalsIgnoreCase("Select a map manually") )
						{ 
							mapsMenu.close();
							chooseMap();
						}
						if (mapsMenu.getSelectedItem().toString().equalsIgnoreCase("Change map directory") )
						{
							mapsMenu.close();
							FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, Global.getPref().baseDir+"/maps");
							fc.addMask("*.wfl");
							fc.setTitle((String)MyLocale.getMsg(4200,"Select map directory:"));
							if(fc.execute() != FileChooser.IDCANCEL){
								mm.loadMaps(fc.getChosen().toString()+"/", mm.posCircleLat);
								mm.forceMapLoad();
							}
						}
						//dont show map
						if (mapsMenu.getSelectedItem().toString().equalsIgnoreCase("hide map") )
						{
							mapsMenu.close();
							mm.hideMap();
						}
						// show map
						if (mapsMenu.getSelectedItem() != null && mapsMenu.getSelectedItem().toString().equalsIgnoreCase("show map") )
						{
							mapsMenu.close();
							mm.showMap();
						}
					}
				}
			} // if (ev.target == mapsMenu)
			if (ev.target == kontextMenu) {
				if ((((MenuEvent)ev).type==MenuEvent.SELECTED)) {
					if (kontextMenu.getSelectedItem() == gotoMenuItem) {
						kontextMenu.close();
						mm.gotoPanel.setDestination(mm.ScreenXY2LatLon(saveMapLoc.x, saveMapLoc.y));	
					}
					if (kontextMenu.getSelectedItem() == openCacheDescMenuItem) {
						//mm.onEvent(new FormEvent(FormEvent.CLOSED, mm));
						kontextMenu.close();
						WindowEvent close = new WindowEvent();
						close.target = mm;
						close.type = WindowEvent.CLOSE;
						mm.postEvent(close);
						mm.gotoPanel.mainT.tbP.selectAndActive(mm.cacheDB.find(clickedCache));
						mm.gotoPanel.mainT.select(mm.gotoPanel.mainT.descP);
						mm.gotoPanel.mainT.openDesciptionPanel(clickedCache);
					}
				}
			} // if (ev.target == kontextMenu)
		} // if (ev instanceof ControlEvent ) 
		super.onEvent(ev);
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

class MapSymbol extends AniImage {
	Object mapObject;
	String name;
	String filename;
	double lat, lon;
	public MapSymbol(String namei, String filenamei, double lati, double loni) {
		name = namei;
		filename = filenamei;
		lat = lati;
		lon = loni;
	}
	public MapSymbol(String namei, Object mapObjecti, Image fromIm, double lati, double loni) {
		name = namei;
		lat = lati;
		lon = loni;
		mapObject = mapObjecti;
		setImage(fromIm);
	}
	public void loadImage(){
		setImage(new Image(filename),0); freeSource();;
		//properties = AniImage.AlwaysOnTop;
	}
}

class ArrowsOnMap extends AniImage {
	float gotoDir = -361;
	float sunDir = -361;
	float moveDir = -361;

	int minY;
	Graphics draw;
	private MapInfoObject map=null;
	public boolean dirsChanged = true;

	final static Color RED = new Color(255,0,0);
	final static Color YELLOW = new Color(255,255,0);
	final static Color GREEN = new Color(0,255,0);
	final static Color BLUE = new Color(0,255,255);
	/**
	 * @param gd goto direction
	 * @param sd sun direction
	 * @param md moving direction
	 */
	public ArrowsOnMap(){
		super();
		newImage();
		//	setDirections(90, 180, -90);
	}

	public void newImage() {
		setImage(new Image(80,80), Color.White);
		draw = new Graphics(image);
	}
	public void setMap(MapInfoObject m) {
		map = m;
	}

	public void setDirections(float gd, float sd, float md ) {
		if (java.lang.Math.abs(gotoDir - gd) > 1 // to save cpu-usage only update if the is a change of directions of more than 1 degree
				|| java.lang.Math.abs(sunDir - sd) > 1
				|| java.lang.Math.abs(moveDir - md) > 1)
		{
			dirsChanged = true;
			gotoDir = gd;
			sunDir = sd;
			moveDir = md;
			refresh();
		}
	}

	/**
	 * draw arrows for the directions of movement and destination waypoint
	 * @param ctrl the control to paint on
	 * @param moveDir degrees of movement
	 * @param destDir degrees of destination waypoint
	 */

	public void doDraw(Graphics g,int options) {
		if (map == null) return;
		if (!dirsChanged) {
			g.drawImage(image,mask,transparentColor,0,-minY,location.width,location.height);
			return;
		}
		dirsChanged = false;
		//super.doDraw(g, options);
		draw.setColor(Color.White);
		draw.fillRect(0, 0, location.width, location.height);
		minY = Integer.MAX_VALUE;
		drawArrows(draw);
		draw.drawImage(image,mask,Color.DarkBlue,0,0,location.width,location.height); // this trick (note: wrong transparentColor) forces a redraw 
		g.drawImage(image,mask,transparentColor,0,-minY,location.width,location.height);
	}

	private void drawArrows(Graphics g){

		if (g != null) {
			// draw only valid arrows
			if (moveDir < 360 && moveDir > -360) drawArrow(g, moveDir, RED);
			if (gotoDir < 360 && gotoDir > -360) drawArrow(g, gotoDir, BLUE);
			if (sunDir < 360 && sunDir> -360) drawArrow(g, sunDir, YELLOW);
			drawArrow(g, 0, Color.DarkBlue); // north direction
		}
	}

	/**
	 * draw single arrow 
	 * @param g handle for drawing
	 * @param angle angle of arrow
	 * @param col color of arrow
	 */
	private void drawArrow(Graphics g, float angle, Color col) {
		float angleRad;
		int x, y, centerX = location.width/2, centerY = location.height/2;

		angleRad = angle * (float)java.lang.Math.PI / 180 + map.rotationRad;
		x = centerX + new Float(centerX * java.lang.Math.sin(angleRad)).intValue();
		y = centerY - new Float(centerY * java.lang.Math.cos(angleRad)).intValue();
		//	g.setPen(new Pen(Color.Black,Pen.SOLID,7));
		//	g.drawLine(centerX,centerY,x,y);
		g.setPen(new Pen(col,Pen.SOLID,3));
		g.drawLine(centerX,centerY,x,y);
		if (y < minY) minY = y;
		if (centerY < minY) minY = centerY;
	}
}

/** 
 * class that can be used with any x and any y
 * it will save taht location and make itself automatically
 * invisible if it is not on the screen. Call setscreensize to
 * set the screensize
 * @author r
 *
 */
class MapImage extends AniImage {
	public Point locAlways = new Point(); // contains the theoretical location even if it the location is out of the screen. If the image is on the screen, it contains the same as location
	static Dimension screenDim;
	
	public MapImage() {
		super();
	}
	
	public MapImage(String f) {
		super(f);
	}
	
	public MapImage(mImage im) {
		super(im);
	}
	public static void setScreenSize(int w, int h) {
		screenDim = new Dimension(w, h);
	}
	public void setLocation (int x, int y) {
		locAlways.x = x;
		locAlways.y = y;
		if (isOnScreen()) { 
			super.setLocation(x, y);
			properties &= ~AniImage.IsInvisible;
		} else {
			properties |= AniImage.IsInvisible;
			super.move(0, 0);
		}
	}
	
	public void move (int x, int y) {
		locAlways.x = x;
		locAlways.y = y;
		if (isOnScreen()) { 
			super.move(x, y);
			properties &= ~AniImage.IsInvisible;
		} else {
			properties |= AniImage.IsInvisible;
			super.move(0, 0);
		}
	}
		
	public boolean isOnScreen() { // i assume that location.width = screen.width and the same for hight
		if ( (locAlways.x + screenDim.width > 0 && locAlways.x < screenDim.width) && 
				(locAlways.y + screenDim.height > 0 && locAlways.y < screenDim.height) ) return true;
		else return false;
	}
}
	

