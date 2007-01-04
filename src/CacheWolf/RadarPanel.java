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
		double dummy;
		height = (int)(pref.myAppHeight);
		//height = (int)height * 2;
		////Vm.debug("Height: " + Convert.toString(height));
		////Vm.debug("App Height: " + Convert.toString(pref.myAppHeight));
		width = (int)(pref.myAppWidth);
		//width = (int)width * 2;
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
		int rowCounter = 0;
		for(int i = 0; i < cacheDB.size(); i++){
			holder = (CacheHolder)cacheDB.get(i);
			if(holder.is_black == false && holder.is_filtered == false)
			{
				////Vm.debug(holder.wayPoint + ": " +Convert.toString(holder.degrees));
				//degrees = (holder.degrees - 360)*-1;
				degrees = holder.degrees * java.lang.Math.PI / 180;
				x =new Float(holder.kilom/scale *  java.lang.Math.sin(degrees)).intValue();
				y = new Float(holder.kilom/scale *  java.lang.Math.cos(degrees)).intValue();
				y=y*-1;
				if(centerX+x<=width && centerY+y <= height){
					if(toggleMod == 1){ // draw waypoint names
						Image img = new Image(fm.getTextWidth(holder.wayPoint),fm.getHeight());
						Graphics g = new Graphics(img);
						g.setColor(new Color(0,0,0));
						g.fillRect(0,0,fm.getTextWidth(holder.wayPoint), fm.getHeight());
						g.setColor(new Color(255,255,255));
						g.drawText(holder.wayPoint, 0,0);
						aImg = new AniImage(img);
						aImg.setLocation(centerX+x+5,centerY+y);
						aImg.transparentColor = new Color(0,0,0);
						aImg.properties = mImage.IsNotHot;
						iActP.addImage(aImg);
					}
					if(toggleMod == 2 && holder.CacheName.length()> 0){ // draw cache names
						try {
							Image img = new Image(fm.getTextWidth(holder.CacheName),fm.getHeight());
							Graphics g = new Graphics(img);
							g.setColor(new Color(0,0,0));
							g.fillRect(0,0,fm.getTextWidth(holder.CacheName), fm.getHeight());
							g.setColor(new Color(255,255,255));
							g.drawText(holder.CacheName, 0,0);
							aImg = new AniImage(img);
							aImg.setLocation(centerX+x+5,centerY+y);
							aImg.transparentColor = new Color(0,0,0);
							aImg.properties = mImage.IsNotHot;
							iActP.addImage(aImg);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					rpi = new RadarPanelImage(cacheImages[Convert.parseInt(holder.type)]);
					rpi.wayPoint = holder.wayPoint;
					rpi.rownum = rowCounter;
					rpi.setLocation(centerX+x-7,centerY+y-7);
					iActP.addImage(rpi);
					if(holder.wayPoint.equals(selectedWaypoint)){
						Image img = new Image(20, 20);
						Graphics g = new Graphics(img);
						g.setColor(new Color(0,0,0));
						g.fillRect(0,0,20,20);
						g.setColor(new Color(255,0,0));
						g.drawEllipse(0,0, 20,20);
						aImg = new AniImage(img);
						aImg.setLocation(centerX+x-9,centerY+y-9);
						aImg.transparentColor = new Color(0,0,0);
						aImg.properties = mImage.IsNotHot;
						iActP.addImage(aImg);
					}
				}//if center...
				rowCounter++;
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
		g.setColor(new Color(0,0,0));
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
		
		g.free();
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
				scaleKm = scaleKm - 10;
				if(scaleKm < 10) scaleKm = 10;
				drawThePanel();
			}
			if (ev.target == btMinus){
				scaleKm = scaleKm + 10;
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
