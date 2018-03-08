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

import org.json.JSONException;
import org.json.JSONObject;

import CacheWolf.controls.ExecutePanel;
import CacheWolf.controls.GuiImageBroker;
import CacheWolf.controls.InfoBox;
import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.controls.TableColumnChooser;
import CacheWolf.utils.Common;
import CacheWolf.utils.FileBugfix;
import CacheWolf.utils.HttpConnection;
import CacheWolf.utils.Metrics;
import CacheWolf.utils.MyLocale;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Font;
import ewe.fx.Insets;
import ewe.io.FileBase;
import ewe.io.IOException;
import ewe.io.SerialPort;
import ewe.io.SerialPortOptions;
import ewe.net.Socket;
import ewe.reflect.FieldTransfer;
import ewe.reflect.Reflect;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.mThread;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Editor;
import ewe.ui.EditorEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.InputStack;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollablePanel;
import ewe.ui.SoftKeyBar;
import ewe.ui.mApp;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mComboBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.mTabbedPanel;
import ewe.ui.formatted.TextDisplay;
import ewe.util.mString;
import net.ax86.GPS;
import net.ax86.GPSException;

/**
 * This class displays a user interface allowing the user to change and set preferences. It also provides a method to save the changed preferences that are saved immediately when the user presses "Apply". Class ID=600
 */
public class PreferencesScreen extends Form {
    public final String[] colNames = new String[]{MyLocale.getMsg(599, "checkbox"), //
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
            MyLocale.getMsg(1051, "Last sync date"), //
            MyLocale.getMsg(677, "PM"), //
            MyLocale.getMsg(362, "solved"), //
    };
    private final ExecutePanel executePanel;
    // cpDataDir
    private mInput DataDir;
    private mCheckBox chkAutoLoad;
    private mButton DataDirBrowseButton;
    // UserDataPanel
    private mInput Alias, inpGcMemberID, Alias2, inpPassword;
    private mChoice inpGCUser;
    // private mInput inpUserID;
    mCheckBox chkPM;
    // Card Maps / GPS
    private mInput MapsDir;
    private mButton MapsDirBrowseButton, gpsButton;
    // importPanel
    private mCheckBox chkCheckLog, chkCheckDTS, chkCheckTBs, alwaysKeepOwnLogs, chkOverwriteLogs, chkKeepTimeOnUpdate, chkAskForMaxValues, chkAddPremiumGC, chkUseGCFavoriteValue;
    private mInput maxLogsToKeep, maxLogsToSpider;

    mChoice inpLanguage, inpMetric, inpSpiderUpdates, chcGarminPort;
    mInput Proxy, ProxyPort, Browser, fontName, fontSize, inpLogsPerPage;
    mCheckBox chkShowDeletedImg;
    mCheckBox chkNoTabs, chkTabsAtTop, chkMenuAtTab, chkShowStatus, chkHasCloseButton, chkUseRadar, chkUseText, chkUseIcons, chkUseBigIcons;
    mCheckBox chkSynthShort, chkProxyActive, chkDescShowImg, chkAddDetailsToWaypoint, chkAddDetailsToName, chkSortingGroupedByCache, chkDebug;
    TableColumnChooser tccList, tccBugs;

