package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.CacheHolder;
import CacheWolf.CacheType;
import CacheWolf.CoordsScreen;
import CacheWolf.DetailsPanel;
import CacheWolf.Global;
import CacheWolf.MainTab;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.Profile;
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
	int currFormat;

	mLabel lblGPS, lblPosition, lblDST;
	mLabel lblLog;
	Color gpsStatus;
	mCheckBox chkLog;
	mInput inpLogSeconds;

	MainTab mainT;
	Vector cacheDB;
	DetailsPanel detP;

	Preferences pref;
	Profile profile;
	// different panels to avoid spanning
	CellPanel ButtonP = new CellPanel();
	CellPanel CoordsP = new CellPanel();
	CellPanel roseP = new CellPanel();
	CellPanel LogP = new CellPanel();

	ImageControl icRose;
	GotoRose compassRose;

	final static Color RED = new Color(255,0,0);
	final static Color YELLOW = new Color(255,255,0);
	final static Color GREEN = new Color(0,255,0);
	final static Color BLUE = new Color(0,0,255);

	final static Font BOLD = new Font("Arial", Font.BOLD, 14);

	int ticker = 0;
	
	Menu mnuContextFormt;
	MenuItem miDMM, miDMS, miDD, miUTM;
	
	Menu mnuContextRose;
	MenuItem miLuminary[] = new MenuItem[SkyOrientation.LUMINARY_NAMES.length];
	MenuItem miNorthCentered;
	
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
		ButtonP.addNext(btnCenter = new mButton(MyLocale.getMsg(309,"Centre")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		ButtonP.addNext(btnSave = new mButton(MyLocale.getMsg(311,"Create Waypoint")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		ButtonP.addLast(btnMap = new mButton("Map"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		//Format selection for coords		
		//context menu
		mnuContextFormt = new Menu();
		mnuContextFormt.addItem(miDD = new MenuItem("d.d°"));
		miDD.modifiers &= ~MenuItem.Checked;
		mnuContextFormt.addItem(miDMM = new MenuItem("d°m.m\'"));
		miDMM.modifiers |= MenuItem.Checked;
		mnuContextFormt.addItem(miDMS = new MenuItem("d°m\'s\""));
		miDMS.modifiers &= ~MenuItem.Checked;
		mnuContextFormt.addItem(miUTM = new MenuItem("UTM"));
		miUTM.modifiers &= ~MenuItem.Checked;
		currFormat = CWPoint.DMM;

		// Create context menu for compass rose: select luminary for orientation
		mnuContextRose = new Menu();
		for (int i=0; i<SkyOrientation.LUMINARY_NAMES.length; i++) {
			mnuContextRose.addItem(miLuminary[i] = new MenuItem(SkyOrientation.getLuminaryName(i)));
			if (i == myNavigation.luminary) miLuminary[i].modifiers |= MenuItem.Checked;
			else miLuminary[i].modifiers &= MenuItem.Checked;
		}

		//Coords
		CoordsP.addNext(lblGPS = new mLabel("GPS: "),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblGPS.backGround = RED;
		lblGPS.setMenu(mnuContextFormt);
		lblGPS.modifyAll(Control.WantHoldDown, 0);
		CoordsP.addLast(lblPosition = new mLabel(myNavigation.gpsPos.toString(currFormat)),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		lblPosition.setMenu(mnuContextFormt);
		lblPosition.modifyAll(Control.WantHoldDown, 0);
		CoordsP.addNext(lblDST = new mLabel(MyLocale.getMsg(1500,"DST:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblDST.backGround = new Color(0,0,255);
		lblDST.setMenu(mnuContextFormt);
		lblDST.modifyAll(Control.WantHoldDown, 0);
		CoordsP.addLast(btnGoto = new mButton(getGotoBtnText()),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));

		//Rose for bearing
		compassRose = new GotoRose("rose.png");
		icRose = new ImageControl(compassRose);
		icRose.setMenu(mnuContextRose);
		icRose.modifyAll(Control.WantHoldDown, 0); // this is necessary in order to make PenHold on a PDA work as right click
		roseP.addLast(icRose,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.NORTH));
		
		mnuContextRose.addItem(new MenuItem("", MenuItem.Separator, null));
		mnuContextRose.addItem(miNorthCentered = new MenuItem(MyLocale.getMsg(1503,"North Centered")));
		if (compassRose.isNorthCentered()) miNorthCentered.modifiers |= MenuItem.Checked;
		else miNorthCentered.modifiers &= MenuItem.Checked;

		//log
		LogP.addNext(lblLog = new mLabel("Log "),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		LogP.addNext(chkLog = new mCheckBox(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		LogP.addNext(inpLogSeconds = new mInput("10"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		LogP.addLast(new mLabel("sec"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		chkLog.useCross = true;
		chkLog.setState(false);
		inpLogSeconds.columns = 5;

		//add Panels
		this.addLast(ButtonP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST).setTag(SPAN,new Dimension(2,1));
		this.addLast(CoordsP,CellConstants.HSTRETCH, CellConstants.HFILL|CellConstants.NORTH).setTag(SPAN,new Dimension(2,1));
		this.addLast(roseP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST).setTag(SPAN,new Dimension(2,1));
		//this.addLast(LogP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.NORTHWEST).setTag(SPAN,new Dimension(1,1));

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
	public void setDestinationAndSwitch(CWPoint where) {
		myNavigation.setDestination(where);
		mainT.select(this);
	}
	
	/**
	 * updates distance and bearing
	 *
	 */
	
	public void updateDistance() {
		//update distance
		float distance = -1.0f;
		if (myNavigation.gpsPos.isValid() && myNavigation.destination.isValid() ) {
			distance = (float)myNavigation.gpsPos.getDistance(myNavigation.destination);
		}
		compassRose.setWaypointDirectionDist((float)myNavigation.gpsPos.getBearing(myNavigation.destination), distance);
	}

	/**
	 * method which is called if a timer is set up  
	 */ 
	public void updateGps(int fix) {
		Double bearMov = new Double();
		Double speed = new Double();
		Double sunAzimut = new Double();
		//Vm.debug("ticked: before");
		compassRose.setGpsStatus(fix, myNavigation.gpsPos.getSats(), myNavigation.gpsPos.getSatsInView(), myNavigation.gpsPos.getHDOP());
		if ((fix > 0) && (myNavigation.gpsPos.getSats()>= 0)) {
			// display values only, if signal good
			//Vm.debug("currTrack.add: nachher");
			lblPosition.setText(myNavigation.gpsPos.toString(currFormat));
			speed.set(myNavigation.gpsPos.getSpeed());
			sunAzimut.set(myNavigation.skyOrientationDir.lonDec);
			bearMov.set(myNavigation.gpsPos.getBear());
			updateDistance();
			compassRose.setSunMoveDirections((float)sunAzimut.value, (float)bearMov.value, (float)speed.value);
			// Set background to signal quality
		}

		// receiving data, but signal ist not good
		if ((fix == 0) && (myNavigation.gpsPos.getSats()>= 0)) {
			gpsStatus = YELLOW;
		}
		// receiving no data
		if (fix == -1) {
			if (gpsStatus != RED) (new MessageBox("Error", "No data from GPS\nConnection to serial port closed",MessageBox.OKB)).exec();
			gpsStatus = RED;
			myNavigation.stopGps();
		}
		// cannot interprete data
		if (fix == -2) {
			if (gpsStatus != RED) (new MessageBox("Error", "Cannot interpret data from GPS\n possible reasons:\n wrong Port,\n wrong Baudrate,\n not NMEA-Protocol\nConnection to serial port closed\nLast String tried to interprete:\n "+myNavigation.gpsPos.lastStrExamined, MessageBox.OKB)).exec();
			gpsStatus = RED;
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
		gpsStatus = this.backGround;
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
		if (ev instanceof MenuEvent) { 
			if (ev.type == MenuEvent.SELECTED ) {
				MenuItem action = (MenuItem) mnuContextFormt.getSelectedItem(); 
				if (action != null) {
					if (action == miDD) {
						mnuContextFormt.close();
						currFormat = CWPoint.DD;
					}
					if (action == miDMM) {
						mnuContextFormt.close();
						currFormat = CWPoint.DMM;
					}
					if (action == miDMS) {
						mnuContextFormt.close();
						currFormat = CWPoint.DMS;
					}
					if (action == miUTM) {
						mnuContextFormt.close();
						currFormat = CWPoint.UTM;
					}
					miDD.modifiers &= ~MenuItem.Checked;
					miDMM.modifiers &= ~MenuItem.Checked;
					miDMS.modifiers &= ~MenuItem.Checked;
					miUTM.modifiers &= ~MenuItem.Checked;
					switch (currFormat) {
					case CWPoint.DD: miDD.modifiers |= MenuItem.Checked; break;   
					case CWPoint.DMM: miDMM.modifiers |= MenuItem.Checked; break;   
					case CWPoint.DMS: miDMS.modifiers |= MenuItem.Checked; break;   
					case CWPoint.UTM: miUTM.modifiers |= MenuItem.Checked; break;
					}

					lblPosition.setText(myNavigation.gpsPos.toString(currFormat));
					btnGoto.setText(getGotoBtnText());
				} // end lat-lon-format context menu
				action = (MenuItem) mnuContextRose.getSelectedItem();
				if (action != null) {
					for (int i=0; i<miLuminary.length; i++) {
						if (action == miLuminary[i]) {
							myNavigation.setLuminary(i);
							miLuminary[i].modifiers |= MenuItem.Checked;
							compassRose.setLuminaryName(SkyOrientation.getLuminaryName(myNavigation.luminary));
						} else miLuminary[i].modifiers &= ~MenuItem.Checked;
					}
					if (action == miNorthCentered) {
						if (compassRose.isNorthCentered()) {
							compassRose.setNorthCentered(false);
							miNorthCentered.modifiers &= ~MenuItem.Checked;							
						}
						else
						{
							compassRose.setNorthCentered(true);
							miNorthCentered.modifiers |= MenuItem.Checked;
						}
					}
				}
			}
		}

		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			// start/stop GPS connection
			if (ev.target == btnGPS){
				if (btnGPS.getText().equals("Start")) startGps();
				else myNavigation.stopGps();
			}

			// set current position as centre and recalculate distance of caches in MainTab 
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
				ch.type = "51"; // see CacheType.GC_AW_STAGE_OF_MULTI // TODO unfertig
				mainT.newWaypoint(ch);
			}
			// change destination waypoint
			if (ev.target == btnGoto){
				CoordsScreen cs = new CoordsScreen();
				if (myNavigation.destination.isValid())	cs.setFields(myNavigation.destination, currFormat);
				else cs.setFields(new CWPoint(0,0), currFormat);
				if (cs.execute(null, Gui.TOP) == CoordsScreen.IDOK)
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
	float distance = -1;
	
	int m_fix = -1;
	int m_sats = -1;
	int m_satsInView = 0;
	double m_hdop = -1;
	float m_speed = -1;
	
	String m_Luminary = MyLocale.getMsg(6100, "Sun");
	
	FontMetrics fm;
	int lineHeight;
	
	boolean northCentered = Global.getPref().northCenteredGoto;
	
	final static Color RED = new Color(255,0,0);
	final static Color YELLOW = new Color(255,255,0);
	final static Color GREEN = new Color(0,255,0);
	final static Color BLUE = new Color(0,0,255);
	final static Color ORANGE = new Color(255,128,0);
	final static Color DARKGREEN = new Color(0,192,0);
	final static Color CYAN = new Color(0,255,255);
	final static Color MAGENTA = new Color(255,0,255);

	/**
	 * @param gd goto direction
	 * @param sd sun direction
	 * @param md moving direction
	 */
	public GotoRose(String fn){
		super(fn);
	}
	
	public void setWaypointDirectionDist(float wd, float dist) {
		gotoDir = wd;
		distance = dist;
	}
	
	public void setSunMoveDirections(float sd, float md, float speed ) {
		sunDir = sd;
		moveDir = md;
		m_speed = speed;
		refresh();
	}
	
	public void setGpsStatus(int fix, int sats, int satsInView, double hdop) {
		m_fix = fix;
		m_sats = sats;
		m_satsInView = satsInView;
		m_hdop = hdop;
		refresh();
	}
	
	public void setLuminaryName(String Luminary) {
		m_Luminary = Luminary;
		refresh();
	}

	
	/**
	 * draw arrows for the directions of movement and destination waypoint
	 * @param ctrl the control to paint on
	 * @param moveDir degrees of movement
	 * @param destDir degrees of destination waypoint
	 */
	
	public void doDraw(Graphics g,int options) {
		if (northCentered) {
			super.doDraw(g, options);
		}
		else {
			g.setColor(Color.White);
			g.fillRect(0, 0, location.width, location.height);
		}
		Font font = new Font("Verdana", Font.BOLD, 12);
		g.setFont(font);
		fm = g.getFontMetrics(font);
		lineHeight = fm.getHeight() + 1;
		drawArrows(g);
		drawWayPointData(g);
		drawGpsData(g);
		drawLuminaryData(g);
		drawGpsStatus(g);
	}
		
	private void drawWayPointData(Graphics g){
		String strTemp = "WayPoint";
		g.setColor(Color.DarkBlue);
		g.fillRect(0, 0, fm.getTextWidth(strTemp) + 4, lineHeight);
		g.setColor(Color.White);		
		g.drawText(strTemp, 2, 0);
		
		g.setColor(Color.Black);		
		
		Double tmp = new Double();
		strTemp = "";
		if ( distance >= 0.0f ) {
			tmp.set(distance);
			if (tmp.value >= 1){
				strTemp = MyLocale.formatDouble(tmp,"0.000")+ " km";
			}
			else {
				tmp.set(tmp.value * 1000);
				strTemp = tmp.toString(3,0,0) + " m";
			}
		}
		else strTemp = "--- km";
		g.drawText(strTemp, 2, lineHeight);
		
		tmp.set(gotoDir);
		if ((tmp.value <= 360) && (tmp.value >= -360))
			strTemp = tmp.toString(0,0,0) + " " + MyLocale.getMsg(1502,"deg");
		else strTemp = "---" + " " + MyLocale.getMsg(1502,"deg");
		g.drawText(strTemp, 2, 2*lineHeight);
	}
	
	private void drawGpsData(Graphics g){
		g.setColor(RED);
		
		String strHeadline = MyLocale.getMsg(1501,"Current");
		
		Double tmp = new Double();

		tmp.set(m_speed);
		String strSpeed = "- km/h";
		if (m_speed >= 0) {
			if (m_speed >= 100) {
				strSpeed = MyLocale.formatDouble(tmp,"0") + " km/h";				
			}
			else {
				strSpeed = MyLocale.formatDouble(tmp,"0.0") + " km/h";
			}
		}
		
		tmp.set(moveDir);
		String strMoveDir = "---" + " " + MyLocale.getMsg(1502,"deg");
		if ((tmp.value <= 360) && (tmp.value >= -360))
			strMoveDir = tmp.toString(0,0,0) + " " + MyLocale.getMsg(1502,"deg");

		int textWidth = java.lang.Math.max(fm.getTextWidth(strSpeed), fm.getTextWidth(strMoveDir));
		textWidth = java.lang.Math.max(textWidth, fm.getTextWidth(strHeadline));
		
		int startX = location.width - (textWidth + 4);
		g.fillRect(startX, 0, location.width - startX, lineHeight);
		
		g.setColor(Color.Black);		
		g.drawText(strHeadline, startX + 2, 0);		
		g.drawText(strSpeed, startX + 2, lineHeight);
		g.drawText(strMoveDir, startX + 2, 2*lineHeight);
	}
	
	private void drawLuminaryData(Graphics g){
		g.setColor(YELLOW);

		String strSunDir = "---" + " " + MyLocale.getMsg(1502,"deg");
		if (sunDir < 360 && sunDir > -360) {
			Double tmp = new Double();
			tmp.set(sunDir);
			strSunDir = tmp.toString(0,0,0) + " " + MyLocale.getMsg(1502,"deg");
		}

		int textWidth = java.lang.Math.max(fm.getTextWidth(m_Luminary), fm.getTextWidth(strSunDir));
		int startY = location.height - 2*lineHeight;
		g.fillRect(0, startY, textWidth + 4, location.height - startY);

		g.setColor(Color.Black);		
		g.drawText(m_Luminary, 2, startY);
		g.drawText(strSunDir, 2, startY + lineHeight);
	}
	
	private void drawGpsStatus(Graphics g){
		if ((m_fix > 0) && (m_sats >= 0)) {
			// Set background to signal quality
			g.setColor(GREEN);
		}
		else
		// receiving data, but signal ist not good
		if ((m_fix == 0) && (m_sats >= 0)) {
			g.setColor(YELLOW);
		}
		else {
			g.setColor(RED);
		}

		String strSats = "Sats: -";
		if (m_sats >= 0) {
			strSats = "Sats: " + Convert.toString(m_sats) + "/" + Convert.toString(m_satsInView);
		}
		String strHdop = "HDOP: -";
		if (m_hdop >= 0) strHdop = "HDOP: " + Convert.toString(m_hdop);

		int textWidth = java.lang.Math.max(fm.getTextWidth(strSats), fm.getTextWidth(strHdop));
		int startX = location.width - (textWidth + 4);
		int startY = location.height - 2*lineHeight;
		g.fillRect(startX, startY, location.width - startX ,location.height - startY);

		g.setColor(Color.Black);
		g.drawText(strSats, startX + 2, startY);
		g.drawText(strHdop, startX + 2, startY + lineHeight);
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
				if (diff > 180.0f)
				{
					diff = 360.0f - diff;
				}
				
				if (diff <= 12.25f)
				{
					moveDirColor = GREEN;
				}
				else if (diff <= 22.5f)
				{
					moveDirColor = CYAN;
				}
				else if (diff <= 45.0f)
				{
					moveDirColor = ORANGE;
				}
				else if (diff <= 90.0f)
				{
					moveDirColor = MAGENTA;
				}
			}

			// draw only valid arrows
			if (northCentered) {
				if (gotoDir < 360 && gotoDir > -360) drawThickArrow(g, gotoDir, Color.DarkBlue, 1.0f);
				if (moveDir < 360 && moveDir > -360) drawThinArrow(g, moveDir, RED, moveDirColor, 1.0f);
				if (sunDir < 360 && sunDir > -360) drawSunArrow(g, sunDir, YELLOW, 0.75f);
			}
			else {
				//moveDir centered
				int radius = (int)((float)(java.lang.Math.min(location.width, location.height) / 2) * 0.75f);

				g.setPen(new Pen(new Color(150,150,150),Pen.SOLID,3));
				g.drawEllipse(location.width/2 - radius, location.height/2 - radius, 2 * radius, 2 * radius );

				if (moveDir < 360 && moveDir > -360) {
					//drawDoubleArrow(g, 360 - moveDir, BLUE, new Color(175,0,0), 1.0f);
					drawRose(g, 360 - moveDir, new Color(100,100,100), new Color(200,200,200), 1.0f);
					
					g.setPen(new Pen(RED,Pen.SOLID,3));
					g.drawLine(location.width/2, location.height/2 - radius, location.width/2, location.height/2 + radius);
					
					if (gotoDir < 360 && gotoDir > -360) drawThinArrow(g, gotoDir - moveDir, Color.DarkBlue, moveDirColor, 1.0f);
					if (sunDir < 360 && sunDir > -360) drawSunArrow(g, sunDir - moveDir, YELLOW, 0.75f);					
				}				
			}
		}
	}

	/**
	 * draw single arrow 
	 * @param g handle for drawing
	 * @param angle angle of arrow
	 * @param col color of arrow
	 */
	private void drawSimpleArrow(Graphics g, float angle, Color col, float scale) {
		float angleRad;
		int x, y, centerX = location.width/2, centerY = location.height/2;
		int arrowLength = java.lang.Math.min(centerX, centerY); 

		angleRad = (angle) * (float)java.lang.Math.PI / 180;
		x = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad) * scale).intValue();
		y = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad) * scale).intValue();
		g.setPen(new Pen(col,Pen.SOLID,3));
		g.drawLine(centerX,centerY,x,y);
	}
	
	private void drawSunArrow(Graphics g, float angle, Color col, float scale) {
		float angleRad = (angle) * (float)java.lang.Math.PI / 180;
		int centerX = location.width/2, centerY = location.height/2;
		float arrowLength = (float)java.lang.Math.min(centerX, centerY) * scale;
		float halfArrowWidth = arrowLength * 0.08f;
		float circlePos = arrowLength * 0.7f;
		int circleRadius = (int)(arrowLength * 0.1f);

		int pointX = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		int pointY = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		int circleX = centerX + new Float(circlePos * java.lang.Math.sin(angleRad)).intValue();
		int circleY = centerY - new Float(circlePos * java.lang.Math.cos(angleRad)).intValue();

		int[] pointsX = new int[4];
		int[] pointsY = new int[4];

		pointsX[0] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		pointsY[0] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		pointsX[1] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsY[1] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsX[2] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI)).intValue();
		pointsY[2] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI)).intValue();
		pointsX[3] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 2.0)).intValue();
		pointsY[3] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 2.0)).intValue();
		
//		g.setPen(new Pen(col,Pen.SOLID,3));
//		g.drawLine(centerX,centerY,pointX,pointY);
		
		g.setPen(new Pen(Color.Black,Pen.SOLID,1));
		g.setBrush(new Brush(col, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 4);
		g.fillEllipse(circleX - circleRadius, circleY - circleRadius, 2 * circleRadius, 2 * circleRadius);
	}
	
	private void drawThinArrow(Graphics g, float angle, Color col, Color colPoint, float scale) {
		float angleRad = (angle) * (float)java.lang.Math.PI / 180;
		int centerX = location.width/2, centerY = location.height/2;
		float arrowLength = (float)java.lang.Math.min(centerX, centerY) * scale;
		float halfOpeningAngle = (float)(java.lang.Math.PI * 0.03);
		float sideLineLength = arrowLength * 0.75f;
		
		int[] pointsX = new int[4];
		int[] pointsY = new int[4];

		pointsX[0] = centerX + new Float(sideLineLength * java.lang.Math.sin(angleRad - halfOpeningAngle)).intValue();
		pointsY[0] = centerY - new Float(sideLineLength * java.lang.Math.cos(angleRad - halfOpeningAngle)).intValue();
		pointsX[1] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		pointsY[1] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		pointsX[2] = centerX + new Float(sideLineLength * java.lang.Math.sin(angleRad + halfOpeningAngle)).intValue();
		pointsY[2] = centerY - new Float(sideLineLength * java.lang.Math.cos(angleRad + halfOpeningAngle)).intValue();
		pointsX[3] = centerX;
		pointsY[3] = centerY;
		
		g.setPen(new Pen(Color.Black,Pen.SOLID,1));
		g.setBrush(new Brush(col, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 4);
		if (colPoint != null) {
			g.setBrush(new Brush(colPoint, Brush.SOLID));
			g.fillPolygon(pointsX, pointsY, 3);			
		}
	}
	
	private void drawDoubleArrow(Graphics g, float angle, Color colFront, Color colRear, float scale) {
		float angleRad = (angle) * (float)java.lang.Math.PI / 180;
		int centerX = location.width/2, centerY = location.height/2;
		float arrowLength = (float)java.lang.Math.min(centerX, centerY) * scale;
		float halfArrowWidth = arrowLength * 0.1f;
		
		int[] pointsX = new int[3];
		int[] pointsY = new int[3];

		pointsX[0] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		pointsY[0] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		pointsX[1] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsY[1] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsX[2] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 2.0)).intValue();
		pointsY[2] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 2.0)).intValue();
		
		g.setPen(new Pen(Color.Black,Pen.SOLID,1));
		g.setBrush(new Brush(colFront, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 3);
		
		pointsX[0] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad + java.lang.Math.PI)).intValue();
		pointsY[0] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad + java.lang.Math.PI)).intValue();
		
		g.setBrush(new Brush(colRear, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 3);
	}
	
	private void drawRose(Graphics g, float angle, Color colFront, Color colRear, float scale) {
		float angleRad = (angle) * (float)java.lang.Math.PI / 180;
		int centerX = location.width/2, centerY = location.height/2;
		float arrowLength = (float)java.lang.Math.min(centerX, centerY) * scale;
		float halfArrowWidth = arrowLength * 0.12f;
		
		int[] pointsX = new int[8];
		int[] pointsY = new int[8];

		pointsX[0] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 4.0)).intValue();
		pointsY[0] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 4.0)).intValue();
		pointsX[1] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		pointsY[1] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		pointsX[2] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 4.0)).intValue();
		pointsY[2] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 4.0)).intValue();
		pointsX[3] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsY[3] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsX[4] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + 3.0 * java.lang.Math.PI / 4.0)).intValue();
		pointsY[4] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + 3.0 * java.lang.Math.PI / 4.0)).intValue();
		pointsX[5] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad + java.lang.Math.PI)).intValue();
		pointsY[5] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad + java.lang.Math.PI)).intValue();
		pointsX[6] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad - 3.0 * java.lang.Math.PI / 4.0)).intValue();
		pointsY[6] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad - 3.0 * java.lang.Math.PI / 4.0)).intValue();
		pointsX[7] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad - java.lang.Math.PI / 2.0)).intValue();
		pointsY[7] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad - java.lang.Math.PI / 2.0)).intValue();
		
		g.setPen(new Pen(colRear,Pen.SOLID,1));
		g.setBrush(new Brush(colRear, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 8);
		
		g.setBrush(new Brush(colFront, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 3);
	}
	
	private void drawThickArrow(Graphics g, float angle, Color col, float scale) {
		float angleRad = (angle) * (float)java.lang.Math.PI / 180;
		int centerX = location.width/2, centerY = location.height/2;
		float arrowLength = (float)java.lang.Math.min(centerX, centerY) * scale;
		float halfArrowWidth = arrowLength * 0.1f;
		
		int[] pointsX = new int[4];
		int[] pointsY = new int[4];

		pointsX[0] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		pointsY[0] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		pointsX[1] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsY[1] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsX[2] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI)).intValue();
		pointsY[2] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI)).intValue();
		pointsX[3] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 2.0)).intValue();
		pointsY[3] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 2.0)).intValue();
		
		g.setPen(new Pen(Color.Black,Pen.SOLID,1));
		g.setBrush(new Brush(col, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 4);
	}
	
	public void setNorthCentered(boolean nc) {
		northCentered = nc;
		if (northCentered != Global.getPref().northCenteredGoto) {
			Global.getPref().northCenteredGoto = northCentered;
			Global.getPref().savePreferences();
		}
		refresh();
	}
	
	public boolean isNorthCentered() {
		return northCentered;
	}
}
