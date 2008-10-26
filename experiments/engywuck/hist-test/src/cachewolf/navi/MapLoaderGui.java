package cachewolf.navi;

import eve.io.File;
import eve.sys.Convert;
import eve.ui.*;
import java.util.Vector;

import cachewolf.CWPoint;
import cachewolf.CacheHolder;
import cachewolf.CoordsScreen;
import cachewolf.Global;
import cachewolf.InfoBox;
import cachewolf.MyLocale;
import cachewolf.Preferences;

import eve.ui.event.ControlEvent;
import eve.sys.Event;
import eve.ui.event.DataChangeEvent;
import eve.fx.gui.IKeys;

/**
 * @author pfeffer
 * This Class is the Dialog for Download calibrated from expedia.com
 * is called from 
 *  * start offset for language file: 1800
 */

public class MapLoaderGui extends Form {
	Button cancelB, okBtiles, okBPerCache, cancelBPerCache;
	Preferences pref;
	TabbedPanel mTab = new TabbedPanel();
	CellPanel pnlTiles = new CellPanel();
	CellPanel pnlPerCache = new CellPanel();

	final String descString = MyLocale.getMsg(1802,"Download georeferenced maps\n Select online service:");
	Choice mapServiceChoice;
	CheckBox forCachesChkBox = new CheckBox(MyLocale.getMsg(1803,"for"));
	Choice forSelectedChkBox = new Choice(new String[] {MyLocale.getMsg(1804,"all"), MyLocale.getMsg(1805,"selected")}, 0);
	Choice forSelectedChkBoxPerCache = new Choice(new String[] {MyLocale.getMsg(1804,"all"), MyLocale.getMsg(1805,"selected")}, 1);
	Label cachesLbl = new Label(MyLocale.getMsg(1806,"caches"));
	Input distanceInput;
	Label distLbl;
	Label km = new Label("km");
	Label coosLbl;
	Button coosBtn;
	Label scaleLbl = new Label(MyLocale.getMsg(1807,"Approx. m per pixel:"));
	Input scaleInput = new Input ("3");
	Input scaleInputPerCache = new Input ("1");
	Label overlappingLbl = new Label(MyLocale.getMsg(1808,"overlapping in pixel:"));
	Input overlappingInput = new Input("100");
	CheckBox overviewChkBox = new CheckBox(MyLocale.getMsg(1809,"download an overview map"));
	CheckBox overviewChkBoxPerCache = new CheckBox(MyLocale.getMsg(1809,"download an overview map"));

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
	//int numCaches;

