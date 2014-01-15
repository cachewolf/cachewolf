/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.CoordsScreen;
import CacheWolf.ExecutePanel;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.MyLocale;
import CacheWolf.UrlFetcher;
import CacheWolf.utils.Common;
import ewe.fx.Point;
import ewe.io.File;
import ewe.io.IOException;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.DataChangeEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.MessageArea;
import ewe.ui.UIConstants;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mComboBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.mTabbedPanel;

/**
 * @author pfeffer This Class is the Dialog for Download calibrated from expedia.com start offset for language file: 1800
 */

public class MapLoaderGui extends Form {
    private ExecutePanel executePanel, executePanelPerCache;
    mTabbedPanel mTab = new mTabbedPanel();

    final String descString = MyLocale.getMsg(1802, "Download georeferenced maps\n Select online service:");
    mChoice mapServiceChoice;
    mCheckBox forCachesChkBox = new mCheckBox(MyLocale.getMsg(1803, "for"));
    mChoice forSelectedChkBox = new mChoice(new String[] { MyLocale.getMsg(1804, "all"), MyLocale.getMsg(1805, "selected") }, 0);
    mChoice forSelectedChkBoxPerCache = new mChoice(new String[] { MyLocale.getMsg(1804, "all"), MyLocale.getMsg(1805, "selected") }, 1);
    mLabel cachesLbl = new mLabel(MyLocale.getMsg(1806, "caches"));
    mInput distanceInput;
    mLabel distLbl;
    mLabel km = new mLabel("km");
    mLabel coosLbl;
    mButton coosBtn;
    mLabel scaleLblPerCache = new mLabel(MyLocale.getMsg(1807, "Approx. m per pixel:"));
    mLabel scaleLbl = new mLabel(MyLocale.getMsg(1807, "Approx. m per pixel:"));
    mComboBox scaleInput = new mComboBox();
    mComboBox scaleInputPerCache = new mComboBox();
    mLabel overlappingLbl = new mLabel(MyLocale.getMsg(1808, "overlapping in pixel:"));
    mInput overlappingInput = new mInput("" + Global.pref.mapOverlapping);
    mCheckBox fetchOnlyMapWithCacheChkBox = new mCheckBox(MyLocale.getMsg(165, "only for caches"));
    mCheckBox smallTiles = new mCheckBox(MyLocale.getMsg(4280, "Small Tiles"));
    mCheckBox bigTiles = new mCheckBox(MyLocale.getMsg(4282, "BigTiles"));

    private mInput tileWidthInput = new mInput();
    private mInput tileHeightInput = new mInput();
    // pnlTiles
    private mInput pnlTilestileWidthInput = new mInput();
    private mInput pnlTilestileHeightInput = new mInput();
    private MapLoader mapLoader;
    private String[] unsortedMapServices;
    private String[] sortedmapServices;
    private int[] sortingMapServices;
    private boolean[] inbound;
    private CWPoint center;
    private CacheDB cacheDB;
    private boolean perCache;
    private boolean onlySelected;
    private float radius;
    private float scale;
    private int overlapping;

    public boolean isCreated;

