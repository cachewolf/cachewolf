package CacheWolf;
import ewe.ui.*;
import ewe.fx.*;
import ewe.graphics.*;

/**
*	The radar panel. Displays the caches around a centre point.<br>
*	Handles scaling as well as toggling the different views in the radar panel.<br>
*	Also handles clicking on a cache.<br>
*	Class ID=500
*/
public class RadarPanel extends CellPanel{
	mButton btMinus = new mButton("   -   ");
	mButton btToggle = new mButton("Toggle");
	mButton btPlus = new mButton("   +   ");
	int toggleMod = 0; //0 = cacheicons, 1= cacheWP, 2 = cacheNames
	Preferences pref;
	CacheDB cacheDB;
	myInteractivePanel iActP;
	double scale;
	int scaleKm = 30;
	int centerX, centerY;
	int height, width;
	CacheHolder selectedWaypoint = null;
	MainTab mt;
	boolean penMoving = false;
	int x1,y1,x2,y2 = 0;
	boolean reCenterImage=true;
	/**
	* Constructor for the radar panel.
	* Loads images, sets up the interactive panel and
	* "navigation" buttons.
	*/
	public RadarPanel(){
		this.addLast(iActP = new myInteractivePanel(), CellConstants.STRETCH, CellConstants.FILL);
		CellPanel cp = new CellPanel();
		cp.addNext(btMinus,CellConstants.HSTRETCH, (CellConstants.FILL|CellConstants.WEST));
		cp.addNext(btToggle,CellConstants.HSTRETCH, CellConstants.FILL);
		cp.addLast(btPlus,CellConstants.HSTRETCH, (CellConstants.FILL|CellConstants.EAST));
		this.addLast(cp, CellConstants.HSTRETCH, CellConstants.FILL);
	}
	
	public void setMainTab(MainTab tb){
		mt = tb;
		iActP.setMainTab(tb);
	}
	
	/**
	* Informs the radar panel on preferences and currently loaded cache
	* database. It also calculates the maximum size available for drawing 
	* the radar.
	*/
	public void setParam(Preferences p, CacheDB db, CacheHolder sWp){
		selectedWaypoint = sWp;
		pref = p;
		cacheDB = db;
		height = (pref.myAppHeight)*6/5; // add 10% each at top/bottom 
		//height = (int)height * 2;
		////Vm.debug("Height: " + Convert.toString(height));
		////Vm.debug("App Height: " + Convert.toString(pref.myAppHeight));
		width = (pref.myAppWidth)*6/5;
		//width = (int)width * 2;
	}
	
	// Call this after the centre has changed to re-center the radar panel
	public void recenterRadar() {
		reCenterImage=true;
	}
	
	/**
	* Public method to draw the different caches and the
	* radar background
	*/
	public void drawThePanel(){
		// If there are any images remove them!
		int anz = iActP.images.size();
		for(int i = 0; i<anz;i++){
			iActP.removeImage((AniImage)iActP.images.get(0));
		}
		iActP.refresh();
		drawBackground();
		drawCaches();
		iActP.repaintNow();
		if (reCenterImage) {
			// Hack to scroll to left origin for a defined position for subsequent
			// scroll which centers the image
			iActP.scroll(-1000,-1000); 
			Dimension dispSize=getDisplayedSize(null);
			iActP.scroll((width-dispSize.width)/2,(height-dispSize.height+btMinus.getSize(null).height)/2);
			reCenterImage=false;
		}
	}
	
