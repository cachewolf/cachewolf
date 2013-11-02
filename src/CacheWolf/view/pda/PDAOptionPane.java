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

import CacheWolf.Global;
import ewe.fx.Color;
import ewe.fx.Font;
import ewe.ui.ControlEvent;
import ewe.ui.Form;
import ewe.ui.Frame;
import ewe.ui.Gui;
import ewe.ui.mLabel;

public class PDAOptionPane extends Form {
	public static final int CANCEL = 0;
	public static final int OK = 1;
	private static final String OK_STR = "OK";
	private static final String CANCEL_STR = "CANCEL";

	private int result = CANCEL;

	public static int showConfirmDialog(Frame parent, String title, String message) {
		PDAOptionPane pane = new PDAOptionPane();
		pane.title = title;
		pane.backGround = new Color(255, 128, 128);
		Font tmpFont = new Font("Helvetica", Font.BOLD, Global.pref.fontSize * 2);
		mLabel messageLabel = new mLabel(message);
		messageLabel.font = tmpFont;
		pane.addLast(messageLabel, HFILL, HSTRETCH);
		PDAMenuButton button = new PDAMenuButton("OK", OK_STR);
		pane.addNext(button, FILL, STRETCH);
		button = new PDAMenuButton("Abbruch", CANCEL_STR);
		pane.addLast(button, FILL, STRETCH);
		pane.setLocation(0, 0);
		pane.execute(null, Gui.FILL_FRAME);
		return pane.result;
	}

	public void onControlEvent(ControlEvent event) {
		switch (event.type) {
		case ControlEvent.PRESSED:
			String action = event.action;
			if (action.equals(OK_STR)) {
				result = OK;
				exit(0);
			}
			else if (action.equals(CANCEL_STR)) {
				exit(0);
			}
		}
	}
}
