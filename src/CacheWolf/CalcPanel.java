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
import CacheWolf.navi.TransformCoordinates;
import ewe.fx.Dimension;
import ewe.fx.FontMetrics;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.CheckBoxGroup;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.FormBase;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.formatted.TextDisplay;

/**
 *	Class to create the panel to do calculation with waypoints<br>
 *	Also allows for creation of a custom waypoint.<br>
 *	Class ID 1400
 */

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

public final class CalcPanel extends CellPanel {

	mCheckBox chkDMM, chkDMS, chkDD, chkCustom;
	CheckBoxGroup chkFormat = new CheckBoxGroup();
	mChoice localCooSystem;
	mChoice chcDistUnit;
	mInput inpBearing, inpDistance, inpText;
	TextDisplay txtOutput;
	mButton btnCalc, btnClear, btnSave, btnGoto, btnParse;
	BearingDistance bd = new BearingDistance();
	CWPoint coordInp = new CWPoint();
	CWPoint coordOut = new CWPoint();
	// Needed for creation of new waypoint
	CacheDB cacheDB;
	MainTab mainT;
	// different panels to avoid spanning
	CellPanel TopP = new CellPanel();
	CellPanel BottomP = new CellPanel();

	String lastWaypoint = "";
	boolean bBearing, bDistance;

	int currFormat;
	mButton btnChangeLatLon;

