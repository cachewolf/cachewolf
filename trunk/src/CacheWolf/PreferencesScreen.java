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
	mInput NSDeg, NSm, EWDeg, EWm, DataDir, Proxy, ProxyPort, Alias, nLogs, Browser, fontSize, inpGPS, inpLogsPerPage;
	mCheckBox dif, ter, loc, own, hid, stat, dist, bear, chkAutoLoad, chkShowDeletedImg, chkMenuAtTop, 
	          chkTabsAtTop, chkShowStatus,chkHasCloseButton;
	mTabbedPanel mTab;
	mChoice chcGarminPort;
	
	Preferences pref;
	
	CellPanel pnlGeneral = new CellPanel();
	CellPanel pnlDisplay = new CellPanel();
	CellPanel pnlMore = new CellPanel();
	ScrollBarPanel scp;
	String [] garminPorts= new String[]{"com1","com2","com3","com4","usb"};
	
	public PreferencesScreen (Preferences p){
		mTab=new mTabbedPanel();
		
		//scp = new ScrollBarPanel(pnlGeneral);
		pref = p;
		this.title = MyLocale.getMsg(600,"Preferences");
		//this.resizable = false;
		//this.moveable = true;
		//this.windowFlagsToSet = Window.FLAG_MAXIMIZE;
		
		/////////////////////////////////////////////////////////
		// First panel - General
		/////////////////////////////////////////////////////////
		pnlGeneral.addNext(new mLabel(MyLocale.getMsg(601,"Your Alias:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlGeneral.addNext(new mLabel("Browser:"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlGeneral.addLast(gpsB = new mButton("GPS"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		Alias = new mInput();
		Browser = new mInput();
		Alias.setText(pref.myAlias);
		Browser.setText(pref.browser);
		pnlGeneral.addNext(Alias,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//content.addNext(Alias.setTag(Control.SPAN, new Dimension(3,1)),content.DONTSTRETCH, (content.HFILL|content.WEST));
		pnlGeneral.addNext(Browser,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlGeneral.addLast(inpGPS=new mInput(""));
		inpGPS.modify(ControlConstants.Disabled|ControlConstants.NoFocus,0);
		inpGPS.setText(pref.mySPO.portName+"/"+pref.mySPO.baudRate);
		
		pnlGeneral.addNext(new mLabel(MyLocale.getMsg(603,"Data Directory:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlGeneral.addLast(brwBt = new mButton(MyLocale.getMsg(604,"Browse")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.EAST));
		DataDir = new mInput();
		DataDir.setText(pref.baseDir);
		pnlGeneral.addLast(DataDir.setTag(Control.SPAN, new Dimension(3,1)),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.EAST));
		pnlGeneral.addLast(chkAutoLoad = new mCheckBox(MyLocale.getMsg(629,"Autoload last profile")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if (pref.autoReloadLastProfile) chkAutoLoad.setState(true);
			
		pnlGeneral.addNext(new mLabel("Proxy"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlGeneral.addNext(new mLabel("Port"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlGeneral.addLast(new mLabel("Font"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlGeneral.addNext(Proxy = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		Proxy.setText(pref.myproxy);
		pnlGeneral.addNext(ProxyPort = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		pnlGeneral.addLast(fontSize = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		ProxyPort.setText(pref.myproxyport);
		fontSize.setText(Convert.toString(pref.fontSize));
		pnlGeneral.addNext(new mLabel("Garmin PC Port"));
		pnlGeneral.addNext(chcGarminPort=new mChoice(garminPorts,0));
		chcGarminPort.selectItem(pref.garminConn);
		pnlGeneral.addLast(new mLabel(""));
		
		/////////////////////////////////////////////////////////
		// Second panel - Screen
		/////////////////////////////////////////////////////////
		Frame frmDisplay=new Frame();
		frmDisplay.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_BOTTOM;
		frmDisplay.addLast(new mLabel(MyLocale.getMsg(605,"Display Preferences")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		frmDisplay.addNext(dif = new mCheckBox(MyLocale.getMsg(606,"Difficulty")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(pref.tablePrefs[2] == 1) dif.setState(true);
		frmDisplay.addNext(ter = new mCheckBox(MyLocale.getMsg(607,"Terrain")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(pref.tablePrefs[3] == 1) ter.setState(true);
		frmDisplay.addLast(loc = new mCheckBox(MyLocale.getMsg(608,"Location")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(pref.tablePrefs[6] == 1) loc.setState(true);
		frmDisplay.addNext(own = new mCheckBox(MyLocale.getMsg(609,"Owner")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(pref.tablePrefs[7] == 1) own.setState(true); 
		frmDisplay.addNext(hid = new mCheckBox(MyLocale.getMsg(610,"Hidden")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(pref.tablePrefs[8] == 1) hid.setState(true);
		frmDisplay.addLast(stat = new mCheckBox(MyLocale.getMsg(611,"Status")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(pref.tablePrefs[9] == 1) stat.setState(true);
		frmDisplay.addNext(dist = new mCheckBox(MyLocale.getMsg(612,"Distance")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(pref.tablePrefs[10] == 1) dist.setState(true);
		frmDisplay.addLast(bear = new mCheckBox(MyLocale.getMsg(613,"Bearing")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if(pref.tablePrefs[11] == 1) bear.setState(true);
		dist.setTag(INSETS,new Insets(0,0,2,0));
		bear.setTag(INSETS,new Insets(0,0,2,0));
		pnlDisplay.addLast(frmDisplay,CellConstants.STRETCH,CellConstants.FILL);
		
		Frame frmImages=new Frame();
		frmImages.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_BOTTOM;
		frmImages.addNext(new mLabel(MyLocale.getMsg(623,"Images:")),CellConstants.VSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		frmImages.addLast(chkShowDeletedImg = new mCheckBox(MyLocale.getMsg(624,"Show deleted images")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.EAST));
		chkShowDeletedImg.setTag(INSETS,new Insets(0,0,2,0));
		if (pref.showDeletedImages) chkShowDeletedImg.setState(true);
		pnlDisplay.addLast(frmImages,CellConstants.STRETCH,CellConstants.FILL);
				
		Frame frmScreen=new Frame();
		frmScreen.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_BOTTOM;
		frmScreen.addLast(new mLabel(MyLocale.getMsg(625,"Screen layout (needs restart):")));
		frmScreen.addNext(chkMenuAtTop = new mCheckBox(MyLocale.getMsg(626,"Menu top")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		chkMenuAtTop.setTag(INSETS,new Insets(0,0,2,0));
		chkMenuAtTop.setState(pref.menuAtTop);
		frmScreen.addNext(chkTabsAtTop = new mCheckBox(MyLocale.getMsg(627,"Tabs top")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		chkTabsAtTop.setState(pref.tabsAtTop);
		chkTabsAtTop.setTag(INSETS,new Insets(0,0,2,0));
		frmScreen.addLast(chkShowStatus = new mCheckBox(MyLocale.getMsg(628,"Show status")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		chkShowStatus.setState(pref.showStatus);
		chkShowStatus.setTag(INSETS,new Insets(0,0,2,0));
		pnlDisplay.addLast(frmScreen,CellConstants.STRETCH,CellConstants.FILL);
		
		Frame frmHintLog=new Frame();
		//frmHintLog.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_BOTTOM;
        frmHintLog.addNext(new mLabel(MyLocale.getMsg(630,"HintLogPanel:  Logs per page ")));	
		frmHintLog.addNext(inpLogsPerPage=new mInput(),CellConstants.DONTSTRETCH,CellConstants.DONTFILL|CellConstants.WEST);
		inpLogsPerPage.setText(Convert.toString(pref.logsPerPage));
		pnlDisplay.addLast(frmHintLog,CellConstants.STRETCH,CellConstants.FILL);

		/////////////////////////////////////////////////////////
		// Third panel - More
		/////////////////////////////////////////////////////////
        pnlMore.addLast(chkHasCloseButton=new mCheckBox(MyLocale.getMsg(631,"PDA has close Button")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));	
        chkHasCloseButton.setState(pref.hasCloseButton);

		mTab.addCard(pnlGeneral,MyLocale.getMsg(621,"General"),null);
		mTab.addCard(pnlDisplay,MyLocale.getMsg(622,"Screen"),null);
		mTab.addCard(pnlMore,MyLocale.getMsg(632,"More"),null);
		//this.addLast(scp.getScrollablePanel(), CellConstants.STRETCH, CellConstants.FILL);
		
		this.addLast(mTab);
		addNext(cancelB = new mButton(MyLocale.getMsg(614,"Cancel")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		addLast(applyB = new mButton(MyLocale.getMsg(615,"Apply")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
	}
	
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB){
				this.close(0);
			}
			if (ev.target == applyB){
				//if (pref.currProfile == 0){
					//pref.curCentrePt.set(btnCentre.getText());
					pref.baseDir = DataDir.getText();
				//}
				pref.fontSize = Convert.toInt(fontSize.getText());
				if (pref.fontSize<6) pref.fontSize=12;
				pref.logsPerPage=Convert.toInt(inpLogsPerPage.getText());
				if (pref.logsPerPage==0) pref.logsPerPage=pref.DEFAULT_LOGS_PER_PAGE;
				Font defaultGuiFont = mApp.findFont("gui");
				int sz = (pref.fontSize);
				Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz); 
				mApp.addFont(newGuiFont, "gui"); 
				mApp.fontsChanged();
				mApp.mainApp.font = newGuiFont;
				
				pref.myAlias = Alias.getText().trim();
				pref.browser = Browser.getText();
				//Vm.debug(myPreferences.browser);
				pref.myproxy = Proxy.getText();
				pref.myproxyport = ProxyPort.getText();
				//myPreferences.nLogs = Convert.parseInt(nLogs.getText());
				pref.tablePrefs[2] = (dif.getState()==true ? 1 : 0);
				pref.tablePrefs[3] = (ter.getState()==true ? 1 : 0);
				pref.tablePrefs[6] = (loc.getState()==true ? 1 : 0);
				pref.tablePrefs[7] = (own.getState()==true ? 1 : 0);
				pref.tablePrefs[8] = (hid.getState()==true ? 1 : 0);
				pref.tablePrefs[9] = (stat.getState()==true ? 1 : 0);
				pref.tablePrefs[10] = (dist.getState()==true ? 1 : 0);
				pref.tablePrefs[11] = (bear.getState()==true ? 1 : 0);
				pref.autoReloadLastProfile=chkAutoLoad.getState();
				pref.showDeletedImages=chkShowDeletedImg.getState();
				pref.garminConn=chcGarminPort.getSelectedItem().toString();
				pref.menuAtTop=chkMenuAtTop.getState();
				pref.tabsAtTop=chkTabsAtTop.getState();
				pref.showStatus=chkShowStatus.getState();
				pref.hasCloseButton=chkHasCloseButton.getState();
				pref.savePreferences();
				pref.dirty = true; // Need to update table in case columns were enabled/disabled
				this.close(0);
			}
			if(ev.target == brwBt){
				FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, pref.baseDir);
				fc.setTitle(MyLocale.getMsg(616,"Select directory"));
				if(fc.execute() != FileChooser.IDCANCEL)	DataDir.setText(fc.getChosen()+"/");
			}
			if (ev.target == gpsB){
				GPSPortOptions spo = new GPSPortOptions();
				spo.portName = pref.mySPO.portName;
				spo.baudRate = pref.mySPO.baudRate;
				Editor s = spo.getEditor(SerialPortOptions.ADVANCED_EDITOR);
				spo.forwardGpsChkB.setState(pref.forwardGPS);
				spo.inputBoxForwardHost.setText(pref.forwardGpsHost);
				Gui.setOKCancel(s);
				if (s.execute()== Editor.IDOK) {
					pref.mySPO.portName = spo.portName; 
					pref.mySPO.baudRate = spo.baudRate;
					pref.forwardGPS = spo.forwardGpsChkB.getState();
					pref.forwardGpsHost = spo.inputBoxForwardHost.getText();
					inpGPS.setText(pref.mySPO.portName+"/"+pref.mySPO.baudRate);

				}
			}
			// change destination waypoint
			/*if (ev.target == btnCentre){
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(pref.curCentrePt, CWPoint.CW);
				if (cs.execute()== CoordsScreen.IDOK){
					pref.curCentrePt.set(cs.getCoords());
					btnCentre.setText(pref.curCentrePt.toString(CWPoint.CW));
				}
			}
			*/
		}
		super.onEvent(ev);
	}
	
}
