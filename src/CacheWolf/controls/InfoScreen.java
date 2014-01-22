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

import ewe.io.FileReader;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.HtmlDisplay;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollablePanel;

/**
 * This class displays an information screen. It loads the html text to display
 * from a file that is given upon creation of this class. It offers
 * a cancel button enabling the user to close the screen and return to
 * wherever the user was before
 * Class ID = 3000
 */
public class InfoScreen extends Form {

    HtmlDisplay disp = new HtmlDisplay();
    ExecutePanel executePanel;

    public InfoScreen(String datei, String tit, boolean readFromFile) {
	String myText = new String();
	this.setTitle(tit);
	if (readFromFile == true) {
	    try {
		FileReader in = new FileReader(datei);
		myText = in.readAll();
		in.close();
	    } catch (Exception ex) {
	    }
	} else
	    myText = datei;
	disp.setHtml(myText);
	ScrollBarPanel sbp = new MyScrollBarPanel(disp, ScrollablePanel.NeverShowHorizontalScrollers);
	this.addLast(sbp);
	executePanel = new ExecutePanel(this, FormBase.OKB);
    }

    public void onEvent(Event ev) {
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == executePanel.applyButton) {
		this.close(0);
	    }
	}
    }
}