	public MapLoaderGui(Vector cacheDBi) {
		super();
		this.title = MyLocale.getMsg(1800, "Download georeferenced maps"); 
		pref = Global.getPref(); // myPreferences sollte später auch diese Einstellungen speichern
		center = new CWPoint(pref.curCentrePt);
		cacheDB = cacheDBi;
		mapLoader = new MapLoader(File.getProgramDirectory()+"/"+"webmapservices");

		// sort the items in the list of services in a way that services which cover the current center point.
		unsortedMapServices = mapLoader.getAvailableOnlineMapServices();
		sortMapServices();
		mapServiceChoice = new Choice(sortedmapServices, 0);
		MessageArea desc = new MessageArea(descString); 
		desc.modifyAll(TextPad.NotEditable | TextPad.DisplayOnly | TextPad.NoFocus, TextPad.TakesKeyFocus);
		desc.borderStyle = TextPad.BDR_NOBORDER;
		this.addLast(desc);
		this.addLast(mapServiceChoice);
		// tiles panel
		pnlTiles.addNext(forCachesChkBox);
		pnlTiles.addNext(forSelectedChkBox);
		pnlTiles.addLast(cachesLbl);
		pnlTiles.addNext(distLbl = new Label(MyLocale.getMsg(1810,"Within a rectangle of:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		distanceInput = new Input();
		int tmp = Convert.toInt((Global.getProfile().distOC));
		tmp = java.lang.Math.max(tmp, Convert.toInt((Global.getProfile().distGC)));
		distanceInput.setText(Convert.toString((tmp > 0 ? tmp : 15)));
		pnlTiles.addNext(distanceInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlTiles.addLast(km);
		pnlTiles.addNext(coosLbl = new Label(MyLocale.getMsg(1811, "around the centre:")+" "));
		pnlTiles.addLast(coosBtn = new Button(center.toString()));
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
		cancelB = new Button(MyLocale.getMsg(1604,"Cancel"));
		cancelB.setHotKey(0, IKeys.ESCAPE);
		pnlTiles.addNext(cancelB,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		okBtiles = new Button(MyLocale.getMsg(1605,"OK"));
		okBtiles.setHotKey(0, IKeys.ACTION);
		okBtiles.setHotKey(0, IKeys.ENTER);
		pnlTiles.addLast(okBtiles,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		updateForCachesState();
		mTab.addCard(pnlTiles, MyLocale.getMsg(1812, "Tiles"), MyLocale.getMsg(1812, "Tiles"));

		// per cache panel
		pnlPerCache.addNext(new Label(MyLocale.getMsg(1813, "Download one map for")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));
		pnlPerCache.addNext(forSelectedChkBoxPerCache, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));
		pnlPerCache.addLast(new Label(MyLocale.getMsg(1806, "caches")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));
		pnlPerCache.addNext(new Label(MyLocale.getMsg(1807, "Approx. m per pixel")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));
		pnlPerCache.addLast(scaleInputPerCache, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));

		cancelBPerCache = new Button(MyLocale.getMsg(1604,"Cancel"));
		cancelBPerCache.setHotKey(0, IKeys.ESCAPE);
		pnlPerCache.addNext(cancelBPerCache, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));
		okBPerCache = new Button(MyLocale.getMsg(1605,"OK"));
		okBPerCache.setHotKey(0, IKeys.ACTION);
		okBPerCache.setHotKey(0, IKeys.ENTER);
		pnlPerCache.addLast(okBPerCache, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL));

		mTab.addCard(pnlPerCache, MyLocale.getMsg(1814, "Per cache"), MyLocale.getMsg(1813, "Per Cache"));
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
		throw new IllegalStateException(MyLocale.getMsg(1818, "getSortedMapServiceIndex: index")+" " + originalindex + MyLocale.getMsg(1819, "not found"));
	}

	public String getMapsDir() {
		String ret = Global.getPref().getMapDownloadSavePath(mapLoader.currentOnlineMapService.getMapType());
		Global.getPref().saveCustomMapsPath(ret);
		return ret;
	}

	public void downloadTiles() {
		String mapsDir = getMapsDir();
		if (mapsDir == null) return;
		InfoBox progressBox = new InfoBox(MyLocale.getMsg(1815, "Downloading georeferenced maps"), MyLocale.getMsg(1816, "Downloading georeferenced maps\n \n \n \n \n"), InfoBox.PROGRESS_WITH_WARNINGS);
		progressBox.setPreferredSize(220, 300);
		progressBox.setInfoHeight(160);
		progressBox.relayout(false);
		progressBox.exec();
		mapLoader.setProgressInfoBox(progressBox);
		Form.showWait();
		eve.fx.Point size = new eve.fx.Point(1000,1000); // Size of the downloaded maps
		if (forCachesChkBox.getState() || perCache) {
			Area surArea = Global.getProfile().getSourroundingArea(onlySelected); // calculate map boundaries from cacheDB
			if (surArea == null) {
				(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1817, "No Caches are seleted"), MessageBox.OKB)).execute();
				Form.cancelWait();
				progressBox.close(0);
				return;
			}
			mapLoader.setTiles(surArea.topleft, surArea.buttomright, scale, size, overlapping );
		} else 
		{ // calculate from centre point an radius
			mapLoader.setTiles(center, radius * 1000, scale, size, overlapping);
		}
		if (overviewmap) {
			progressBox.setInfo(MyLocale.getMsg(1824, "downloading overview map")); 
			float scale = MapLoader.getScale(center, radius * 1000, size);
			try {
				mapLoader.downloadMap(center, scale, size, mapsDir);
			} catch (Exception e) {
				progressBox.addWarning(MyLocale.getMsg(1825, "Overview map: Ignoring error:")+" " + e.getMessage()+"\n");
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
						tmpca.set(ch.latLon);
						ch.pos = new CWPoint(tmpca);
					}
					if (ch.pos.isValid() && ch.pos.latDec != 0 && ch.pos.lonDec != 0) { // TODO != 0 sollte verschwinden, sobald das handling von nicht gesetzten Koos überall korrekt ist
						numdownloaded++;
						progressBox.setInfo(MyLocale.getMsg(1820, "Downloading map '")+mapLoader.currentOnlineMapService.getName()+"'\n"+numdownloaded+" / "+numCaches+MyLocale.getMsg(1821, "\n for cache:\n")+ch.cacheName);
						try {
							mapLoader.downloadMap(ch.pos, scale, size, mapsDir);
						} catch (Exception e) {
							progressBox.addWarning(MyLocale.getMsg(1822, "Cache:")+" " + ch.cacheName + "(" + ch.wayPoint + ") "+MyLocale.getMsg(1823, "Ignoring error:")+" " + e.getMessage()+"\n");
						}
					}
				}
			}
		}
		Form.cancelWait();
		progressBox.addWarning(MyLocale.getMsg(1826, "Finished downloading and calibration of maps"));
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
					if (forSelectedChkBox.getSelectedItem().toString().equalsIgnoreCase(MyLocale.getMsg(1804, "all"))) onlySelected = false;
					else onlySelected = true;
					overviewmap = overviewChkBox.getState();
					radius = (float)cachewolf.utils.Common.parseDouble(distanceInput.getText());
					scale = (float)cachewolf.utils.Common.parseDouble(scaleInput.getText());
					overlapping = Convert.toInt(overlappingInput.getText());
					if (!forCachesChkBox.getState()) {
						if (radius <= 0) { 
							(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1827, "'radius' must be graeter than 0"), MessageBox.OKB)).execute();
							return;
						}
						if (overlapping < 0) { 
							(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1828, "'overlapping' must be greater or equal 0"), MessageBox.OKB)).execute();
							return;
						}
						if (!center.isValid() && !forCachesChkBox.getState()) {
							(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1829, "Please enter the 'centre' around which the maps shall be downloaded"), MessageBox.OKB)).execute();
							return;
						}
					}
				} else { // per cache
					perCache = true;
					if (forSelectedChkBoxPerCache.getSelectedItem().toString().equalsIgnoreCase(MyLocale.getMsg(1804, "all"))) onlySelected = false;
					else onlySelected = true;
					overviewmap = overviewChkBoxPerCache.getState();
					scale = Convert.toFloat(scaleInputPerCache.getText());
				}
				if (scale < mapLoader.currentOnlineMapService.minscale || scale > mapLoader.currentOnlineMapService.maxscale) {
					(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1830, "The selected online map service provides map in the scale from")+" " + mapLoader.currentOnlineMapService.minscale + MyLocale.getMsg(1831, " to")+" "+ mapLoader.currentOnlineMapService.maxscale +MyLocale.getMsg(1832, "\n please adjust 'Approx. meter pro pixel' accordingly"), MessageBox.OKB)).execute();
					return;
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
