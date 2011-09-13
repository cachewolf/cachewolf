/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://developer.berlios.de/projects/cachewolf/
for more information.
Contact: 	bilbowolf@users.berlios.de
			kalli@users.berlios.de

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
import CacheWolf.CoordsScreen;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import ewe.fx.Point;
import ewe.io.FileBase;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.CheckBoxGroup;
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
 *         is called from
 *         * start offset for language file: 1800
 */

public class MapLoaderGui extends Form {
	mButton cancelB, okBtiles, okBPerCache, cancelBPerCache;
	Preferences pref = Global.getPref();
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
	mComboBox scaleInput = new mComboBox();
	mComboBox scaleInputPerCache = new mComboBox();
	mLabel overlappingLbl = new mLabel(MyLocale.getMsg(1808, "overlapping in pixel:"));
	mInput overlappingInput = new mInput("" + pref.mapOverlapping);
	mCheckBox overviewChkBox = new mCheckBox(MyLocale.getMsg(1809, "download an overview map"));
	mCheckBox fetchOnlyMapWithCacheChkBox = new mCheckBox(MyLocale.getMsg(165, "only for caches"));
	mCheckBox overviewChkBoxPerCache = new mCheckBox(MyLocale.getMsg(1809, "download an overview map"));
	mCheckBox smallTiles = new mCheckBox(MyLocale.getMsg(4280, "Small Tiles"));
	mCheckBox bigTiles = new mCheckBox(MyLocale.getMsg(4282, "BigTiles"));
	CheckBoxGroup tileSize = new CheckBoxGroup();

	/**
	 * Inputfields for width an height of tile size
	 */
	private mInput tileWidthInput = new mInput();
	private mInput tileHeightInput = new mInput();
	MapLoader mapLoader;
	String[] unsortedMapServices;
	String[] sortedmapServices;
	int[] sortingMapServices;
	boolean[] inbound;
	CWPoint center;
	CacheDB cacheDB;
	boolean perCache;
	boolean onlySelected;
	float radius;
	float scale;
	int overlapping;
	boolean overviewmap;
	/**
	 * Determines width and height of tiles for `per cache maps'
	 */
	private int tileWidth;
	private int tileHeight;

