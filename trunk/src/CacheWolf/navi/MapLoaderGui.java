package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.CacheHolder;
import CacheWolf.CoordsScreen;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import ewe.io.File;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.ui.*;
import ewe.util.Vector;

/**
 * @author pfeffer
 * This Class is the Dialog for Download calibrated from expedia.com
 * is called from 
 */

public class MapLoaderGui extends Form {
	mButton cancelB, okBtiles, okBPerCache, cancelBPerCache;
	Preferences pref;
	mTabbedPanel mTab = new mTabbedPanel();
	CellPanel pnlTiles = new CellPanel();
	CellPanel pnlPerCache = new CellPanel();

	final String descString = "Download georeferenced maps\n Select online service:";
	mChoice mapServiceChoice;
	mCheckBox forCachesChkBox = new mCheckBox("for");
	mChoice forSelectedChkBox = new mChoice(new String[] {"all", "selected"}, 0);
	mChoice forSelectedChkBoxPerCache = new mChoice(new String[] {"all", "selected"}, 1);
	mLabel cachesLbl = new mLabel("caches");
	mInput distanceInput;
	mLabel distLbl;
	mLabel km = new mLabel("km");
	mLabel coosLbl;
	mButton coosBtn;
	mLabel scaleLbl = new mLabel("Approx. m per pixel:");
	mInput scaleInput = new mInput ("3");
	mInput scaleInputPerCache = new mInput ("1");
	mLabel overlappingLbl = new mLabel("overlapping in pixel:");
	mInput overlappingInput = new mInput("100");
	mCheckBox overviewChkBox = new mCheckBox("download an overview map");
	mCheckBox overviewChkBoxPerCache = new mCheckBox("download an overview map");

	MapLoader mapLoader;
	String[] unsortedMapServices;
	String[] sortedmapServices;
	int[] sortingMapServices;
	boolean[] inbound;
	CWPoint center;
	Vector cacheDB;
	boolean perCache;
	boolean onlySelected;
	float radius;
	float scale;
	int overlapping;
	boolean overviewmap;
	int numCaches;

