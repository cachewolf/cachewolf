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

import CacheWolf.controls.*;
import CacheWolf.database.Attribute;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheType;
import CacheWolf.utils.Metrics;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import ewe.fx.*;
import ewe.graphics.AniImage;
import ewe.graphics.InteractivePanel;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.*;

/**
 * This class displays a form that the user uses to set the filter criteria.
 * Class ID=700
 */
public class FilterScreen extends Form {
    private static final Color COLOR_FILTERINACTIVE = new Color(190, 190, 190);
    private static final Color COLOR_FILTERACTIVE = new Color(0, 255, 0);
    private static final Color COLOR_FILTERALL = new Color(255, 0, 0); // Red

    private final ExecutePanel executePanel;
    AttributesSelector attV;
    private mButton btnSaveFlt, btnDelFlt, btnBearing, btnTypes, btnStati, btnRatings, btnContainer, btnSearch, btnAddi, btnCacheAttributes;
    private mChoice chcDist, chcDiff, chcTerr, chcAttrib;
    private ExecutePanel selectRose, selectCacheTypes;
    private CardPanel cp;
    private mCheckBox chkTrad, chkMulti, chkMystery, chkWebcam, chkVirtual, chkEvent, chkEarth, chkMega, chkLetter, chkLocless, chkWherigo, chkCito, chkApe, chkMaze, chkCustom;
    private myChkBox addiWptChk;
    //
    private mCheckBox chkParking, chkStage, chkQuestion, chkFinal, chkTrailhead, chkReference;
    private mCheckBox chkMicro, chkSmall, chkRegular, chkLarge, chkVeryLarge, chkOther, chkNotChosen, chkSizeVirtual;
    private mCheckBox chkPremium, chkNoPremium, chkSolved, chkNotSolved, chkFound, chkNotFound, chkOwned, chkNotOwned, chkArchived, chkNotArchived, chkAvailable, chkNotAvailable;
    private mCheckBox chkNW, chkNNW, chkN, chkNNE, chkNE, chkENE, chkE, chkESE, chkSE, chkSSE, chkS, chkSSW, chkSW, chkWSW, chkW, chkWNW, chkNoCoord;
    private mComboBox chcStatus;
    private mChoice fltList;
    private mCheckBox chkUseRegexp;
    // elements for the search panel
    private mChoice syncDateCompare;
    private mChoice srchNameCompare;
    private mCheckBox srchNameCaseSensitive;
    private mInput syncDateInput;
    private mButton btnSrchSyncDate;
    private mButton btnSrchSyncDateClear;
    private mInput srchNameInput;
    private mButton btnSrchNameClear;
    private mButton btnClearSearch;
    private mInput inpDist, inpTerr, inpDiff;


    // ID of last filter selected from the filter list
    private String currentFilterID = "";
    // Flag, true if filters have been changed, added or deleted. Leads to saving of pref.xml
    private boolean savedFiltersChanged = false;

    public FilterScreen() {
        this.title = MyLocale.getMsg(700, "Set Filter");

        CellPanel frmScreen = new CellPanel();
        mLabel lblInfo;
        frmScreen.addLast(lblInfo = new mLabel(MyLocale.getMsg(725, "Note: Filters are additive, active filter=green"))).setTag(SPAN, new Dimension(2, 1));
        lblInfo.setTag(INSETS, new Insets(0, 0, 2, 0));
        frmScreen.borderStyle = UIConstants.BDR_RAISEDOUTER | UIConstants.BDR_SUNKENINNER | UIConstants.BF_BOTTOM;
        this.addLast(frmScreen, HSTRETCH, HFILL);

        // On small screens the buttons gets too wide, additional panel as workaround
        CellPanel middlePanel = new CellPanel();
        middlePanel.addNext(FilterButtons(), HSTRETCH, FILL);
        cp = new CardPanel();
        cp.addItem(CacheTypes(), btnTypes.text, null);
        cp.select(btnTypes.text);
        cp.addItem(AdditionalWaypoints(), btnAddi.text, null);
        cp.addItem(BearingAndDistance(), btnBearing.text, null);
        cp.addItem(CacheStati(), btnStati.text, null);
        cp.addItem(CacheRatings(), btnRatings.text, null);
        cp.addItem(CacheContainer(), btnContainer.text, null);
        cp.addItem(Search(), btnSearch.text, null);
        cp.addItem(CacheAttributes(), btnCacheAttributes.text, null);
        middlePanel.addLast(cp, STRETCH, FILL);
        this.addLast(middlePanel);

        CellPanel fltListPanel = new CellPanel();
        fltListPanel.equalWidths = true;
        fltListPanel.addNext(fltList = new mChoice());
        fltListPanel.addNext(btnDelFlt = GuiImageBroker.getButton("", "trash"));
        fltListPanel.addLast(btnSaveFlt = GuiImageBroker.getButton("", "clsave"));
        this.addLast(fltListPanel);

        executePanel = new ExecutePanel(this);

        // Populating the comboBox of saved filters
        buildFilterList();
    }

    private CellPanel BearingAndDistance() {
        //////////////////////////
        // Panel 1 - Bearing & Distance
        //////////////////////////
        CellPanel pnl = new CellPanel();
        addTitle(pnl, MyLocale.getMsg(714, "Bearings & Distance"));

        pnl.addNext(new mLabel(MyLocale.getMsg(701, "Distance: ")), DONTSTRETCH, FILL);
        pnl.addNext(chcDist = new mChoice(new String[]{"<=", ">="}, 0), DONTSTRETCH, (DONTFILL | WEST));
        pnl.addLast(inpDist = new mInput(), DONTSTRETCH, FILL);
        pnl.addLast(new mLabel(""));
        pnl.addLast(chkNoCoord = new mCheckBox(MyLocale.getMsg(743, "No coordinates")), HSTRETCH, FILL);

        CellPanel pnlRose = new CellPanel();
        pnlRose.equalWidths = true;
        pnlRose.addNext(chkNW = new mCheckBox("NW"), HSTRETCH, FILL);
        pnlRose.addNext(chkNNW = new mCheckBox("NNW"), HSTRETCH, FILL);
        pnlRose.addNext(chkN = new mCheckBox("N"), HSTRETCH, FILL);
        pnlRose.addLast(chkNNE = new mCheckBox("NNE"), HSTRETCH, FILL);

        pnlRose.addNext(chkNE = new mCheckBox("NE"), HSTRETCH, FILL);
        pnlRose.addNext(chkENE = new mCheckBox("ENE"), HSTRETCH, FILL);
        pnlRose.addNext(chkE = new mCheckBox("E "), HSTRETCH, FILL);
        pnlRose.addLast(chkESE = new mCheckBox("ESE"), HSTRETCH, FILL);

        pnlRose.addNext(chkSE = new mCheckBox("SE"), HSTRETCH, FILL);
        pnlRose.addNext(chkSSE = new mCheckBox("SSE"), HSTRETCH, FILL);
        pnlRose.addNext(chkS = new mCheckBox("S"), HSTRETCH, FILL);
        pnlRose.addLast(chkSSW = new mCheckBox("SSW"), HSTRETCH, FILL);

        pnlRose.addNext(chkSW = new mCheckBox("SW"), HSTRETCH, FILL);
        pnlRose.addNext(chkWSW = new mCheckBox("WSW"), HSTRETCH, FILL);
        pnlRose.addNext(chkW = new mCheckBox("W "), HSTRETCH, FILL);
        pnlRose.addLast(chkWNW = new mCheckBox("WNW"), HSTRETCH, FILL);

        selectRose = new ExecutePanel(pnlRose);
        selectRose.setText(MyLocale.getMsg(717, "Select all"), FormBase.YESB);
        selectRose.setText(MyLocale.getMsg(716, "Deselect all"), FormBase.CANCELB);

        pnl.addLast(pnlRose, STRETCH, FILL);
        return pnl;
    }

