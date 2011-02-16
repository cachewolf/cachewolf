    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */
package CacheWolf;

import CacheWolf.navi.Metrics;
import CacheWolf.utils.FileBugfix;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.Dimension;
import ewe.fx.Font;
import ewe.fx.Insets;
import ewe.io.FileBase;
import ewe.sys.Convert;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Editor;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Frame;
import ewe.ui.Gui;
import ewe.ui.IKeys;
import ewe.ui.UIConstants;
import ewe.ui.mApp;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.mTabbedPanel;

/**
*	This class displays a user interface allowing the user to change and set
*	preferences. It also provides a method to save the changed preferences that
*	are saved immediatly when the user presses "Apply".
*	Class ID=600
*/
public class PreferencesScreen extends Form {
	mButton cancelB, applyB, brwBt, gpsB;
	mChoice inpLanguage, inpMetric, inpSpiderUpdates;
	mInput DataDir, Proxy, ProxyPort, Alias, nLogs, Browser, fontName, fontSize, 
	       inpLogsPerPage,inpMaxLogsToSpider,inpPassword,inpGcMemberID;
	mCheckBox chkAutoLoad, chkShowDeletedImg, chkMenuAtTop, chkTabsAtTop, chkShowStatus,chkHasCloseButton,
	          chkSynthShort,chkProxyActive, chkDescShowImg, chkAddDetailsToWaypoint, chkAddDetailsToName, 
	          chkSetCurrentCentreFromGPSPosition,chkSortingGroupedByCache,chkuseOwnSymbols,chkDebug,chkPM;
	mTabbedPanel mTab;
	mChoice chcGarminPort;
	mLabel lblGarmin;
	TableColumnChooser tccBugs,tccList;
	
	Preferences pref;
	
	CellPanel pnlGeneral = new CellPanel();
	CellPanel pnlDisplay = new CellPanel();
	CellPanel pnlMore = new CellPanel();
	CellPanel pnlTB = new CellPanel();

	// ScrollBarPanel scp;
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
		else if(pref.fontSize <= 28){
			// was for <=16 setPreferredSize(288,252);
			setPreferredSize(pref.fontSize*20,pref.fontSize*18);
		}
		else if(pref.fontSize <= 20){
			setPreferredSize(352,302);
		}
		else if(pref.fontSize <= 24){
			setPreferredSize(420,350);
		}
		else if(pref.fontSize <= 28){
			setPreferredSize(480,390);
		}
		else{
			setPreferredSize(576,512);
		}
		
		//scp = new ScrollBarPanel(pnlGeneral);
		
