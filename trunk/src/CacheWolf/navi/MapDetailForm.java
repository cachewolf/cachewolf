package CacheWolf.navi;
import CacheWolf.ImageDetailForm;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import ewe.graphics.*;
import ewe.sys.*;
import ewe.fx.*;
import ewe.ui.*;
import ewe.util.*;

/**
 *	Class to display map images in different zoom levels.
 *	Extends ImageDetailForm that handles the resizing to screen
 *	size. The extension in this class handles the switching between
 *	different (map) images.
 */
public class MapDetailForm extends ImageDetailForm {
 /* // it's obsolete since we have a well working moving map 
	mButton btSwitch;
	String cache;
	String imgLoc = new String();
	int status = 0;
	Profile profile;
	
	public MapDetailForm(String cacheName, Preferences p, Profile prof){
		profile=prof;  // keep ref for later use
		cache=cacheName;
		imgLoc = prof.dataDir + cacheName + "_map.gif";
		scp = new ScrollBarPanel(ipp);
		setUp(imgLoc, p);
		this.title = "Maps";
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		this.addLast(scp.getScrollablePanel(), this.STRETCH, this.FILL);
		CellPanel butPanel = new CellPanel();
		btSwitch = new mButton("Zoom");
		butPanel.addLast(btSwitch);
		this.addLast(butPanel, this.HSTRETCH, this.FILL);
	}

	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btSwitch){
				ipp.removeImage(ai);
				imgLoc = profile.dataDir + cache;
				if(status == 0) {
					imgLoc = imgLoc + "_map_2.gif";
					status = 1;
				} else {
					imgLoc = imgLoc + "_map.gif";
					status = 0;
				}
				try {
					setUp(imgLoc, pref);
					this.repaintNow();
				} catch (IllegalArgumentException e) {
					MessageBox tmp = new MessageBox(MyLocale.getMsg(321,"Fehler"), MyLocale.getMsg(322,"Kann Bild/Karte nicht finden")+": "+imgLoc, MessageBox.OKB); // @todo: language support
					tmp.exec();
				}
			}
		}
	}
	*/
}

