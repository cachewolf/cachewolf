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
import CacheWolf.TravelbugJourney;
import CacheWolf.model.TravelBugJourneyScreenModel;
import ewe.graphics.AniImage;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Form;

public class PDATravelbugJourneyScreen extends Form {
	private static final String LINE = "Line";

	private static final String NEXT_PAGE = "NextPage";

	private static final String PREV_PAGE = "PrevPage";

	private static final String MENUE = "Menue";

	PDAListButton[] listButtons;

	TravelBugJourneyScreenModel model;

	/**
	 * The index of the first item in the list shown
	 */
	private int firstLine;

	private final int linesOnScreen = 7;

	/**
	 * The six visible entries in the List
	 */

	public PDATravelbugJourneyScreen(TravelBugJourneyScreenModel travelbugModel) {
		listButtons = new PDAListButton[linesOnScreen];
		addListener(this);
		setTitle("TravelBugs");

		model = travelbugModel;

		// backgroundImage = new Image("bug_vga.gif");
		for (int i = 0; i < model.allTravelbugJourneys.size(); i++) {
			model.shownTravelbugJourneys.add(model.allTravelbugJourneys.getTBJourney(i));
		}

		firstLine = 0;
		for (int i = 0; i < linesOnScreen; i++) {
			listButtons[i] = new PDAListButton("", LINE + i);
			addLast(listButtons[i], CellConstants.STRETCH, CellConstants.FILL);
		}
		model.createShowSet();
		setupTBButtons();
		PDAMenuButton b1 = new PDAMenuButton("<<<", PREV_PAGE);
		addNext(b1, CellConstants.HSTRETCH, CellConstants.HFILL);
		b1 = new PDAMenuButton(MyLocale.getMsg(6052, "MENU"), MENUE);
		b1.anchor = 0;
		addNext(b1, CellConstants.HSTRETCH, CellConstants.HFILL);
		b1 = new PDAMenuButton(">>>", NEXT_PAGE);
		b1.anchor = CellConstants.EAST;
		addLast(b1, CellConstants.HSTRETCH, CellConstants.HFILL);
	}

	public void onControlEvent(ControlEvent ev) {
		if (ev instanceof ControlEvent) {
			switch (ev.type) {
			case ControlEvent.PRESSED:
				if (ev.action.equals(NEXT_PAGE) && model.shownTravelbugJourneys.size() > firstLine + linesOnScreen) {
					firstLine += linesOnScreen;
					setupTBButtons();
				} else if (ev.action.equals(PREV_PAGE) && firstLine > 0) {
					firstLine -= linesOnScreen;
					if (firstLine < 0) {
						firstLine = 0;
					}
					setupTBButtons();
				} else if (ev.action.startsWith(LINE)) {
					int line = ev.action.charAt(LINE.length()) - '0';
					TravelbugJourney tbJourney = (TravelbugJourney) model.shownTravelbugJourneys.get(line + firstLine);
					Form form = new PDATravelbugDetailPanel(tbJourney, this);
					form.setPreferredSize(800, 600);
					form.execute();
					setupTBButtons();
				} else if (ev.action.equals(MENUE)) {
					Form form = new PDATravelbugMenuPanel(this);
					form.setPreferredSize(800, 600);
					int execute = form.execute();
					if (execute == 1){
						exit(0);
					}
					setupTBButtons();
				}
				break;
			default:
				super.onControlEvent(ev);
			}
		}
	}

	public void setupTBButtons() {
		for (int i = 0; i < linesOnScreen; i++) {
			if (i + firstLine < model.shownTravelbugJourneys.size()) {
				TravelbugJourney tbJourney = (TravelbugJourney) model.shownTravelbugJourneys.get(i + firstLine);
				String tbName = tbJourney.getTb().getName();
				listButtons[i].text = tbName;
				listButtons[i].fromText = tbJourney.getFromWaypoint() + '/' + tbJourney.getFromProfile();
				listButtons[i].fromLogged = tbJourney.getFromLogged();
				listButtons[i].toText = tbJourney.getToWaypoint() + '/' + tbJourney.getToProfile();
				listButtons[i].toLogged = tbJourney.getToLogged();
				listButtons[i].image = new AniImage("bug_vga.gif");
				listButtons[i].modify(ControlConstants.Disabled, 1);
			} else {
				listButtons[i].text = listButtons[i].fromText =	listButtons[i].toText = "";
				listButtons[i].toLogged = listButtons[i].fromLogged = true;
				listButtons[i].image = null;
				listButtons[i].modify(ControlConstants.Disabled, 0);
			}
			listButtons[i].repaint();
		}
	}

	public void toggleOnlyLogged() {
		model.toggleOnlyLogged();
		firstLine = 0;
		setupTBButtons();
	}

	public void createShowSet() {
		firstLine = 0;
		model.createShowSet();
	}
}
