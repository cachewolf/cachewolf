package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.CacheHolder;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.MainTab;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.myTableModel;
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
	public final static int gotFix = 4; //green
	public final static int lostFix = 3; //yellow
	public final static int noGPSData = 2; // red
	public final static int noGPS = 1; // no GPS-Position marker, manually disconnected 
	public final static int ignoreGPS = -1; // ignore even changes in GPS-signal (eg. from lost fix to gotFix) this is wanted when the map is moved manually

	public MapSymbol gotoPos = null;
	public int GpsStatus;
	Preferences pref;
	MovingMapPanel mmp;
	MapsList maps;
	Vector symbols;
	Vector cacheDB;
	TrackOverlay[] TrackOverlays;
	CWPoint TrackOverlaySetCenterTopLeft;
	Vector tracks;
	MapInfoObject currentMap = null;
	//String mapPath;
	Navigate myNavigation;
	boolean running = false;

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
	AniImage buttonImageLensActivatedZoomIn = new AniImage("lupe_activated_zin.png");
	AniImage buttonImageLensActivatedZoomOut = new AniImage("lupe_activated_zout.png");
	AniImage buttonImageZoom1to1 = new AniImage("zoom1to1.png");
	AniImage DistanceImage;
	Graphics DistanceImageGraphics;
	AniImage ScaleImage;
	Graphics ScaleImageGraphics;
	MapSymbol posCircle = new MapSymbol("position_green.png", "gps-position", new CWPoint());
	public static final String MARK_CACHE_IMAGE = "mark_cache.png";
	int posCircleX = 0, posCircleY = 0, lastCompareX = Integer.MAX_VALUE, lastCompareY = Integer.MAX_VALUE;
	//double posCircleLat, posCircleLon;
	FontMetrics fm;

	boolean dontUpdatePos = false; // this is only internaly used to avoid multi-threading problems
	boolean ignoreGps = false; // ignores updateGps-calls if true
	boolean autoSelectMap = true;
	boolean forceMapLoad = true; // only needed to force updateposition to try to load the best map again after OutOfMemoryError after an repeated click on snap-to-gps
	boolean mapHidden = false;
	boolean noMapsAvailable;
	boolean zoomingMode = false;
	boolean mapsloaded = false;
	
	Point lastRepaintMapPos = null;
	double lastDistance = -1;

	public MovingMap(Navigate nav, Vector cacheDB){
		this.cacheDB = cacheDB;
		this.myNavigation = nav;
		this.pref = Global.getPref();
		if (pref.myAppHeight <= 640 && pref.myAppWidth <= 640)	this.windowFlagsToSet = Window.FLAG_FULL_SCREEN;
		this.windowFlagsToClear = Window.FLAG_HAS_TITLE | Window.BDR_NOBORDER;
		this.hasTopBar = false;
		this.noBorder = true;
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		this.title = "Moving Map";
		this.backGround = new Color(254,254,254); // background must not be black because black is interpreted as transparent and transparent images above (eg trackoverlay) want be drawn in windows-VM, so be care, don|t use white either
		//this.mapPath = Global.getPref().getMapLoadPath();

		mmp = new MovingMapPanel(this);
		this.addLast(mmp);
		DrawnIcon closeX = new DrawnIcon(DrawnIcon.CROSS,15,15,new Color(0,0,0));
		bottonImageClose = new AniImage(new Image(closeX.getWidth(), closeX.getHeight()));
		Graphics tmp = new Graphics(bottonImageClose.image);
		tmp.setColor(255, 255, 255);
		tmp.fillRect(0, 0, closeX.getWidth(), closeX.getHeight());
		closeX.doDraw(tmp, 0);
		bottonImageClose.properties |= AniImage.AlwaysOnTop;
		mmp.addImage(bottonImageClose);
		buttonImageGpsOn.properties = AniImage.AlwaysOnTop;
		mmp.addImage(buttonImageGpsOn);
		bottonImageChooseMap.properties = AniImage.AlwaysOnTop;
		mmp.addImage(bottonImageChooseMap);
		directionArrows.properties = AniImage.AlwaysOnTop;
		mmp.addImage(directionArrows);
		buttonImageLens.properties = AniImage.AlwaysOnTop;
		buttonImageLensActivated.properties = AniImage.AlwaysOnTop;
		buttonImageLensActivatedZoomIn.properties = AniImage.AlwaysOnTop;
		buttonImageLensActivatedZoomOut.properties = AniImage.AlwaysOnTop;
		mmp.addImage(buttonImageLens);
		buttonImageZoom1to1.properties = AniImage.AlwaysOnTop;
		mmp.addImage(buttonImageZoom1to1);
		//target distance
		Font font = new Font("Helvetica", Font.PLAIN, 13);
		fm = getFontMetrics(font);
		DistanceImage = new AniImage();
		DistanceImage.setImage(new Image(120, 15), Color.White); // consider the size of the font used
		DistanceImageGraphics = new Graphics(DistanceImage.image);
		DistanceImageGraphics.setFont(font);
		DistanceImage.properties = AniImage.AlwaysOnTop;
		mmp.addImage(DistanceImage);
		//scale
		ScaleImage = new AniImage();
		ScaleImage.setImage(new Image(120, 15), Color.White); // consider the size of the font used
		ScaleImageGraphics = new Graphics(ScaleImage.image);
		ScaleImageGraphics.setFont(font);
		ScaleImage.properties = AniImage.AlwaysOnTop;
		mmp.addImage(ScaleImage);
		//resizeTo(pref.myAppWidth, pref.myAppWidth); // is necessary to initialise mapImage.screenSize
		setGpsStatus(noGPS);
		posCircle.properties = AniImage.AlwaysOnTop;
		mmp.addImage(posCircle);
		mmp.startDragResolution = 5;
		mapsloaded = false;
		//updateDistance(); // fill Rect with transparent color
		scaleWanted = 1;
		mapChangeModus = HIGHEST_RESOLUTION_GPS_DEST;
		
		lastRepaintMapPos = new Point(pref.myAppWidth +1, pref.myAppHeight +1);
	}

	public void resizeTo(int w,int h) {
		super.resizeTo(w, h);
		updateFormSize(w, h);
	}

	public void updateFormSize(int w, int h) {
		MapImage.setScreenSize(w, h);
		bottonImageClose.setLocation(w- bottonImageClose.getWidth()- 5, 5);
		buttonImageGpsOn.setLocation(w- bottonImageChooseMap.getWidth()-5, bottonImageClose.getHeight() + 20);
		bottonImageChooseMap.setLocation(10,10);
		directionArrows.setLocation(w/2-directionArrows.getWidth()/2, 10);
		buttonImageZoom1to1.setLocation(w - buttonImageZoom1to1.getWidth()-10, h/2 - buttonImageLens.getHeight()/2 - buttonImageZoom1to1.getHeight() -10);
		buttonImageLens.setLocation(w - buttonImageLens.getWidth()-10, h/2 - buttonImageLens.getHeight()/2 );
		buttonImageLensActivated.setLocation(w - buttonImageLensActivated.getWidth()-10, h/2 - buttonImageLensActivated.getHeight()/2 );
		buttonImageLensActivatedZoomIn.setLocation(w - buttonImageLensActivatedZoomIn.getWidth()-10, h/2 - buttonImageLensActivatedZoomIn.getHeight()/2 );
		buttonImageLensActivatedZoomOut.setLocation(w - buttonImageLensActivatedZoomOut.getWidth()-10, h/2 - buttonImageLensActivatedZoomOut.getHeight()/2 );
		DistanceImage.setLocation(0, h - DistanceImage.getHeight());
		ScaleImage.setLocation(w - ScaleImage.getWidth(), h - ScaleImage.getHeight());
		if (mmp.mapImage != null) mmp.mapImage.screenDimChanged(); 
		if (posCircle != null) posCircle.screenDimChanged();
		if (tracks != null) rebuildOverlaySet();
		for (int i = symbols.size() -1; i >= 0; i-- ) {
			((MapSymbol)symbols.get(i)).screenDimChanged();
		}
	}

	boolean loadingMapList = false;
	/**
	 * loads the list of maps
	 * @param mapsPath must not have a trailing end "/"
	 * @param lat used to create empty maps with correct conversion from lon to meters the latitude must be known
	 */
	public void loadMaps(String mapsPath, double lat){
		if (loadingMapList) return;
		loadingMapList = true;
		//this.mapPath = mapsPath;
		Vm.showWait(this, true);
		resetCenterOfMap();
		InfoBox inf = new InfoBox("Info", "Loading list of maps...");
		inf.exec();
		inf.waitUntilPainted(100);
		boolean saveGpsIgnoreStatus = dontUpdatePos;
		dontUpdatePos = true;
		maps = new MapsList(mapsPath); // this actually loads the maps
		if (maps.isEmpty()) {
			(new MessageBox(MyLocale.getMsg(327, "Information"), MyLocale.getMsg(326, "Es steht keine kalibrierte Karte zur Verfügung \n Bitte wählen Sie einen Maßstab,\n in dem der Track und die markierten Caches angezeigt werden sollen"), MessageBox.OKB)).execute();
			(new MessageBox(MyLocale.getMsg(327, "Information"), MyLocale.getMsg(326, "Es steht keine kalibrierte Karte zur Verfügung \n Sie können eine herunter laden im Hauptmenü: Anwendung/Karten/kalibrierte herunterladen"), MessageBox.OKB)).execute();
			noMapsAvailable = true;
		} else noMapsAvailable = false;
		maps.addEmptyMaps(lat);
		dontUpdatePos = saveGpsIgnoreStatus;
		inf.close(0);
		Vm.showWait(this, false);
		this.mapsloaded = true;
		loadingMapList = false;
	}

	public void updateScale() {
		ScaleImageGraphics.setColor(ScaleImage.transparentColor);
		ScaleImageGraphics.fillRect(0, 0, ScaleImage.location.width,ScaleImage.location.height);
		
		if (currentMap != null)
		{
			float lineLengthMeters = 40 * currentMap.scale;
			float digits = (float)java.lang.Math.floor( java.lang.Math.log(lineLengthMeters) / java.lang.Math.log(10.0) );
			lineLengthMeters = (float)java.lang.Math.ceil( lineLengthMeters / (float)java.lang.Math.pow(10, digits) ) * (float)java.lang.Math.pow(10, digits);
			int lineLengthPixels = (int)java.lang.Math.round( lineLengthMeters / currentMap.scale );

			String lineLengthString;
			if (lineLengthMeters < 1000)
			{
				lineLengthString = Convert.toString((int) lineLengthMeters) + "m";
			}
			else
			{
				lineLengthString = Convert.toString((int) lineLengthMeters / 1000) + "km";
			}
			
			int backgroundStartX = ScaleImage.location.width - (lineLengthPixels + fm.getTextWidth(lineLengthString) + 7);
			
			ScaleImageGraphics.setColor(new Color(250,250,250));
			ScaleImageGraphics.fillRect(backgroundStartX, 0, ScaleImage.location.width - backgroundStartX ,ScaleImage.location.height);

			ScaleImageGraphics.setPen(new Pen(Color.DarkBlue,Pen.SOLID,3));
			ScaleImageGraphics.drawLine(backgroundStartX + 2, ScaleImage.location.height / 2, backgroundStartX+2+lineLengthPixels, ScaleImage.location.height / 2);
			ScaleImageGraphics.setColor(Color.DarkBlue);
			ScaleImageGraphics.drawText(lineLengthString , backgroundStartX + lineLengthPixels + 5, 0);
		}
		
		ScaleImageGraphics.drawImage(ScaleImage.image,null,Color.LightBlue,0,0,ScaleImage.location.width,ScaleImage.location.height); // changing the mask forces graphics to copy from image._awtImage to image.bufferedImage, which is displayed 
		ScaleImageGraphics.drawImage(ScaleImage.image,null,Color.White,0,0,ScaleImage.location.width,ScaleImage.location.height); // these 2 commands are necessary because of a bug or near to a bug in the ewe-vm
	}
	
	public void updateDistance(boolean repaint) {
		DistanceImageGraphics.setColor(DistanceImage.transparentColor);
		DistanceImageGraphics.fillRect(0, 0, DistanceImage.location.width,DistanceImage.location.height);
		if (gotoPos != null && posCircle.where.isValid())
		{
			double currentDistance = gotoPos.where.getDistance(posCircle.where);
			if (currentDistance != lastDistance)
			{
				lastDistance = currentDistance;
				ewe.sys.Double dd = new ewe.sys.Double();
				dd.set(currentDistance);
				String d;
				if (dd.value < 1) {
					dd.value = dd.value * 1000; 
					dd.decimalPlaces = 0;
					d = "Dist: " + dd.toString() + "m";
					int digits = (int)java.lang.Math.floor( java.lang.Math.log(dd.value) / java.lang.Math.log(10.0) );
					digits = java.lang.Math.max(0, digits);
				} 
				else {
					dd.decimalPlaces = 2;
					d = "Dist: " + dd.toString() + "km";
					int digits = (int)java.lang.Math.floor( java.lang.Math.log(dd.value) / java.lang.Math.log(10.0) );
					digits = java.lang.Math.max(0, digits);
				}
				
				int backgroundWidth = fm.getTextWidth(d) + 4;

				DistanceImageGraphics.setColor(new Color(250,250,250));
				DistanceImageGraphics.fillRect(0, 0, backgroundWidth ,DistanceImage.location.height);

				DistanceImageGraphics.setColor(Color.DarkBlue);
				DistanceImageGraphics.drawText(d, 2, 0);
				
				DistanceImageGraphics.drawImage(DistanceImage.image,null,Color.LightBlue,0,0,DistanceImage.location.width,DistanceImage.location.height); // changing the mask forces graphics to copy from image._awtImage to image.bufferedImage, which is displayed 
				DistanceImageGraphics.drawImage(DistanceImage.image,null,Color.White,0,0,DistanceImage.location.width,DistanceImage.location.height); // these 2 commands are necessary because of a bug or near to a bug in the ewe-vm
				if (repaint)
				{
					DistanceImage.refreshNow();
				}
			}
		}
		else
		{
			DistanceImageGraphics.drawImage(DistanceImage.image,null,Color.LightBlue,0,0,DistanceImage.location.width,DistanceImage.location.height); // changing the mask forces graphics to copy from image._awtImage to image.bufferedImage, which is displayed 
			DistanceImageGraphics.drawImage(DistanceImage.image,null,Color.White,0,0,DistanceImage.location.width,DistanceImage.location.height); // these 2 commands are necessary because of a bug or near to a bug in the ewe-vm
		}
	}

	public void forceMapLoad() {
		forceMapLoad = true;
		updatePosition(posCircle.where); // this sets forceMapLoad to false after loading a map
	}

	public final FormFrame myExec() {
		// update cache symbols in map
		running = true;
		MainTab mainT = Global.mainTab;
		if (Global.getProfile().selectionChanged) {
			Global.getProfile().selectionChanged = false;
			removeAllMapSymbolsButGoto();
			CacheHolder ch;
			for (int i=cacheDB.size()-1; i>=0; i--) {
				ch = (CacheHolder) cacheDB.get(i);
				if (ch.is_Checked && !ch.is_filtered && ch != mainT.ch) {
					int ct = Convert.parseInt(ch.type);
					if (ch.pos.isValid()) addSymbol(ch.CacheName, ch, myTableModel.cacheImages[ct], ch.pos);
				}
			}
		}
		setMarkedCache(mainT.ch);
		addTrack(myNavigation.curTrack);
		if (tracks != null && tracks.size() > 0 && ((Track)tracks.get(0)).num > 0) 
			rebuildOverlaySet(); // show points which where added when MavingMap was not running
		destChanged(myNavigation.destination);
		FormFrame ret = exec();
		return ret;
	}
	
	CacheHolder markedCache = null;
	public void setMarkedCache(CacheHolder ch) {
		if (ch == markedCache) return;
		if (markedCache != null) {
			removeMapSymbol("selectedCache");
			if (!markedCache.is_Checked) removeMapSymbol(markedCache);
		}
		if (ch != null) {
			addSymbol("selectedCache", MARK_CACHE_IMAGE, ch.pos);
			int ct = Convert.parseInt(ch.type);
			addSymbolIfNecessary(ch.CacheName, ch, myTableModel.cacheImages[ct], ch.pos);
		}
		markedCache = ch;
	}
	
	public void addTrack(Track tr) {
		if (tr == null) return;
		if (tracks == null) tracks = new Vector();
		if (tracks.find(tr) >= 0 ) return; // track already in list
		tracks.add(tr);
		rebuildOverlaySet();
	}

	public void addTracks(Track[] trs) {
		if (trs==null || trs.length == 0) return;
		for (int i=0; i<trs.length; i++) {
			addTrack(trs[i]);
		}
		rebuildOverlaySet();
	}

	/**
	 * adds an 3x3 set of overlays to the map-window which contain the track
	 * 
	 * add tracks with addtrack(track) before
	 */

	public void addOverlaySet() {
		if (tracks == null) return; // no tracks
		try {
			TrackOverlaySetCenterTopLeft = ScreenXY2LatLon(100, 100);
			addMissingOverlays();
		} catch (NullPointerException e) {} // hapens if currentmap == null or PosCircle not valid
		catch (IllegalArgumentException e) {} // happens if screensize is still not known    ---> in both cases creation of Overlayset will be done in updateOverlayPos if tracks != null 
	}

	public void destroyOverlaySet() {
		if (TrackOverlays != null) {
			for (int i=0; i< TrackOverlays.length; i++) {	destroyOverlay(i);	}
		}
		Vm.getUsedMemory(true); // call garbage collection
		Vm.gc();
	}

	public void rebuildOverlaySet() {
		destroyOverlaySet();
		addOverlaySet();
	}

	public void addMissingOverlays() {
		if (currentMap == null || (!posCircle.where.isValid()) || width == 0 || height == 0) return; // height == 0 happens if this is called before the form ist displayed on the screen
		if (TrackOverlays == null) {
			TrackOverlays = new TrackOverlay[9];
			TrackOverlaySetCenterTopLeft = ScreenXY2LatLon(100, 100);
		}
		boolean saveGPSIgnoreStatus = dontUpdatePos; // avoid multi-threading problems
		dontUpdatePos = true;
		Point upperleftOf4 = getXYonScreen(TrackOverlaySetCenterTopLeft); // TrackOverlay[4] == center of Trackoverlays 
		//upperleftOf4.x = (upperleftOf4.x + 1* width) % (width * 2) - 1 * width;
		//upperleftOf4.y = (upperleftOf4.y + 1* height) % (height * 2) - 1 * height;
		int i;
		for (int yi=0; yi<3; yi++) {
			for (int xi=0; xi<3; xi++) {
				i = yi*3+xi;
				if (TrackOverlays[i]==null) { 
					TrackOverlays[i]= new TrackOverlay(ScreenXY2LatLon(upperleftOf4.x+(xi-1)*width, upperleftOf4.y+(yi-1)*height), width, height, currentMap); 
					TrackOverlays[i].setLocation(width+1, height+1); // outside of the screen will hide it automatically it will get the correct position in upadteOverlayposition 
					TrackOverlays[i].tracks = this.tracks;
					TrackOverlays[i].paintTracks();
					mmp.addImage(TrackOverlays[i]);
				}
			}
		}
		updateOverlayOnlyPos();
		if (mmp.mapImage != null) mmp.images.moveToBack(mmp.mapImage);
		dontUpdatePos = saveGPSIgnoreStatus;
	}

	private void destroyOverlay(int ov) {
		if (TrackOverlays[ov] == null) return; 
		mmp.removeImage(TrackOverlays[ov]);
		TrackOverlays[ov].free();
		TrackOverlays[ov]=null;
	}
	public void rearangeOverlays() {
		Point oldp = getXYonScreen(TrackOverlaySetCenterTopLeft);
		if (TrackOverlays[1].isOnScreen()) { // oben raus
			TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x, oldp.y - 2* height));
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
				TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x - 2* width, oldp.y ));
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
					TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x + 2* width, oldp.y ));
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
						TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x, oldp.y + 2* height));
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
							TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x - 2* width, oldp.y - 2* height));
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
								TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x + 2* width, oldp.y - 2* height));
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
									TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x - 2* width, oldp.y + 2* height));
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
										TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x + 2* width, oldp.y + 2* height));
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
										for (int i=0; i<TrackOverlays.length; i++) {
											destroyOverlay(i);
											TrackOverlaySetCenterTopLeft = ScreenXY2LatLon(100, 100);
										} // this happens if a position jump occured
								}}}}}}} // close all IFs
		Vm.gc(); // call garbage collection
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
		posOnScreen = getXYonScreen(TrackOverlays[4].topLeft);
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
	 * move posCircle to the Centre of the Screen
	 *
	 */
	public void resetCenterOfMap() {
		if (width != 0) {
			posCircleX = width /2;
			posCircleY = height /2;
		} else {
			posCircleX = pref.myAppWidth/2; // maybe this could /should be repleced to windows size
			posCircleY = pref.myAppHeight/2;
		}
		posCircle.hidden = false;
		posCircle.move(posCircleX-posCircle.getWidth()/2, posCircleY-posCircle.getHeight()/2); // posCircle.setLocation caused a problem -> hiding the posCircle in some situation
	}

	public void movePosCircleToLatLon(CWPoint p, boolean repaint) {
		moveScreenXYtoLatLon(new Point(posCircleX, posCircleY), p, repaint);
	}

	public void setCenterOfScreen (CWPoint c, boolean repaint) {
		moveScreenXYtoLatLon(new Point (this.width/2, this.height/2), c, repaint);
	}

	public void moveScreenXYtoLatLon(Point s, CWPoint c, boolean repaint) {
		Point mappos = getMapPositionOnScreen();
		Point onscreenpos = getXYonScreen(c);
		if (mmp != null && mmp.mapImage != null) mmp.mapImage.move(mappos.x - onscreenpos.x + s.x, mappos.y - onscreenpos.y + s.y);
		mapMoved(s.x - onscreenpos.x, s.y - onscreenpos.y);
		if (repaint) mmp.repaintNow();
	}

	/** call this if the map moved on the screen (by dragging)
	 * this routine will adjust (move accordingly) all other symbols on the screen
	 * @param diffX
	 * @param diffY
	 */
	public void mapMoved(int diffX, int diffY) {
		int w = posCircle.getWidth();
		int h = posCircle.getHeight();
		int npx = posCircleX-w/2+diffX; 
		int npy = posCircleY-h/2+diffY;
		posCircle.move(npx, npy);
		posCircleX = posCircleX+diffX;
		posCircleY = posCircleY+diffY;
		if (posCircle.where.isValid()){
			dontUpdatePos = false;
			updatePosition(posCircle.where);
		}
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
		if (currentMap == null || !posCircle.where.isValid()) return new Point(pref.myAppWidth +1, pref.myAppHeight +1); // in case no calculation is possible return somthing outside of the screen
		Point mapPos = new Point(); 
		//if (mmp.mapImage != null) mmp.mapImage.getLocation(mapPos);
		//else {
		Point mapposint = currentMap.calcMapXY(posCircle.where);
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
	public Point getXYonScreen(TrackPoint ll){
		if (currentMap == null) return null;
		Point coords = currentMap.calcMapXY(ll);
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
		int w, h;
		for (int i=symbols.size()-1; i>=0; i--) {
			symb = (MapSymbol)symbols.get(i);
			pOnScreen = getXYonScreen(symb.where);
			w=symb.getWidth();
			h=symb.getHeight();
			symb.move(pOnScreen.x-w/2, pOnScreen.y-h/2);
		}
	}

	public MapSymbol addSymbol(String name, String filename, CWPoint where) {
		if (symbols==null) symbols=new Vector();
		MapSymbol ms = new MapSymbol(name, filename, where);
		ms.loadImage();
		ms.properties |= AniImage.AlwaysOnTop;
		Point pOnScreen = getXYonScreen(where);
		ms.setLocation(pOnScreen.x-ms.getWidth()/2, pOnScreen.y-ms.getHeight()/2);
		symbols.add(ms);
		mmp.addImage(ms);
		return ms;
	}
	
	public void addSymbolIfNecessary(String name, Object mapObject, Image imSymb, CWPoint where) {
		if (findMapSymbol(name) >= 0) return;
		else addSymbol(name, mapObject, imSymb, where);
		
	}
		
	public void addSymbol(String name, Object mapObject, Image imSymb, CWPoint ll) {
		if (symbols==null) symbols=new Vector();
		MapSymbol ms = new MapSymbol(name, mapObject, imSymb, ll);
		ms.properties = AniImage.AlwaysOnTop;
		Point pOnScreen = getXYonScreen(ll);
		if (pOnScreen != null) ms.setLocation(pOnScreen.x-ms.getWidth()/2, pOnScreen.y-ms.getHeight()/2);
		symbols.add(ms);
		mmp.addImage(ms);
	}

	public void destChanged(CWPoint d) {
		if(!running || (d == null && gotoPos == null) || 
				(d != null && gotoPos != null && gotoPos.where.equals(d))) return;
		removeGotoPosition();
		if (d == null || !d.isValid() ) return;
		gotoPos = addSymbol("goto", "goto_map.png", d);
		//updateDistance(); - this is called from updatePosition
		forceMapLoad = true;
		if (this.width != 0) updatePosition(posCircle.where); // dirty hack: if this.width == 0, then the symbols are not on the screen and get hidden by updateSymbolPositions
	}

	public void removeGotoPosition() {
		removeMapSymbol("goto");
	}

	public CWPoint getGotoPos(){
		if (gotoPos == null) return null;
		return new CWPoint(gotoPos.where);
	}

	public void removeAllMapSymbolsButGoto(){
		if (symbols == null) return;
		for (int i = symbols.size()-1; i >= 0; i--) {
			mmp.removeImage((MapSymbol)symbols.get(i));
		}
		symbols.removeAllElements();
		if (gotoPos != null) symbols.add(gotoPos);
	}

	public void removeMapSymbol(String name) {
		int symbNr = findMapSymbol(name);
		if (symbNr != -1) removeMapSymbol(symbNr);
	}

	public void removeMapSymbol(Object obj) {
		int symbNr = findMapSymbol(obj);
		if (symbNr != -1) removeMapSymbol(symbNr);
	}


	public void removeMapSymbol(int SymNr) {
		mmp.removeImage(((MapSymbol)symbols.get(SymNr)));
		symbols.removeElementAt(SymNr);
	}

	public int findMapSymbol(String name) {
		if (symbols == null) return -1;
		MapSymbol ms;
		for (int i = symbols.size() -1; i >= 0 ; i--) {
			ms= (MapSymbol)symbols.get(i);
			if (ms.name == name) return i;
		}
		return -1;
	}
	
	public int findMapSymbol(Object obj) {
		if (symbols == null) return -1;
		MapSymbol ms;
		for (int i = symbols.size() -1; i >= 0 ; i--) {
			ms= (MapSymbol)symbols.get(i);
			if (ms.mapObject == obj) return i;
		}
		return -1;
	}

	/**
	 * Move the map so that the posCircle is at lat/lon
	 * 
	 * @param  
	 */
	public void updateOnlyPosition(CWPoint where, boolean updateOverlay){
		//Point oldMapPos = getMapPositionOnScreen();
		posCircle.where.set(where);
		Point mapPos = getMapPositionOnScreen();
		//Vm.debug("mapx = " + mapx);
		//Vm.debug("mapy = " + mapy);
		if (forceMapLoad || (java.lang.Math.abs(lastRepaintMapPos.x - mapPos.x) > 1 || java.lang.Math.abs(lastRepaintMapPos.y - mapPos.y) > 1)) 
		{
			lastRepaintMapPos = mapPos;
			if (mmp.mapImage != null) 	mmp.mapImage.move(mapPos.x, mapPos.y);
			updateSymbolPositions();
			updateDistance(false);
			if (updateOverlay ) updateOverlayPos(); // && TrackOverlays != null
			mmp.repaintNow(); 
		}
		else
		{
			updateDistance(true);
		}
		//Vm.debug("update only position");			
	}
	/**
	 * Method to laod the best map for lat/lon and move the map so that the posCircle is at lat/lon
	 */
	public void updatePosition(CWPoint where){
		if (dontUpdatePos || loadingMapList) return; // avoid multi-threading problems
		Vm.debug("updatepos, lat: "+where.latDec+" lon: "+where.lonDec);
		if (!mapsloaded) {
			loadMaps(Global.getPref().getMapLoadPath(), where.latDec);
			lastCompareX = Integer.MAX_VALUE;
			lastCompareY = Integer.MAX_VALUE;
			autoSelectMap = true;
			setBestMap(where, true);
			forceMapLoad = false;
			return;
		}
		updateOnlyPosition(where, true);
		if (!autoSelectMap) return;
		Point mapPos = getMapPositionOnScreen();
		boolean screenNotCompletlyCovered =  mmp.mapImage == null || (mmp.mapImage != null && ( mapPos.y > 0 || mapPos.x > 0 || mapPos.y+mmp.mapImage.getHeight()<this.height	|| mapPos.x+mmp.mapImage.getWidth()<this.width));
		if (forceMapLoad || wantMapTest|| screenNotCompletlyCovered) 	{ // if force || want || map doesn't cover the scree completly
			//Vm.debug("Screen not completly covered by map");
			if (forceMapLoad || (java.lang.Math.abs(lastCompareX-mapPos.x) > this.width/10 || java.lang.Math.abs(lastCompareY-mapPos.y) > this.height/10)) {
				// more then 1/10 of screen moved since last time we tried to find a better map
				lastCompareX = mapPos.x;
				lastCompareY = mapPos.y;
				setBestMap(where, screenNotCompletlyCovered);
				forceMapLoad = false;
			}
		}
	}

	public void updateGps(int fix) {
		if (!running || ignoreGps) return;
		// runMovingMap neccessary in case of multi-threaded Java-VM: ticked could be called during load of mmp 
		if ((fix > 0) && (myNavigation.gpsPos.getSats()>= 0)) { // TODO is getSats really necessary?
			directionArrows.setDirections((float)myNavigation.gpsPos.getBearing(myNavigation.destination),
					(float)myNavigation.skyOrientationDir.lonDec, (float)myNavigation.gpsPos.getBear());
			setGpsStatus(MovingMap.gotFix);
			updatePosition(myNavigation.gpsPos);
			ShowLastAddedPoint(myNavigation.curTrack);
		}
		if (fix == 0 && myNavigation.gpsPos.getSats()== 0) 	setGpsStatus(MovingMap.lostFix);
		if (fix < 0 )	setGpsStatus(MovingMap.noGPSData);
	}
	
	public void gpsStarted() {
		addTrack(myNavigation.curTrack);
		ignoreGps = false;
	}
	public void gpsStoped() {
		setGpsStatus(MovingMap.noGPS);
	}

	int mapChangeModus = HIGHEST_RESOLUTION_GPS_DEST;
	float scaleWanted;
	boolean wantMapTest = true; // if true updateposition calls setBestMap regulary even if the currentmap covers the whole screen
	public final static int NORMAL_KEEP_RESOLUTION = 1; // keeps the choosen resolution as long as a map is available that overlaps with the screen and with the PosCircle - it changes the resolution if no such map is available. It wil cahnge back to the wanted scale as soon as a map becomes available (through movement of the GPS-receiver)
	public final static int HIGHEST_RESOLUTION = 2;
	public final static int HIGHEST_RESOLUTION_GPS_DEST = 3;
	boolean inBestMap = false; // to avoid multi-threading problems

	/**
	 * loads the best map for lat/lon according to mapChangeModus
	 * lat/lon will be at the screen-pos of posCircle
	 * when posCircle is not on the screen (shifted outside my the user)
	 * then this routine uses the centre of the screen to find the best map
	 * but anyway the map will be adjusted (moved) relativ to posCircle
	 * when a better map was found the called method updateposition will set
	 * posCirleLat/-Lon to lat/lon.
	 * 
	 * @param lat
	 * @param lon
	 * @param loadIfSameScale false: will not change the map if the better map has the same scale as the current - this is used not to change the map if it covers already the screen completely
	 * true: willchange the map, regardless of change in scale
	 */
	public void setBestMap(CWPoint where, boolean loadIfSameScale) {
		if (inBestMap) return;
		inBestMap = true;
		Object [] s = getRectForMapChange(where);
		CWPoint cll = (CWPoint) s[0]; 
		Rect screen = (Rect) s[1]; 
		MapInfoObject newmap = null;
		//if (mapChangeModus == 0) mapChangeModus = HIGHEST_RESOLUTION_GPS_DEST;
		wantMapTest = true;
		switch (mapChangeModus) {
		case NORMAL_KEEP_RESOLUTION: 
			newmap = maps.getBestMap(cll, screen, scaleWanted, false);
			if (newmap == null) newmap = currentMap;
			if (MapsList.scaleEquals(scaleWanted, newmap)) wantMapTest = false;
			break;
		case HIGHEST_RESOLUTION: newmap = maps.getBestMap(cll, screen, 0.000001f, false); break;
		case HIGHEST_RESOLUTION_GPS_DEST: 
			if (gotoPos!= null && GpsStatus != noGPS && posCircle.where.isValid()) {
				newmap = maps.getMapForArea(posCircle.where, gotoPos.where); // TODO use home-coos if no gps? - consider start from details panel and from gotopanel
				if (newmap == null) newmap = maps.getBestMap(cll, screen, 10000000000000000000000000000000000f, false); // use map with most available overview if no map containing PosCircle and GotoPos is available
			}
			//	either Goto-Pos or GPS-Pos not set
			else newmap = maps.getBestMap(cll, screen, 0.000001f, false); 
			break;
		default: (new MessageBox("Error", "Programmfehler: \nillegal mapChangeModus: " + mapChangeModus, MessageBox.OKB)).execute(); break;
		}
		if ( newmap != null && (currentMap == null || !currentMap.mapName.equals(newmap.mapName)) ) {
			if (loadIfSameScale || !MapsList.scaleEquals(currentMap.scale / currentMap.zoomFactor, newmap) ) {
				Vm.debug("better map found");
				setMap(newmap, where);
				moveScreenXYtoLatLon(new Point(screen.x, screen.y), cll, true);
			}
			inBestMap = false;
			return;
		}
		if (currentMap == null && newmap == null) {
			// (new MessageBox("Information", "Für die aktuelle Position steht keine Karte zur Verfüng, bitte wählen Sie eine manuell", MessageBox.OKB)).execute();
			posCircle.where.set(cll); // choosemap calls setmap with posCircle-coos
			try {
				setMap( ((MapListEntry)maps.elementAt(maps.getCount() - 4)).getMap(), where); // beware: "-4" only works if the empty maps were added last see MapsList.addEmptyMaps
			} catch (IOException e) { (new MessageBox("Error", "setBestMap: problem in: setMap( ((MapListEntry)maps.elementAt(maps.getCount() - 4)).getMap(), lat, lon) lat/lon:" + where.toString(), MessageBox.OKB)).exec(); }
			while (currentMap == null) {
				mmp.chooseMap(); // force the user to select a scale // TODO empty maps on top?
				if (currentMap == null) (new MessageBox("Error", "Moving map cannot run without a map - please select one. \n You can select an empty map", MessageBox.OKB)).execute();
			}
		}
		inBestMap = false;
	}

	public void setResModus (int modus) {
		scaleWanted = currentMap.scale;
		if (mapChangeModus == modus) return;
		mapChangeModus = modus;
		if (modus != NORMAL_KEEP_RESOLUTION) setBestMap(posCircle.where, true);
	}
	/**
	 * method to get a point on the screen which must be included in the map
	 * the map methods are looking for. If the poscircle is on the screen this will be 
	 * that point. If it is outside then the centre of the screen will be used.
	 * 
	 * returns [0] = CWPoint of that point, [1] Rect describing the screen around it 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public Object[] getRectForMapChange(CWPoint ll) {
		int w = (width != 0 ? width : pref.myAppWidth); // width == 0 happens if this routine is run before the windows is on the screen
		int h = (height != 0 ? height : pref.myAppHeight);
		int x, y;
		CWPoint cll;
		if (posCircleX >= 0 && posCircleX <= w && posCircleY >= 0 && posCircleY <= h && ll.isValid()) {
			x = posCircleX; // posCircle is inside the screen
			y = posCircleY; // TODO eigentlich interessiert, ob nach dem evtl. Kartenwechsel PosCircle on Screen ist. So wie es jetzt ist, kann 2mal der gleiche Aufruf zum laden unterschiedlicher Karten führen, wenn vorher PosCircle nicht auf dem SChirm war, nach dem ersten Laden aber schon.
			cll = new CWPoint(ll);
		} else { // when posCircle out of screen - use centre of screen as point which as to be included in the map
			cll = ScreenXY2LatLon(w/2, h/2);
			x = w/2;
			y = h/2;
		} 
		Object[] ret = new Object[2];
		ret[0] = cll;
		ret[1] = new Rect(x, y, w, h);
		return ret; 
	}

	/**
	 * 
	 * @param betterOverview true: getmap with better overview
	 * @return
	 */
	public void loadMoreDetailedMap(boolean betterOverview){
		Object [] s = getRectForMapChange(posCircle.where);
		CWPoint cll = (CWPoint) s[0]; 
		Rect screen = (Rect) s[1]; 
		//Rect screen = new Rect(posCircleX, posCircleY, (width != 0 ? width : pref.myAppWidth), (height != 0 ? height : pref.myAppHeight));
		MapInfoObject m = maps.getMapChangeResolution(cll, screen, currentMap.scale / currentMap.zoomFactor, !betterOverview);
		if (m != null) {
			boolean saveGpsIgnStatus = dontUpdatePos;
			dontUpdatePos = true;
			setMap(m, posCircle.where);
			setResModus(MovingMap.NORMAL_KEEP_RESOLUTION);
			dontUpdatePos = saveGpsIgnStatus;
		}
		else (new MessageBox("Error", "No "+ (betterOverview ? "less" : "more") + " detailed map available", MessageBox.OKB)).execute();
	}

	public void loadMapForAllCaches(){
		Area sur = Global.getProfile().getSourroundingArea(true);
		if (sur == null) {
			(new MessageBox("Error", "Keine  Caches mit Häckchen ausgewählt", MessageBox.OKB)).execute();
			return;
		}
		MapInfoObject newmap = maps.getMapForArea(sur.topleft, sur.buttomright);
		if (newmap == null ) { // no map that includs all caches is available -> load map with lowest resolution
			Object [] s = getRectForMapChange(posCircle.where);
			CWPoint cll = (CWPoint) s[0]; 
			Rect screen = (Rect) s[1]; 
			newmap = maps.getBestMap(cll, screen, Float.MAX_VALUE -1, false);
		}
		boolean saveGpsIgnStatus = dontUpdatePos;
		dontUpdatePos = true;
		setMap(newmap, posCircle.where);
		setResModus(MovingMap.NORMAL_KEEP_RESOLUTION);
		dontUpdatePos = saveGpsIgnStatus;
	}

	public void setGpsStatus (int status) {
		if (status == GpsStatus) return; // if ignoreGpsStatutsChanges == true than the Map is in manual-mode
		GpsStatus = status;
		dontUpdatePos = false;
		ignoreGps = false;
		switch (status) {
		case noGPS: 	{ posCircle.change(null); ignoreGps = true; break; }
		case gotFix:    { posCircle.change(statusImageHaveSignal); break; }
		case lostFix:   { posCircle.change(statusImageNoSignal); break; }
		case noGPSData: { posCircle.change(statusImageNoGps); break; }
		}
		mapMoved(0, 0); // positions the posCircle correctly accourding to its size (which can change when the image changes, e.g. from null to something else 
		posCircle.refreshNow();
	}

	public void SnapToGps() {
		resetCenterOfMap();
		dontUpdatePos = false;
		ignoreGps = false;
		lastCompareX = Integer.MAX_VALUE; // neccessary to make updateposition to test if the current map is the best one for the GPS-Position
		lastCompareY = Integer.MAX_VALUE;
		autoSelectMap = true;
		forceMapLoad = true;
		showMap();
		if (myNavigation.gpsPos.Fix <=0) updatePosition(posCircle.where);
		else updateGps(myNavigation.gpsPos.getFix());
	}

	/** sets and displays the map
	 * 
	 * @param newmap
	 * @param lat move map so that lat/lon is in the centre / -361: don't adust to lat/lon
	 * @param lon -361: don't adust to lat/lon
	 */
	public void setMap(MapInfoObject newmap, CWPoint where) {
		if (currentMap != null && newmap.mapName.equals(currentMap.mapName) && !forceMapLoad) { // note: newmap.mapName == currentMap.mapName won't work because they are different String containing the same text 
			updateOnlyPosition(where, true); 
			return;
		}
		Vm.showWait(true);
		boolean saveIgnoreStatus;
		saveIgnoreStatus = dontUpdatePos;
		dontUpdatePos = true;  // make updatePosition ignore calls during loading new map
		InfoBox inf;
		inf = new InfoBox("Info", "Loading map...");
		inf.show();
		inf.waitUntilPainted(100);
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
				maps.remove(currentMap);
				(new MessageBox("Error", "Could not find image associated with: \n"+currentMap.fileNameWFL, MessageBox.OKB)).execute();
			}
			else { 
				if (ImageFilename.length() > 0) mmp.mapImage = new MapImage(ImageFilename); // attention: when running in native java-vm, no exception will be thrown, not even OutOfMemeoryError
				else mmp.mapImage = new MapImage(); // no image associated with the calibration info ("empty map")
			}
			mapImage1to1 = mmp.mapImage;
			mmp.mapImage.properties = mmp.mapImage.properties | AniImage.IsMoveable;
			if (mapHidden) mmp.mapImage.hide();
			mmp.mapImage.move(0,0);
			mmp.addImage(mmp.mapImage);
			mmp.images.moveToBack(mmp.mapImage);
			rebuildOverlaySet();
			forceMapLoad = true; // forces updateOnlyPosition to redraw
			updateOnlyPosition(where, false);
			forceMapLoad = false;
			directionArrows.setMap(currentMap);
			updateScale();
			inf.close(0);  // this doesn't work in a ticked-thread in the ewe-vm. That's why i made a new mThread in gotoPanel for ticked
			Vm.showWait(false);
			dontUpdatePos = saveIgnoreStatus;
		} catch (IllegalArgumentException e) { // thrown by new AniImage() in ewe-vm if file not found;
			if (mmp.mapImage != null) {
				mmp.removeImage(mmp.mapImage); 
				mmp.mapImage.free();
				mmp.mapImage = null; mapImage1to1 = mmp.mapImage;
			}
			rebuildOverlaySet();
			updateOnlyPosition(where, false);
			inf.close(0);
			Vm.showWait(false);
			(new MessageBox("Error", "Could not load map: "+ newmap.getImageFilename(), MessageBox.OKB)).execute();
			dontUpdatePos = saveIgnoreStatus;
		} catch (OutOfMemoryError e) {
			if (mmp.mapImage != null) {
				mmp.removeImage(mmp.mapImage); 
				mmp.mapImage.free();
				mmp.mapImage = null; mapImage1to1 = mmp.mapImage;
			}
			rebuildOverlaySet();
			updateOnlyPosition(where, false);
			inf.close(0);
			Vm.showWait(false);
			(new MessageBox("Error", "Not enough memory to load map: "+ newmap.getImageFilename()+"\nYou can try to close\n all prgrams and \nrestart CacheWolf", MessageBox.OKB)).execute();
			dontUpdatePos = saveIgnoreStatus;
		}catch (SystemResourceException e) {
			if (mmp.mapImage != null) {
				mmp.removeImage(mmp.mapImage); 
				mmp.mapImage.free();
				mmp.mapImage = null; mapImage1to1 = mmp.mapImage;
			}
			rebuildOverlaySet();
			updateOnlyPosition(where, false); // TODO this doesn't work correctly if the resolution changed, I guess because the pixels of PosCircle will be interpreted from the new resolution, but should be interpreted using the old resolution to test: select a map with a much greater value of m per pixel manually 
			inf.close(0);
			Vm.showWait(false);
			(new MessageBox("Error", "Not enough ressources to load map: "+ newmap.getImageFilename()+"\nYou can try to close\n all prgrams and \nrestart CacheWolf", MessageBox.OKB)).execute();
			dontUpdatePos = saveIgnoreStatus;
		}
	}

	public void hideMap() {
		if (mmp != null && mmp.mapImage != null)
			mmp.mapImage.hide();
		mapHidden = true;
		repaintNow();
	}

	public void showMap() {
		if (mmp != null && mmp.mapImage != null) mmp.mapImage.unhide();
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
		if (h < 0) {
			h = java.lang.Math.abs(h);
			firstclickpoint.y = firstclickpoint.y - h;
		}
		if (w > 0) { // zoom in
			zoomFactor = java.lang.Math.min((float)this.width / (float)w, (float)this.height / (float)h);
		}
		else { // zoom out
			w = java.lang.Math.abs(w);
			firstclickpoint.x = firstclickpoint.x - w; // make firstclickedpoint the upper left corner
			zoomFactor = java.lang.Math.max((float)w / (float)this.width, (float)h / (float)this.height);
		}
		// calculate rect in unzoomed image in a way that the centre of the new image is the centre of selected area but give priority to the prefered image size of the scaled image
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
		Vm.showWait(this, true);
		boolean savegpsstatus = dontUpdatePos;
		if (mapImage1to1 != null) {
			dontUpdatePos = true; // avoid multi-thread problems
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
			if (mapHidden) mmp.mapImage.hide();
			mmp.addImage(mmp.mapImage);
			mmp.images.moveToBack(mmp.mapImage);
			if (mapImage1to1 != null && mmp.mapImage != null && mapImage1to1.image != null)
			{
				Point mappos = getMapPositionOnScreen();
				mmp.mapImage.move(mappos.x,mappos.y);
			}
		} else // no map image loaded 
		{ currentMap.zoom(zoomFactor, newImageRect.x, newImageRect.y); }
		// scaleWanted = currentMap.scale; use this if you want to change automatically to a map scale that best fits the zooming
		destroyOverlaySet();
		Vm.getUsedMemory(true); // call garbage collection
		setCenterOfScreen(center, false);
		addOverlaySet();
		updateScale();
		this.repaintNow();
		Vm.showWait(this, false);
		dontUpdatePos = savegpsstatus;
	}

	/*	public void gotFocus(int how) {
		super.gotFocus(how);
		Dimension ws = getSize(null);
		onWindowResize(ws.width, ws.height);
		Vm.debug(ws.width + " h: "+ws.height);
		this.setPreferredSize(width, height)
	}
	 */
	public void onEvent(Event ev){
		if(ev instanceof FormEvent && (ev.type == FormEvent.CLOSED )){
			running = false;
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
	Menu kontextMenu;
	MenuItem gotoMenuItem = new MenuItem("Goto here$g", 0, null);
	MenuItem newWayPointMenuItem = new MenuItem("Create new Waypoint here$n", 0, null);;
	MenuItem openCacheDescMenuItem,addCachetoListMenuItem;
	
	MenuItem miLuminary[];

	Menu mapsMenu;
	MenuItem selectMapMI = new MenuItem("Select a map manually$s", new IconAndText(new mImage("map_open.png"), "Select a map manually", null, Graphics.RIGHT));
	MenuItem changeMapDirMI = new MenuItem("Change map directory$c", new IconAndText(new mImage("map_cd.png"), "Change map directory", null, Graphics.RIGHT));
	MenuItem showMapMI = new MenuItem("Show map", new IconAndText(new mImage("map_on.png"), "Show map", null, Graphics.RIGHT));
	MenuItem hideMapMI = new MenuItem("Hide map", new IconAndText(new mImage("map_off.png"), "Hide map", null, Graphics.RIGHT));
	// automatic
	MenuItem mapChangeModusMI = new MenuItem("Modus for automatic map change", MenuItem.Separator, null);;
	MenuItem highestResGpsDestMI = new MenuItem("Highest res. containing dest. & cur. position", new IconAndText(new mImage("res_gps_goto.png"), "Highest res. containing dest. & cur. position", null, Graphics.RIGHT)); //immer höchste Auflösung wählen, die akt. Pos. und Ziel enthalten 
	MenuItem highestResolutionMI = new MenuItem("Highest resolution", new IconAndText(new mImage("res_high.png"), "Highest resolution", null, Graphics.RIGHT)); //immer höchste Auflösung wählen 
	MenuItem keepManResolutionMI = new MenuItem("Keep manual resolution", new IconAndText(new mImage("res_manuell.png"), "Keep manual resolution", null, Graphics.RIGHT)); // manuell gewählte Auflösung beibehalten  
	// manuell
	MenuItem mapChangeResMI = new MenuItem("Change resolution manually", MenuItem.Separator, null);;
	MenuItem AllCachesResMI = new MenuItem("Load a map containing all marked caches",  new IconAndText(new mImage("loupe_all.png"), "Load a map containing all marked caches", null, Graphics.RIGHT));   
	MenuItem moreDetailsMI = new MenuItem("Load a map with more details", new IconAndText(new mImage("loupe_more_details.png"), "Load a map with more details", null, Graphics.RIGHT)); // laod a map with more details 
	MenuItem moreOverviewMI = new MenuItem("Load a map for a better overview", new IconAndText(new mImage("loupe_better_overview.png"), "Load a map for a better overview", null, Graphics.RIGHT)); // Load a map for a better overview --> lesser details  
	// move map to
	MenuItem moveToMI = new MenuItem("Move map to an load map", MenuItem.Separator, null);;
	MenuItem moveToDestMI = new MenuItem("Move to goto point", new IconAndText(new mImage("move2goto.png"), "Move to goto point", null, Graphics.RIGHT)); //* Karte zum Ziel verschieben (und ggf. entsprechende Karte laden) 
	MenuItem moveToGpsMI = new MenuItem("Move to GPS position", new IconAndText(new mImage("move2gps.png"), "Move to GPS position", null, Graphics.RIGHT));   
	MenuItem moveToCenterMI = new MenuItem("Move to centre", new IconAndText(new mImage("move2center.png"), "Move to centre", null, Graphics.RIGHT));   

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
		miLuminary = new MenuItem[SkyOrientation.LUMINARY_NAMES.length];
		for (int i=0; i<SkyOrientation.LUMINARY_NAMES.length; i++) {
			miLuminary[i] = new MenuItem(SkyOrientation.getLuminaryName(i));
		}
		set(Control.WantHoldDown, true); // want to get simulated right-clicks
	}

	public boolean imageBeginDragged(AniImage which,Point pos) {
		if (mm.zoomingMode == true) { // zoom
//			saveMapLoc = pos;
			//		saveGpsIgnoreStatus = mm.ignoreGps;
			//	mm.ignoreGps = true;
			return false;
		}
		// move (drag) map
		//if (!(which == null || which == mapImage || which instanceof TrackOverlay || which == mm.directionArrows) ) return false;
		saveGpsIgnoreStatus = mm.dontUpdatePos; 
		mm.dontUpdatePos = true;
		saveMapLoc = pos;
		bringMapToTop();
		if (mapImage.isOnScreen() && !mapImage.hidden ) return super.imageBeginDragged(mapImage, pos);
		else return super.imageBeginDragged(null, pos);
	}

	public boolean imageNotDragged(ImageDragContext dc,Point pos){
		boolean ret = super.imageNotDragged(dc, pos);
		bringMaptoBack();
		if (dc.image == null) moveMap(pos.x - saveMapLoc.x, pos.y - saveMapLoc.y);
		else mapMoved(pos.x - saveMapLoc.x, pos.y - saveMapLoc.y);
		mm.dontUpdatePos = saveGpsIgnoreStatus;
		this.repaintNow();
		return ret;
	}

	public void onPenEvent(PenEvent ev) {
		if (!mm.zoomingMode && ev.type == PenEvent.PEN_DOWN) {
			saveMapLoc = new Point (ev.x, ev.y);
		}
		if (mm.zoomingMode && ev.type == PenEvent.PEN_DOWN) {
			saveGpsIgnoreStatus = mm.dontUpdatePos;
			mm.dontUpdatePos = true;
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
			mm.dontUpdatePos = saveGpsIgnoreStatus;
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
			left -= 2;
			top -= 2;
			if (top < 0) top = 0;
			if (left < 0) left = 0;
			if ((lastZoomWidth <= 0) && (ev.x - saveMapLoc.x > 0)) { // changed from zooming out to zooming in 
				removeImage(mm.buttonImageLensActivated);
				removeImage(mm.buttonImageLensActivatedZoomOut);
				addImage(mm.buttonImageLensActivatedZoomIn);
				this.repaintNow(dr, new Rect(mm.buttonImageLensActivatedZoomIn.location.x, mm.buttonImageLensActivatedZoomIn.location.y, mm.buttonImageLensActivatedZoomIn.getWidth(), mm.buttonImageLensActivatedZoomIn.getHeight()));
			}
			if ((lastZoomWidth >= 0) && (ev.x - saveMapLoc.x < 0)) { // changed from zooming out to zooming in 
				removeImage(mm.buttonImageLensActivated);
				removeImage(mm.buttonImageLensActivatedZoomIn);
				addImage(mm.buttonImageLensActivatedZoomOut);
				this.repaintNow(dr, new Rect(mm.buttonImageLensActivatedZoomOut.location.x, mm.buttonImageLensActivatedZoomOut.location.y, mm.buttonImageLensActivatedZoomOut.getWidth(), mm.buttonImageLensActivatedZoomOut.getHeight()));
			}
			this.repaintNow(dr, new Rect(left, top, java.lang.Math.abs(lastZoomWidth)+4, java.lang.Math.abs(lastZoomHeight)+4));
			lastZoomWidth = ev.x - saveMapLoc.x;
			lastZoomHeight =  ev.y - saveMapLoc.y;
			if (lastZoomWidth < 0) left = saveMapLoc.x + lastZoomWidth;
			else left = saveMapLoc.x;
			if (lastZoomHeight < 0)top = saveMapLoc.y + lastZoomHeight;
			else top = saveMapLoc.y;
			dr.setPen(new Pen(new Color(255,0,0),Pen.SOLID,3));
			dr.drawRect(left, top, java.lang.Math.abs(lastZoomWidth), java.lang.Math.abs(lastZoomHeight), 0); // bug in ewe: thickness parameter is ignored
		}
		super.onPenEvent(ev);
	}

	private void bringMapToTop() {
		if (mapImage == null || mapImage.hidden) {
			saveImageList = null;
			return;
		}
		saveImageList = new ImageList();
		saveImageList.copyFrom(images);
		images.removeAllElements();
		images.add(mapImage);
	}
	private void bringMaptoBack() {
		if (saveImageList == null) return;
		images = saveImageList;
		saveImageList = null;
	}

	public void moveMap(int diffX, int diffY) {
		Point p = new Point();
		if (mapImage!= null) {
			p = mapImage.locAlways;
			mapImage.move(p.x+diffX,p.y+diffY);
			//		if (mm.mapHidden) mapImage.properties |= AniImage.IsInvisible; // this is neccesarry because move will unhide the map if the coos show that the map is on the screen
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
			Point dest = mm.getXYonScreen(mm.gotoPos.where);
			g.setPen(new Pen(Color.DarkBlue, Pen.SOLID, 3));
			g.drawLine(mm.posCircleX, mm.posCircleY, dest.x, dest.y);
		}
	}

	public void chooseMap() {
		CWPoint gpspos;
		if (mm.myNavigation.gpsPos.Fix > 0) gpspos = new CWPoint(mm.myNavigation.gpsPos.latDec, mm.myNavigation.gpsPos.lonDec);
		else gpspos = null;
		ListBox l = new ListBox(mm.maps, gpspos, mm.getGotoPos(), mm.currentMap);
		if(l.execute() == FormBase.IDOK){
//			Vm.debug("Trying map: " + l.selectedMap.fileName);
			mm.autoSelectMap = false;
			if (l.selectedMap.isInBound(mm.posCircle.where) || l.selectedMap.getImageFilename().length()==0) {
				mm.setMap(l.selectedMap, mm.posCircle.where);
				mm.setResModus(MovingMap.NORMAL_KEEP_RESOLUTION);
				mm.ignoreGps = false;
			} else {
				mm.setGpsStatus(MovingMap.noGPS);
				mm.ignoreGps = true;
				mm.setMap(l.selectedMap, mm.posCircle.where); 
				if (mm.currentMap.fileNameWFL.length() > 0)
					mm.setCenterOfScreen(l.selectedMap.center, true); // if map has an image
				mm.setResModus(MovingMap.NORMAL_KEEP_RESOLUTION);
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
			mapsMenu = new Menu();
			mapsMenu.addItem(selectMapMI);
			mapsMenu.addItem(changeMapDirMI);
			if (!mm.noMapsAvailable) 
			{
				if (mm.mapHidden) mapsMenu.addItem(showMapMI);
				else mapsMenu.addItem(hideMapMI);
			}

			// automatic
			highestResGpsDestMI.modifiers &= ~MenuItem.Checked;
			highestResolutionMI.modifiers &= ~MenuItem.Checked;
			keepManResolutionMI.modifiers &= ~MenuItem.Checked;
			switch (mm.mapChangeModus) {
			case MovingMap.NORMAL_KEEP_RESOLUTION: keepManResolutionMI.modifiers |= MenuItem.Checked; break;   
			case MovingMap.HIGHEST_RESOLUTION: highestResolutionMI.modifiers |= MenuItem.Checked; break;   
			case MovingMap.HIGHEST_RESOLUTION_GPS_DEST: highestResGpsDestMI.modifiers |= MenuItem.Checked; break;   
			}
			mapsMenu.addItem(mapChangeModusMI);
			mapsMenu.addItem(highestResGpsDestMI);
			mapsMenu.addItem(highestResolutionMI);
			mapsMenu.addItem(keepManResolutionMI);
			// manuell
			mapsMenu.addItem(mapChangeResMI);
			mapsMenu.addItem(AllCachesResMI);
			mapsMenu.addItem(moreDetailsMI);
			mapsMenu.addItem(moreOverviewMI);
			// move map to
			mapsMenu.addItem(moveToMI);
			if (mm.gotoPos != null) moveToDestMI.modifiers &= ~MenuItem.Disabled;
			else moveToDestMI.modifiers |= MenuItem.Disabled;
			mapsMenu.addItem(moveToDestMI);
			if (Global.getPref().curCentrePt.isValid()) moveToCenterMI.modifiers &= ~MenuItem.Disabled;
			else moveToCenterMI.modifiers |= MenuItem.Disabled;
			mapsMenu.addItem(moveToCenterMI);
			mapsMenu.addItem(moveToGpsMI);

			//m.set(Menu., status)
			mapsMenu.exec(this, new Point(which.location.x, which.location.y), this);
		}
		if (which == mm.buttonImageGpsOn) {
			this.snapToGps();
		}
		if (which == mm.buttonImageLens) {
			mm.setZoomingMode();
			lastZoomWidth = 0;
			lastZoomHeight = 0;
		}
		if (which == mm.buttonImageZoom1to1) {
			mm.zoom1to1(); 
		}
		if (which == mm.bottonImageClose) {
			WindowEvent tmp = new WindowEvent();
			tmp.type = WindowEvent.CLOSE;
			mm.postEvent(tmp);
		}
	}

	public void snapToGps() {
		mm.myNavigation.startGps();
		mm.SnapToGps();
	}

	public void penHeld(Point p){
		//	if (!menuIsActive()) doMenu(p);
		if (!mm.zoomingMode) { 
			//( (ev.type == PenEvent.PEN_DOWN) && ((PenEvent)ev).modifiers == PenEvent.RIGHT_BUTTON)
			//|| ((ev.type == PenEvent.RIGHT_BUTTON) ) )) ---> these events are not posted --> this overridering is the only solution 
			kontextMenu = new Menu();
			if ( !(mm.directionArrows.onHotArea(p.x, p.y)) ) {
				kontextMenu.addItem(gotoMenuItem);
				kontextMenu.addItem(newWayPointMenuItem);
				AniImage clickedOnImage = images.findHotImage(p);
				if (clickedOnImage != null && clickedOnImage instanceof MapSymbol) {
					clickedCache = ((CacheHolder)((MapSymbol)clickedOnImage).mapObject);
					if (clickedCache != null) {
						openCacheDescMenuItem = new MenuItem("Open '"+(clickedCache.CacheName.length()>0?clickedCache.CacheName:clickedCache.wayPoint)+"'$o"); // clickedCache == null can happen if clicked on the goto-symbol
						kontextMenu.addItem(openCacheDescMenuItem);
						if (Global.mainForm.cacheListVisible) { 
							addCachetoListMenuItem = new MenuItem(MyLocale.getMsg(199,"Add to cachetour"));
							kontextMenu.addItem(addCachetoListMenuItem);
						}
					}
				}
			}
			else {			
				for (int i=0; i<SkyOrientation.LUMINARY_NAMES.length; i++) {
					kontextMenu.addItem(miLuminary[i]);
					if (i == mm.myNavigation.luminary) miLuminary[i].modifiers |= MenuItem.Checked;
					else miLuminary[i].modifiers &= MenuItem.Checked;
				}
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
					MenuItem action = (MenuItem) mapsMenu.getSelectedItem(); 
					if (mapsMenu.getSelectedItem() != null) {
						//maps
						if (action == selectMapMI)	{ 
							mapsMenu.close();
							chooseMap();
						}
						if (action == changeMapDirMI)	{
							mapsMenu.close();
							FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, Global.getPref().baseDir+"maps");
							fc.addMask("*.wfl");
							fc.setTitle((String)MyLocale.getMsg(4200,"Select map directory:"));
							if(fc.execute() != FileChooser.IDCANCEL){
								Global.getPref().saveCustomMapsPath(fc.getChosen().toString());
								mm.loadMaps(Global.getPref().getMapLoadPath(), mm.posCircle.where.latDec);
								mm.forceMapLoad();
							}
						}
						//dont show map
						if (action == hideMapMI) {
							mapsMenu.close();
							mm.hideMap();
						}
						// show map
						if (action == showMapMI) {
							mapsMenu.close();
							mm.showMap();
						}
						// map change modus
						if (action == highestResGpsDestMI) {
							mapsMenu.close();
							mm.setResModus(MovingMap.HIGHEST_RESOLUTION_GPS_DEST);
						}
						if (action == highestResolutionMI) {
							mapsMenu.close();
							mm.setResModus(MovingMap.HIGHEST_RESOLUTION);
						}
						if (action == keepManResolutionMI) {
							mapsMenu.close();
							mm.setResModus(MovingMap.NORMAL_KEEP_RESOLUTION);
						}
						// manually change map resolution
						if (action == moreDetailsMI) {
							mapsMenu.close();
							mm.loadMoreDetailedMap(false);
						} 
						if (action == moreOverviewMI) {
							mapsMenu.close();
							mm.loadMoreDetailedMap(true);
						}
						if (action == AllCachesResMI) {
							mapsMenu.close();
							mm.loadMapForAllCaches();
						}
						// moveto position
						if (action == moveToCenterMI) {
							mapsMenu.close();
							mm.setCenterOfScreen(Global.getPref().curCentrePt, true);
						}
						if (action == moveToDestMI) {
							mapsMenu.close();
							mm.setCenterOfScreen(mm.gotoPos.where, true);
						}
						if (action == moveToGpsMI) {
							mapsMenu.close();
							this.snapToGps();						
						}

					}
				}
			} // if (ev.target == mapsMenu)
			if (ev.target == kontextMenu) {
				if ((((MenuEvent)ev).type==MenuEvent.SELECTED)) {
					MenuItem action = (MenuItem) kontextMenu.getSelectedItem(); 
					if (action == gotoMenuItem) {
						kontextMenu.close();
						mm.myNavigation.setDestination(mm.ScreenXY2LatLon(saveMapLoc.x, saveMapLoc.y));	
					}
					if (action == openCacheDescMenuItem) {
						//mm.onEvent(new FormEvent(FormEvent.CLOSED, mm));
						kontextMenu.close();
						WindowEvent close = new WindowEvent();
						close.target = mm;
						close.type = WindowEvent.CLOSE;
						mm.postEvent(close);
						MainTab mainT = Global.mainTab;
						mainT.openDescriptionPanel(clickedCache);
					}
					if (action == newWayPointMenuItem) {
						kontextMenu.close();
						WindowEvent close = new WindowEvent();
						close.target = mm; 
						close.type = WindowEvent.CLOSE;
						mm.postEvent(close);
						CacheHolder newWP = new CacheHolder();
						newWP.pos = mm.ScreenXY2LatLon(saveMapLoc.x, saveMapLoc.y);
						newWP.LatLon=newWP.pos.toString(); 
						Global.mainTab.newWaypoint(newWP);
					}
					if (action == addCachetoListMenuItem) {
						kontextMenu.close();
						Global.mainForm.cacheList.addCache(clickedCache.wayPoint);
					}
					for (int i=0; i<miLuminary.length; i++) {
						if (action == miLuminary[i]) {
							kontextMenu.close();
							mm.myNavigation.setLuminary(i);
							mm.updateGps(mm.myNavigation.gpsPos.getFix());
							miLuminary[i].modifiers |= MenuItem.Checked;
						} else miLuminary[i].modifiers &= ~MenuItem.Checked;
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
	public MapInfoObject selectedMap; // = new MapInfoObject();
	mButton cancelButton, okButton;
	mList list = new mList(4,1,false);
	public boolean selected = false;
	Vector maps;

	public ListBox(Vector maps, CWPoint Gps, CWPoint gotopos, MapInfoObject curMap){
		this.title = "Maps";
		// if (Gui.screenIs(Gui.PDA_SCREEN)) this.setPreferredSize(200,100); else 
		// set width to screenwidth *3/4 but to at least 240 if the screen is big engough for 240px width
		this.setPreferredSize(java.lang.Math.max(MyLocale.getScreenWidth()*3/4, java.lang.Math.min(240, MyLocale.getScreenWidth()) ), MyLocale.getScreenHeight()*3/4);
		this.maps = maps;
		MapInfoObject map;
		MapListEntry ml;
		String cmp;
		ScrollBarPanel scb;
		int oldmap = -1;
		boolean curMapFound = false;
		boolean[] inList = new boolean[maps.size()];
		int row = -1;
		if (curMap == null) curMapFound = true;
		if (gotopos != null && Gps != null) {
			list.addItem("--- Karten von akt. Position und Ziel ---");
			row++;
			cmp = "FF1"+(new Area(new CWPoint(Gps.latDec, Gps.lonDec), gotopos)).getEasyFindString();
			for(int i = 0; i<maps.size();i++){
				ml = (MapListEntry)maps.get(i);
				try {
					if (!Area.containsRoughly(ml.sortEntryBBox, cmp)) continue; // TODO if no map available
					else { map = ml.getMap();}
				} catch (IOException ex) {continue; } // could not read .wfl-file
				if( map.isInBound(Gps.latDec, Gps.lonDec) && map.isInBound(gotopos) ) 
				{
					list.addItem(i + ": " + map.mapName);
					row++;
					inList[i] = true;
					if (!curMapFound && map.mapName.equals(curMap.mapName)) { oldmap = row; curMapFound = true; }
				} else inList[i] = false;
			}
		}
		if (Gps != null) {
			list.addItem("--- Karten der aktuellen Position ---");
			row++;
			cmp = "FF1"+Area.getEasyFindString(new CWPoint(Gps.latDec, Gps.lonDec), 30);
			for(int i = 0; i<maps.size();i++){
				ml = (MapListEntry)maps.get(i);
				try {
					if (!Area.containsRoughly(ml.sortEntryBBox, cmp)) continue; // TODO if no map available
					else { map = ml.getMap();}
				} catch (IOException ex) {continue; } // could not read .wfl-file
				if( map.isInBound(Gps.latDec, Gps.lonDec) ) 
				{
					list.addItem(i + ": " + map.mapName);
					row++;
					inList[i] = true;
					if (!curMapFound  && map.mapName.equals(curMap.mapName)) { oldmap = row; curMapFound = true; }
				}
			}
		}
		if (gotopos != null) {
			list.addItem("--- Karten des Ziels ---");
			row++;
			cmp = "FF1"+Area.getEasyFindString(gotopos, 30);
			for(int i = 0; i<maps.size();i++){
				ml = (MapListEntry)maps.get(i);
				try {
					if (!Area.containsRoughly(ml.sortEntryBBox, cmp)) continue; // TODO if no map available
					else { map = ml.getMap();}
				} catch (IOException ex) {continue; } // could not read .wfl-file
				if(map.isInBound(gotopos)) {
					list.addItem(i + ": " + map.mapName);
					row++;
					inList[i] = true;
					if (!curMapFound  && map.mapName.equals(curMap.mapName)) { oldmap = row; curMapFound = true; }
				}
			}
		}
		list.addItem("--- andere Karten ---");
		row++;
		for(int i = 0; i<maps.size();i++){
			ml = (MapListEntry)maps.get(i);
			if(!inList[i]) {
				list.addItem(i + ": " + ml.filename);
				row++;
				if (!curMapFound && ml.filename.equals(curMap.mapName)) { oldmap = row; curMapFound = true; }
			}
		}
		list.selectItem(oldmap, true);
		this.addLast(scb = new CacheWolf.MyScrollBarPanel(list),CellConstants.STRETCH, CellConstants.FILL);
		cancelButton = new mButton("Cancel");
		cancelButton.setHotKey(0, KeyEvent.getCancelKey(true));
		this.addNext(cancelButton,CellConstants.STRETCH, CellConstants.FILL);
		okButton = new mButton("Select");
		okButton.setHotKey(0, KeyEvent.getActionKey(true));
		this.addLast(okButton,CellConstants.STRETCH, CellConstants.FILL);
		okButton.takeFocus(0);
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

	public void mapSelected() {
		try { 
			selectedMap = null;
			int mapNum = 0;
			String it = new String();
			it = list.getText();
			if (it != ""){
				it = it.substring(0,it.indexOf(':'));
				mapNum = Convert.toInt(it);
				//	Vm.debug("Kartennummer: " + mapNum);
				try {
				selectedMap = ((MapListEntry)maps.get(mapNum)).getMap();
				selected = true;
				this.close(FormBase.IDOK);
				} catch (IOException e) {
					(new MessageBox("Error", "Cannot load wfl-file: \n" + ((MapListEntry)maps.get(mapNum)).filename, MessageBox.OKB)).execute();
				}
			}
			else {
				selected = false;
				this.close(FormBase.IDCANCEL);
			}
		}catch (NegativeArraySizeException e) {} // happens in substring when a dividing line selected 
	}

	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelButton){
				selectedMap = null;
				selected = false;
				this.close(FormBase.IDCANCEL);
			}
			if (ev.target == okButton || ev.target == list){ // ev.target == list is posted by mList if a selection was double clicked
				mapSelected();
			}
		}
		super.onEvent(ev);
	}

	public void  penDoubleClicked(Point where) {
		mapSelected();
	}
}

class ArrowsOnMap extends AniImage {
	float gotoDir = -361;
	float sunDir = -361;
	float moveDir = -361;

	int minY;
	Graphics draw;
	private MapInfoObject map=null;

	Color moveDirColor = new Color(255,0,0); // RED 
	final static Color sunDirColor = new Color(255,255,0); // Yellow
	//final static Color GREEN = new Color(0,255,0);
	final static Color gotoDirColor = new Color(0,0,128); // dark blue
	final static Color northDirColor = new Color(0,0,255); // Blue
	Point[] sunDirArrow = null;
	Point[] gotoDirArrow = null;
	Point[] moveDirArrow = null;
	Point[] northDirArrow = null;
	
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
		makeArrows();
	}

	public void setDirections(float gd, float sd, float md ) {
		if (java.lang.Math.abs(gotoDir - gd) > 1 // to save cpu-usage only update if the is a change of directions of more than 1 degree
				|| java.lang.Math.abs(sunDir - sd) > 1
				|| java.lang.Math.abs(moveDir - md) > 1)
		{
			//dirsChanged = false;
			gotoDir = gd;
			sunDir = sd;
			moveDir = md;
			makeArrows();
		}
	}

	/**
	 * draw arrows for the directions of movement and destination waypoint
	 * @param ctrl the control to paint on
	 * @param moveDir degrees of movement
	 * @param destDir degrees of destination waypoint
	 */

	public void doDraw(Graphics g,int options) {
		if (map == null || g == null) return;
		drawArrows(g);
		return;
/*		if (!dirsChanged) {
			g.drawImage(image,mask,transparentColor,0,-minY,location.width,location.height); // the transparency with a transparent color doesn't work in ewe-vm for pocketpc, it works in java-vm, ewe-vm on pocketpc2003 
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
*/	}

	private void makeArrows(){
			// draw only valid arrows
			if (moveDir < 360 && moveDir > -360) {
				if (moveDirArrow == null) moveDirArrow = new Point[2];
				makeArrow(moveDirArrow, moveDir, 1.0f);
			} else moveDirArrow = null;
			if (gotoDir < 360 && gotoDir > -360) {
				if (gotoDirArrow == null) gotoDirArrow = new Point[2];
				makeArrow(gotoDirArrow, gotoDir, 1.0f);
			} else gotoDirArrow = null;
			if (sunDir < 360 && sunDir> -360) {
				if (sunDirArrow == null ) sunDirArrow = new Point[2];
				makeArrow(sunDirArrow, sunDir, 0.75f);
			} else sunDirArrow = null;
			if (java.lang.Math.abs(map.rotationRad) > 1.5 / 180 * java.lang.Math.PI)	{ // show northth arrow only if it has more than 1.5 degree deviation from vertical direction
				if (northDirArrow == null) northDirArrow = new Point[2];
				makeArrow(northDirArrow, 0, 1.0f); // north direction
			} else northDirArrow = null;
			
			//select moveDirColor according to difference to gotoDir
			moveDirColor = new Color(255,0,0); // red
			
			if (moveDirArrow != null && gotoDirArrow != null)
			{
				float diff = java.lang.Math.abs(moveDir - gotoDir);
				while (diff > 360)
				{
					diff -= 360.0f;
				}
				if (diff > 180)
				{
					diff = 360.0f - diff;
				}
				
				if (diff <= 5.0)
				{
					moveDirColor = new Color(0,192,0);// darkgreen
				}
				else if (diff <= 22.5)
				{
					moveDirColor = new Color(0,255,0);// green
				}
				else if (diff <= 45.0)
				{
					moveDirColor = new Color(255,128,0);// orange
				}
			}
		}

	/**
	 * make (calculate) Pixel array for a single arrow 
	 * @param g handle for drawing
	 * @param angle angle of arrow
	 * @param col color of arrow
	 */
	private void makeArrow(Point[] arrow, float angle, float scale) {
		if (map == null) return;

		float angleRad;
		int centerX = location.width/2, centerY = location.height/2;
		if (arrow[0] == null) arrow[0] = new Point();
		if (arrow[1] == null) arrow[1] = new Point();
		arrow[0].x = centerX;
		arrow[0].y = centerY;
		angleRad = angle * (float)java.lang.Math.PI / 180 + map.rotationRad;
		arrow[1].x = centerX + new Float(centerX * java.lang.Math.sin(angleRad) * scale).intValue();
		arrow[1].y = centerY - new Float(centerY * java.lang.Math.cos(angleRad) * scale).intValue();
		//	g.setPen(new Pen(Color.Black,Pen.SOLID,7));
		//	g.drawLine(centerX,centerY,x,y);
	}

	public void drawArrows(Graphics g) {
		drawArrow(g, northDirArrow, northDirColor);
		drawArrow(g, gotoDirArrow, gotoDirColor);
		drawArrow(g, moveDirArrow, moveDirColor);
		drawArrow(g, sunDirArrow, sunDirColor);
	}
	
	public void drawArrow(Graphics g, Point[] arrow, Color col) {
		if (arrow == null) return;
		g.setPen(new Pen(col,Pen.SOLID,3));
		g.drawLine(arrow[0].x, arrow[0].y, arrow[1].x,arrow[1].y);
	}
}


