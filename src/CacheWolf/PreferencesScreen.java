package CacheWolf;

import ewe.ui.*;
import ewe.io.*;
import ewe.fx.*;
import ewe.filechooser.*;
import ewe.sys.*;

/**
*	This class displays a user interface allowing the user to change and set
*	preferences. It also provides a method to save the changed preferences that
*	are saved immediatly when the user presses "Apply".
*	Class ID=600
*/
public class PreferencesScreen extends Form {
	mButton cancelB, applyB, brwBt, gpsB;
	mChoice NS, EW;
	mInput NSDeg, NSm, EWDeg, EWm, DataDir, Proxy, ProxyPort, Alias, nLogs, Browser;
	mCheckBox dif, ter, loc, own, hid, stat, dist, bear;
	Preferences myPreferences = new Preferences();
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	
	public PreferencesScreen (Preferences pref){
		myPreferences = pref;
		
		this.title = (String)lr.get(600,"Preferences");
		//this.resizable = false;
		//this.moveable = true;
		//this.windowFlagsToSet = Window.FLAG_MAXIMIZE;
		this.addNext(new mLabel((String)lr.get(601,"Your Alias:")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addLast(new mLabel("Browser:"),this.DONTSTRETCH, (this.DONTFILL|this.WEST));

		Alias = new mInput();
		Browser = new mInput();
		Alias.setText(myPreferences.myAlias);
		Browser.setText(myPreferences.browser);
		this.addNext(Alias,this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		//this.addNext(Alias.setTag(Control.SPAN, new Dimension(3,1)),this.DONTSTRETCH, (this.HFILL|this.WEST));
		this.addNext(Browser,this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addLast(gpsB = new mButton("GPS"),this.DONTSTRETCH, (this.DONTFILL|this.WEST));

		// display some things only, if no profile is active
		if (pref.currProfile == 0){
			this.addLast(new mLabel((String)lr.get(602,"Your Location:")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
			this.addNext(NS = new mChoice(new String[]{"N", "S"},0),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
			if(myPreferences.mylgNS.equals("N")) NS.setInt(0);
			else NS.setInt(1);
			this.addNext(NSDeg = new mInput(),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
			NSDeg.setText(myPreferences.mylgDeg);
			this.addLast(NSm = new mInput(),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
			NSm.setText(myPreferences.mylgMin);
			this.addNext(EW = new mChoice(new String[]{"E", "W"},0),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
			if(myPreferences.mybrWE.equals("E")) EW.setInt(0);
			else EW.setInt(1);
			this.addNext(EWDeg = new mInput(),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
			EWDeg.setText(myPreferences.mybrDeg);
			this.addLast(EWm = new mInput(),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
			EWm.setText(myPreferences.mybrMin);
			this.addNext(new mLabel((String)lr.get(603,"Data Directory:")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
			this.addLast(brwBt = new mButton((String)lr.get(604,"Browse")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
			DataDir = new mInput();
			DataDir.setText(myPreferences.mydatadir);
			this.addLast(DataDir.setTag(Control.SPAN, new Dimension(3,1)),this.DONTSTRETCH, (this.HFILL|this.WEST));
		}
		//this.addNext(nLogs = new mInput(),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		//nLogs.setText(Convert.toString(myPreferences.nLogs));
		//this.addLast(new mLabel("Logs"), this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addNext(new mLabel("Proxy"),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addLast(new mLabel("Port"),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addNext(Proxy = new mInput(),this.DONTSTRETCH, (this.HFILL|this.WEST));
		Proxy.setText(myPreferences.myproxy);
		this.addLast(ProxyPort = new mInput(),this.DONTSTRETCH, (this.HFILL|this.WEST));
		ProxyPort.setText(myPreferences.myproxyport);
		this.addLast(new mLabel((String)lr.get(605,"Display Preferences")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addNext(dif = new mCheckBox((String)lr.get(606,"Difficulty")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		if(myPreferences.tablePrefs[2] == 1) dif.setState(true);
		this.addNext(ter = new mCheckBox((String)lr.get(607,"Terrain")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		if(myPreferences.tablePrefs[3] == 1) ter.setState(true);
		this.addLast(loc = new mCheckBox((String)lr.get(608,"Location")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		if(myPreferences.tablePrefs[6] == 1) loc.setState(true);
		this.addNext(own = new mCheckBox((String)lr.get(609,"Owner")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		if(myPreferences.tablePrefs[7] == 1) own.setState(true); 
		this.addNext(hid = new mCheckBox((String)lr.get(610,"Hidden")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		if(myPreferences.tablePrefs[8] == 1) hid.setState(true);
		this.addLast(stat = new mCheckBox((String)lr.get(611,"Status")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		if(myPreferences.tablePrefs[9] == 1) stat.setState(true);
		this.addNext(dist = new mCheckBox((String)lr.get(612,"Distance")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		if(myPreferences.tablePrefs[10] == 1) dist.setState(true);
		this.addLast(bear = new mCheckBox((String)lr.get(613,"Bearing")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		if(myPreferences.tablePrefs[11] == 1) bear.setState(true);
		this.addNext(cancelB = new mButton((String)lr.get(614,"Cancel")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addLast(applyB = new mButton((String)lr.get(615,"Apply")),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
	}
	
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB){
				this.close(0);
			}
			if (ev.target == applyB){
				if (myPreferences.currProfile == 0){
					myPreferences.mylgNS = NS.getText();
					myPreferences.mylgDeg = NSDeg.getText();
					myPreferences.mylgMin = NSm.getText();
					myPreferences.mybrWE = EW.getText();
					myPreferences.mybrDeg = EWDeg.getText();
					myPreferences.mybrMin = EWm.getText();
					myPreferences.mydatadir = DataDir.getText();

				}
				myPreferences.myAlias = Alias.getText();
				myPreferences.browser = Browser.getText();
				//Vm.debug(myPreferences.browser);
				myPreferences.myproxy = Proxy.getText();
				myPreferences.myproxyport = ProxyPort.getText();
				//myPreferences.nLogs = Convert.parseInt(nLogs.getText());
				myPreferences.tablePrefs[2] = (dif.getState()==true ? 1 : 0);
				myPreferences.tablePrefs[3] = (ter.getState()==true ? 1 : 0);
				myPreferences.tablePrefs[6] = (loc.getState()==true ? 1 : 0);
				myPreferences.tablePrefs[7] = (own.getState()==true ? 1 : 0);
				myPreferences.tablePrefs[8] = (hid.getState()==true ? 1 : 0);
				myPreferences.tablePrefs[9] = (stat.getState()==true ? 1 : 0);
				myPreferences.tablePrefs[10] = (dist.getState()==true ? 1 : 0);
				myPreferences.tablePrefs[11] = (bear.getState()==true ? 1 : 0);
				myPreferences.savePreferences();
				myPreferences.dirty = true;
				this.close(0);
			}
			if(ev.target == brwBt){
				FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, myPreferences.mydatadir);
				fc.setTitle((String)lr.get(616,"Select data directory"));
				if(fc.execute() != fc.IDCANCEL)	DataDir.setText(fc.getChosen()+"/");
			}
			if (ev.target == gpsB){
				SerialPortOptions spo = new GPSPortOptions();
				spo.portName = myPreferences.mySPO.portName;
				spo.baudRate = myPreferences.mySPO.baudRate;
				Editor s = spo.getEditor(SerialPortOptions.ADVANCED_EDITOR);
				Gui.setOKCancel(s);
				if (s.execute()== Editor.IDOK) {
					myPreferences.mySPO.portName = spo.portName; 
					myPreferences.mySPO.baudRate = spo.baudRate;
				}
			}
		}
		super.onEvent(ev);
	}
	
}