    private CellPanel CacheStati() {
        //////////////////////////
        // Panel 2 - Cache attributes
        //////////////////////////
        CellPanel pnl = new CellPanel();

        addTitle(pnl, MyLocale.getMsg(720, "Status"));

        mLabel lblTitleAtt;
        pnl.addLast(lblTitleAtt = new mLabel(MyLocale.getMsg(715, "Show all caches with status:")), HSTRETCH, FILL);
        lblTitleAtt.setTag(SPAN, new Dimension(2, 1));

        pnl.addNext(chkArchived = new mCheckBox(MyLocale.getMsg(710, "Archived")), DONTSTRETCH, FILL);
        pnl.addLast(chkNotArchived = new mCheckBox(MyLocale.getMsg(729, "Nicht archiviert")), DONTSTRETCH, FILL);

        pnl.addNext(chkAvailable = new mCheckBox(MyLocale.getMsg(730, "Suchbar")), DONTSTRETCH, FILL);
        pnl.addLast(chkNotAvailable = new mCheckBox(MyLocale.getMsg(711, "Not available")), DONTSTRETCH, FILL);

        pnl.addNext(chkFound = new mCheckBox(MyLocale.getMsg(703, "Found")), DONTSTRETCH, FILL);
        pnl.addLast(chkNotFound = new mCheckBox(MyLocale.getMsg(731, "Noch nicht gefunden")), DONTSTRETCH, FILL);

        pnl.addNext(chkOwned = new mCheckBox(MyLocale.getMsg(707, "Owned")), DONTSTRETCH, FILL);
        pnl.addLast(chkNotOwned = new mCheckBox(MyLocale.getMsg(732, "Anderer Besitzer")), DONTSTRETCH, FILL);

        pnl.addNext(chkPremium = new mCheckBox(MyLocale.getMsg(751, "Is Premium")), DONTSTRETCH, FILL);
        pnl.addLast(chkNoPremium = new mCheckBox(MyLocale.getMsg(752, "Isn't Premium")), DONTSTRETCH, FILL);

        pnl.addNext(chkSolved = new mCheckBox(MyLocale.getMsg(753, "Is solved")), DONTSTRETCH, FILL);
        pnl.addLast(chkNotSolved = new mCheckBox(MyLocale.getMsg(754, "Isn't solved")), DONTSTRETCH, FILL);

        pnl.addNext(new mLabel(MyLocale.getMsg(307, "Status:")), DONTSTRETCH, (DONTFILL | WEST));
        pnl.addLast(chcStatus = new mComboBox(CacheHolder.GetGuiLogTypes(), 0), HSTRETCH, (HFILL | WEST));
        pnl.addLast(chkUseRegexp = new mCheckBox(MyLocale.getMsg(299, "Regular expression")));

        return pnl;
    }

    private CellPanel CacheRatings() {
        //////////////////////////
        // Panel 3 - Cache ratings
        //////////////////////////
        CellPanel pnl = new CellPanel();
        addTitle(pnl, MyLocale.getMsg(718, "Cache ratings"));
        pnl.addNext(new mLabel(MyLocale.getMsg(702, "Difficulty: ")), DONTSTRETCH, FILL);
        pnl.addNext(chcDiff = new mChoice(new String[]{"<=", "=", ">="}, 0), DONTSTRETCH, (DONTFILL | WEST));
        //pnlRatings.addLast(difIn = new mChoice(new String[]{"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"},0),DONTSTRETCH, (DONTFILL|WEST));
        pnl.addLast(inpDiff = new mInput(), DONTSTRETCH, FILL);

        pnl.addNext(new mLabel("Terrain: "), DONTSTRETCH, FILL);
        pnl.addNext(chcTerr = new mChoice(new String[]{"<=", "=", ">="}, 0), DONTSTRETCH, (DONTFILL | WEST));
        //pnlRatings.addLast(terrIn = new mChoice(new String[]{"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"},0),DONTSTRETCH, (DONTFILL|WEST));
        pnl.addLast(inpTerr = new mInput(), DONTSTRETCH, FILL);
        return pnl;
    }

