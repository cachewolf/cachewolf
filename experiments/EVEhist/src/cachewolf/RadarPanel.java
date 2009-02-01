package cachewolf;
import eve.ui.Button;
import eve.ui.Panel;
import eve.ui.CellPanel;
import eve.ui.CellConstants;
import eve.ui.List;
import eve.ui.DragContext;
import eve.ui.Gui;
import eve.ui.Control;
import java.util.Vector;
import eve.fx.Color;
import eve.fx.Dimension;
import eve.fx.Font;
import eve.fx.FontMetrics;
import eve.fx.Image;
import eve.fx.Graphics;
import eve.fx.Picture;
import eve.fx.Rect;
import eve.fx.Point;

import eve.fx.IconAndText;
import eve.sys.Event;
import eve.sys.Vm;
import eve.sys.Device;
import eve.ui.game.AniImage;
import eve.ui.game.ImageDragContext;
import eve.ui.game.InteractivePanel;
import eve.ui.event.ControlEvent;
import eve.ui.event.PenEvent;

/**
*	The radar panel. Displays the caches around a centre point.<br>
*	Handles scaling as well as toggling the different views in the radar panel.<br>
*	Also handles clicking on a cache.<br>
*/
public class RadarPanel extends CellPanel{
	private Button btMinus = new Button("   -   ");
	private Button btToggle = new Button("Toggle");
	private Button btPlus = new Button("   +   ");
	private int toggleMod = 0; //0 = cacheicons, 1= cacheWP, 2 = cacheNames
	private Vector cacheDB;
	private InterActiveRadarPanel iActP;
	private double scale;
	private int scaleKm = 30;
	private int centerX, centerY;
	private int height, width;
	private static Color RED=new Color(255,0,0);
	private static Color GREEN=new Color(0,255,0);
	private static Color BLUE=new Color(0,0,255);
	private static Color YELLOW=new Color(255,255,0);
	private AniImage circle=null;
	/** Flag to indicate that the background and all caches needs to be redrawn */
	private static boolean redrawCaches=true;
	/**
	* Constructor for the radar panel.
	* Loads images, sets up the interactive panel and
	* "navigation" buttons.
	*/
	public RadarPanel(){
		this.addLast(iActP = new InterActiveRadarPanel(), CellConstants.STRETCH, CellConstants.FILL);
		Panel cp = new Panel();
		cp.stretchFirstRow=true;
		cp.equalWidths=true;
		cp.addNext(btMinus,CellConstants.HSTRETCH, (CellConstants.FILL|CellConstants.WEST));
		cp.addNext(btToggle,CellConstants.HSTRETCH, CellConstants.FILL);
		cp.addLast(btPlus,CellConstants.HSTRETCH, (CellConstants.FILL|CellConstants.EAST));
		this.addLast(cp, CellConstants.HSTRETCH, CellConstants.FILL);
		clearRadarPanel();
		cacheDB=Global.getProfile().cacheDB;
	}

	public void clearRadarPanel() {
		int anz = iActP.images.size();
		for(int i = 0; i<anz;i++){
			iActP.removeImage((AniImage)iActP.images.get(0));
		}
		drawBackground();
		iActP.refresh();
		redrawCaches=true;
	}

	/**
	* Public method to draw the different caches and the
	* radar background
	*/
	public void drawCachesAndCircle(){
		// If there are any images remove them!
		if (redrawCaches) {
			drawCaches();
			redrawCaches=false;
		}
		drawCircle();
		iActP.repaintNow();
	}