    public MapLoaderGui(CacheDB cacheDBi) {
	super();
	isCreated = false;
	this.title = MyLocale.getMsg(1800, "Download georeferenced maps");
	center = new CWPoint(Global.pref.getCurCentrePt());
	cacheDB = cacheDBi;
	mapLoader = new MapLoader();

	// sort the items in the list of services in a way that services which cover the current center point.
	unsortedMapServices = mapLoader.getAvailableOnlineMapServices(center);
	if (unsortedMapServices.length <= 0) {
	    Global.pref.log("no OnlineMapServices defined");
	    return;
	}
	sortMapServices();
	mapServiceChoice = new mChoice(sortedmapServices, 0);
	MessageArea desc = new MessageArea(descString);
	desc.modifyAll(ControlConstants.NotEditable | ControlConstants.DisplayOnly | ControlConstants.NoFocus, ControlConstants.TakesKeyFocus);
	desc.borderStyle = UIConstants.BDR_NOBORDER;

	this.addLast(desc);
	this.addLast(mapServiceChoice);

	// tiles panel
	CellPanel pnlTiles = new CellPanel();
	pnlTiles.addNext(forCachesChkBox);
	pnlTiles.addNext(forSelectedChkBox);
	pnlTiles.addLast(cachesLbl);
	pnlTiles.addNext(distLbl = new mLabel(MyLocale.getMsg(1810, "Within a rectangle of:")), DONTSTRETCH, (DONTFILL | WEST));
	distanceInput = new mInput();
	int tmp = Convert.toInt((Global.profile.getDistOC()));
	tmp = java.lang.Math.max(tmp, Convert.toInt((Global.profile.getDistGC())));
	distanceInput.setText(Convert.toString((tmp > 0 ? tmp : 15)));
	pnlTiles.addNext(distanceInput, DONTSTRETCH, (DONTFILL | WEST));
	pnlTiles.addLast(km);
	pnlTiles.addNext(coosLbl = new mLabel(MyLocale.getMsg(1811, "around the centre:") + " "));
	pnlTiles.addLast(coosBtn = new mButton(center.toString()));
	pnlTiles.addNext(scaleLbl);

	mapLoader.setCurrentMapService(sortingMapServices[mapServiceChoice.selectedIndex]);

	this.focusFirst();
	pnlTiles.addLast(scaleInput);
	pnlTiles.addNext(overlappingLbl);
	pnlTiles.addLast(overlappingInput);
	fetchOnlyMapWithCacheChkBox.setState(false);
	pnlTiles.addLast(fetchOnlyMapWithCacheChkBox);
	pnlTiles.addNext(new mLabel(MyLocale.getMsg(1835, "Tilesize")), DONTSTRETCH, DONTFILL | WEST);
	pnlTilestileHeightInput.columns = pnlTilestileWidthInput.columns = 5; // 5 Zeichen vorsehen
	CellPanel p03 = new CellPanel();
	p03.addNext(pnlTilestileWidthInput, DONTSTRETCH, CENTER);
	p03.addNext(new mLabel("x"), DONTSTRETCH, CENTER);
	p03.addLast(pnlTilestileHeightInput, DONTSTRETCH, CENTER);
	pnlTiles.addLast(p03, HSTRETCH, HFILL);

	executePanel = new ExecutePanel(pnlTiles);
	executePanel.setText(MyLocale.getMsg(162, "Download"), FormBase.YESB);
	executePanel.setText(MyLocale.getMsg(4107, "Done"), FormBase.CANCELB);

	updateForCachesState();
	mTab.addCard(pnlTiles, MyLocale.getMsg(1812, "Tiles"), MyLocale.getMsg(1812, "Tiles"));

	// per cache panel
	CellPanel perCacheCard = new CellPanel();
	CellPanel pccSelectionPanel = new CellPanel();
	CellPanel p1 = new CellPanel();
	pccSelectionPanel.addNext(new mLabel(MyLocale.getMsg(1813, "Download one map for")), VSTRETCH, LEFT);
	p1.addNext(forSelectedChkBoxPerCache);
	p1.addLast(new mLabel(MyLocale.getMsg(1806, "caches")));
	pccSelectionPanel.addLast(p1, HSTRETCH, HFILL);

	CellPanel p2 = new CellPanel();
	pccSelectionPanel.addNext(scaleLblPerCache, VSTRETCH, LEFT);
	p2.addLast(scaleInputPerCache);
	pccSelectionPanel.addLast(p2, HSTRETCH, HFILL);

	CellPanel p3 = new CellPanel();
	tileHeightInput.columns = tileWidthInput.columns = 5; // 5 Zeichen vorsehen
	pccSelectionPanel.addNext(new mLabel(MyLocale.getMsg(1835, "Tilesize")), VSTRETCH, LEFT);
	p3.addNext(tileWidthInput, DONTSTRETCH, CENTER);
	p3.addNext(new mLabel("x"), DONTSTRETCH, CENTER);
	p3.addLast(tileHeightInput, DONTSTRETCH, CENTER);
	pccSelectionPanel.addLast(p3, HSTRETCH, HFILL);

	perCacheCard.addLast(pccSelectionPanel, STRETCH, FILL);
	executePanelPerCache = new ExecutePanel(perCacheCard);
	executePanelPerCache.setText(MyLocale.getMsg(162, "Download"), FormBase.YESB);
	executePanelPerCache.setText(MyLocale.getMsg(4107, "Done"), FormBase.CANCELB);

	mTab.addCard(perCacheCard, MyLocale.getMsg(1814, "Per cache"), MyLocale.getMsg(1813, "Per Cache"));
	setRecommScaleInput();
	setRecommPixelSize();
	this.addLast(mTab);
	isCreated = true;
    }