    public PreferencesScreen() {

        Preferences.itself().setSubWindowSize(this);
        mTabbedPanel mTab = new mTabbedPanel();

        this.title = MyLocale.getMsg(108, "Preferences");

        // ///////////////////////////////////////////////////////
        // Card General
        // ///////////////////////////////////////////////////////

        CellPanel pnlGeneral = new CellPanel();

        CellPanel cpDataDir = new CellPanel();
        cpDataDir.setText(MyLocale.getMsg(603, "Data Directory:"));
        DataDir = new mInput();
        DataDir.setText(Preferences.itself().baseDir);
        cpDataDir.addLast(DataDir, STRETCH, HFILL);
        cpDataDir.addNext(chkAutoLoad = new mCheckBox(MyLocale.getMsg(629, "Autoload last profile")), DONTSTRETCH, LEFT);
        if (Preferences.itself().autoReloadLastProfile)
            chkAutoLoad.setState(true);
        cpDataDir.addLast(DataDirBrowseButton = GuiImageBroker.getButton(MyLocale.getMsg(604, "Browse"), "search"), DONTSTRETCH, RIGHT);
        pnlGeneral.addLast(cpDataDir, HSTRETCH, HFILL);

        CellPanel UserDataPanel = new CellPanel();
        UserDataPanel.setText(MyLocale.getMsg(659, "Account"));
        UserDataPanel.addNext(new mLabel(MyLocale.getMsg(601, "Your alias:")), DONTSTRETCH, DONTFILL | LEFT);
        UserDataPanel.addNext(Alias = new mInput(Preferences.itself().myAlias), STRETCH, HFILL);
        UserDataPanel.addLast(chkPM = new mCheckBox("PM"), DONTSTRETCH, DONTFILL | LEFT);
        if (Preferences.itself().havePremiumMemberRights)
            chkPM.setState(true);
        //UserDataPanel.addNext(new mLabel(MyLocale.getMsg(658, "User ID:")), DONTSTRETCH, DONTFILL | LEFT);
        //UserDataPanel.addLast(inpUserID = new mInput(Preferences.itself().userID), STRETCH, HFILL);
        UserDataPanel.addNext(new mLabel(MyLocale.getMsg(650, "GcMemberID:")), DONTSTRETCH, DONTFILL | LEFT);
        UserDataPanel.addLast(inpGcMemberID = new mInput(Preferences.itself().gcMemberId), STRETCH, HFILL);
        UserDataPanel.addNext(new mLabel(MyLocale.getMsg(657, "Second alias:")), DONTSTRETCH, DONTFILL | LEFT);
        UserDataPanel.addLast(Alias2 = new mInput(Preferences.itself().myAlias2), STRETCH, HFILL);
        UserDataPanel.addNext(new mLabel(MyLocale.getMsg(594, "Pwd")), DONTSTRETCH, DONTFILL | LEFT);
        UserDataPanel.addLast(inpPassword = new mInput(Preferences.itself().password), STRETCH, HFILL);
        inpPassword.setToolTip(MyLocale.getMsg(593, "Password is optional here.\nEnter only if you want to store it in Preferences.itself().xml"));
        inpPassword.isPassword = true;

        String[] gcLogins = Preferences.itself().getGCLogins();
        int selectedLogin = -1;
        for (int i = 0; i < gcLogins.length; i++) {
            if (gcLogins[i].equals(Preferences.itself().gcLogin)) {
                selectedLogin = i;
                break;
            }
        }
        UserDataPanel.addNext(new mLabel(MyLocale.getMsg(658, "Spider as:")), DONTSTRETCH, DONTFILL | LEFT);
        UserDataPanel.addLast(this.inpGCUser = new mChoice(gcLogins, selectedLogin), STRETCH, HFILL);

        pnlGeneral.addLast(UserDataPanel, HSTRETCH, HFILL);

        mTab.addCard(pnlGeneral, MyLocale.getMsg(621, "General"), null).iconize(GuiImageBroker.getImage("person"), Preferences.itself().useIcons);

        // ///////////////////////////////////////////////////////
        // Card Maps / GPS
        // ///////////////////////////////////////////////////////
        CellPanel pnlGPSMaps = new CellPanel();

        CellPanel cpMaps = new CellPanel();
        cpMaps.setText(MyLocale.getMsg(654, "Kartenverzeichnis"));
        MapsDir = new mInput();
        MapsDir.setText(Preferences.itself().mapsBaseDir);
        cpMaps.addLast(MapsDir, STRETCH, (FILL | LEFT));
        cpMaps.addLast(MapsDirBrowseButton = GuiImageBroker.getButton(MyLocale.getMsg(604, "Browse"), "search"), DONTSTRETCH, RIGHT);
        pnlGPSMaps.addLast(cpMaps, HSTRETCH, HFILL);

        CellPanel cpGPS = new CellPanel();
        cpGPS.setText("GPS");
        cpGPS.addLast(gpsButton = GuiImageBroker.getButton(MyLocale.getMsg(108, "Preferences"), "tools"), HSTRETCH, HFILL);
        pnlGPSMaps.addLast(cpGPS, HSTRETCH, HFILL);

        mTab.addCard(pnlGPSMaps, MyLocale.getMsg(655, "Maps/GPS"), null).iconize(GuiImageBroker.getImage("globe"), Preferences.itself().useIcons);
        // ///////////////////////////////////////////////////////
        // Card Import (Spider, GPX, ...)
        // ///////////////////////////////////////////////////////
        CellPanel importPanel = new CellPanel();

        CellPanel SpiderPanel = new CellPanel();
        SpiderPanel.setText(MyLocale.getMsg(670, "Checking for change"));
	/*
	SpiderPanel.addNext(new mLabel(MyLocale.getMsg(639, "Update changed caches?")), DONTSTRETCH, DONTFILL | LEFT);
	String[] spiderUpdateOptions = { MyLocale.getMsg(640, "Yes"), MyLocale.getMsg(641, "No"), MyLocale.getMsg(642, "Ask") };
	SpiderPanel.addLast(inpSpiderUpdates = new mChoice(spiderUpdateOptions, Preferences.itself().spiderUpdates), DONTSTRETCH, DONTFILL | LEFT);
	*/
        SpiderPanel.addLast(chkCheckLog = new mCheckBox(MyLocale.getMsg(666, "Update if new finds exist?")));
        chkCheckLog.setState(Preferences.itself().checkLog);
        //
        SpiderPanel.addLast(chkCheckDTS = new mCheckBox(MyLocale.getMsg(667, "Update if Difficulty, Terrain, Size changed?")));
        chkCheckDTS.setState(Preferences.itself().checkDTS);
        //
        SpiderPanel.addLast(chkCheckTBs = new mCheckBox(MyLocale.getMsg(669, "Update if dropped Tbs changed?")));
        chkCheckTBs.setState(Preferences.itself().checkTBs);
        importPanel.addLast(SpiderPanel, HSTRETCH, HFILL);

        CellPanel logsPanel = new CellPanel();
        logsPanel.setText(MyLocale.getMsg(671, "Storage of logs"));
        //

        logsPanel.addNext(new mLabel(MyLocale.getMsg(672, "Memory limit for logs per cache")), DONTSTRETCH, DONTFILL | LEFT);
        logsPanel.addLast(maxLogsToKeep = new mInput(Preferences.itself().maxLogsToKeep == Integer.MAX_VALUE ? "" : "" + Preferences.itself().maxLogsToKeep));
        logsPanel.addLast(alwaysKeepOwnLogs = new mCheckBox(MyLocale.getMsg(600, "Always keep your own logs")));
        alwaysKeepOwnLogs.setState(Preferences.itself().alwaysKeepOwnLogs);

        logsPanel.addNext(new mLabel(MyLocale.getMsg(1626, "Max. logs:")), DONTSTRETCH, DONTFILL | LEFT);
        logsPanel.addLast(maxLogsToSpider = new mInput(Preferences.itself().maxLogsToSpider == -1 ? "" : "" + Preferences.itself().maxLogsToSpider));
        logsPanel.addLast(chkOverwriteLogs = new mCheckBox(MyLocale.getMsg(668, "Overwrite saved Logs?")));
        chkOverwriteLogs.setState(Preferences.itself().overwriteLogs);
        logsPanel.addLast(chkKeepTimeOnUpdate = new mCheckBox(MyLocale.getMsg(678, "Keep logtime")));
        chkKeepTimeOnUpdate.setState(Preferences.itself().keepTimeOnUpdate);
        importPanel.addLast(logsPanel, HSTRETCH, HFILL);
        //

        CellPanel ViewPanel = new CellPanel();
        ViewPanel.setText(MyLocale.getMsg(673, "View"));
        //
        ViewPanel.addLast(chkAskForMaxValues = new mCheckBox(MyLocale.getMsg(674, "Ask about download limits")));
        chkAskForMaxValues.setState(Preferences.itself().askForMaxNumbersOnImport);
        ViewPanel.addLast(chkAddPremiumGC = new mCheckBox(MyLocale.getMsg(675, "Create placeholder for PM - cache")));
        chkAddPremiumGC.setState(Preferences.itself().addPremiumGC);
        ViewPanel.addLast(chkUseGCFavoriteValue = new mCheckBox(MyLocale.getMsg(676, "Show GC favorites")));
        chkUseGCFavoriteValue.setState(Preferences.itself().useGCFavoriteValue);

        importPanel.addLast(ViewPanel, HSTRETCH, HFILL);

        mTab.addCard(importPanel, MyLocale.getMsg(175, "Import"), null).iconize(GuiImageBroker.getImage("import"), Preferences.itself().useIcons);

        // ///////////////////////////////////////////////////////
        // Card Export (Garmin and GPSBabel)
        // ///////////////////////////////////////////////////////
        CellPanel exportPanel = new CellPanel();
        // ExportPanel.setText(MyLocale.getMsg(107, "Export"));

        CellPanel locExportPanel = new CellPanel();
        locExportPanel.setText(MyLocale.getMsg(215, "to LOC") + " + " + MyLocale.getMsg(122, "zum GPS mit GPSBabel"));
        // loc Exporter
        locExportPanel.addLast(new mLabel(MyLocale.getMsg(643, "Append cache details to:")), DONTSTRETCH, LEFT);
        locExportPanel.addNext(chkAddDetailsToWaypoint = new mCheckBox(MyLocale.getMsg(644, "waypoints")), DONTSTRETCH, LEFT);
        chkAddDetailsToWaypoint.setState(Preferences.itself().addDetailsToWaypoint);
        // loc Exporter
        locExportPanel.addLast(chkAddDetailsToName = new mCheckBox(MyLocale.getMsg(645, "names")), DONTSTRETCH, LEFT);
        chkAddDetailsToName.setState(Preferences.itself().addDetailsToName);
        exportPanel.addLast(locExportPanel, HSTRETCH, HFILL);

        CellPanel cpBabel = new CellPanel();
        // first loc-file will be generated
        cpBabel.setText(MyLocale.getMsg(122, "zum GPS mit GPSBabel"));
        // GPSBabel Port
        cpBabel.addNext(new mLabel(MyLocale.getMsg(173, "Port:")), DONTSTRETCH, LEFT);
        cpBabel.addLast(chcGarminPort = new mChoice(new String[]{"com1", "com2", "com3", "com4", "com5", "com6", "com7", "usb"}, 0), STRETCH, LEFT);
        chcGarminPort.selectItem(Preferences.itself().garminConn);
        // GPSBabeloption -s
        cpBabel.addLast(chkSynthShort = new mCheckBox(MyLocale.getMsg(174, "Shorten Cachenames?")), DONTSTRETCH, LEFT);
        chkSynthShort.setState(!Preferences.itself().garminGPSBabelOptions.equals(""));
        exportPanel.addLast(cpBabel, HSTRETCH, HFILL);

        mTab.addCard(exportPanel, MyLocale.getMsg(107, "Export"), null).iconize(GuiImageBroker.getImage("export"), Preferences.itself().useIcons);
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
            if (langs[i + 1].equalsIgnoreCase(Preferences.itself().language))
                curlang = i + 1;
        }
        // ewe.sys.Vm.copyArray(tmp, 0, langs, 1, tmp.length);
        pnlLanguage.addLast(inpLanguage = new mChoice(langs, curlang), DONTSTRETCH, DONTFILL | LEFT);
        inpLanguage.setToolTip(MyLocale.getMsg(591, "Select \"auto\" for system language or select your preferred language, e.g. DE or EN"));
        pnlScreen.addLast(pnlLanguage);

