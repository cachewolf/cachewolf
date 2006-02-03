package CacheWolf;
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
	
	mButton btSwitch;
	String cache = new String();
	String imgLoc = new String();
	int status = 0;
	
	public MapDetailForm(String cacheName, Preferences p){
		cache = cacheName;
		imgLoc = p.mydatadir + "/";
		imgLoc = imgLoc + cache + "_map.gif";
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
					imgLoc = pref.mydatadir + "/";
					imgLoc = imgLoc + cache;
					if(status == 0) {
						imgLoc = imgLoc + "_map_2.gif";
						status = 1;
					} else {
						imgLoc = imgLoc + "_map.gif";
						status = 0;
					}
					setUp(imgLoc, pref);
					this.repaintNow();
				}
		}
	}
}

