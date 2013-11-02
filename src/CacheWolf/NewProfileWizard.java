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
import ewe.ui.FormBase;
import ewe.ui.Gui;

public class NewProfileWizard {

	static public boolean startNewProfileWizard(ewe.ui.Frame parent) {
		if (Global.mainTab != null)
			Global.mainTab.saveUnsavedChanges(true);
		NewProfileForm f = new NewProfileForm(Global.pref.absoluteBaseDir);
		int code = f.execute(parent, Gui.CENTER_FRAME);
		if (code == 0) {
			Global.profile.clearProfile();
			Global.pref.lastProfile = Global.profile.name = f.profileDir;
			Global.pref.savePreferences(); // Remember that this was the last profile used
			Global.profile.dataDir = Global.pref.absoluteBaseDir + f.profileDir + "/";

			CoordsScreen cs = new CoordsScreen();
			cs.setFields(new CWPoint(), TransformCoordinates.CW);
			if (cs.execute() == FormBase.IDOK) {
				Global.profile.setCenterCoords(cs.getCoords());
			}
			MainForm.itself.setTitle(Global.profile.name + " - CW " + Version.getRelease());
			Global.profile.notifyUnsavedChanges(true);
		}
		f.close(0);
		return (code == 0);
	}
}
