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
import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.utils.FileBugfix;
import CacheWolf.utils.MyLocale;
import ewe.fx.Graphics;
import ewe.fx.Insets;
import ewe.fx.Rect;
import ewe.io.FileBase;
import ewe.ui.*;

/**
 * This form displays the list of profiles for a user to choose from,
 * when CacheWolf starts up. Also allows to open a new profile.
 * ClassID = 1300
 */
public class ProfilesForm extends Form {

    private final ExecutePanel executePanel;
    public String newSelectedProfile; // This is only used if a new profile is being created
    private MyList choice;
    private ScrollablePanel spMList;
    private mButton btnNew;
    private String baseDir;

    /**
     * Constructor to create a form to select profiles. It requires that the preferences
     * have been loaded so that the calling parameters can be set.
     *
     * @param baseDir         The base directory which holds one subdirectory per profile
     * @param selectedProfile Name of the last used profile
     * @param outfit          for the different purposes
     */
    public ProfilesForm(String baseDir, String selectedProfile, int outfit) {
        super();
        Preferences.itself().setSubWindowSize(this);
        defaultTags.set(CellConstants.INSETS, new Insets(2, 2, 2, 2));
        title = MyLocale.getMsg(1301, "Select Profile:");
        if (outfit == 0) {
            addNext(new mLabel(MyLocale.getMsg(1106, "Choose profile or New")), DONTSTRETCH, DONTSTRETCH | LEFT);
            addLast(btnNew = new mButton(MyLocale.getMsg(1107, "New")), HSTRETCH, HFILL | RIGHT);
        } else {
            if (outfit == 1) {
                addLast(new mLabel(MyLocale.getMsg(1108, "Choose profile")), DONTSTRETCH, DONTSTRETCH | LEFT);
            } else {
                if (outfit == 2) {
                    //delete
                    String msg = MyLocale.getMsg(1118, "profile") + " " + MyLocale.getMsg(1125, "delete");
                    addLast(new mLabel(msg), DONTSTRETCH, DONTSTRETCH | LEFT);
                } else {
                    if (outfit == 3) {
                        // rename
                        String msg = MyLocale.getMsg(1118, "profile") + " " + MyLocale.getMsg(1126, "rename");
                        addLast(new mLabel(msg), DONTSTRETCH, DONTSTRETCH | LEFT);
                    }
                }
            }
        }

        choice = new MyList();
        // Get all subdirectories in the base directory
        FileBugfix fileBaseDir = new FileBugfix(baseDir);
        String[] existingProfiles = fileBaseDir.list(null, FileBase.LIST_DIRECTORIES_ONLY);
        // Now add these subdirectories to the list of profiles but
        // exclude the "maps" directory which will contain the moving maps
        for (int i = 0; i < existingProfiles.length; i++)
            if (!existingProfiles[i].equalsIgnoreCase("maps"))
                choice.addItem(existingProfiles[i]);
        // Highlight the profile that was used last
        choice.selectLastProfile(selectedProfile);
        // Add a scroll bar to the list of profiles
        spMList = choice.getScrollablePanel();
        spMList.setOptions(ScrollablePanel.NeverShowHorizontalScrollers);
        choice.setServer(spMList);
        addLast(spMList);
        executePanel = new ExecutePanel(this);
        if (choice.getListItems().length == 0)
            executePanel.applyButton.modify(Disabled, 0);
        this.baseDir = baseDir;
        choice.takeFocus(ControlConstants.ByKeyboard);
    }

    /**
     * Ask for a new profile directory. If it exists, cancel. If it does not exist, create it
     *
     * @return Name of directory (just the part below baseDir)
     */
    public String createNewProfile() {
        NewProfileForm f = new NewProfileForm(baseDir);
        int code = f.execute(getFrame(), Gui.CENTER_FRAME);
        if (code == 0) {
            return f.profileDir;
        } else
            return "";
    }

    /**
     * The event handler to react to a users selection.
     * A return value is created and passed back to the calling form
     * while it closes itself.
     */
    public void onEvent(Event ev) {
        if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
            if (ev.target == executePanel.cancelButton) {
                close(-1);
            }
            if (ev.target == executePanel.applyButton || ev.target == choice) {
                if (choice.getSelectedItem() != null) {
                    newSelectedProfile = choice.getSelectedItem().toString();
                    close(1);
                }
            }
            if (ev.target == btnNew) {
                if (NewProfileWizard.startNewProfileWizard(getFrame())) {
                    newSelectedProfile = MainForm.profile.name;
                    close(1);
                }
            }
        }
        super.onEvent(ev);
    }

    // A subclassed mList which allows the highlighting of an entry
    // Maybe there is an easier way of making this happen, but I could not find it.
    private class MyList extends mList {
        private int first = 1;
        private int select;

        public MyList() {
            super(1, 1, false);
        }

        public void selectLastProfile(String selectedItem) {
            selectItem(selectedItem);
            select = getSelectedIndex(0);
        }

        public void doPaint(Graphics gr, Rect area) {
            if (first == 1) {
                first = 0;
                selectAndView(select);
                makeVisible(select);
            }
            super.doPaint(gr, area);
        }

        // Copied from BasicList.getScrollablePanel(), but exchanging
        // the standard scroll bar with the fontsize sensitive one.
        public ScrollablePanel getScrollablePanel() {
            dontAutoScroll = amScrolling = true;
            ScrollablePanel sp = new MyScrollBarPanel(this);
            sp.modify(0, TakeControlEvents);
            return sp;
        }

    }

}
