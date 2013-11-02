/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://www.cachewolf.de/ for more information.
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
import ewe.fx.IconAndText;
import ewe.fx.Image;
import ewe.fx.mImage;
import ewe.io.FileBase;
import ewe.sys.Convert;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Editor;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.mApp;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.mTabbedPanel;

/**
 * This class displays a user interface allowing the user to change and set preferences. It also provides a method to
 * save the changed preferences that are saved immediately when the user presses "Apply". Class ID=600
 */
public class PreferencesScreen extends Form {
	private final ExecutePanel executePanel;
	mButton DataDirBrowseButton, MapsDirBrowseButton, gpsButton;
	mChoice inpLanguage, inpMetric, inpSpiderUpdates, chcGarminPort;
	mInput DataDir, MapsDir, Proxy, ProxyPort, Alias, Alias2, Browser, fontName, fontSize, inpLogsPerPage, inpGcMemberID, inpUserID;
	mCheckBox chkAutoLoad, chkShowDeletedImg;
	mCheckBox chkNoTabs, chkTabsAtTop, chkShowStatus, chkHasCloseButton, chkUseRadar, chkUseText, chkUseIcons, chkUseBigIcons;
	mCheckBox chkSynthShort, chkProxyActive, chkDescShowImg, chkAddDetailsToWaypoint, chkAddDetailsToName, chkSortingGroupedByCache, chkDebug, chkPM;
	TableColumnChooser tccList, tccBugs;

	mInput inpPassword;
	String imagesize = "";