	public MapLoaderGui(Vector cacheDBi) {
		super();
		this.title = MyLocale.getMsg(1800, "Download georeferenced maps"); 
		pref = Global.getPref(); // myPreferences sollte sp�ter auch diese Einstellungen speichern
		center = new CWPoint(pref.curCentrePt);
		cacheDB = cacheDBi;
		mapLoader = new MapLoader(Global.getPref().myproxy, Global.getPref().myproxyport, File.getProgramDirectory());

		// sort the items in the list of services in a way that services which cover the current center point.
		unsortedMapServices = mapLoader.getAvailableOnlineMapServices();
		sortMapServices();
		mapServiceChoice = new mChoice(sortedmapServices, 0);
		MessageArea desc = new MessageArea(descString); 
		desc.modifyAll(mTextPad.NotEditable | mTextPad.DisplayOnly | mTextPad.NoFocus, mTextPad.TakesKeyFocus);
		desc.borderStyle = mTextPad.BDR_NOBORDER;
		this.addLast(desc);
		this.addLast(mapServiceChoice);
		// tiles panel
		pnlTiles.addNext(forCachesChkBox);
		pnlTiles.addNext(forSelectedChkBox);
		pnlTiles.addLast(cachesLbl);
		pnlTiles.addNext(distLbl = new mLabel(MyLocale.getMsg(1802,"Within a rectangle of:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		distanceInput = new mInput();
		int tmp = Convert.toInt((Global.getProfile().distOC));
		distanceInput.setText(Convert.toString((tmp > 0 ? tmp : 15)));
		pnlTiles.addNext(distanceInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlTiles.addLast(km);
		pnlTiles.addNext(coosLbl = new mLabel(MyLocale.getMsg(1803, "around the centre: ")));
		pnlTiles.addLast(coosBtn = new mButton(center.toString()));
		pnlTiles.addNext(scaleLbl);
		mapLoader.setCurrentMapService(sortingMapServices[mapServiceChoice.selectedIndex]);
		scaleInput.setText(Convert.toString(mapLoader.currentOnlineMapService.recommendedScale));
		scaleInputPerCache.setText(Convert.toString(mapLoader.currentOnlineMapService.recommendedScale));
		this.focusFirst();
		pnlTiles.addLast(scaleInput);
		//	pnlTiles.addLast(resolutionLbl);
		pnlTiles.addNext(overlappingLbl);
		pnlTiles.addLast(overlappingInput);
		overviewChkBox.setState(true);
		pnlTiles.addLast(overviewChkBox);
		cancelB = new mButton(MyLocale.getMsg(1604,"Cancel"));
		cancelB.setHotKey(0, IKeys.ESCAPE);
		pnlTiles.addNext(cancelB,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		okBtiles = new mButton(MyLocale.getMsg(1605,"OK"));
		okBtiles.setHotKey(0, IKeys.ACTION);
		okBtiles.setHotKey(0, IKeys.ENTER);
		pnlTiles.addLast(okBtiles,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		updateForCachesState();
		mTab.addCard(pnlTiles, MyLocale.getMsg(1804, "Tiles"), MyLocale.getMsg(1804, "Tiles"));

		// per cache panel
		pnlPerCache.addNext(new mLabel("Download one map for"), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));
		pnlPerCache.addNext(forSelectedChkBoxPerCache, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));
		pnlPerCache.addLast(new mLabel("caches"), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));
		pnlPerCache.addNext(new mLabel("Approx. m per pixel"), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));
		pnlPerCache.addLast(scaleInputPerCache, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));

		cancelBPerCache = new mButton(MyLocale.getMsg(1604,"Cancel"));
		cancelBPerCache.setHotKey(0, IKeys.ESCAPE);
		pnlPerCache.addNext(cancelBPerCache, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));
		okBPerCache = new mButton(MyLocale.getMsg(1605,"OK"));
		okBPerCache.setHotKey(0, IKeys.ACTION);
		okBPerCache.setHotKey(0, IKeys.ENTER);
		pnlPerCache.addLast(okBPerCache, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));