	/**
	* Private method to draw the caches.
	*/
	private void drawCaches(){
		Font font = new Font("Gui", Font.BOLD,Global.getPref().fontSize);
		FontMetrics fm = getFontMetrics(font);
		AniImage aImg;
		RadarPanelImage rpi;
		int x,y = 0;
		CacheHolder ch;
		double degrees;
		double pi180=java.lang.Math.PI / 180.0;
		for(int i = cacheDB.size()-1; i >=0 ; i--){
			ch = (CacheHolder)cacheDB.get(i);
			if(!ch.is_filtered && ch.pos.isValid()) {
				degrees = ch.degrees * pi180;
				x = (int) (ch.kilom/scale *  java.lang.Math.sin(degrees));
				y = -(int)(ch.kilom/scale *  java.lang.Math.cos(degrees));
				if(centerX+x>=0 && centerY+y>=0 && centerX+x<=width && centerY+y <= height){
					if (toggleMod>0) {
						String s;
						if (toggleMod==1)
							s=ch.wayPoint;
						else
							s=ch.cacheName;
						if (s.length()>0) {
							int tw;
							Image img = new Image(tw=fm.getTextWidth(s),fm.getHeight());
							Graphics g = new Graphics(img);
							g.setFont(font);
							g.setColor(Color.Black);
							g.fillRect(0,0,tw, fm.getHeight());
							g.setColor(Color.White);
							g.drawText(s, 0,0);
							aImg = new AniImage(img,Color.Black);
							aImg.setLocation(centerX+x+5,centerY+y);
							aImg.properties = eve.fx.Drawing.IsNotHot;
							iActP.addImage(aImg);
						}
					}
					Picture imgCache=CacheType.cache2Img(ch.type);
					// If we have no image for the cache type use a question mark
					if (imgCache==null) imgCache=CacheType.cachePictures[8];
					rpi = new RadarPanelImage(imgCache);
					rpi.wayPoint = ch.wayPoint;
					rpi.rownum = i;
					rpi.setLocation(centerX+x-7,centerY+y-7);
					iActP.addImage(rpi);
				}//if center...
			}// if is_black...
		}
	}

	public void removeCircle() {
		if (circle!=null) iActP.removeImage(circle);
		circle=null;
	}

	private void drawCircle() {
		removeCircle();
		double pi180=java.lang.Math.PI / 180.0;
		CacheHolder ch = (CacheHolder)cacheDB.get(Global.mainTab.tbP.getSelectedCache());
		if(!ch.is_filtered && ch.pos.isValid()) {
			double degrees = ch.degrees * pi180;
			int x = (int) (ch.kilom/scale *  java.lang.Math.sin(degrees));
			int y = -(int)(ch.kilom/scale *  java.lang.Math.cos(degrees));
			Image imgCircle = new Image(20, 20);
			Graphics gCircle = new Graphics(imgCircle);
			gCircle.setColor(Color.Black);
			gCircle.fillRect(0,0,20,20);
			gCircle.setColor(RED);
			gCircle.drawEllipse(0,0, 19,19);
			circle = new AniImage(imgCircle,Color.Black);
			circle.properties = eve.fx.Drawing.IsNotHot;
			circle.setLocation(centerX+x-9,centerY+y-9);
			iActP.addImage(circle);
		}
	}
	/**
	* Private method to draw the black background and green radar.
	* Also calculates some other parameters.
	* Always call this before calling drawCaches().
	*/
	private void drawBackground(){
		width=MyLocale.getScreenWidth();
		height=MyLocale.getScreenHeight();
		Rect r = new Rect(new Dimension(width, height));
		iActP.virtualSize = r;
		iActP.refresh();
		Image img = new Image(width, height);
		Graphics g = new Graphics(img);
		g.setColor(Color.Black);
		//Vm.debug(Convert.toString(height));
		g.fillRect(0,0,width, height);


		if(width < height) {
			scale = (double)scaleKm / (double)height;
		} else {
			scale = (double)scaleKm / (double)width;
		}
		centerX = width / 2;
		centerY = height / 2;
		//centerY = (int)(centerY-centerY*0.15);
		g.setColor(GREEN);
		int radstep= 0, steps=0, radius = 0;

		if(width > height){
			radstep = (int)(10 / scale);
			steps = width / radstep;
		}else{
			radstep = (int)(10 / scale);
			steps = height / radstep;
		}
		for(int i = 1; i <= steps; i++){
			radius = (radstep * i)*2;
			//Vm.debug("Draw: " + Convert.toString(scale));
			g.drawEllipse(centerX-radius/2,centerY-radius/2, radius,radius);
		}
		g.drawLine(centerX,0,centerX,height);
		g.drawLine(0,centerY,width,centerY);

		// Show 1 KM radius only if we have zoomed in (useful for cities with high density of caches)
		if (scaleKm<=20) {
			g.setColor(YELLOW); // Yellow for 1km circle
			radius = radstep/5;
			g.drawEllipse(centerX-radius/2,centerY-radius/2, radius,radius);
			g.free();
		}
		AniImage aImg = new AniImage(img);
		iActP.backgroundImage = img;
		aImg.refresh();
	}

	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btPlus){
				if (scaleKm>10) scaleKm = scaleKm - 10;
				else if (scaleKm==10) scaleKm=5;
				else if (scaleKm==5) scaleKm=2;
				else scaleKm=1;
				clearRadarPanel();
				drawCachesAndCircle();
			}
			if (ev.target == btMinus){
				if (scaleKm==1) scaleKm=2;
				else if(scaleKm==2) scaleKm=5;
				else if(scaleKm==5) scaleKm=10;
				else scaleKm = scaleKm + 10;
				clearRadarPanel();
				drawCachesAndCircle();
			}
			if (ev.target == btToggle){
				toggleMod++;
				if(toggleMod > 2) toggleMod = 0;
				clearRadarPanel();
				drawCachesAndCircle();
			}
		}

	}

	public void resizeTo(int width, int height) {
		//eve.sys.Vm.debug("RadarP: resize: "+width+","+height);
		iActP.origin.x=(MyLocale.getScreenWidth()-width)/2;
		iActP.origin.y=(MyLocale.getScreenHeight()- height)/2;
		repaint();
		super.resizeTo(width, height);
	}