        CellPanel pnlFont = new CellPanel();
        pnlFont.addNext(new mLabel("Font"), DONTSTRETCH, (DONTFILL | LEFT));
        pnlFont.addNext(fontName = new mInput(), DONTSTRETCH, (HFILL | LEFT));
        fontName.maxLength = 50;
        fontName.setText(Preferences.itself().fontName);
        pnlFont.addLast(fontSize = new mInput(), DONTSTRETCH, (HFILL | LEFT));
        fontSize.maxLength = 2;
        fontSize.setPreferredSize(2 * Preferences.itself().fontSize, -1);
        fontSize.setText(Convert.toString(Preferences.itself().fontSize));
        pnlScreen.addLast(pnlFont);

        pnlScreen.addLast(chkUseText = new mCheckBox(MyLocale.getMsg(664, "Show Text on Buttons")), DONTSTRETCH, DONTFILL | LEFT);
        chkUseText.setState(Preferences.itself().useText);
        pnlScreen.addNext(chkUseIcons = new mCheckBox(MyLocale.getMsg(665, "Show Icon on Buttons")), DONTSTRETCH, DONTFILL | LEFT);
        chkUseIcons.setState(Preferences.itself().useIcons);
        pnlScreen.addLast(chkUseBigIcons = new mCheckBox(MyLocale.getMsg(661, "Use big Icons")), DONTSTRETCH, DONTFILL | LEFT);
        chkUseBigIcons.setState(Preferences.itself().useBigIcons);
        pnlScreen.addLast(chkNoTabs = new mCheckBox(MyLocale.getMsg(1212, "Select tabs by button")), DONTSTRETCH, DONTFILL | LEFT);
        chkNoTabs.setState(Preferences.itself().noTabs);
        pnlScreen.addLast(chkTabsAtTop = new mCheckBox(MyLocale.getMsg(627, "Tabs at top")), DONTSTRETCH, DONTFILL | LEFT);
        chkTabsAtTop.setState(Preferences.itself().tabsAtTop);
        pnlScreen.addLast(chkMenuAtTab = new mCheckBox(MyLocale.getMsg(626, "Menubuttons under/over Tabselection")), DONTSTRETCH, DONTFILL | LEFT);
        chkMenuAtTab.setState(Preferences.itself().menuAtTab);
        pnlScreen.addLast(chkShowStatus = new mCheckBox(MyLocale.getMsg(628, "Status")), DONTSTRETCH, DONTFILL | LEFT);
        chkShowStatus.setState(Preferences.itself().showStatus);

        pnlScreen.addLast(chkHasCloseButton = new mCheckBox(MyLocale.getMsg(631, "PDA has close Button")), DONTSTRETCH, DONTFILL | LEFT);
        chkHasCloseButton.setState(Preferences.itself().hasCloseButton);
        //pnlScreen.addLast(chkUseRadar = new mCheckBox(MyLocale.getMsg(660, "Show radartab on small screen")), DONTSTRETCH, DONTFILL | LEFT);
        //chkUseRadar.setState(Preferences.itself().useRadar);

        pnlDisplay.addLast(pnlScreen, HSTRETCH, HFILL);

        mTab.addCard(pnlDisplay, MyLocale.getMsg(622, "Screen"), null).iconize(GuiImageBroker.getImage("monitor"), Preferences.itself().useIcons);
        // ///////////////////////////////////////////////////////
        // Card Pages
        // ///////////////////////////////////////////////////////
        CellPanel pnlTabs = new CellPanel();

        CellPanel ListPanel = new CellPanel();
        ListPanel.setText(MyLocale.getMsg(1200, "List"));
        ListPanel.addLast(chkSortingGroupedByCache = new mCheckBox(MyLocale.getMsg(647, "Sorting grouped by Cache")), DONTSTRETCH, (DONTFILL | LEFT));
        chkSortingGroupedByCache.setState(Preferences.itself().SortingGroupedByCache);
        pnlTabs.addLast(ListPanel, HSTRETCH, HFILL);

        CellPanel DescriptionPanel = new CellPanel();
        DescriptionPanel.setText(MyLocale.getMsg(1202, "Description"));
        DescriptionPanel.addLast(chkDescShowImg = new mCheckBox(MyLocale.getMsg(638, "Show pictures in description?")), DONTSTRETCH, DONTFILL | LEFT);
        if (Preferences.itself().descShowImg)
            chkDescShowImg.setState(true);
        pnlTabs.addLast(DescriptionPanel, HSTRETCH, HFILL);

        CellPanel ImagesPanel = new CellPanel();
        ImagesPanel.setText(MyLocale.getMsg(1203, "Images"));
        ImagesPanel.addLast(chkShowDeletedImg = new mCheckBox(MyLocale.getMsg(624, "Show information \"missing image file\"?")), DONTSTRETCH, DONTFILL | LEFT);
        if (Preferences.itself().showDeletedImages)
            chkShowDeletedImg.setState(true);
        pnlTabs.addLast(ImagesPanel, HSTRETCH, HFILL);

        CellPanel LogsViewPanel = new CellPanel();
        LogsViewPanel.setText(MyLocale.getMsg(1204, "Hints & Logs"));
        LogsViewPanel.addNext(inpLogsPerPage = new mInput(), DONTSTRETCH, HSHRINK | LEFT);
        inpLogsPerPage.setPreferredSize(40, -1);
        LogsViewPanel.addLast(new mLabel(MyLocale.getMsg(630, "Logs per page (HintLogPanel)")), STRETCH, HFILL | LEFT);
        inpLogsPerPage.setText(Convert.toString(Preferences.itself().logsPerPage));
        pnlTabs.addLast(LogsViewPanel, HSTRETCH, HFILL);

        mTab.addCard(pnlTabs, MyLocale.getMsg(662, "Pages"), null).iconize(GuiImageBroker.getImage("tabs"), Preferences.itself().useIcons);
        // ///////////////////////////////////////////////////////
        // Card More
        // ///////////////////////////////////////////////////////
        CellPanel pnlMore = new CellPanel();

