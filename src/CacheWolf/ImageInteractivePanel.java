package CacheWolf;
import ewe.graphics.*;
import ewe.sys.*;
import ewe.fx.*;
import ewe.ui.*;
import ewe.util.*;

/**
*	This class handles the resizing im images
*/
public class ImageInteractivePanel extends InteractivePanel{
	int origH, origW;
	int state = 0; // 0 = nothing, -1 = scaled to app, 1 = scaled to original size
	int scaleX = 0, scaleY = 0;
	ScrollBarPanel scp;
	String imgLoc = new String();
	private Menu mClose = new Menu(new String[]{
			"Close"},"");
	
	public void setParams(int state, int scaleX, int scaleY, int origH, int origW, ScrollBarPanel sp, String loc){
		imgLoc = loc;
		this.state = state;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.origH = origH;
		this.origW = origW;
		scp = sp;
		setMenu(mClose);
	}
	
	public void imageClicked(AniImage which, Point pos){
		if(state == -1){
			//Vm.debug("Hit and state -1!");
			this.removeImage(which);
			mImage mI = new mImage(imgLoc);
			//this.refresh();
			which = new AniImage(mI);
			this.addImage(which);
			//this.setPreferredSize(origW, origH);
			this.repaintNow();
			scp.repaintNow();
		}
		if(state == 1){
			//Vm.debug("Hit and state 1!");
			this.removeImage(which);
			//this.refresh();
			which = new AniImage(which.scale(scaleX,scaleY,null,0));
			this.addImage(which);
			//this.setPreferredSize(b,h);
			this.repaintNow();
			scp.repaintNow();
		}
		state = -state;
	}
	public void penRightReleased(Point p){
			menuState.doShowMenu(p,true,null); // direct call (not through doMenu) is neccesary because it will exclude the whole table
	}
	public void penHeld(Point p){
			menuState.doShowMenu(p,true,null); 
	}
	public void popupMenuEvent(Object selectedItem){
		postEvent(new ControlEvent(ControlEvent.EXITED,this));
	}
}