//################################################################################
//   InteractiveRadarPanel
//################################################################################

	/**
	*	This class allows handling of a user click on a cache
	*	in the radar panel.
	*	@see RadarPanel
	*/
	private static class InterActiveRadarPanel extends InteractivePanel{
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
			if (timePenOff-timePenOn<500 || !Device.isMobile()) {
				if(which instanceof RadarPanelImage){
					RadarPanelImage ich = (RadarPanelImage)which;
					Global.mainTab.clearDetails();
					Global.mainTab.selectAndActive(ich.rownum);
				}
			} else {
				if (imgInfo!=null) clearInfo();
			}
		}

		public boolean imageMovedOn(AniImage which) {
			timePenOn=Vm.getTimeStampLong();
			setFont(font);
			RadarPanelImage imgRP=(RadarPanelImage) which;
			java.util.Vector cacheDB=Global.getProfile().cacheDB;
			CacheHolder ch=(CacheHolder) cacheDB.get(imgRP.rownum);
			String s=ch.wayPoint+"  "+ch.getCacheSize()+" / "+strDifficulty+"="+ch.hard+"  "+strTerrain+"="+ch.terrain;
			String s1=ch.cacheName;
			if (s1.length()>40) s1=s1.substring(0,40);
			int tw=fm.getTextWidth(s)+2;
			int tw1=fm.getTextWidth(s1)+2;
			if (tw1>tw) tw=tw1;
			int h=fm.getHeight();
			Image img = new Image(tw,h+h);
			Graphics g = new Graphics(img);
			g.setFont(font);
			g.setColor(BLUE);
			g.fillRect(0,0,tw, h+h);
			g.setColor(Color.White);
			g.drawText(s, 1,1);
			g.drawText(s1,1,h);
			imgInfo = new AniImage(img);
			Rect r=getVisibleArea(null);
			imgInfo.setLocation(r.x,r.y); // Place the info at top left corner
			imgInfo.properties = eve.fx.Drawing.IsNotHot;
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
				// The next line is needed due to a bug in eve (it does not call penReleased)
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
				 icnDrag.addColumn( CacheType.cache2Img(ch.type));
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
				java.util.Vector cacheDB=Global.getProfile().cacheDB;
				CacheHolder ch=(CacheHolder) cacheDB.get(imgRP.rownum);
				wayPoint=ch.wayPoint;

				int tw,th;
				Image img = new Image(tw=fm.getTextWidth(wayPoint)+17,th=fm.getHeight()>15?fm.getHeight():15);
				Graphics g = new Graphics(img);
				g.setFont(font);
				g.setColor(Color.White);
				g.fillRect(0,0,tw, th);
				g.setColor(RED);
				g.drawText(wayPoint, 15,1);
				which.draw(g,0,0,0);
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
			 Point p = Gui.getPosInParent(this,getWindow(),null);
			 p.x += drag.curPoint.x-origin.x;
			 p.y += drag.curPoint.y-origin.y;
			 Control c = getWindow().findChild(p.x,p.y);
		     if (c instanceof List && c.text.equals("CacheList")) {
		    	 if (Global.mainForm.cacheList.addCache(wayPoint)) {
		    		 c.repaintNow();
		    		 ((List) c).makeItemVisible(((List)c).itemsSize()-1);
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
			 return false;
		 }
	}

//################################################################################
//  RadarPanelImage
//################################################################################

	/**
	* The ImagePanelImage extends AniImage by a fileName.
	* This is an easy way to identify the image clicked,
	* what is needed to display the full image from the
	* thumbnail.
	*/
	public class RadarPanelImage extends AniImage{
		public String wayPoint = "";
		public int rownum;

		public RadarPanelImage(Picture i){
			super(i);
		}
	}



}
