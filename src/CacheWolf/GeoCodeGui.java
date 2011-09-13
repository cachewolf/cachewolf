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
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.HtmlDisplay;
import ewe.ui.IKeys;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mButton;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.mList;
import ewe.util.Vector;

/**
 * Class for entering an address and convert it to lat/lon
 * starting index in language files: 7300 
 */

public class GeoCodeGui extends Form {

	mInput streetInp, cityInp;
	mButton searchBtn, searchCancelBtn, btnCancel, btnOk;
	CWPoint coordInp = new CWPoint();
	CellPanel topLinePanel = new CellPanel();
	CellPanel mainPanel = new CellPanel();
	// HtmlDisplay foundTxt;
	mList choice;
	int exitKeys[]={75009};

	Vector geoCodeAnsw;
	String searchText;

	public GeoCodeGui()
	{
		topLinePanel.addNext(new mLabel(MyLocale.getMsg(7300, "Street/POI")),CellConstants.DONTSTRETCH, CellConstants.WEST);
		topLinePanel.addLast(streetInp = new mInput(MyLocale.getMsg(7305, "Hauptbahnhof")),CellConstants.STRETCH, CellConstants.FILL | CellConstants.WEST);
		//streetInp.setPreferredSize(500, 20);
		topLinePanel.addNext(new mLabel(MyLocale.getMsg(7301, "City")),CellConstants.DONTSTRETCH, CellConstants.WEST);
		topLinePanel.addNext(cityInp   = new mInput(MyLocale.getMsg(7304, "München, Deutschland")),CellConstants.HSTRETCH, CellConstants.HFILL | CellConstants.WEST);
		topLinePanel.addNext(searchBtn = new mButton(MyLocale.getMsg(7302, "Search")),CellConstants.DONTSTRETCH,CellConstants.WEST);
		topLinePanel.addLast(searchCancelBtn = new mButton(MyLocale.getMsg(7303, "Cancel")),CellConstants.DONTSTRETCH,CellConstants.WEST);
		// inpText.toolTip=MyLocale.getMsg(1406,"Enter coordinates in any format or GCxxxxx");

		this.addLast(topLinePanel,CellConstants.STRETCH, CellConstants.FILL | CellConstants.WEST);

		// Description of found sites
		choice=new mList(8,50,false);
		ScrollBarPanel sbp = new MyScrollBarPanel(choice, 0);
		sbp.setOptions(MyScrollBarPanel.NeverShowVerticalScrollers);
		mainPanel.addLast(sbp, CellConstants.STRETCH, CellConstants.FILL | CellConstants.WEST);

		// Buttons for cancel and apply
		btnCancel = new mButton(MyLocale.getMsg(614,"Cancel"));
		btnCancel.setHotKey(0, IKeys.ESCAPE);
		mainPanel.addNext(btnCancel, CellConstants.HSTRETCH, (CellConstants.HFILL));
		//btnCancel.setTag(SPAN,new Dimension(4,1));
		mainPanel.addNext(btnOk = new mButton(MyLocale.getMsg(615,"Apply")),CellConstants.HSTRETCH, (CellConstants.HFILL));

		//add Panels
		this.addLast(mainPanel,CellConstants.STRETCH, CellConstants.FILL | CellConstants.WEST);
	}

	public void onEvent(Event ev){

		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == searchBtn ){
		        Vm.showWait(true);
				try {
					geoCodeAnsw = GeocoderOsm.geocode(cityInp.text.trim(), streetInp.text.trim());
				} catch (Exception e) {
					geoCodeAnsw = new Vector();
					geoCodeAnsw.add(new GeocodeAnswer(new CWPoint(), e.getMessage()));
				}
		        Vm.showWait(false);
				if (geoCodeAnsw.size() == 0) {
					geoCodeAnsw = new Vector();
					geoCodeAnsw.add(new GeocodeAnswer(new CWPoint(), "nothing found"));
				}
				choice.items.clear();
				for (int i = 0; i < geoCodeAnsw.size(); i++) {
					GeocodeAnswer ga = (GeocodeAnswer)geoCodeAnsw.get(i);
					choice.addItem(ga.where.toString() + " | " + ga.foundname);
				}
				choice.updateItems();
			}

			if (ev.target == searchCancelBtn){
			}

			if (ev.target == btnCancel){
				this.close(IDCANCEL);
			}

			if (ev.target == btnOk){
				if (geoCodeAnsw != null && geoCodeAnsw.size() > 0) {
					int i = choice.selectedIndex;
					coordInp = ((GeocodeAnswer)geoCodeAnsw.get(i)).where;					
				}
				else coordInp.makeInvalid();
				this.close(IDOK);
			}

		}
		super.onEvent(ev);
	}

}



