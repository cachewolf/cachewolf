package CacheWolf;
import ewe.ui.*;
import ewe.io.*;
import ewe.graphics.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.filechooser.*;

/**
*	This class displays a form to show and edit notes for a cache.
*	Class ID=
*/
public class NotesScreen extends Form{
	mTextPad wayNotes = new mTextPad();
	CacheHolderDetail thisCache = null;
	mButton addDateTime = new mButton((IImage)new mImage("date_time.png"));
	mButton btSave = new mButton(MyLocale.getMsg(127,"Save"));
	mButton cancelBtn = new mButton("Cancel");
	ScrollBarPanel sbp = new MyScrollBarPanel(wayNotes);
	
	public NotesScreen(CacheHolderDetail ch){
		this.title = "Notes";
		setPreferredSize(Global.getPref().myAppWidth, Global.getPref().myAppHeight);
		this.resizeOnSIP = true;
		thisCache = ch;
		wayNotes.setText(thisCache.CacheNotes);
		addLast(sbp.setTag(Control.SPAN, new Dimension(3,1)),CellConstants.STRETCH, (CellConstants.FILL|CellConstants.WEST));
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
				thisCache.CacheNotes = wayNotes.getText();
				thisCache.saveCacheDetails( Global.getProfile().dataDir);
				this.close(0);
			}
			if(ev.target == cancelBtn){
				if ( (!thisCache.CacheNotes.equals(wayNotes.getText())) ) {
					if ( (new MessageBox("Warning", "You will loose any changes made to the notes. Do you want to continue?"
							, MessageBox.YESB|MessageBox.NOB)).execute() == MessageBox.IDYES) {
						this.close(0);
					}
				} else this.close(0); // no changes -> exit without asking
			} 
			if(ev.target == titleOK){
				if ( (!thisCache.CacheNotes.equals(wayNotes.getText())) ) {
					if ( (new MessageBox("Warning", "Save changes made to the notes?"
							, MessageBox.YESB|MessageBox.NOB)).execute() == MessageBox.IDYES) {
						thisCache.CacheNotes = wayNotes.getText();
						thisCache.saveCacheDetails( Global.getProfile().dataDir);
					}
				}
			}
		}
		super.onEvent(ev);
	}
}
