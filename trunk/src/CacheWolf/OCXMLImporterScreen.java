/**
 * 
 */
package CacheWolf;

import CacheWolf.SpiderGC.SpiderProperties;
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
	mInput distanceInput;
	mInput maxNumberInput;
	mInput maxLogsInput;
	mCheckBox imagesCheckBox, /*mapsCheckBox, */ missingCheckBox, foundCheckBox, travelbugsCheckBox;
	mLabel distLbl;
	mLabel maxNumberLbl;
	mLabel distUnit;
	boolean isGC = true;
	static int DIST = 1;
	static int IMAGES = 2;
	static int ALL = 4;
	static int INCLUDEFOUND = 8;
	static int ISGC = 16;
	static int MAXNUMBER = 32;
	static int TRAVELBUGS = 64;
	static int MAXLOGS = 128;
	static int TYPE = 256;

	
	public OCXMLImporterScreen(String title, int options) {
		super();
		pref = Global.getPref(); // myPreferences sollte sp�ter auch diese Einstellungen speichern
		
		isGC = ((options & ISGC) > 0);
		
		this.title = title;
				
		if ((options & TYPE) > 0) {
			this.addLast( chcType = new mChoice(new String[] {
					MyLocale.getMsg(1627,"All caches"),	
					CacheType.CW_GUISTR_TRADI,
					CacheType.CW_GUISTR_MULTI,
					CacheType.CW_GUISTR_VIRTUAL,
					CacheType.CW_GUISTR_LETTERBOX,
					CacheType.CW_GUISTR_EVENT,
					CacheType.CW_GUISTR_MEGAEVENT,
					CacheType.CW_GUISTR_WEBCAM,
					CacheType.CW_GUISTR_UNKNOWN,
					CacheType.CW_GUISTR_CITO,
					CacheType.CW_GUISTR_EARTH,
					CacheType.CW_GUISTR_WHEREIGO
				},0), CellConstants.STRETCH, (CellConstants.FILL|CellConstants.WEST));
		}

		if ((options & DIST) > 0) {
			this.addNext(distLbl = new mLabel(MyLocale.getMsg(1601,"Distance:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			distanceInput = new mInput();
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
			distanceInput.setText(dist1);
			this.addNext(distanceInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			this.addLast(distUnit = new mLabel(" km/mi."),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
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
			missingCheckBox.setState(pref.downloadmissingOC);
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
				    // distOC wird hier noch nicht in Pref eingetragen, damit noch gepr�ft werden kann, ob es gr��er oder kleiner ist als vorher
					if (imagesCheckBox!=null) pref.downloadPics = imagesCheckBox.state;
					if (missingCheckBox!=null) pref.downloadmissingOC = missingCheckBox.state;
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
}
