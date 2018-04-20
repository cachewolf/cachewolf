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
import ewe.ui.ControlEvent;
import ewe.ui.Form;

public abstract class PDAMenu extends Form {

    protected static final String CANCEL = "__Cancel_Exit__";

    public PDAMenu() {
        setPreferredSize(Preferences.itself().getScreenWidth(), Preferences.itself().getScreenHeight());
    }

    public abstract void actionPerformed(String actionCommand);

    public void onControlEvent(ControlEvent paramEvent) {
        switch (paramEvent.type) {
            case ControlEvent.PRESSED:
                if (paramEvent.action.equals(CANCEL)) {
                    exit(0);

                } else {
                    actionPerformed(paramEvent.action);
                }
        }
        super.onControlEvent(paramEvent);
    }

    protected void buildMenu() {
        PDAMenuButton button = new PDAMenuButton(MyLocale.getMsg(6057, "Back"), CANCEL);
        addLast(button);
    }

    protected void addMenuItem(String item, String actionCommand) {
        PDAMenuButton button = new PDAMenuButton(item, actionCommand);
        addLast(button);
    }

}