        CellPanel BrowserPanel = new CellPanel();
        BrowserPanel.setText("Browser:");
        BrowserPanel.addLast(Browser = new mInput(Preferences.itself().browser));
        pnlMore.addLast(BrowserPanel, HSTRETCH, HFILL);

        CellPanel pnlProxy = new CellPanel();
        pnlProxy.setText("Proxy");
        pnlProxy.addNext(new mLabel(""), HSTRETCH, HFILL);
        pnlProxy.addLast(Proxy = new mInput(), HSTRETCH, (HFILL | LEFT)).setTag(SPAN, new Dimension(2, 1));
        Proxy.setText(Preferences.itself().myproxy);
        pnlProxy.addNext(new mLabel("Port"), DONTSTRETCH, (DONTFILL | LEFT));
        pnlProxy.addLast(ProxyPort = new mInput(), DONTSTRETCH, (DONTFILL | LEFT));
        ProxyPort.setText(Preferences.itself().myproxyport);
        pnlProxy.addNext(new mLabel(""), HSTRETCH, HFILL);
        pnlProxy.addLast(chkProxyActive = new mCheckBox(MyLocale.getMsg(634, "use Proxy")));
        chkProxyActive.setState(Preferences.itself().proxyActive);
        pnlMore.addLast(pnlProxy, HSTRETCH, HFILL);

        CellPanel EtcPanel = new CellPanel();
        EtcPanel.setText(MyLocale.getMsg(632, "More"));
        String[] metriken = {MyLocale.getMsg(589, "Metric (km)"), MyLocale.getMsg(590, "Imperial (mi)")};
        EtcPanel.addNext(new mLabel(MyLocale.getMsg(588, "Length units")), DONTSTRETCH, DONTFILL | LEFT);
        int currMetrik = Preferences.itself().metricSystem == Metrics.METRIC ? 0 : 1;
        EtcPanel.addLast(inpMetric = new mChoice(metriken, currMetrik), DONTSTRETCH, DONTFILL | LEFT);
        EtcPanel.addLast(chkDebug = new mCheckBox(MyLocale.getMsg(648, "Debug Mode")), DONTSTRETCH, (DONTFILL | LEFT));
        chkDebug.setState(Preferences.itself().debug);
        pnlMore.addLast(EtcPanel, HSTRETCH, HFILL);

        mTab.addCard(pnlMore, MyLocale.getMsg(632, "More"), null).iconize(GuiImageBroker.getImage("more"), Preferences.itself().useIcons);
        // ///////////////////////////////////////////////////////
        // Card - Listview
        // ///////////////////////////////////////////////////////

        mTab.addCard(tccList = new TableColumnChooser(//
                colNames, Preferences.itself().listColMap), MyLocale.getMsg(595, "List"), null).iconize(GuiImageBroker.getImage("list"), Preferences.itself().useIcons);

        // ///////////////////////////////////////////////////////
        // Card - Travelbugs
        // ///////////////////////////////////////////////////////
        mTab.addCard(tccBugs = new TableColumnChooser(//
                new String[]{MyLocale.getMsg(6000, "Guid"), //
                        MyLocale.getMsg(6001, "Name"), //
                        MyLocale.getMsg(6002, "track#"), //
                        MyLocale.getMsg(6003, "Mission"), //
                        MyLocale.getMsg(6004, "From Prof"), //
                        MyLocale.getMsg(6005, "From Wpt"), //
                        MyLocale.getMsg(6006, "From Date"), //
                        MyLocale.getMsg(6007, "From Log"), //
                        MyLocale.getMsg(6008, "To Prof"), //
                        MyLocale.getMsg(6009, "To Wpt"), //
                        MyLocale.getMsg(6010, "To Date"), //
                        MyLocale.getMsg(6011, "To Log") //
                }, Preferences.itself().travelbugColMap), "T-bugs", null).iconize(GuiImageBroker.getImage("bug"), Preferences.itself().useIcons);

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
                int oldFontsize = Preferences.itself().fontSize;
                Preferences.itself().setBaseDir(DataDir.getText());
                Preferences.itself().setMapsBaseDir(MapsDir.getText());
                Preferences.itself().fontSize = Convert.toInt(fontSize.getText());
                if (Preferences.itself().fontSize < 6)
                    Preferences.itself().fontSize = 11;
                Preferences.itself().fontName = fontName.getText();
                Preferences.itself().logsPerPage = Common.parseInt(inpLogsPerPage.getText());
                if (Preferences.itself().logsPerPage == 0)
                    Preferences.itself().logsPerPage = Preferences.itself().DEFAULT_LOGS_PER_PAGE;