    private CellPanel CacheTypes() {
        //////////////////////////
        // Panel 4 - Cache types
        //////////////////////////
        CellPanel pnl = new CellPanel();

        addTitle(pnl, MyLocale.getMsg(719, "Cache types"));
        pnl.equalWidths = true;
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_TRADITIONAL)), HSHRINK, HCONTRACT);
        pnl.addNext(chkTrad = new mCheckBox("Traditonal"), DONTSTRETCH, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_MULTI)), HSHRINK, HCONTRACT);
        pnl.addLast(chkMulti = new mCheckBox("Multi"), DONTSTRETCH, FILL);

        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_VIRTUAL)), HSHRINK, HCONTRACT);
        pnl.addNext(chkVirtual = new mCheckBox("Virtual"), DONTSTRETCH, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_LETTERBOX)), HSHRINK, HCONTRACT);
        pnl.addLast(chkLetter = new mCheckBox("Letterbox"), DONTSTRETCH, FILL);

        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_EVENT)), HSHRINK, HCONTRACT);
        pnl.addNext(chkEvent = new mCheckBox("Event"), DONTSTRETCH, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_WEBCAM)), HSHRINK, HCONTRACT);
        pnl.addLast(chkWebcam = new mCheckBox("Webcam"), DONTSTRETCH, FILL);

        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_MYSTERY)), HSHRINK, HCONTRACT);
        pnl.addNext(chkMystery = new mCheckBox("Mystery"), DONTSTRETCH, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_EARTH)), HSHRINK, HCONTRACT);
        pnl.addLast(chkEarth = new mCheckBox("Earth"), DONTSTRETCH, FILL);

        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_LOCATIONLESS)), HSHRINK, HCONTRACT);
        pnl.addNext(chkLocless = new mCheckBox("Locationless"), DONTSTRETCH, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_MEGA_EVENT)), HSHRINK, HCONTRACT);
        pnl.addLast(chkMega = new mCheckBox("Mega-Ev."), DONTSTRETCH, FILL);

        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_CITO)), HSHRINK, HCONTRACT);
        pnl.addNext(chkCito = new mCheckBox("Cito-Ev."), DONTSTRETCH, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_WHEREIGO)), HSHRINK, HCONTRACT);
        pnl.addLast(chkWherigo = new myChkBox("WherIGo"), DONTSTRETCH, FILL);

        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_APE)), HSHRINK, HCONTRACT);
        pnl.addNext(chkApe = new myChkBox("Ape"), DONTSTRETCH, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_MAZE)), HSHRINK, HCONTRACT);
        pnl.addLast(chkMaze = new mCheckBox("Maze"), DONTSTRETCH, FILL);

        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_CUSTOM)), HSHRINK, HCONTRACT);
        pnl.addNext(chkCustom = new mCheckBox("Custom"), DONTSTRETCH, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_DRIVE_IN)), HSHRINK, HCONTRACT);
        pnl.addLast(addiWptChk = new myChkBox("Add. Wpt"), DONTSTRETCH, FILL);

        selectCacheTypes = new ExecutePanel(pnl);
        selectCacheTypes.setText(MyLocale.getMsg(717, "Select all"), FormBase.YESB);
        selectCacheTypes.setText(MyLocale.getMsg(716, "Deselect all"), FormBase.CANCELB);

        return pnl;
    }

    private CellPanel AdditionalWaypoints() {
        //////////////////////////
        // Panel 5 - Addi waypoints
        //////////////////////////
        CellPanel pnl = new CellPanel();
        addTitle(pnl, MyLocale.getMsg(726, "Additional waypoints"));
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_PARKING)), HSHRINK, HCONTRACT);
        pnl.addLast(chkParking = new mCheckBox("Parking"), HGROW, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_STAGE)), HSHRINK, HCONTRACT);
        pnl.addLast(chkStage = new mCheckBox("Stage"), HGROW, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_QUESTION)), HSHRINK, HCONTRACT);
        pnl.addLast(chkQuestion = new mCheckBox("Question"), HGROW, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_FINAL)), HSHRINK, HCONTRACT);
        pnl.addLast(chkFinal = new mCheckBox("Final"), HGROW, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_TRAILHEAD)), HSHRINK, HCONTRACT);
        pnl.addLast(chkTrailhead = new mCheckBox("Trailhead"), HGROW, FILL);
        pnl.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_REFERENCE)), HSHRINK, HCONTRACT);
        pnl.addLast(chkReference = new mCheckBox("Reference"), HGROW, FILL);
        pnl.addLast(new mLabel(""), VSTRETCH, FILL);
        return pnl;
    }

    private CellPanel CacheContainer() {
        //////////////////////////
        // Panel 6 - Cache container
        //////////////////////////
        CellPanel pnl = new CellPanel();
        addTitle(pnl, MyLocale.getMsg(727, "Cache container"));
        pnl.addNext(addImg(new Image(CacheSize.CW_GUIIMG_MICRO + ".png")), HSHRINK, HCONTRACT);
        pnl.addLast(chkMicro = new mCheckBox("Micro"), HGROW, FILL);
        pnl.addNext(addImg(new Image(CacheSize.CW_GUIIMG_SMALL + ".png")), HSHRINK, HCONTRACT);
        pnl.addLast(chkSmall = new mCheckBox("Small"), HGROW, FILL);
        pnl.addNext(addImg(new Image(CacheSize.CW_GUIIMG_NORMAL + ".png")), HSHRINK, HCONTRACT);
        pnl.addLast(chkRegular = new mCheckBox("Regular"), HGROW, FILL);
        pnl.addNext(addImg(new Image(CacheSize.CW_GUIIMG_LARGE + ".png")), HSHRINK, HCONTRACT);
        pnl.addLast(chkLarge = new mCheckBox("Large"), HGROW, FILL);
        pnl.addNext(addImg(new Image(CacheSize.CW_GUIIMG_VERYLARGE + ".png")), HSHRINK, HCONTRACT);
        pnl.addLast(chkVeryLarge = new mCheckBox("Very Large"), HGROW, FILL);
        pnl.addNext(addImg(new Image(CacheSize.CW_GUIIMG_OTHER + ".png")), HSHRINK, HCONTRACT);
        pnl.addLast(chkOther = new mCheckBox("Other"), HGROW, FILL);
        pnl.addNext(addImg(new Image(CacheSize.CW_GUIIMG_NOTCHOSEN + ".png")), HSHRINK, HCONTRACT);
        pnl.addLast(chkNotChosen = new mCheckBox("Not chosen"), HGROW, FILL);
        pnl.addNext(addImg(new Image(CacheSize.CW_GUIIMG_VIRTUAL + ".png")), HSHRINK, HCONTRACT);
        pnl.addLast(chkSizeVirtual = new mCheckBox("Virtual"), HGROW, FILL);
        pnl.addLast(new mLabel(""), VSTRETCH, HCONTRACT);
        return pnl;
    }

    private CellPanel Search() {
        //////////////////////////
        // Panel 7 - Search
        //////////////////////////
        CellPanel pnl = new CellPanel();
        addTitle(pnl, MyLocale.getMsg(133, "Search"));
        // Search for sync date
        CellPanel pnlSyncDate = new CellPanel();
        pnlSyncDate.setText(MyLocale.getMsg(1051, "Last sync date:"));
        pnlSyncDate.addLast(syncDateCompare = new mChoice(new String[]{MyLocale.getMsg(747, "is before"), MyLocale.getMsg(748, "is at"), MyLocale.getMsg(749, "is after")}, 0));
        pnlSyncDate.addNext(syncDateInput = new mInput(""));
        pnlSyncDate.addLast(btnSrchSyncDate = GuiImageBroker.getButton("", "calendar"));
        pnlSyncDate.addLast(btnSrchSyncDateClear = GuiImageBroker.getButton("", "clear"));
        syncDateInput.modifyAll(DisplayOnly, 0);
        btnSrchSyncDate.setToolTip(MyLocale.getMsg(31415, "Set found date / time"));
        pnl.addLast(pnlSyncDate, HSHRINK, HFILL);
        // Search for cache name
        CellPanel pnlName = new CellPanel();
        pnlName.setText(MyLocale.getMsg(303, "Name :"));
        pnlName.addNext(srchNameCaseSensitive = new mCheckBox(MyLocale.getMsg(750, "Upper / lower case")));
        pnlName.addLast(srchNameCompare = new mChoice(new String[]{MyLocale.getMsg(744, "begins with"), MyLocale.getMsg(745, "contains"), MyLocale.getMsg(746, "ends with"), MyLocale.getMsg(755, "Doesn't contain")}, 0));
        pnlName.addLast(srchNameInput = new mInput());
        pnlName.addLast(btnSrchNameClear = GuiImageBroker.getButton("", "clear"));
        pnl.addLast(pnlName, HSHRINK, HFILL);
        // Clear button for whole search panel
        pnl.addLast(btnClearSearch = GuiImageBroker.getButton("Clear Search", "clear"), HSHRINK | HFILL, BOTTOM);
        return pnl;
    }

    private CellPanel OwnerPanel() {
        // Search for owner
        // coming soon
        CellPanel pnl = new CellPanel();
        return pnl;
    }

    private CellPanel CacheAttributes() {
        //////////////////////////
        // Panel 8 - Cache attributes
        //////////////////////////
        CellPanel pnl = new CellPanel();
        if (Preferences.itself().getScreenHeight() > 240)
            addTitle(pnl, MyLocale.getMsg(737, "Attributes"));
        pnl.addNext(new mLabel(MyLocale.getMsg(739, "Filter on") + ":"), DONTSTRETCH, LEFT);
        pnl.addLast(chcAttrib = new mChoice(new String[]{MyLocale.getMsg(740, "all"), MyLocale.getMsg(741, "one"), MyLocale.getMsg(742, "none")}, 0), DONTSTRETCH, LEFT);
        pnl.addLast(attV = new AttributesSelector(), DONTSTRETCH, CENTER | TOP);
        long[] ini = {0l, 0l, 0l, 0l};
        attV.setSelectionMasks(ini);
        return pnl;
    }

    private CellPanel FilterButtons() {
        CellPanel pnl = new CellPanel();
        pnl.addLast(new mLabel("Filter"));
        pnl.addLast(btnTypes = new mButton(MyLocale.getMsg(723, "Types")));
        pnl.addLast(btnAddi = new mButton(MyLocale.getMsg(733, "Add. Wpt")));
        pnl.addLast(btnBearing = new mButton(MyLocale.getMsg(721, "Bearing")));
        pnl.addLast(btnStati = new mButton(MyLocale.getMsg(720, "Attributes")));
        pnl.addLast(btnRatings = new mButton(MyLocale.getMsg(722, "Ratings")));
        pnl.addLast(btnContainer = new mButton(MyLocale.getMsg(724, "Container")));
        pnl.addLast(btnCacheAttributes = new mButton(MyLocale.getMsg(738, "Attributes")));
        pnl.addLast(btnSearch = new mButton(MyLocale.getMsg(133, "Search")));
        return pnl;
    }

    private mButton addImg(Image pImage) {
        mButton mb = new mButton(pImage);
        mb.borderWidth = 0;
        mb.modify(NotEditable | PreferredSizeOnly, 0);
        return mb;
    }

    private void addTitle(CellPanel c, String pTitle) {
        mLabel lblTitle;
        c.addLast(lblTitle = new mLabel(pTitle), HSTRETCH, FILL | CENTER);
        lblTitle.backGround = new Color(127, 127, 127);
        lblTitle.foreGround = Color.White;
        lblTitle.setTag(INSETS, new Insets(2, 0, 4, 0));
    }

    public void resizeTo(int width, int height) {
        attV.changeIapSize(width, height);
        this.relayout(true);
        super.resizeTo(width, height);
    }

    public void setData(FilterData data) {

        //////////////////////////
        // Panel 1 - Bearing & Distance
        //////////////////////////

        if (data.getFilterDist().length() > 1) {
            if (data.getFilterDist().charAt(0) == 'L')
                chcDist.select(0);
            else
                chcDist.select(1);
            String dist = data.getFilterDist().substring(1);
            if (Preferences.itself().metricSystem == Metrics.IMPERIAL) {
                double distValue = java.lang.Double.valueOf(dist).doubleValue();
                double newDistValue = Metrics.convertUnit(distValue, Metrics.KILOMETER, Metrics.MILES);
                dist = String.valueOf(newDistValue);
            }
            inpDist.setText(dist);
        } else {
            chcDist.select(0);
            inpDist.setText("");
        }
        chkNoCoord.state = data.getFilterNoCoord();
        String fltRose = data.getFilterRose();
        chkNW.state = fltRose.charAt(0) == '1';
        chkNNW.state = fltRose.charAt(1) == '1';
        chkN.state = fltRose.charAt(2) == '1';
        chkNNE.state = fltRose.charAt(3) == '1';

        chkNE.state = fltRose.charAt(4) == '1';
        chkENE.state = fltRose.charAt(5) == '1';
        chkE.state = fltRose.charAt(6) == '1';
        chkESE.state = fltRose.charAt(7) == '1';

        chkSE.state = fltRose.charAt(8) == '1';
        chkSSE.state = fltRose.charAt(9) == '1';
        chkS.state = fltRose.charAt(10) == '1';
        chkSSW.state = fltRose.charAt(11) == '1';

        chkSW.state = fltRose.charAt(12) == '1';
        chkWSW.state = fltRose.charAt(13) == '1';
        chkW.state = fltRose.charAt(14) == '1';
        chkWNW.state = fltRose.charAt(15) == '1';

        //////////////////////////
        // Panel 2 - Cache attributes
        //////////////////////////
        String fltVar = data.getFilterVar();
        chkArchived.state = fltVar.charAt(0) == '1';
        chkAvailable.state = fltVar.charAt(1) == '1';
        chkFound.state = fltVar.charAt(2) == '1';
        chkOwned.state = fltVar.charAt(3) == '1';
        chkNotArchived.state = fltVar.charAt(4) == '1';
        chkNotAvailable.state = fltVar.charAt(5) == '1';
        chkNotFound.state = fltVar.charAt(6) == '1';
        chkNotOwned.state = fltVar.charAt(7) == '1';
        chkPremium.state = fltVar.charAt(8) == '1';
        chkNoPremium.state = fltVar.charAt(9) == '1';
        chkSolved.state = fltVar.charAt(10) == '1';
        chkNotSolved.state = fltVar.charAt(11) == '1';
        chcStatus.setText(data.getFilterStatus());
        chkUseRegexp.setState(data.useRegexp());

        //////////////////////////
        // Panel 3 - Cache ratings
        //////////////////////////

        if (data.getFilterDiff().length() > 1) {
            if (data.getFilterDiff().charAt(0) == 'L')
                chcDiff.select(0);
            else if (data.getFilterDiff().charAt(0) == '=')
                chcDiff.select(1);
            else
                chcDiff.select(2);
            inpDiff.setText(data.getFilterDiff().substring(1));
        } else {
            chcDiff.select(0);
            inpDiff.setText("");
        }

        if (data.getFilterTerr().length() > 1) {
            if (data.getFilterTerr().charAt(0) == 'L')
                chcTerr.select(0);
            else if (data.getFilterTerr().charAt(0) == '=')
                chcTerr.select(1);
            else
                chcTerr.select(2);
            inpTerr.setText(data.getFilterTerr().substring(1));
        } else {
            chcTerr.select(0);
            inpTerr.setText("");
        }

        //////////////////////////
        // Panel 4 - Cache types
        //////////////////////////

        String fltType = data.getFilterType();
        chkTrad.state = fltType.charAt(0) == '1';
        chkMulti.state = fltType.charAt(1) == '1';
        chkVirtual.state = fltType.charAt(2) == '1';
        chkLetter.state = fltType.charAt(3) == '1';
        chkEvent.state = fltType.charAt(4) == '1';
        chkWebcam.state = fltType.charAt(5) == '1';
        chkMystery.state = fltType.charAt(6) == '1';
        chkEarth.state = fltType.charAt(7) == '1';
        chkLocless.state = fltType.charAt(8) == '1';
        chkMega.state = fltType.charAt(9) == '1';
        chkCustom.state = fltType.charAt(10) == '1';
        chkCito.state = fltType.charAt(17) == '1';
        chkWherigo.state = fltType.charAt(18) == '1';
        chkApe.state = fltType.charAt(19) == '1';
        chkMaze.state = fltType.charAt(20) == '1';

        // Note addiWptState is set by setColors

        //////////////////////////
        // Panel 5 - Additional waypoints
        //////////////////////////

        chkParking.state = fltType.charAt(11) == '1';
        chkStage.state = fltType.charAt(12) == '1';
        chkQuestion.state = fltType.charAt(13) == '1';
        chkFinal.state = fltType.charAt(14) == '1';
        chkTrailhead.state = fltType.charAt(15) == '1';
        chkReference.state = fltType.charAt(16) == '1';
        addiWptChk.state = !fltType.substring(11, 17).equals("000000");

        //////////////////////////
        // Panel 6 - Cache container
        //////////////////////////

        String fltSize = data.getFilterSize();
        chkMicro.state = fltSize.charAt(0) == '1';
        chkSmall.state = fltSize.charAt(1) == '1';
        chkRegular.state = fltSize.charAt(2) == '1';
        chkLarge.state = fltSize.charAt(3) == '1';
        chkVeryLarge.state = fltSize.charAt(4) == '1';
        chkOther.state = fltSize.charAt(5) == '1';
        chkNotChosen.state = fltSize.charAt(6) == '1';
        chkSizeVirtual.state = fltSize.charAt(7) == '1';

        //////////////////////////
        // Panel 7 - Search
        //////////////////////////

        String syncDate = data.getSyncDate();
        if (syncDate.length() >= 10) {
            // First sign is <, =, >, followed by '-' and then yyyymmdd
            String theOperator = syncDate.substring(0, 1);
            String theDate = syncDate.substring(2, 10);
            syncDateInput.setText(theDate.substring(0, 4) + "-" + theDate.substring(4, 6) + "-" + theDate.substring(6, 8));
            syncDateCompare.select(2);
            if (theOperator.equals("<"))
                syncDateCompare.select(0);
            if (theOperator.equals("="))
                syncDateCompare.select(1);
            if (theOperator.equals(">"))
                syncDateCompare.select(2);
        } else {
            syncDateInput.setText("");
        }

        this.srchNameInput.setText(data.getNamePattern());
        this.srchNameCompare.select(data.getNameCompare());
        this.srchNameCaseSensitive.setState(data.getNameCaseSensitive());

        //////////////////////////
        // Panel 8 - Cache attributes
        //////////////////////////

        attV.setSelectionMasks(data.getFilterAttr());
        chcAttrib.select(data.getFilterAttrChoice());

        // Adjust colors of buttons depending on which filters are active
        setColors();
    }

    // Set the colors of the filter buttons according to which filters are active
    private void setColors() {
        // Panel 1 - Bearing & Distance
        if (inpDist.getText().length() > 0
                || !(chkNW.state && chkNNW.state && chkN.state && chkNNE.state && chkNE.state && chkENE.state && chkE.state && chkESE.state && chkSE.state && chkSSE.state && chkS.state && chkSSW.state && chkSW.state && chkWSW.state && chkW.state
                && chkWNW.state && chkNoCoord.state))
            btnBearing.backGround = COLOR_FILTERACTIVE;
        else
            btnBearing.backGround = COLOR_FILTERINACTIVE;
        if (!(chkNW.state || chkNNW.state || chkN.state || chkNNE.state || chkNE.state || chkENE.state || chkE.state || chkESE.state || chkSE.state || chkSSE.state || chkS.state || chkSSW.state || chkSW.state || chkWSW.state || chkW.state
                || chkWNW.state || chkNoCoord.state))
            btnBearing.backGround = COLOR_FILTERALL;
        btnBearing.repaint();

        // Panel 2 - Cache attributes
        if (!(chkArchived.state && chkAvailable.state && chkFound.state && chkOwned.state && chkNotArchived.state && chkNotAvailable.state && chkNotFound.state && chkNotOwned.state && chkPremium.state && chkNoPremium.state && chkSolved.state
                && chkNotSolved.state && chcStatus.getText().equals("")))
            btnStati.backGround = COLOR_FILTERACTIVE;
        else
            btnStati.backGround = COLOR_FILTERINACTIVE;
        if ((chkArchived.state == false && chkNotArchived.state == false) || (chkAvailable.state == false && chkNotAvailable.state == false) || (chkFound.state == false && chkNotFound.state == false)
                || (chkOwned.state == false && chkNotOwned.state == false) || (chkPremium.state == false && chkNoPremium.state == false) || (chkSolved.state == false && chkNotSolved.state == false))
            btnStati.backGround = COLOR_FILTERALL;
        btnStati.repaint();

        // Panel 3 - Cache ratings
        if (inpDiff.getText().length() > 0 || inpTerr.getText().length() > 0)
            btnRatings.backGround = COLOR_FILTERACTIVE;
        else
            btnRatings.backGround = COLOR_FILTERINACTIVE;
        btnRatings.repaint();

        // Panel 5 - Addi Waypoints
        if (chkParking.state || chkStage.state || chkQuestion.state || chkFinal.state || chkTrailhead.state || chkReference.state) { // At least one tick
            btnAddi.backGround = COLOR_FILTERACTIVE;
            addiWptChk.state = true;
            if (chkParking.state && chkStage.state && chkQuestion.state && chkFinal.state && chkTrailhead.state && chkReference.state) { // All ticked?
                addiWptChk.bgColor = Color.White;
                btnAddi.backGround = COLOR_FILTERINACTIVE;
            } else {
                addiWptChk.bgColor = Color.LightGray;
            }
        } else { // All not ticked
            btnAddi.backGround = COLOR_FILTERACTIVE;
            addiWptChk.bgColor = Color.White;
            addiWptChk.state = false;
        }
        btnAddi.repaint();

        // Panel 4 - Cache types
        boolean allAddis = (chkParking.state && chkStage.state && chkQuestion.state && chkFinal.state && chkTrailhead.state && chkReference.state);
        if (!(chkTrad.state && chkMulti.state && chkVirtual.state && chkLetter.state && chkEvent.state && chkWebcam.state && chkMystery.state && chkEarth.state && chkLocless.state && chkMega.state && chkCito.state && chkWherigo.state && chkApe.state
                && chkMaze.state && chkCustom.state && allAddis))
            btnTypes.backGround = COLOR_FILTERACTIVE;
        else
            btnTypes.backGround = COLOR_FILTERINACTIVE;
        if (!(chkTrad.state || chkMulti.state || chkVirtual.state || chkLetter.state || chkEvent.state || chkWebcam.state || chkMystery.state || chkEarth.state || chkLocless.state || chkMega.state || chkCustom.state || chkParking.state
                || chkStage.state || chkQuestion.state || chkFinal.state || chkTrailhead.state || chkCito.state || chkWherigo.state || chkApe.state || chkMaze.state || chkReference.state))
            btnTypes.backGround = COLOR_FILTERALL;
        btnTypes.repaint();

        // Panel 6 - Cache container
        if (!(chkMicro.state && chkSmall.state && chkRegular.state && chkLarge.state && chkVeryLarge.state && chkOther.state && chkNotChosen.state && chkSizeVirtual.state))
            btnContainer.backGround = COLOR_FILTERACTIVE;
        else
            btnContainer.backGround = COLOR_FILTERINACTIVE;
        if (!(chkMicro.state || chkSmall.state || chkRegular.state || chkLarge.state || chkVeryLarge.state || chkOther.state || chkNotChosen.state || chkSizeVirtual.state))
            btnContainer.backGround = COLOR_FILTERALL;
        btnContainer.repaint();

        // Panel 7 - Search
        if (syncDateInput.getText().length() > 0 || srchNameInput.getText().length() > 0) {
            btnSearch.backGround = COLOR_FILTERACTIVE;
        } else {
            btnSearch.backGround = COLOR_FILTERINACTIVE;
        }
        btnSearch.repaint();

        // Panel 8 - Cache attributes
        if (attV.isSetSelectionMask())
            btnCacheAttributes.backGround = COLOR_FILTERACTIVE;
        else
            btnCacheAttributes.backGround = COLOR_FILTERINACTIVE;
        btnCacheAttributes.repaint();
    }

    /**
     * React to the users input, create a filter and set the variable of the filter.
     *
     * @see Filter
     */
    public void onEvent(Event ev) {
        if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
            if (ev.target == executePanel.cancelButton) {
                if (savedFiltersChanged) {
                    Preferences.itself().savePreferences();
                    savedFiltersChanged = false;
                }
                fltList.select(-1);
                currentFilterID = "";
                this.close(0);
            } else if (ev.target == executePanel.applyButton) {
                Vm.showWait(true);

                FilterData data = getDataFromScreen();
                MainForm.profile.setCurrentFilter(data);

                Filter flt = new Filter();
                flt.setFilter();
                flt.doFilter();
                if (savedFiltersChanged) {
                    Preferences.itself().savePreferences();
                    savedFiltersChanged = false;
                }
                MainTab.itself.tablePanel.myTableControl.scrollToVisible(0, 0);
                Vm.showWait(false);
                fltList.select(-1);
                currentFilterID = "";
                this.close(0);
            } else if (ev.target == btnSaveFlt) {
                String ID = fltList.getText();
                FilterData data = getDataFromScreen();
                InputBox inp = new InputBox("ID");
                String newID = inp.input(ID, 20);
                if (newID != null && !newID.equals("")) {
                    if (Preferences.itself().hasFilter(newID)) {
                        if (new InfoBox(MyLocale.getMsg(221, "Overwrite Filter?"), MyLocale.getMsg(222, "The filter already exists. Overwrite it?")).wait(FormBase.IDYES | FormBase.IDNO) == FormBase.IDYES) {
                            Preferences.itself().addFilter(newID, data);
                            savedFiltersChanged = true;
                            buildFilterList();
                        }
                    } else {
                        Preferences.itself().addFilter(newID, data);
                        savedFiltersChanged = true;
                        buildFilterList();
                    }
                }
            } else if (ev.target == btnDelFlt) {
                String ID = fltList.getText();
                if (!ID.equals("")) {
                    FilterData data = Preferences.itself().getFilter(ID);
                    // We only need to delete anything, if there is already a filter of the id
                    // in the list box. If not, just delete the text in the box.
                    if (data != null) {
                        if (new InfoBox(MyLocale.getMsg(223, "Delete filter?"), ID + MyLocale.getMsg(224, " - Delete this filter?")).wait(FormBase.IDYES | FormBase.IDNO) == FormBase.IDYES) {
                            Preferences.itself().removeFilter(ID);
                            fltList.setText("");
                            savedFiltersChanged = true;
                            this.buildFilterList();
                        }
                    } else {
                        fltList.setText("");
                    }
                }
            } else if (ev.target == addiWptChk) { // Set all addi filters to value of main addi filter
                chkParking.setState(addiWptChk.state);
                chkStage.setState(addiWptChk.state);
                chkQuestion.setState(addiWptChk.state);
                chkFinal.setState(addiWptChk.state);
                chkTrailhead.setState(addiWptChk.state);
                chkReference.setState(addiWptChk.state);
                addiWptChk.bgColor = Color.White;
                addiWptChk.repaint();
            } else if (ev.target == btnBearing)
                cp.select(btnBearing.text);
            else if (ev.target == btnStati)
                cp.select(btnStati.text);
            else if (ev.target == btnRatings)
                cp.select(btnRatings.text);
            else if (ev.target == btnTypes)
                cp.select(btnTypes.text);
            else if (ev.target == btnAddi)
                cp.select(btnAddi.text);
            else if (ev.target == btnContainer)
                cp.select(btnContainer.text);
            else if (ev.target == btnSearch)
                cp.select(btnSearch.text);
            else if (ev.target == btnCacheAttributes)
                cp.select(btnCacheAttributes.text);
            else if (ev.target == selectRose.cancelButton) {
                chkNW.state = chkNNW.state = chkN.state = chkNNE.state = chkNE.state = chkENE.state = chkE.state = chkESE.state = chkSE.state = chkSSE.state = chkS.state = chkSSW.state = chkSW.state = chkWSW.state = chkW.state = chkWNW.state = false;
                setColors();
                repaint();
            } else if (ev.target == selectRose.applyButton) {
                chkNW.state = chkNNW.state = chkN.state = chkNNE.state = chkNE.state = chkENE.state = chkE.state = chkESE.state = chkSE.state = chkSSE.state = chkS.state = chkSSW.state = chkSW.state = chkWSW.state = chkW.state = chkWNW.state = true;
                setColors();
                repaint();
            } else if (ev.target == selectCacheTypes.applyButton) {
                chkTrad.state = true;
                chkMulti.state = true;
                chkMystery.state = true;
                chkVirtual.state = true;
                chkLetter.state = true;
                chkEvent.state = true;
                chkWebcam.state = true;
                chkEarth.state = true;
                chkMega.state = true;
                chkLocless.state = true;
                chkWherigo.state = true;
                chkCito.state = true;
                chkApe.state = true;
                chkMaze.state = true;
                chkCustom.state = true;
                //addiWptChk.state = true; do more
                repaint();
                //
            } else if (ev.target == selectCacheTypes.cancelButton) {
                chkTrad.state = false;
                chkMulti.state = false;
                chkMystery.state = false;
                chkVirtual.state = false;
                chkLetter.state = false;
                chkEvent.state = false;
                chkWebcam.state = false;
                chkEarth.state = false;
                chkMega.state = false;
                chkLocless.state = false;
                chkWherigo.state = false;
                chkCito.state = false;
                chkApe.state = false;
                chkMaze.state = false;
                chkCustom.state = false;
                //addiWptChk.state = false; do more
                repaint();
            } else if (ev.target == btnSrchSyncDate) {
                final DateTimeChooser dc = new DateTimeChooser(Vm.getLocale(), false);
                dc.title = "Last Update Time";
                Preferences.itself().setSubWindowSize(dc);
                if (syncDateInput.getText().length() == 10)
                    try {
                        dc.setDate(new Time(Convert.parseInt(syncDateInput.getText().substring(8)), Convert.parseInt(syncDateInput.getText().substring(5, 7)), Convert.parseInt(syncDateInput.getText().substring(0, 4))));
                    } catch (NumberFormatException e) {
                        dc.reset(new Time());
                    }
                if (dc.run() == ewe.ui.FormBase.IDOK) {
                    syncDateInput.setText(Convert.toString(dc.year) + "-" + MyLocale.formatLong(dc.month, "00") + "-" + MyLocale.formatLong(dc.day, "00"));
                }
                setColors();
                repaint();
            } else if (ev.target == btnSrchSyncDateClear) {
                syncDateInput.setText("");
                setColors();
                repaint();
            } else if (ev.target == btnSrchNameClear) {
                srchNameInput.setText("");
                setColors();
                repaint();
            } else if (ev.target == btnClearSearch) {
                syncDateInput.setText("");
                srchNameInput.setText("");
                setColors();
                repaint();
            }
        }
        if (ev instanceof DataChangeEvent) {
            if (ev.target == fltList) {
                if (!currentFilterID.equals(fltList.getText())) {
                    FilterData data = Preferences.itself().getFilter(fltList.getText());
                    if (data != null) {
                        currentFilterID = fltList.getText();
                        this.setData(data);
                        this.repaintNow();
                    }
                }
            }
            setColors();
        }
    }

    /**
     * Populating the list of available filters in the comboBox from memory, so that the comboBox
     * reflects the filters that are currenty in memory.
     */
    private void buildFilterList() {
        while (fltList.itemsSize() > 0) {
            fltList.deleteItem(0);
        }
        fltList.addItems(Preferences.itself().getFilterIDs());
        fltList.updateItems();
    }

    /**
     * Examines the filter screen and creates a FilterData object that represents the data
     * entered in the screen.
     */
    private FilterData getDataFromScreen() {
        FilterData data = new FilterData();
        data.setFilterVar((chkArchived.state ? "1" : "0") + (chkAvailable.state ? "1" : "0") + (chkFound.state ? "1" : "0") + (chkOwned.state ? "1" : "0") + (chkNotArchived.state ? "1" : "0") + (chkNotAvailable.state ? "1" : "0")
                + (chkNotFound.state ? "1" : "0") + (chkNotOwned.state ? "1" : "0") + (chkPremium.state ? "1" : "0") + (chkNoPremium.state ? "1" : "0") + (chkSolved.state ? "1" : "0") + (chkNotSolved.state ? "1" : "0"));
        data.setFilterType((chkTrad.state ? "1" : "0") + (chkMulti.state ? "1" : "0") + (chkVirtual.state ? "1" : "0") + (chkLetter.state ? "1" : "0") + (chkEvent.state ? "1" : "0") + (chkWebcam.state ? "1" : "0") + (chkMystery.state ? "1" : "0")
                + (chkEarth.state ? "1" : "0") + (chkLocless.state ? "1" : "0") + (chkMega.state ? "1" : "0") + (chkCustom.state ? "1" : "0") + (chkParking.state ? "1" : "0") + (chkStage.state ? "1" : "0") + (chkQuestion.state ? "1" : "0")
                + (chkFinal.state ? "1" : "0") + (chkTrailhead.state ? "1" : "0") + (chkReference.state ? "1" : "0") + (chkCito.state ? "1" : "0") + (chkWherigo.state ? "1" : "0") + (chkApe.state ? "1" : "0") + (chkMaze.state ? "1" : "0"));
        data.setFilterRose((chkNW.state ? "1" : "0") + (chkNNW.state ? "1" : "0") + (chkN.state ? "1" : "0") + (chkNNE.state ? "1" : "0") + (chkNE.state ? "1" : "0") + (chkENE.state ? "1" : "0") + (chkE.state ? "1" : "0")
                + (chkESE.state ? "1" : "0") + (chkSE.state ? "1" : "0") + (chkSSE.state ? "1" : "0") + (chkS.state ? "1" : "0") + (chkSSW.state ? "1" : "0") + (chkSW.state ? "1" : "0") + (chkWSW.state ? "1" : "0") + (chkW.state ? "1" : "0")
                + (chkWNW.state ? "1" : "0"));
        data.setFilterSize((chkMicro.state ? "1" : "0") + (chkSmall.state ? "1" : "0") + (chkRegular.state ? "1" : "0") + (chkLarge.state ? "1" : "0") + (chkVeryLarge.state ? "1" : "0") + (chkOther.state ? "1" : "0")
                + (chkNotChosen.state ? "1" : "0") + (chkSizeVirtual.state ? "1" : "0"));

        // Distance: If Metric system is set to imperial units,
        //           then the entered value is meant to be miles,
        //           otherwise it's kilometer.
        double distValue = java.lang.Double.NaN;
        String rawDistance = inpDist.getText().replace(',', '.');
        String newDistance = rawDistance; // initial Value;
        if (!rawDistance.trim().equals("")) {
            distValue = java.lang.Double.valueOf(rawDistance).doubleValue();
            if (Preferences.itself().metricSystem == Metrics.IMPERIAL) {
                newDistance = String.valueOf(Metrics.convertUnit(distValue, Metrics.MILES, Metrics.KILOMETER));
            }
        }
        if (chcDist.selectedIndex == 0) {
            data.setFilterDist("L" + newDistance);
        } else {
            data.setFilterDist("G" + newDistance);
        }

        if (chcDiff.selectedIndex == 0) {
            data.setFilterDiff("L" + inpDiff.getText());
        } else if (chcDiff.selectedIndex == 1) {
            data.setFilterDiff("=" + inpDiff.getText());
        } else {
            data.setFilterDiff("G" + inpDiff.getText());
        }

        if (chcTerr.selectedIndex == 0) {
            data.setFilterTerr("L" + inpTerr.getText());
        } else if (chcTerr.selectedIndex == 1) {
            data.setFilterTerr("=" + inpTerr.getText());
        } else {
            data.setFilterTerr("G" + inpTerr.getText());
        }
        data.setFilterAttr(attV.getSelectionMasks());
        data.setFilterAttrChoice(chcAttrib.selectedIndex);
        data.setFilterStatus(chcStatus.getText());
        data.setUseRegexp(chkUseRegexp.getState());
        data.setFilterNoCoord(chkNoCoord.getState());

        if (syncDateInput.getText().length() == 0) {
            data.setSyncDate("");
        } else {
            // last sync has a special format, remove '-'
            String aDate = STRreplace.replace(syncDateInput.getText(), "-", "");
            String aDirection = ">-";
            if (syncDateCompare.selectedIndex == 0) {
                aDirection = "<-";
            } else if (syncDateCompare.selectedIndex == 1) {
                aDirection = "=-";
            } else {
                aDirection = ">-";
            }
            data.setSyncDate(aDirection + aDate);
        }

        data.setNamePattern(srchNameInput.getText());
        data.setNameCompare(this.srchNameCompare.selectedIndex);
        data.setNameCaseSensitive(this.srchNameCaseSensitive.getState());

        return data;
    }

    // A subclassed checkbox with a "third" state (=grey background).
    // If all addi wpts are false or all addi wpts are true, the background is white
    // If the addi wpt filter is a mixture of true/false, the bg is grey
    // Thus the addi filter can be set in one of two ways: Using the single checkbox with all the other
    // attributes, or using the multiple checkboxes for each addi waypoint type
    private class myChkBox extends mCheckBox {
        Color bgColor = Color.White;

        myChkBox(String s) {
            super(s);
        }

        public void doPaintSquare(Graphics g) {
            int h = height;
            g.setColor(bgColor);
            int bx = text.length() == 0 ? 0 : 2;
            int by = text.length() == 0 ? 0 : (h - boxWidth) / 2 + 1;
            g.fillRect(bx + 2, by + 2, boxWidth - 4, boxWidth - 4);
            if (state || pressState) {
                Color c = Color.LightGray;
                if (!pressState) {
                    if (!state)
                        c = bgColor;
                    else
                        c = Color.Black;
                }
                Pen oldpen = g.setPen(new Pen(c, Pen.SOLID, 2));
                g.drawLine(bx + 4, by + boxWidth - 5, bx + boxWidth - 5, by + 4);
                g.drawLine(bx + 4, by + boxWidth - 5, bx + 4, by + boxWidth - 10);
                //				g.drawLine(bx+3,by+3,bx+boxWidth-5,by+boxWidth-5);
                //				g.drawLine(bx+3,by+boxWidth-5,bx+boxWidth-5,by+3);
                g.setPen(oldpen);
            }
            g.draw3DRect(new Rect(bx, by, boxWidth, boxWidth), ButtonObject.checkboxEdge, true, null, Color.DarkGray);
        }
    }
}

