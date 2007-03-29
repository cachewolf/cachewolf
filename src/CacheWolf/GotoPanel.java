package CacheWolf;

import ewe.ui.*;
import ewe.util.Vector;
import ewe.util.mString;
import ewe.fx.*;
import ewe.graphics.AniImage;
import ewe.io.*;
import ewe.net.Socket;
//import ewe.io.IOException;
//import ewe.io.SerialPort;
//import ewe.io.SerialPortOptions;
import ewe.sys.*;
import ewe.sys.Double;



/**
 *	Class to create the panel which handles the connection to the GPS-device<br>
 *	Displays: current position,speed and bearing; relation to destination waypoint<br>
 *	Class ID: 1500
 */


public class GotoPanel extends CellPanel {

	//public CWGPSPoint gpsPosition = new CWGPSPoint();
	//public CWPoint toPoint = new CWPoint();
	public Navigate myNavigation;
	mButton btnGPS, btnCenter,btnSave;
	mButton btnGoto, btnMap;
	mCheckBox chkDMM, chkDMS, chkDD, chkUTM;
	CheckBoxGroup chkFormat = new CheckBoxGroup();
	int currFormat;

	mLabel lblPosition, lblSats, lblSpeed, lblBearMov, lblBearWayP, lblDist, lblHDOP;
	mLabel lblSatsText, lblSpeedText, lblDirText, lblDistText, lblSunAzimut;
	mLabel lblGPS, lblDST, lblCurr, lblWayP;
	mLabel lblLog;
	mCheckBox chkLog;
	mInput inpLogSeconds;

	MainTab mainT;
	Vector cacheDB;
	DetailsPanel detP;

	Preferences pref;
	Profile profile;
	// different panels to avoid spanning
	CellPanel FormatP = new CellPanel();
	CellPanel ButtonP = new CellPanel();
	CellPanel CoordsP = new CellPanel();
	CellPanel roseP = new CellPanel();
	CellPanel GotoP = new CellPanel();
	CellPanel LogP = new CellPanel();

	ImageControl icRose;
	GotoRose compassRose;

	final static Color RED = new Color(255,0,0);
	final static Color YELLOW = new Color(255,255,0);
	final static Color GREEN = new Color(0,255,0);
	final static Color BLUE = new Color(0,0,255);

	final static Font BOLD = new Font("Arial", Font.BOLD, 14);

	GotoRose rose;
	int ticker = 0;

