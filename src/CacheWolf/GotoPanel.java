package CacheWolf;

import ewe.ui.*;
import ewe.util.Vector;
import ewe.util.mString;
import ewe.fx.*;
import ewe.io.*;
//import ewe.io.IOException;
//import ewe.io.SerialPort;
//import ewe.io.SerialPortOptions;
import ewe.sys.*;
import ewe.sys.Double;



/**
 * Thread for reading data from COM-port
 *
 */
class SerialThread extends mThread{
	SerialPort comSp;   
	byte[] comBuff = new byte[1024];  
	int comLength = 0;
	CWGPSPoint myGPS;
	boolean run;
	
	public SerialThread(SerialPortOptions spo, CWGPSPoint GPSPoint){
		try {
			comSp = new SerialPort(spo);
		} catch (IOException e) {
			Vm.debug("Error open COM-Port " + spo.portName);
		}
		myGPS = GPSPoint;
	}
	
	public void run() {
		int noData = 0;
		run = true;
		while (run){
			try {
				sleep(1000);
				//Vm.debug("Loop? " + noData);
				noData++;
				if (noData > 5) myGPS.noData();
			} catch (InterruptedException e) {}
			if (comSp != null)	{  
				comLength = comSp.nonBlockingRead(comBuff, 0 ,comBuff.length);
				//Vm.debug("Length: " + comBuff.length);
				if (comLength > 0)	{
					noData = 0;
					String str = mString.fromAscii(comBuff, 0, comLength); 
					//Vm.debug(str);
					myGPS.examine(str);
				}
			}
		}
	}
	public void stop() {
		run = false;
		myGPS.noData();
		comSp.close();
	}
}

/**
*	Class to create the panel which handles the connection to the GPS-device<br>
*	Displays: current position,speed and bearing; relation to destination waypoint<br>
*	Class ID: 1500
*/