class AttributesSelector extends Panel {
    protected static int TILESIZE;
    protected static int W_OFFSET; // depends on Preferences.itself().fontSize ?
    protected static int H_OFFSET; // depends on Preferences.itself().fontSize ?
    protected mLabel mInfo;
    protected InteractivePanel iap = new attInteractivePanel();
    protected MyScrollBarPanel scp = new MyScrollBarPanel(iap);
    private long[] selectionMaskYes = {0l, 0l};
    private long[] selectionMaskNo = {0l, 0l};
    private int virtualWidth;

    public AttributesSelector() {
        scp.setOptions(MyScrollBarPanel.NeverShowHorizontalScrollers);
        TILESIZE = 30;
        W_OFFSET = 100;
        H_OFFSET = 150;
        if (Vm.isMobile()) {
            if (Preferences.itself().getScreenWidth() == 240 & Preferences.itself().getScreenHeight() == 320) {
                TILESIZE = 28;
                W_OFFSET = 80;
                H_OFFSET = 120;
            }
            if (Preferences.itself().getScreenWidth() == 320 & Preferences.itself().getScreenHeight() == 240) {
            }
            if (Preferences.itself().getScreenWidth() == 480 & Preferences.itself().getScreenHeight() == 640) {
            }
            if (Preferences.itself().getScreenWidth() == 480 & Preferences.itself().getScreenHeight() == 800) {
            }
            if (Preferences.itself().getScreenWidth() == 640 & Preferences.itself().getScreenHeight() == 480) {
            }
        } else {
            TILESIZE = 36;
            W_OFFSET = 106;
            H_OFFSET = 150;
        }
        iap.virtualSize = new Rect(0, 0, 0, 0); // create once
        addLast(scp, STRETCH, FILL);
        addLast(mInfo = new mLabel(""), HSTRETCH, HFILL);
    }