	public CalcPanel() {
		mainT = Global.mainTab;
		cacheDB = Global.profile.cacheDB;

		TopP.addNext(chkDD = new mCheckBox("d.d°"), CellConstants.DONTSTRETCH, CellConstants.WEST);
		TopP.addNext(chkDMM = new mCheckBox("d°m.m\'"), CellConstants.DONTSTRETCH, CellConstants.WEST);
		TopP.addNext(chkDMS = new mCheckBox("d°m\'s\""), CellConstants.DONTSTRETCH, CellConstants.WEST);
		TopP.addNext(chkCustom = new mCheckBox(""), CellConstants.DONTSTRETCH, CellConstants.WEST);

		chkDD.setGroup(chkFormat);
		chkDMM.setGroup(chkFormat);
		chkDMS.setGroup(chkFormat);
		chkCustom.setGroup(chkFormat);
		chkFormat.setInt(1);
		currFormat = 1;
		String[] ls = TransformCoordinates.getProjectedSystemNames();
		TopP.addLast(localCooSystem = new mChoice(ls, 0), CellConstants.DONTSTRETCH, CellConstants.WEST);

		btnChangeLatLon = new mButton();
		TopP.addLast(btnChangeLatLon, CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));
		// inpBearing and direction, unit for inpDistance
		BottomP.addNext(new mLabel(MyLocale.getMsg(1403, "Bearing")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		BottomP.addLast(new mLabel(MyLocale.getMsg(1404, "Distance")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		BottomP.addNext(inpBearing = new mInput(), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		inpBearing.setText("0");
		BottomP.addNext(inpDistance = new mInput(), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		inpDistance.setText("0");
		// Check for narrow screen and reduce width of fields to avoid horizontal scroll panel
		if (MyLocale.getScreenWidth() <= 240) {
			FontMetrics fm = getFontMetrics(inpBearing.getFont());
			inpBearing.setPreferredSize(fm.getTextWidth("99999999"), fm.getHeight() * 4 / 3);
			inpDistance.setPreferredSize(fm.getTextWidth("99999999"), fm.getHeight() * 4 / 3);
		}
		BottomP.addLast(chcDistUnit = new mChoice(new String[] { "m", "km", MyLocale.getMsg(1407, "steps"), MyLocale.getMsg(1408, "feet"), MyLocale.getMsg(1409, "yards"), MyLocale.getMsg(1410, "miles") }, 0), CellConstants.DONTSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST)).setTag(CellConstants.INSETS, new ewe.fx.Insets(0, 2, 0, 0));
		if (Global.pref.metricSystem == Metrics.METRIC) {
			chcDistUnit.setInt(0); // Meter
		}
		else {
			chcDistUnit.setInt(3); // Feet
		}

		// Buttons for calc and save
		BottomP.addNext(btnCalc = new mButton("Calc"), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		BottomP.addNext(btnClear = new mButton("Clear"), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		BottomP.addNext(btnGoto = new mButton("Goto"), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		BottomP.addLast(btnSave = new mButton(MyLocale.getMsg(311, "Create Waypoint")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));

		// Output
		txtOutput = new TextDisplay(3, 1); // Need to limit size for small screens
		ScrollBarPanel sbp = new MyScrollBarPanel(txtOutput);
		BottomP.addLast(sbp.setTag(CellConstants.SPAN, new Dimension(4, 1)), CellConstants.STRETCH, (CellConstants.FILL | CellConstants.WEST));

		// add Panels
		this.addLast(TopP, CellConstants.HSTRETCH, CellConstants.WEST);// .setTag(SPAN,new Dimension(4,1));
		this.addLast(BottomP, CellConstants.VSTRETCH, CellConstants.VFILL | CellConstants.WEST); // .setTag(SPAN,new Dimension(4,1));

	}

	private final int getLocalCooSystem() {
		return CoordsScreen.getLocalSystem(currFormat);
	}

	public final void readFields(CWPoint coords, BearingDistance degKm) {
		// coords.set(btnChangeLatLon.getText());
		currFormat = CoordsScreen.combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt());
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

	public void setFields() {
		btnChangeLatLon.setText(coordInp.toString(getLocalCooSystem()));
		// chkFormat.selectIndex(currFormat);
	}

	public void onEvent(Event ev) {

		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == chkFormat || ((ev.type == ControlEvent.PRESSED) && (ev.target == localCooSystem))) {
				if (ev.target == localCooSystem)
					chkFormat.selectIndex(3);
				readFields(coordInp, bd);
				setFields();
				this.repaintNow();
			}

			if (ev.target == btnCalc) {
				readFields(coordInp, bd);
				coordOut = coordInp.project(bd.degrees, bd.distance);
				txtOutput.appendText(coordOut.toString(getLocalCooSystem()) + "\n", true);
			}
			if (ev.target == btnClear) {
				txtOutput.setText("");
			}
			if (ev.target == btnSave) {
				CacheHolder ch = new CacheHolder();
				readFields(coordInp, bd);
				coordOut = coordInp.project(bd.degrees, bd.distance);
				ch.setPos(coordOut);
				ch.setType(CacheType.CW_TYPE_STAGE); // TODO unfertig
				mainT.newWaypoint(ch);
			}

			if (ev.target == btnGoto) {
				readFields(coordInp, bd);
				coordOut = coordInp.project(bd.degrees, bd.distance);
				mainT.gotoP.setDestinationAndSwitch(coordOut);
			}

			if (ev.target == btnChangeLatLon) {
				if (Vm.isMobile()) {
					readFields(coordInp, bd);
					InputScreen InScr = new InputScreen(getLocalCooSystem());
					if (coordInp.isValid())
						InScr.setCoords(coordInp);
					else
						InScr.setCoords(new CWPoint(0, 0));
					if (InScr.execute(null, CellConstants.TOP) == FormBase.IDOK) {
						btnChangeLatLon.setText(InScr.getCoords().toString(getLocalCooSystem()));
						coordInp.set(InScr.getCoords());
					}
				}
				else {
					CoordsScreen cs = new CoordsScreen();
					readFields(coordInp, bd);
					cs.setFields(coordInp, getLocalCooSystem());
					if (cs.execute() == FormBase.IDOK) {
						btnChangeLatLon.setText(cs.getCoords().toString(getLocalCooSystem()));
						coordInp.set(cs.getCoords());
					}
				}

			}
			super.onEvent(ev);
		}
	}
}