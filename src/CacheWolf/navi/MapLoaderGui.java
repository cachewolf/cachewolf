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
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.Common;
import CacheWolf.CoordsScreen;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.MyLocale;
import CacheWolf.utils.FileBugfix;
import ewe.fx.Point;
import ewe.io.FileBase;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.DataChangeEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.IKeys;
import ewe.ui.MessageArea;
import ewe.ui.MessageBox;
import ewe.ui.UIConstants;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mComboBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.mTabbedPanel;

/**
 * @author pfeffer
 *         This Class is the Dialog for Download calibrated from expedia.com
 *         start offset for language file: 1800
 */

public class MapLoaderGui extends Form {
	mButton cancelB, okBtiles, okBPerCache, cancelBPerCache;
	mTabbedPanel mTab = new mTabbedPanel();
	CellPanel pnlTiles = new CellPanel();
	CellPanel pnlPerCache = new CellPanel();

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
	mLabel scaleLbl = new mLabel(MyLocale.getMsg(1807, "Approx. m per pixel:"));
	mLabel scaleLblPerCache = new mLabel(MyLocale.getMsg(1807, "Approx. m per pixel:"));
	mComboBox scaleInput = new mComboBox();
	mComboBox scaleInputPerCache = new mComboBox();
	mLabel overlappingLbl = new mLabel(MyLocale.getMsg(1808, "overlapping in pixel:"));
	mInput overlappingInput = new mInput("" + Global.pref.mapOverlapping);
	mCheckBox fetchOnlyMapWithCacheChkBox = new mCheckBox(MyLocale.getMsg(165, "only for caches"));
	mCheckBox smallTiles = new mCheckBox(MyLocale.getMsg(4280, "Small Tiles"));
	mCheckBox bigTiles = new mCheckBox(MyLocale.getMsg(4282, "BigTiles"));

	/**
	 * Inputfields for width an height of tile size
	 */
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
	/**
	 * Determines width and height of tiles for `per cache maps'
	 */
	private int tileWidth;
	private int tileHeight;

	public boolean isCreated;

