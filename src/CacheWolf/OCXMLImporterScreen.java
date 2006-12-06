/**
 * 
 */
package CacheWolf;

import ewe.sys.LocalResource;
import ewe.sys.Locale;
import ewe.sys.Vm;
import ewe.ui.*;

/**
 * @author pfeffer
 * This Class is the Dialog for Download from Opencaching.de 
 * is called from OCXMLImporter
 */
public class OCXMLImporterScreen extends Form {
	mButton cancelB, okB;
	Preferences myPreferences = new Preferences();
	mInput distanceInput;
	mCheckBox imagesCheckBox, mapsCheckBox, missingCheckBox;
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	static int IMAGESANDMAPS = 0;
	static int ALL = 1;
	
	public OCXMLImporterScreen(Preferences myPreferences, String title, int options) {
		super();
		this.myPreferences = myPreferences; // myPreferences sollte später auch diese Einstellungen speichern
		

		this.title = title;
		this.addNext(new mLabel((String)lr.get(1601,"Distance:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		distanceInput = new mInput();
		distanceInput.setText(myPreferences.distOC);
		this.addLast(distanceInput,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));	
		
		imagesCheckBox = new mCheckBox();
		imagesCheckBox.setText((String)lr.get(1602,"Download Images"));
		imagesCheckBox.setState(true); // @ToDo: aus Prefs
		this.addLast(imagesCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST);
		
		mapsCheckBox = new mCheckBox();
		mapsCheckBox.setText((String)lr.get(1603,"Download Maps"));
		mapsCheckBox.setState(true); // @ToDo: aus Prefs
		this.addLast(mapsCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST);
		
		if(options == ALL){
			missingCheckBox = new mCheckBox();
			missingCheckBox.setText((String)lr.get(1606,"Alle erneut downloaden"));
			missingCheckBox.setState(false); // @ToDo: aus Prefs
			this.addLast(missingCheckBox, CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST);
		}

		this.addNext(cancelB = new mButton((String)lr.get(1604,"Cancel")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(okB = new mButton((String)lr.get(1605,"OK")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
	}
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB){
				this.close(Form.IDCANCEL);
			}
			if (ev.target == okB){
				    // distOC wird hier noch nicht in Pref eingetragen, damit noch geprüft werden kann, ob es größer oder kleiner ist als vorher
					myPreferences.downloadMapsOC = mapsCheckBox.state;
					myPreferences.downloadPicsOC = imagesCheckBox.state;
					myPreferences.downloadmissingOC = missingCheckBox.state;
					// @todo: sofort speichern?
				this.close(Form.IDOK);
				}
		}
		super.onEvent(ev);
	}
}
