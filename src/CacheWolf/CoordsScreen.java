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

import CacheWolf.imp.GCImporter;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.ProjectedPoint;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;
import CacheWolf.utils.STRreplace;
import ewe.sys.Vm;
import ewe.ui.CardPanel;
import ewe.ui.CellPanel;
import ewe.ui.CheckBoxGroup;
import ewe.ui.Control;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mInput;
import ewe.ui.mLabel;

/**
 * Class for entering coordinates<br>
 * Class IDs 1400 and 600 (same as calc panel and preferences screen)<br>
 */

public class CoordsScreen extends Form {

    CellPanel dp, zp;
    CardPanel tp;
    mCheckBox chkDMM, chkDMS, chkDD, chkCustom;
    CheckBoxGroup chkFormat = new CheckBoxGroup();
    mChoice localCooSystem;
    mChoice chcNS, chcEW;
    mInput inpNSDeg, inpNSm, inpNSs, inpEWDeg, inpEWm, inpEWs;
    mInput inpUTMZone, inpUTMNorthing, inpUTMEasting;
    mInput inpText;
    mButton btnCopy, btnPaste, btnParse, btnGps, btnClear, btnSearch;
    private ExecutePanel executePanel = null;
    InputScreen inpScreen;

    CWPoint coordInp = new CWPoint();
    CellPanel topLinePanel = new CellPanel();
    CellPanel mainPanel = new CellPanel();
    int exitKeys[] = { 75009 };
    int currFormat;

    boolean bNSDeg = false;
    boolean bNSm = false;
    boolean bNSs = false;
    boolean bEWDeg = false;
    boolean bEWm = false;
    boolean bEWs = false;
    boolean bUTMNorthing = false;
    boolean bUTMEasting = false;

    private boolean allowInvalid = false;

    public CoordsScreen(boolean allowInvalidCoords) {
	allowInvalid = allowInvalidCoords;
	InitCoordsScreen();
    }

    public CoordsScreen() {
	InitCoordsScreen();
    }

