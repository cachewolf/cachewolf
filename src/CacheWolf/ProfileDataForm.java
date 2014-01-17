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
package CacheWolf;

import CacheWolf.navi.TransformCoordinates;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.UIConstants;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mLabel;

/**
 * This form displays profile specific data.
 * It allows the copying of the current centre to the profile centre
 */
public class ProfileDataForm extends Form {

	private mButton btnOK, btnCurrentCentre, btnProfileCentre, btnCur2Prof, btnProf2Cur;//, btnGPS2Cur;
	mCheckBox chkSetCurrentCentreFromGPSPosition;
	private CellPanel content = new CellPanel();

	public ProfileDataForm() {
		super();

		resizable = false;
		content.setText(MyLocale.getMsg(1115, "Centre"));
		content.borderStyle = UIConstants.BDR_RAISEDOUTER | UIConstants.BDR_SUNKENINNER | UIConstants.BF_RECT;
		//defaultTags.set(this.INSETS,new Insets(2,2,2,2));
		title = MyLocale.getMsg(1118, "Profile") + ": " + MainForm.profile.name;
		content.addNext(new mLabel(MyLocale.getMsg(600, "Preferences")));
		content.addLast(chkSetCurrentCentreFromGPSPosition = new mCheckBox(MyLocale.getMsg(646, "centre from GPS")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		// content.addLast(btnGPS2Cur=new mButton("   v   "),DONTSTRETCH,DONTFILL|LEFT);
		if (Preferences.itself().setCurrentCentreFromGPSPosition)
			chkSetCurrentCentreFromGPSPosition.setState(true);
		content.addNext(new mLabel(MyLocale.getMsg(1116, "Current")));
		content.addLast(btnCurrentCentre = new mButton(Preferences.itself().getCurCentrePt().toString()), HSTRETCH, HFILL | LEFT);
		content.addNext(new mLabel("      "), HSTRETCH, HFILL);
		content.addNext(btnCur2Prof = new mButton("   v   "), DONTSTRETCH, DONTFILL | LEFT);
		content.addNext(new mLabel(MyLocale.getMsg(1117, "copy")));
		content.addLast(btnProf2Cur = new mButton("   ^   "), DONTSTRETCH, DONTFILL | RIGHT);
		content.addNext(new mLabel(MyLocale.getMsg(1118, "Profile")));
		content.addLast(btnProfileCentre = new mButton(MainForm.profile.centre.toString()), HSTRETCH, HFILL | LEFT);
		addLast(content, HSTRETCH, HFILL);
		//addLast(new mLabel(""),VSTRETCH,FILL);
		//addNext(btnCancel = new mButton(MyLocale.getMsg(1604,"Cancel")),DONTSTRETCH,DONTFILL|LEFT);
		addLast(btnOK = new mButton("OK"), DONTSTRETCH, HFILL | RIGHT);
	}

	/**
	 * The event handler to react to a users selection.
	 * A return value is created and passed back to the calling form
	 * while it closes itself.
	 */
	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			/*if (ev.target == btnCancel){
				close(-1);
			}*/
			if (ev.target == btnOK) {
				Preferences.itself().setCurrentCentreFromGPSPosition = chkSetCurrentCentreFromGPSPosition.getState();
				close(1);
			}
			if (ev.target == btnCurrentCentre) {
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(Preferences.itself().getCurCentrePt(), TransformCoordinates.CW);
				if (cs.execute() == FormBase.IDOK) {
					Preferences.itself().setCurCentrePt(cs.getCoords());
					btnCurrentCentre.setText(Preferences.itself().getCurCentrePt().toString());
					MainForm.profile.updateBearingDistance();
				}
			}
			if (ev.target == btnProfileCentre) {
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(MainForm.profile.centre, TransformCoordinates.CW);
				if (cs.execute() == FormBase.IDOK) {
					MainForm.profile.notifyUnsavedChanges(cs.getCoords().equals(MainForm.profile.centre));
					MainForm.profile.centre.set(cs.getCoords());
					btnProfileCentre.setText(MainForm.profile.centre.toString());
				}
			}
			if (ev.target == btnCur2Prof) {
				MainForm.profile.notifyUnsavedChanges(Preferences.itself().getCurCentrePt().equals(MainForm.profile.centre));
				MainForm.profile.centre.set(Preferences.itself().getCurCentrePt());
				btnProfileCentre.setText(MainForm.profile.centre.toString());
			}
			if (ev.target == btnProf2Cur) {
				Preferences.itself().setCurCentrePt(MainForm.profile.centre);
				btnCurrentCentre.setText(Preferences.itself().getCurCentrePt().toString());
				MainForm.profile.updateBearingDistance();
			}
		}
		super.onEvent(ev);
	}

}
