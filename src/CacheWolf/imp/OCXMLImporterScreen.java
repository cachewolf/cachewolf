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
package CacheWolf.imp;

import CacheWolf.CacheType;
import CacheWolf.Common;
import CacheWolf.Global;
import CacheWolf.MyLocale;
import CacheWolf.OC;
import CacheWolf.Preferences;
import CacheWolf.imp.SpiderGC.SpiderProperties;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.sys.Convert;
import ewe.ui.CellConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.IKeys;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mInput;
import ewe.ui.mLabel;

/**
 * @author pfeffer
 *         This Class is the Dialog for Download from Opencaching.de
 *         is called from OCXMLImporter
 *         20061209 Bugfix: Checking for uninitialised missingCheckBox
 */
public class OCXMLImporterScreen extends Form {
	mButton cancelB, okB;
	Preferences pref;
	mChoice chcType;
	mInput maxDistanceInput;
	mInput minDistanceInput;
	mInput directionInput;
	mInput maxNumberInput;
	mInput maxNumberUpdates;
	mInput maxLogsInput;
	mCheckBox imagesCheckBox, /* mapsCheckBox, */missingCheckBox, foundCheckBox, travelbugsCheckBox;
	ewe.ui.mChoice domains;
	String fileName;

	mLabel distLbl;
	mLabel maxNumberLbl;
	mLabel distUnit;
	boolean isGC = true;
	public static final int DIST = 1;
	public static final int IMAGES = 2;
	public static final int ALL = 4;
	public static final int INCLUDEFOUND = 8;
	public static final int ISGC = 16;
	public static final int MAXNUMBER = 32;
	public static final int TRAVELBUGS = 64;
	public static final int MAXLOGS = 128;
	public static final int TYPE = 256;
	public static final int HOST = 512;
	public static final int MINDIST = 1024;
	public static final int DIRECTION = 2048;
	public static final int MAXUPDATE = 4096;
	public static final int FILENAME = 8192; // track or route gpx