                Font defaultGuiFont = mApp.findFont("gui");
                int sz = (Preferences.itself().fontSize);
                Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz);
                mApp.addFont(newGuiFont, "gui");
                mApp.fontsChanged();
                mApp.mainApp.font = newGuiFont;

                Preferences.itself().myAlias = Alias.getText().trim();
                Preferences.itself().myAlias2 = Alias2.getText().trim();
                Preferences.itself().password = inpPassword.getText().trim();
                Preferences.itself().gcMemberId = inpGcMemberID.getText().trim();
                //Preferences.itself().userID = inpUserID.getText().trim();
                Preferences.itself().language = inpLanguage.getText().toUpperCase().trim();
                Preferences.itself().browser = Browser.getText();
                Preferences.itself().myproxy = Proxy.getText();
                Preferences.itself().myproxyport = ProxyPort.getText();
                Preferences.itself().proxyActive = chkProxyActive.getState();
                HttpConnection.setProxy(Preferences.itself().myproxy, Common.parseInt(Preferences.itself().myproxyport), Preferences.itself().proxyActive);
                Preferences.itself().autoReloadLastProfile = chkAutoLoad.getState();
                Preferences.itself().havePremiumMemberRights = chkPM.getState();
                Preferences.itself().showDeletedImages = chkShowDeletedImg.getState();
                Preferences.itself().garminConn = chcGarminPort.getSelectedItem().toString();
                Preferences.itself().gcLogin = this.inpGCUser.getSelectedItem().toString();
                Preferences.itself().garminGPSBabelOptions = chkSynthShort.state ? "-s" : "";
                Preferences.itself().noTabs = chkNoTabs.getState();
                Preferences.itself().tabsAtTop = chkTabsAtTop.getState();
                Preferences.itself().menuAtTab = chkMenuAtTab.getState();
                Preferences.itself().showStatus = chkShowStatus.getState();
                Preferences.itself().hasCloseButton = chkHasCloseButton.getState();
                Preferences.itself().useText = chkUseText.getState();
                Preferences.itself().useIcons = chkUseIcons.getState();
                if (!Preferences.itself().useText && !Preferences.itself().useIcons)
                    Preferences.itself().useText = true;
                Preferences.itself().useBigIcons = chkUseBigIcons.getState();
                if (!Preferences.itself().travelbugColMap.equals(tccBugs.getSelectedCols())) {
                    Preferences.itself().travelbugColMap = tccBugs.getSelectedCols();
                    // TODO it with event raise
                    MainTab.itself.tablePanel.myTableModel.setColumnNamesAndWidths();
                    MainTab.itself.tablePanel.refreshControl();
                }
                Preferences.itself().listColMap = tccList.getSelectedCols();
                if (Preferences.itself().fontSize != oldFontsize) {
                    float f = (float) Preferences.itself().fontSize / (float) oldFontsize;
                    int[] colWidth = TableColumnChooser.str2Array(Preferences.itself().listColWidth, 10, 1024, 50, -1);
                    for (int col = 0; col < colWidth.length; col++) {
                        colWidth[col] = (int) (colWidth[col] * f);
                    }
                    StringBuffer sb = new StringBuffer(100);
                    for (int i = 0; i < colWidth.length; i++) {
                        if (sb.length() != 0)
                            sb.append(',');
                        sb.append(colWidth[i]);
                    }
                    Preferences.itself().listColWidth = sb.toString();
                }
                Preferences.itself().descShowImg = chkDescShowImg.getState();
                MainTab.itself.tablePanel.myTableModel.setColumnNamesAndWidths();
                Preferences.itself().metricSystem = inpMetric.getInt() == 0 ? Metrics.METRIC : Metrics.IMPERIAL;
                // Preferences.itself().spiderUpdates = inpSpiderUpdates.getInt();
                Preferences.itself().addDetailsToWaypoint = chkAddDetailsToWaypoint.getState();
                Preferences.itself().addDetailsToName = chkAddDetailsToName.getState();
                Preferences.itself().SortingGroupedByCache = chkSortingGroupedByCache.getState();
                Preferences.itself().debug = chkDebug.getState();
                Preferences.itself().checkLog = chkCheckLog.getState();
                Preferences.itself().checkDTS = chkCheckDTS.getState();
                Preferences.itself().checkTBs = chkCheckTBs.getState();
                String tmp = maxLogsToKeep.getText().trim();
                Preferences.itself().maxLogsToKeep = (tmp.length() == 0 ? Integer.MAX_VALUE : Common.parseInt(tmp));
                Preferences.itself().alwaysKeepOwnLogs = alwaysKeepOwnLogs.getState();
                tmp = maxLogsToSpider.getText().trim();
                Preferences.itself().maxLogsToSpider = (tmp.length() == 0 ? -1 : Common.parseInt(tmp));

                Preferences.itself().overwriteLogs = chkOverwriteLogs.getState();
                Preferences.itself().keepTimeOnUpdate = chkKeepTimeOnUpdate.getState();
                Preferences.itself().askForMaxNumbersOnImport = chkAskForMaxValues.getState();
                Preferences.itself().addPremiumGC = this.chkAddPremiumGC.getState();
                Preferences.itself().useGCFavoriteValue = this.chkUseGCFavoriteValue.getState();
                MainTab.itself.tablePanel.mainMenu.updateGCVotesMenu();

                Preferences.itself().dirty = true;
                Preferences.itself().savePreferences();
                this.close(0);
            }
            if (ev.target == DataDirBrowseButton) {
		/*
		Preferences.itself().absoluteBaseDir = "";
		Preferences.itself().checkAbsoluteBaseDir();
		DataDir.setText(Preferences.itself().baseDir);
		*/
                FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Preferences.itself().absoluteBaseDir);
                fc.setTitle(MyLocale.getMsg(616, "Select directory"));
                if (fc.execute() != FormBase.IDCANCEL)
                    DataDir.setText(fc.getChosen() + "/");
            }
            if (ev.target == MapsDirBrowseButton) {
                FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Preferences.itself().absoluteMapsBaseDir);
                fc.setTitle(MyLocale.getMsg(616, "Select directory"));
                if (fc.execute() != FormBase.IDCANCEL)
                    MapsDir.setText(fc.getChosen() + "/");
            }
            if (ev.target == gpsButton) {
                GPSPortOptions gpo = new GPSPortOptions();
                gpo.portName = Preferences.itself().mySPO.portName;
                gpo.baudRate = Preferences.itself().mySPO.baudRate;
                Editor s = gpo.getEditor();
                gpo.forwardGpsChkB.setState(Preferences.itself().forwardGPS);
                gpo.inputBoxForwardHost.setText(Preferences.itself().forwardGpsHost);
                gpo.chcUseGpsd.select(Preferences.itself().useGPSD);
                if (Preferences.itself().gpsdPort != Preferences.itself().DEFAULT_GPSD_PORT) {
                    gpo.inputBoxGpsdHost.setText(Preferences.itself().gpsdHost + ":" + Preferences.itself().gpsdPort);
                } else {
                    gpo.inputBoxGpsdHost.setText(Preferences.itself().gpsdHost);
                }
                gpo.logGpsChkB.setState(Preferences.itself().logGPS);
                gpo.inputBoxLogTimer.setText(Preferences.itself().logGPSTimer);
                Gui.setOKCancel(s);
                if (s.execute() == FormBase.IDOK) {
                    Preferences.itself().mySPO.portName = gpo.portName;
                    Preferences.itself().mySPO.baudRate = gpo.baudRate;
                    Preferences.itself().forwardGPS = gpo.forwardGpsChkB.getState();
                    Preferences.itself().forwardGpsHost = gpo.inputBoxForwardHost.getText();
                    Preferences.itself().useGPSD = gpo.chcUseGpsd.getInt();
                    String gpsdHostString = gpo.inputBoxGpsdHost.getText(); // hostname[:port]
                    int posColon = gpsdHostString.indexOf(':');
                    if (posColon >= 0) {
                        Preferences.itself().gpsdHost = gpsdHostString.substring(0, posColon);
                        Preferences.itself().gpsdPort = Convert.toInt(gpsdHostString.substring(posColon + 1));
                    } else {
                        Preferences.itself().gpsdHost = gpsdHostString;
                        Preferences.itself().gpsdPort = Preferences.itself().DEFAULT_GPSD_PORT;
                    }
                    Preferences.itself().logGPS = gpo.logGpsChkB.getState();
                    Preferences.itself().logGPSTimer = gpo.inputBoxLogTimer.getText();
                    // gpsButton.text = ("GPS: " + Preferences.itself().mySPO.portName + "/" + Preferences.itself().mySPO.baudRate);
                }
            }
        }
        super.onEvent(ev);
    }

}

/**
 * Thread for reading data from COM-port
 */
class mySerialThread extends mThread {
    SerialPort comSp;
    byte[] comBuff = new byte[1024];
    int comLength = 0;
    TextDisplay out;
    boolean run;
    public String lastgot;

    public mySerialThread(SerialPortOptions spo, TextDisplay td) throws IOException {
        comSp = new SerialPort(spo);
        //comSp.setFlowControl(SerialPort.SOFTWARE_FLOW_CONTROL);
        out = td;
        lastgot = null;
    }

    public void run() {
        run = true;
        while (run) {
            try {
                sleep(200);
            } catch (InterruptedException e) {
                // Preferences.itself().log("Ignored exception", e, true);
            }
            if (comSp != null) {
                comLength = comSp.nonBlockingRead(comBuff, 0, comBuff.length);
                if (comLength > 0) {
                    String str = mString.fromAscii(comBuff, 0, comLength).toUpperCase();
                    lastgot = str;
                    if (out != null)
                        out.appendText(str, true);
                }
            }
        }
    }