	public MapLoaderGui(CacheDB cacheDBi) {
		super();
		isCreated = false;
		this.title = MyLocale.getMsg(1800, "Download georeferenced maps");
		center = new CWPoint(Global.pref.getCurCentrePt());
		tileWidth = Global.pref.tilewidth;
		tileHeight = Global.pref.tileheight;
		// minimale Grösse 200 x 175 
		if (tileWidth == 0 || tileHeight == 0) {
			tileWidth = Global.pref.myAppWidth > 200 ? Global.pref.myAppWidth : 200;
			tileHeight = Global.pref.myAppHeight > 175 ? Global.pref.myAppHeight : 175;
		}
		Global.pref.tilewidth = tileWidth;
		Global.pref.tileheight = tileHeight;
		cacheDB = cacheDBi;
		mapLoader = new MapLoader(FileBase.getProgramDirectory() + "/" + "webmapservices");

		// sort the items in the list of services in a way that services which cover the current center point.
		unsortedMapServices = mapLoader.getAvailableOnlineMapServices();
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
		pnlTiles.addNext(forCachesChkBox);
		pnlTiles.addNext(forSelectedChkBox);
		pnlTiles.addLast(cachesLbl);
		pnlTiles.addNext(distLbl = new mLabel(MyLocale.getMsg(1810, "Within a rectangle of:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		distanceInput = new mInput();
		int tmp = Convert.toInt((Global.profile.getDistOC()));
		tmp = java.lang.Math.max(tmp, Convert.toInt((Global.profile.getDistGC())));
		distanceInput.setText(Convert.toString((tmp > 0 ? tmp : 15)));
		pnlTiles.addNext(distanceInput, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		pnlTiles.addLast(km);
		pnlTiles.addNext(coosLbl = new mLabel(MyLocale.getMsg(1811, "around the centre:") + " "));
		pnlTiles.addLast(coosBtn = new mButton(center.toString()));
		pnlTiles.addNext(scaleLbl);
		mapLoader.setCurrentMapService(sortingMapServices[mapServiceChoice.selectedIndex]);

		initTileInputfields();

		this.focusFirst();
		pnlTiles.addLast(scaleInput);
		// pnlTiles.addLast(resolutionLbl);
		pnlTiles.addNext(overlappingLbl);
		pnlTiles.addLast(overlappingInput);
		fetchOnlyMapWithCacheChkBox.setState(false);
		pnlTiles.addLast(fetchOnlyMapWithCacheChkBox);

		/*
		pnlTiles.addNext(smallTiles);
		pnlTiles.addLast(bigTiles);
		smallTiles.setGroup(tileSize);
		bigTiles.setGroup(tileSize);
		tileSize.selectIndex(pref.mapTileSize);
		 */
		pnlTiles.addNext(new mLabel(MyLocale.getMsg(1835, "Tilesize")), CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST);
		pnlTilestileHeightInput.columns = pnlTilestileWidthInput.columns = 5;
		pnlTiles.addNext(pnlTilestileWidthInput, CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST);
		pnlTiles.addNext(new mLabel("x"), CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST);
		pnlTiles.addLast(pnlTilestileHeightInput, CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST);

		cancelB = new mButton(MyLocale.getMsg(1604, "Cancel"));
		cancelB.setHotKey(0, IKeys.ESCAPE);
		pnlTiles.addNext(cancelB, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		okBtiles = new mButton(MyLocale.getMsg(1605, "OK"));
		okBtiles.setHotKey(0, IKeys.ACTION);
		okBtiles.setHotKey(0, IKeys.ENTER);
		pnlTiles.addLast(okBtiles, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		updateForCachesState();
		mTab.addCard(pnlTiles, MyLocale.getMsg(1812, "Tiles"), MyLocale.getMsg(1812, "Tiles"));

		// per cache panel
		pnlPerCache.addNext(new mLabel(MyLocale.getMsg(1813, "Download one map for")), CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		pnlPerCache.addNext(forSelectedChkBoxPerCache, CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		pnlPerCache.addLast(new mLabel(MyLocale.getMsg(1806, "caches")), CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		pnlPerCache.addNext(scaleLblPerCache, CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		pnlPerCache.addLast(scaleInputPerCache, CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST);

		pnlPerCache.addNext(new mLabel(MyLocale.getMsg(1835, "Tilesize")), CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		CellPanel pnl = new CellPanel();
		tileHeightInput.columns = tileWidthInput.columns = 5;
		pnl.addNext(tileWidthInput, CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		pnl.addNext(new mLabel("x"), CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		pnl.addLast(tileHeightInput, CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		pnlPerCache.addLast(pnl, CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST);

		cancelBPerCache = new mButton(MyLocale.getMsg(1604, "Cancel"));
		cancelBPerCache.setHotKey(0, IKeys.ESCAPE);
		pnlPerCache.addNext(cancelBPerCache, CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		okBPerCache = new mButton(MyLocale.getMsg(1605, "OK"));
		okBPerCache.setHotKey(0, IKeys.ACTION);
		okBPerCache.setHotKey(0, IKeys.ENTER);
		pnlPerCache.addLast(okBPerCache, CellConstants.DONTSTRETCH, CellConstants.DONTFILL);

		mTab.addCard(pnlPerCache, MyLocale.getMsg(1814, "Per cache"), MyLocale.getMsg(1813, "Per Cache"));
		setRecommScaleInput();
		this.addLast(mTab);
		isCreated = true;
	}

	private void initTileInputfields() {
		tileWidthInput.setText(Integer.toString(Global.pref.tilewidth));
		tileHeightInput.setText(Integer.toString(Global.pref.tileheight));
		pnlTilestileWidthInput.setText(Integer.toString(Global.pref.tilewidth));
		pnlTilestileHeightInput.setText(Integer.toString(Global.pref.tileheight));
	}

	private void initTileInputfields(int wh) {
		if (wh == Integer.MAX_VALUE)
			wh = 1024;
		tileWidthInput.setText("" + wh);
		tileHeightInput.setText("" + wh);
		pnlTilestileWidthInput.setText("" + wh);
		pnlTilestileHeightInput.setText("" + wh);
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
		Global.pref.tileheight = ih;
		Global.pref.tilewidth = iw;
		initTileInputfields();
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
			}
			else
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
		String ret = Global.pref.getMapDownloadSavePath(mapLoader.getCurrentOnlineMapService().getMapType());
		Global.pref.saveCustomMapsPath(getLeadingPath(ret, Global.pref.getMapLoadPath()));
		// eigentlich dürft das erst gespeichert werden, wenn erfolgreich heruntergeladen wurde
		return ret;
	}

	private String getLeadingPath(String newPath, String oldPath) {
		String LeadingPath = "";
		int StartPos = 0;
		int EndPos;
		int LastPos = java.lang.Math.min(newPath.length(), oldPath.length());
		do {
			EndPos = java.lang.Math.min(newPath.indexOf("/", StartPos), LastPos);
			if (newPath.substring(StartPos, EndPos).equals(oldPath.substring(StartPos, EndPos))) {
				LeadingPath = LeadingPath.concat(newPath.substring(StartPos, EndPos + 1));
				StartPos = EndPos + 1;
			}
			else {
				break;
			}
		}
		while (StartPos < LastPos);
		return LeadingPath;
	}

	public void downloadTiles() {
		String mapsDir = getMapsDir();
		if (mapsDir == null)
			return;

		InfoBox progressBox = new InfoBox(MyLocale.getMsg(1815, "Downloading georeferenced maps"), MyLocale.getMsg(1816, "Downloading georeferenced maps\n \n \n \n \n"), InfoBox.PROGRESS_WITH_WARNINGS);
		progressBox.setPreferredSize(220, 300);
		progressBox.setInfoHeight(160);
		progressBox.relayout(false);
		progressBox.exec();
		mapLoader.setProgressInfoBox(progressBox);
		Vm.showWait(true);

		Point TileSizeInPixels = new Point(Global.pref.tilewidth, Global.pref.tileheight);
		if (forCachesChkBox.getState() || perCache) {
			// calculate map boundaries from cacheDB
			Area surArea = Global.profile.getSourroundingArea(onlySelected);
			if (surArea == null) {
				(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1817, "No Caches are selected"), FormBase.OKB)).execute();
				Vm.showWait(false);
				progressBox.close(0);
				return;
			}
			mapLoader.setTopleft(surArea.topleft);
			mapLoader.setBottomright(surArea.bottomright);
			mapLoader.setTiles(scale, TileSizeInPixels, overlapping);
		}
		else {
			// calculate from centre point an radius
			mapLoader.setTiles(center, radius * 1000, scale, TileSizeInPixels, overlapping);
		}

		// download map(s)
		if (!perCache) {
			mapLoader.setProgressInfoBox(progressBox);
			mapLoader.setFetchOnlyMapWithCache(fetchOnlyMapWithCacheChkBox.getState());
			if (mapLoader.getNumMapsY() == 0) {
				try {
					mapLoader.downloadMap(mapLoader.getTopleft(), scale, TileSizeInPixels, mapsDir);
				}
				catch (Exception e) {
				}
			}
			else {
				mapLoader.downlaodMaps(mapsDir);
			}
		}
		else {
			CacheHolder ch;
			int numdownloaded = 0;
			Global.profile.getSourroundingArea(onlySelected);
			int numCaches = Global.profile.numCachesInArea;
			for (int i = cacheDB.size() - 1; i >= 0; i--) {
				ch = cacheDB.get(i);
				if (!this.onlySelected || ch.is_Checked) {
					// TODO != 0 sollte verschwinden, sobald das handling von nicht gesetzten Koos überall korrekt ist
					if (ch.getPos().isValid() && ch.getPos().latDec != 0 && ch.getPos().lonDec != 0) {
						numdownloaded++;
						progressBox.setInfo(MyLocale.getMsg(1820, "Downloading map '") + mapLoader.getCurrentOnlineMapService().getName() + "'\n" + numdownloaded + " / " + numCaches + MyLocale.getMsg(1821, "\n for cache:\n") + ch.getCacheName());
						try {
							mapLoader.downloadMap(ch.getPos(), scale, TileSizeInPixels, mapsDir);
						}
						catch (Exception e) {
							progressBox.addWarning(MyLocale.getMsg(1822, "Cache:") + " " + ch.getCacheName() + "(" + ch.getWayPoint() + ") " + MyLocale.getMsg(1823, "Ignoring error:") + " " + e.getMessage() + "\n");
						}
					}
				}
			}
		}
		Vm.showWait(false);
		progressBox.addWarning(MyLocale.getMsg(1826, "Finished downloading and calibration of maps"));
		progressBox.addOkButton();
		progressBox.waitUntilClosed();
		mapLoader.setProgressInfoBox(null);
		if (Global.mainTab.mm != null)
			Global.mainTab.mm.setMapsloaded(false);
		// rebuild MapsList.txt
		FileBugfix MapsListFile = new FileBugfix(Global.pref.getCustomMapsPath() + "/MapsList.txt");
		MapsListFile.delete();
		MapsList maps = new MapsList(center.latDec);
		maps.clear();
		maps = null;
	}

	private void updateForCachesState() {
		int a, b;
		if (forCachesChkBox.getState()) {
			// create map rectangle from caches
			a = 0;
			b = ControlConstants.Disabled;
		}
		else { // use centre and distance input
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
			if (ev.target == cancelB || ev.target == cancelBPerCache) {
				this.close(FormBase.IDCANCEL);
			}
			else if (ev.target == okBtiles || ev.target == okBPerCache) {
				if (sortingMapServices[mapServiceChoice.selectedIndex] == -1) {
					(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1833, "Please don't select the separator line in the wms service option"), FormBase.OKB)).execute();
				}
				mapLoader.setCurrentMapService(sortingMapServices[mapServiceChoice.selectedIndex]);
				if (ev.target == okBtiles) { // get tiles
					perCache = false;
					if (forSelectedChkBox.getSelectedItem().toString().equalsIgnoreCase(MyLocale.getMsg(1804, "all")))
						onlySelected = false;
					else
						onlySelected = true;
					radius = (float) CacheWolf.Common.parseDouble(distanceInput.getText());
					scale = (float) CacheWolf.Common.parseDouble(scaleInput.getText());
					overlapping = Convert.toInt(overlappingInput.getText());
					if (!forCachesChkBox.getState()) {
						if (radius <= 0) {
							(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1827, "'radius' must be greater than 0"), FormBase.OKB)).execute();
							return;
						}
						if (overlapping < 0) {
							(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1828, "'overlapping' must be greater or equal 0"), FormBase.OKB)).execute();
							return;
						}
						if (!center.isValid() && !forCachesChkBox.getState()) {
							(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1829, "Please enter the 'centre' around which the maps shall be downloaded"), FormBase.OKB)).execute();
							return;
						}
					}
				}
				else {
					// per cache
					perCache = true;
					if (forSelectedChkBoxPerCache.getSelectedItem().toString().equalsIgnoreCase(MyLocale.getMsg(1804, "all")))
						onlySelected = false;
					else
						onlySelected = true;
					scale = (float) CacheWolf.Common.parseDouble(scaleInputPerCache.getText());

				}

				double minScale = java.lang.Math.ceil(mapLoader.getCurrentOnlineMapService().minscale * 141) / 100;
				double maxScale = java.lang.Math.floor(mapLoader.getCurrentOnlineMapService().maxscale * 142) / 100;

				if (scale < minScale || scale > maxScale) {
					if (scale < minScale) {
						scaleInput.setText(Convert.toString(minScale));
						scaleInputPerCache.setText(Convert.toString(minScale));
					}
					else {
						scaleInput.setText(Convert.toString(maxScale));
						scaleInputPerCache.setText(Convert.toString(maxScale));
					}
					(new MessageBox(MyLocale.getMsg(321, "Error"), "! " + scale + "\n" + MyLocale.getMsg(1830, "The selected online map service provides map in the scale from") + " " + minScale + MyLocale.getMsg(1831, " to") + " " + maxScale
							+ MyLocale.getMsg(1832, "\n please adjust 'Approx. meter pro pixel' accordingly"), FormBase.OKB)).execute();
					return;
				}
				this.close(FormBase.IDOK);
				this.downloadTiles();
			}
			else if (ev.target == coosBtn) {
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(center, TransformCoordinates.CW);
				if (cs.execute() != FormBase.IDCANCEL) {
					center = cs.getCoords();
					coosBtn.setText(center.toString());
					int tmp = sortingMapServices[mapServiceChoice.selectedIndex];
					sortMapServices();
					mapServiceChoice.set(sortedmapServices, (!inbound[tmp] ? 0 : getSortedMapServiceIndex((tmp))));
				}
			}
			else if (ev.target == forCachesChkBox) {
				updateForCachesState();
			}

		} // end of "if controllEvent..."
		if (ev instanceof DataChangeEvent) {
			if (ev.target == mapServiceChoice) {
				mapLoader.setCurrentMapService(sortingMapServices[mapServiceChoice.selectedIndex]);
				setRecommScaleInput();
				initTileInputfields(mapLoader.getCurrentOnlineMapService().maxPixelSize);
				if (mapLoader.getCurrentOnlineMapService() instanceof WebMapService) {
					// es gibt keinen anderen mehr (bis ihn jemand wieder einbaut)
					WebMapService wms = (WebMapService) mapLoader.getCurrentOnlineMapService();
					if (wms.requestUrlPart.equalsIgnoreCase("Tile")) {
						// Text nach Zoom ändern
						this.scaleLbl.text = "Zoom";
						this.scaleLblPerCache.text = "Zoom";
					}
					else {
						this.scaleLbl.text = MyLocale.getMsg(1807, "Approx. m per pixel:");
						this.scaleLblPerCache.text = MyLocale.getMsg(1807, "Approx. m per pixel:");
					}
					this.repaint();
				}
			}
			else if (ev.target == tileWidthInput) {
				this.checkTileSizeInputfields(tileWidthInput.getText(), tileHeightInput.getText());
			}
			else if (ev.target == tileHeightInput) {
				this.checkTileSizeInputfields(tileWidthInput.getText(), tileHeightInput.getText());
			}
			else if (ev.target == pnlTilestileWidthInput) {
				this.checkTileSizeInputfields(pnlTilestileWidthInput.getText(), pnlTilestileHeightInput.getText());
			}
			else if (ev.target == pnlTilestileHeightInput) {
				this.checkTileSizeInputfields(pnlTilestileWidthInput.getText(), pnlTilestileHeightInput.getText());
			}
			else if (ev.target == overlappingInput) {
				Global.pref.mapOverlapping = Convert.toInt(overlappingInput.getText());
			}
		}
		super.onEvent(ev);
	}
}
