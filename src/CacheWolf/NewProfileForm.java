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

import CacheWolf.controls.ExecutePanel;
import CacheWolf.controls.InfoBox;
import CacheWolf.utils.MyLocale;
import ewe.io.File;
import ewe.ui.*;

public class NewProfileForm extends Form {
    private final ExecutePanel executePanel;
    public String profileDir;
    private mInput inpDir;
    private TextMessage description;
    private String baseDir;

    //private Profile profile;

    public NewProfileForm(String baseDir) {
        super();
        //profile=prof;
        title = MyLocale.getMsg(1111, "Create new profile:");
        addLast(inpDir = new mInput(MyLocale.getMsg(1112, "New profile name")), HSTRETCH, HFILL | LEFT);
        description = new TextMessage(MyLocale.getMsg(1123, "Click 'Next' to define the center coordinates for this profile."));
        description.setPreferredSize(240, -1);
        addLast(description, HSTRETCH, HFILL | LEFT);

        executePanel = new ExecutePanel(this);
        this.setPreferredSize(240, -1);
        this.baseDir = baseDir;
    }

    public void onEvent(Event ev) {
        if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
            if (ev.target == executePanel.cancelButton) {
                this.close(-1);
            }
            if (ev.target == executePanel.applyButton) {
                profileDir = inpDir.getDisplayText();
                if (profileDir.equalsIgnoreCase("maps")) {
                    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1122, "'maps' is reserved for the maps directory.")).wait(FormBase.OKB);
                    profileDir = "";
                } else {
                    File f = new File(baseDir + profileDir);
                    if (f.exists()) {
                        new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1114, "Directory exists already.")).wait(FormBase.OKB);
                        profileDir = "";
                    } else {
                        if (profileDir.indexOf("/") >= 0 || profileDir.indexOf("\\") >= 0 || !f.createDir()) {
                            new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1113, "Cannot create directory")).wait(FormBase.OKB);
                            profileDir = "";
                            this.close(-1);
                        }
                        MainForm.profile.setFilterActive(Filter.FILTER_INACTIVE);
                        this.close(0);
                    }
                }
            }
        }
        super.onEvent(ev);
    }
}
