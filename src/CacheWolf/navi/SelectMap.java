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
package CacheWolf.navi;

import CacheWolf.MainForm;
import CacheWolf.utils.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.controls.ExecutePanel;
import CacheWolf.controls.InfoBox;
import CacheWolf.controls.MyScrollBarPanel;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.ui.CellConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.ListEvent;
import ewe.ui.MenuEvent;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mLabel;
import ewe.ui.mList;

/**
 * This class displays a user interface to select maps, that should be or are already calibrated
 * MyLocale.getMsg resourceID >= 4100
 */
public class SelectMap extends Form {
    ScrollBarPanel top;
    ScrollBarPanel bot;
    private final ExecutePanel executePanel;
    String mapsPath;
    mList nonCMaps = new mList(3, 1, false);
    mList CMaps = new mList(3, 1, false);
    String dateien[];
    String selectedMap = new String();
    public boolean worldfileexists = false;

    public SelectMap() {
	mapsPath = MainForm.profile.getMapsDir();
	top = new MyScrollBarPanel(CMaps);
	bot = new MyScrollBarPanel(nonCMaps);
	this.title = MyLocale.getMsg(4101, "Maps");
	this.addLast(new mLabel(MyLocale.getMsg(4102, "Calibrated Maps")), CellConstants.STRETCH, CellConstants.FILL);
	this.addLast(top, CellConstants.STRETCH, CellConstants.FILL);
	this.addLast(new mLabel(MyLocale.getMsg(4103, "Non Calibrated Maps")), CellConstants.STRETCH, CellConstants.FILL);
	this.addLast(bot, CellConstants.STRETCH, CellConstants.FILL);
	executePanel = new ExecutePanel(this);
	InfoBox inf = new InfoBox("Info", MyLocale.getMsg(4109, "Loading maps..."));
	inf.show();
	try {
	    File files = new File(mapsPath);
	    File checkWFL;
	    String rawFileName = new String();
	    dateien = files.listMultiple("*.png,*.jpg,*.gif,*.bmp", FileBase.LIST_FILES_ONLY); // use FileBugfix if FileBase.LIST_DIRECTORIES_ONLY
	    for (int i = 0; i < dateien.length; i++) {
		rawFileName = dateien[i].substring(0, dateien[i].lastIndexOf('.'));
		checkWFL = new File(mapsPath + rawFileName + ".wfl");

		if (checkWFL.exists()) {
		    CMaps.addItem(rawFileName);
		} else {
		    nonCMaps.addItem(rawFileName);
		}
	    }
	} catch (Exception ex) {
	    Preferences.itself().log("Problem retrieveing map files", ex);
	}
	inf.close(0);
    }

    public void onEvent(Event ev) {
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == executePanel.cancelButton) {
		this.close(0);
	    }
	    if (ev.target == executePanel.applyButton) {
		this.close(0);
	    }
	}
	if (ev instanceof ListEvent && ev.type == MenuEvent.SELECTED) {
	    if (ev.target == nonCMaps) {
		selectedMap = nonCMaps.getText();
		CMaps.deleteSelection();
		worldfileexists = false;
	    }
	    if (ev.target == CMaps) {
		selectedMap = CMaps.getText();
		nonCMaps.deleteSelection();
		worldfileexists = true;
	    }
	}
	super.onEvent(ev);
    }

    public String getSelectedMap() {
	return selectedMap;
    }
}