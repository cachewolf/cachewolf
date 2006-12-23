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
	mButton cancelB, applyB, brwBt, gpsB,btnCentre;
	mChoice NS, EW;
	mInput NSDeg, NSm, EWDeg, EWm, DataDir, Proxy, ProxyPort, Alias, nLogs, Browser, fontSize;
	mCheckBox dif, ter, loc, own, hid, stat, dist, bear;
	Preferences myPreferences = new Preferences();

	CellPanel content = new CellPanel();
	ScrollBarPanel scp;
	
	public PreferencesScreen (Preferences pref){
		scp = new ScrollBarPanel(content);
		myPreferences = pref;
		
		this.title = MyLocale.getMsg(600,"Preferences");
		//this.resizable = false;
		//this.moveable = true;
		//this.windowFlagsToSet = Window.FLAG_MAXIMIZE;
		
		
		content.addNext(new mLabel(MyLocale.getMsg(601,"Your Alias:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addLast(new mLabel("Browser:"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		Alias = new mInput();
		Browser = new mInput();
		Alias.setText(myPreferences.myAlias);
		Browser.setText(myPreferences.browser);
		content.addNext(Alias,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//content.addNext(Alias.setTag(Control.SPAN, new Dimension(3,1)),content.DONTSTRETCH, (content.HFILL|content.WEST));
		content.addNext(Browser,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addLast(gpsB = new mButton("GPS"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		// display some things only, if no profile is active
		if (pref.currProfile == 0){
			content.addLast(new mLabel(MyLocale.getMsg(602,"Your Location:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			content.addLast(btnCentre = new mButton(myPreferences.curCentrePt.toString(CWPoint.CW)),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
			content.addNext(new mLabel(MyLocale.getMsg(603,"Data Directory:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			content.addLast(brwBt = new mButton(MyLocale.getMsg(604,"Browse")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.EAST));
			DataDir = new mInput();
			DataDir.setText(myPreferences.mydatadir);
			content.addLast(DataDir.setTag(Control.SPAN, new Dimension(3,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		}
		//content.addNext(nLogs = new mInput(),content.DONTSTRETCH, (content.DONTFILL|content.WEST));
		//nLogs.setText(Convert.toString(myPreferences.nLogs));
		//content.addLast(new mLabel("Logs"), content.DONTSTRETCH, (content.DONTFILL|content.WEST));
		content.addNext(new mLabel("Proxy"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addNext(new mLabel("Port"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addLast(new mLabel("Font"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addNext(Proxy = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		Proxy.setText(myPreferences.myproxy);
		content.addNext(ProxyPort = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		content.addLast(fontSize = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		ProxyPort.setText(myPreferences.myproxyport);
		fontSize.setText(Convert.toString(myPreferences.fontSize));
		content.addLast(new mLabel(MyLocale.getMsg(605,"Display Preferences")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addNext(dif = new mCheckBox(MyLocale.getMsg(606,"Difficulty")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(myPreferences.tablePrefs[2] == 1) dif.setState(true);
		content.addNext(ter = new mCheckBox(MyLocale.getMsg(607,"Terrain")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(myPreferences.tablePrefs[3] == 1) ter.setState(true);
		content.addLast(loc = new mCheckBox(MyLocale.getMsg(608,"Location")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(myPreferences.tablePrefs[6] == 1) loc.setState(true);
		content.addNext(own = new mCheckBox(MyLocale.getMsg(609,"Owner")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(myPreferences.tablePrefs[7] == 1) own.setState(true); 
		content.addNext(hid = new mCheckBox(MyLocale.getMsg(610,"Hidden")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(myPreferences.tablePrefs[8] == 1) hid.setState(true);
		content.addLast(stat = new mCheckBox(MyLocale.getMsg(611,"Status")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(myPreferences.tablePrefs[9] == 1) stat.setState(true);
		content.addNext(dist = new mCheckBox(MyLocale.getMsg(612,"Distance")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(myPreferences.tablePrefs[10] == 1) dist.setState(true);
		content.addLast(bear = new mCheckBox(MyLocale.getMsg(613,"Bearing")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(myPreferences.tablePrefs[11] == 1) bear.setState(true);
		content.addNext(cancelB = new mButton(MyLocale.getMsg(614,"Cancel")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addLast(applyB = new mButton(MyLocale.getMsg(615,"Apply")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		
		this.addLast(scp.getScrollablePanel(), CellConstants.STRETCH, CellConstants.FILL);
	}
	
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB){
				this.close(0);
			}
			if (ev.target == applyB){
				if (myPreferences.currProfile == 0){
					myPreferences.curCentrePt.set(btnCentre.getText());
					myPreferences.mydatadir = DataDir.getText();
				}
				myPreferences.fontSize = Convert.toInt(fontSize.getText());
				
				Font defaultGuiFont = mApp.findFont("gui");
				int sz = (myPreferences.fontSize);
				Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz); 
				mApp.addFont(newGuiFont, "gui"); 
				mApp.fontsChanged();
				mApp.mainApp.font = newGuiFont;
				
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
				fc.setTitle(MyLocale.getMsg(616,"Select data directory"));
				if(fc.execute() != FileChooser.IDCANCEL)	DataDir.setText(fc.getChosen()+"/");
			}
			if (ev.target == gpsB){
				GPSPortOptions spo = new GPSPortOptions();
				spo.portName = myPreferences.mySPO.portName;
				spo.baudRate = myPreferences.mySPO.baudRate;
				Editor s = spo.getEditor(SerialPortOptions.ADVANCED_EDITOR);
				spo.forwardGpsChkB.setState(myPreferences.forwardGPS);
				spo.inputBoxForwardHost.setText(myPreferences.forwardGpsHost);
				Gui.setOKCancel(s);
				if (s.execute()== Editor.IDOK) {
					myPreferences.mySPO.portName = spo.portName; 
					myPreferences.mySPO.baudRate = spo.baudRate;
					myPreferences.forwardGPS = spo.forwardGpsChkB.getState();
					myPreferences.forwardGpsHost = spo.inputBoxForwardHost.getText();
				}
			}
			// change destination waypoint
			if (ev.target == btnCentre){
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(myPreferences.curCentrePt, CWPoint.CW);
				if (cs.execute()== CoordsScreen.IDOK){
					myPreferences.curCentrePt.set(cs.getCoords());
					btnCentre.setText(myPreferences.curCentrePt.toString(CWPoint.CW));
				}
			}
			
		}
		super.onEvent(ev);
	}
	
}
