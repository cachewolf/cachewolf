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

import CacheWolf.controls.ExecutePanel;
import CacheWolf.utils.MyLocale;
import ewe.ui.CheckBoxGroup;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.mCheckBox;

public class POIExporterScreen extends Form {
    private final ExecutePanel executePanel;
    mCheckBox chkAllPic, chkOnlySpoiler;
    CheckBoxGroup chkGroupFormat;

    public POIExporterScreen(String title) {
	super();
	this.title = title;

	// checkboxgroup for all pictures or Spoiler only
	chkGroupFormat = new CheckBoxGroup();
	chkAllPic = new mCheckBox(MyLocale.getMsg(2201, "all Pics"));
	chkAllPic.setGroup(chkGroupFormat);
	chkOnlySpoiler = new mCheckBox(MyLocale.getMsg(2202, "only Spoiler"));
	chkOnlySpoiler.setGroup(chkGroupFormat);
	chkGroupFormat.selectIndex(1);

	this.addNext(chkAllPic);
	this.addLast(chkOnlySpoiler);

	executePanel = new ExecutePanel(this);
    }

    public boolean getOnlySpoiler() {
	if (chkGroupFormat.getSelectedIndex() == 1)
	    return true;
	else
	    return false;
    }

    public void onEvent(Event ev) {
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == executePanel.cancelButton) {
		this.close(FormBase.IDCANCEL);
	    }
	    if (ev.target == executePanel.applyButton) {
		this.close(FormBase.IDOK);
	    }
	}
	super.onEvent(ev);
    }

}
