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

import ewe.io.IOException;
import ewe.sys.Handle;
import ewe.sys.HandleStoppedException;
import ewe.sys.mThread;
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
import ewe.util.Vector;
import ewesoft.xml.sax.SAXException;

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
	HtmlDisplay foundTxt;
	int exitKeys[]={75009};

	Vector geoCodeAnsw;
	String searchText;
	Handle[] fetchHandle = new Handle[1];

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
		foundTxt     = new HtmlDisplay();
		foundTxt.setPreferredSize(200, 200);
		ScrollBarPanel sbp = new MyScrollBarPanel(foundTxt, 0);
		sbp.setClientConstraints(ScrollBarPanel.HCONTRACT|ScrollBarPanel.HCONTRACT);
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

		// Ensure that the Enter key moves to the appropriate field
		// for Checkboxes and Choice controls this is done via the exitKeys
		// For input fields we use the wantReturn field
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == searchBtn ){
				foundTxt.setHtml(MyLocale.getMsg(7306, "searching..."));
				// only insert "," if city AND street is set
				searchText = streetInp.text.trim();
				if (searchText.length() > 0) {
					if (cityInp.text.trim().length() > 0) searchText = searchText + ","+cityInp.text;
				} else searchText = cityInp.text;

				mThread thrdfetch = 
					new mThread() {
					public void run() {
						try {
							fetchHandle[0] = null;
							geoCodeAnsw = GeocoderOsm.geocode(searchText, fetchHandle);
						} catch (IOException e) {
							geoCodeAnsw = new Vector();
							geoCodeAnsw.add(new GeocodeAnswer(new CWPoint(), "IOExecption"));
						} catch (SAXException e) {
							geoCodeAnsw = new Vector();
							geoCodeAnsw.add(new GeocodeAnswer(new CWPoint(), "SAXException"));
						} catch (HandleStoppedException ie) {
							geoCodeAnsw = new Vector();
							if (fetchHandle[0].stopReason == 4321)
								geoCodeAnsw.add(new GeocodeAnswer(new CWPoint(), MyLocale.getMsg(7307, "Canceled by user")));
							else geoCodeAnsw.add(new GeocodeAnswer(new CWPoint(), MyLocale.getMsg(7308, "Could not connect")));
						} catch (InterruptedException ie) {
							geoCodeAnsw = new Vector();
							if (fetchHandle[0].stopReason == 4321)
								geoCodeAnsw.add(new GeocodeAnswer(new CWPoint(), MyLocale.getMsg(7307, "Canceled by user")));
							else geoCodeAnsw.add(new GeocodeAnswer(new CWPoint(), MyLocale.getMsg(7308, "Could not connect")));

						}
						// foundTxt.startHtml();
						if (geoCodeAnsw.size() == 0) foundTxt.setHtml("nothing found");
						else {
							GeocodeAnswer ga = (GeocodeAnswer)geoCodeAnsw.get(0);
							foundTxt.setHtml(ga.where.toString() + "<br>" + ga.foundname);
						}
						fetchHandle[0] = null;
					}
				};
				thrdfetch.start();
			}

			if (ev.target == searchCancelBtn){
				if (fetchHandle != null && fetchHandle[0] != null)
				{
					fetchHandle[0].stop(4321);
					fetchHandle[0].set(Handle.Stopped);
				}
			}

			if (ev.target == btnCancel){
				this.close(IDCANCEL);
			}

			if (ev.target == btnOk){
				if (geoCodeAnsw != null && geoCodeAnsw.size() > 0)
				coordInp = ((GeocodeAnswer)geoCodeAnsw.get(0)).where;
				else coordInp.makeInvalid();
				this.close(IDOK);
			}

		}
		super.onEvent(ev);
	}

}



