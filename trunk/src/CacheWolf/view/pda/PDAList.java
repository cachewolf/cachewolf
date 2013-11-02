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
import CacheWolf.model.DefaultListModel;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Form;

public abstract class PDAList extends Form {
	protected static final String LINE = "Line";

	protected static final String NEXT_PAGE = "NextPage";

	protected static final String PREV_PAGE = "PrevPage";

	protected static final String MENUE = "Menue";

	private static final String NONE = "none";

	public DefaultListModel model;
	private int linesOnScreen=7;
	PDAListButton[] listButtons;
	protected int firstLine;

	public PDAList() {
		listButtons = new PDAListButton[linesOnScreen];
		//Show Full Screen
		setPreferredSize(MyLocale.getScreenWidth(), MyLocale.getScreenHeight());
		addListener(this);
		setTitle("Liste");

		firstLine = 0;
		for (int i = 0; i < linesOnScreen; i++) {
			listButtons[i]  = createListButton(i);
			addLast(listButtons[i], CellConstants.STRETCH, CellConstants.FILL);
		}
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

	protected PDAListButton createListButton(int i) {
		return new PDAListButton("", LINE + i);
	}

	public void onControlEvent(ControlEvent ev) {
		if (ev instanceof ControlEvent) {
			switch (ev.type) {
			case ControlEvent.PRESSED:
				if (ev.action.equals(NEXT_PAGE) && model.size() > firstLine + linesOnScreen) {
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
					Object clickedItem =  model.get(line + firstLine);
					Vm.debug("List clicked: " + clickedItem);
				} else if (ev.action.equals(MENUE)) {
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
			if (model != null && i + firstLine < model.size()) {
				Object modelElement = model.get(i + firstLine);
				listButtons[i].text = modelElement.toString();
				listButtons[i].action = LINE+i;
			} else {
				listButtons[i].text = listButtons[i].fromText = listButtons[i].toText = "";
				listButtons[i].action = NONE;
			}
			listButtons[i].repaint();
		}
	}

}