    public long[] getSelectionMasks() {
        long[] SelectionMasks = new long[4];
        SelectionMasks[0] = selectionMaskYes[0];
        SelectionMasks[1] = selectionMaskYes[1];
        SelectionMasks[2] = selectionMaskNo[0];
        SelectionMasks[3] = selectionMaskNo[1];
        return SelectionMasks;
    }

    public void setSelectionMasks(long[] SelectionMasks) {
        selectionMaskYes[0] = SelectionMasks[0];
        selectionMaskYes[1] = SelectionMasks[1];
        selectionMaskNo[0] = SelectionMasks[2];
        selectionMaskNo[1] = SelectionMasks[3];
        showAttributePalette();
    }

    public boolean isSetSelectionMask() {
        return selectionMaskYes[0] != 0l || selectionMaskNo[0] != 0l || selectionMaskYes[1] != 0l || selectionMaskNo[1] != 0l;
    }

    private void showAttributePalette() {
        iap.images.clear();
        int myWidth = virtualWidth;
        int myX = 2;
        int myY = 2;
        int inc = 2;
        for (int i = 0; i < Attribute.maxAttRef; i++) {
            long[] bitMask = Attribute.getIdBit(i);
            if (((selectionMaskYes[0] & bitMask[0]) != 0) || ((selectionMaskYes[1] & bitMask[1]) != 0))
                inc = 1;
            else if (((selectionMaskNo[0] & bitMask[0]) != 0) || ((selectionMaskNo[1] & bitMask[1]) != 0))
                inc = 0;
            else
                inc = 2;
            Attribute att = new Attribute(i, inc);
            mImage rawImg = att.getImage();
            int iHeight = rawImg.getHeight();
            if (iHeight > 0) {
                attImage img = new attImage(iHeight != TILESIZE - 2 ? rawImg.scale(TILESIZE - 2, TILESIZE - 2, null, Image.FOR_DISPLAY) : rawImg, att);

                if (myX + TILESIZE > myWidth) {
                    myX = 2;
                    myY += TILESIZE;
                }
                img.location = new Rect(myX, myY, TILESIZE, TILESIZE);
                iap.addImage(img);
                myX += TILESIZE;
            }
        }
        iap.repaintNow();
    }

