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

import CacheWolf.controls.ExecutePanel;
import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.database.TravelbugList;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.SafeXML;
import CacheWolf.view.ITravelbugScreen;
import ewe.ui.*;

/**
 * Choose a travelbug to pick up or drop
 *
 * @author salzkammergut
 */
public class TravelbugScreen extends Form implements ITravelbugScreen {
    private final ExecutePanel executePanel;
    /**
     * The index into the list of travelbugs indicating the selected bug
     */
    public int selectedItem = -1;
    private myList disp;

    /**
     * A screen to choose a travelbug from a list of bugs
     *
     * @param tbl      The list of travelbugs from which to choose
     * @param title    The title of the screen
     * @param allowNew True if a travelbug not on the list can be selected
     */
    public TravelbugScreen(TravelbugList tbl, String title, boolean allowNew) {
        this.setTitle(title);
        this.setPreferredSize(240, -1);
        disp = new myList(tbl, allowNew);
        ScrollBarPanel sbp = new MyScrollBarPanel(disp, ScrollablePanel.NeverShowHorizontalScrollers);
        this.addLast(sbp);
        executePanel = new ExecutePanel(this);
        executePanel.disable(FormBase.YESB);
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void onEvent(Event ev) {
        if (ev instanceof ListEvent && ev.type == MenuEvent.SELECTED) {
            executePanel.enable(FormBase.YESB);
        }
        if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
            if (ev.target == executePanel.cancelButton) {
                this.close(0);
            }
            if (ev.target == executePanel.applyButton) {
                this.close(0);
                selectedItem = disp.getSelectedIndex(0);
            }
        }
    }

    private class myList extends SimpleList {
        private TravelbugList tbl;
        private boolean allowNew;
        private int size;

        myList(TravelbugList tbl, boolean allowNew) {
            this.tbl = tbl;
            this.size = tbl.size();
            this.allowNew = allowNew;
        }

        public Object getObjectAt(int idx) {
            return getDisplayItem(idx);
        }

        public int getItemCount() {
            return tbl.size() + (allowNew ? 1 : 0);
        }

        public String getDisplayItem(int idx) {
            if (idx == size)
                return MyLocale.getMsg(6015, "*** OTHER ***");
            else if (tbl.getTB(idx).getName().indexOf("&#") < 0)
                return tbl.getTB(idx).getName();
            else
                // If the name contains HTML entities, we need to convert it back
                return SafeXML.html2iso8859s1(tbl.getTB(idx).getName());
        }
    }
}