	public PreferencesScreen() {
		int sw = MyLocale.getScreenWidth();
		int sh = MyLocale.getScreenHeight();

		mTabbedPanel mTab = new mTabbedPanel();

		this.title = MyLocale.getMsg(600, "Preferences");
		if ((sw > 240) && (sh > 240))
			this.resizable = true;
		// this.moveable = true;
		// this.windowFlagsToSet = Window.FLAG_MAXIMIZE;

		// set dialog-width according to fontsize
		if ((Global.pref.fontSize <= 13) || (sw <= 240) || (sh <= 240)) {
			setPreferredSize(240, 240);
		}
		else if (Global.pref.fontSize <= 28) {
			// was for <=16 setPreferredSize(288,252);
			setPreferredSize(Global.pref.fontSize * 21, Global.pref.fontSize * 19);
		}
		/*
		else if (Global.pref.fontSize <= 20) {
			setPreferredSize(352, 302);
		}
		else if (Global.pref.fontSize <= 24) {
			setPreferredSize(420, 350);
		}
		else if (Global.pref.fontSize <= 28) {
			setPreferredSize(480, 390);
		}
		else {
			setPreferredSize(576, 512);
		}
		*/

		// ///////////////////////////////////////////////////////
		// Card General
		// ///////////////////////////////////////////////////////
		if (Global.pref.useBigIcons)
			imagesize = "_vga";
		CellPanel pnlGeneral = new CellPanel();

		CellPanel cpDataDir = new CellPanel();
		cpDataDir.setText(MyLocale.getMsg(603, "Data Directory:"));
		DataDir = new mInput();
		DataDir.setText(Global.pref.baseDir);
		cpDataDir.addLast(DataDir, STRETCH, HFILL);
		cpDataDir.addNext(chkAutoLoad = new mCheckBox(MyLocale.getMsg(629, "Autoload last profile")), DONTSTRETCH, DONTFILL | LEFT);
		if (Global.pref.autoReloadLastProfile)
			chkAutoLoad.setState(true);
		DataDirBrowseButton = new mButton();
		DataDirBrowseButton.image = new IconAndText(new mImage("search.png"), MyLocale.getMsg(604, "Browse"), null);
		cpDataDir.addLast(DataDirBrowseButton, DONTSTRETCH, RIGHT);
		pnlGeneral.addLast(cpDataDir, HSTRETCH, HFILL);

		CellPanel UserDataPanel = new CellPanel();
		UserDataPanel.setText(MyLocale.getMsg(659, "Account"));
		UserDataPanel.addNext(new mLabel(MyLocale.getMsg(601, "Your alias:")), DONTSTRETCH, DONTFILL | LEFT);
		UserDataPanel.addNext(Alias = new mInput(Global.pref.myAlias), STRETCH, HFILL);
		UserDataPanel.addLast(chkPM = new mCheckBox("PM"), DONTSTRETCH, DONTFILL | LEFT);
		if (Global.pref.isPremium)
			chkPM.setState(true);
		UserDataPanel.addNext(new mLabel(MyLocale.getMsg(658, "User ID:")), DONTSTRETCH, DONTFILL | LEFT);
		UserDataPanel.addLast(inpUserID = new mInput(Global.pref.userID), STRETCH, HFILL);
		UserDataPanel.addNext(new mLabel(MyLocale.getMsg(650, "GcMemberID:")), DONTSTRETCH, DONTFILL | LEFT);
		UserDataPanel.addLast(inpGcMemberID = new mInput(Global.pref.gcMemberId), STRETCH, HFILL);
		UserDataPanel.addNext(new mLabel(MyLocale.getMsg(657, "Second alias:")), DONTSTRETCH, DONTFILL | LEFT);
		UserDataPanel.addLast(Alias2 = new mInput(Global.pref.myAlias2), STRETCH, HFILL);
		UserDataPanel.addNext(new mLabel(MyLocale.getMsg(594, "Pwd")), DONTSTRETCH, DONTFILL | LEFT);
		UserDataPanel.addLast(inpPassword = new mInput(Global.pref.password), STRETCH, HFILL);
		inpPassword.setToolTip(MyLocale.getMsg(593, "Password is optional here.\nEnter only if you want to store it in Global.pref.xml"));
		inpPassword.isPassword = true;
		pnlGeneral.addLast(UserDataPanel, HSTRETCH, HFILL);

		mTab.addCard(pnlGeneral, MyLocale.getMsg(621, "General"), null).iconize(new Image("person" + imagesize + ".png"), true);
		// ///////////////////////////////////////////////////////
		// Card Maps / GPS
		// ///////////////////////////////////////////////////////
		CellPanel pnlGPSMaps = new CellPanel();

		CellPanel cpMaps = new CellPanel();
		cpMaps.setText(MyLocale.getMsg(654, "Kartenverzeichnis"));
		MapsDir = new mInput();
		MapsDir.setText(Global.pref.mapsBaseDir);
		cpMaps.addLast(MapsDir, STRETCH, (FILL | LEFT));
		MapsDirBrowseButton = new mButton();
		MapsDirBrowseButton.image = new IconAndText(new mImage("search.png"), MyLocale.getMsg(604, "Browse"), null);
		cpMaps.addLast(MapsDirBrowseButton, DONTSTRETCH, DONTFILL | RIGHT);
		pnlGPSMaps.addLast(cpMaps, HSTRETCH, HFILL);

		CellPanel cpGPS = new CellPanel();
		cpGPS.setText("GPS");
		cpGPS.addLast(gpsButton = new mButton(), HSTRETCH, HFILL);
		gpsButton.image = new IconAndText(FormBase.tools, MyLocale.getMsg(600, "Preferences"), null);
		pnlGPSMaps.addLast(cpGPS, HSTRETCH, HFILL);

		mTab.addCard(pnlGPSMaps, MyLocale.getMsg(655, "Maps/GPS"), null).iconize(new Image("globe" + imagesize + ".png"), true);
		// ///////////////////////////////////////////////////////
		// Card Export (Garmin and GPSBabel)
		// ///////////////////////////////////////////////////////
		CellPanel pnlImportExport = new CellPanel();

		CellPanel SpiderPanel = new CellPanel();
		SpiderPanel.setText(MyLocale.getMsg(175, "Import"));
		SpiderPanel.addNext(new mLabel(MyLocale.getMsg(639, "Update changed caches?")), DONTSTRETCH, DONTFILL | LEFT);
		String[] spiderUpdateOptions = { MyLocale.getMsg(640, "Yes"), MyLocale.getMsg(641, "No"), MyLocale.getMsg(642, "Ask") };
		SpiderPanel.addLast(inpSpiderUpdates = new mChoice(spiderUpdateOptions, Global.pref.spiderUpdates), DONTSTRETCH, DONTFILL | LEFT);
		pnlImportExport.addLast(SpiderPanel, HSTRETCH, HFILL);

		CellPanel ExportPanel = new CellPanel();
		ExportPanel.setText(MyLocale.getMsg(107, "Export"));

		CellPanel locExportPanel = new CellPanel();
		locExportPanel.setText(MyLocale.getMsg(215, "to LOC") + " + " + MyLocale.getMsg(122, "zum GPS mit GPSBabel"));
		// loc Exporter
		locExportPanel.addLast(new mLabel(MyLocale.getMsg(643, "Append cache details to:")), DONTSTRETCH, LEFT);
		locExportPanel.addNext(chkAddDetailsToWaypoint = new mCheckBox(MyLocale.getMsg(644, "waypoints")), DONTSTRETCH, LEFT);
		chkAddDetailsToWaypoint.setState(Global.pref.addDetailsToWaypoint);
		// loc Exporter
		locExportPanel.addLast(chkAddDetailsToName = new mCheckBox(MyLocale.getMsg(645, "names")), DONTSTRETCH, LEFT);
		chkAddDetailsToName.setState(Global.pref.addDetailsToName);
		ExportPanel.addLast(locExportPanel, HSTRETCH, HFILL);

		CellPanel cpBabel = new CellPanel();
		// first loc-file will be generated
		cpBabel.setText(MyLocale.getMsg(122, "zum GPS mit GPSBabel"));
		// GPSBabel Port
		cpBabel.addNext(new mLabel(MyLocale.getMsg(173, "Port:")), DONTSTRETCH, LEFT);
		cpBabel.addLast(chcGarminPort = new mChoice(new String[] { "com1", "com2", "com3", "com4", "com5", "com6", "com7", "usb" }, 0), STRETCH, LEFT);
		chcGarminPort.selectItem(Global.pref.garminConn);
		// GPSBabeloption -s
		cpBabel.addLast(chkSynthShort = new mCheckBox(MyLocale.getMsg(174, "Shorten Cachenames?")), DONTSTRETCH, LEFT);
		chkSynthShort.setState(!Global.pref.garminGPSBabelOptions.equals(""));
		ExportPanel.addLast(cpBabel, HSTRETCH, HFILL);

		pnlImportExport.addLast(ExportPanel);

		mTab.addCard(pnlImportExport, MyLocale.getMsg(656, "Import/Export"), null).iconize(new Image("database" + imagesize + ".png"), true);
		// ///////////////////////////////////////////////////////
		// Card Screen
		// ///////////////////////////////////////////////////////
		CellPanel pnlDisplay = new CellPanel();

		CellPanel pnlScreen = new CellPanel();
		pnlScreen.setText(MyLocale.getMsg(625, "Screen (needs restart):"));

		CellPanel pnlLanguage = new CellPanel();
		pnlLanguage.addNext(new mLabel(MyLocale.getMsg(592, "Language (needs restart)")), DONTSTRETCH, DONTFILL | LEFT);
		String[] tmp = (new FileBugfix(FileBase.getProgramDirectory() + "/languages").list("*.cfg", FileBase.LIST_FILES_ONLY));
		if (tmp == null)
			tmp = new String[0];
		String[] langs = new String[tmp.length + 1];
		langs[0] = "auto";
		int curlang = 0;
		for (int i = 0; i < tmp.length; i++) {
			langs[i + 1] = tmp[i].substring(0, tmp[i].lastIndexOf('.'));
			if (langs[i + 1].equalsIgnoreCase(MyLocale.language))
				curlang = i + 1;
		}
		// ewe.sys.Vm.copyArray(tmp, 0, langs, 1, tmp.length);
		pnlLanguage.addLast(inpLanguage = new mChoice(langs, curlang), DONTSTRETCH, DONTFILL | LEFT);
		// inpLanguage.setPreferredSize(20,-1);
		inpLanguage.setToolTip(MyLocale.getMsg(591, "Select \"auto\" for system language or select your preferred language, e.g. DE or EN"));
		pnlScreen.addLast(pnlLanguage);

		CellPanel pnlFont = new CellPanel();
		pnlFont.addNext(new mLabel("Font"), DONTSTRETCH, (DONTFILL | LEFT));
		pnlFont.addNext(fontName = new mInput(), DONTSTRETCH, (HFILL | LEFT));
		fontName.maxLength = 50;
		fontName.setText(Global.pref.fontName);
		pnlFont.addLast(fontSize = new mInput(), DONTSTRETCH, (HFILL | LEFT));
		fontSize.maxLength = 2;
		fontSize.setPreferredSize(2 * Global.pref.fontSize, -1);
		fontSize.setText(Convert.toString(Global.pref.fontSize));
		pnlScreen.addLast(pnlFont);

		pnlScreen.addLast(chkUseText = new mCheckBox(MyLocale.getMsg(664, "Show Text on Buttons")), DONTSTRETCH, DONTFILL | LEFT);
		chkUseText.setState(Global.pref.useText);
		pnlScreen.addNext(chkUseIcons = new mCheckBox(MyLocale.getMsg(665, "Show Icon on Buttons")), DONTSTRETCH, DONTFILL | LEFT);
		chkUseIcons.setState(Global.pref.useIcons);
		pnlScreen.addLast(chkUseBigIcons = new mCheckBox(MyLocale.getMsg(661, "Use big Icons")), DONTSTRETCH, DONTFILL | LEFT);
		chkUseBigIcons.setState(Global.pref.useBigIcons);
		pnlScreen.addLast(chkNoTabs = new mCheckBox(MyLocale.getMsg(1212, "Select tabs by button")), DONTSTRETCH, DONTFILL | LEFT);
		chkNoTabs.setState(Global.pref.noTabs);
		pnlScreen.addLast(chkTabsAtTop = new mCheckBox(MyLocale.getMsg(627, "Tabs at top")), DONTSTRETCH, DONTFILL | LEFT);
		chkTabsAtTop.setState(Global.pref.tabsAtTop);
		pnlScreen.addLast(chkShowStatus = new mCheckBox(MyLocale.getMsg(628, "Status")), DONTSTRETCH, DONTFILL | LEFT);
		chkShowStatus.setState(Global.pref.showStatus);

		pnlScreen.addLast(chkHasCloseButton = new mCheckBox(MyLocale.getMsg(631, "PDA has close Button")), DONTSTRETCH, DONTFILL | LEFT);
		chkHasCloseButton.setState(Global.pref.hasCloseButton);
		//pnlScreen.addLast(chkUseRadar = new mCheckBox(MyLocale.getMsg(660, "Show radartab on small screen")), DONTSTRETCH, DONTFILL | LEFT);
		//chkUseRadar.setState(Global.pref.useRadar);

		pnlDisplay.addLast(pnlScreen, HSTRETCH, HFILL);

		mTab.addCard(pnlDisplay, MyLocale.getMsg(622, "Screen"), null).iconize(new Image("monitor" + imagesize + ".png"), true);
		// ///////////////////////////////////////////////////////
		// Card Pages
		// ///////////////////////////////////////////////////////
		CellPanel pnlTabs = new CellPanel();

		CellPanel ListPanel = new CellPanel();
		ListPanel.setText(MyLocale.getMsg(1200, "List"));
		ListPanel.addLast(chkSortingGroupedByCache = new mCheckBox(MyLocale.getMsg(647, "Sorting grouped by Cache")), DONTSTRETCH, (DONTFILL | LEFT));
		chkSortingGroupedByCache.setState(Global.pref.SortingGroupedByCache);
		pnlTabs.addLast(ListPanel, HSTRETCH, HFILL);

		CellPanel DescriptionPanel = new CellPanel();
		DescriptionPanel.setText(MyLocale.getMsg(1202, "Description"));
		DescriptionPanel.addLast(chkDescShowImg = new mCheckBox(MyLocale.getMsg(638, "Show pictures in description?")), DONTSTRETCH, DONTFILL | LEFT);
		if (Global.pref.descShowImg)
			chkDescShowImg.setState(true);
		pnlTabs.addLast(DescriptionPanel, HSTRETCH, HFILL);

		CellPanel ImagesPanel = new CellPanel();
		ImagesPanel.setText(MyLocale.getMsg(1203, "Images"));
		ImagesPanel.addLast(chkShowDeletedImg = new mCheckBox(MyLocale.getMsg(624, "Show information \"missing image file\"?")), DONTSTRETCH, DONTFILL | LEFT);
		if (Global.pref.showDeletedImages)
			chkShowDeletedImg.setState(true);
		pnlTabs.addLast(ImagesPanel, HSTRETCH, HFILL);

		CellPanel LogsViewPanel = new CellPanel();
		LogsViewPanel.setText(MyLocale.getMsg(1204, "Hints & Logs"));
		LogsViewPanel.addNext(inpLogsPerPage = new mInput(), DONTSTRETCH, HSHRINK | LEFT);
		inpLogsPerPage.setPreferredSize(40, -1);
		LogsViewPanel.addLast(new mLabel(MyLocale.getMsg(630, "Logs per page (HintLogPanel)")), STRETCH, HFILL | LEFT);
		inpLogsPerPage.setText(Convert.toString(Global.pref.logsPerPage));
		pnlTabs.addLast(LogsViewPanel, HSTRETCH, HFILL);

		mTab.addCard(pnlTabs, MyLocale.getMsg(662, "Pages"), null).iconize(new Image("tabs" + imagesize + ".png"), true);
		// ///////////////////////////////////////////////////////
		// Card More
		// ///////////////////////////////////////////////////////
		CellPanel pnlMore = new CellPanel();

		CellPanel BrowserPanel = new CellPanel();
		BrowserPanel.setText("Browser:");
		BrowserPanel.addLast(Browser = new mInput(Global.pref.browser));
		pnlMore.addLast(BrowserPanel, HSTRETCH, HFILL);

		CellPanel pnlProxy = new CellPanel();
		pnlProxy.setText("Proxy");
		pnlProxy.addNext(new mLabel(""), HSTRETCH, HFILL);
		pnlProxy.addLast(Proxy = new mInput(), HSTRETCH, (HFILL | LEFT)).setTag(SPAN, new Dimension(2, 1));
		Proxy.setText(Global.pref.myproxy);
		pnlProxy.addNext(new mLabel("Port"), DONTSTRETCH, (DONTFILL | LEFT));
		pnlProxy.addLast(ProxyPort = new mInput(), DONTSTRETCH, (DONTFILL | LEFT));
		ProxyPort.setText(Global.pref.myproxyport);
		pnlProxy.addNext(new mLabel(""), HSTRETCH, HFILL);
		pnlProxy.addLast(chkProxyActive = new mCheckBox(MyLocale.getMsg(634, "use Proxy")));
		chkProxyActive.setState(Global.pref.proxyActive);
		pnlMore.addLast(pnlProxy, HSTRETCH, HFILL);

		CellPanel EtcPanel = new CellPanel();
		EtcPanel.setText(MyLocale.getMsg(632, "More"));
		String[] metriken = { MyLocale.getMsg(589, "Metric (km)"), MyLocale.getMsg(590, "Imperial (mi)") };
		EtcPanel.addNext(new mLabel(MyLocale.getMsg(588, "Length units")), DONTSTRETCH, DONTFILL | LEFT);
		int currMetrik = Global.pref.metricSystem == Metrics.METRIC ? 0 : 1;
		EtcPanel.addLast(inpMetric = new mChoice(metriken, currMetrik), DONTSTRETCH, DONTFILL | LEFT);
		EtcPanel.addLast(chkDebug = new mCheckBox(MyLocale.getMsg(648, "Debug Mode")), DONTSTRETCH, (DONTFILL | LEFT));
		chkDebug.setState(Global.pref.debug);
		pnlMore.addLast(EtcPanel, HSTRETCH, HFILL);

		mTab.addCard(pnlMore, MyLocale.getMsg(632, "More"), null).iconize(new Image("more" + imagesize + ".png"), true);
		// ///////////////////////////////////////////////////////
		// Card - Listview
		// ///////////////////////////////////////////////////////
		mTab.addCard(tccList = new TableColumnChooser(//
				new String[] { MyLocale.getMsg(599, "checkbox"), //
						MyLocale.getMsg(598, "type"), //
						MyLocale.getMsg(606, "Difficulty"), //
						MyLocale.getMsg(607, "Terrain"), //
						MyLocale.getMsg(597, "waypoint"), //
						MyLocale.getMsg(596, "name"), //
						MyLocale.getMsg(608, "Location"), //
						MyLocale.getMsg(609, "Owner"), //
						MyLocale.getMsg(610, "Hidden"), //
						MyLocale.getMsg(611, "Status"), //
						MyLocale.getMsg(612, "Distance"), //
						MyLocale.getMsg(613, "Bearing"), //
						MyLocale.getMsg(635, "Size"), //
						MyLocale.getMsg(636, "OC Empfehlungen"), //
						MyLocale.getMsg(637, "OC Index"), //
						MyLocale.getMsg(1039, "Solver exists"), //
						MyLocale.getMsg(1041, "Note exists"), //
						MyLocale.getMsg(1046, "# Additionals"), //
						MyLocale.getMsg(1048, "# DNF Logs"), //
						MyLocale.getMsg(1051, "Last sync date") //
				}, Global.pref.listColMap), MyLocale.getMsg(595, "List"), null).iconize(new Image("list" + imagesize + ".png"), true);

		// ///////////////////////////////////////////////////////
		// Card - Travelbugs
		// ///////////////////////////////////////////////////////
		mTab.addCard(tccBugs = new TableColumnChooser(//
				new String[] { MyLocale.getMsg(6000, "Guid"), //
						MyLocale.getMsg(6001, "Name"), //
						MyLocale.getMsg(6002, "track#"),//
						MyLocale.getMsg(6003, "Mission"),//
						MyLocale.getMsg(6004, "From Prof"),//
						MyLocale.getMsg(6005, "From Wpt"),//
						MyLocale.getMsg(6006, "From Date"),//
						MyLocale.getMsg(6007, "From Log"),//
						MyLocale.getMsg(6008, "To Prof"), //
						MyLocale.getMsg(6009, "To Wpt"), //
						MyLocale.getMsg(6010, "To Date"), //
						MyLocale.getMsg(6011, "To Log") //
				}, Global.pref.travelbugColMap), "T-bugs", null).iconize(new Image("bug" + imagesize + ".png"), true);

		// this PreferencesScreen
		this.addLast(mTab);
		executePanel = new ExecutePanel(this);
	}