	public OCXMLImporterScreen(String title, int options) {
		super();
		pref = Global.getPref(); // myPreferences sollte später auch diese Einstellungen speichern

		isGC = ((options & ISGC) > 0);

		this.title = title;

		if ((options & HOST) > 0) {
			domains = new mChoice(OC.OCHostNames(), OC.getSiteIndex(pref.lastOCSite));
			domains.setTextSize(25, 1);
			this.addLast(domains, CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST);
		}

		if ((options & TYPE) > 0) {
			this.addLast(chcType = new mChoice(new String[] { MyLocale.getMsg(1627, "All caches"), MyLocale.getMsg(2, "Tradi"), MyLocale.getMsg(3, "Multi"), MyLocale.getMsg(4, "Virtual"), MyLocale.getMsg(5, "Letterbox"), MyLocale.getMsg(6, "Event"),
					MyLocale.getMsg(14, "Mega Event"), MyLocale.getMsg(11, "Webcam"), MyLocale.getMsg(8, "Mysterie"), MyLocale.getMsg(13, "CITO"), MyLocale.getMsg(18, "Earth"), MyLocale.getMsg(15, "WhereIGo"), }, 0), CellConstants.STRETCH,
					(CellConstants.FILL | CellConstants.WEST));
		}

		if ((options & MINDIST) > 0) {
			this.addNext(distLbl = new mLabel(MyLocale.getMsg(1628, "min. Distance:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			minDistanceInput = new mInput();
			minDistanceInput.setText(Global.getProfile().getMinDistGC());
			this.addNext(minDistanceInput, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			this.addLast(new mLabel(" km/mi."), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		}

		if ((options & DIST) > 0) {
			this.addNext(distLbl = new mLabel(MyLocale.getMsg(1601, "Distance:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			maxDistanceInput = new mInput();
			String dist1;
			String dist2;
			if (isGC) {
				dist1 = Global.getProfile().getDistGC();
				dist2 = Global.getProfile().getDistOC();
			} else {
				dist1 = Global.getProfile().getDistOC();
				dist2 = Global.getProfile().getDistGC();
			}
			if (dist1.equals("") || dist1.equals("0") || dist1.equals("0.0")) {
				dist1 = dist2;
			}
			maxDistanceInput.setText(dist1);
			this.addNext(maxDistanceInput, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			this.addLast(distUnit = new mLabel(" km/mi."), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		}

		if ((options & DIRECTION) > 0) {
			this.addNext(new mLabel(MyLocale.getMsg(1629, "Richtung:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			directionInput = new mInput();
			directionInput.setText(Global.getProfile().getDirectionGC());
			directionInput.toolTip = MyLocale.getMsg(1630, "z.B. leer oder von-bis (Grad)");
			this.addLast(directionInput, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		}

		if ((options & MAXNUMBER) > 0) {
			this.addNext(maxNumberLbl = new mLabel(MyLocale.getMsg(1623, "Max. number:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			maxNumberInput = new mInput();
			if (pref.maxSpiderNumber < 0 || pref.maxSpiderNumber == Integer.MAX_VALUE) {
				maxNumberInput.setText("");
			} else {
				maxNumberInput.setText(Integer.toString(pref.maxSpiderNumber));
			}
			this.addNext(maxNumberInput, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			this.addLast(new mLabel(MyLocale.getMsg(1624, " caches")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		}

		if ((options & MAXUPDATE) > 0) {
			this.addNext(new mLabel(MyLocale.getMsg(1631, "Max. Updates:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			maxNumberUpdates = new mInput();
			maxNumberUpdates.setText("");
			this.addNext(maxNumberUpdates, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			this.addLast(new mLabel(MyLocale.getMsg(1624, " caches")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		}

		if ((options & MAXLOGS) > 0) {
			this.addNext(new mLabel(MyLocale.getMsg(1626, "Max. logs:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
			maxLogsInput = new mInput();
			maxLogsInput.setText(Convert.toString(pref.maxLogsToSpider));
			this.addLast(maxLogsInput, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		}

		if ((options & IMAGES) > 0) {
			imagesCheckBox = new mCheckBox();
			imagesCheckBox.setText(MyLocale.getMsg(1602, "Download Images"));
			imagesCheckBox.setState(pref.downloadPics);
			this.addLast(imagesCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST);
		}

		if ((options & TRAVELBUGS) > 0) {
			travelbugsCheckBox = new mCheckBox();
			travelbugsCheckBox.setText(MyLocale.getMsg(1625, "Download TBs"));
			travelbugsCheckBox.setState(pref.downloadTBs);
			this.addLast(travelbugsCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST);
		}

		if ((options & INCLUDEFOUND) > 0) {
			foundCheckBox = new mCheckBox();
			foundCheckBox.setText(MyLocale.getMsg(1622, "Exclude found caches"));
			foundCheckBox.setState(pref.doNotGetFound);
			this.addLast(foundCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST);
		}

		if ((options & ALL) > 0) {
			missingCheckBox = new mCheckBox();
			missingCheckBox.setText(MyLocale.getMsg(1606, "Alle erneut downloaden"));
			missingCheckBox.setState(pref.downloadAllOC);
			this.addLast(missingCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST);
		}

		if ((options & FILENAME) > 0) {
			String dir = pref.getImporterPath("LocGpxImporter");
			FileChooser fc = new FileChooser(FileChooserBase.OPEN, dir);
			fc.addMask("*.gpx");
			fc.setTitle(MyLocale.getMsg(909, "Select file(s)"));
			if (fc.execute() != FormBase.IDCANCEL) {
				dir = fc.getChosenDirectory().toString();
				pref.setImporterPath("LocGpxImporter", dir);
				// String files[] = fc.getAllChosen();
				fileName = fc.file;
			} else {
				fileName = "";
			}
		}

		cancelB = new mButton(MyLocale.getMsg(1604, "Cancel"));
		cancelB.setHotKey(0, IKeys.ESCAPE);
		this.addNext(cancelB, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		okB = new mButton(MyLocale.getMsg(1605, "OK"));
		okB.setHotKey(0, IKeys.ACTION);
		okB.setHotKey(0, IKeys.ENTER);
		this.addLast(okB, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
	}

	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == cancelB) {
				this.close(FormBase.IDCANCEL);
			}
			if (ev.target == okB) {
				// distOC wird hier noch nicht in Pref eingetragen, damit noch geprüft werden kann, ob es größer oder kleiner ist als vorher
				if (missingCheckBox != null)
					pref.downloadAllOC = missingCheckBox.state;
				if (imagesCheckBox != null)
					pref.downloadPics = imagesCheckBox.state;
				if (travelbugsCheckBox != null)
					pref.downloadTBs = travelbugsCheckBox.state;
				if (maxLogsInput != null)
					pref.maxLogsToSpider = Common.parseInt(maxLogsInput.getText());
				pref.savePreferences();
				this.close(FormBase.IDOK);
			}
		}
		super.onEvent(ev);
	}

	public String getCacheTypeRestriction(SpiderProperties p) {
		String cacheTypeRestriction = "";

		if (chcType != null) {
			try {
				switch (chcType.getInt()) {
				case 0:
					cacheTypeRestriction = "";
					break;
				case 1:
					cacheTypeRestriction = p.getProp("onlyTraditional");
					break;
				case 2:
					cacheTypeRestriction = p.getProp("onlyMulti");
					break;
				case 3:
					cacheTypeRestriction = p.getProp("onlyVirtual");
					break;
				case 4:
					cacheTypeRestriction = p.getProp("onlyLetterboxHybrid");
					break;
				case 5:
					cacheTypeRestriction = p.getProp("onlyEvent");
					break;
				case 6:
					cacheTypeRestriction = p.getProp("onlyMegaEvent");
					break;
				case 7:
					cacheTypeRestriction = p.getProp("onlyWebcam");
					break;
				case 8:
					cacheTypeRestriction = p.getProp("onlyUnknown");
					break;
				case 9:
					cacheTypeRestriction = p.getProp("onlyCito");
					break;
				case 10:
					cacheTypeRestriction = p.getProp("onlyEarth");
					break;
				case 11:
					cacheTypeRestriction = p.getProp("onlyWherigo");
					break;
				default:
					cacheTypeRestriction = "";
				}
			} catch (Exception ex) { // Some tag missing from spider.def
			}
		}
		return cacheTypeRestriction;
	}

	public byte getRestrictedCacheType(SpiderProperties p) {
		byte RestrictedType = CacheType.CW_TYPE_ERROR;

		if (chcType != null) {
			try {
				switch (chcType.getInt()) {
				case 0:
					RestrictedType = CacheType.CW_TYPE_ERROR;
					break;
				case 1:
					RestrictedType = CacheType.CW_TYPE_TRADITIONAL;
					break;
				case 2:
					RestrictedType = CacheType.CW_TYPE_MULTI;
					break;
				case 3:
					RestrictedType = CacheType.CW_TYPE_VIRTUAL;
					break;
				case 4:
					RestrictedType = CacheType.CW_TYPE_LETTERBOX;
					break;
				case 5:
					RestrictedType = CacheType.CW_TYPE_EVENT;
					break;
				case 6:
					RestrictedType = CacheType.CW_TYPE_MEGA_EVENT;
					break;
				case 7:
					RestrictedType = CacheType.CW_TYPE_WEBCAM;
					break;
				case 8:
					RestrictedType = CacheType.CW_TYPE_UNKNOWN;
					break;
				case 9:
					RestrictedType = CacheType.CW_TYPE_CITO;
					break;
				case 10:
					RestrictedType = CacheType.CW_TYPE_EARTH;
					break;
				case 11:
					RestrictedType = CacheType.CW_TYPE_WHEREIGO;
					break;
				default:
					RestrictedType = CacheType.CW_TYPE_ERROR;
				}
			} catch (Exception ex) { // Some tag missing from spider.def
			}
		}
		return RestrictedType;
	}
}
