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

import CacheWolf.controls.GuiImageBroker;
import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheType;
import CacheWolf.navi.Metrics;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;
import CacheWolf.utils.MyLocale;
import ewe.fx.FontMetrics;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.FormBase;
import ewe.ui.Menu;
import ewe.ui.MenuEvent;
import ewe.ui.MenuItem;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mButton;
import ewe.ui.mChoice;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.formatted.TextDisplay;

/**
 * Class to create the panel to do calculation with waypoints<br>
 * Also allows for creation of a custom waypoint.<br>
 * Class ID 1400
 */

public final class CalcPanel extends CellPanel {

    private mChoice chcDistUnit;
    private mInput inpBearing, inpDistance;
    private TextDisplay txtOutput;
    private mButton btnCalc, btnClear, btnSave, btnGoto;
    private BearingDistance bearingDistance = new BearingDistance();
    private CWPoint coordInp = new CWPoint();
    private CWPoint coordOut = new CWPoint();
    private MainTab mainTab;

    private String lastWaypoint = "";

    private int currFormat;
    private mButton btnChangeLatLon;
    private mButton btnChangeProjection;
    private Menu mnuContextFormt;

    public CalcPanel() {
	mainTab = MainTab.itself;

	CellPanel buttonPanel = new CellPanel();
	buttonPanel.equalWidths = true;
	buttonPanel.addNext(btnGoto = GuiImageBroker.getButton(MyLocale.getMsg(1500, "Destination"), "goto"));
	btnGoto.setToolTip(MyLocale.getMsg(326, "Set as destination and show Compass View"));
	buttonPanel.addLast(btnSave = GuiImageBroker.getButton(MyLocale.getMsg(311, "Create Waypoint"), "newwpt"));
	btnSave.setToolTip(MyLocale.getMsg(311, "Create Waypoint"));
	if (Preferences.itself().tabsAtTop) {
	    if (Preferences.itself().menuAtTab)
		this.addLast(buttonPanel, CellConstants.HSTRETCH, CellConstants.HFILL);
	} else {
	    if (!Preferences.itself().menuAtTab)
		this.addLast(buttonPanel, CellConstants.HSTRETCH, CellConstants.HFILL);
	}
	CellPanel TopP = new CellPanel();
	// Format selection for coords context menu
	MenuItem miCooformat[] = new MenuItem[TransformCoordinates.localSystems.length + 3];
	mnuContextFormt = new Menu();
	currFormat = 1; // default to d° m.m
	mnuContextFormt.addItem(miCooformat[0] = new MenuItem("d.d°"));
	miCooformat[0].modifiers &= ~MenuItem.Checked;
	mnuContextFormt.addItem(miCooformat[1] = new MenuItem("d°m.m\'"));
	miCooformat[1].modifiers |= MenuItem.Checked; // default
	mnuContextFormt.addItem(miCooformat[2] = new MenuItem("d°m\'s\""));
	miCooformat[2].modifiers &= ~MenuItem.Checked;
	mnuContextFormt.addItems(TransformCoordinates.getProjectedSystemNames());

	btnChangeLatLon = GuiImageBroker.getButton("from", "from");
	btnChangeLatLon.setMenu(mnuContextFormt);
	btnChangeLatLon.modifyAll(ControlConstants.WantHoldDown, 0);

	btnChangeProjection = GuiImageBroker.getButton("...", "projection");
	btnChangeProjection.setMenu(mnuContextFormt);
	btnChangeProjection.modifyAll(ControlConstants.WantHoldDown, 0);

	TopP.addNext(btnChangeLatLon, HSTRETCH, FILL);
	TopP.addLast(btnChangeProjection, DONTSTRETCH, DONTFILL);

	// inpBearing and direction, unit for inpDistance
	CellPanel BottomP = new CellPanel();
	BottomP.addNext(new mLabel(MyLocale.getMsg(1403, "Bearing")), DONTSTRETCH, (DONTFILL | LEFT));
	BottomP.addLast(new mLabel(MyLocale.getMsg(1404, "Distance")), DONTSTRETCH, (DONTFILL | LEFT));

	BottomP.addNext(inpBearing = new mInput(), DONTSTRETCH, (DONTFILL | LEFT));
	inpBearing.setText("0");
	BottomP.addNext(inpDistance = new mInput(), DONTSTRETCH, (DONTFILL | LEFT));
	inpDistance.setText("0");
	// Check for narrow screen and reduce width of fields to avoid horizontal scroll panel
	if (MyLocale.getScreenWidth() <= 240) {
	    FontMetrics fm = getFontMetrics(inpBearing.getFont());
	    inpBearing.setPreferredSize(fm.getTextWidth("99999999"), fm.getHeight() * 4 / 3);
	    inpDistance.setPreferredSize(fm.getTextWidth("99999999"), fm.getHeight() * 4 / 3);
	}
	BottomP.addNext(chcDistUnit = new mChoice(new String[] { "m", "km", MyLocale.getMsg(1407, "steps"), MyLocale.getMsg(1408, "feet"), MyLocale.getMsg(1409, "yards"), MyLocale.getMsg(1410, "miles") }, 0), DONTSTRETCH, (HFILL | LEFT)).setTag(
		INSETS, new ewe.fx.Insets(0, 2, 0, 0));
	BottomP.addLast(btnCalc = GuiImageBroker.getButton(MyLocale.getMsg(1735, "Solve!"), "calc"), DONTSTRETCH, HFILL);

	if (Preferences.itself().metricSystem == Metrics.METRIC) {
	    chcDistUnit.setInt(0); // Meter
	} else {
	    chcDistUnit.setInt(3); // Feet
	}

	// Output
	txtOutput = new TextDisplay(3, 1); // Need to limit size for small screens
	ScrollBarPanel sbp = new MyScrollBarPanel(txtOutput);
	BottomP.addLast(sbp, STRETCH, (FILL | LEFT));
	BottomP.addLast(btnClear = GuiImageBroker.getButton("Clear", "clear"), DONTSTRETCH, (DONTFILL | LEFT));

	// add Panels
	this.addLast(TopP, HSTRETCH, HFILL);// .setTag(SPAN,new Dimension(4,1));
	this.addLast(BottomP); //, VSTRETCH, VFILL | LEFT // .setTag(SPAN,new Dimension(4,1));
	if (Preferences.itself().tabsAtTop) {
	    if (!Preferences.itself().menuAtTab)
		this.addLast(buttonPanel, CellConstants.HSTRETCH, CellConstants.HFILL);
	} else {
	    if (Preferences.itself().menuAtTab)
		this.addLast(buttonPanel, CellConstants.HSTRETCH, CellConstants.HFILL);
	}
    }

