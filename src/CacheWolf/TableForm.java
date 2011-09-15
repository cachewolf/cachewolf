    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

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

import ewe.ui.CellConstants;
import ewe.ui.Editor;

public class TableForm extends Editor {
	public TableForm(TablePanel tp) {
		if (Global.getPref().menuAtTop) {
			this.addLast(Global.mainForm.mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
			this.addLast(tp,STRETCH,FILL);
		} else {
			this.addLast(tp,STRETCH,FILL);
			this.addLast(Global.mainForm.mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
		}
		this.firstFocus = tp; // give the first fokus to the list of caches, not to the main menu
		/*
		CellPanel[] menuList = addToolbar();
		menuList[0].addLast(Global.mainForm.mMenu);
		menuList[1].addLast(tp);
		*/
	}
}