    public String nonBlockingRead() {
        String ret = lastgot; //mString.fromAscii(gpsBuff,0,gpsLen);
        lastgot = null;
        return ret;

    }

    public boolean stop() {
        run = false;
        boolean ret;
        if (comSp != null) {
            ret = comSp.close(); //compSp == null can happen if a exception occured
            try {
                ewe.sys.mThread.sleep(500); // wait in order to give the system time to close the serial port
            } catch (InterruptedException e) {
                // Preferences.itself().log("Ignored exception", e, true);
            }
        } else
            ret = true;
        return ret;
    }
}

/**
 * Thread for reading data from gpsd and simply displaying it to the user.
 * <p>
 * This is a modified version of {@link CacheWolf.navi.GpsdThread}.
 *
 * @author Tilman Blumenbach
 */
class GpsdThread extends mThread {
    GPS gpsObj;
    TextDisplay out;
    boolean run;

    public GpsdThread(TextDisplay td) throws IOException, JSONException, GPSException {
        JSONObject response;
        int proto_major;

        gpsObj = new GPS(Preferences.itself().gpsdHost, Preferences.itself().gpsdPort);
        gpsObj.stream(GPS.WATCH_ENABLE);

        // Check major protocol version:
        response = gpsObj.read();

        if (!response.getString("class").equals("VERSION")) {
            throw new GPSException("Expected VERSION object at connect.");
        } else if ((proto_major = response.getInt("proto_major")) != 3) {
            throw new GPSException("Invalid protocol API version; got " + proto_major + ", wanted 3.");
        }

        out = td;
        // Show data to user:
        out.appendText(response.toString(2) + "\n", true);
    }

    public void run() {
        JSONObject response;

        run = true;
        while (run) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                // Preferences.itself().log("Ignored Exception", e, true);
            }

            if (gpsObj == null) {
                continue;
            }

            try {
                /* Tblue> This is ugly, but BufferedReader::ready() seems to
                 *        be broken in Ewe, so instead of only polling when
                 *        there is no data from gpsd, we poll on every iteration.
                 *        Not ideal, but works for now.
                 */
                gpsObj.poll();

                /* Tblue> TODO: I think this call should not block, but
                 *              my GPS class does not yet support non-blocking
                 *              reads... Seems to work, anyway.
                 */
                response = gpsObj.read();
                out.appendText(response.toString(2) + "\n", true);

                // Keep up with new devices:
                if (response.getString("class").equals("DEVICE") && response.has("activated") && response.getDouble("activated") != 0) { // This is a new device, we need to tell gpsd we want to watch it:
                    Preferences.itself().log("New GPS device, sending WATCH command.");
                    gpsObj.stream(GPS.WATCH_ENABLE);
                }
            } catch (Exception e) {
                // We will just ignore this JSON object:
                // Preferences.itself().log("Ignored Exception", e, true);
            }
        } // while
    }

    public boolean stop() {
        run = false;

        if (gpsObj == null) {
            return true;
        }

        gpsObj.cleanup();
        return false;
    }
}

class OldGpsdThread extends mThread {
    Socket gpsdSocket;
    boolean run;
    TextDisplay out;
    Socket tcpConn;
    String lastError = "";
    public String lastgot;

    public OldGpsdThread(TextDisplay td) throws IOException {
        try {
            gpsdSocket = new Socket(Preferences.itself().gpsdHost, Preferences.itself().gpsdPort);
        } catch (IOException e) {
            throw new IOException(Preferences.itself().gpsdHost);
        } // catch (UnsatisfiedLinkError e) {} // TODO in original java-vm
        out = td;
        lastgot = null;
    }

    public void run() {
        String gpsResult;
        run = true;
        while (run) {
            try {
                sleep(900);
            } catch (InterruptedException e) {
                // Preferences.itself().log("Ignored Exception", e, true);
            }
            if (gpsdSocket != null) {
                gpsResult = getGpsdData("ADPQTV\r\n");
                if (gpsResult != null) {
                    lastgot = gpsResult;
                    if (out != null)
                        out.appendText(gpsResult, true);
                }
            }
        } // while
    }

    private String getGpsdData(String command) {
        byte[] rcvBuff = new byte[1024 * 10]; // when some action takes a long time (eg. loading or zooming a map), a lot of data can be in the buffer, read that at once
        int rcvLength = 0;
        try {
            gpsdSocket.write(command.getBytes());
        } catch (IOException e) {
            Preferences.itself().log("Socket exception", e, true);
        }
        try {
            sleep(100);
        } catch (InterruptedException e) {
            // Preferences.itself().log("Ignored exception", e, true);
        }
        try {
            rcvLength = gpsdSocket.read(rcvBuff);
        } catch (IOException e) {
            Preferences.itself().log("Socket exception", e, true);
        }
        String str = null;
        if (rcvLength > 0) {
            str = mString.fromAscii(rcvBuff, 0, rcvLength);
        }
        return str;
    }

    public String nonBlockingRead() {
        String ret = lastgot; //mString.fromAscii(gpsBuff,0,gpsLen);
        lastgot = null;
        return ret;

    }

    public void stop() {
        run = false;
        if (gpsdSocket != null)
            gpsdSocket.close();
    }
}

class GPSPortOptions extends SerialPortOptions {
    TextDisplay txtOutput;
    mButton btnTest, btnUpdatePortList, btnScan;
    public mInput inputBoxForwardHost;
    mLabel labelForwardHost;
    public mCheckBox forwardGpsChkB;
    public mInput inputBoxGpsdHost;
    mLabel labelUseGpsd;
    public mChoice chcUseGpsd;
    mLabel labelGpsdHost;
    public mInput inputBoxLogTimer;
    mLabel labelLogTimer;
    public mCheckBox logGpsChkB;
    mySerialThread serThread;
    GpsdThread gpsdThread = null;
    OldGpsdThread oldGpsdThread = null;
    boolean gpsRunning = false;
    MyEditor ed = new MyEditor();

    private String[] useGpsdChoices = new String[]{MyLocale.getMsg(641, "No"), MyLocale.getMsg(99999, "Yes (< v2.91)"), MyLocale.getMsg(99999, "Yes (>= v2.91)"),};