    private void checkTileSizeInputfields(String w, String h) {
	int iw = Common.parseInt(w);
	int ih = Common.parseInt(h);
	if (iw < mapLoader.getCurrentOnlineMapService().minPixelSize)
	    iw = mapLoader.getCurrentOnlineMapService().minPixelSize;
	if (ih < mapLoader.getCurrentOnlineMapService().minPixelSize)
	    ih = mapLoader.getCurrentOnlineMapService().minPixelSize;
	if (iw > mapLoader.getCurrentOnlineMapService().maxPixelSize)
	    iw = mapLoader.getCurrentOnlineMapService().maxPixelSize;
	if (ih > mapLoader.getCurrentOnlineMapService().maxPixelSize)
	    ih = mapLoader.getCurrentOnlineMapService().maxPixelSize;
	tileWidthInput.setText(Integer.toString(iw));
	tileHeightInput.setText(Integer.toString(ih));
	pnlTilestileWidthInput.setText(Integer.toString(iw));
	pnlTilestileHeightInput.setText(Integer.toString(ih));
	Global.pref.tileheight = ih;
	Global.pref.tilewidth = iw;
    }

    private void checkTileWidthInput(String w) {
	int iw = Common.parseInt(w);
	if (iw < mapLoader.getCurrentOnlineMapService().minPixelSize)
	    iw = mapLoader.getCurrentOnlineMapService().minPixelSize;
	if (iw > mapLoader.getCurrentOnlineMapService().maxPixelSize)
	    iw = mapLoader.getCurrentOnlineMapService().maxPixelSize;
	tileWidthInput.setText(Integer.toString(iw));
	pnlTilestileWidthInput.setText(Integer.toString(iw));
	Global.pref.tilewidth = iw;
    }

    private void checkTileHeightInput(String h) {
	int ih = Common.parseInt(h);
	if (ih < mapLoader.getCurrentOnlineMapService().minPixelSize)
	    ih = mapLoader.getCurrentOnlineMapService().minPixelSize;
	if (ih > mapLoader.getCurrentOnlineMapService().maxPixelSize)
	    ih = mapLoader.getCurrentOnlineMapService().maxPixelSize;
	tileHeightInput.setText(Integer.toString(ih));
	pnlTilestileHeightInput.setText(Integer.toString(ih));
	Global.pref.tileheight = ih;
    }

    private void setRecommScaleInput() {
	String[] recScales = new String[mapLoader.getCurrentOnlineMapService().recommendedScales.length];
	for (int i = 0; i < recScales.length; i++) {
	    recScales[i] = Convert.toString(mapLoader.getCurrentOnlineMapService().recommendedScales[i]);
	}
	scaleInput.choice.set(recScales, mapLoader.getCurrentOnlineMapService().preselectedRecScaleIndex);
	scaleInputPerCache.choice.set(recScales, mapLoader.getCurrentOnlineMapService().preselectedRecScaleIndex);
	scaleInput.setText(recScales[mapLoader.getCurrentOnlineMapService().preselectedRecScaleIndex]);
	scaleInputPerCache.setText(recScales[mapLoader.getCurrentOnlineMapService().preselectedRecScaleIndex]);
    }

    private float checkScale(double scale) {

	double minScale = ((WebMapService) mapLoader.getCurrentOnlineMapService()).minscaleWMS;
	double maxScale = ((WebMapService) mapLoader.getCurrentOnlineMapService()).maxscaleWMS;

	if (scale < minScale || scale > maxScale) {
	    if (scale < minScale) {
		scale = minScale;
	    } else {
		scale = maxScale;
	    }
	    new InfoBox(MyLocale.getMsg(5500, "Error"), "! " + scale + "\n" + MyLocale.getMsg(1830, "The selected online map service provides map in the scale from") + " " + minScale + " " + MyLocale.getMsg(1831, " to") + " " + maxScale + "\n "
		    + MyLocale.getMsg(1832, "\n please adjust 'Approx. meter pro pixel' accordingly")).wait(FormBase.OKB);
	}
	scaleInput.setText(Convert.toString(scale));
	scaleInputPerCache.setText(Convert.toString(scale));
	return (float) scale;
    }

