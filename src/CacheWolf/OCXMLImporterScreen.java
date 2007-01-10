/**
 * 
 */
package CacheWolf;

import ewe.sys.Vm;
import ewe.ui.*;

/**
 * @author pfeffer
 * This Class is the Dialog for Download from Opencaching.de 
 * is called from OCXMLImporter
 * 20061209 Bugfix: Checking for uninitialized missingCheckBox
 */
public class OCXMLImporterScreen extends Form {
	mButton cancelB, okB;
	Preferences pref;
	mInput distanceInput;
	mCheckBox imagesCheckBox, mapsCheckBox, missingCheckBox;
	mLabel distLbl;
	static int IMAGESANDMAPS = 0;
	static int ALL = 1;
	
	public OCXMLImporterScreen(String title, int options) {
		super();
		pref = Global.getPref(); // myPreferences sollte später auch diese Einstellungen speichern
		

		this.title = title;
		this.addNext(distLbl = new mLabel(MyLocale.getMsg(1601,"Distance:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		distanceInput = new mInput();
		distanceInput.setText(Global.getProfile().distOC);
		this.addLast(distanceInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));	
		
		imagesCheckBox = new mCheckBox();
		imagesCheckBox.setText(MyLocale.getMsg(1602,"Download Images"));
		imagesCheckBox.setState(true); // @ToDo: aus Prefs
		this.addLast(imagesCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST);
		
		mapsCheckBox = new mCheckBox();
		mapsCheckBox.setText(MyLocale.getMsg(1603,"Download Maps"));
		mapsCheckBox.setState(true); // @ToDo: aus Prefs
		this.addLast(mapsCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST);
		
		if(options == ALL){
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
				this.close(Form.IDCANCEL);
			}
			if (ev.target == okB){
				    // distOC wird hier noch nicht in Pref eingetragen, damit noch geprüft werden kann, ob es größer oder kleiner ist als vorher
					pref.downloadMapsOC = mapsCheckBox.state;
					pref.downloadPicsOC = imagesCheckBox.state;
					if (missingCheckBox!=null) pref.downloadmissingOC = missingCheckBox.state;
					// @todo: sofort speichern?
				this.close(Form.IDOK);
				}
		}
		super.onEvent(ev);
	}
}
