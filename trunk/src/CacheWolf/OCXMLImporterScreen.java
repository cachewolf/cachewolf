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
	
	public OCXMLImporterScreen(Preferences myPreferences) {
		super();
		this.myPreferences = myPreferences; // myPreferences sollte später auch diese Einstellungen speichern
		this.title=(String)lr.get(1600,"Opencahing.de Download");

		this.addNext(new mLabel((String)lr.get(1601,"Distance:")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		distanceInput = new mInput();
		distanceInput.setText(myPreferences.distOC);
		this.addLast(distanceInput,this.DONTSTRETCH, (this.DONTFILL|this.WEST));	
		
		imagesCheckBox = new mCheckBox();
		imagesCheckBox.setText((String)lr.get(1602,"Download Images"));
		imagesCheckBox.setState(true); // @ToDo: aus Prefs
		this.addLast(imagesCheckBox, this.DONTSTRETCH, this.DONTFILL|this.WEST);
		
		mapsCheckBox = new mCheckBox();
		mapsCheckBox.setText((String)lr.get(1603,"Download Maps"));
		mapsCheckBox.setState(true); // @ToDo: aus Prefs
		this.addLast(mapsCheckBox, this.DONTSTRETCH, this.DONTFILL|this.WEST);

		missingCheckBox = new mCheckBox();
		missingCheckBox.setText((String)lr.get(1606,"Alle erneut downloaden"));
		missingCheckBox.setState(false); // @ToDo: aus Prefs
		this.addLast(missingCheckBox, this.DONTSTRETCH, this.DONTFILL|this.WEST);

		this.addNext(cancelB = new mButton((String)lr.get(1604,"Cancel")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addLast(okB = new mButton((String)lr.get(1605,"OK")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
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
