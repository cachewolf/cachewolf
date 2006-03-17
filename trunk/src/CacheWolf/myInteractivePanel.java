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
