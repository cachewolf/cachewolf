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
package CacheWolf.exp;

import CacheWolf.ExecutePanel;
import ewe.ui.CheckBoxGroup;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.mCheckBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;

public class TomTomExporterScreen extends Form {
	private final ExecutePanel executePanel;
	mCheckBox chkASC, chkOV2, chkOneFilePerType;
	CheckBoxGroup chkGroupFormat;
	mInput inpPrefix;

	public TomTomExporterScreen(String title) {
		super();
		this.title = title;

		// checkboxgroup for fileformat
		chkGroupFormat = new CheckBoxGroup();
		chkASC = new mCheckBox(".asc");
		chkASC.setGroup(chkGroupFormat);
		chkOV2 = new mCheckBox(".ov2");
		chkOV2.setGroup(chkGroupFormat);
		chkGroupFormat.selectIndex(TomTomExporter.TT_OV2);

		this.addLast(new mLabel("Fileformat"));
		this.addNext(chkASC);
		this.addLast(chkOV2);

		// checkbox for one file for all or one file per cachetype
		chkOneFilePerType = new mCheckBox("Eine Datei pro Cachetyp");
		chkOneFilePerType.setState(true);
		this.addLast(chkOneFilePerType);

		//prefix for files, if one file per cachetype
		inpPrefix = new mInput("GC-");
		activateInpPrefix();
		this.addLast(inpPrefix);

		executePanel = new ExecutePanel(this);
	}

	public int getFormat() {
		return chkGroupFormat.getSelectedIndex();
	}

	public boolean oneFilePerType() {
		return chkOneFilePerType.getState();
	}

	public String getPrefix() {
		return inpPrefix.getText();
	}

	private void activateInpPrefix() {
		if (chkOneFilePerType.getState())
			inpPrefix.modify(0, ControlConstants.Disabled);
		else
			inpPrefix.modify(ControlConstants.Disabled, 0);
		inpPrefix.repaintNow();
	}

	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == executePanel.cancelButton) {
				this.close(FormBase.IDCANCEL);
			}
			if (ev.target == executePanel.applyButton) {
				this.close(FormBase.IDOK);
			}
			if (ev.target == chkOneFilePerType) {
				activateInpPrefix();
			}
		}
		super.onEvent(ev);
	}

}
