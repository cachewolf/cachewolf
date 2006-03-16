package CacheWolf;
import ewe.graphics.*;
import ewe.ui.*;
import ewe.sys.*;
import ewe.fx.*;

/**
*	This class allows handling of a user click on a cache
*	in the radar panel.
*	@see RadarPanel
*/
public class myInteractivePanel extends InteractivePanel{

	MainTab mt;
	
	public void imageClicked(AniImage which, Point pos){
		String fn = new String();
		if(which instanceof RadarPanelImage){
			RadarPanelImage ich = (RadarPanelImage)which;
			mt.selectAndActive(ich.rownum);
		}
	}
	
	public void setMainTab(MainTab tb){
		mt = tb;
	}
}
