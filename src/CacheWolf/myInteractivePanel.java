package CacheWolf;
import ewe.graphics.*;
import ewe.ui.*;
import ewe.sys.*;
import ewe.fx.*;
import ewe.graphics.*;

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
	Font font = new Font("Verdana", Font.BOLD, 10);
	FontMetrics fm = getFontMetrics();
	long timePenOn=0;
	AniImage imgInfo;
	String strDifficulty=MyLocale.getMsg(1120,"Diff");
	String strTerrain=MyLocale.getMsg(1121,"Terr");
	
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
		RadarPanelImage imgRP=(RadarPanelImage) which;
		ewe.util.Vector cacheDB=Global.getProfile().cacheDB;
		CacheHolder ch=(CacheHolder) cacheDB.get(imgRP.rownum);
		String s=ch.wayPoint+"  "+ch.CacheSize+" / "+strDifficulty+"="+ch.hard+"  "+strTerrain+"="+ch.terrain;
		int tw;
		Image img = new Image(tw=fm.getTextWidth(s)+2,fm.getHeight()+2);
		Graphics g = new Graphics(img);
		g.setColor(new Color(0,0,255));
		g.fillRect(0,0,tw, fm.getHeight()+2);
		g.setColor(Color.White);
		g.drawText(s, 1,1);
		imgInfo = new AniImage(img);
		Rect r=getVisibleArea(null);
		imgInfo.setLocation(r.x,r.y); // Place the info at top left corner
		imgInfo.properties = mImage.IsNotHot;
		addImage(imgInfo);
		refreshOnScreen(imgInfo);
		return true;
	}
	public boolean imageMovedOff(AniImage which) {
		clearInfo();
		return true;
	}
	public void onPenEvent(PenEvent ev) {
		super.onPenEvent(ev);
		if (ev.type==PenEvent.PEN_UP) clearInfo();
	}
	/*
	public void onEvent(Event ev){
		BufferedGraphics bfg;
		Graphics g;
		if(ev instanceof PenEvent && ev.type == PenEvent.PEN_DRAG){
			PenEvent pev = (PenEvent)ev;
			if(penMoving == false){
				penMoving = true;
				x1 = pev.x;
				y1 = pev.y;
				x2 = x1;
				y2 = y1;
				Vm.debug("Pen starting");
			} else {
				bfg = new BufferedGraphics(this.getGraphics(), new Rect(new Dimension(50,50)));
				g = bfg.getGraphics();
				g.setDrawOp(Graphics.DRAW_XOR);
				g.setPen(new Pen(RED,Pen.SOLID,1));
				g.drawRect(x1,y1,20,20);
				bfg.release();
				x2 = pev.x;
				y2 = pev.y;
				bfg = new BufferedGraphics(this.getGraphics(), new Rect(new Dimension(50,50)));
				g = bfg.getGraphics();
				g.setDrawOp(Graphics.DRAW_XOR);
				g.setPen(new Pen(RED,Pen.SOLID,1));
				g.drawRect(x1,y1,x2-x1,y2-y1);
				bfg.release();
				Vm.debug("Pen moving");
			}
			
		}
		super.onEvent(ev);
	}
	*/
}
