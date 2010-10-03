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
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.ListEvent;
import ewe.ui.MenuEvent;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollablePanel;
import ewe.ui.SimpleList;
import ewe.ui.mButton;

/**
 * Choose a travelbug to pick up or drop
 * @author salzkammergut
 */ 
public class TravelbugScreen extends Form {
	private myList disp;
	private mButton btCancel,btAccept;
	/** The index into the list of travelbugs indicating the selected bug */
	public int selectedItem=-1;
	
	/**
	 * A screen to choose a travelbug from a list of bugs
	 * @param tbl The list of travelbugs from which to choose
	 * @param title The title of the screen
	 * @param allowNew True if a travelbug not on the list can be selected
	 */
	TravelbugScreen(TravelbugList tbl, String title,boolean allowNew) {
		this.setTitle(title);
		this.setPreferredSize(240, -1);
		disp=new myList(tbl,allowNew);
		ScrollBarPanel sbp = new MyScrollBarPanel(disp, ScrollablePanel.NeverShowHorizontalScrollers);
		this.addLast(sbp);
		this.addNext(btCancel = new mButton(MyLocale.getMsg(614,"Cancel")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		this.addLast(btAccept = new mButton("OK"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		btAccept.modify(Disabled,0);
	}

	public void onEvent(Event ev){
        if (ev instanceof ListEvent && ev.type==MenuEvent.SELECTED) {
        	btAccept.modify(0,Disabled);
        	btAccept.repaint();
        }
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btCancel){
				this.close(0);
			}
			if (ev.target == btAccept){
				this.close(0);
				selectedItem=disp.getSelectedIndex(0);
			}
		}
	}

	private class myList extends SimpleList {
		private TravelbugList tbl;
		private boolean allowNew;
		private int size; 
		myList(TravelbugList tbl,boolean allowNew) {
			this.tbl=tbl;
			this.size=tbl.size();
			this.allowNew=allowNew;
		}
		
		public Object getObjectAt(int idx) {
			return getDisplayItem(idx);		
		}
		public int getItemCount() {
			return tbl.size()+ (allowNew?1:0);
		}
		public String getDisplayItem(int idx) {
			if (idx==size)
				return MyLocale.getMsg(6015,"*** OTHER ***");
			else if (tbl.getTB(idx).getName().indexOf("&#")<0)
				return tbl.getTB(idx).getName();
			else // If the name contains HTML entities, we need to convert it back
				return SafeXML.cleanback(tbl.getTB(idx).getName());
		}
	}



}