    public Editor getEditor() {
        // The following lines are mainly copied from SerialPortOptions.
        // Reason: We want to use MyEditor instead of the default Editor,
        //         because the latter places the ok/cancel buttons centered.
        // Because this is from the general SerialPortOptions class, maybe not all of the code
        // must be necessary.
        ed.objectClass = Reflect.getForObject(this);
        ed.sampleObject = this;
        ed.setObject(this);
        ed.title = MyLocale.getMsg(7100, "Serial Port Options");
        InputStack is = new InputStack();
        ed.addLast(is).setCell(CellConstants.HSTRETCH);
        CellPanel cp = new CellPanel();
        ed.addField(cp.addNext(new mComboBox()).setCell(CellConstants.HSTRETCH), "portName");
        btnUpdatePortList = new mButton(MyLocale.getMsg(7101, "Update Ports$u"));
        ed.addField(cp.addLast(btnUpdatePortList).setCell(CellConstants.DONTSTRETCH), "update");
        is.add(cp, "Port:$p");
        mComboBox cb = new mComboBox();
        is.add(ed.addField(cb, "baudRate"), MyLocale.getMsg(7102, "Baud:$b"));
        cb.choice.addItems(ewe.util.mString.split("110|300|1200|2400|4800|9600|19200|38400|57600|115200"));
        //
        // End of copy from SerialPortOptions.
        //
        ed.buttonConstraints = CellConstants.HFILL;
        btnScan = new mButton(MyLocale.getMsg(7103, "Scan$u"));
        btnScan.setCell(CellConstants.DONTSTRETCH);
        ed.addField(ed.addNext(btnScan), "scan");
        btnTest = new mButton(MyLocale.getMsg(7104, "Test$t"));
        ed.addField(ed.addLast(btnTest.setCell(CellConstants.DONTSTRETCH)), "test");
        txtOutput = new TextDisplay();
        ScrollBarPanel sbp = new MyScrollBarPanel(txtOutput);
        sbp.setOptions(ScrollablePanel.AlwaysShowVerticalScrollers | ScrollablePanel.AlwaysShowHorizontalScrollers);
        ed.addField(ed.addLast(sbp), "out");

        forwardGpsChkB = new mCheckBox("");
        ed.addField(ed.addNext(forwardGpsChkB, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "forwardGpsChkB");
        labelForwardHost = new mLabel(MyLocale.getMsg(7105, "Forward GPS data to host (serial port only)"));
        ed.addField(ed.addNext(labelForwardHost, CellConstants.DONTSTRETCH, (CellConstants.WEST | CellConstants.DONTFILL)), "labelForwardIP");
        inputBoxForwardHost = new mInput("tcpForwardHost");
        inputBoxForwardHost.setPromptControl(labelForwardHost);
        inputBoxForwardHost.setToolTip(MyLocale.getMsg(7106, "All data from GPS will be sent to TCP-port 23\n and can be redirected there to a serial port\n by HW Virtual Serial Port"));
        ed.addField(ed.addLast(inputBoxForwardHost, 0, (CellConstants.WEST | CellConstants.HFILL)), "tcpForwardHost");

        logGpsChkB = new mCheckBox("");
        ed.addField(ed.addNext(logGpsChkB, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "logGpsChkB");
        labelLogTimer = new mLabel(MyLocale.getMsg(7107, "Interval in sec for logging (serial port only)"));
        ed.addField(ed.addNext(labelLogTimer, CellConstants.DONTSTRETCH, (CellConstants.WEST | CellConstants.DONTFILL)), "labelLogTimer");
        inputBoxLogTimer = new mInput("GPSLogTimer");
        inputBoxLogTimer.setPromptControl(labelLogTimer);
        ed.addField(ed.addLast(inputBoxLogTimer, 0, (CellConstants.WEST | CellConstants.HFILL)), "GPSLogTimer");

        labelUseGpsd = new mLabel(MyLocale.getMsg(7121, "Receive GPS data from gpsd:"));
        ed.addField(ed.addNext(labelUseGpsd, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "labelUseGpsd");
        chcUseGpsd = new mChoice(useGpsdChoices, 0);
        chcUseGpsd.setPromptControl(labelUseGpsd);
        chcUseGpsd.setToolTip(MyLocale.getMsg(7122, "GPS data will be received from a gpsd server, not from a serial port"));
        ed.addField(ed.addLast(chcUseGpsd, 0, (CellConstants.WEST | CellConstants.HFILL)), "UseGpsd");

        labelGpsdHost = new mLabel(MyLocale.getMsg(99999, "gpsd host:"));
        ed.addField(ed.addNext(labelGpsdHost, CellConstants.DONTSTRETCH, (CellConstants.EAST | CellConstants.DONTFILL)), "labelGpsdHost");
        inputBoxGpsdHost = new mInput("GpsdHost");
        inputBoxGpsdHost.setPromptControl(labelGpsdHost);
        ed.addField(ed.addLast(inputBoxGpsdHost, 0, (CellConstants.WEST | CellConstants.HFILL)), "GpsdHost");

        this.ed.firstFocus = btnUpdatePortList;
        gpsRunning = false;
        return ed;
    }

    boolean interruptScan = false;
    boolean scanRunning = false;

    public void action(String field, Editor ed_) {
        if (field.equals("scan")) {
            if (scanRunning == false) {
                txtOutput.setText("");
                new mThread() {
                    public void run() {
                        btnTest.set(ControlConstants.Disabled, true);
                        btnTest.repaintNow();
                        btnScan.setText(Gui.getTextFrom(MyLocale.getMsg(7119, "Stop")));
                        btnScan.repaintNow();
                        String[] ports = SerialPort.enumerateAvailablePorts(); // in case of bluethooth this can take several seconds
                        if (ports == null) {
                            txtOutput.appendText(MyLocale.getMsg(7109, "Could not get list of available serial ports\n"), true);
                        } else {
                            scanRunning = true;
                            interruptScan = false;
                            int i;
                            for (i = 0; i < ports.length; i++) {
                                if (interruptScan) {
                                    txtOutput.appendText(MyLocale.getMsg(7120, "Canceled"), true); // MyLocale.getMsg(7109, "Could not get list of available serial ports\n"), true);
                                    fin();
                                    return;
                                }
                                if (!testPort(ports[i], baudRate))
                                    continue;
                                else {
                                    portName = ports[i];
                                    if (ed != null)
                                        ed.toControls("portName");
                                    break;
                                }
                            }
                            if (i >= ports.length)
                                txtOutput.appendText(MyLocale.getMsg(7110, "GPS not found\n"), true);
                        }
                        fin();
                    }

                    private void fin() {
                        scanRunning = false;
                        if (btnTest != null) {
                            btnTest.set(ControlConstants.Disabled, false);
                            btnTest.repaintNow();
                        }
                        if (btnScan != null) {
                            btnScan.setText(Gui.getTextFrom(MyLocale.getMsg(7103, "Scan$u")));
                            btnScan.repaintNow();
                        }
                    }
                }.start();
            } else { // port scan running -> stop it.
                interruptScan = true;
            }
        }
        if (field.equals("test")) {
            if (!gpsRunning) {
                ed_.fromControls();

                switch (Preferences.itself().useGPSD) {
                    case Preferences.GPSD_FORMAT_NEW:
                        txtOutput.setText(MyLocale.getMsg(99999, "Displaying data from gpsd directly (JSON):\n"));
                        try {
                            btnScan.set(ControlConstants.Disabled, true);
                            btnScan.repaintNow();
                            gpsdThread = new GpsdThread(txtOutput);
                            gpsdThread.start();
                            btnTest.setText(Gui.getTextFrom(MyLocale.getMsg(7118, "Stop")));
                            gpsRunning = true;
                        } catch (IOException e) {
                            new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(99999, "Could not connect to GPSD: ") + e.getMessage() + MyLocale.getMsg(99999, "\nPossible reasons:\nGPSD is not running or GPSD host is not reachable"))
                                    .wait(FormBase.OKB);
                        } catch (Exception e) {
                            // Other error (JSON/GPS).
                            new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(99999, "Could not initialize GPSD connection: ") + e.getMessage()).wait(FormBase.OKB);
                        }
                        break;

                    case Preferences.GPSD_FORMAT_OLD:
                        txtOutput.setText(MyLocale.getMsg(99999, "Displaying data from gpsd directly (old protocol):\n"));
                        try {
                            btnScan.set(ControlConstants.Disabled, true);
                            btnScan.repaintNow();
                            oldGpsdThread = new OldGpsdThread(txtOutput);
                            oldGpsdThread.start();
                            btnTest.setText(Gui.getTextFrom(MyLocale.getMsg(7118, "Stop")));
                            gpsRunning = true;
                        } catch (IOException e) {
                            new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(99999, "Could not connect to GPSD: ") + e.getMessage() + MyLocale.getMsg(99999, "\nPossible reasons:\nGPSD is not running or GPSD host is not reachable"))
                                    .wait(FormBase.OKB);
                        }
                        break;

                    case Preferences.GPSD_DISABLED:
                    default:
                        txtOutput.setText(MyLocale.getMsg(7117, "Displaying data from serial port directly:\n"));
                        try {
                            btnScan.set(ControlConstants.Disabled, true);
                            btnScan.repaintNow();
                            this.portName = Common.fixSerialPortName(portName);
                            serThread = new mySerialThread(this, txtOutput);
                            serThread.start();
                            btnTest.setText(Gui.getTextFrom(MyLocale.getMsg(7118, "Stop")));
                            gpsRunning = true;
                        } catch (IOException e) {
                            btnScan.set(ControlConstants.Disabled, false);
                            btnScan.repaintNow();
                            txtOutput.appendText(MyLocale.getMsg(7108, "Failed to open serial port: ") + this.portName + ", IOException: " + e.getMessage() + "\n", true);
                        }
                        break;
                }
            } else {
                if (serThread != null)
                    serThread.stop();
                if (gpsdThread != null)
                    gpsdThread.stop();
                if (oldGpsdThread != null)
                    oldGpsdThread.stop();
                btnTest.setText(Gui.getTextFrom(MyLocale.getMsg(7104, "Test$t")));
                gpsRunning = false;
                btnScan.set(ControlConstants.Disabled, false);
                btnScan.repaintNow();
            }

        }

        super.action(field, ed_);
    }

