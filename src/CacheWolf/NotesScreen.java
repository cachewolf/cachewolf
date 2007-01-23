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
	CacheHolder thisCache = new CacheHolder();
	mButton addDateTime, btSave;
	Profile profile;
	
	public NotesScreen(CacheHolder ch, Profile profile){
		this.title = "Notes";
		this.setPreferredSize(Global.getPref().myAppWidth, Global.getPref().myAppHeight);
		thisCache = ch;
		this.profile = profile;
		mImage mI3 = new mImage("date_time.png");
		addDateTime = new mButton((IImage)mI3);
		CellPanel cp = new CellPanel();
		btSave = new mButton("Save");
		cp.addNext(addDateTime);
		cp.addLast(btSave);
		ScrollBarPanel sbp = new ScrollBarPanel(wayNotes);
		wayNotes.setText(thisCache.CacheNotes);
		//this.addLast(sbp, this.STRETCH, this.FILL);
		this.addLast(sbp.setTag(Control.SPAN, new Dimension(3,1)),CellConstants.STRETCH, (CellConstants.FILL|CellConstants.WEST));
		this.addLast(cp);
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
				thisCache.saveCacheDetails( profile.dataDir);
				this.close(0);
			}
		}
		super.onEvent(ev);
	}
}
