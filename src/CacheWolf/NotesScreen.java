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
import CacheWolf.controls.GuiImageBroker;
import CacheWolf.controls.InfoBox;
import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.utils.MyLocale;
import ewe.fx.Dimension;
import ewe.sys.Time;
import ewe.ui.*;

/**
 * This class displays a form to show and edit notes for a cache.
 * Class ID=
 */
public class NotesScreen extends Form {
    private final ExecutePanel executePanel;
    mTextPad wayNotes = new mTextPad();
    CacheHolder ch = null;
    CacheHolderDetail chD = null;
    mButton addDateTime;
    ScrollBarPanel sbp = new MyScrollBarPanel(wayNotes);

    public NotesScreen(CacheHolder _ch) {
        addDateTime = GuiImageBroker.getButton("", "date_time");
        this.title = "Notes";
        Preferences.itself().setBigWindowSize(this);
        this.resizeOnSIP = true;
        ch = _ch;
        chD = _ch.getDetails();
        wayNotes.setText(chD.getCacheNotes());
        addLast(sbp.setTag(CellConstants.SPAN, new Dimension(3, 1)), CellConstants.STRETCH, (CellConstants.FILL | CellConstants.WEST));
        titleControls = new CellPanel();
        titleControls.addNext(addDateTime, CellConstants.HSTRETCH, CellConstants.HFILL);
        executePanel = new ExecutePanel(titleControls);
    }

    public void onEvent(Event ev) {
        if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
            if (ev.target == addDateTime) {
                String note = wayNotes.getText();
                Time dtm = new Time();
                dtm.getTime();
                dtm.setFormat("E dd.MM.yyyy '/' HH:mm");
                if (note.length() > 0)
                    note = note + "\n" + dtm.toString();
                else
                    note = note + dtm.toString();
                note = note + "\n";
                wayNotes.setText(note);
            }
            if (ev.target == executePanel.applyButton) {
                chD.setCacheNotes(wayNotes.getText());
                chD.saveCacheDetails(MainForm.profile.dataDir);
                this.close(0);
            }
            if (ev.target == executePanel.cancelButton) {
                if ((!chD.getCacheNotes().equals(wayNotes.getText()))) {
                    if (new InfoBox(MyLocale.getMsg(144, "Warning"), MyLocale.getMsg(352,"You will loose any changes made to the notes. Do you want to continue?")).wait(FormBase.YESB | FormBase.NOB) == FormBase.IDYES) {
                        this.close(0);
                    }
                } else
                    this.close(0); // no changes -> exit without asking
            }
            if (ev.target == titleOK) {
                if ((!chD.getCacheNotes().equals(wayNotes.getText()))) {
                    if (new InfoBox(MyLocale.getMsg(144, "Warning"),MyLocale.getMsg(353, "Save changes made to the notes?")).wait(FormBase.YESB | FormBase.NOB) == FormBase.IDYES) {
                        chD.setCacheNotes(wayNotes.getText());
                        chD.saveCacheDetails(MainForm.profile.dataDir);
                    }
                }
            }
        }
        super.onEvent(ev);
    }
}
