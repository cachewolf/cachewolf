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
	GotoPanel gotoPanel;
	AniImage statusImageHaveSignal = new AniImage("center_green.png");
	AniImage statusImageNoSignal = new AniImage("center_yellow.png");
	AniImage statusImageNoGps = new AniImage("center.png");
	AniImage arrowUp = new AniImage("arrow_up.png");
	AniImage arrowDown = new AniImage("arrow_down.png");
	AniImage arrowLeft = new AniImage("arrow_left.png");
	AniImage arrowRight = new AniImage("arrow_right.png");
	
	public MovingMap(Preferences pref, Vector maps, GotoPanel gP){
		gotoPanel = gP;
		this.maps = maps;
		this.pref = pref;
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		this.title = "Moving Map";
		mmp = new MovingMapPanel(this, maps, gotoPanel);
		this.addLast(mmp);
		//Create index of all world files
		//Create form
		if(gP.toPoint.latDec == 0 && gP.toPoint.latDec == 0 && maps.size()>0){
			try{
				// GPS has been switched on
				//This means we display the correct map if we have a fix
				if(gotoPanel.displayTimer != 0){
					ListBox l = new ListBox(maps, true, null);
					l.execute();
				}else{ //Default: display the first map in the list.
					MapInfoObject mo = (MapInfoObject)maps.get(0);
					mapImage = new AniImage(mo.fileName);
					this.setTitle = "Mov. Map: " + mo.mapName;
					mapImage.setLocation(0,0);
					mmp.addImage(mapImage);
					statusImageNoGps.setLocation(10,10);
					statusImageNoGps.properties = AniImage.AlwaysOnTop;
					arrowUp.setLocation(pref.myAppWidth/2, 10);
					arrowDown.setLocation(pref.myAppWidth/2, pref.myAppHeight-20);
					arrowLeft.setLocation(10, pref.myAppHeight/2+7);
					arrowRight.setLocation(pref.myAppWidth-25, pref.myAppHeight/2+7);
					arrowUp.properties = AniImage.AlwaysOnTop;
					arrowDown.properties = AniImage.AlwaysOnTop;
					arrowLeft.properties = AniImage.AlwaysOnTop;
					arrowRight.properties = AniImage.AlwaysOnTop;
					mmp.addImage(arrowUp);
					mmp.addImage(arrowDown);
					mmp.addImage(arrowLeft);
					mmp.addImage(arrowRight);
					mmp.addImage(statusImageNoGps);
				}
			}catch (Exception ex){
				Vm.debug("Problem loading map image file!");
			}
		}
		
	}
	
	/**
	* Method to reset the position of the moving map.
	*/
	public void updatePosition(){
	}
}

/**
*	Class to display the map bitmap and to select another bitmap to display.
*/
class MovingMapPanel extends InteractivePanel{
	MovingMap mm;
	Vector maps;
	CellPanel gotoPanel;
	AniImage mapImage;
	public MovingMapPanel(MovingMap f, Vector maps, GotoPanel gP){
		gotoPanel = gP;
		this.mm = f;
		this.maps = maps;
	}
	
	/**
	*	Method to react to user.
	*/
	public void imageClicked(AniImage which, Point pos){
		if(which == mm.statusImageNoGps){
			ListBox l = new ListBox(maps, false, null);
			l.execute();
			if(l.selected == true){
				this.removeImage(mapImage);
				try{
					mapImage = new AniImage(l.selectedMap.fileName);
					mapImage.setLocation(-100,-100);
					this.addImage(mapImage);
				}catch (Exception ex){
					Vm.debug("Problem loading map image file!");
				}
				this.repaintNow();
			}
		}
		if(which == mm.arrowRight){
		}
		if(which == mm.arrowLeft){
		}
		if(which == mm.arrowDown){
		}
		if(which == mm.arrowUp){
		}
	}
}

/**
*	Class to display maps to choose from
*/
class ListBox extends Form{
	public MapInfoObject selectedMap = new MapInfoObject();
	mButton cancelButton, okButton;
	mList list = new mList(4,1,false);
	public boolean selected = false;
	Vector maps;
	
	public ListBox(Vector maps, boolean showInBoundOnly, CWPoint position){
		this.title = "Maps";
		this.setPreferredSize(200,100);
		this.maps = maps;
		MapInfoObject map;
		ScrollBarPanel scb;
		for(int i = 0; i<maps.size();i++){
			map = new MapInfoObject();
			map = (MapInfoObject)maps.get(i);
			if(showInBoundOnly == true) {
				if(map.inBound == true) list.addItem(i + ": " + map.mapName);
			} else list.addItem(i + ": " + map.mapName);
		}
		this.addLast(scb = new ScrollBarPanel(list),this.STRETCH, this.FILL);
		this.addNext(cancelButton = new mButton("Cancel"),this.STRETCH, this.FILL);
		this.addLast(okButton = new mButton("Select"),this.STRETCH, this.FILL);
	}
	
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelButton){
				this.close(0);
			}
			if (ev.target == okButton){
				int i,mapNum = 0;
				String it = new String();
				it = list.getText();
				it = it.substring(0,it.indexOf(':'));
				mapNum = Convert.toInt(it);
				Vm.debug("Kartennummer: " + mapNum);
				selectedMap = (MapInfoObject)maps.get(mapNum);
				selected = true;
				this.close(0);
			}
		}
		super.onEvent(ev);
	}
}