    private void setIapSize(int width, int height) {
        iap.setPreferredSize(width, height);
        Preferences.itself().log("[AttributesSelector:changeIapSize]  pref. area: " + width + "x" + height);

        int anzPerWidth = width / (TILESIZE + 2) - 1;
        virtualWidth = anzPerWidth * (TILESIZE + 2);
        double max = Attribute.maxAttRef;
        int anzPerHeight = (int) java.lang.Math.ceil(max / anzPerWidth);
        iap.virtualSize.set(0, 0, virtualWidth, anzPerHeight * (TILESIZE + 2));
        Preferences.itself().log("[AttributesSelector:setIapSize] virt. area: " + virtualWidth + "x" + anzPerHeight * (TILESIZE + 2));

    }

    public void changeIapSize(int width, int height) {
        Preferences.itself().log("[AttributesSelector:changeIapSize]  max. area: " + width + "x" + height);
        setIapSize(width - W_OFFSET, height - H_OFFSET);
        showAttributePalette();
    }

    protected class attImage extends AniImage {
        public Attribute att;

        attImage(mImage img, Attribute _att) {
            super(img);
            att = _att;
        }

        attImage(attImage cp, int val) {
            att = cp.att;
            att.setInc(val);
            mImage rawImg = att.getImage();
            setMImage(rawImg.getHeight() != TILESIZE - 2 ? rawImg.scale(TILESIZE - 2, TILESIZE - 2, null, Image.FOR_DISPLAY) : rawImg);
            location = cp.location;
        }
    }

    protected class attInteractivePanel extends InteractivePanel {
        public boolean imageMovedOn(AniImage which) {
            mInfo.setText(((attImage) which).att.getMsg());
            mInfo.repaintNow();
            return true;
        }

        public boolean imageMovedOff(AniImage which) {
            mInfo.setText("");
            mInfo.repaintNow();
            return true;
        }

        //Overrides: imagePressed(...) in InteractivePanel
        public boolean imagePressed(AniImage which, Point pos) {
            if (which != null) {
                int value = ((attImage) which).att.getInc();
                value = (value + 1) % 3;
                ((attImage) which).att.setInc(value);
                selectionMaskNo = ((attImage) which).att.getNoBit(selectionMaskNo);
                selectionMaskYes = ((attImage) which).att.getYesBit(selectionMaskYes);
                attImage tmpImg = new attImage(((attImage) which), value);
                removeImage(which);
                addImage(tmpImg);
                refresh();
                notifyDataChange(new DataChangeEvent(DataChangeEvent.DATA_CHANGED, this));
            }
            return true;
        }
    }

}