    private void InitCoordsScreen() {
	this.setTitle("");
	// Radiobuttons for format
	topLinePanel.addNext(chkDD = new mCheckBox("d.d°"), DONTSTRETCH, LEFT);
	topLinePanel.addNext(chkDMM = new mCheckBox("d°m.m\'"), DONTSTRETCH, LEFT);
	topLinePanel.addNext(chkDMS = new mCheckBox("d°m\'s\""), DONTSTRETCH, LEFT);
	// topLinePanel.addNext(chkUTM =new mCheckBox("UTM"),DONTSTRETCH, LEFT);
	topLinePanel.addNext(chkCustom = new mCheckBox(""), DONTSTRETCH, LEFT);

	String[] ls = TransformCoordinates.getProjectedSystemNames();
	topLinePanel.addLast(localCooSystem = new mChoice(ls, 0), DONTSTRETCH, LEFT);

	chkDD.setGroup(chkFormat);
	chkDD.exitKeys = exitKeys;
	chkDMM.setGroup(chkFormat);
	chkDMM.exitKeys = exitKeys;
	chkDMS.setGroup(chkFormat);
	chkDMS.exitKeys = exitKeys;
	chkCustom.setGroup(chkFormat);
	chkCustom.exitKeys = exitKeys;
	this.addLast(topLinePanel, DONTSTRETCH, LEFT);

	// Input for degrees
	dp = new CellPanel();
	dp.equalWidths = true;
	dp.addNext(chcNS = new mChoice(new String[] { "N", "S" }, 0));
	chcNS.setInt(0);
	dp.addNext(inpNSDeg = new mInput());
	dp.addNext(inpNSm = new mInput());
	dp.addLast(inpNSs = new mInput());

	dp.addNext(chcEW = new mChoice(new String[] { "E", "W" }, 0));
	chcEW.setInt(0);
	dp.addNext(inpEWDeg = new mInput());
	dp.addNext(inpEWm = new mInput());
	dp.addLast(inpEWs = new mInput());
	tp = new CardPanel();
	tp.addItem(dp, "dp", null);

	zp = new CellPanel();
	zp.equalWidths = true;
	zp.addNext(new mLabel(MyLocale.getMsg(1400, "Zone")));
	zp.addNext(new mLabel(MyLocale.getMsg(1402, "Easting")));
	zp.addLast(new mLabel(MyLocale.getMsg(1401, "Northing")));

	zp.addNext(inpUTMZone = new mInput());
	zp.addNext(inpUTMEasting = new mInput());
	zp.addLast(inpUTMNorthing = new mInput());
	tp.addItem(zp, "zp", null);
	mainPanel.addLast(tp);

	mainPanel.addLast(inpText = new mInput());
	inpText.toolTip = MyLocale.getMsg(1406, "Enter coordinates in any format or GCxxxxx");

	CellPanel ep = new CellPanel();
	ep.equalWidths = true;
	ep.addNext(btnCopy = GuiImageBroker.getButton(MyLocale.getMsg(618, "Copy"), "toclipboard"));
	ep.addNext(btnClear = GuiImageBroker.getButton(MyLocale.getMsg(1413, "Clear"), "illegal"));
	if (!allowInvalid) {
	    btnClear.set(ControlConstants.Invisible, true);
	    btnClear.set(ControlConstants.Disabled, true);
	}
	ep.addLast(btnParse = GuiImageBroker.getButton(MyLocale.getMsg(619, "Parse"), "examine"));
	mainPanel.addLast(ep);

	CellPanel ip = new CellPanel();
	ip.equalWidths = true;
	ip.addNext(btnPaste = GuiImageBroker.getButton(MyLocale.getMsg(617, "Paste"), "fromclipboard"));
	ip.addNext(btnGps = GuiImageBroker.getButton("GPS", "gps"));
	ip.addLast(btnSearch = GuiImageBroker.getButton(MyLocale.getMsg(1414, "Search"), "search"));
	mainPanel.addLast(ip);

	executePanel = new ExecutePanel(mainPanel);
	chcNS.exitKeys = exitKeys;
	chcEW.exitKeys = exitKeys;
	// add Panels
	this.addLast(mainPanel, DONTSTRETCH, LEFT);
	chcNS.takeFocus(ControlConstants.ByKeyboard);

    }

    public void activateFields(int format) {
	// inpEWDeg.wantReturn=false; inpEWm.wantReturn=false; inpEWs.wantReturn=false; inpUTMNorthing.wantReturn=false;
	tp.select(dp);
	switch (format) {
	case TransformCoordinates.DD:
	    disable(inpNSm);
	    disable(inpNSs);
	    disable(inpEWm);
	    disable(inpEWs);
	    break;
	case TransformCoordinates.DMM:
	    enable(inpNSm);
	    disable(inpNSs);
	    enable(inpEWm);
	    disable(inpEWs);
	    break;
	case TransformCoordinates.DMS:
	    enable(inpNSm);
	    enable(inpNSs);
	    enable(inpEWm);
	    enable(inpEWs);
	    break;
	default:
	    tp.select(zp);
	    if (TransformCoordinates.localSystems[localCooSystem.getInt()].zoneSeperatly)
		enable(inpUTMZone);
	    else
		disable(inpUTMZone);
	    break;
	}

	this.stretchLastColumn = true;
	this.stretchLastRow = true;
	this.repaintNow();
    }

    private void disable(Control c) {
	c.set(ControlConstants.Invisible, true);
	c.set(ControlConstants.Disabled, true);
    }

    private void enable(Control c) {
	c.set(ControlConstants.Invisible, false);
	c.set(ControlConstants.Disabled, false);
    }

