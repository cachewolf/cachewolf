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
	          chkTabsAtTop, chkShowStatus,chkHasCloseButton,chkSynthShort;
	mTabbedPanel mTab;
	mChoice chcGarminPort;
	mLabel lblGarmin;
	TableColumnChooser tccBugs,tccList;
	
	Preferences pref;
	
	CellPanel pnlGeneral = new CellPanel();
	CellPanel pnlDisplay = new CellPanel();
	CellPanel pnlMore = new CellPanel();
	CellPanel pnlTB = new CellPanel();
	Frame frmGarmin = new Frame();
	ScrollBarPanel scp;
	String [] garminPorts= new String[]{"com1","com2","com3","com4","com5","com6","com7","usb"};
	
	public PreferencesScreen (Preferences p){
		mTab=new mTabbedPanel();
		setPreferredSize(240,240);
		//scp = new ScrollBarPanel(pnlGeneral);
		pref = p;
		this.title = MyLocale.getMsg(600,"Preferences");
		//this.resizable = false;
		//this.moveable = true;
		//this.windowFlagsToSet = Window.FLAG_MAXIMIZE;
		
		/////////////////////////////////////////////////////////
		// First panel - General
		/////////////////////////////////////////////////////////
		Frame frmDataDir=new Frame();
		frmDataDir.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_BOTTOM;
		frmDataDir.addNext(new mLabel(MyLocale.getMsg(603,"Data Directory:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//frmDataDir.setTag(INSETS,new Insets(10,10,10,10));
		frmDataDir.addLast(brwBt = new mButton(MyLocale.getMsg(604,"Browse")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.EAST));
		DataDir = new mInput();
		DataDir.setText(pref.baseDir);
		frmDataDir.addLast(DataDir.setTag(Control.SPAN, new Dimension(3,1)),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.EAST));
		frmDataDir.addLast(chkAutoLoad = new mCheckBox(MyLocale.getMsg(629,"Autoload last profile")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if (pref.autoReloadLastProfile) chkAutoLoad.setState(true);
		chkAutoLoad.setTag(INSETS,new Insets(0,0,2,0));
		pnlGeneral.addLast(frmDataDir,HSTRETCH,HFILL);
		
		CellPanel pnlBrowser=new CellPanel();
		pnlBrowser.setTag(INSETS,new Insets(2,0,0,0));
		pnlBrowser.addNext(new mLabel("Browser:"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		Browser = new mInput();
		Browser.setText(pref.browser);
		pnlBrowser.addLast(Browser,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));

		pnlBrowser.addNext(new mLabel(MyLocale.getMsg(601,"Your Alias:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		Alias = new mInput();
		Alias.setText(pref.myAlias);
		pnlBrowser.addNext(Alias,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlBrowser.addLast(new mLabel("")).setTag(SPAN,new Dimension(2,1));
		
		pnlGeneral.addLast(pnlBrowser,HSTRETCH,HFILL);
		
		pnlGeneral.addNext(gpsB = new mButton("GPS"),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		//content.addNext(Alias.setTag(Control.SPAN, new Dimension(3,1)),content.DONTSTRETCH, (content.HFILL|content.WEST));
		pnlGeneral.addLast(inpGPS=new mInput(""));
		inpGPS.modify(ControlConstants.Disabled|ControlConstants.NoFocus,0);
		inpGPS.setText(pref.mySPO.portName+"/"+pref.mySPO.baudRate);
		
		// Garmin and GPSBabel
		frmGarmin.addNext(lblGarmin=new mLabel(MyLocale.getMsg(173,"Garmin:  PC Port:")),DONTSTRETCH,LEFT);
		lblGarmin.setTag(INSETS,new Insets(4,0,0,0));
		frmGarmin.addNext(chcGarminPort=new mChoice(garminPorts,0),DONTSTRETCH,LEFT);
		chcGarminPort.setTag(INSETS,new Insets(4,0,0,0));
		chcGarminPort.selectItem(pref.garminConn);
		frmGarmin.addLast(chkSynthShort=new mCheckBox(MyLocale.getMsg(174,"Short Names")),STRETCH,RIGHT);
		chkSynthShort.setTag(INSETS,new Insets(4,0,0,0));
		chkSynthShort.setState(!pref.garminGPSBabelOptions.equals(""));
		frmGarmin.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_TOP;
		frmGarmin.setTag(INSETS,new Insets(4,0,0,0));
		pnlGeneral.addLast(frmGarmin);
		pnlGeneral.addLast(new mLabel(""));
		
		/////////////////////////////////////////////////////////
		// Second panel - Screen
		/////////////////////////////////////////////////////////
		
/*		CellPanel pnlFont=new CellPanel();
		pnlFont.addNext(new mLabel("Font"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlFont.addLast(fontSize = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		fontSize.setText(Convert.toString(pref.fontSize));
		pnlDisplay.addLast(pnlFont);
*/		
		Frame frmScreen=new Frame();
		frmScreen.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER;
		mLabel lblTitle;
		CellPanel pnlScreen=new CellPanel();
		pnlScreen.addNext(lblTitle=new mLabel(MyLocale.getMsg(625,"Screen (needs restart):")));
		pnlScreen.addNext(new mLabel("Font"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlScreen.addLast(fontSize = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		fontSize.maxLength=2;
		fontSize.setPreferredSize(40,-1);
		frmScreen.addLast(pnlScreen,HSTRETCH,HFILL);
		fontSize.setText(Convert.toString(pref.fontSize));
		
		frmScreen.addLast(chkHasCloseButton=new mCheckBox(MyLocale.getMsg(631,"PDA has close Button")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));	
    	//lblTitle.setTag(INSETS,new Insets(2,0,0,0));
        chkHasCloseButton.setState(pref.hasCloseButton);
		frmScreen.addNext(chkMenuAtTop = new mCheckBox(MyLocale.getMsg(626,"Menu top")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		chkMenuAtTop.setTag(INSETS,new Insets(0,0,2,0));
		chkMenuAtTop.setState(pref.menuAtTop);
		frmScreen.addNext(chkTabsAtTop = new mCheckBox(MyLocale.getMsg(627,"Tabs top")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		chkTabsAtTop.setState(pref.tabsAtTop);
		chkTabsAtTop.setTag(INSETS,new Insets(0,0,2,0));
		frmScreen.addLast(chkShowStatus = new mCheckBox(MyLocale.getMsg(628,"Status")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		chkShowStatus.setState(pref.showStatus);
		chkShowStatus.setTag(INSETS,new Insets(0,0,2,0));
		pnlDisplay.addLast(frmScreen,CellConstants.HSTRETCH,CellConstants.FILL);
		
		Frame frmImages=new Frame();
		frmImages.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_TOP|CellPanel.BF_BOTTOM;
		frmImages.addNext(new mLabel(MyLocale.getMsg(623,"Images:")),CellConstants.VSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		frmImages.addLast(chkShowDeletedImg = new mCheckBox(MyLocale.getMsg(624,"Show deleted images")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.EAST));
		chkShowDeletedImg.setTag(INSETS,new Insets(0,0,2,0));
		if (pref.showDeletedImages) chkShowDeletedImg.setState(true);
		pnlDisplay.addLast(frmImages,CellConstants.STRETCH,CellConstants.FILL);

		Frame frmHintLog=new Frame();
		frmHintLog.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_BOTTOM;
        mLabel lblHlP;
		frmHintLog.addNext(lblHlP=new mLabel(MyLocale.getMsg(630,"HintLogPanel:  Logs per page ")));	
		frmHintLog.addLast(inpLogsPerPage=new mInput(),CellConstants.DONTSTRETCH,CellConstants.DONTFILL|CellConstants.WEST);
		inpLogsPerPage.setText(Convert.toString(pref.logsPerPage));
		inpLogsPerPage.setPreferredSize(40,-1);
		inpLogsPerPage.setTag(INSETS,new Insets(0,0,2,0));
		lblHlP.setTag(INSETS,new Insets(6,0,2,0));
		pnlDisplay.addLast(frmHintLog,CellConstants.STRETCH,CellConstants.FILL);

		pnlDisplay.addLast(new mLabel(""));
		/////////////////////////////////////////////////////////
		// Third panel - More
		/////////////////////////////////////////////////////////
		CellPanel pnlProxy=new CellPanel();
		pnlProxy.addNext(new mLabel("Proxy"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlProxy.addLast(Proxy = new mInput(),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		Proxy.setText(pref.myproxy);
		pnlProxy.addNext(new mLabel("Port"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlProxy.addNext(ProxyPort = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlProxy.addLast(new mLabel("")).setTag(SPAN,new Dimension(2,1));
		pnlMore.addLast(pnlProxy,HSTRETCH,HFILL);
		
		ProxyPort.setText(pref.myproxyport);
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
		pnlMore.addLast(frmDisplay,CellConstants.STRETCH,CellConstants.FILL);

		/////////////////////////////////////////////////////////
		// Fourth panel - Travelbugs
		/////////////////////////////////////////////////////////

		
        mTab.addCard(pnlGeneral,MyLocale.getMsg(621,"General"),null);
		mTab.addCard(pnlDisplay,MyLocale.getMsg(622,"Screen"),null);
		mTab.addCard(pnlMore,MyLocale.getMsg(632,"More"),null);
		/*mTab.addCard(tccList=new TableColumnChooser(new String[] {
				"checkbox","type","difficulty","terrain",
				"waypoint","name","coords","owner",
				"date hidden","status","distance","bearing"},pref.listColMap),"List",null);
		*/
		Card c=mTab.addCard(tccBugs=new TableColumnChooser(new String[] {
				MyLocale.getMsg(6000,"Guid"),
				MyLocale.getMsg(6001,"Name"),
				MyLocale.getMsg(6002,"track#"),
				MyLocale.getMsg(6003,"Mission"),
				MyLocale.getMsg(6004,"From Prof"),
				MyLocale.getMsg(6005,"From Wpt"),
				MyLocale.getMsg(6006,"From Date"),
				MyLocale.getMsg(6007,"From Log"),
				MyLocale.getMsg(6008,"To Prof"),
				MyLocale.getMsg(6009,"To Wpt"),
				MyLocale.getMsg(6010,"To Date"),
				MyLocale.getMsg(6011,"To Log")},pref.travelbugColMap),"T-bugs",null);
		//c.iconize(new Image("bug.gif"));
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
				pref.garminGPSBabelOptions=chkSynthShort.state?"-s":"";
				pref.menuAtTop=chkMenuAtTop.getState();
				pref.tabsAtTop=chkTabsAtTop.getState();
				pref.showStatus=chkShowStatus.getState();
				pref.hasCloseButton=chkHasCloseButton.getState();
				pref.travelbugColMap=tccBugs.getSelectedCols();
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