    /**
     * sort the map services in order to have the services, which cover the current center first in the list this sets inbound[], sortedMapServices[] and sortingmapServices[]
     * 
     */
    private void sortMapServices() {
	sortingMapServices = new int[unsortedMapServices.length + 1];
	inbound = new boolean[unsortedMapServices.length];
	int j = 0;
	for (int i = 0; i < sortingMapServices.length - 1; i++) {
	    if (((OnlineMapService) mapLoader.getOnlineMapServices().get(i)).boundingBox.isInBound(center)) {
		sortingMapServices[j] = i;
		j++;
		inbound[i] = true;
	    } else
		inbound[i] = false;
	}
	int k = j;
	sortedmapServices = new String[unsortedMapServices.length + 1];
	for (int i = 0; i < sortedmapServices.length - 1; i++) {
	    if (!inbound[i]) {
		sortingMapServices[j] = i;
		j++;
	    }
	    sortedmapServices[i] = ((OnlineMapService) mapLoader.getOnlineMapServices().get(sortingMapServices[i])).getName();
	}
	sortedmapServices[j] = sortedmapServices[k];
	sortedmapServices[k] = "===== ===== ===== ===== ===== ===== =====";
	sortingMapServices[j] = sortingMapServices[k];
	sortingMapServices[k] = -1;
    }

    private int getSortedMapServiceIndex(int originalindex) {
	for (int i = 0; i < sortingMapServices.length; i++) {
	    if (sortingMapServices[i] == originalindex)
		return i;
	}
	throw new IllegalStateException(MyLocale.getMsg(1818, "getSortedMapServiceIndex: index") + " " + originalindex + MyLocale.getMsg(1819, "not found"));
    }