    public void readFields(CWPoint coords) {
	String NS, EW;
	if (localSystemToformatSel(currFormat) >= formatSelToLocalSystem.length) {
	    if (TransformCoordinates.getLocalSystem(currFormat).zoneSeperatly)
		coords.set(inpUTMNorthing.getText(), inpUTMEasting.getText(), inpUTMZone.getText(), currFormat);
	    else
		coords.set(inpUTMNorthing.getText(), inpUTMEasting.getText(), currFormat);
	} else {
	    NS = chcNS.getInt() == 0 ? "N" : "S";
	    EW = chcEW.getInt() == 0 ? "E" : "W";
	    coords.set(NS, inpNSDeg.getText(), inpNSm.getText(), inpNSs.getText(), EW, inpEWDeg.getText(), inpEWm.getText(), inpEWs.getText(), currFormat);
	}
	int formatsel = combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt());
	currFormat = getLocalSystem(formatsel);
	return;
    }

    public void setFields(CWPoint coords, int format) {
	int formatsel = localSystemToformatSel(format);
	if (formatsel >= formatSelToLocalSystem.length) { // projected point = neither dd, dd° mm.mm nor dd° mm' ss.s"
	    if (coords.isValid()) {
		localCooSystem.setInt(formatsel - formatSelToLocalSystem.length);
		ProjectedPoint pp = TransformCoordinates.wgs84ToLocalsystem(coords, format);
		inpText.setText(pp.toHumanReadableString());
		inpUTMNorthing.setText(Common.DoubleToString(pp.getNorthing(), 0, 0));
		inpUTMEasting.setText(Common.DoubleToString(pp.getEasting(), 0, 0));
		if (TransformCoordinates.getLocalSystem(format).zoneSeperatly)
		    inpUTMZone.setText(pp.getZoneString());
		else
		    inpUTMZone.setText("");
	    } else {
		inpUTMNorthing.setText("0");
		inpUTMEasting.setText("0");
	    }
	} else {
	    chcNS.setInt(coords.getNSLetter().equals("N") ? 0 : 1);
	    chcEW.setInt(coords.getEWLetter().equals("E") ? 0 : 1);

	    inpNSDeg.setText(STRreplace.replace(coords.getLatDeg(format), "-", ""));
	    inpNSm.setText(coords.getLatMin(format));
	    inpNSs.setText(coords.getLatSec(format));

	    inpEWDeg.setText(STRreplace.replace(coords.getLonDeg(format), "-", ""));
	    inpEWm.setText(coords.getLonMin(format));
	    inpEWs.setText(coords.getLonSec(format));
	}
	chkFormat.selectIndex(java.lang.Math.min(localSystemToformatSel(format), formatSelToLocalSystem.length));
	inpText.setText(coords.toString(format));
	currFormat = format;
	activateFields(format);
    }

    public CWPoint getCoords() {
	return coordInp;
    }

    public void onEvent(Event ev) {

	// Ensure that the Enter key moves to the appropriate field
	// for Checkboxes and Choice controls this is done via the exitKeys
	// For input fields we use the wantReturn field

	if (ev instanceof ControlEvent && ev.type == ControlEvent.EXITED) {
	    if (((ControlEvent) ev).target == chkDD || ((ControlEvent) ev).target == chkDMM || ((ControlEvent) ev).target == chkDMS)
		Gui.takeFocus(chcNS, ControlConstants.ByKeyboard);
	    if (((ControlEvent) ev).target == chkCustom)
		Gui.takeFocus(inpUTMEasting, ControlConstants.ByKeyboard);
	    if (((ControlEvent) ev).target == chcNS)
		Gui.takeFocus(inpNSDeg, ControlConstants.ByKeyboard);
	    if (((ControlEvent) ev).target == chcEW)
		Gui.takeFocus(inpEWDeg, ControlConstants.ByKeyboard);
	}
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (((ControlEvent) ev).target == inpEWDeg || ((ControlEvent) ev).target == inpEWm || ((ControlEvent) ev).target == inpEWs || ((ControlEvent) ev).target == inpUTMNorthing)
		Gui.takeFocus(executePanel.applyButton, ControlConstants.ByKeyboard);

	    if (ev.target == chkFormat || ev.target == localCooSystem) {
		if (ev.target == localCooSystem)
		    chkFormat.selectIndex(3);
		readFields(coordInp);
		setFields(coordInp, currFormat);
		this.repaintNow();
	    }

	    if (ev.target == executePanel.cancelButton) {
		this.close(IDCANCEL);
	    }

	    if (ev.target == executePanel.applyButton) {
		currFormat = getLocalSystem(combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt()));
		readFields(coordInp);
		if (coordInp.isValid())
		    this.close(IDOK);
		else {
		    if (allowInvalid) {
			if ((new InfoBox(MyLocale.getMsg(144, "Warnung"), MyLocale.getMsg(1412, "Coordinates invalid. Apply anyway?"))).wait(FormBase.DEFOKB | FormBase.NOB) == FormBase.IDOK) {
			    this.close(IDOK);
			}
		    } else {
			new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1411, "Please enter valid coordinates")).wait(FormBase.OKB);
		    }
		}
	    }

	    if (ev.target == btnPaste) {
		inpText.setText(Vm.getClipboardText(""));
	    }

	    if (ev.target == btnCopy) {
		readFields(coordInp);
		Vm.setClipboardText(coordInp.toString(currFormat));
	    }

	    if (ev.target == btnParse) {
		// try to parse coords
		CWPoint coord;
		String inp = inpText.getText().trim().toUpperCase();
		if (inp.startsWith("GC")) {
		    GCImporter spider = new GCImporter();
		    coord = new CWPoint(spider.getCacheCoordinates(inp));
		    Global.pref.setOldGCLanguage();
		} else {
		    coord = new CWPoint(inp);
		}
		if (!coord.isValid()) {
		    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(4111, "Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM")).wait(FormBase.OKB);
		} else {
		    currFormat = getLocalSystem(combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt()));
		    setFields(coord, currFormat);
		    this.repaintNow();
		}
	    }

	    if (ev.target == btnGps) {
		Navigate nav = Global.mainTab.navigate;
		if (nav.gpsPos.isValid()) {
		    CWPoint coord = nav.gpsPos;
		    currFormat = getLocalSystem(combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt()));
		    setFields(coord, currFormat);
		}
	    }

	    if (ev.target == btnClear) {
		CWPoint coord = new CWPoint(91, 361);
		currFormat = getLocalSystem(combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt()));
		setFields(coord, currFormat);
	    }

	    if (ev.target == btnSearch) {
		GeoCodeGui s = new GeoCodeGui();
		int ok = s.execute();
		if (ok == FormBase.IDOK) {
		    currFormat = getLocalSystem(combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt()));
		    setFields(s.coordInp, currFormat);
		}
	    }
	}
	super.onEvent(ev);
    }

    private static final int[] formatSelToLocalSystem = { TransformCoordinates.DD, TransformCoordinates.DMM, TransformCoordinates.DMS, };

    public int localSystemToformatSel(int cwpointformat) {
	for (int i = 0; i < formatSelToLocalSystem.length; i++)
	    if (formatSelToLocalSystem[i] == cwpointformat)
		return i;
	for (int i = 0; i < TransformCoordinates.localSystems.length; i++)
	    if (TransformCoordinates.localSystems[i].code == cwpointformat)
		return i + formatSelToLocalSystem.length;

	throw new IllegalArgumentException("CoordScreen.CWPointformatToformatSel: cwpointformat " + cwpointformat + "not supported");
    }

    public static final int getLocalSystem(int formatsel) { // be carefull: this method is also used by CalcPanel
	if (formatsel < formatSelToLocalSystem.length)
	    return formatSelToLocalSystem[formatsel];
	return TransformCoordinates.localSystems[formatsel - formatSelToLocalSystem.length].code;
    }

    public static final int combineToFormatSel(int radiobuttonindex, int choiceindex) {
	int ret = radiobuttonindex;
	if (ret == formatSelToLocalSystem.length)
	    ret += choiceindex;
	return ret;
    }

}