	/**
	 * Create GotoPanel 
	 * @param Preferences 	global preferences
	 * @param MainTab		reference to MainTable
	 * @param DetailsPanel 	reference to DetailsPanel
	 * @param Vector		cacheDB
	 */
	public GotoPanel(Navigate nav) {
		myNavigation = nav;
		pref = Global.getPref();
		profile=Global.getProfile();
		mainT = Global.mainTab;
		detP = mainT.detP;
		cacheDB = profile.cacheDB;

		// Button
		ButtonP.addNext(btnGPS = new mButton("Start"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		ButtonP.addNext(btnCenter = new mButton(MyLocale.getMsg(309,"Center")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		ButtonP.addNext(btnSave = new mButton(MyLocale.getMsg(311,"Create Waypoint")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
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
		CoordsP.addLast(lblPosition = new mLabel(myNavigation.gpsPos.toString(currFormat)),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		CoordsP.addNext(lblDST = new mLabel(MyLocale.getMsg(1500,"DST:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblDST.backGround = new Color(0,0,255);
		CoordsP.addLast(btnGoto = new mButton(getGotoBtnText()),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));

		//Rose for bearing
		compassRose = new GotoRose("rose.png");
		icRose = new ImageControl(compassRose);
		roseP.addLast(icRose,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.NORTH));

		//Goto
		//things from GPS
		GotoP.addLast(lblCurr = new mLabel(MyLocale.getMsg(1501,"Current")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblCurr.backGround = RED;
		lblCurr.font = BOLD;

		//GotoP.addNext(lblSatsText = new mLabel("Sats: "),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//lblSatsText.font = BOLD;
		GotoP.addLast(lblSats = new mLabel("Sats:    " + Convert.toString(myNavigation.gpsPos.getSats())),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblSats.font = BOLD;
		GotoP.addLast(lblHDOP = new mLabel("HDOP:    " + Convert.toString(myNavigation.gpsPos.getHDOP())),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		lblHDOP.font = BOLD;


		GotoP.addLast(lblSpeed = new mLabel(Convert.toString(myNavigation.gpsPos.getSpeed())),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		lblSpeed.font = BOLD;

		GotoP.addLast(lblBearMov = new mLabel("0"),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		lblBearMov.font = BOLD;

		//things about destination
		GotoP.addLast(lblWayP = new mLabel("WayPoint"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblWayP.backGround = Color.DarkBlue;
		lblWayP.foreGround = Color.White;
		lblWayP.font = BOLD;
		GotoP.addLast(lblBearWayP = new mLabel("0"),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		lblBearWayP.font = BOLD;

		GotoP.addLast(lblDist = new mLabel("0"),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		lblDist.font = BOLD;

		LogP.addNext(lblLog = new mLabel("Log "),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		LogP.addNext(chkLog = new mCheckBox(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		LogP.addNext(inpLogSeconds = new mInput("10"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		LogP.addLast(new mLabel("sec"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		chkLog.useCross = true;
		chkLog.setState(false);
		inpLogSeconds.columns = 5;

		LogP.addNext(lblGPS = new mLabel("Sonne: "),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblGPS.backGround = YELLOW;
		lblGPS.setTag(SPAN, new Dimension(2,1));

		LogP.addLast(lblSunAzimut = new mLabel("---"),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.NORTH));
		lblSunAzimut.setText("---");
		lblSunAzimut.font = BOLD;


		//add Panels
		this.addLast(ButtonP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST).setTag(SPAN,new Dimension(2,1));
		this.addLast(FormatP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST).setTag(SPAN,new Dimension(2,1));
		this.addLast(CoordsP,CellConstants.HSTRETCH, CellConstants.HFILL|CellConstants.NORTH).setTag(SPAN,new Dimension(2,1));
		this.addNext(roseP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST).setTag(SPAN,new Dimension(1,1));
		this.addLast(GotoP,CellConstants.HSTRETCH, CellConstants.HFILL|CellConstants.NORTHWEST).setTag(SPAN,new Dimension(1,2));
		this.addLast(LogP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.NORTHWEST).setTag(SPAN,new Dimension(1,1));

		// for debuging
		/*		CWGPSPoint myGPS;
		myGPS = new CWGPSPoint();
		String ex = new String();
		boolean test = false;
		try { FileReader f = new FileReader("c:\\kw\\20060928_1051.log");
		ex = f.readAll();
		test = myGPS.examine(ex);
		f.close();
		} catch (IOException e ) {
			Vm.debug(e.toString());
		}
		test = myGPS.examine("$GPGLL,5226.8935,N,01338.5327,E,084635.00,A,D*6E");
		test = myGPS.examine("$GPGSA,A,3,10,28,26,29,09,,,,,,,,04.1,02.6,03.2*0F");
		test = myGPS.examine("$GPGSV,4,1,13,08,40,072,28,10,29,201,47,27,15,079,29,28,62,102,44*7E");
		test = myGPS.examine("$GPGSV,4,2,13,29,72,289,38,26,63,296,41,09,12,259,35,18,14,324,*79");
		test = myGPS.examine("$GPGSV,4,3,13,19,09,025,,17,06,138,,21,06,300,,37,29,171,40*7A");
		test = myGPS.examine("$GPGSV,4,4,13,39,29,166,38*40");
		 */

		//while (true){
//		int notinterpreted = 0;
//		if (myGPS.examine("@ööH @ööHö@ÖÖHHÜÄÜÖÄÄÄH")) { notinterpreted = 0;} else notinterpreted++;
//		if (notinterpreted > 5) myGPS.noInterpretableData();
//		// myGPS.noInterpretableData();


//		}	
	}


	/**
	 * set the coords of the destination  
	 * @param dest destination
	 */ 
	public void setDestination(CWPoint dest){
		myNavigation.setDestination(dest);
		if (!myNavigation.destination.isValid()) (new MessageBox("Error", "Coordinates are out of range: \n"+"latitude: "+myNavigation.destination.latDec+"\n longditue: "+myNavigation.destination.lonDec, MessageBox.OKB)).execute();
		
	}
	
	public void destChanged(CWPoint d) { // called from myNavigate
		btnGoto.setText(getGotoBtnText());
		updateDistance();
	}
	

	/**
	 * set the coords of the destination and switch to gotoPanel  
	 * @param LatLon destination
	 */ 
	public void setDestinationAndSwitch(String LatLon) {
		myNavigation.setDestination(LatLon);
		mainT.select(this);
	}
	
	/**
	 * updates distance and bearing
	 *
	 */
	
	public void updateDistance() {
		//update distance
		Double tmp = new Double();
		if (myNavigation.gpsPos.isValid() && myNavigation.destination.isValid() ) {
			tmp.set(myNavigation.gpsPos.getDistance(myNavigation.destination)); // TODO distance in navigate.java berechnen
			if (tmp.value >= 1){
				lblDist.setText(MyLocale.formatDouble(tmp,"0.000")+ " km");
			}
			else {
				tmp.set(tmp.value * 1000);
				lblDist.setText(tmp.toString(3,0,0) + " m");
			}
		}
		else lblDist.setText("--- km");
		// update goto-bearing
		tmp.set(myNavigation.gpsPos.getBearing(myNavigation.destination));
		if (tmp.value <= 360) 
			lblBearWayP.setText(tmp.toString(0,0,0) + " Grad");
		else lblBearWayP.setText("---" + " Grad");
		compassRose.setWaypointDirection((float)tmp.value);
	}

	/**
	 * method which is called if a timer is set up  
	 */ 
	public void updateGps(int fix) {
		Double bearMov = new Double();
		Double speed = new Double();
		Double sunAzimut = new Double();
		Vm.debug("ticked: voher");
		lblSats.setText("Sats: " + Convert.toString(myNavigation.gpsPos.getSats()));
		lblHDOP.setText("HDOP: " + Convert.toString(myNavigation.gpsPos.getHDOP()));
		if ((fix > 0) && (myNavigation.gpsPos.getSats()>= 0)) {
			// display values only, if signal good
			//Vm.debug("currTrack.add: nachher");
			lblPosition.setText(myNavigation.gpsPos.toString(currFormat));
			speed.set(myNavigation.gpsPos.getSpeed());
			lblSpeed.setText(MyLocale.formatDouble(speed,"0.0") + " km/h");
			sunAzimut.set((double)myNavigation.sunAzimut);
			if (sunAzimut.value >= -360) lblSunAzimut.setText(MyLocale.formatDouble(sunAzimut,"0.0") + " Grad");
			else lblSunAzimut.setText("--- Grad");
			bearMov.set(myNavigation.gpsPos.getBear());
			lblBearMov.setText(bearMov.toString(0,0,0) + " Grad");
			compassRose.setSunMoveDirections((float)sunAzimut.value, (float)bearMov.value);
			updateDistance();
			// Set background to signal quality
			lblSats.backGround = GREEN;
		}

		// receiving data, but signal ist not good
		if ((fix == 0) && (myNavigation.gpsPos.getSats()>= 0)) {
			lblSats.backGround = YELLOW;
		}
		// receiving no data
		if (fix == -1) {
			if (lblSats.backGround != RED) (new MessageBox("Error", "No data from GPS\nConnection to serial port closed",MessageBox.OKB)).exec();
			lblSats.backGround = RED;
			myNavigation.stopGps();
		}
		// cannot interprete data
		if (fix == -2) {
			if (lblSats.backGround != RED) (new MessageBox("Error", "Cannot interpret data from GPS\n possible reasons:\n wrong Port,\n wrong Baudrate,\n not NMEA-Protocol\nConnection to serial port closed\nLast String tried to interprete:\n "+myNavigation.gpsPos.lastStrExamined, MessageBox.OKB)).exec();
			lblSats.backGround = RED;
			myNavigation.stopGps(); // TODO automatic in myNavigate?
		}
	}

	public void gpsStarted() {
		chkLog.modify(ControlConstants.Disabled,0);
		btnGPS.setText("Stop");
	}
	
	public void startGps() {
		myNavigation.setRawLogging(chkLog.getState(), Convert.toInt(inpLogSeconds.getText()));
		myNavigation.startGps();
	}

	public void gpsStoped() {
		btnGPS.setText("Start");
		lblSats.backGround = this.backGround;
		chkLog.modify(0,ControlConstants.Disabled);
		this.repaintNow(); // without this the change in the background color will not be displayed
	}

	
	private String getGotoBtnText() {
		if (myNavigation.destination == null) return "not set";
		else return myNavigation.destination.toString(currFormat);
	}
	
	public void switchToMovingMap() {
		CWPoint centerTo;
		if (myNavigation.isGpsPosValid()) centerTo = new CWPoint(myNavigation.gpsPos); // set gps-pos if gps is on
		else {
			// setze Zielpunkt als Ausgangspunkt, wenn GPS aus ist und lade entsprechende Karte
			//centerTo = new CWPoint(myNavigation.destination);
			if (myNavigation.destination.isValid())	centerTo = new CWPoint(myNavigation.destination);
			else centerTo = new CWPoint(pref.curCentrePt); // if not goto-point defined move map to centere point
		}  
		mainT.SwitchToMovingMap(centerTo, false);
	}
	
	/**
	 * Eventhandler
	 */

	public void onEvent(Event ev){

		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			// display coords in another format
			if (ev.target == chkFormat){
				currFormat = chkFormat.getSelectedIndex();
				lblPosition.setText(myNavigation.gpsPos.toString(currFormat));
				btnGoto.setText(getGotoBtnText());
			}

			// start/stop GPS connection
			if (ev.target == btnGPS){
				if (btnGPS.getText().equals("Start")) startGps();
				else myNavigation.stopGps();
			}

			// set current position as center and recalculate distance of caches in MainTab 
			if (ev.target == btnCenter){
				Vm.showWait(true);
				pref.curCentrePt.set(myNavigation.gpsPos);
				mainT.updateBearDist();
				Vm.showWait(false);
			}
			//Start moving map
			if (ev.target == btnMap){
				switchToMovingMap();
			} 
			// create new waypoint with current GPS-position
			if (ev.target == btnSave){
				CacheHolder ch = new CacheHolder();
				ch.LatLon = myNavigation.gpsPos.toString();
				ch.pos = new CWPoint(myNavigation.gpsPos);
				mainT.newWaypoint(ch);
			}
			// change destination waypoint
			if (ev.target == btnGoto){
				CoordsScreen cs = new CoordsScreen();
				if (myNavigation.destination.isValid())	cs.setFields(myNavigation.destination, currFormat);
				else cs.setFields(new CWPoint(0,0), currFormat);
				if (cs.execute() == CoordsScreen.IDOK)
					setDestination(cs.getCoords());
			}
		}
		super.onEvent(ev);
	}
}

/** class for displaying the compass rose
 * including goto, sun and moving direction
 */
class GotoRose extends AniImage {
	float gotoDir = -361;
	float sunDir = -361;
	float moveDir = -361;
	
	final static Color RED = new Color(255,0,0);
	final static Color YELLOW = new Color(255,255,0);
	final static Color GREEN = new Color(0,255,0);
	final static Color BLUE = new Color(0,255,255);
	final static Color ORANGE = new Color(255,128,0);
	final static Color DARKGREEN = new Color(0,128,0);

	/**
	 * @param gd goto direction
	 * @param sd sun direction
	 * @param md moving direction
	 */
	public GotoRose(String fn){
		super(fn);
	}
	
	public void setWaypointDirection(float wd) {
		gotoDir = wd;
	}
	
	public void setSunMoveDirections(float sd, float md ) {
		sunDir = sd;
		moveDir = md;
		refresh();
	}
	
	/**
	 * draw arrows for the directions of movement and destination waypoint
	 * @param ctrl the control to paint on
	 * @param moveDir degrees of movement
	 * @param destDir degrees of destination waypoint
	 */
	
	public void doDraw(Graphics g,int options) {
		super.doDraw(g, options);
		drawArrows(g);
	}

	private void drawArrows(Graphics g){
		if (g != null)
		{
			// select moveDirColor according to difference to gotoDir
			Color moveDirColor = RED;
			
			if (gotoDir < 360 && gotoDir > -360 && moveDir < 360 && moveDir > -360)
			{
				float diff = java.lang.Math.abs(moveDir - gotoDir);
				while (diff > 360)
				{
					diff -= 360.0f;
				}
				if (diff > 180)
				{
					diff = 360.0f - diff;
				}
				
				if (diff <= 5.0)
				{
					moveDirColor = DARKGREEN;
				}
				else if (diff <= 22.5)
				{
					moveDirColor = GREEN;
				}
				else if (diff <= 45.0)
				{
					moveDirColor = ORANGE;
				}
			}

			// draw only valid arrows
			if (gotoDir < 360 && gotoDir > -360) drawArrow(g, gotoDir, Color.DarkBlue, 1.0f);
			if (moveDir < 360 && moveDir > -360) drawArrow(g, moveDir, moveDirColor, 1.0f);
			if (sunDir < 360 && sunDir > -360) drawArrow(g, sunDir, YELLOW, 0.75f);
		}
	}

	/**
	 * draw single arrow 
	 * @param g handle for drawing
	 * @param angle angle of arrow
	 * @param col color of arrow
	 */
	private void drawArrow(Graphics g, float angle, Color col, float scale) {
		float angleRad;
		int x, y, centerX = location.width/2, centerY = location.height/2;

		angleRad = (angle) * (float)java.lang.Math.PI / 180;
		x = centerX + new Float(centerX * java.lang.Math.sin(angleRad) * scale).intValue();
		y = centerY - new Float(centerY * java.lang.Math.cos(angleRad) * scale).intValue();
		g.setPen(new Pen(col,Pen.SOLID,3));
		g.drawLine(centerX,centerY,x,y);
	}
}