		mTab.addCard(pnlPerCache, MyLocale.getMsg(1805, "Per cache"), MyLocale.getMsg(1805, "Per Cache"));
		this.addLast(mTab);
	}

	/**
	 * sort the map services in order to have the services, which cover
	 * the current center first in the list 
	 * this sets inbound[], sortedMapServices[] and sortingmapServices[]
	 *
	 */	
	private void sortMapServices() {
		sortingMapServices = new int[unsortedMapServices.length];
		inbound = new boolean[unsortedMapServices.length];
		int j=0;
		for (int i=0; i < sortingMapServices.length; i++) {
			if( ((OnlineMapService)mapLoader.onlineMapServices.get(i)).boundingBox.isInBound(center)) {
				sortingMapServices[j] = i;
				j++;
				inbound[i] = true;
			} else inbound[i] = false;
		}
		sortedmapServices = new String[unsortedMapServices.length];
		for (int i=0; i < sortedmapServices.length; i++) {
			if (!inbound[i]) { 
				sortingMapServices[j] = i;
				j++;
			}
			sortedmapServices[i] = ((OnlineMapService)mapLoader.onlineMapServices.get(sortingMapServices[i])).getName();
		}
	}
	
	private int getSortedMapServiceIndex(int originalindex) {
		for (int i = 0; i < sortingMapServices.length; i++) {
			if (sortingMapServices[i] == originalindex) return i;
		}
		throw new IllegalStateException("getSortedMapServiceIndex: index " + originalindex + "not found");
	}

	public String getMapsDir() {
		String ret = Global.getPref().getMapDownloadSavePath(mapLoader.currentOnlineMapService.getMapType());
		Global.getPref().saveCustomMapsPath(ret);
		return ret;
	}

	public void downloadTiles() {
		String mapsDir = getMapsDir();
		if (mapsDir == null) return;
		InfoBox progressBox = new InfoBox("Downloading georeferenced maps", "Downloading georeferenced maps\n \n \n \n \n", InfoBox.PROGRESS_WITH_WARNINGS);
		progressBox.setPreferredSize(220, 300);
		progressBox.setInfoHeight(160);
		progressBox.relayout(false);
		progressBox.exec();
		mapLoader.setProgressInfoBox(progressBox);
		Vm.showWait(true);
		ewe.fx.Point size = new ewe.fx.Point(1000,1000); // Size of the downloaded maps
		if (forCachesChkBox.getState() || perCache) {
			Area surArea = Global.getProfile().getSourroundingArea(onlySelected); // calculate map boundaries from cacheDB
			if (surArea == null) {
				(new MessageBox("Error", "No Caches are seleted", MessageBox.OKB)).execute();
				Vm.showWait(false);
				progressBox.close(0);
				return;
			}
			mapLoader.setTiles(surArea.topleft, surArea.buttomright, scale, size, overlapping );
		} else 
		{ // calculate from centre point an radius
			mapLoader.setTiles(center, radius * 1000, scale, size, overlapping);
		}
		if (overviewmap) {
			progressBox.setInfo("downloading overview map"); 
			float scale = MapLoader.getScale(center, radius * 1000, size);
			try {
				mapLoader.downloadMap(center, scale, size, mapsDir);
			} catch (Exception e) {
				progressBox.addWarning("Overview map: Ignoring error: " + e.getMessage()+"\n");
			}
		}
		if (!perCache){  // download tiles
			mapLoader.setProgressInfoBox(progressBox);
			mapLoader.downlaodTiles(mapsDir);
		} else { // per cache
			CacheHolder ch; 
			CWPoint tmpca = new CWPoint();
			int numdownloaded = 0;
			Global.getProfile().getSourroundingArea(onlySelected); // calculate numCachesInArea
			int numCaches = Global.getProfile().numCachesInArea;
			for (int i=cacheDB.size()-1; i >= 0; i--) {
				ch = (CacheHolder) cacheDB.get(i);
				if (!this.onlySelected || ch.is_Checked) {
					if (ch.pos == null) { // this can not happen
						tmpca.set(ch.LatLon);
						ch.pos = new CWPoint(tmpca);
					}
					if (ch.pos.isValid() && ch.pos.latDec != 0 && ch.pos.lonDec != 0) { // TODO != 0 sollte verschwinden, sobald das handling von nicht gesetzten Koos �berall korrekt ist
						numdownloaded++;
						progressBox.setInfo("Downloading map '"+mapLoader.currentOnlineMapService.getName()+"'\n"+numdownloaded+" / "+numCaches+"\n for cache:\n"+ch.CacheName);
						try {
							mapLoader.downloadMap(ch.pos, scale, size, mapsDir);
						} catch (Exception e) {
							progressBox.addWarning("Cache: " + ch.CacheName + "(" + ch.wayPoint + ") Ignoring error: " + e.getMessage()+"\n");
						}
					}
				}
			}
		}
		Vm.showWait(false);
		progressBox.addWarning("Finished downloading and calibration of maps");
		progressBox.addOkButton();
		progressBox.waitUntilClosed();
		mapLoader.setProgressInfoBox(null);
		//progressBox.close(0);
		if(Global.mainTab.mm != null) Global.mainTab.mm.mapsloaded = false; 
		//	(new MessageBox("Download maps", "Downloaded and calibrated the maps successfully", MessageBox.OKB)).execute();
	}


	private void updateForCachesState() {
		int a, b;
		if (forCachesChkBox.getState()) {
			// create map rectangle from caches
			a = 0;
			b = Control.Disabled;
		}
		else { // use centre and distance input
			a = Control.Disabled;
			b = 0;
		}
		forSelectedChkBox.modify(a, b);
		cachesLbl.modify(a, b);
		distanceInput.modify(b, a);
		distLbl.modify(b, a);
		coosBtn.modify(b, a);
		coosLbl.modify(b, a);
		km.modify(b,a);
		repaintNow();

	}

	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB || ev.target == cancelBPerCache){
				this.close(Form.IDCANCEL);
			}
			if (ev.target == okBtiles || ev.target == okBPerCache){
				mapLoader.setCurrentMapService(sortingMapServices[mapServiceChoice.selectedIndex]);
				if (ev.target == okBtiles) { // get tiles
					perCache = false;
					if (forSelectedChkBox.getSelectedItem().toString().equalsIgnoreCase("all")) onlySelected = false;
					else onlySelected = true;
					overviewmap = overviewChkBox.getState();
					radius = Convert.toFloat(distanceInput.getText());
					scale = Convert.toFloat(scaleInput.getText());
					overlapping = Convert.toInt(overlappingInput.getText());
					if (!forCachesChkBox.getState()) {
						if (radius <= 0) { 
							(new MessageBox("Error", "'radius' must be graeter than null", MessageBox.OKB)).execute();
							return;
						}
						if (overlapping < 0) { 
							(new MessageBox("Error", "'overlapping' must be greater or equal 0 ", MessageBox.OKB)).execute();
							return;
						}
						if (!center.isValid() && !forCachesChkBox.getState()) {
							(new MessageBox("Error", "Please enter the 'centre' around which the maps shall be downloaded", MessageBox.OKB)).execute();
							return;
						}
					}
				} else { // per cache
					perCache = true;
					if (forSelectedChkBoxPerCache.getSelectedItem().toString().equalsIgnoreCase("all")) onlySelected = false;
					else onlySelected = true;
					overviewmap = overviewChkBoxPerCache.getState();
					scale = Convert.toFloat(scaleInputPerCache.getText());
				}
				if (scale < mapLoader.currentOnlineMapService.minscale || scale > mapLoader.currentOnlineMapService.maxscale) {
					(new MessageBox("Error", "The selected online map service provides map in the scale from " + mapLoader.currentOnlineMapService.minscale + " to "+ mapLoader.currentOnlineMapService.maxscale +"\n please adjust 'Approx. meter pro pixel' accordingly", MessageBox.OKB)).execute();
					return;
				}
				if (!mapLoader.currentOnlineMapService.getName().equalsIgnoreCase("expedia") &&
						scale > 0.5) {
					int a = (new MessageBox("Error", "Momentanously the calibration of downloaded maps from a WMS has a maximam deviation of about 1 percent of the map size in meters. That's why I strongly recommend to use it only up to a scale of 0.5. In this case the deviation will be lower than 5m. Do you whish to continue anyway?", MessageBox.YESB | MessageBox.NOB)).execute();
					if (a == MessageBox.NOB) return;
				}
				this.close(Form.IDOK); 
				this.downloadTiles();
			}
			if (ev.target == coosBtn) {
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(center, CWPoint.CW);
				if (cs.execute() != CoordsScreen.IDCANCEL) {
					center = cs.getCoords();
					coosBtn.setText(center.toString());
					int tmp = sortingMapServices[mapServiceChoice.selectedIndex];
					sortMapServices();
					mapServiceChoice.set(sortedmapServices, (!inbound[tmp] ? 0 : getSortedMapServiceIndex((tmp))));
				}
			}
			if (ev.target == forCachesChkBox) {
				updateForCachesState();
			}
		} // end of "if controllEvent..."
		if (ev instanceof DataChangeEvent && ev.target == mapServiceChoice) {
			mapLoader.setCurrentMapService(sortingMapServices[mapServiceChoice.selectedIndex]);
			scaleInput.setText(Convert.toString(mapLoader.currentOnlineMapService.recommendedScale));
			scaleInputPerCache.setText(Convert.toString(mapLoader.currentOnlineMapService.recommendedScale));
		}
		super.onEvent(ev);
	}
}
