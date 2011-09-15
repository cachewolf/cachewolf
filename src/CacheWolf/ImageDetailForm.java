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
import ewe.ui.ScrollBarPanel;
import ewe.ui.mLabel;


/**
* Class creates a view on the image scaled
* to the application size, but only if the image is larger than
* the available app size.
*/
public class ImageDetailForm extends Form{
	ImageInteractivePanel ipp = new ImageInteractivePanel();
	ScrollBarPanel scp;
	
	public ImageDetailForm(String imgLoc, String imgTitle, String imgComment, Preferences p){
		scp = new MyScrollBarPanel(ipp);
		ipp.setImage(imgLoc);
		this.title = "Image";
		this.setPreferredSize(p.myAppWidth, p.myAppHeight);
		this.addLast(scp, CellConstants.STRETCH, CellConstants.FILL);
		this.addLast(new mLabel(imgTitle), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		this.addLast(new mLabel(imgComment), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
	}
	
	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type==ControlEvent.EXITED) {
			ev.consumed=true;
			this.close(0);
		} else super.onEvent(ev);
	}
}

