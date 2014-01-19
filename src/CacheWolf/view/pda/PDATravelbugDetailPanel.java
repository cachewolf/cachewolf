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
package CacheWolf.view.pda;

import CacheWolf.Preferences;
import CacheWolf.utils.MyLocale;
import CacheWolf.database.TravelbugJourney;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Font;
import ewe.fx.mImage;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.CardPanel;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Form;
import ewe.ui.Gui;
import ewe.ui.HtmlDisplay;
import ewe.ui.SipButton;
import ewe.ui.WindowConstants;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;

public class PDATravelbugDetailPanel extends Form {

    private static final int BOXWIDTH = 40;

    private static final String MENUE = "MENUE";

    private static final String BACK = "back";

    private static final String FORWARD = "Vor";

    private static final String FROM_DATE = "from_date";

    private static final String TO_DATE = "to_date";

    private TravelbugJourney travelbug;

    public TravelbugJourney getTravelbug() {
	return travelbug;
    }

    private CardPanel pnlTab;

    private mInput inpName;

    public mInput getInpName() {
	return inpName;
    }

    private mInput inpTrackingNo;

    public mInput getInpTrackingNo() {
	return inpTrackingNo;
    }

    private mLabel lblId;

    private mInput inpFromProfile;

    public mInput getInpFromProfile() {
	return inpFromProfile;
    }

    private mInput inpFromWaypoint;

    public mInput getInpFromWaypoint() {
	return inpFromWaypoint;
    }

    private mInput inpFromDate;

    public mInput getInpFromDate() {
	return inpFromDate;
    }

    private mButton btnFromDate;

    private mCheckBox chkFromLogged;

    public mCheckBox getChkFromLogged() {
	return chkFromLogged;
    }

    private mInput inpToProfile;

    public mInput getInpToProfile() {
	return inpToProfile;
    }

    private mInput inpToWaypoint;

    public mInput getInpToWaypoint() {
	return inpToWaypoint;
    }

    private mInput inpToDate;

    public mInput getInpToDate() {
	return inpToDate;
    }

    private mButton btnToDate;

    private mCheckBox chkToLogged;

    public mCheckBox getChkToLogged() {
	return chkToLogged;
    }

    private HtmlDisplay txtMission;

    private PDATravelbugJourneyScreen view;

