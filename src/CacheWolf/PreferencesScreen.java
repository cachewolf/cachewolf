package CacheWolf;

import CacheWolf.imp.SpiderGC;
import CacheWolf.navi.Metrics;
import CacheWolf.utils.FileBugfix;
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
	mChoice inpLanguage, inpMetric, inpSpiderUpdates;
	mInput DataDir, Proxy, ProxyPort, Alias, nLogs, Browser, fontSize, 
	       inpLogsPerPage,inpMaxLogsToSpider,inpPassword;
	mCheckBox chkAutoLoad, chkShowDeletedImg, chkMenuAtTop, chkTabsAtTop, chkShowStatus,chkHasCloseButton,
	          chkSynthShort,chkProxyActive, chkDescShowImg, chkAddDetailsToWaypoint, chkAddDetailsToName, 
	          chkSetCurrentCentreFromGPSPosition,chkSortingGroupedByCache,chkuseOwnSymbols,chkDebug;
	mTabbedPanel mTab;
	mChoice chcGarminPort;
	mLabel lblGarmin;
	TableColumnChooser tccBugs,tccList;
	
	Preferences pref;
	
	CellPanel pnlGeneral = new CellPanel();
	CellPanel pnlDisplay = new CellPanel();
	CellPanel pnlMore = new CellPanel();
	CellPanel pnlTB = new CellPanel();
	//Frame frmGarmin = new Frame();
	ScrollBarPanel scp; // TODO not neede any more?
	String [] garminPorts= new String[]{"com1","com2","com3","com4","com5","com6","com7","usb"};
	
	public PreferencesScreen (Preferences p){
		int sw = MyLocale.getScreenWidth();
		int sh = MyLocale.getScreenHeight();

		mTab=new mTabbedPanel();
		
		pref = p;
		this.title = MyLocale.getMsg(600,"Preferences");
		if ((sw > 240) && (sh > 240))
			this.resizable = true;
		//this.moveable = true;
		//this.windowFlagsToSet = Window.FLAG_MAXIMIZE;

		// set dialog-width according to fontsize
		if((pref.fontSize <= 13)||(sw <= 240)||(sh <= 240)){
			setPreferredSize(240,240);
		}
		else if(pref.fontSize <= 17){
			setPreferredSize(300,250);
		}
		else if(pref.fontSize <= 20){
			setPreferredSize(350,300);
		}
		else if(pref.fontSize <= 24){
			setPreferredSize(400,350);
		}
		else if(pref.fontSize <= 28){
			setPreferredSize(450,400);
		}
		else{
			setPreferredSize(500,450);
		}
		
		//scp = new ScrollBarPanel(pnlGeneral);
		
		/////////////////////////////////////////////////////////
		// First panel - General
		/////////////////////////////////////////////////////////
		Frame frmDataDir=new Frame();
		frmDataDir.borderStyle=UIConstants.BDR_RAISEDOUTER|UIConstants.BDR_SUNKENINNER|UIConstants.BF_BOTTOM;
		frmDataDir.addNext(new mLabel(MyLocale.getMsg(603,"Data Directory:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//frmDataDir.setTag(INSETS,new Insets(10,10,10,10));
		frmDataDir.addLast(brwBt = new mButton(MyLocale.getMsg(604,"Browse")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.EAST));
		DataDir = new mInput();
		DataDir.setText(pref.getBaseDir());
		frmDataDir.addLast(DataDir.setTag(CellConstants.SPAN, new Dimension(3,1)),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.EAST));
		frmDataDir.addNext(chkAutoLoad = new mCheckBox(MyLocale.getMsg(629,"Autoload last profile")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		if (pref.autoReloadLastProfile) chkAutoLoad.setState(true);
		chkAutoLoad.setTag(INSETS,new Insets(0,0,2,0));
		frmDataDir.addLast(chkSetCurrentCentreFromGPSPosition = new mCheckBox(MyLocale.getMsg(646,"centre from GPS")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.EAST));
		if (pref.setCurrentCentreFromGPSPosition) chkSetCurrentCentreFromGPSPosition.setState(true);
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
		pnlBrowser.addNext(new mLabel(MyLocale.getMsg(594,"Pwd")));
		pnlBrowser.addLast(inpPassword=new mInput(pref.password),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		inpPassword.setToolTip(MyLocale.getMsg(593,"Password is optional here.\nEnter only if you want to store it in pref.xml"));
		inpPassword.isPassword=true;
		pnlGeneral.addLast(pnlBrowser,HSTRETCH,HFILL);
		
		pnlGeneral.addLast(gpsB = new mButton("GPS: " + 
			(pref.useGPSD ? "gpsd " + pref.gpsdHost : pref.mySPO.portName+"/"+pref.mySPO.baudRate) ),
			CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));

		// Garmin and GPSBabel
		pnlGeneral.addNext(lblGarmin=new mLabel(MyLocale.getMsg(173,"Garmin:  PC Port:")),DONTSTRETCH,LEFT);
//		lblGarmin.setTag(INSETS,new Insets(4,0,0,0));
		pnlGeneral.addNext(chcGarminPort=new mChoice(garminPorts,0),DONTSTRETCH,RIGHT);
		//chcGarminPort.setTag(INSETS,new Insets(4,0,0,0));
		chcGarminPort.selectItem(pref.garminConn);
		pnlGeneral.addLast(chkSynthShort=new mCheckBox(MyLocale.getMsg(174,"Short Names")),STRETCH,LEFT);
		//chkSynthShort.setTag(INSETS,new Insets(4,0,0,0));
		chkSynthShort.setState(!pref.garminGPSBabelOptions.equals(""));
		//frmGarmin.borderStyle=UIConstants.BDR_RAISEDOUTER|UIConstants.BDR_SUNKENINNER|UIConstants.BF_TOP;
		//frmGarmin.setTag(INSETS,new Insets(4,0,0,0));
		//pnlGeneral.addLast(frmGarmin);
		pnlGeneral.addNext(new mLabel(MyLocale.getMsg(643,"Append cache details to:")),DONTSTRETCH,LEFT);
		pnlGeneral.addNext(chkAddDetailsToWaypoint=new mCheckBox(MyLocale.getMsg(644,"waypoints")),DONTSTRETCH,RIGHT);
		chkAddDetailsToWaypoint.setState(pref.addDetailsToWaypoint);
		pnlGeneral.addLast(chkAddDetailsToName=new mCheckBox(MyLocale.getMsg(645,"names")),STRETCH,LEFT);
		chkAddDetailsToName.setState(pref.addDetailsToName);
		//pnlGeneral.addLast(new mLabel(""));
		
		/////////////////////////////////////////////////////////
		// Second panel - Screen
		/////////////////////////////////////////////////////////
		
		Frame frmScreen=new Frame();
		frmScreen.borderStyle=UIConstants.BDR_RAISEDOUTER|UIConstants.BDR_SUNKENINNER;
		CellPanel pnlScreen=new CellPanel();
		pnlScreen.addNext(new mLabel(MyLocale.getMsg(625,"Screen (needs restart):")));
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
		frmImages.borderStyle=UIConstants.BDR_RAISEDOUTER|UIConstants.BDR_SUNKENINNER|UIConstants.BF_TOP|UIConstants.BF_BOTTOM;
		//frmImages.addNext(new mLabel(MyLocale.getMsg(623,"Images:")),CellConstants.VSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		frmImages.addLast(chkShowDeletedImg = new mCheckBox(MyLocale.getMsg(624,"Show deleted images")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		chkShowDeletedImg.setTag(INSETS,new Insets(2,0,0,0));
		if (pref.showDeletedImages) chkShowDeletedImg.setState(true);
		//mLabel dummy;
		//frmImages.addNext(dummy=new mLabel(""),CellConstants.VSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST|CellConstants.NORTH));
		//dummy.setTag(INSETS,new Insets(0,0,2,0));
		frmImages.addLast(chkDescShowImg = new mCheckBox(MyLocale.getMsg(638,"Show pictures in description")),CellConstants.VSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST|CellConstants.NORTH));
		chkDescShowImg.setTag(INSETS,new Insets(0,0,2,0));
		if (pref.descShowImg) chkDescShowImg.setState(true);
		pnlDisplay.addLast(frmImages,CellConstants.STRETCH,CellConstants.FILL);

		Frame frmHintLog=new Frame();
		//frmHintLog.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_BOTTOM;
		frmHintLog.addNext(new mLabel(MyLocale.getMsg(630,"HintLogPanel:  Logs per page ")),CellConstants.DONTSTRETCH,CellConstants.DONTFILL);	
		frmHintLog.addLast(inpLogsPerPage=new mInput(),CellConstants.DONTSTRETCH,CellConstants.DONTFILL|CellConstants.EAST);
		inpLogsPerPage.setText(Convert.toString(pref.logsPerPage));
		inpLogsPerPage.setPreferredSize(40,-1);
		//inpLogsPerPage.setTag(INSETS,new Insets(0,0,2,0));
		//lblHlP.setTag(INSETS,new Insets(6,0,2,0));

		frmHintLog.addNext(new mLabel(MyLocale.getMsg(633,"Max. logs to spider")),CellConstants.DONTSTRETCH,CellConstants.DONTFILL);	
		frmHintLog.addLast(inpMaxLogsToSpider=new mInput(),CellConstants.DONTSTRETCH,CellConstants.DONTFILL|CellConstants.EAST);
		inpMaxLogsToSpider.setText(Convert.toString(pref.maxLogsToSpider));
		inpMaxLogsToSpider.setPreferredSize(40,-1);
		
		String [] spiderUpdateOptions = { MyLocale.getMsg(640,"Yes"), MyLocale.getMsg(641,"No"), MyLocale.getMsg(642,"Ask") };
		frmHintLog.addNext(new mLabel( MyLocale.getMsg(639,"Update caches when spidering?") ),DONTSTRETCH,DONTFILL|WEST);
		frmHintLog.addLast(inpSpiderUpdates=new mChoice(spiderUpdateOptions, pref.spiderUpdates),DONTSTRETCH,DONTFILL|WEST);
		pnlDisplay.addLast(frmHintLog,CellConstants.STRETCH,CellConstants.FILL);

		/////////////////////////////////////////////////////////
		// Third panel - More
		/////////////////////////////////////////////////////////
		CellPanel pnlProxy=new CellPanel();
		pnlProxy.addNext(new mLabel("Proxy"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlProxy.addLast(Proxy = new mInput(),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST)).setTag(SPAN,new Dimension(2,1));
		Proxy.setText(pref.myproxy);
		pnlProxy.addNext(new mLabel("Port"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlProxy.addLast(ProxyPort = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		ProxyPort.setText(pref.myproxyport);
		pnlProxy.addNext(new mLabel(""),HSTRETCH,HFILL);
		pnlProxy.addLast(chkProxyActive=new mCheckBox(MyLocale.getMsg(634,"use Proxy")));
		chkProxyActive.setState(pref.proxyActive);
		pnlMore.addLast(pnlProxy,HSTRETCH,HFILL);
		pnlMore.addNext(new mLabel(MyLocale.getMsg(592,"Language (needs restart)")),DONTSTRETCH,DONTFILL|WEST);
		String[] tmp = (new FileBugfix(FileBase.getProgramDirectory()+"/languages").list("*.cfg", FileBase.LIST_FILES_ONLY)); //"*.xyz" doesn't work on some systems -> use FileBugFix
		if (tmp == null) tmp = new String[0];
		String [] langs = new String[tmp.length +1];
		langs[0] = "auto";
		int curlang = 0;
		for (int i = 0; i < tmp.length; i++) {
			langs[i+1] = tmp[i].substring(0, tmp[i].lastIndexOf('.'));
			if (langs[i+1].equalsIgnoreCase(MyLocale.language)) curlang = i+1 ;
		}
		//ewe.sys.Vm.copyArray(tmp, 0, langs, 1, tmp.length);
		pnlMore.addLast(inpLanguage=new mChoice(langs, curlang),DONTSTRETCH,DONTFILL|WEST);
		//inpLanguage.setPreferredSize(20,-1);
		inpLanguage.setToolTip(MyLocale.getMsg(591,"Select \"auto\" for system language or select your preferred language, e.g. DE or EN"));
		String [] metriken = {MyLocale.getMsg(589, "Metric (km)"), 
				              MyLocale.getMsg(590, "Imperial (mi)")};
		pnlMore.addNext(new mLabel(MyLocale.getMsg(588, "Length units")),DONTSTRETCH,DONTFILL|WEST);
		int currMetrik = pref.metricSystem == Metrics.METRIC ? 0 : 1;
		pnlMore.addLast(inpMetric=new mChoice(metriken, currMetrik),DONTSTRETCH,DONTFILL|WEST);
		pnlMore.addLast(chkSortingGroupedByCache=new mCheckBox(MyLocale.getMsg(647,"Sorting grouped by Cache")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));	
		chkSortingGroupedByCache.setState(pref.SortingGroupedByCache);
		pnlMore.addLast(chkuseOwnSymbols=new mCheckBox(MyLocale.getMsg(649,"use own symbols")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));	
		chkuseOwnSymbols.setState(pref.useOwnSymbols);
		pnlMore.addLast(chkDebug=new mCheckBox(MyLocale.getMsg(648,"Debug Mode")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));	
		chkDebug.setState(pref.debug);

		/////////////////////////////////////////////////////////
		// Fourth/Fifth panel - Listview and Travelbugs
		/////////////////////////////////////////////////////////

        mTab.addCard(pnlGeneral,MyLocale.getMsg(621,"General"),null);
		mTab.addCard(pnlDisplay,MyLocale.getMsg(622,"Screen"),null);
		mTab.addCard(pnlMore,MyLocale.getMsg(632,"More"),null);
		mTab.addCard(tccList=new TableColumnChooser(new String[] {
				MyLocale.getMsg(599,"checkbox"),
				MyLocale.getMsg(598,"type"),
				MyLocale.getMsg(606,"Difficulty"),
				MyLocale.getMsg(607,"Terrain"),
				MyLocale.getMsg(597,"waypoint"),
				MyLocale.getMsg(596,"name"),
				MyLocale.getMsg(608,"Location"),
				MyLocale.getMsg(609,"Owner"),
				MyLocale.getMsg(610,"Hidden"),
				MyLocale.getMsg(611,"Status"),
				MyLocale.getMsg(612,"Distance"),
				MyLocale.getMsg(613,"Bearing"),
				MyLocale.getMsg(635,"Size"),
				MyLocale.getMsg(636,"OC Empfehlungen"),
				MyLocale.getMsg(637,"OC Index"),
				MyLocale.getMsg(1039,"Solver exists"),
				MyLocale.getMsg(1041,"Note exists"),
				MyLocale.getMsg(1046,"# Additionals"),
				MyLocale.getMsg(1048, "# DNF Logs"),
				MyLocale.getMsg(1051, "Last sync date")
				},pref.listColMap),MyLocale.getMsg(595,"List"),null);

		mTab.addCard(tccBugs=new TableColumnChooser(new String[] {
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
		
		this.addLast(mTab);
		cancelB = new mButton(MyLocale.getMsg(614,"Cancel"));
		cancelB.setHotKey(0, IKeys.ESCAPE);
		addNext(cancelB,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		applyB = new mButton(MyLocale.getMsg(615,"Apply"));
		applyB.setHotKey(0, IKeys.ACTION);
		addLast(applyB,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
	}
	
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB){
				this.close(0);
			}
			if (ev.target == applyB){
				//if (pref.currProfile == 0){
					//pref.curCentrePt.set(btnCentre.getText());
					pref.setBaseDir(DataDir.getText());
				//}
				pref.fontSize = Convert.toInt(fontSize.getText());
				if (pref.fontSize<6) pref.fontSize=11;
				pref.logsPerPage=Common.parseInt(inpLogsPerPage.getText());
				if (pref.logsPerPage==0) pref.logsPerPage=pref.DEFAULT_LOGS_PER_PAGE;
				pref.maxLogsToSpider=Common.parseInt(inpMaxLogsToSpider.getText());
				if (pref.maxLogsToSpider==0) pref.maxLogsToSpider=pref.DEFAULT_MAX_LOGS_TO_SPIDER;
				
				Font defaultGuiFont = mApp.findFont("gui");
				int sz = (pref.fontSize);
				Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz); 
				mApp.addFont(newGuiFont, "gui"); 
				mApp.fontsChanged();
				mApp.mainApp.font = newGuiFont;
				
				pref.myAlias = Alias.getText().trim();
				SpiderGC.passwort=pref.password= inpPassword.getText().trim();
				MyLocale.saveLanguage(MyLocale.language=inpLanguage.getText().toUpperCase().trim());
				pref.browser = Browser.getText();
				//Vm.debug(myPreferences.browser);
				pref.myproxy = Proxy.getText();
				pref.myproxyport = ProxyPort.getText();
				pref.proxyActive=chkProxyActive.getState();
				HttpConnection.setProxy(pref.myproxy, Common.parseInt(pref.myproxyport), pref.proxyActive); // TODO generate an error message if proxy port is not a number
				//myPreferences.nLogs = Convert.parseInt(nLogs.getText());
				pref.autoReloadLastProfile=chkAutoLoad.getState();
				pref.setCurrentCentreFromGPSPosition=chkSetCurrentCentreFromGPSPosition.getState();
				pref.showDeletedImages=chkShowDeletedImg.getState();
				pref.garminConn=chcGarminPort.getSelectedItem().toString();
				pref.garminGPSBabelOptions=chkSynthShort.state?"-s":"";
				pref.menuAtTop=chkMenuAtTop.getState();
				pref.tabsAtTop=chkTabsAtTop.getState();
				pref.showStatus=chkShowStatus.getState();
				pref.hasCloseButton=chkHasCloseButton.getState();
				pref.travelbugColMap=tccBugs.getSelectedCols();
				pref.listColMap=tccList.getSelectedCols();
				pref.descShowImg=chkDescShowImg.getState();
				Global.mainTab.tbP.myMod.setColumnNamesAndWidths();
				pref.metricSystem = inpMetric.getInt() == 0 ? Metrics.METRIC : Metrics.IMPERIAL;
				pref.spiderUpdates = inpSpiderUpdates.getInt();
				pref.addDetailsToWaypoint = chkAddDetailsToWaypoint.getState();
				pref.addDetailsToName = chkAddDetailsToName.getState();
				pref.SortingGroupedByCache=chkSortingGroupedByCache.getState();
				pref.useOwnSymbols=chkuseOwnSymbols.getState();
				pref.debug=chkDebug.getState();

				pref.savePreferences();
				pref.dirty = true; // Need to update table in case columns were enabled/disabled
				this.close(0);
			}
			if(ev.target == brwBt){
				FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, pref.getBaseDir());
				fc.setTitle(MyLocale.getMsg(616,"Select directory"));
				if(fc.execute() != FormBase.IDCANCEL)	DataDir.setText(fc.getChosen()+"/");
			}
			if (ev.target == gpsB){
				GPSPortOptions gpo = new GPSPortOptions();
				gpo.portName = pref.mySPO.portName;
				gpo.baudRate = pref.mySPO.baudRate;
				Editor s = gpo.getEditor();
				gpo.forwardGpsChkB.setState(pref.forwardGPS);
				gpo.inputBoxForwardHost.setText(pref.forwardGpsHost);
				gpo.gpsdChkB.setState(pref.useGPSD);
				if(pref.gpsdPort!=pref.DEFAULT_GPSD_PORT){
					gpo.inputBoxGpsdHost.setText(pref.gpsdHost + ":" + Convert.toString(pref.gpsdPort));
				}else{
					gpo.inputBoxGpsdHost.setText(pref.gpsdHost);
				}
				gpo.logGpsChkB.setState(pref.logGPS);
				gpo.inputBoxLogTimer.setText(pref.logGPSTimer);
				Gui.setOKCancel(s);
				if (s.execute()== FormBase.IDOK) {
					pref.mySPO.portName = gpo.portName; 
					pref.mySPO.baudRate = gpo.baudRate;
					pref.forwardGPS = gpo.forwardGpsChkB.getState();
					pref.forwardGpsHost = gpo.inputBoxForwardHost.getText();
					pref.useGPSD = gpo.gpsdChkB.getState();
					String gpsdHostString = gpo.inputBoxGpsdHost.getText();	// hostname[:port]
					int posColon = gpsdHostString.indexOf(':');
					if(posColon>=0){
						pref.gpsdHost=gpsdHostString.substring(0,posColon);
						pref.gpsdPort=Convert.toInt(gpsdHostString.substring(posColon+1));
					}else{
						pref.gpsdHost=gpsdHostString;
						pref.gpsdPort=pref.DEFAULT_GPSD_PORT;
					}
					pref.logGPS = gpo.logGpsChkB.getState();
					pref.logGPSTimer = gpo.inputBoxLogTimer.getText();
					gpsB.setText("GPS: " + pref.mySPO.portName+"/"+pref.mySPO.baudRate);
				}
			}
		}
		super.onEvent(ev);
	}
	
}
