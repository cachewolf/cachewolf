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
 * Diese Class ist der Dialog fuer den Download von Opencaching.de 
 * wird von OCXMLImporter ausgerufen
 */
public class OCXMLImporterScreen extends Form {
	mButton cancelB, okB;
	Preferences myPreferences = new Preferences();
	mInput distanceInput;
	mCheckBox imagesCheckBox, mapsCheckBox;
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	
	public OCXMLImporterScreen(Preferences myPreferences) {
		super();
		this.myPreferences = myPreferences; // myPreferences sollte sp�ter auch diese Einstellungen speichern
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

		this.addNext(cancelB = new mButton((String)lr.get(1604,"Cancel")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addLast(okB = new mButton((String)lr.get(1605,"OK")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
	}
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB){
				this.close(Form.IDCANCEL);
			}
			if (ev.target == okB){
//				if (myPreferences.currProfile == 0){
//					myPreferences.mylgNS = NS.getText();
//					myPreferences.mylgDeg = NSDeg.getText();
//					myPreferences.mylgMin = NSm.getText();
//					myPreferences.mybrWE = EW.getText();
//					myPreferences.mybrDeg = EWDeg.getText();
//					myPreferences.mybrMin = EWm.getText();
//					myPreferences.mydatadir = DataDir.getText();
//					myPreferences.savePreferences();
//					myPreferences.dirty = true;
				this.close(Form.IDOK);
				}
		}
		super.onEvent(ev);
	}
}