    private final int getLocalCooSystem() {
	return CoordsInput.getLocalSystem(currFormat);
    }

    public final void readFields(CWPoint coords, BearingDistance degKm) {
	degKm.degrees = Common.parseDouble(inpBearing.getText());

	double rawDistance = Common.parseDouble(inpDistance.getText());
	switch (chcDistUnit.getInt()) {
	case 0:
	    // meter
	    degKm.distance = rawDistance / 1000.0;
	    break;
	case 1:
	    // kilometer
	    degKm.distance = rawDistance;
	    break;
	case 2:
	    // steps
	    degKm.distance = rawDistance * 0.00063;
	    break;
	case 3:
	    // feet
	    degKm.distance = rawDistance * 0.0003048;
	    break;
	case 4:
	    // yards
	    degKm.distance = rawDistance * 0.0009144;
	    break;
	case 5:
	    // miles
	    degKm.distance = rawDistance * 1.609344;
	    break;
	default:
	    // meter
	    degKm.distance = rawDistance / 1000.0;
	    break;
	}
	return;
    }

    // ch must not be null
    public void setFields(CacheHolder ch) {
	if (!ch.getWayPoint().equalsIgnoreCase(lastWaypoint)) {
	    lastWaypoint = ch.getWayPoint();
	    if (ch.getPos().isValid()) {
		inpBearing.setText("0");
		inpDistance.setText("0");
		coordInp.set(ch.getPos());
		setFields();
	    }
	}
    }

    private void setFields() {
	GuiImageBroker.setButtonText(btnChangeLatLon, coordInp.toString(getLocalCooSystem()));
	// repaint();
    }

    public void onEvent(Event ev) {
	if (ev instanceof MenuEvent) {
	    if (ev.type == MenuEvent.SELECTED) {
		if (((MenuEvent) ev).menu == mnuContextFormt) {
		    mnuContextFormt.close();
		    mnuContextFormt.getItemAt(currFormat).modifiers &= ~MenuItem.Checked;
		    currFormat = mnuContextFormt.getInt();
		    mnuContextFormt.getItemAt(currFormat).modifiers |= MenuItem.Checked;
		    setFields();
		}
	    }
	}
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == this.btnChangeProjection) {
		Rect rm = mnuContextFormt.getRect();
		btnChangeProjection.startDropMenu(new Point(rm.x, rm.y));
	    }
	    if (ev.target == btnCalc) {
		readFields(coordInp, bearingDistance);
		coordOut = coordInp.project(bearingDistance.degrees, bearingDistance.distance);
		txtOutput.appendText(coordOut.toString(getLocalCooSystem()) + "\n", true);
	    }
	    if (ev.target == btnClear) {
		txtOutput.setText("");
	    }
	    if (ev.target == btnSave) {
		CacheHolder ch = new CacheHolder();
		readFields(coordInp, bearingDistance);
		coordOut = coordInp.project(bearingDistance.degrees, bearingDistance.distance);
		ch.setPos(coordOut);
		ch.setType(CacheType.CW_TYPE_STAGE);
		mainTab.newWaypoint(ch);
	    }

	    if (ev.target == btnGoto) {
		readFields(coordInp, bearingDistance);
		coordOut = coordInp.project(bearingDistance.degrees, bearingDistance.distance);
		Navigate.itself.setDestination(coordOut);
		mainTab.select(MainTab.GOTO_CARD);
	    }

	    if (ev.target == btnChangeLatLon) {
		if (Vm.isMobile()) {
		    readFields(coordInp, bearingDistance);
		    CoordsPDAInput InScr = new CoordsPDAInput(getLocalCooSystem());
		    if (coordInp.isValid())
			InScr.setCoords(coordInp);
		    else
			InScr.setCoords(new CWPoint(0, 0));
		    if (InScr.execute(null, TOP) == FormBase.IDOK) {
			GuiImageBroker.setButtonText(btnChangeLatLon, InScr.getCoords().toString(getLocalCooSystem()));
			coordInp.set(InScr.getCoords());
			// repaint();
		    }
		} else {
		    CoordsInput cs = new CoordsInput();
		    readFields(coordInp, bearingDistance);
		    cs.setFields(coordInp, getLocalCooSystem());
		    if (cs.execute() == FormBase.IDOK) {
			GuiImageBroker.setButtonText(btnChangeLatLon, cs.getCoords().toString(getLocalCooSystem()));
			coordInp.set(cs.getCoords());
			// repaint();
		    }
		}

	    }
	    super.onEvent(ev);
	}
    }
}

/**
 * Wrapper class to pass bearing and distance
 */
class BearingDistance {
    public double degrees;
    public double distance;

    public BearingDistance() {
	this.degrees = 0;
	this.distance = 0;
    }

    public BearingDistance(double degrees, double distance) {
	this.degrees = degrees;
	this.distance = distance;
    }
}