    public void fieldEvent(FieldTransfer xfer, Editor editor, Object event) {
        if (event != null && event instanceof EditorEvent) {
            EditorEvent ev = (EditorEvent) event;
            if (xfer.fieldName.equals("_editor_")) {
                if (ev.type == EditorEvent.CLOSED) {
                    if (serThread != null)
                        serThread.stop();
                }
            }
            super.fieldEvent(xfer, editor, event);
        }
    }

    private boolean testPort(String port, int baud) {
        mySerialThread gpsPort;
        long now;

        SerialPortOptions testspo = new SerialPortOptions();
        testspo.baudRate = baud;
        testspo.portName = Common.fixSerialPortName(port);
        try {
            gpsPort = new mySerialThread(testspo, null);
        } catch (IOException e) {
            txtOutput.appendText(MyLocale.getMsg(7108, "Failed to open serial port: ") + testspo.portName + "\n", true);
            return false;
        }
        //if (!gpsPort.isOpen()) txtOutput.appendText(MyLocale.getMsg(7108, "Failed (2) to open serial port: ") + this.portName + "\n", true);

        //try to read some data
        now = new Time().getTime();
        txtOutput.appendText(MyLocale.getMsg(7111, "Trying ") + port + MyLocale.getMsg(7112, " at ") + baud + " Baud\n", true);
        gpsPort.start();
        boolean gpsfound = false;
        boolean gotdata = false;
        while ((new Time().getTime() - now) < 3000 && !gpsfound) {
            //			gpsLen = gpsPort.lastgot.length(); // nonBlockingRead(gpsBuff,0, gpsBuff.length);
            //txtOutput.appendText("gpsLen: " + gpsLen, true);
            if (gpsPort.lastgot != null) {
                if (!gotdata) {
                    gotdata = true;
                    txtOutput.appendText(MyLocale.getMsg(7113, " - got some data\n"), true);
                    now = new Time().getTime(); // if receiced some data, give the GPS some extra time to send NMEA data (e.g. Sirf initially sends some non-NMEA text info about it self)
                }
                if (gpsPort.nonBlockingRead().indexOf("$GP", 0) >= 0)
                    gpsfound = true;
            }
            try {
                ewe.sys.mThread.sleep(200);
            } catch (InterruptedException e) {
                // Preferences.itself().log("Ignored exception", e, true);
            }
        }
        gpsPort.stop();
        if (gpsfound)
            txtOutput.appendText(MyLocale.getMsg(7114, " - GPS Port found\n"), true);
        else {
            if (gotdata)
                txtOutput.appendText(MyLocale.getMsg(7115, " - No GPS data tag found\n"), true);
            else
                txtOutput.appendText(MyLocale.getMsg(7116, " - No data received\n"), true);
        }
        //catch (IOException io) { txtOutput.appendText("error closing serial port", true); }
        return gpsfound;
    }

}

/**
 * Descendant from ewe.ui.Editor to allow more flexibility when needed
 *
 * @author engywuck
 */
class MyEditor extends Editor {

    // Constraint used to align buttons of MyEditor
    public int buttonConstraints = CellConstants.CENTER;

    /**
     * Mainly overwritten of ewe.ui.Editor, except for the placement constraints for
     * the buttons which allow for variable buttonConstraints.
     */
    protected void checkButtons() {
        if (buttons != null) {
            if (buttons.size() != 0) {
                if (Gui.isSmartPhone && getSoftKeyBarFor(null) == null) {
                    buttonsToSoftKeyBar(buttons, (no != null && cancel != null) ? "No/Cancel" : "Actions", BUTTONS_TO_SOFT_KEY_FIRST_BUTTON_SEPARATE);
                } else {
                    CellPanel p = new CellPanel();
                    p.defaultTags.set(INSETS, new Insets(0, 1, 0, 1));
                    p.modify(AlwaysEnabled | NotAnEditor, 0); // Just in case a dialog pops up
                    // with global disabling.
                    for (int i = 0; i < buttons.size(); i++) {
                        p.addNext((Control) buttons.get(i));
                        if ((buttonsPerRow > 0) && (((i + 1) % buttonsPerRow) == 0))
                            p.endRow();
                    }
                    p.endRow();
                    CellPanel p2 = buttonsPanel = new CellPanel();
                    p.defaultTags.set(INSETS, new Insets(2, 2, 2, 2));
                    //
                    // Here is difference from ewe.ui.Editor: CENTER -> buttonConstraints
                    //
                    p2.addLast(p).setControl(buttonConstraints);// p2.borderStyle =
                    // Graphics.EDGE_SUNKEN;
                }
            }
        }
        if (!hasExitButton()) {
            if (Gui.isSmartPhone) {
                if (getSoftKeyBarFor(null) == null) {
                    SoftKeyBar sk = makeSoftKeys();
                    sk.setKey(1, "Close|" + EXIT_IDCANCEL, close, null);
                }
            } else {
                titleOK = new mButton(close);// getButton("OK");
                titleOK.backGround = Color.DarkBlue;
                ((mButton) titleOK).insideColor = getBackground();
            }
        }
        if (titleOK != null)
            titleOK.modify(AlwaysEnabled | NotAnEditor, 0);
        if (titleCancel != null)
            titleCancel.modify(AlwaysEnabled | NotAnEditor, 0);
    }

}