	/*
	private Frame separator(CellPanel pnl) {
		Frame outerPnl = new Frame();
		pnl.setTag(INSETS, new Insets(0, 0, 2, 0));
		outerPnl.borderStyle = UIConstants.BDR_RAISEDOUTER | UIConstants.BDR_SUNKENINNER | UIConstants.BF_BOTTOM;
		outerPnl.setTag(INSETS, new Insets(0, 0, 2, 0));
		outerPnl.addLast(pnl, HSTRETCH, HFILL);
		return outerPnl;
	}
	*/

	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == executePanel.cancelButton) {
				this.close(0);
			}
			if (ev.target == executePanel.applyButton) {
				Global.pref.setBaseDir(DataDir.getText());
				Global.pref.setMapsBaseDir(MapsDir.getText());
				Global.pref.fontSize = Convert.toInt(fontSize.getText());
				if (Global.pref.fontSize < 6)
					Global.pref.fontSize = 11;
				Global.pref.fontName = fontName.getText();
				Global.pref.logsPerPage = Common.parseInt(inpLogsPerPage.getText());
				if (Global.pref.logsPerPage == 0)
					Global.pref.logsPerPage = Global.pref.DEFAULT_LOGS_PER_PAGE;

				Font defaultGuiFont = mApp.findFont("gui");
				int sz = (Global.pref.fontSize);
				Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz);
				mApp.addFont(newGuiFont, "gui");
				mApp.fontsChanged();
				mApp.mainApp.font = newGuiFont;

				Global.pref.myAlias = Alias.getText().trim();
				Global.pref.myAlias2 = Alias2.getText().trim();
				Global.pref.password = inpPassword.getText().trim();
				Global.pref.gcMemberId = inpGcMemberID.getText().trim();
				Global.pref.userID = inpUserID.getText().trim();
				MyLocale.saveLanguage(MyLocale.language = inpLanguage.getText().toUpperCase().trim());
				Global.pref.browser = Browser.getText();
				Global.pref.myproxy = Proxy.getText();
				Global.pref.myproxyport = ProxyPort.getText();
				Global.pref.proxyActive = chkProxyActive.getState();
				// TODO generate an error message if proxy port is not a number
				HttpConnection.setProxy(Global.pref.myproxy, Common.parseInt(Global.pref.myproxyport), Global.pref.proxyActive);
				Global.pref.autoReloadLastProfile = chkAutoLoad.getState();
				Global.pref.isPremium = chkPM.getState();
				Global.pref.showDeletedImages = chkShowDeletedImg.getState();
				Global.pref.garminConn = chcGarminPort.getSelectedItem().toString();
				Global.pref.garminGPSBabelOptions = chkSynthShort.state ? "-s" : "";
				Global.pref.noTabs = chkNoTabs.getState();
				Global.pref.tabsAtTop = chkTabsAtTop.getState();
				Global.pref.showStatus = chkShowStatus.getState();
				Global.pref.hasCloseButton = chkHasCloseButton.getState();
				Global.pref.useText = chkUseText.getState();
				Global.pref.useIcons = chkUseIcons.getState();
				if (!Global.pref.useText && !Global.pref.useIcons)
					Global.pref.useText = true;
				Global.pref.useBigIcons = chkUseBigIcons.getState();
				Global.pref.travelbugColMap = tccBugs.getSelectedCols();
				Global.pref.listColMap = tccList.getSelectedCols();
				Global.pref.descShowImg = chkDescShowImg.getState();
				Global.mainTab.tablePanel.myTableModel.setColumnNamesAndWidths();
				Global.pref.metricSystem = inpMetric.getInt() == 0 ? Metrics.METRIC : Metrics.IMPERIAL;
				Global.pref.spiderUpdates = inpSpiderUpdates.getInt();
				Global.pref.addDetailsToWaypoint = chkAddDetailsToWaypoint.getState();
				Global.pref.addDetailsToName = chkAddDetailsToName.getState();
				Global.pref.SortingGroupedByCache = chkSortingGroupedByCache.getState();
				Global.pref.debug = chkDebug.getState();

				Global.pref.savePreferences();
				Global.pref.dirty = true; // Need to update table in case columns were enabled/disabled
				this.close(0);
			}
			if (ev.target == DataDirBrowseButton) {
				/*
				Global.pref.absoluteBaseDir = "";
				Global.pref.checkAbsoluteBaseDir();
				DataDir.setText(Global.pref.baseDir);
				*/
				FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Global.pref.absoluteBaseDir);
				fc.setTitle(MyLocale.getMsg(616, "Select directory"));
				if (fc.execute() != FormBase.IDCANCEL)
					DataDir.setText(fc.getChosen() + "/");
			}
			if (ev.target == MapsDirBrowseButton) {
				FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Global.pref.absoluteMapsBaseDir);
				fc.setTitle(MyLocale.getMsg(616, "Select directory"));
				if (fc.execute() != FormBase.IDCANCEL)
					MapsDir.setText(fc.getChosen() + "/");
			}
			if (ev.target == gpsButton) {
				GPSPortOptions gpo = new GPSPortOptions();
				gpo.portName = Global.pref.mySPO.portName;
				gpo.baudRate = Global.pref.mySPO.baudRate;
				Editor s = gpo.getEditor();
				gpo.forwardGpsChkB.setState(Global.pref.forwardGPS);
				gpo.inputBoxForwardHost.setText(Global.pref.forwardGpsHost);
				gpo.chcUseGpsd.select(Global.pref.useGPSD);
				if (Global.pref.gpsdPort != Global.pref.DEFAULT_GPSD_PORT) {
					gpo.inputBoxGpsdHost.setText(Global.pref.gpsdHost + ":" + Convert.toString(Global.pref.gpsdPort));
				}
				else {
					gpo.inputBoxGpsdHost.setText(Global.pref.gpsdHost);
				}
				gpo.logGpsChkB.setState(Global.pref.logGPS);
				gpo.inputBoxLogTimer.setText(Global.pref.logGPSTimer);
				Gui.setOKCancel(s);
				if (s.execute() == FormBase.IDOK) {
					Global.pref.mySPO.portName = gpo.portName;
					Global.pref.mySPO.baudRate = gpo.baudRate;
					Global.pref.forwardGPS = gpo.forwardGpsChkB.getState();
					Global.pref.forwardGpsHost = gpo.inputBoxForwardHost.getText();
					Global.pref.useGPSD = gpo.chcUseGpsd.getInt();
					String gpsdHostString = gpo.inputBoxGpsdHost.getText(); // hostname[:port]
					int posColon = gpsdHostString.indexOf(':');
					if (posColon >= 0) {
						Global.pref.gpsdHost = gpsdHostString.substring(0, posColon);
						Global.pref.gpsdPort = Convert.toInt(gpsdHostString.substring(posColon + 1));
					}
					else {
						Global.pref.gpsdHost = gpsdHostString;
						Global.pref.gpsdPort = Global.pref.DEFAULT_GPSD_PORT;
					}
					Global.pref.logGPS = gpo.logGpsChkB.getState();
					Global.pref.logGPSTimer = gpo.inputBoxLogTimer.getText();
					// gpsButton.text = ("GPS: " + Global.pref.mySPO.portName + "/" + Global.pref.mySPO.baudRate);
				}
			}
		}
		super.onEvent(ev);
	}

}