public class GotoPanel extends CellPanel {
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);

	public CWGPSPoint gpsPosition = new CWGPSPoint();
	public CWPoint toPoint = new CWPoint();

	mButton btnGPS, btnCenter,btnSave;
	mButton btnGoto, btnMap;
	mCheckBox chkDMM, chkDMS, chkDD, chkUTM;
	CheckBoxGroup chkFormat = new CheckBoxGroup();
	int currFormat;
	
	mLabel lblPosition, lblSats, lblSpeed, lblBearMov, lblBearWayP, lblDist;
	mLabel lblSatsText, lblSpeedText, lblDirText, lblDistText;
	mLabel lblGPS, lblDST, lblCurr, lblWayP;
	mLabel lblLog;
	mCheckBox chkLog;
	mInput inpLogSeconds;
	
	MainTab mainT;
	Vector cacheDB;
	DetailsPanel detP;

	Preferences pref;
	// different panels to avoid spanning
	CellPanel FormatP = new CellPanel();
	CellPanel ButtonP = new CellPanel();
	CellPanel CoordsP = new CellPanel();
	CellPanel roseP = new CellPanel();
	CellPanel GotoP = new CellPanel();
	CellPanel LogP = new CellPanel();
	
	SerialThread serThread;
	public int displayTimer = 0;
	
	ImageControl ic; 
	
	static Color RED = new Color(255,0,0);
	static Color YELLOW = new Color(255,255,0);
	static Color GREEN = new Color(0,255,0);
	static Color BLUE = new Color(0,255,255);
	
	static Font BOLD = new Font("Arial", Font.BOLD, 14);

	int centerX, centerY;
	
	int ticker = 0;
	
	boolean mapsLoaded = false;
	public boolean runMovingMap = false;
	Vector availableMaps = new Vector();
	MapInfoObject tempMIO = new MapInfoObject();
	MovingMap mmp;
	
	/**
	 * Create GotoPanel 
	 * @param Preferences 	global preferences
	 * @param MainTab		reference to MainTable
	 * @param DetailsPanel 	reference to DetailsPanel
	 * @param Vector		cacheDB
	 */
	public GotoPanel(Preferences p, MainTab mt, DetailsPanel dp, Vector db)
	{
		
		pref = p;
		mainT = mt;
		detP = dp;
		cacheDB = db;
		

		// Button
		ButtonP.addNext(btnGPS = new mButton("Start"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		ButtonP.addNext(btnCenter = new mButton((String)lr.get(309,"Center")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		ButtonP.addNext(btnSave = new mButton((String)lr.get(311,"Create Waypoint")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		ButtonP.addLast(btnMap = new mButton("Map"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		

		//Format selection for coords
		FormatP.addNext(chkDD =new mCheckBox("d.d°"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		FormatP.addNext(chkDMM =new mCheckBox("d°m.m\'"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		FormatP.addNext(chkDMS =new mCheckBox("d°m\'s\""),CellConstants.DONTSTRETCH,CellConstants.WEST);
		FormatP.addLast(chkUTM =new mCheckBox("UTM"),CellConstants.DONTSTRETCH, CellConstants.WEST);

		chkDD.setGroup(chkFormat);
		chkDMM.setGroup(chkFormat);
		chkDMS.setGroup(chkFormat);
		chkUTM.setGroup(chkFormat);
		currFormat = CWPoint.DMM;
		chkFormat.selectIndex(currFormat);

		
		//Coords
		CoordsP.addNext(lblGPS = new mLabel("GPS: "),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblGPS.backGround = RED;
		CoordsP.addLast(lblPosition = new mLabel(gpsPosition.toString(currFormat)),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		CoordsP.addNext(lblDST = new mLabel((String)lr.get(1500,"DST:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblDST.backGround = BLUE;
		CoordsP.addLast(btnGoto = new mButton(toPoint.toString(currFormat)),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));

		//Rose for bearing
		Image img = new Image("rose.png");
		ic = new ImageControl(img);
		centerY = img.getHeight() / 2;
		centerX = img.getWidth() / 2;
		roseP.addLast(ic,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.NORTH));
		
		//Goto
		//things from GPS
		GotoP.addLast(lblCurr = new mLabel((String)lr.get(1501,"Current")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblCurr.backGround = RED;
		lblCurr.font = BOLD;

		GotoP.addNext(lblSatsText = new mLabel("Sats: "),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblSatsText.font = BOLD;
		GotoP.addLast(lblSats = new mLabel(Convert.toString(gpsPosition.getSats())),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.EAST));
		lblSats.font = BOLD;

		GotoP.addLast(lblSpeed = new mLabel(Convert.toString(gpsPosition.getSpeed())),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.NORTH));
		lblSpeed.font = BOLD;

		GotoP.addLast(lblBearMov = new mLabel("0"),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.NORTH));
		lblBearMov.font = BOLD;

		//things about destination
		GotoP.addLast(lblWayP = new mLabel("WayPoint"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblWayP.backGround = BLUE;
		lblWayP.font = BOLD;
		GotoP.addLast(lblBearWayP = new mLabel("0"),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.NORTH));
		lblBearWayP.font = BOLD;
		
		GotoP.addLast(lblDist = new mLabel("0"),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.NORTH));
		lblDist.font = BOLD;
		
		LogP.addNext(lblLog = new mLabel("Log "),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		LogP.addNext(chkLog = new mCheckBox(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		LogP.addNext(inpLogSeconds = new mInput("10"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		LogP.addLast(new mLabel("sec"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		chkLog.useCross = true;
		chkLog.setState(false);
		
		//add Panels
		this.addLast(ButtonP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST).setTag(SPAN,new Dimension(3,1));
		this.addLast(FormatP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST).setTag(SPAN,new Dimension(4,1));
		this.addLast(CoordsP,CellConstants.HSTRETCH, CellConstants.HFILL|CellConstants.NORTH).setTag(SPAN,new Dimension(2,1));
		this.addNext(roseP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.NORTH).setTag(SPAN,new Dimension(1,1));
		this.addLast(GotoP,CellConstants.HSTRETCH, CellConstants.HFILL|CellConstants.NORTH).setTag(SPAN,new Dimension(2,5));
		this.addNext(LogP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST).setTag(SPAN,new Dimension(2,1));
		
		
	}
	
	/**
	 * draw arrows for the directions of movement and destination waypoint
	 * @param ctrl the control to paint on
	 * @param moveDir degrees of movement
	 * @param destDir degrees of destination waypoint
	 */
	
	private void drawArrows(Control ctrl,double moveDir, double destDir){
		Graphics g = ctrl.getGraphics();
		
		if (g != null) {
			ctrl.repaintNow();
			//g.setColor(RED);
			drawArrow(g, moveDir, RED);
			drawArrow(g, destDir, BLUE);
			g.free();
		}

	}

	/**
	 * draw single arrow 
	 * @param g handle for drawing
	 * @param angle angle of arrow
	 * @param col color of arrow
	 */
	private void drawArrow(Graphics g, double angle, Color col) {
		double angleRad;
		int x, y;
		
		angleRad = angle * java.lang.Math.PI / 180;
		x = centerX + new Float(centerX * java.lang.Math.sin(angleRad)).intValue();
		y = centerY - new Float(centerY * java.lang.Math.cos(angleRad)).intValue();
		g.setPen(new Pen(col,Pen.SOLID,3));
		g.drawLine(centerX,centerY,x,y);
		
	}
	
	/**
	 * set the coords of the destination  
	 * @param dest destination
	 */ 
	public void setDestination(CWPoint dest){
		toPoint.set(dest);
		btnGoto.setText(toPoint.toString(currFormat));
		mainT.select(this);
	}

	/**
	 * set the coords of the destination  
	 * @param LatLon destination
	 */ 
	public void setDestination(String LatLon) {
		toPoint.set(LatLon,CWPoint.CW);
		btnGoto.setText(toPoint.toString(currFormat));
		mainT.select(this);
	}
	
	/**
	 * method which is called if a timer is set up  
	 * @param timerId id of the timer, which has expired
	 * @param elapsed ticks since last calling
	 */ 
	public void ticked(int timerId, int elapsed){
		Double bearMov = new Double();
		Double bearWayP = new Double();
		Double dist = new Double();
		Double speed = new Double();

		if (timerId == displayTimer) {
			if(!runMovingMap){
				lblSats.setText(Convert.toString(gpsPosition.getSats()));
				// display values only, if signal good
				if ((gpsPosition.getFix()> 0) && (gpsPosition.getSats()>= 0)) {
					//gpsPosition.printAll();
					lblPosition.setText(gpsPosition.toString(currFormat));
					
					speed.set(gpsPosition.getSpeed());
					lblSpeed.setText(l.format(Locale.FORMAT_PARSE_NUMBER,speed,"0.0") + " km/h");
	
					bearMov.set(gpsPosition.getBear());
					lblBearMov.setText(bearMov.toString(0,0,0) + " Grad");
	
					
					bearWayP.set(gpsPosition.getBearing(toPoint));
					lblBearWayP.setText(bearWayP.toString(0,0,0) + " Grad");
					
					dist.set(gpsPosition.getDistance(toPoint));
					if (dist.value >= 1){
						lblDist.setText(l.format( Locale.FORMAT_PARSE_NUMBER,dist,"0.000")+ " km");
					}
					else {
						dist.set(dist.value * 1000);
						lblDist.setText(dist.toString(3,0,0) + " m");
					}
					
					drawArrows(ic,bearMov.value,bearWayP.value);
		
					// Set background to signal quality
					lblSats.backGround = GREEN;
					return;
				}
				// receiving data, but signal ist not good
				if ((gpsPosition.getFix()== 0) && (gpsPosition.getSats()>= 0)) {
					lblSats.backGround = YELLOW;
					return;
				}
				// receiving no data
				if (gpsPosition.getFix()== -1) {
					lblSats.backGround = RED;
					return;
				}
			}else{ // In moving map mode
				if ((gpsPosition.getFix()> 0) && (gpsPosition.getSats()>= 0)) {
					mmp.updatePosition(gpsPosition.latDec, gpsPosition.lonDec);
				}
				/* The following code does nothing, comment it out
				// receiving data, but signal ist not good
				if ((gpsPosition.getFix()== 0) && (gpsPosition.getSats()>= 0)) {
					//lblSats.backGround = YELLOW;
					//return;
				}
				// receiving no data
				if (gpsPosition.getFix()== -1) {
					//lblSats.backGround = RED;
					//return;
				}
				*/
				/*
				if(ticker == 0) mmp.updatePosition(48.23003333, 11.63345);
				if(ticker == 1) mmp.updatePosition(48.23651667, 11.63716667);
				if(ticker == 2) mmp.updatePosition(48.24335, 11.64035);
				if(ticker == 3) mmp.updatePosition(48.22103333, 11.62976667);
				ticker++;
				if(ticker > 3) ticker = 0;
				*/
			}
		}
	}

	public void stopTheTimer(){
		Vm.cancelTimer(displayTimer);
	}
	
	/**
	 * Eventhandler
	 */
	public void onEvent(Event ev){

		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			// display coords in another format
			if (ev.target == chkFormat){
				currFormat = chkFormat.getSelectedIndex();
				lblPosition.setText(gpsPosition.toString(currFormat));
				btnGoto.setText(toPoint.toString(currFormat));
			}

			// start/stop GPS connection
			if (ev.target == btnGPS){
				if (btnGPS.getText().equals("Start")){
					serThread = new SerialThread(pref.mySPO, gpsPosition);
					serThread.start();
					displayTimer = Vm.requestTimer(this, 1000);
					if (chkLog.getState()){
						gpsPosition.startLog(pref.mydatadir, Convert.toInt(inpLogSeconds.getText()), CWGPSPoint.LOGALL);
					}
					chkLog.modify(ControlConstants.Disabled,0);
					btnGPS.setText("Stop");
				}
				else {
					serThread.stop();
					Vm.cancelTimer(displayTimer);
					btnGPS.setText("Start");
					gpsPosition.stopLog();
					chkLog.modify(0,ControlConstants.Disabled);
				}
			}
			// set current position as center and recalculate distance of caches in MainTab 
			if (ev.target == btnCenter){
				pref.mylgNS = gpsPosition.getNSLetter();
				pref.mylgDeg = gpsPosition.getLatDeg(CWPoint.DMM);
				pref.mylgMin = gpsPosition.getLatMin(CWPoint.DMM);
				pref.mybrDeg = gpsPosition.getLonDeg(CWPoint.DMM);
				pref.mybrMin = gpsPosition.getLonMin(CWPoint.DMM);
				pref.mybrWE = gpsPosition.getEWLetter();
				mainT.updateBearDist();
			}
			//Start moving map
			if (ev.target == btnMap){
				if(mapsLoaded == false){
					String dateien[];
					InfoBox inf = new InfoBox("Info", "Loading maps...");
					Vm.showWait(true);
					inf.show();
					String mapsPath = new String();
					mapsPath = File.getProgramDirectory() + "/maps/";
					File files = new File(mapsPath);
					Extractor ext;
					String rawFileName = new String();
					dateien = files.list("*.png", File.LIST_FILES_ONLY);
					tempMIO = new MapInfoObject();
					for(int i = 0; i < dateien.length;i++){
						ext = new Extractor(dateien[i], "", ".", 0, true);
						rawFileName = ext.findNext();
						try {
							tempMIO.loadwfl(mapsPath, rawFileName);
							availableMaps.add(tempMIO);
							mapsLoaded = true;
						}catch(IOException ex){ } // TODO etwas genauer auch Fehlermeldung ausgeben? Bei vorhandenen .wfl-Datei mit ungültigen Werten Fehler ausgeben oder wie jetz einfach ignorieren?
					}
					inf.close(0);
					Vm.showWait(false);
				}
				mmp = new MovingMap(pref, availableMaps, this, cacheDB);
				//position test
				//gpsPosition.latDec = 48.22103333;
				//gpsPosition.lonDec = 11.62976667;
				mmp.loadMap();
				runMovingMap = true;
				//serThread = new SerialThread(pref.mySPO, gpsPosition);
				//serThread.start();
				//displayTimer = Vm.requestTimer(this, 1000);
				//end position test
				mmp.execute();
			}
			// create new waypoint with current GPS-position
			if (ev.target == btnSave){
				CacheHolder ch = new CacheHolder();
				ch.LatLon = gpsPosition.toString();
				detP.newWaypoint(ch,cacheDB, mainT, pref);
			}
			// change destination waypoint
			if (ev.target == btnGoto){
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(toPoint, currFormat);
				if (cs.execute()== CoordsScreen.IDOK){
					toPoint = cs.getCoords();
					btnGoto.setText(toPoint.toString(currFormat));
				}
			}
		}
		super.onEvent(ev);
	}
}
