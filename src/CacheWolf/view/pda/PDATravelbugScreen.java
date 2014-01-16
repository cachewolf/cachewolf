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

import CacheWolf.MyLocale;
import CacheWolf.database.Travelbug;
import CacheWolf.database.TravelbugList;
import CacheWolf.model.DefaultListModel;
import CacheWolf.view.ITravelbugScreen;
import ewe.ui.ControlEvent;

public class PDATravelbugScreen extends PDAList implements ITravelbugScreen {

    /** The index into the list of travelbugs indicating the selected bug */
    public int selectedItem = -1;

    /**
     * A screen to choose a travelbug from a list of bugs
     * @param tbl The list of travelbugs from which to choose
     * @param title The title of the screen
     * @param allowNew True if a travelbug not on the list can be selected
     */
    public PDATravelbugScreen(TravelbugList tbl, String title, boolean allowNew) {
	super();
	model = new DefaultListModel();
	for (int i = 0; i < tbl.size(); i++) {
	    Travelbug tb = tbl.getTB(i);
	    model.add(tb.getName());
	}
	if (allowNew) {
	    model.add(MyLocale.getMsg(6015, "*** OTHER ***"));
	}
	model.createShowSet();
	setupTBButtons();
    }

    protected PDAListButton createListButton(int i) {
	return new PDAListButton("", LINE + i);
    }

    public void onControlEvent(ControlEvent ev) {
	if (ev instanceof ControlEvent) {
	    switch (ev.type) {
	    case ControlEvent.PRESSED:
		if (ev.action.equals(NEXT_PAGE) || (ev.action.equals(PREV_PAGE))) {
		    super.onControlEvent(ev);
		} else if (ev.action.startsWith(LINE)) {
		    selectedItem = ev.action.charAt(LINE.length()) - '0';
		    exit(0);
		} else if (ev.action.equals(MENUE)) {
		    setupTBButtons();
		}
		break;
	    default:
		super.onControlEvent(ev);
	    }
	}
    }

    public int getSelectedItem() {
	return selectedItem;
    }
}