    public PDATravelbugDetailPanel(TravelbugJourney tbJourney, PDATravelbugJourneyScreen view) {
	SipButton.sipButtonSize = new Dimension(35, 40);
	Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
	this.windowFlagsToSet = WindowConstants.FLAG_SHOW_SIP_BUTTON;
	travelbug = tbJourney;
	this.view = view;
	setTitle(tbJourney.getTb().getName());

	pnlTab = new CardPanel();
	addLast(pnlTab);

	// ------------------------------------------------
	// First Tab - Name & Tracking #
	// ------------------------------------------------
	CellPanel pnlName = new CellPanel();
	CellPanel panel = new CellPanel();
	panel.backGround = Color.White;
	panel.addNext(new mLabel(MyLocale.getMsg(6025, "Name:")), DONTSTRETCH, DONTFILL | WEST);
	inpName = new mInput(tbJourney.getTb().getName());
	inpName.backGround = Color.White;
	inpName.borderStyle = BDR_OUTLINE | BF_LEFT | BF_TOP | BF_RIGHT | BF_SQUARE;
	panel.addLast(inpName, HSTRETCH, HFILL);
	panel.addNext(new mLabel(MyLocale.getMsg(6026, "Tracking #:")), DONTSTRETCH, DONTFILL | WEST);
	inpTrackingNo = new mInput(tbJourney.getTb().getTrackingNo());
	inpTrackingNo.backGround = Color.White;
	inpTrackingNo.borderStyle = BDR_OUTLINE | BF_LEFT | BF_TOP | BF_RIGHT | BF_SQUARE;
	panel.addLast(inpTrackingNo, HSTRETCH, HFILL);
	panel.addNext(new mLabel(MyLocale.getMsg(6027, "ID/GUID:")), DONTSTRETCH, DONTFILL | WEST);
	lblId = new mLabel(tbJourney.getTb().getGuid());
	lblId.backGround = Color.White;
	lblId.borderStyle = BDR_OUTLINE | BF_LEFT | BF_TOP | BF_RIGHT | BF_SQUARE;
	panel.addLast(lblId, HSTRETCH, HFILL);
	pnlName.addLast(panel, STRETCH, FILL);
	// VON-Tab
	mLabel label = new mLabel(MyLocale.getMsg(6058, "Retrieved:"));
	Font tmpFont = new Font("Helvetica", Font.BOLD, Preferences.itself().fontSize * 2);
	label.font = tmpFont;
	panel.addLast(label, DONTSTRETCH, DONTFILL | WEST);
	panel.addNext(new mLabel(MyLocale.getMsg(6029, "Profile/Cache:")), DONTSTRETCH, DONTFILL | WEST);
	inpFromProfile = new mInput(tbJourney.getFromProfile());
	panel.addNext(inpFromProfile, HSTRETCH, HFILL);
	inpFromWaypoint = new mInput(tbJourney.getFromWaypoint());
	panel.addLast(inpFromWaypoint, HSTRETCH, HFILL);

	panel.addNext(new mLabel(MyLocale.getMsg(6059, "Date:")), DONTSTRETCH, DONTFILL | WEST);
	inpFromDate = new mInput(tbJourney.getFromDate());
	panel.addNext(inpFromDate, CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
	btnFromDate = new mButton(new mImage("calendar.png"));
	btnFromDate.action = FROM_DATE;
	panel.addLast(btnFromDate, DONTSTRETCH, HFILL | WEST);
	btnFromDate.modify(0, ControlConstants.TakesKeyFocus);

	panel.addNext(new mLabel(MyLocale.getMsg(6031, "Logged:")), DONTSTRETCH, DONTFILL | WEST);
	chkFromLogged = new mCheckBox("");
	chkFromLogged.boxWidth = BOXWIDTH;
	chkFromLogged.font = tmpFont;
	chkFromLogged.setState(tbJourney.getFromLogged());
	panel.addLast(chkFromLogged, DONTSTRETCH, DONTFILL | WEST);
	chkFromLogged.exitKeys = exitKeys;
	// To-Tab
	label = new mLabel(MyLocale.getMsg(6060, "Dropped off:"));
	label.font = tmpFont;
	panel.addLast(label, DONTSTRETCH, DONTFILL | WEST);
	panel.addNext(new mLabel(MyLocale.getMsg(6029, "Profile/Cache:")), DONTSTRETCH, DONTFILL | WEST);
	inpToProfile = new mInput(tbJourney.getToProfile());
	panel.addNext(inpToProfile, HSTRETCH, HFILL);
	inpToWaypoint = new mInput(tbJourney.getToWaypoint());
	panel.addLast(inpToWaypoint, HSTRETCH, HFILL);

	panel.addNext(new mLabel(MyLocale.getMsg(6059, "Date:")), DONTSTRETCH, DONTFILL | WEST);
	inpToDate = new mInput(tbJourney.getToDate());
	panel.addNext(inpToDate, CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
	btnToDate = new mButton(new mImage("calendar.png"));
	btnToDate.action = TO_DATE;
	panel.addLast(btnToDate, DONTSTRETCH, HFILL | WEST);
	btnToDate.modify(0, ControlConstants.TakesKeyFocus);

	panel.addNext(new mLabel(MyLocale.getMsg(6031, "Logged:")), DONTSTRETCH, DONTFILL | WEST);
	chkToLogged = new mCheckBox("");
	chkToLogged.boxWidth = BOXWIDTH;
	chkToLogged.setState(tbJourney.getToLogged());
	panel.addLast(chkToLogged, DONTSTRETCH, DONTFILL | WEST);
	chkToLogged.exitKeys = exitKeys;

	panel = new CellPanel();
	PDAMenuButton pdaListButton = new PDAMenuButton("<<<", BACK);
	pdaListButton.change(ControlConstants.Disabled, 0);
	panel.addNext(pdaListButton, HSTRETCH, HFILL);
	panel.addNext(new PDAMenuButton(MyLocale.getMsg(6052, "MENU"), MENUE));
	panel.addLast(new PDAMenuButton(">>>", FORWARD), HSTRETCH, HFILL);
	pnlName.addLast(panel, HSTRETCH, HFILL);

	pnlTab.addItem(pnlName, MyLocale.getMsg(6028, "Name"), "Name");

	// ------------------------------------------------
	// Last Panel - TB Mission
	// ------------------------------------------------
	CellPanel pnlDest = new CellPanel();
	label = new mLabel(MyLocale.getMsg(6035, "Mission:"));
	tmpFont = new Font("Helvetica", Font.BOLD, Preferences.itself().fontSize * 2);
	label.setFont(tmpFont);
	pnlDest.addLast(label, DONTSTRETCH, DONTFILL);
	txtMission = new HtmlDisplay();
	txtMission.setHtml(tbJourney.getTb().getMission());
	pnlDest.addLast(txtMission, STRETCH, FILL);
	txtMission.rows = 3;

	panel = new CellPanel();
	panel.addNext(new PDAMenuButton("<<<", BACK), HSTRETCH, HFILL);
	panel.addNext(new PDAMenuButton(MyLocale.getMsg(6052, "MENU"), MENUE));
	pdaListButton = new PDAMenuButton(">>>", "");
	pdaListButton.change(ControlConstants.Disabled, 0);
	panel.addLast(pdaListButton, HSTRETCH, HFILL);
	panel.backGround = new Color(250, 0, 0);
	pnlDest.addLast(panel, DONTSTRETCH, FILL);
	pnlTab.addItem(pnlDest, MyLocale.getMsg(6036, "Mission"), "Mission");
	SipButton.placeIn(this);
	Gui.takeFocus(null, ControlConstants.ByKeyboard);
    }

    public void onControlEvent(ControlEvent paramEvent) {
	switch (paramEvent.type) {
	case ControlEvent.PRESSED:
	    String action = paramEvent.action;
	    if (action.equals(FORWARD)) {
		pnlTab.select(pnlTab.getSelectedItem() + 1);
	    } else if (action.equals(BACK)) {
		pnlTab.select(pnlTab.getSelectedItem() - 1);
		//				pnlTab.selectNextTab(false, true);
	    } else if (action.equals(MENUE)) {
		PDATravelbugDetailMenu detailMenu = new PDATravelbugDetailMenu(this, view);
		int execute = detailMenu.execute();
		if (execute == 1) {
		    exit(0);
		}
	    } else if (action.equals(FROM_DATE) || action.equals(TO_DATE)) {
		mInput inpDate = action.equals(FROM_DATE) ? inpFromDate : inpToDate;
		PDADateTimeChooser dc = new PDADateTimeChooser();
		dc.setTitle(MyLocale.getMsg(328, "Date found"));
		dc.setLocation(0, 0);
		//				dc.setPreferredSize(240, 240);
		String foundDate = inpDate.getText();
		Time t = new Time();
		try {
		    t.parse(foundDate, "y-M-d H:m");
		} catch (IllegalArgumentException e) {
		    try {
			t.parse(foundDate, "y-M-d");
		    } catch (IllegalArgumentException e1) {
			// Can't parse date - should not happen
		    }
		}
		dc.reset(t);
		if (dc.execute() == ewe.ui.FormBase.IDOK) {
		    inpDate.setText(Convert.toString(dc.getYear()) + "-" + MyLocale.formatLong(dc.getMonth(), "00") + "-" + MyLocale.formatLong(dc.getDay(), "00") + " " + dc.getTime() + " " + MyLocale.formatLong(dc.getHour(), "00") + ":"
			    + MyLocale.formatLong(dc.getMinute(), "00"));
		}
	    }
	}
    }

    public void focusFirst(int paramInt) {
	super.focusFirst(paramInt);
	// Erst mal den Focus so setzen, dass die Tastatur nicht aufgeklappt
	// wird.
	// Wenn der TB noch nicht abgelegt wurde, dann den Focus auf den Button
	// zum ablegen, sonst immer auf die Aufnahme
	if (travelbug.getToDate().length() == 0 && travelbug.getFromDate().length() > 0) {
	    Gui.takeFocus(btnToDate, paramInt);

	} else {
	    Gui.takeFocus(btnFromDate, paramInt);
	}
    }
}
