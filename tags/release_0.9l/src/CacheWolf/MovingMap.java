package CacheWolf;

import ewe.ui.*;
import ewe.graphics.*;
import ewe.sys.*;
import ewe.fx.*;
import ewe.util.Vector;

/**
*	Class to handle a moving map.
*/
public class MovingMap extends Form{
	Preferences pref;
	MovingMapPanel mmp;
	AniImage mapImage;
	Vector maps;
	AniImage statusImageHaveSignal = new AniImage("center_green.png");
	AniImage statusImageNoSignal = new AniImage("center_yellow.png");
	public MovingMap(Preferences pref, Vector maps){
		this.maps = maps;
		this.pref = pref;
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		this.title = "Moving Map";
		//KHF to get a clean compile, class MovingMapPanel doesn't exist yet
		//mmp = new MovingMapPanel(this, maps);
		//Create index of all world files
		//Create form
		try{
			mapImage = new AniImage("F:/EWE/maps/muc_01.png");
		}catch (Exception ex){
			Vm.debug("Problem loading map image file!");
		}
		mapImage.setLocation(-100,-100);
		mmp.addImage(mapImage);
		this.addLast(mmp);
	}
	
	
}

class MovingMapPanel extends InteractivePanel{
	MovingMap mm;
	Vector maps;
	CellPanel gotoPanel;
	public MovingMapPanel(MovingMap f, Vector maps, CellPanel gP){
		gotoPanel = gP;
		this.mm = f;
		this.maps = maps;
	}
	
	public void imageClicked(AniImage which, Point pos){
		ListBox l = new ListBox(maps, false);
		l.execute();
		/*
		for(int i = -100; i<50;i++){
			which.setLocation(i,-100);
			which.refresh();
		}
		*/
	}
}

/**
*	Class to display maps to choose from
*/
class ListBox extends Form{
	
	public ListBox(Vector maps, boolean showInBoundOnly){
		this.title = "Maps";
		this.setPreferredSize(200,100);
		mList list = new mList(4,1,false);
		MapInfoObject map;
		ScrollBarPanel scb;
		for(int i = 0; i<maps.size();i++){
			map = new MapInfoObject();
			map = (MapInfoObject)maps.get(i);
			if(showInBoundOnly == true) {
				if(map.inBound == true) list.addItem(map.mapName);
			} else list.addItem(map.mapName);
		}
		this.addLast(scb = new ScrollBarPanel(list),this.STRETCH, this.FILL);
	}
}