	public MapLoaderGui(CacheDB cacheDBi) {
		super();
		this.title = MyLocale.getMsg(1800, "Download georeferenced maps");
		center = new CWPoint(pref.getCurCentrePt());
		tileWidth = pref.tilewidth;
		tileHeight = pref.tileheight;
		if (tileWidth == 0 || tileHeight == 0) {
			tileWidth = pref.myAppWidth > 200 ? pref.myAppWidth : 200;
			tileHeight = pref.myAppHeight > 175 ? pref.myAppHeight : 175;
		}
		pref.tilewidth = tileWidth;
		pref.tileheight = tileHeight;
		// ist das wirklich so gewollt?
		pref.tilewidth = 1000;
		pref.tileheight = 1000;
		initTileInputfields();
		cacheDB = cacheDBi;
		mapLoader = new MapLoader(FileBase.getProgramDirectory() + "/" + "webmapservices");

		// sort the items in the list of services in a way that services which cover the current center point.
		unsortedMapServices = mapLoader.getAvailableOnlineMapServices();
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
		int tmp = Convert.toInt((Global.getProfile().getDistOC()));
		tmp = java.lang.Math.max(tmp, Convert.toInt((Global.getProfile().getDistGC())));
		distanceInput.setText(Convert.toString((tmp > 0 ? tmp : 15)));
		pnlTiles.addNext(distanceInput, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		pnlTiles.addLast(km);
		pnlTiles.addNext(coosLbl = new mLabel(MyLocale.getMsg(1811, "around the centre:") + " "));
		pnlTiles.addLast(coosBtn = new mButton(center.toString()));
		pnlTiles.addNext(scaleLbl);
		mapLoader.setCurrentMapService(sortingMapServices[mapServiceChoice.selectedIndex]);
		this.focusFirst();
		pnlTiles.addLast(scaleInput);
		// pnlTiles.addLast(resolutionLbl);
		pnlTiles.addNext(overlappingLbl);
		pnlTiles.addLast(overlappingInput);
		overviewChkBox.setState(false);
		pnlTiles.addNext(overviewChkBox);
		fetchOnlyMapWithCacheChkBox.setState(false);
		pnlTiles.addLast(fetchOnlyMapWithCacheChkBox);
		pnlTiles.addNext(smallTiles);
		pnlTiles.addLast(bigTiles);
		smallTiles.setGroup(tileSize);
		bigTiles.setGroup(tileSize);
		tileSize.selectIndex(pref.mapTileSize);
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
		pnlPerCache.addNext(new mLabel(MyLocale.getMsg(1807, "Approx. m per pixel")), CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
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
	}

	private void initTileInputfields() {
		tileWidthInput.setText(Integer.toString(pref.tilewidth));
		tileHeightInput.setText(Integer.toString(pref.tileheight));
	}

	private void setRecommScaleInput() {
		String[] recScales = new String[mapLoader.currentOnlineMapService.recommendedScales.length];
		for (int i = 0; i < recScales.length; i++) {
			recScales[i] = Convert.toString(mapLoader.currentOnlineMapService.recommendedScales[i]);
		}
		scaleInput.choice.set(recScales, mapLoader.currentOnlineMapService.preselectedRecScaleIndex);
		scaleInputPerCache.choice.set(recScales, mapLoader.currentOnlineMapService.preselectedRecScaleIndex);
		scaleInput.setText(recScales[mapLoader.currentOnlineMapService.preselectedRecScaleIndex]);
		scaleInputPerCache.setText(recScales[mapLoader.currentOnlineMapService.preselectedRecScaleIndex]);
	}

	/**
	 * sort the map services in order to have the services, which cover
	 * the current center first in the list
	 * this sets inbound[], sortedMapServices[] and sortingmapServices[]
	 * 
	 */
	private void sortMapServices() {
		sortingMapServices = new int[unsortedMapServices.length + 1];
		inbound = new boolean[unsortedMapServices.length];
		int j = 0;
		for (int i = 0; i < sortingMapServices.length - 1; i++) {
			if (((OnlineMapService) mapLoader.onlineMapServices.get(i)).boundingBox.isInBound(center)) {
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
			sortedmapServices[i] = ((OnlineMapService) mapLoader.onlineMapServices.get(sortingMapServices[i])).getName();
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

	public String getMapsDir() {
		String ret = Global.getPref().getMapDownloadSavePath(mapLoader.currentOnlineMapService.getMapType());
		Global.getPref().saveCustomMapsPath(getLeadingPath(ret, Global.getPref().getMapLoadPath()));
		// Global.getPref().saveCustomMapsPath(ret);
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
			} else {
				break;
			}
		} while (StartPos < LastPos);
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
		int length;
		switch (tileSize.getSelectedIndex()) {
		// Perhaps introduce a medium size??
		case 0:
			length = 500;
			break;
		default:
			length = 1000;
		}
		// Override size if one tile for each cache is wanted
		Point size = new Point(length, length);
		if (perCache) {
			length = 1000;
			size = new Point(tileWidth, tileHeight);
		}
		if (forCachesChkBox.getState() || perCache) {
			Area surArea = Global.getProfile().getSourroundingArea(onlySelected); // calculate map boundaries from cacheDB
			if (surArea == null) {
				(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1817, "No Caches are selected"), FormBase.OKB)).execute();
				Vm.showWait(false);
				progressBox.close(0);
				return;
			}
			mapLoader.setTiles(surArea.topleft, surArea.bottomright, scale, size, overlapping);
		} else { // calculate from centre point an radius
			mapLoader.setTiles(center, radius * 1000, scale, size, overlapping);
		}
		if (overviewmap) {
			progressBox.setInfo(MyLocale.getMsg(1824, "downloading overview map"));
			float overviewScale = MapLoader.getScale(center, radius * 1000, size);
			try {
				mapLoader.downloadMap(center, overviewScale, size, mapsDir);
			} catch (Exception e) {
				progressBox.addWarning(MyLocale.getMsg(1825, "Overview map: Ignoring error:") + " " + e.getMessage() + "\n");
			}
		}
		if (!perCache) { // download tiles
			mapLoader.setProgressInfoBox(progressBox);
			mapLoader.setFetchOnlyMapWithCache(fetchOnlyMapWithCacheChkBox.getState());
			mapLoader.downlaodTiles(mapsDir);
		} else { // per cache
			CacheHolder ch;
			int numdownloaded = 0;
			Global.getProfile().getSourroundingArea(onlySelected); // calculate numCachesInArea
			int numCaches = Global.getProfile().numCachesInArea;
			for (int i = cacheDB.size() - 1; i >= 0; i--) {
				ch = cacheDB.get(i);
				if (!this.onlySelected || ch.is_Checked) {
					if (ch.getPos().isValid() && ch.getPos().latDec != 0 && ch.getPos().lonDec != 0) { // TODO != 0 sollte verschwinden, sobald das handling von nicht gesetzten Koos überall korrekt ist
						numdownloaded++;
						progressBox.setInfo(MyLocale.getMsg(1820, "Downloading map '") + mapLoader.currentOnlineMapService.getName() + "'\n" + numdownloaded + " / " + numCaches + MyLocale.getMsg(1821, "\n for cache:\n") + ch.getCacheName());
						try {
							mapLoader.downloadMap(ch.getPos(), scale, size, mapsDir);
						} catch (Exception e) {
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
		// progressBox.close(0);
		if (Global.mainTab.mm != null)
			Global.mainTab.mm.mapsloaded = false;
		// (new MessageBox("Download maps", "Downloaded and calibrated the maps successfully", MessageBox.OKB)).execute();
	}

	private void updateForCachesState() {
		int a, b;
		if (forCachesChkBox.getState()) {
			// create map rectangle from caches
			a = 0;
			b = ControlConstants.Disabled;
		} else { // use centre and distance input
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
			} else if (ev.target == okBtiles || ev.target == okBPerCache) {
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
					overviewmap = overviewChkBox.getState();
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
				} else { // per cache
					perCache = true;
					if (forSelectedChkBoxPerCache.getSelectedItem().toString().equalsIgnoreCase(MyLocale.getMsg(1804, "all")))
						onlySelected = false;
					else
						onlySelected = true;
					overviewmap = overviewChkBoxPerCache.getState();
					scale = (float) CacheWolf.Common.parseDouble(scaleInputPerCache.getText());
					tileWidth = CacheWolf.Common.parseInt(tileWidthInput.getText());
					tileHeight = CacheWolf.Common.parseInt(tileHeightInput.getText());
					if (tileWidth <= 0) {
						new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1836, "Width of tiles must be greater 0."), FormBase.OKB).execute();
						initTileInputfields();
						return;
					}
					if (tileHeight <= 0) {
						new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1837, "Height of tiles must be greater 0."), FormBase.OKB).execute();
						initTileInputfields();
						return;
					}
					// If width and height has been valid, set them in preferences also:
					pref.tileheight = tileHeight;
					pref.tilewidth = tileWidth;
				}
				if (scale < mapLoader.currentOnlineMapService.minscale || scale > mapLoader.currentOnlineMapService.maxscale) {
					if (scale < mapLoader.currentOnlineMapService.minscale) {
						scaleInput.setText(Convert.toString(mapLoader.currentOnlineMapService.minscale));
						scaleInputPerCache.setText(Convert.toString(java.lang.Math.ceil(mapLoader.currentOnlineMapService.minscale * 100) / 100));
					} else {
						scaleInput.setText(Convert.toString(java.lang.Math.floor(mapLoader.currentOnlineMapService.maxscale * 100) / 100));
						scaleInputPerCache.setText(Convert.toString(mapLoader.currentOnlineMapService.maxscale));
					}
					(new MessageBox(MyLocale.getMsg(321, "Error"), "! " + scale + "\n" + MyLocale.getMsg(1830, "The selected online map service provides map in the scale from") + " " + mapLoader.currentOnlineMapService.minscale
							+ MyLocale.getMsg(1831, " to") + " " + mapLoader.currentOnlineMapService.maxscale + MyLocale.getMsg(1832, "\n please adjust 'Approx. meter pro pixel' accordingly"), FormBase.OKB)).execute();
					return;
				}
				this.close(FormBase.IDOK);
				this.downloadTiles();
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
			} else if (ev.target == tileSize) {
				switch (tileSize.getSelectedIndex()) {
				case 0:
					overlappingInput.setText("10");
					pref.mapTileSize = 0;
					pref.mapOverlapping = 10;
					break;
				default:
					overlappingInput.setText("100");
					pref.mapTileSize = 1;
					pref.mapOverlapping = 100;
				}
			}
		} // end of "if controllEvent..."
		if (ev instanceof DataChangeEvent) {
			if (ev.target == mapServiceChoice) {
				mapLoader.setCurrentMapService(sortingMapServices[mapServiceChoice.selectedIndex]);
				setRecommScaleInput();
			} else if (ev.target == overlappingInput) {
				pref.mapOverlapping = Convert.toInt(overlappingInput.getText());
			}
		}
		super.onEvent(ev);
	}
}
