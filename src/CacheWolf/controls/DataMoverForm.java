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
package CacheWolf.controls;

import CacheWolf.Global;
import CacheWolf.MyLocale;
import ewe.ui.CellConstants;
import ewe.ui.CheckBoxGroup;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.mCheckBox;
import ewe.ui.mLabel;

public class DataMoverForm extends Form {
    private mCheckBox ticked, visible, tickedVisible;
    private CheckBoxGroup chkFormat = new CheckBoxGroup();
    private mLabel firstLine;

    public DataMoverForm(String tickedText, String visibleText, String tickedVisibleText, String firstLineText) {
	title = MyLocale.getMsg(144, "Warning");
	ticked = new mCheckBox(MyLocale.getMsg(254, "All visible"));
	ticked.setGroup(chkFormat);
	visible = new mCheckBox(MyLocale.getMsg(255, "All ticked"));
	visible.setGroup(chkFormat);
	tickedVisible = new mCheckBox(MyLocale.getMsg(256, "All visible and ticked"));
	tickedVisible.setGroup(chkFormat);
	firstLine = new mLabel("");
	firstLine.anchor = CellConstants.CENTER;
	addLast(firstLine);
	addLast(visible);
	addLast(ticked);
	addLast(tickedVisible);
	mLabel continueQuestion = new mLabel(MyLocale.getMsg(259, "Do You really want to continue?"));
	continueQuestion.anchor = CellConstants.CENTER;
	addLast(continueQuestion);
	doButtons(FormBase.YESB | FormBase.CANCELB);
	setModefromPref();
	ticked.text = tickedText;
	visible.text = visibleText;
	tickedVisible.text = tickedVisibleText;
	firstLine.text = firstLineText;
    }

    /**
     * Gets the last mode from the preferences
     */
    private void setModefromPref() {
	switch (Global.pref.processorMode) {
	case 1:
	    ticked.setState(true);
	    break;
	case 2:
	    tickedVisible.setState(true);
	    break;
	case 0:
	    visible.setState(true);
	    break;
	}
    }

    public void onEvent(Event ev) {
	if (ev.target == yes || ev.target == no) {
	    Global.pref.processorMode = getMode();
	}
	super.onEvent(ev);
    }

    public int getMode() {
	if (visible.getState()) {
	    return 0;
	} else if (ticked.getState()) {
	    return 1;
	} else if (tickedVisible.getState()) {
	    return 2;
	} else {
	    throw new IllegalStateException("No radiobutton selected");
	}
    }
}
