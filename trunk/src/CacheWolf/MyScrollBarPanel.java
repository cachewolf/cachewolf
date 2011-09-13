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

import ewe.sys.Vm;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollClient;
import ewe.ui.ScrollablePanel;

/**
 * Always use this class instead of ewe.ui.ScrollBarPanel
 * as it will change it's size automatically with
 * the font size, which is entered in the preferences dialog
 * 
 * @author pfeffer
 */

public class MyScrollBarPanel extends ScrollBarPanel {
	public MyScrollBarPanel(ScrollClient client,int options) {
		super(client, options);
		int s = java.lang.Math.round(Global.getPref().fontSize / 11f * 15f); // standard fontsize = 1, standard bottum size = 15
		setScrollBarSize(s, s, s);
	}
	public MyScrollBarPanel(ScrollClient client)
	{
		this(client,0);
		if(Vm.isMobile()){
//			hbar.setPreferredSizes(50,50,50);
		}
		
	}
	public ScrollablePanel getScrollablePanel()
	{
		return new MyScrollBarPanel(getScrollClient());
	}
}
