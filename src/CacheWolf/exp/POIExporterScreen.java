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
package CacheWolf.exp;

import CacheWolf.Preferences;
import CacheWolf.controls.ExecutePanel;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.SafeXML;
import ewe.ui.CellPanel;
import ewe.ui.CheckBoxGroup;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.mCheckBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;

public class POIExporterScreen extends Form {
    private final ExecutePanel executePanel;
    private mCheckBox chkNoPic, chkAllPic, chkOnlySpoiler;
    private CheckBoxGroup chkGroupFormat;
    private mInput inpName, inpAddiName; // <name>
    private mInput inpCmt, inpAddiCmt; // <cmt>
    private mInput inpDesc, inpAddiDesc; // <desc>
    private mInput inpAnzLogs;
    private mCheckBox chkAutoSplitByType;
    private String expName;
    private int anzLogs;

    // todo List or possible extensions:
    // deletion of old gpx and bmp
    // auto Split Profiles to UnterVerzeichnisse(POI Databases)
    // auto Calling POILoader
    // Variable for LastFound , last5logs (in TemplateTable.java)
    // Ask for ShortWaypoint length, Description Length, ...

    public POIExporterScreen(String expName) {
	super();
	this.title = MyLocale.getMsg(2200, "POI Exporter");
	this.expName = expName;
	this.setPreferredSize((int) (0.75 * Preferences.itself().getScreenWidth()), (int) (0.5 * Preferences.itself().getScreenHeight()));

	chkAutoSplitByType = new mCheckBox(MyLocale.getMsg(2210, "Split by cachetype?"));
	chkAutoSplitByType.setState(Boolean.valueOf(Preferences.itself().getExportPref(expName + "-split")).booleanValue());
	this.addLast(chkAutoSplitByType);
	// checkboxgroup for all pictures or Spoiler only
	chkGroupFormat = new CheckBoxGroup();
	chkNoPic = new mCheckBox(MyLocale.getMsg(2203, "No pictures"));
	chkAllPic = new mCheckBox(MyLocale.getMsg(2201, "All pictures"));
	chkOnlySpoiler = new mCheckBox(MyLocale.getMsg(2202, "Only spoiler"));
	chkNoPic.setGroup(chkGroupFormat);
	chkAllPic.setGroup(chkGroupFormat);
	chkOnlySpoiler.setGroup(chkGroupFormat);
	String picIndex = Preferences.itself().getExportPref(expName + "-picIndex");
	if (picIndex.length() == 0)
	    picIndex = "1";
	chkGroupFormat.selectIndex(Integer.parseInt(picIndex));

	String sAnzLogs = Preferences.itself().getExportPref(expName + "-anzLogs");
	if (sAnzLogs.length() == 0)
	    sAnzLogs = "-1";
	anzLogs = Integer.parseInt(sAnzLogs);
	if (anzLogs == -1)
	    sAnzLogs = "";

	this.addNext(chkNoPic);
	this.addNext(chkAllPic);
	this.addLast(chkOnlySpoiler);

	CellPanel mainCaches = new CellPanel();
	mainCaches.setText(MyLocale.getMsg(2208, "Definition for Caches"));

	mainCaches.addNext(new mLabel(MyLocale.getMsg(2204, "Add to <name> - tag:")), LEFT, LEFT);
	inpName = new mInput();
	inpName.setText(Preferences.itself().getExportPref(expName + "-name"));
	if (inpName.getText().length() == 0)
	    inpName.setText("%IFFOUND,%IFOWN,%IFNOTAVAILABLE,%IFARCHIVED,%IFPM,%SSHORTWAYPOINT, ,%PIC#, ,%IFSOLVED,%SHORTTYPE, ,%SHORTNAME,(,%DIFFICULTY,/,%TERRAIN,/,%SHORTSIZE,)");
	mainCaches.addLast(inpName);

	mainCaches.addNext(new mLabel(MyLocale.getMsg(2205, "Add to <cmt> - tag:")), LEFT, LEFT);
	inpCmt = new mInput();
	inpCmt.setText(Preferences.itself().getExportPref(expName + "-cmt"));
	if (inpCmt.getText().length() == 0)
	    inpCmt.setText("%DECRYPTEDHINTS, ,%NOTES");
	mainCaches.addLast(inpCmt);

	mainCaches.addNext(new mLabel(MyLocale.getMsg(2206, "Add to <desc> - tag:")), LEFT, LEFT);
	inpDesc = new mInput();
	inpDesc.setText(Preferences.itself().getExportPref(expName + "-desc"));
	mainCaches.addLast(inpDesc);

	this.addLast(mainCaches);

	// Addis
	CellPanel addis = new CellPanel();
	addis.setText(MyLocale.getMsg(2209, "Definition for additional waypoints"));

	addis.addNext(new mLabel(MyLocale.getMsg(2204, "Add to <name> - tag:")), LEFT, LEFT);
	inpAddiName = new mInput();
	inpAddiName.setText(Preferences.itself().getExportPref(expName + "-AddiName"));
	if (inpAddiName.getText().length() == 0)
	    inpAddiName.setText("%SSHORTWAYPOINT, ,%PREFIX, ,%SHORTTYPE, ,%SHORTNAME");
	addis.addLast(inpAddiName);

	addis.addNext(new mLabel(MyLocale.getMsg(2205, "Add to <cmt> - tag:")), LEFT, LEFT);
	inpAddiCmt = new mInput();
	inpAddiCmt.setText(Preferences.itself().getExportPref(expName + "-AddiCmt"));
	if (inpAddiCmt.getText().length() == 0)
	    inpAddiCmt.setText("%DESCRIPTION, ,%DECRYPTEDHINTS");
	addis.addLast(inpAddiCmt);

	addis.addNext(new mLabel(MyLocale.getMsg(2206, "Add to <desc> - tag:")), LEFT, LEFT);
	inpAddiDesc = new mInput();
	inpAddiDesc.setText(Preferences.itself().getExportPref(expName + "-AddiDesc"));
	addis.addLast(inpAddiDesc);

	this.addLast(addis);

	this.addNext(new mLabel(MyLocale.getMsg(2207, "No. of logs:")), LEFT, LEFT);
	inpAnzLogs = new mInput();
	inpAnzLogs.setText(sAnzLogs);
	this.addLast(inpAnzLogs, HSTRETCH, HFILL);

	executePanel = new ExecutePanel(this);
    }

