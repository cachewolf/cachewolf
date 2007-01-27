package CacheWolf;
import ewe.ui.*;
import ewe.util.*;
import ewe.fx.*;
import ewe.sys.*;
import ewe.graphics.*;

/**
*	The radar panel. Displays the caches around a center point.<br>
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
	Vector cacheDB;
	myInteractivePanel iActP;
	double scale;
	int scaleKm = 30;
	int centerX, centerY;
	int height, width;
	Image cacheImages[] = new Image[454];
	String selectedWaypoint = new String();
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
		cacheImages[0] = new Image("0.png");
		//cacheImages[1] = new Image();
		cacheImages[2] = new Image("2.png");
		cacheImages[3] = new Image("3.png");
		cacheImages[4] = new Image("4.png");
		cacheImages[5] = new Image("5.png");
		cacheImages[6] = new Image("6.png");
		cacheImages[8] = new Image("8.png");
		//cacheImages[9] = new Image();
		//cacheImages[10] = new Image();
		cacheImages[11] = new Image("11.png");
		cacheImages[12] = new Image("12.png");
		cacheImages[13] = new Image("13.png");
		//additional waypoints, begin with 50
		cacheImages[50] = new Image("pkg.png");
		cacheImages[51] = new Image("stage.png");
		cacheImages[52] = new Image("puzzle.png");
		cacheImages[53] = new Image("flag.png");

		cacheImages[137] = new Image("137.png");
		cacheImages[453] = new Image("453.png");
		this.addLast(iActP = new myInteractivePanel(), this.STRETCH, this.FILL);
		CellPanel cp = new CellPanel();
		cp.addNext(btMinus,this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		cp.addNext(btToggle,this.HSTRETCH, this.FILL);
		cp.addLast(btPlus,this.DONTSTRETCH, (this.DONTFILL|this.EAST));
		this.addLast(cp, this.HSTRETCH, this.FILL);
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
	public void setParam(Preferences p, Vector db, String sWp){
		selectedWaypoint = sWp;
		pref = p;
		cacheDB = db;
		height = (int)(pref.myAppHeight)*6/5; // add 10% each at top/bottom 
		//height = (int)height * 2;
		////Vm.debug("Height: " + Convert.toString(height));
		////Vm.debug("App Height: " + Convert.toString(pref.myAppHeight));
		width = (int)(pref.myAppWidth)*6/5;
		//width = (int)width * 2;
	}
	
	// Call this after the center has changed to re-center the radar panel
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
		Font font = new Font("Verdana", Font.BOLD, 10);
		FontMetrics fm = getFontMetrics();
		AniImage aImg;
		RadarPanelImage rpi;
		int x,y = 0;
		CacheHolder holder;
		double degrees;
		double pi180=java.lang.Math.PI / 180.0;
		for(int i = cacheDB.size()-1; i >=0 ; i--){
			holder = (CacheHolder)cacheDB.get(i);
			if(!holder.is_black && !holder.is_filtered) {
				degrees = holder.degrees * pi180;
				x =new Float(holder.kilom/scale *  java.lang.Math.sin(degrees)).intValue();
				y = -new Float(holder.kilom/scale *  java.lang.Math.cos(degrees)).intValue();
				if(centerX+x>=0 && centerY+y>=0 && centerX+x<=width && centerY+y <= height){
					if (toggleMod>0) {
						String s;
						if (toggleMod==1)
							s=holder.wayPoint;
						else
							s=holder.CacheName;
						if (s.length()>0) { 
							int tw;
							Image img = new Image(tw=fm.getTextWidth(s),fm.getHeight());
							Graphics g = new Graphics(img);
							g.setColor(Color.Black);
							g.fillRect(0,0,tw, fm.getHeight());
							g.setColor(Color.White);
							g.drawText(s, 0,0);
							aImg = new AniImage(img);
							aImg.setLocation(centerX+x+5,centerY+y);
							aImg.transparentColor = Color.Black;
							aImg.properties = mImage.IsNotHot;
							iActP.addImage(aImg);
						}
					}
					Image imgCache=cacheImages[Convert.parseInt(holder.type)];
					// If we have no image for the cache type use a question mark
					if (imgCache==null) imgCache=cacheImages[8]; 
					rpi = new RadarPanelImage(imgCache);
					rpi.wayPoint = holder.wayPoint;
					rpi.rownum = i;
					rpi.setLocation(centerX+x-7,centerY+y-7);
					iActP.addImage(rpi);
					if(holder.wayPoint.equals(selectedWaypoint)){ // Draw red circle around selected wpt
						Image imgCircle = new Image(20, 20);
						Graphics gCircle = new Graphics(imgCircle);
						gCircle.setColor(Color.Black);
						gCircle.fillRect(0,0,20,20);
						gCircle.setColor(new Color(255,0,0));
						gCircle.drawEllipse(0,0, 20,20);
						aImg = new AniImage(imgCircle);
						aImg.setLocation(centerX+x-9,centerY+y-9);
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
		centerX = (int)(width / 2);
		centerY = (int)(height / 2);
		//centerY = (int)(centerY-centerY*0.15);
		g.setColor(new Color(0,255,0));
		int radstep= 0, steps=0, radius = 0;
		
		if(width > height){
			radstep = (int)(10 / scale);
			steps = (int)(width / radstep);
		}else{
			radstep = (int)(10 / scale);
			steps = (int)(height / radstep);
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
		int xPos = (int)(pref.myAppWidth/2 - width/2);
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