	/**
	* Private method to draw the caches.
	*/
	private void drawCaches(){
		Font radarFont = new Font("Gui", Font.BOLD,Global.getPref().fontSize);
		FontMetrics fm = getFontMetrics(radarFont);
		AniImage aImg;
		RadarPanelImage rpi;
		int drX,drY = 0;
		CacheHolder holder;
		double degrees;
		double pi180=java.lang.Math.PI / 180.0;
		for(int i = cacheDB.size()-1; i >=0 ; i--){
			holder = cacheDB.get(i);
			if(holder.isVisible() && holder.pos.isValid()) {
				degrees = holder.degrees * pi180;
				drX =new Float(holder.kilom/scale *  java.lang.Math.sin(degrees)).intValue();
				drY = -new Float(holder.kilom/scale *  java.lang.Math.cos(degrees)).intValue();
				if(centerX+drX>=0 && centerY+drY>=0 && centerX+drX<=width && centerY+drY <= height){
					if (toggleMod>0) {
						String s;
						if (toggleMod==1)
							s=holder.getWayPoint();
						else
							s=holder.getCacheName();
						if (s.length()>0) { 
							int tw;
							Image img = new Image(tw=fm.getTextWidth(s),fm.getHeight());
							Graphics g = new Graphics(img);
							g.setFont(radarFont);
							g.setColor(Color.Black);
							g.fillRect(0,0,tw, fm.getHeight());
							g.setColor(Color.White);
							g.drawText(s, 0,0);
							aImg = new AniImage(img);
							aImg.setLocation(centerX+drX+5,centerY+drY);
							aImg.transparentColor = Color.Black;
							aImg.properties = mImage.IsNotHot;
							iActP.addImage(aImg);
						}
					}
					Image imgCache=GuiImageBroker.getTypeImage(holder.getType(),true);
					// If we have no image for the cache type use a question mark
					if (imgCache==null) imgCache=GuiImageBroker.getTypeImage(CacheType.CW_TYPE_UNKNOWN,true); 
					rpi = new RadarPanelImage(imgCache);
					rpi.wayPoint = holder.getWayPoint();
					rpi.rownum = i;
					rpi.setLocation(centerX+drX-7,centerY+drY-7);
					iActP.addImage(rpi);
					if(holder == selectedWaypoint){ // Draw red circle around selected wpt
						Image imgCircle = new Image(20, 20);
						Graphics gCircle = new Graphics(imgCircle);
						gCircle.setColor(Color.Black);
						gCircle.fillRect(0,0,20,20);
						gCircle.setColor(new Color(255,0,0));
						gCircle.drawEllipse(0,0, 20,20);
						aImg = new AniImage(imgCircle);
						aImg.setLocation(centerX+drX-9,centerY+drY-9);
						aImg.transparentColor = new Color(0,0,0);
						aImg.properties = mImage.IsNotHot;
						iActP.addImage(aImg);
					}
				}//if center...
			}// if is_black...
		}
	}
	
	/**
	* Private method to draw the black background and green radar.
	* Also calculates some other parameters.
	* Always call this before calling drawCaches().
	*/
	private void drawBackground(){
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
		centerX = (width / 2);
		centerY = (height / 2);
		//centerY = (int)(centerY-centerY*0.15);
		g.setColor(new Color(0,255,0));
		int radstep= 0, steps=0, radius = 0;
		
		if(width > height){
			radstep = (int)(10 / scale);
			steps = (width / radstep);
		}else{
			radstep = (int)(10 / scale);
			steps = (height / radstep);
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
			g.setColor(new Color(255,255,0)); // Yellow for 1km circle
			radius = radstep/5;
			g.drawEllipse(centerX-radius/2,centerY-radius/2, radius,radius);
			g.free();
		}	
		AniImage aImg = new AniImage(img);
		//iActP.addImage(aImg);
		iActP.backgroundImage = img;
		int xPos = (pref.myAppWidth/2 - width/2);
		aImg.setLocation(xPos,0);
		aImg.refresh();
	}
	
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btPlus){
				if (scaleKm>10) scaleKm = scaleKm - 10;
				else if (scaleKm==10) scaleKm=5;
				else if (scaleKm==5) scaleKm=2;
				else scaleKm=1;
				drawThePanel();
			}
			if (ev.target == btMinus){
				if (scaleKm==1) scaleKm=2;
				else if(scaleKm==2) scaleKm=5;
				else if(scaleKm==5) scaleKm=10;
				else scaleKm = scaleKm + 10;
				drawThePanel();
			}
			if (ev.target == btToggle){
				toggleMod++;
				if(toggleMod > 2) toggleMod = 0;
				drawThePanel();
			}
		}

	}
}
