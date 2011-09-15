    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

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
import ewe.fx.Dimension;
import ewe.fx.mImage;
import ewe.sys.Time;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mButton;
import ewe.ui.mTextPad;

/**
*	This class displays a form to show and edit notes for a cache.
*	Class ID=
*/
public class NotesScreen extends Form{
	mTextPad wayNotes = new mTextPad();
	CacheHolderDetail chD = null;
	mButton addDateTime;
	mButton btSave = new mButton(MyLocale.getMsg(127,"Save"));
	mButton cancelBtn = new mButton("Cancel");
	ScrollBarPanel sbp = new MyScrollBarPanel(wayNotes);
	
	public NotesScreen(CacheHolderDetail ch){
		String imagesize = "";
		if (Global.getPref().useBigIcons) imagesize="_vga";
		addDateTime = new mButton(new mImage("date_time"+imagesize+".gif"));
		
		this.title = "Notes";
		setPreferredSize(Global.getPref().myAppWidth, Global.getPref().myAppHeight);
		this.resizeOnSIP = true;
		chD = ch;
		wayNotes.setText(chD.getCacheNotes());
		addLast(sbp.setTag(CellConstants.SPAN, new Dimension(3,1)),CellConstants.STRETCH, (CellConstants.FILL|CellConstants.WEST));
		titleControls=new CellPanel();
		titleControls.addNext(addDateTime,CellConstants.HSTRETCH,CellConstants.HFILL);
		titleControls.addNext(cancelBtn,CellConstants.HSTRETCH,CellConstants.HFILL);
		titleControls.addLast(btSave,CellConstants.HSTRETCH,CellConstants.HFILL);
	}
	
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == addDateTime){
				String note = wayNotes.getText();
				Time dtm = new Time();
				dtm.getTime();
				dtm.setFormat("E dd.MM.yyyy '/' HH:mm");
				if(note.length() > 0)	note = note + "\n" + dtm.toString();
				else 	note = note + dtm.toString();
				note = note + "\n";
				wayNotes.setText(note);
			}
			if(ev.target == btSave){
				chD.setCacheNotes(wayNotes.getText());
				chD.getParent().save();
				this.close(0);
			}
			if(ev.target == cancelBtn){
				if ( (!chD.getCacheNotes().equals(wayNotes.getText())) ) {
					if ( (new MessageBox("Warning", "You will loose any changes made to the notes. Do you want to continue?"
							, FormBase.YESB|FormBase.NOB)).execute() == FormBase.IDYES) {
						this.close(0);
					}
				} else this.close(0); // no changes -> exit without asking
			} 
			if(ev.target == titleOK){
				if ( (!chD.getCacheNotes().equals(wayNotes.getText())) ) {
					if ( (new MessageBox("Warning", "Save changes made to the notes?"
							, FormBase.YESB|FormBase.NOB)).execute() == FormBase.IDYES) {
						chD.setCacheNotes(wayNotes.getText());
						chD.getParent().save();
					}
				}
			}
		}
		super.onEvent(ev);
	}
}
