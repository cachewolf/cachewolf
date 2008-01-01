package CacheWolf;
import ewe.graphics.*;
import ewe.ui.*;
import ewe.util.Vector;
import ewe.sys.*;
import ewe.fx.*;

/**
*	This class allows handling of a user click on a cache
*	in the radar panel.
*	@see RadarPanel
*/
public class myInteractivePanel extends InteractivePanel{

	MainTab mt;
	
	boolean penMoving = false;
	int x1,y1,x2,y2 = 0;
	static Color RED = new Color(255,0,0);
	Font font = new Font("gui", Font.BOLD,Global.getPref().fontSize);
	FontMetrics fm = getFontMetrics(font);
	long timePenOn=0;
	AniImage imgInfo;
	String strDifficulty=MyLocale.getMsg(1120,"Diff");
	String strTerrain=MyLocale.getMsg(1121,"Terr");
	AniImage imgDrag; // Allows the dragging of the cache into the cachelist
	boolean canScroll=true;
	
	private void clearInfo() {
		removeImage(imgInfo);
		imgInfo=null;
		refresh();
		onImage=null;
	}
	
	public void imageClicked(AniImage which, Point pos){
		long timePenOff=Vm.getTimeStampLong();
		// If the pen rested more than 500 msec, we only display the info and don't treat it as a click
		if (timePenOff-timePenOn<500 || !Vm.isMobile()) { 
			String fn = new String();
			if(which instanceof RadarPanelImage){
				RadarPanelImage ich = (RadarPanelImage)which;
				mt.selectAndActive(ich.rownum);
			}
		} else {
			if (imgInfo!=null) clearInfo(); 
		}
	}
	
	public void setMainTab(MainTab tb){
		mt = tb;
	}
	public boolean imageMovedOn(AniImage which) {
		timePenOn=Vm.getTimeStampLong();
		setFont(font);
		RadarPanelImage imgRP=(RadarPanelImage) which;
		ewe.util.Vector cacheDB=Global.getProfile().cacheDB;
		CacheHolder ch=(CacheHolder) cacheDB.get(imgRP.rownum);
		String s=ch.wayPoint+"  "+ch.CacheSize+" / "+strDifficulty+"="+ch.hard+"  "+strTerrain+"="+ch.terrain;
		String s1=ch.CacheName;
		if (s1.length()>40) s1=s1.substring(0,40);
		int tw=fm.getTextWidth(s)+2;
		int tw1=fm.getTextWidth(s1)+2;
		if (tw1>tw) tw=tw1;
		int h=fm.getHeight();
		Image img = new Image(tw,h+h);
		Graphics g = new Graphics(img);
		g.setFont(font);
		g.setColor(new Color(0,0,255));
		g.fillRect(0,0,tw, h+h);
		g.setColor(Color.White);
		g.drawText(s, 1,1);
		g.drawText(s1,1,h);
		imgInfo = new AniImage(img);
		Rect r=getVisibleArea(null);
		imgInfo.setLocation(r.x,r.y); // Place the info at top left corner
		imgInfo.properties = mImage.IsNotHot;
		addImage(imgInfo);
		refreshOnScreen(imgInfo);
		imgDrag=which;
	return true;
	}
	public boolean imageMovedOff(AniImage which) {
		clearInfo();
		return true;
	}
	public void onPenEvent(PenEvent ev) {
		super.onPenEvent(ev);
		if (ev.type==PenEvent.PEN_UP) {
			clearInfo();
			// The next line is needed due to a bug in EWE (it does not call penReleased)
			if (isDragging) penReleased(new Point(ev.x,ev.y));
		}
	}
	
    ///////////////////////////////////////////////////
	//  Allow the caches to be dragged into a cachelist
    ///////////////////////////////////////////////////
	
	String wayPoint;
	
	public void startDragging(DragContext dc) {
		if (!Global.mainForm.cacheListVisible) return;
		Vector cacheDB=Global.getProfile().cacheDB;
//Vm.debug("myIAP startDrag "+dc.start.x+"/"+dc.start.y);
		int idx=Global.getProfile().getCacheIndex(wayPoint); 
		if (idx>=0) {
			 CacheHolder ch=(CacheHolder) cacheDB.get(idx);
			 //wayPoint=ch.wayPoint;
			 //Vm.debug("Waypoint : "+ch.wayPoint);
			 IconAndText icnDrag=new IconAndText();
			 icnDrag.addColumn((IImage) Global.mainTab.tbP.myMod.cacheImages[Convert.parseInt(ch.type)]);
			 icnDrag.addColumn(ch.wayPoint);
			 dc.dragData=dc.startImageDrag(icnDrag,new Point(8,8),this);
			 //if (dc instanceof ImageDragContext) Vm.debug(">>>>Is Image drag");
			 canScroll=false;
		}
	 }

	 public void stopDragging(DragContext dc) {		 
		canScroll=true;
	 }
	 public void draggingStarted(ImageDragContext dc) {}
	 public void draggingStopped(ImageDragContext dc) {}
	 
	 public boolean imageBeginDragged(AniImage which,Point pos) {
		if (!Global.mainForm.cacheListVisible) return false;
		canScroll=false;
		clearInfo();
		wayPoint=null;
		AniImage dragImage=null;
		if (which instanceof RadarPanelImage) {
			RadarPanelImage imgRP=(RadarPanelImage) which;
			ewe.util.Vector cacheDB=Global.getProfile().cacheDB;
			CacheHolder ch=(CacheHolder) cacheDB.get(imgRP.rownum);
			wayPoint=ch.wayPoint;
			
			int tw,th;
			Image img = new Image(tw=fm.getTextWidth(wayPoint+15),th=fm.getHeight()>15?fm.getHeight():15);
			Graphics g = new Graphics(img);
			g.setFont(font);
			g.setColor(Color.White);
			g.fillRect(0,0,tw, th);
			g.setColor(new Color(255,0,0));
			g.drawText(wayPoint, 15,1);
			g.drawImage(which.image,0,0);
			dragImage=new AniImage(img);
			dragImage.properties|=AniImage.IsMoveable;
			dragImage.setLocation(pos.x,pos.y);
		}
		return super.imageBeginDragged(dragImage,pos);
	 }

	 public boolean imageDragged(ImageDragContext drag, Point pos) {
		 	if (drag.image!=null) {
/*			    Point p = Gui.getPosInParent(this,getWindow());
			 	p.x += pos.x-origin.x;
			 	p.y += pos.y-origin.y;
			 	Control c = getWindow().findChild(p.x,p.y);
*/
				drag.clearPendingDrags();
		 	}
		 	return super.imageDragged(drag,pos);
	 }
	 
	 public boolean imageNotDragged(ImageDragContext drag, Point pos) {
		if (drag.image!=null) {
			images.remove(drag.image);
			drag.image=null;
			refresh();
		}			
		 Point p = Gui.getPosInParent(this,getWindow());
		 p.x += drag.curPoint.x-origin.x;
		 p.y += drag.curPoint.y-origin.y;
		 Control c = getWindow().findChild(p.x,p.y);
	     if (c instanceof mList && c.text.equals("CacheList")) {
	    	 if (Global.mainForm.cacheList.addCache(wayPoint)) {
	    		 c.repaintNow();
	    		 ((mList) c).makeItemVisible(((mList)c).itemsSize()-1);
	    	 }
	     }
		 return false; 
	 }
	 
	 public boolean canScreenScroll() {
		 return canScroll;
	 }
	 public boolean scroll(int dx,int dy,Point moved) {
		 if (canScroll)
			 return super.scroll(dx,dy,moved);
		 else
			 return false;
	 }
}
