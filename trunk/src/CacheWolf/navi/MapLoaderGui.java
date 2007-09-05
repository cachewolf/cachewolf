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

	final String descString = "Download georeferenced maps from expedia.com";
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
		pref = Global.getPref(); // myPreferences sollte später auch diese Einstellungen speichern
		center = new CWPoint(pref.curCentrePt);
		cacheDB = cacheDBi;
		// tiles panel
		MessageArea desc = new MessageArea(descString);
		desc.modifyAll(mTextPad.NotEditable | mTextPad.DisplayOnly | mTextPad.NoFocus, mTextPad.TakesKeyFocus);
		desc.borderStyle = mTextPad.BDR_NOBORDER;
		pnlTiles.addLast(desc);
		pnlTiles.addNext(forCachesChkBox);
		pnlTiles.addNext(forSelectedChkBox);
		pnlTiles.addLast(cachesLbl);
		pnlTiles.addNext(distLbl = new mLabel(MyLocale.getMsg(1802,"Within a rectangle of:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		distanceInput = new mInput();
		int tmp = Convert.toInt((Global.getProfile().distOC));
		distanceInput.setText(Convert.toString((tmp > 0 ? tmp : 15)));
		pnlTiles.addNext(distanceInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlTiles.addLast(km);
		pnlTiles.addNext(coosLbl = new mLabel(MyLocale.getMsg(1803, "around the center: ")));
		pnlTiles.addLast(coosBtn = new mButton(center.toString()));
		pnlTiles.addNext(scaleLbl);
		scaleInput.setText("5");
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
		pnlPerCache.addLast(new MessageArea(descString));
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
	public String getMapsDir() {
		return Global.getPref().getMapExpediaSavePath();
	}
	public void downloadTiles() {
		String mapsDir = getMapsDir();
		if (mapsDir == null) return;
		InfoBox progressBox = new InfoBox("Downloading georeferenced maps", "Downloading georeferenced maps\n from www.expedia.com");
		progressBox.setPreferredSize(230, 150);
		progressBox.exec();
		Vm.showWait(true);
		ewe.fx.Point size = new ewe.fx.Point(1000,1000); // Size of the downloaded maps
		MapLoader ml = new MapLoader(Global.getPref().myproxy, Global.getPref().myproxyport);
		if (forCachesChkBox.getState() || perCache) {
			Area surArea = Global.getProfile().getSourroundingArea(onlySelected); // calculate map boundaries from cacheDB
			if (surArea == null) {
				(new MessageBox("Error", "No Caches are seleted", MessageBox.OKB)).execute();
				Vm.showWait(false);
				progressBox.close(0);
				return;
			}
			ml.setTiles(surArea.topleft, surArea.buttomright, (int)scale, size, overlapping );
			// calculate radius and center for overview map
			center = new CWPoint((surArea.topleft.latDec + surArea.buttomright.latDec)/2, (surArea.topleft.lonDec + surArea.buttomright.lonDec)/2);
			double radiuslat = (new CWPoint(center.latDec, surArea.buttomright.lonDec)).getDistance(surArea.buttomright);
			double radiuslon = (new CWPoint(surArea.buttomright.latDec, center.lonDec)).getDistance(surArea.buttomright);
			radius = (float) (radiuslat < radiuslon ? radiuslon : radiuslat);
		} else 
		{ // calculate from center point an radius
			ml.setTiles(center, radius * 1000, (int)scale, size, overlapping);
		}
		if (overviewmap) {
			progressBox.setInfo("downloading overview map"); 
			int expediaAlti = MapLoader.getExpediaAlti(center, radius * 1000, size);
			ml.downloadMap(center.latDec, center.lonDec, expediaAlti, size.x, size.y, mapsDir);
		}
		if (!perCache){  // download tiles
			ml.setProgressInfoBox(progressBox);
			ml.downlaodTiles(mapsDir);
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
					if (ch.pos.isValid() && ch.pos.latDec != 0 && ch.pos.lonDec != 0) { // TODO != 0 sollte verschwinden, sobald das handling von nicht gesetzten Koos überall korrekt ist
						numdownloaded++;
						progressBox.setInfo("Downloading map from expedia.de\n"+numdownloaded+" / "+numCaches+"\n for cache:\n"+ch.CacheName);
						ml.downloadMap(ch.pos.latDec, ch.pos.lonDec, (int)scale, size.x, size.y, mapsDir);
					}
				}
			}
		}
		Vm.showWait(false);
		ml.setProgressInfoBox(null);
		progressBox.close(0);
		if(Global.mainTab.mm != null) Global.mainTab.mm.mapsloaded = false; 
		(new MessageBox("Expedia maps", "Downloaded and calibrated the maps successfully", MessageBox.OKB)).execute();
	}


	private void updateForCachesState() {
		int a, b;
		if (forCachesChkBox.getState()) {
			// create map rectangle from caches
			a = 0;
			b = Control.Disabled;
		}
		else { // use center and distance input
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
					}
				} else { // per cache
					perCache = true;
					if (forSelectedChkBoxPerCache.getSelectedItem().toString().equalsIgnoreCase("all")) onlySelected = false;
					else onlySelected = true;
					overviewmap = overviewChkBoxPerCache.getState();
					scale = Convert.toFloat(scaleInputPerCache.getText());
				}
				if (scale < 1 || scale != java.lang.Math.floor(scale)) {
					(new MessageBox("Error", "'Approx. meter pro pixel' must be greater than 0 and must not contain a point", MessageBox.OKB)).execute();
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
				}
			}
			if (ev.target == forCachesChkBox) {
				updateForCachesState();
			}
		} // if controllEvent...
		super.onEvent(ev);
	}
}
