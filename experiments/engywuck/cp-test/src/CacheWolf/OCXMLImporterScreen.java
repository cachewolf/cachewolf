/**
 * 
 */
package CacheWolf;

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
	mInput distanceInput;
	mCheckBox imagesCheckBox, /*mapsCheckBox, */ missingCheckBox, foundCheckBox;
	mLabel distLbl;
	mLabel distUnit;
	static int DIST = 1;
	static int IMAGES = 2;
	static int ALL = 4;
	static int INCLUDEFOUND = 8;
	static int ISGC = 16;

	
	public OCXMLImporterScreen(String title, int options) {
		super();
		pref = Global.getPref(); // myPreferences sollte sp�ter auch diese Einstellungen speichern
		
		this.title = title;
		if ((options & DIST) > 0) {
			this.addNext(distLbl = new mLabel(MyLocale.getMsg(1601,"Distance:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			distanceInput = new mInput();
			String dist1;
			String dist2;
			if ((options & ISGC) > 0) {
				dist1 = Global.getProfile().distGC;
				dist2 = Global.getProfile().distOC;
			} else {
				dist1 = Global.getProfile().distOC;
				dist2 = Global.getProfile().distGC;
			}
			if ( dist1.equals("") || dist1.equals("0") || dist1.equals("0.0") ) {
				dist1 = dist2;
			}
			distanceInput.setText(dist1);
			this.addNext(distanceInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			this.addLast(distUnit = new mLabel(" km"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		}

		if ((options & IMAGES) > 0) {
			imagesCheckBox = new mCheckBox();
			imagesCheckBox.setText(MyLocale.getMsg(1602,"Download Images"));
			imagesCheckBox.setState(true); // @ToDo: aus Prefs
			this.addLast(imagesCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST);
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
			missingCheckBox.setState(false); // @ToDo: aus Prefs
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
					if (imagesCheckBox!=null) pref.downloadPicsOC = imagesCheckBox.state;
					if (missingCheckBox!=null) pref.downloadmissingOC = missingCheckBox.state;
					// TODO: sofort speichern?
				this.close(FormBase.IDOK);
				}
		}
		super.onEvent(ev);
	}
}
