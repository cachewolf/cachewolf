/**
 *
 */
package CacheWolf.imp;

import CacheWolf.CacheType;
import CacheWolf.Common;
import CacheWolf.Global;
import CacheWolf.MyLocale;
import CacheWolf.OC;
import CacheWolf.Preferences;
import CacheWolf.imp.SpiderGC.SpiderProperties;
import ewe.sys.Convert;
import ewe.ui.*;

/**
 * @author pfeffer
 * This Class is the Dialog for Download from Opencaching.de
 * is called from OCXMLImporter
 * 20061209 Bugfix: Checking for uninitialised missingCheckBox
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
	mCheckBox imagesCheckBox, /*mapsCheckBox, */ missingCheckBox, foundCheckBox, travelbugsCheckBox;
	ewe.ui.mChoice domains;

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


	public OCXMLImporterScreen(String title, int options) {
		super();
		pref = Global.getPref(); // myPreferences sollte später auch diese Einstellungen speichern

		isGC = ((options & ISGC) > 0);

		this.title = title;

		if((options & HOST) > 0){
			domains = new mChoice(OC.OCHostNames(),OC.getSiteIndex(pref.lastOCSite));
			domains.setTextSize(25, 1);
			this.addLast(domains, CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST);
		}

		if ((options & TYPE) > 0) {
			this.addLast( chcType = new mChoice(new String[] {
					MyLocale.getMsg(1627,"All caches"),
					MyLocale.getMsg(2,"Tradi"),
					MyLocale.getMsg(3,"Multi"),
					MyLocale.getMsg(4,"Virtual"),
					MyLocale.getMsg(5,"Letterbox"),
					MyLocale.getMsg(6,"Event"),
					MyLocale.getMsg(14,"Mega Event"),
					MyLocale.getMsg(11,"Webcam"),
					MyLocale.getMsg(8,"Mysterie"),
					MyLocale.getMsg(13,"CITO"),
					MyLocale.getMsg(18,"Earth"),
					MyLocale.getMsg(15,"WhereIGo"),
					MyLocale.getMsg(11,"Webcam"),
				},0), CellConstants.STRETCH, (CellConstants.FILL|CellConstants.WEST));
		}

		if ((options & MINDIST) > 0) {
			this.addNext(distLbl = new mLabel(MyLocale.getMsg(1628,"min. Distance:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			minDistanceInput = new mInput();
			minDistanceInput.setText(Global.getProfile().getMinDistGC());
			this.addNext(minDistanceInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			this.addLast(new mLabel(" km/mi."),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		}

		if ((options & DIST) > 0) {
			this.addNext(distLbl = new mLabel(MyLocale.getMsg(1601,"Distance:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
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
			if ( dist1.equals("") || dist1.equals("0") || dist1.equals("0.0") ) {
				dist1 = dist2;
			}
			maxDistanceInput.setText(dist1);
			this.addNext(maxDistanceInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			this.addLast(distUnit = new mLabel(" km/mi."),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		}

		if ((options & DIRECTION) > 0) {
			this.addNext(new mLabel(MyLocale.getMsg(1629,"Richtung:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			directionInput = new mInput();
			directionInput.setText(Global.getProfile().getDirectionGC());
			directionInput.toolTip=MyLocale.getMsg(1630,"z.B. leer oder N oder N* oder N,NE,E");
			this.addLast(directionInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		}

		if ((options & MAXNUMBER) > 0) {
			this.addNext(maxNumberLbl = new mLabel(MyLocale.getMsg(1623,"Max. number:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			maxNumberInput = new mInput();
			if ( pref.maxSpiderNumber < 0 ) {
				maxNumberInput.setText("");
			} else {
				maxNumberInput.setText(Integer.toString(pref.maxSpiderNumber));
			}
			this.addNext(maxNumberInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			this.addLast( new mLabel(MyLocale.getMsg(1624," caches")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		}

		if ((options & MAXUPDATE) > 0) {
			this.addNext(new mLabel(MyLocale.getMsg(1631,"Max. Updates:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			maxNumberUpdates = new mInput();
			maxNumberUpdates.setText("");
			this.addNext(maxNumberUpdates,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			this.addLast( new mLabel(MyLocale.getMsg(1624," caches")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		}

		if ((options & MAXLOGS) > 0) {
			this.addNext(new mLabel(MyLocale.getMsg(1626,"Max. logs:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			maxLogsInput = new mInput();
			maxLogsInput.setText(Convert.toString(pref.maxLogsToSpider));
			this.addLast(maxLogsInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		}

		if ((options & IMAGES) > 0) {
			imagesCheckBox = new mCheckBox();
			imagesCheckBox.setText(MyLocale.getMsg(1602,"Download Images"));
			imagesCheckBox.setState(pref.downloadPics);
			this.addLast(imagesCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST);
		}

		if ((options & TRAVELBUGS) > 0) {
			travelbugsCheckBox = new mCheckBox();
			travelbugsCheckBox.setText(MyLocale.getMsg(1625,"Download TBs"));
			travelbugsCheckBox.setState(pref.downloadTBs);
			this.addLast(travelbugsCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST);
		}

		if((options & INCLUDEFOUND) > 0){
			foundCheckBox = new mCheckBox();
			foundCheckBox.setText(MyLocale.getMsg(1622,"Exclude found caches"));
			foundCheckBox.setState(true);
			this.addLast(foundCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST);
		}

		if((options & ALL) > 0){
			missingCheckBox = new mCheckBox();
			missingCheckBox.setText(MyLocale.getMsg(1606,"Alle erneut downloaden"));
			missingCheckBox.setState(pref.downloadMissingOC);
			this.addLast(missingCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST);
		}

		cancelB = new mButton(MyLocale.getMsg(1604,"Cancel"));
		cancelB.setHotKey(0, IKeys.ESCAPE);
		this.addNext(cancelB,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		okB = new mButton(MyLocale.getMsg(1605,"OK"));
		okB.setHotKey(0, IKeys.ACTION);
		okB.setHotKey(0, IKeys.ENTER);
		this.addLast(okB,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
	}
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB){
				this.close(FormBase.IDCANCEL);
			}
			if (ev.target == okB){
				    // distOC wird hier noch nicht in Pref eingetragen, damit noch geprüft werden kann, ob es größer oder kleiner ist als vorher
					if (imagesCheckBox!=null) pref.downloadPics = imagesCheckBox.state;
					if (missingCheckBox!=null) pref.downloadMissingOC = missingCheckBox.state;
					if (travelbugsCheckBox!=null) pref.downloadTBs = travelbugsCheckBox.state;
					if (maxLogsInput!=null) pref.maxLogsToSpider=Common.parseInt(maxLogsInput.getText());
					pref.savePreferences();
				this.close(FormBase.IDOK);
				}
		}
		super.onEvent(ev);
	}

	public String getCacheTypeRestriction(SpiderProperties p){
		String cacheTypeRestriction = "";

		if (chcType!=null){
			try {
				switch (chcType.getInt()) {
				case  0:
					cacheTypeRestriction = "";
					break;
				case  1:
					cacheTypeRestriction = p.getProp("onlyTraditional");
					break;
				case  2:
					cacheTypeRestriction = p.getProp("onlyMulti");
					break;
				case  3:
					cacheTypeRestriction = p.getProp("onlyVirtual") ;
					break;
				case  4:
					cacheTypeRestriction = p.getProp("onlyLetterboxHybrid");
					break;
				case  5:
					cacheTypeRestriction = p.getProp("onlyEvent");
					break;
				case  6:
					cacheTypeRestriction = p.getProp("onlyMegaEvent");
					break;
				case  7:
					cacheTypeRestriction = p.getProp("onlyWebcam");
					break;
				case  8:
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
			}catch (Exception ex) { // Some tag missing from spider.def
			}
		}
		return cacheTypeRestriction;
	}

	public byte getRestrictedCacheType(SpiderProperties p){
		byte RestrictedType = CacheType.CW_TYPE_ERROR;

		if (chcType!=null){
			try {
				switch (chcType.getInt()) {
				case  0:
					RestrictedType = CacheType.CW_TYPE_ERROR;
					break;
				case  1:
					RestrictedType = CacheType.CW_TYPE_TRADITIONAL;
					break;
				case  2:
					RestrictedType = CacheType.CW_TYPE_MULTI;
					break;
				case  3:
					RestrictedType = CacheType.CW_TYPE_VIRTUAL;
					break;
				case  4:
					RestrictedType = CacheType.CW_TYPE_LETTERBOX;
					break;
				case  5:
					RestrictedType = CacheType.CW_TYPE_EVENT;
					break;
				case  6:
					RestrictedType = CacheType.CW_TYPE_MEGA_EVENT;
					break;
				case  7:
					RestrictedType = CacheType.CW_TYPE_WEBCAM;
					break;
				case  8:
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
			}catch (Exception ex) { // Some tag missing from spider.def
			}
		}
		return RestrictedType;
	}
}
