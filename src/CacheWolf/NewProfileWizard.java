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

import CacheWolf.database.CWPoint;
import CacheWolf.navi.TransformCoordinates;
import ewe.ui.FormBase;
import ewe.ui.Gui;

public class NewProfileWizard {

    static public boolean startNewProfileWizard(ewe.ui.Frame parent) {
        if (MainTab.itself != null)
            MainTab.itself.saveUnsavedChanges(true);
        NewProfileForm f = new NewProfileForm(Preferences.itself().absoluteBaseDir);
        int code = f.execute(parent, Gui.CENTER_FRAME);
        if (code == 0) {
            MainForm.profile.clearProfile();
            Preferences.itself().lastProfile = MainForm.profile.name = f.profileDir;
            Preferences.itself().savePreferences(); // Remember that this was the last profile used
            MainForm.profile.dataDir = Preferences.itself().absoluteBaseDir + f.profileDir + "/";

            CoordsInput cs = new CoordsInput();
            cs.setFields(new CWPoint(), TransformCoordinates.DMM);
            if (cs.execute() == FormBase.IDOK) {
                MainForm.profile.setCenterCoords(cs.getCoords());
            }
            MainForm.itself.setTitle(MainForm.profile.name + " - CW " + Version.getRelease());
            MainForm.profile.notifyUnsavedChanges(true);
        }
        f.close(0);
        return (code == 0);
    }
}