		/////////////////////////////////////////////////////////
		// First panel - General
		/////////////////////////////////////////////////////////
		CellPanel cpDataDir=new CellPanel();
		cpDataDir.addNext(new mLabel(MyLocale.getMsg(603,"Data Directory:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		DataDir = new mInput();
		DataDir.setText(pref.getBaseDir());
		cpDataDir.addNext(DataDir,CellConstants.STRETCH, (CellConstants.FILL|CellConstants.LEFT));
		cpDataDir.addLast(brwBt = new mButton(MyLocale.getMsg(604,"Browse")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.RIGHT));
		cpDataDir.addNext(chkAutoLoad = new mCheckBox(MyLocale.getMsg(629,"Autoload last profile")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		if (pref.autoReloadLastProfile) chkAutoLoad.setState(true);
		cpDataDir.addNext(chkSetCurrentCentreFromGPSPosition = new mCheckBox(MyLocale.getMsg(646,"centre from GPS")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.RIGHT));
		if (pref.setCurrentCentreFromGPSPosition) chkSetCurrentCentreFromGPSPosition.setState(true);
		pnlGeneral.addLast(separator(cpDataDir),HSTRETCH,HFILL);
		
		CellPanel cpBrowser=new CellPanel();
		cpBrowser.addNext(new mLabel("Browser:"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		cpBrowser.addLast(Browser = new mInput(pref.browser),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.LEFT));
		cpBrowser.addNext(new mLabel(MyLocale.getMsg(601,"Your Alias:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		
		cpBrowser.addNext(Alias = new mInput(pref.myAlias),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		cpBrowser.addNext(new mLabel(MyLocale.getMsg(594,"Pwd")));
		cpBrowser.addLast(inpPassword=new mInput(pref.password),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		inpPassword.setToolTip(MyLocale.getMsg(593,"Password is optional here.\nEnter only if you want to store it in pref.xml"));
		inpPassword.isPassword=true;
		cpBrowser.addNext(chkPM=new mCheckBox("PM"));
		if (pref.isPremium) chkPM.setState(true);
		cpBrowser.addNext(new mLabel(MyLocale.getMsg(650,"GcMemberID:")));
		cpBrowser.addLast(inpGcMemberID=new mInput(pref.gcMemberId),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		
		pnlGeneral.addLast(separator(cpBrowser),HSTRETCH,HFILL);
		
		CellPanel cpGPS=new CellPanel();
		cpGPS.addNext(new mLabel("GPS: "),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		cpGPS.addLast(gpsB = new mButton(MyLocale.getMsg(600,"Preferences")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		// "GPS: " + (pref.useGPSD ? "gpsd " + pref.gpsdHost : pref.mySPO.portName+"/"+pref.mySPO.baudRate) 
		pnlGeneral.addLast(separator(cpGPS),HSTRETCH,HFILL);

		// Garmin and GPSBabel
		CellPanel cpBabel=new CellPanel();
		cpBabel.addNext(lblGarmin=new mLabel(MyLocale.getMsg(173,"Garmin:  PC Port:")),DONTSTRETCH,LEFT);
		cpBabel.addNext(chcGarminPort=new mChoice(garminPorts,0),DONTSTRETCH,RIGHT);
		chcGarminPort.selectItem(pref.garminConn);
		cpBabel.addLast(chkSynthShort=new mCheckBox(MyLocale.getMsg(174,"Short Names")),STRETCH,LEFT);
		chkSynthShort.setState(!pref.garminGPSBabelOptions.equals(""));
		cpBabel.addNext(new mLabel(MyLocale.getMsg(643,"Append cache details to:")),DONTSTRETCH,LEFT);
		cpBabel.addNext(chkAddDetailsToWaypoint=new mCheckBox(MyLocale.getMsg(644,"waypoints")),DONTSTRETCH,RIGHT);
		chkAddDetailsToWaypoint.setState(pref.addDetailsToWaypoint);
		cpBabel.addLast(chkAddDetailsToName=new mCheckBox(MyLocale.getMsg(645,"names")),STRETCH,LEFT);
		chkAddDetailsToName.setState(pref.addDetailsToName);
		pnlGeneral.addLast(cpBabel,HSTRETCH,HFILL);
		
		/////////////////////////////////////////////////////////
		// Second panel - Screen
		/////////////////////////////////////////////////////////
		
		CellPanel pnlScreen=new CellPanel();
		Frame frmScreen=new Frame();
		frmScreen.borderStyle=UIConstants.BDR_RAISEDOUTER|UIConstants.BDR_SUNKENINNER;
		pnlScreen.addNext(new mLabel(MyLocale.getMsg(625,"Screen (needs restart):")));
		pnlScreen.addNext(new mLabel("Font"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		pnlScreen.addNext(fontName = new mInput(),CellConstants.STRETCH, (CellConstants.HFILL|CellConstants.LEFT));
		fontName.maxLength=50;
		fontName.setText(pref.fontName);
		pnlScreen.addLast(fontSize = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.LEFT));
		fontSize.maxLength=2;
		fontSize.setPreferredSize(2*pref.fontSize,-1);
		fontSize.setText(Convert.toString(pref.fontSize));
		frmScreen.addLast(pnlScreen,HSTRETCH,HFILL);
		
		frmScreen.addLast(chkHasCloseButton=new mCheckBox(MyLocale.getMsg(631,"PDA has close Button")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));	
    	//lblTitle.setTag(INSETS,new Insets(2,0,0,0));
        chkHasCloseButton.setState(pref.hasCloseButton);
		frmScreen.addNext(chkMenuAtTop = new mCheckBox(MyLocale.getMsg(626,"Menu top")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		chkMenuAtTop.setTag(INSETS,new Insets(0,0,2,0));
		chkMenuAtTop.setState(pref.menuAtTop);
		frmScreen.addNext(chkTabsAtTop = new mCheckBox(MyLocale.getMsg(627,"Tabs top")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		chkTabsAtTop.setState(pref.tabsAtTop);
		chkTabsAtTop.setTag(INSETS,new Insets(0,0,2,0));
		frmScreen.addLast(chkShowStatus = new mCheckBox(MyLocale.getMsg(628,"Status")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		chkShowStatus.setState(pref.showStatus);
		chkShowStatus.setTag(INSETS,new Insets(0,0,2,0));
		pnlDisplay.addLast(frmScreen,CellConstants.HSTRETCH,CellConstants.FILL);
		
		Frame frmImages=new Frame();
		frmImages.borderStyle=UIConstants.BDR_RAISEDOUTER|UIConstants.BDR_SUNKENINNER|UIConstants.BF_TOP|UIConstants.BF_BOTTOM;
		//frmImages.addNext(new mLabel(MyLocale.getMsg(623,"Images:")),CellConstants.VSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		frmImages.addLast(chkShowDeletedImg = new mCheckBox(MyLocale.getMsg(624,"Show deleted images")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		chkShowDeletedImg.setTag(INSETS,new Insets(2,0,0,0));
		if (pref.showDeletedImages) chkShowDeletedImg.setState(true);
		//mLabel dummy;
		//frmImages.addNext(dummy=new mLabel(""),CellConstants.VSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT|CellConstants.NORTH));
		//dummy.setTag(INSETS,new Insets(0,0,2,0));
		frmImages.addLast(chkDescShowImg = new mCheckBox(MyLocale.getMsg(638,"Show pictures in description")),CellConstants.VSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT|CellConstants.NORTH));
		chkDescShowImg.setTag(INSETS,new Insets(0,0,2,0));
		if (pref.descShowImg) chkDescShowImg.setState(true);
		pnlDisplay.addLast(frmImages,CellConstants.STRETCH,CellConstants.FILL);

		Frame frmHintLog=new Frame();
		//frmHintLog.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_BOTTOM;
		frmHintLog.addNext(new mLabel(MyLocale.getMsg(630,"HintLogPanel:  Logs per page ")),CellConstants.DONTSTRETCH,CellConstants.DONTFILL);	
		frmHintLog.addLast(inpLogsPerPage=new mInput(),CellConstants.DONTSTRETCH,CellConstants.DONTFILL|CellConstants.RIGHT);
		inpLogsPerPage.setText(Convert.toString(pref.logsPerPage));
		inpLogsPerPage.setPreferredSize(40,-1);
		//inpLogsPerPage.setTag(INSETS,new Insets(0,0,2,0));
		//lblHlP.setTag(INSETS,new Insets(6,0,2,0));

		frmHintLog.addNext(new mLabel(MyLocale.getMsg(633,"Max. logs to spider")),CellConstants.DONTSTRETCH,CellConstants.DONTFILL);	
		frmHintLog.addLast(inpMaxLogsToSpider=new mInput(),CellConstants.DONTSTRETCH,CellConstants.DONTFILL|CellConstants.RIGHT);
		inpMaxLogsToSpider.setText(Convert.toString(pref.maxLogsToSpider));
		inpMaxLogsToSpider.setPreferredSize(40,-1);
		
		String [] spiderUpdateOptions = { MyLocale.getMsg(640,"Yes"), MyLocale.getMsg(641,"No"), MyLocale.getMsg(642,"Ask") };
		frmHintLog.addNext(new mLabel( MyLocale.getMsg(639,"Update caches when spidering?") ),DONTSTRETCH,DONTFILL|LEFT);
		frmHintLog.addLast(inpSpiderUpdates=new mChoice(spiderUpdateOptions, pref.spiderUpdates),DONTSTRETCH,DONTFILL|LEFT);
		pnlDisplay.addLast(frmHintLog,CellConstants.STRETCH,CellConstants.FILL);

		/////////////////////////////////////////////////////////
		// Third panel - More
		/////////////////////////////////////////////////////////
		CellPanel pnlProxy=new CellPanel();
		pnlProxy.addNext(new mLabel("Proxy"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		pnlProxy.addLast(Proxy = new mInput(),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.LEFT)).setTag(SPAN,new Dimension(2,1));
		Proxy.setText(pref.myproxy);
		pnlProxy.addNext(new mLabel("Port"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		pnlProxy.addLast(ProxyPort = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		ProxyPort.setText(pref.myproxyport);
		pnlProxy.addNext(new mLabel(""),HSTRETCH,HFILL);
		pnlProxy.addLast(chkProxyActive=new mCheckBox(MyLocale.getMsg(634,"use Proxy")));
		chkProxyActive.setState(pref.proxyActive);
		pnlMore.addLast(pnlProxy,HSTRETCH,HFILL);
		pnlMore.addNext(new mLabel(MyLocale.getMsg(592,"Language (needs restart)")),DONTSTRETCH,DONTFILL|LEFT);
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
		pnlMore.addLast(inpLanguage=new mChoice(langs, curlang),DONTSTRETCH,DONTFILL|LEFT);
		//inpLanguage.setPreferredSize(20,-1);
		inpLanguage.setToolTip(MyLocale.getMsg(591,"Select \"auto\" for system language or select your preferred language, e.g. DE or EN"));
		String [] metriken = {MyLocale.getMsg(589, "Metric (km)"), 
				              MyLocale.getMsg(590, "Imperial (mi)")};
		pnlMore.addNext(new mLabel(MyLocale.getMsg(588, "Length units")),DONTSTRETCH,DONTFILL|LEFT);
		int currMetrik = pref.metricSystem == Metrics.METRIC ? 0 : 1;
		pnlMore.addLast(inpMetric=new mChoice(metriken, currMetrik),DONTSTRETCH,DONTFILL|LEFT);
		pnlMore.addLast(chkSortingGroupedByCache=new mCheckBox(MyLocale.getMsg(647,"Sorting grouped by Cache")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));	
		chkSortingGroupedByCache.setState(pref.SortingGroupedByCache);
		pnlMore.addLast(chkuseOwnSymbols=new mCheckBox(MyLocale.getMsg(649,"use own symbols")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));	
		chkuseOwnSymbols.setState(pref.useOwnSymbols);
		pnlMore.addLast(chkDebug=new mCheckBox(MyLocale.getMsg(648,"Debug Mode")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));	
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
		addNext(cancelB,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
		applyB = new mButton(MyLocale.getMsg(615,"Apply"));
		applyB.setHotKey(0, IKeys.ACTION);
		addLast(applyB,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.LEFT));
	}
	
	private CellPanel separator(CellPanel pnl) {
		CellPanel outerPnl = new CellPanel();
		pnl.setTag(INSETS,new Insets(0,0,2,0));
		outerPnl.borderStyle=UIConstants.BDR_RAISEDOUTER|UIConstants.BDR_SUNKENINNER|UIConstants.BF_BOTTOM;
		outerPnl.setTag(INSETS,new Insets(0,0,2,0));
		outerPnl.addLast(pnl,HSTRETCH,HFILL);
		return outerPnl;
	}
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB){
				this.close(0);
			}
			if (ev.target == applyB){
				pref.setBaseDir(DataDir.getText());
				pref.fontSize = Convert.toInt(fontSize.getText());
				if (pref.fontSize<6) pref.fontSize=11;
				pref.fontName=fontName.getText();
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
				pref.password= inpPassword.getText().trim();
				pref.gcMemberId=inpGcMemberID.getText().trim();
				MyLocale.saveLanguage(MyLocale.language=inpLanguage.getText().toUpperCase().trim());
				pref.browser = Browser.getText();
				pref.myproxy = Proxy.getText();
				pref.myproxyport = ProxyPort.getText();
				pref.proxyActive=chkProxyActive.getState();
				HttpConnection.setProxy(pref.myproxy, Common.parseInt(pref.myproxyport), pref.proxyActive); // TODO generate an error message if proxy port is not a number
				pref.autoReloadLastProfile=chkAutoLoad.getState();
				pref.isPremium=chkPM.getState();
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
				gpo.chcUseGpsd.select(pref.useGPSD);
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
					pref.useGPSD = gpo.chcUseGpsd.getInt();
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