    public boolean getAutoSplitByType() {
	return chkAutoSplitByType.getState();
    }

    public boolean onlySpoiler() {
	if (chkGroupFormat.getSelectedIndex() == 2)
	    return true;
	else
	    return false;
    }

    public boolean noPictures() {
	if (chkGroupFormat.getSelectedIndex() == 0)
	    return true;
	else
	    return false;
    }

    public String getNameTagDefinitions() {
	return this.inpName.getText();
    }

    public String getCmtTagDefinitions() {
	return this.inpCmt.getText();
    }

    public String getDescTagDefinitions() {
	return this.inpDesc.getText();
    }

    public String getAddiNameTagDefinitions() {
	return this.inpAddiName.getText();
    }

    public String getAddiCmtTagDefinitions() {
	return this.inpAddiCmt.getText();
    }

    public String getAddiDescTagDefinitions() {
	return this.inpAddiDesc.getText();
    }

    public int getAnzLogs() {
	String sAnzLogs = this.inpAnzLogs.getText();
	if (sAnzLogs.length() == 0)
	    sAnzLogs = "-1";
	try {
	    anzLogs = Integer.parseInt(sAnzLogs);
	} catch (Exception e) {
	    anzLogs = 0;
	}
	return this.anzLogs;
    }

    public void onEvent(Event ev) {
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == executePanel.cancelButton) {
		this.close(FormBase.IDCANCEL);
	    }
	    if (ev.target == executePanel.applyButton) {
		Preferences.itself().setExportPref(expName + "-split", SafeXML.strxmlencode(chkAutoSplitByType.getState()));
		Preferences.itself().setExportPref(expName + "-picIndex", "" + chkGroupFormat.getSelectedIndex());
		Preferences.itself().setExportPref(expName + "-name", this.inpName.getText());
		Preferences.itself().setExportPref(expName + "-cmt", this.inpCmt.getText());
		Preferences.itself().setExportPref(expName + "-desc", this.inpDesc.getText());
		Preferences.itself().setExportPref(expName + "-AddiName", this.inpAddiName.getText());
		Preferences.itself().setExportPref(expName + "-AddiCmt", this.inpAddiCmt.getText());
		Preferences.itself().setExportPref(expName + "-AddiDesc", this.inpAddiDesc.getText());
		Preferences.itself().setExportPref(expName + "-anzLogs", this.inpAnzLogs.getText());
		this.close(FormBase.IDOK);
	    }
	}
	super.onEvent(ev);
    }

}