    private String getMapsDir() {
	String mapsDir = Global.profile.getMapsDir() + Common.ClearForFileName(mapLoader.getCurrentOnlineMapService().getMapType()) + "/";
	// check and create mapsDir
	if (!(new File(mapsDir).isDirectory())) {
	    if (new File(mapsDir).mkdirs() == false) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(172, "Error: cannot create maps directory: \n") + new File(mapsDir).getParentFile()).wait(FormBase.OKB);
		return null;
	    }
	}
	return mapsDir;
    }

    private void download() {
	String mapsDir = getMapsDir();
	if (mapsDir == null)
	    return;

	InfoBox progressBox = new InfoBox(MyLocale.getMsg(1815, "Downloading georeferenced maps"), MyLocale.getMsg(1816, "Downloading georeferenced maps\n \n \n \n \n"), InfoBox.PROGRESS_WITH_WARNINGS);
	progressBox.setButtonText(MyLocale.getMsg(111, "Exit"), FormBase.YESB);
	progressBox.disableButton(FormBase.YESB);
	progressBox.exec();
	mapLoader.setProgressInfoBox(progressBox);
	Vm.showWait(true);

	Point TileSizeInPixels = new Point(Global.pref.tilewidth, Global.pref.tileheight);
	if (forCachesChkBox.getState() || perCache) {
	    // calculate map boundaries from cacheDB
	    Area surArea = Global.profile.getSourroundingArea(onlySelected);
	    if (surArea == null) {
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1817, "No Caches are selected")).wait(FormBase.OKB);
		Vm.showWait(false);
		progressBox.close(0);
		return;
	    }
	    mapLoader.setTopleft(surArea.topleft);
	    mapLoader.setBottomright(surArea.bottomright);
	    mapLoader.setTiles(scale, TileSizeInPixels, overlapping);
	} else {
	    // calculate from centre point an radius
	    mapLoader.setTiles(center, radius * 1000, scale, TileSizeInPixels, overlapping);
	}

	// to get Cookies not delivered from gettings maps (DE Dop-Viewer)
	try {
	    String specialUrl = mapLoader.getCurrentOnlineMapService().specialUrl;
	    if (specialUrl.length() > 0) {
		UrlFetcher.fetch(specialUrl);
		UrlFetcher.rememberCookies();
	    }
	} catch (IOException e1) {
	}
	// download map(s)
	if (!perCache) {
	    mapLoader.setProgressInfoBox(progressBox);
	    mapLoader.setFetchOnlyMapWithCache(fetchOnlyMapWithCacheChkBox.getState());
	    if (mapLoader.getNumMapsY() == 0) {
		mapLoader.downloadMap(mapLoader.getTopleft(), scale, TileSizeInPixels, mapsDir);
	    } else {
		mapLoader.downloadMaps(mapsDir);
	    }
	} else {
	    CacheHolder ch;
	    int numdownloaded = 0;
	    Global.profile.getSourroundingArea(onlySelected);
	    int numCaches = Global.profile.numCachesInArea;
	    for (int i = cacheDB.size() - 1; i >= 0; i--) {
		if (!progressBox.isClosed()) {
		    ch = cacheDB.get(i);
		    if (!this.onlySelected || ch.is_Checked) {
			// TODO != 0 sollte verschwinden, sobald das handling von nicht gesetzten Koos überall korrekt ist
			if (ch.getPos().isValid() && ch.getPos().latDec != 0 && ch.getPos().lonDec != 0) {
			    numdownloaded++;
			    progressBox.setInfo(MyLocale.getMsg(1820, "Downloading map '") + mapLoader.getCurrentOnlineMapService().getName() + "'\n" + numdownloaded + " / " + numCaches + MyLocale.getMsg(1821, "\n for cache:\n") + ch.getCacheName());
			    try {
				mapLoader.downloadMap(ch.getPos(), scale, TileSizeInPixels, mapsDir);
			    } catch (Exception e) {
				progressBox.addWarning(MyLocale.getMsg(1822, "Cache:") + " " + ch.getCacheName() + "(" + ch.getWayPoint() + ") " + MyLocale.getMsg(1823, "Ignoring error:") + " " + e.getMessage() + "\n");
			    }
			}
		    }
		}
	    }
	}
	if (!progressBox.isClosed()) {
	    if (Global.mainTab.movingMap != null)
		Global.mainTab.movingMap.setMapsloaded(false);
	    // rebuild MapsList.txt
	    progressBox.setInfo("rebuild MapsList.txt");
	    File MapsListFile = new File(Global.profile.getMapsDir() + "/MapsList.txt");
	    MapsListFile.delete();
	    MapsList maps = new MapsList(center.latDec);
	    maps.clear();
	    maps = null;
	    progressBox.addWarning(MyLocale.getMsg(1826, "Finished downloading and calibration of maps"));
	    progressBox.disableButton(FormBase.CANCELB);
	    progressBox.enableButton(FormBase.YESB);
	    Vm.showWait(false);
	    progressBox.waitUntilClosed();
	}
	Vm.showWait(false);
	mapLoader.setProgressInfoBox(null);
    }

    private void updateForCachesState() {
	int a, b;
	if (forCachesChkBox.getState()) {
	    // create map rectangle from caches
	    a = 0;
	    b = ControlConstants.Disabled;
	} else {
	    // use centre and distance input
	    a = ControlConstants.Disabled;
	    b = 0;
	}
	forSelectedChkBox.modify(a, b);
	cachesLbl.modify(a, b);
	distanceInput.modify(b, a);
	distLbl.modify(b, a);
	coosBtn.modify(b, a);
	coosLbl.modify(b, a);
	km.modify(b, a);
	repaintNow();

    }

    public void onEvent(Event ev) {
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == executePanel.cancelButton || ev.target == executePanelPerCache.cancelButton) {
		this.close(FormBase.IDCANCEL);
	    } else if (ev.target == executePanel.applyButton || ev.target == executePanelPerCache.applyButton) {
		if (sortingMapServices[mapServiceChoice.selectedIndex] == -1) {
		    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1833, "Please don't select the separator line in the wms service option")).wait(FormBase.OKB);
		}
		mapLoader.setCurrentMapService(sortingMapServices[mapServiceChoice.selectedIndex]);
		if (ev.target == executePanel.applyButton) { // get tiles
		    this.checkTileSizeInputfields(tileWidthInput.getText(), tileHeightInput.getText());
		    perCache = false;
		    if (forSelectedChkBox.getSelectedItem().toString().equalsIgnoreCase(MyLocale.getMsg(1804, "all")))
			onlySelected = false;
		    else
			onlySelected = true;
		    radius = (float) Common.parseDouble(distanceInput.getText());

		    scale = checkScale(Common.parseDouble(scaleInput.getText()));
		    overlapping = Convert.toInt(overlappingInput.getText());
		    if (!forCachesChkBox.getState()) {
			if (radius <= 0) {
			    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1827, "'radius' must be greater than 0")).wait(FormBase.OKB);
			    return;
			}
			if (overlapping < 0) {
			    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1828, "'overlapping' must be greater or equal 0")).wait(FormBase.OKB);
			    return;
			}
			if (!center.isValid() && !forCachesChkBox.getState()) {
			    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1829, "Please enter the 'centre' around which the maps shall be downloaded")).wait(FormBase.OKB);
			    return;
			}
		    }
		} else {
		    // per cache
		    this.checkTileSizeInputfields(tileWidthInput.getText(), tileHeightInput.getText());
		    perCache = true;
		    if (forSelectedChkBoxPerCache.getSelectedItem().toString().equalsIgnoreCase(MyLocale.getMsg(1804, "all")))
			onlySelected = false;
		    else
			onlySelected = true;

		    scale = checkScale(CacheWolf.utils.Common.parseDouble(scaleInputPerCache.getText()));

		}

		// this.close(FormBase.IDOK);
		this.download();
	    } else if (ev.target == coosBtn) {
		CoordsScreen cs = new CoordsScreen();
		cs.setFields(center, TransformCoordinates.CW);
		if (cs.execute() != FormBase.IDCANCEL) {
		    center = cs.getCoords();
		    coosBtn.setText(center.toString());
		    int tmp = sortingMapServices[mapServiceChoice.selectedIndex];
		    sortMapServices();
		    mapServiceChoice.set(sortedmapServices, (!inbound[tmp] ? 0 : getSortedMapServiceIndex((tmp))));
		}
	    } else if (ev.target == forCachesChkBox) {
		updateForCachesState();
	    }

	} // end of "if controllEvent..."
	if (ev instanceof DataChangeEvent) {
	    if (ev.target == mapServiceChoice) {
		mapLoader.setCurrentMapService(sortingMapServices[mapServiceChoice.selectedIndex]);
		setRecommScaleInput();
		setRecommPixelSize();
		if (mapLoader.getCurrentOnlineMapService() instanceof WebMapService) {
		    // es gibt keinen anderen mehr (bis ihn jemand wieder einbaut)
		    WebMapService wms = (WebMapService) mapLoader.getCurrentOnlineMapService();
		    if (wms.requestUrlPart.equalsIgnoreCase("Tile")) {
			// Text nach Zoom ändern
			this.scaleLbl.text = "Zoom";
			this.scaleLblPerCache.text = "Zoom";
		    } else {
			this.scaleLbl.text = MyLocale.getMsg(1807, "Approx. m per pixel:");
			this.scaleLblPerCache.text = MyLocale.getMsg(1807, "Approx. m per pixel:");
		    }
		    this.repaint();
		}
	    } else if (ev.target == tileWidthInput) {
		this.checkTileWidthInput(tileWidthInput.getText());
	    } else if (ev.target == tileHeightInput) {
		this.checkTileHeightInput(tileHeightInput.getText());
	    } else if (ev.target == pnlTilestileWidthInput) {
		this.checkTileWidthInput(pnlTilestileWidthInput.getText());
	    } else if (ev.target == pnlTilestileHeightInput) {
		this.checkTileHeightInput(pnlTilestileHeightInput.getText());
	    } else if (ev.target == scaleInput) {
		scale = checkScale(Common.parseDouble(scaleInput.getText()));
	    } else if (ev.target == scaleInputPerCache) {
		scale = checkScale(Common.parseDouble(scaleInputPerCache.getText()));
	    } else if (ev.target == overlappingInput) {
		Global.pref.mapOverlapping = Convert.toInt(overlappingInput.getText());
	    }
	}
	super.onEvent(ev);
    }

    private void setRecommPixelSize() {
	tileWidthInput.setText(mapLoader.getCurrentOnlineMapService().prefWidthPixelSize);
	tileHeightInput.setText(mapLoader.getCurrentOnlineMapService().prefHeightPixelSize);
	pnlTilestileWidthInput.setText(mapLoader.getCurrentOnlineMapService().prefWidthPixelSize);
	pnlTilestileHeightInput.setText(mapLoader.getCurrentOnlineMapService().prefHeightPixelSize);

    }
}
